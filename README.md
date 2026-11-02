# Job Tracker 🎯

A full-stack job application tracking system built with **Spring Boot** and **React**.

Track your job applications through stages (Applied → Assessment → Interview → Offer/Rejected), manage interviews, search and filter, and view analytics on your job search.

## Tech Stack

| Layer      | Technology                                       |
|------------|--------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.5, Spring Data JPA, Maven |
| Database   | PostgreSQL 16 with Flyway migrations             |
| Frontend   | React (JavaScript), Vite, React Router, Axios    |
| Auth       | Spring Security + JWT                            |
| Testing    | JUnit 5 + Mockito                                |
| DevOps     | Docker + docker-compose                          |

## Prerequisites

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ and npm (for frontend; React Router 7 requires Node 20+)

## Quick Start

### 1. Configure local environment

Copy the root environment template and replace the example database password and JWT
secret with local-only values. Keep `.env` private; it is git-ignored.

```bash
copy .env.example .env
```

The JWT secret must be at least 32 characters. The values in `.env.example` are examples,
not suitable for deployment.

### 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

### 3. Configure and run the backend

Copy the local JWT configuration, which is also git-ignored:

```bash
copy backend\src\main\resources\application-local.yml.example backend\src\main\resources\application-local.yml
```

Run the backend from PowerShell. Set `JAVA_HOME` if Java 17 is not already configured:

```bash
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
cd backend
.\mvnw.cmd spring-boot:run
```

If you changed `DB_PASSWORD` in `.env`, set the same value in the shell before starting the
backend (Spring Boot does not load the root `.env` automatically). Also set
`$env:JWT_SECRET` to the secret you chose; this overrides the example in the local YAML file.

### 4. Run the frontend

```powershell
cd frontend
Copy-Item .env.example .env
npm ci
npm run dev
```

Open `http://localhost:5173`. The default frontend API URL is `http://localhost:8080/api`.

### 5. Verify

```powershell
curl.exe http://localhost:8080/api/health
curl.exe http://localhost:8080/api/health/db
```

## Deploy: Vercel frontend + Render API

The repository includes `frontend/vercel.json` for React Router deep links and a root
`render.yaml` Blueprint for the Java 17 API and PostgreSQL database.

### Important free-tier limits

This configuration uses Render's free web-service and PostgreSQL plans for a demo:

- The API may sleep after inactivity; its first request afterward can take about a minute.
- Free PostgreSQL expires 30 days after creation. Upgrade it or export/restore your data
  before expiry; after the additional grace period, Render deletes the database.
- The backend filesystem is temporary on the free plan. Uploaded PDFs can disappear after
  restarts, deploys, or sleep. Do not rely on free-tier attachment storage for important
  documents.
- Interview email reminders are disabled by default.

### 1. Push the project to a private GitHub repository

Create a **private** GitHub repository and push this project. Do not commit `.env`,
`application-local.yml`, SMTP credentials, or any production secrets. The repository must
include `render.yaml` at its root.

### 2. Deploy the backend and database on Render

In the Render Dashboard, create a **Blueprint** from the GitHub repository and accept the
services defined in `render.yaml`. The Blueprint generates a JWT secret and wires the API
to its PostgreSQL database. The database is created empty; Flyway applies the migrations
when the API starts. If Render prompts for `CORS_ALLOWED_ORIGINS`, use
`http://localhost:5173` temporarily; replace it with the Vercel production origin in step 4.

After creation, note the API's public URL, for example
`https://job-tracker-api.onrender.com`.

### 3. Deploy the frontend on Vercel

Import the same GitHub repository into Vercel and set **Root Directory** to `frontend`.
Vercel detects Vite; use `npm run build` and `dist` if prompted. Add this environment
variable for the Production environment before deploying:

```text
VITE_API_BASE_URL=https://YOUR-RENDER-API-URL.onrender.com/api
```

Replace the example host with the API URL from Render. This URL is public configuration,
not a secret. Deploy the site and note its exact production origin, such as
`https://your-job-tracker.vercel.app`.

### 4. Allow the Vercel site to call the API

