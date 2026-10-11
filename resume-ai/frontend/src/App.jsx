import { useEffect, useState } from "react";
import { api, getToken, setSession, clearSession } from "./api.js";

/* ---------- Auth screen ---------- */
function AuthScreen({ onDone }) {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const res = mode === "login" ? await api.login(email, password) : await api.register(email, password);
      setSession(res.token, res.email);
      onDone();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth">
      <div className="auth-card">
        <h1>Resume<mark>Fit</mark></h1>
        <p className="lede">See how well your resume matches a job, and what to change before you apply.</p>
        <form onSubmit={submit}>
          <label>Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
          </label>
          <label>Password
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8}
              autoComplete={mode === "login" ? "current-password" : "new-password"} />
          </label>
          {error && <p className="error" role="alert">{error}</p>}
          <button className="primary" disabled={busy}>
            {busy ? "Please wait…" : mode === "login" ? "Log in" : "Create account"}
          </button>
        </form>
        <button className="link" onClick={() => { setMode(mode === "login" ? "register" : "login"); setError(""); }}>
          {mode === "login" ? "New here? Create an account" : "Already have an account? Log in"}
        </button>
      </div>
    </main>
  );
}

/* ---------- New analysis form ---------- */
function AnalyzeForm({ onResult }) {
  const [file, setFile] = useState(null);
  const [jobTitle, setJobTitle] = useState("");
  const [jobDescription, setJobDescription] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    if (!file) return setError("Choose your resume as a PDF.");
    setBusy(true);
    setError("");
    try {
      onResult(await api.analyze(file, jobTitle, jobDescription));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="panel" onSubmit={submit}>
      <h2>Check a resume against a job</h2>
      <label className="drop">
        <span>{file ? file.name : "Choose your resume (PDF, up to 5 MB)"}</span>
        <input type="file" accept="application/pdf" onChange={(e) => setFile(e.target.files[0] || null)} />
      </label>
      <label>Job title
        <input value={jobTitle} onChange={(e) => setJobTitle(e.target.value)} required placeholder="Backend Developer" />
      </label>
      <label>Job description
        <textarea rows={9} value={jobDescription} onChange={(e) => setJobDescription(e.target.value)} required
          placeholder="Paste the full job description here" />
      </label>
      {error && <p className="error" role="alert">{error}</p>}
      <button className="primary" disabled={busy}>{busy ? "Analyzing… this takes about 15 seconds" : "Analyze resume"}</button>
    </form>
  );
}

/* ---------- Result view ---------- */
function Result({ item, onDelete }) {
  let r = {};
  try { r = JSON.parse(item.result); } catch { /* ignore */ }
  const tone = item.matchScore >= 75 ? "good" : item.matchScore >= 50 ? "mid" : "low";

  return (
    <div className="panel result">
      <div className="result-head">
        <div>
          <h2>{item.jobTitle}</h2>
          <p className="muted">{item.fileName} · {new Date(item.createdAt).toLocaleString()}</p>
        </div>
        <button className="link danger" onClick={() => onDelete(item.id)}>Delete</button>
      </div>

      <div className="score">
        <strong className={tone}>{item.matchScore}<small>/100</small></strong>
        <div className="bar" aria-hidden="true"><i className={tone} style={{ width: `${item.matchScore}%` }} /></div>
      </div>
      <p>{r.summary}</p>

      <div className="cols">
        <section>
          <h3>What already works</h3>
          <ul>{(r.strengths || []).map((s, i) => <li key={i}>{s}</li>)}</ul>
        </section>
        <section>
          <h3>Skills to add or show</h3>
          <div className="chips">{(r.missingSkills || []).map((s, i) => <span key={i}>{s}</span>)}</div>
        </section>
      </div>

      <h3>Changes to make</h3>
      <ul className="fixes">
        {(r.improvements || []).map((x, i) => (
          <li key={i}><b>{x.section}</b><span>{x.suggestion}</span></li>
        ))}
      </ul>

      <h3>Suggested summary for this job</h3>
      <blockquote>{r.rewrittenSummary}</blockquote>
      <button className="link" onClick={() => navigator.clipboard.writeText(r.rewrittenSummary || "")}>Copy summary</button>
    </div>
  );
}

/* ---------- Main app ---------- */
export default function App() {
  const [authed, setAuthed] = useState(!!getToken());
  const [history, setHistory] = useState([]);
  const [current, setCurrent] = useState(null); // null = show the form

  const refresh = () => api.list().then(setHistory).catch(() => {});
  useEffect(() => { if (authed) refresh(); }, [authed]);

  if (!authed) return <AuthScreen onDone={() => setAuthed(true)} />;

  async function open(id) {
    setCurrent(await api.get(id));
  }
  async function remove(id) {
    await api.remove(id);
    setCurrent(null);
    refresh();
  }

  return (
    <div className="shell">
      <aside>
        <h1 className="brand">Resume<mark>Fit</mark></h1>
        <button className="primary" onClick={() => setCurrent(null)}>New analysis</button>
        <nav aria-label="Past analyses">
          {history.length === 0 && <p className="muted">Your past analyses will appear here.</p>}
          {history.map((h) => (
            <button key={h.id} className={"hist" + (current?.id === h.id ? " active" : "")} onClick={() => open(h.id)}>
              <span>{h.jobTitle}</span><b>{h.matchScore}</b>
            </button>
          ))}
        </nav>
        <button className="link" onClick={() => { clearSession(); setAuthed(false); }}>
          Log out ({localStorage.getItem("email")})
        </button>
      </aside>
      <main>
        {current
          ? <Result item={current} onDelete={remove} />
          : <AnalyzeForm onResult={(r) => { setCurrent(r); refresh(); }} />}
      </main>
    </div>
  );
}
