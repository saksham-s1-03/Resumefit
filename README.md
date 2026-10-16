# ResumeFit: AI Resume Analyzer

Upload a resume (PDF) and paste a job description. The app returns a 0-100 match score, strengths, missing skills,
specific changes to make, and a rewritten summary. Every analysis is saved to your account history.

**Stack:** Java 17, Spring Boot 3, Spring Security (JWT), JPA, H2 / PostgreSQL, Apache PDFBox, Claude API | React 18 + Vite

## Architecture

    React (Vite)  --JWT-->  Spring Boot REST API  -->  PDFBox (extract text)
                                   |                -->  Claude API (analysis JSON)
                                   +--> H2 (dev) / PostgreSQL (prod)

## Run locally

Prerequisites: JDK 17+, Maven, Node 18+, and an Anthropic API key (console.anthropic.com).

    # Terminal 1: backend
    cd backend
    export ANTHROPIC_API_KEY=your_key_here        # Windows PowerShell: $env:ANTHROPIC_API_KEY="your_key_here"
    export JWT_SECRET=any-long-random-string-32-chars-or-more
    mvn spring-boot:run

    # Terminal 2: frontend
    cd frontend
    npm install
    npm run dev            # open http://localhost:5173

## Run with Docker (backend + PostgreSQL)

    export ANTHROPIC_API_KEY=your_key_here
    export JWT_SECRET=any-long-random-string-32-chars-or-more
    docker compose up --build

## API

| Method | Path | Purpose |
|---|---|---|
| POST | /api/auth/register | Create account, returns JWT |
| POST | /api/auth/login | Log in, returns JWT |
| POST | /api/analyses | multipart: resume (PDF), jobTitle, jobDescription |
| GET | /api/analyses | List your analyses |
| GET | /api/analyses/{id} | One analysis with full result |
| DELETE | /api/analyses/{id} | Delete one |

## Security notes
- Passwords are hashed with BCrypt; sessions are stateless JWTs.
- Every query is scoped to the logged-in user's id, so users cannot read each other's data.
- The API key lives only on the server (env variable), never in the frontend.
- The prompt marks resume/job text as untrusted data to reduce prompt injection.

## Deploy
- Backend: Render or Railway (use the Dockerfile) + managed PostgreSQL. Set JWT_SECRET, ANTHROPIC_API_KEY, CORS_ORIGIN, SPRING_DATASOURCE_*.
- Frontend: Vercel or Netlify. Set VITE_API_URL to your backend URL, build command `npm run build`, output `dist`.

## Ideas to extend
1. Rate-limit analyses per user (Bucket4j) to control API cost.
2. Add Flyway migrations and switch `ddl-auto` to `validate`.
3. Stream the AI response to the UI (SSE).
4. Unit tests for AiService (mock RestClient) and an integration test for AuthController.
5. GitHub Actions: build and test on every push.