In Render, open the API service's environment settings and set
`CORS_ALLOWED_ORIGINS` to the exact Vercel origin (scheme and host only, with no `/` path),
for example:

```text
https://your-job-tracker.vercel.app
```

Save the change and let Render redeploy. If using a custom frontend domain, include that
origin instead. Do not use `*` for the allowed origin.

### 5. Verify the deployment

- Open the Vercel URL and register a test account.
- Check `https://YOUR-RENDER-API-URL.onrender.com/api/health` and
  `https://YOUR-RENDER-API-URL.onrender.com/api/health/db`.
- Add a test application, then confirm it remains after the API restarts. Do not upload
  important PDFs on the free plan.

For lasting production data and attachments, upgrade the Render database and API service,
attach a persistent disk for `/app/uploads`, and configure backups before inviting users.

## Project Structure

```
job-tracker/
├── backend/                    # Spring Boot application
│   └── src/main/java/com/jobtracker/
│       ├── controller/         # REST endpoints (HTTP layer)
│       ├── service/            # Business logic
│       ├── repository/         # Database access (Spring Data JPA)
│       ├── entity/             # JPA entities (DB table mappings)
│       ├── dto/                # Data Transfer Objects (API contracts)
│       ├── security/           # JWT auth filters and config
│       ├── exception/          # Global error handling
│       └── config/             # App configuration beans
├── frontend/                   # React + Vite application
│   └── src/
│       ├── api/                # Axios HTTP client setup
│       ├── context/            # React Context providers
│       ├── pages/              # Page-level components
│       ├── components/         # Reusable UI components
│       ├── utils/              # Helper functions
│       └── styles/             # CSS files
├── docker-compose.yml          # PostgreSQL, backend, and frontend containers
├── .env.example                # Template for environment variables
└── README.md
```

## Environment Variables

Copy `.env.example` to `.env` and adjust as needed:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `jobtracker` | Database name |
| `DB_USERNAME` | `jobtracker` | Database user |
| `DB_PASSWORD` | `jobtracker` | Database password |
| `SERVER_PORT` | `8080` | Backend server port |
| `JWT_SECRET` | *(required)* | HMAC key for JWTs (32+ chars) |
| `JWT_EXPIRATION_MS` | `86400000` | Access token lifetime (24 hours) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |
| `ATTACHMENT_STORAGE_PATH` | `uploads` | Directory for uploaded PDFs |
| `ATTACHMENT_MAX_SIZE_BYTES` | `10485760` | Maximum PDF upload size in bytes |
| `ATTACHMENT_MAX_SIZE` | `10MB` | Maximum individual multipart file size |
| `ATTACHMENT_MAX_REQUEST_SIZE` | `11MB` | HTTP multipart size, including request overhead |
| `REMINDERS_ENABLED` | `false` | Enable scheduled interview email reminders |
| `REMINDER_CRON` | `0 0 * * * *` | Reminder schedule (Spring cron expression) |
| `SMTP_HOST`, `SMTP_PORT` | `localhost`, `1025` | SMTP server |
| `SMTP_USERNAME`, `SMTP_PASSWORD` | *(empty)* | SMTP credentials |
| `SMTP_AUTH`, `SMTP_STARTTLS` | `false` | SMTP authentication and STARTTLS |
| `MAIL_FROM` | `no-reply@jobtracker.local` | From address for reminders |

## Development Phases

- [x] Phase 1: Project setup, PostgreSQL, health endpoints
- [x] Phase 2: Entities, repositories, Flyway migrations
- [x] Phase 3: Application CRUD APIs
- [x] Phase 4: JWT authentication
- [x] Phase 5: Status history, interviews, search/filter
- [x] Phase 6: Dashboard and analytics
- [x] Phase 7: React frontend
- [x] Phase 8: Attachments and email reminders
- [x] Phase 9: Tests, Docker, deployment

## Phase 9: Tests, Docker, and deployment

### Run tests and builds

