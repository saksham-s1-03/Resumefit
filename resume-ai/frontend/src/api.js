const BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const getToken = () => localStorage.getItem("token");
export const setSession = (token, email) => {
  localStorage.setItem("token", token);
  localStorage.setItem("email", email);
};
export const clearSession = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("email");
};

async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(BASE + path, { ...options, headers });

  if (res.status === 401 && token) {
    clearSession();
    window.location.reload();
  }
  if (res.status === 204) return null;

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || "Something went wrong. Please try again.");
  return data;
}

const json = (body) => ({
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(body),
});

export const api = {
  register: (email, password) => request("/api/auth/register", json({ email, password })),
  login: (email, password) => request("/api/auth/login", json({ email, password })),
  list: () => request("/api/analyses"),
  get: (id) => request(`/api/analyses/${id}`),
  remove: (id) => request(`/api/analyses/${id}`, { method: "DELETE" }),
  analyze: (file, jobTitle, jobDescription) => {
    const form = new FormData();
    form.append("resume", file);
    form.append("jobTitle", jobTitle);
    form.append("jobDescription", jobDescription);
    return request("/api/analyses", { method: "POST", body: form });
  },
};
