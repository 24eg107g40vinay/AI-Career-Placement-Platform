# AI-Powered Career & Placement Intelligence Platform

A React and Spring Boot application for career planning, job matching, and placement workflows. It includes student, recruiter, and administrator workspaces; the platform is still being expanded and is not yet a finished production system.

## Current project layout

- `frontend/` — React and Vite application (port `5173`)
- `backend/` — Spring Boot REST API (port `8080`)
- `database/` — database scripts (being added with the database phase)
- `.env.example` — names of backend environment variables (no credentials)

## Prerequisites

- Node.js 20 or newer and npm
- Java 17 or newer
- Maven 3.9 or newer
- MySQL 8

The project has been checked with Node 24, npm 11, Java 26, Maven 3.9, and MySQL 8 on Windows.

## Configure the backend database

The MySQL Windows service must be running. The backend connects to a database named `career_platform`; Hibernate creates/updates its tables in the development profile.

The values in `.env.example` are a reference only: Spring Boot does **not** automatically load `.env` files. Use the Windows launcher below. It asks for your MySQL password without displaying it and generates a temporary JWT secret for local development:

```powershell
cd backend
.\run-local.ps1
```

The temporary signing secret changes each time the script runs, so local JWTs expire when you restart the server. Optional variables are listed in `.env.example`. In production, provide a persistent, securely stored `JWT_SECRET`, set `JPA_DDL_AUTO=validate`, and use a dedicated database user rather than the MySQL root account.

### Create and access the first admin account

Public sign-up cannot create administrator accounts. To create the first admin locally, stop any backend already listening on port `8080`, then run this from `backend/` in PowerShell:

```powershell
.\run-local.ps1 -BootstrapAdmin
```

Enter the admin name, email, and a password of at least 12 characters when prompted; the password prompt is hidden. The usual MySQL password prompt follows. The bootstrap creates an administrator only when none exists, and it never changes an existing account. Keep the backend running after startup.

Open `http://localhost:5173/login` and sign in with the admin email and password. You will be redirected to `/admin/dashboard`. The sidebar provides the dashboard, student and recruiter directories, companies, jobs, and applications. Admin actions include changing student/recruiter access, closing job listings, and updating application statuses.

The career assistant provides curated English, Hindi, and Spanish guidance about supported career and website workflows when `AI_API_KEY` is unset. For open-ended general answers and broad multilingual support, set `AI_API_KEY` in the environment used to start the backend; the assistant then follows the language of the latest question. `AI_API_URL` and `AI_MODEL` can optionally select a compatible chat-completions provider and model. Do not put live API keys in source files or commit them. The assistant sends at most the last eight chat turns to the provider and only shares profile education, graduation year, and listed skills as optional context.

## Run locally

### Backend

In the same PowerShell terminal where you set `DB_USERNAME` and `DB_PASSWORD`:

```powershell
cd backend
.\run-local.ps1
```

The launcher prompts for the database password and generates a temporary JWT secret when one is not supplied. For direct `mvn spring-boot:run` startup, set `DB_PASSWORD` and `JWT_SECRET` in the environment first.
The local launcher enables 18 development sample jobs only when the database has no active jobs. Disable this with `$env:SAMPLE_DATA_ENABLED = 'false'`; sample listings are never enabled by default in other environments.

The student dashboard loads the signed-in student's profile, applications, resumes, and recommendations. API documentation is served at `http://localhost:8080/swagger-ui.html` when the backend is running.

### Frontend

Open another PowerShell terminal:

```powershell
cd frontend
npm install
Copy-Item .env.example .env.local
npm run dev
```

Open `http://localhost:5173`. The API base URL can be changed in `frontend/.env.local` using `VITE_API_URL`; its default is `http://localhost:8080/api`.

## Tests

Run the React production build:

```powershell
cd frontend
npm run build
```

Run backend tests (persistence model tests use an embedded test database and do not need the MySQL password):

```powershell
cd backend
mvn test
```

## Current API

- `GET /` and `GET /api/health` — public minimal health checks; protected API routes still require authentication.
- The student dashboard aggregates the authenticated profile, applications, resumes, and recommendations endpoints.
- `POST /api/auth/register` — create a student or recruiter account
- `POST /api/auth/login` — verify credentials and receive a 24-hour JWT
- `GET /api/jobs/search` — database-backed, case-insensitive, pageable search; supports keyword, location, company, skill, employment type, experience, salary, date-posted, and sort filters
- `GET /api/jobs` — list active roles for existing platform workflows
- `GET /api/jobs/{id}` — read an open role
- `GET /api/students/profile` and `PUT /api/students/profile` — student profile, including persisted years of experience
- `/api/students/profile/skills`, `/api/students/resumes`, `/api/applications/my`, `/api/recommendations/jobs`, `/api/skill-gap/{jobId}`, and `/api/ai/chat` — student career tools
- `/api/recruiters/dashboard`, `/api/recruiters/company`, and `/api/jobs/mine` — recruiter workspace and job management
- `/api/applications/job/{jobId}` and `/api/applications/{applicationId}/status` — recruiter applicant review
- `/api/admin/dashboard`, `/api/admin/students`, `/api/admin/recruiters`, `/api/admin/companies`, `/api/admin/jobs`, and `/api/admin/applications` — administrator reporting and directory views
- `PATCH /api/admin/users/{id}` and `DELETE /api/admin/jobs/{id}` — administrator account and job moderation

Registration and login accept JSON. For example, register with:

```json
{
  "name": "Jamie Student",
  "email": "jamie@example.com",
  "password": "choose-a-password",
  "role": "STUDENT"
}
```

Login returns a bearer token and the public account fields. Protected API requests use `Authorization: Bearer <token>`. Passwords are BCrypt-hashed and public registration cannot create administrators.

The database domain model defines users and roles, student profiles, recruiters and companies, skills, jobs, resumes, and applications. The frontend includes public job search, student career tools, recruiter job/applicant management, and administrator directories. Some dashboard data remains illustrative, and production hardening, broader test coverage, and deployment guidance are still outstanding; do not treat the present build as the finished feature set yet.