Run backend unit tests (JUnit 5 + Mockito) and build the frontend bundle:

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm ci
npm run build
npm audit
```

The project targets Java 17 to match the available development JDK. The service tests cover
application creation and status history, user ownership checks, authentication behavior,
interview reminder reset when rescheduled, PDF validation and storage, dashboard rates, and
analytics conversion calculations.

### Run the complete stack with Docker Compose

Copy `.env.example` to `.env` and set unique `DB_PASSWORD` and `JWT_SECRET` values before
starting containers. `JWT_SECRET` must have at least 32 characters. Then run:

```powershell
Copy-Item .env.example .env
# Edit .env and replace the example DB_PASSWORD and JWT_SECRET before continuing.
docker compose up --build -d
docker compose ps
```

Open `http://localhost:3000`. Nginx serves the compiled React application and proxies `/api`
to Spring Boot. The backend waits for PostgreSQL's health check; the frontend waits for the
backend health check. Flyway applies database migrations automatically at backend startup.
The `postgres_data` and `attachments_data` named volumes preserve database content and
uploaded PDFs when containers are recreated.

Useful local commands:

```powershell
docker compose logs -f backend
docker compose logs -f frontend
docker compose down
```

`docker compose down` preserves named volumes. Do not use `docker compose down -v` unless you
intend to permanently delete the local database and uploaded files.

### Deployment notes

- Build and run the containers behind a TLS-terminating reverse proxy; do not expose
  PostgreSQL publicly. The Compose port bindings are loopback-only for local use.
- Replace all example credentials with high-entropy secrets from the deployment platform's
  secret manager. Do not store production secrets in a committed `.env` file.
- Set a persistent, access-controlled attachment volume and include it in backup/restore
  procedures alongside PostgreSQL. Restrict uploaded file access to the application service.
- Configure SMTP credentials, sender address, and `REMINDERS_ENABLED=true` only when a real
  SMTP provider is available. Use a sandbox for non-production testing.
- Set `CORS_ALLOWED_ORIGINS` to the actual browser origins. If frontend and API are served
  through the Compose Nginx proxy on the same origin, no extra browser origin is needed.
- Back up PostgreSQL before deploying schema changes. Flyway migrations run on application
  startup; review migration compatibility before rolling out multiple backend replicas.
- Configure platform health monitoring against `/api/health` and `/api/health/db`, and review
  application logs for failed email delivery and file-storage errors.

## Phase 5 API

All application endpoints below require `Authorization: Bearer <token>`. Application and
interview lookups are scoped to the authenticated user; an ID belonging to another user
returns 404.

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/applications` | Paginated list with optional filters and sorting |
| `PATCH` | `/api/applications/{id}/status` | Change status and add a timestamped history row |
| `GET` | `/api/applications/{id}/status-history` | Read the status timeline, newest first |
| `GET` | `/api/applications/{id}/interviews` | List interviews for an application |
| `POST` | `/api/applications/{id}/interviews` | Add an interview |
| `PUT` | `/api/applications/{id}/interviews/{interviewId}` | Update an interview |
| `DELETE` | `/api/applications/{id}/interviews/{interviewId}` | Delete an interview |

List filters are optional: `company` and `role` perform case-insensitive partial matching;
`status` is one of `APPLIED`, `ASSESSMENT`, `INTERVIEW`, `OFFER`, or `REJECTED`;
`fromDate` and `toDate` use `yyyy-MM-dd` and filter `dateApplied`. Pagination uses
zero-based `page`, `size` (1–100), `sortBy`, and `sortDir` (`asc` or `desc`).

Example:

```text
GET /api/applications?company=Acme&status=INTERVIEW&fromDate=2026-01-01&page=0&size=10&sortBy=dateApplied&sortDir=desc
```

Changing an application's status through the dedicated endpoint updates the current status
and appends a row to `status_history` in the same transaction. The original `APPLIED`
entry is created along with the application. Sending its current status again is a no-op,
so it does not create duplicate history entries.

### Database relationships

The Flyway migration `V1__create_schema.sql` defines the current tables:

- `users` has many `applications`; each application belongs to one user.
- `applications` has many `status_history` entries, `interviews`, and `attachments`.
- `status_history` is an append-only timeline: a status transition creates a new row.
- `interviews` stores separate interview rounds with their own dates, types, and notes.
- `attachments` stores file metadata and a disk path; the file contents are not in PostgreSQL.

Each child table references `applications.id` with a foreign key and cascades on application
deletion. The application API scopes reads and writes by the authenticated user's ID.

## Phase 6 API

Both endpoints require the authenticated user's JWT and return only that user's data:

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/dashboard` | Current status totals, this week's upcoming interviews, offer and response rates |
| `GET` | `/api/analytics` | Monthly application counts, stage conversions, and average time per stage |

Dashboard rates use the user's total applications as the denominator. The offer rate counts
applications that have ever reached `OFFER`; the response rate counts applications that have
ever progressed beyond `APPLIED`. The upcoming interview list includes interviews from today
through Sunday of the current calendar week.

Analytics are calculated from the user's application and status-history records. Monthly
counts group applications by `date_applied`. A stage conversion is the number of applications
that moved from the previous stage to the next one (in that order) divided by those that
ever reached the previous stage (`APPLIED` → `ASSESSMENT` → `INTERVIEW` → `OFFER`). Average
days per stage uses the time between each status-history event; for applications still in a
stage, it measures from the latest event until now. Rates and duration averages are rounded
to two decimal places.

## Phase 7: Frontend

The React/Vite frontend is JavaScript-only and uses React Router for navigation and Axios
for API requests. It includes registration/login, an application table with server-side
filters and pagination, create/edit forms, a drag-and-drop pipeline (with a select control
for keyboard-accessible stage changes), and a dashboard with inline SVG/CSS charts. JWTs are
stored in browser local storage and attached to API requests; logout clears the client token.

Run the backend and PostgreSQL as described above, then in a second terminal:

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Open `http://localhost:5173`. Set `VITE_API_BASE_URL` in `frontend/.env` if the backend is
not at `http://localhost:8080/api`. The API's `CORS_ALLOWED_ORIGINS` must include the
frontend origin. To verify a production bundle, run `npm run build` in `frontend`.

## Phase 8: Attachments and interview reminders

### PDF attachments

Edit an application and upload a `RESUME` or `JOB_DESCRIPTION` PDF. The backend checks the
`.pdf` filename, PDF signature, and size (10 MB by default), then stores the file in the
configured directory using a generated filename. PostgreSQL stores only the metadata and
generated storage filename. Users can list and download documents only for their own
applications.

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/applications/{id}/attachments` | List a user's documents for an application |
| `POST` | `/api/applications/{id}/attachments?type=RESUME` | Upload a PDF as multipart field `file` |
| `GET` | `/api/applications/{id}/attachments/{attachmentId}/download` | Download a PDF |

Set `ATTACHMENT_STORAGE_PATH` to choose the disk directory, `ATTACHMENT_MAX_SIZE_BYTES` for
the application-level limit, `ATTACHMENT_MAX_SIZE` for the per-file HTTP limit, and
`ATTACHMENT_MAX_REQUEST_SIZE` for the full multipart request limit.
The default storage directory is `uploads` relative to the backend's working directory;
keep it persistent and backed up in deployments.

### Email reminders

Reminders are disabled unless `REMINDERS_ENABLED=true`. When enabled, the scheduler checks
hourly (configurable with `REMINDER_CRON`) for interviews in the next 24 hours using the
backend server's local time zone and emails the account email on file. A successful send is
timestamped in `interviews.reminder_sent_at`, so later scheduler runs do not send the same
reminder again. Rescheduling an interview clears that timestamp so a reminder can be sent for
its new time. Failed sends are logged and left unmarked.

For local testing, use an SMTP sandbox such as Mailtrap and configure its values in
`backend/src/main/resources/application-local.yml` or the environment, never in source
control:

```yaml
app:
  reminders:
    enabled: true
    cron: "0 */5 * * * *"
    from: "jobtracker@example.test"
spring:
  mail:
    host: "sandbox.smtp.mailtrap.io"
    port: 2525
    username: "${SMTP_USERNAME}"
    password: "${SMTP_PASSWORD}"
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

Set `SMTP_USERNAME` and `SMTP_PASSWORD` in the environment before launching the backend.
The five-minute cron is useful for testing; use the default hourly schedule for normal local
development. A Flyway migration adds the reminder timestamp column.
