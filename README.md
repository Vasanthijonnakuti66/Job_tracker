# Job Tracker 🎯

A full-stack job application tracking system built with **Spring Boot** and **React**.

Track your job applications through stages (Applied → Assessment → Interview → Offer/Rejected), manage interviews, search and filter, and view analytics on your job search.

## Tech Stack

| Layer      | Technology                                       |
|------------|--------------------------------------------------|
| Backend    | Java 21, Spring Boot 3.5, Spring Data JPA, Maven |
| Database   | PostgreSQL 16 with Flyway migrations             |
| Frontend   | React (JavaScript), Vite, React Router, Axios    |
| Auth       | Spring Security + JWT                            |
| Testing    | JUnit 5 + Mockito                                |
| DevOps     | Docker + docker-compose                          |

## Prerequisites

- JDK 21+ (JDK 24 works — it compiles down to Java 21 bytecode)
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ and npm (for frontend)

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d postgres
```

### 2. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

### 3. Verify

```bash
# App health
curl http://localhost:8080/api/health

# Database health
curl http://localhost:8080/api/health/db
```

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
├── docker-compose.yml          # PostgreSQL + app containers
├── .env.example                # Template for environment variables
└── README.md
```

## Environment Variables

Copy `.env.example` to `.env` and adjust as needed:

| Variable      | Default      | Description             |
|---------------|--------------|-------------------------|
| `DB_HOST`     | `localhost`  | PostgreSQL host         |
| `DB_PORT`     | `5432`       | PostgreSQL port         |
| `DB_NAME`     | `jobtracker` | Database name           |
| `DB_USERNAME` | `jobtracker` | Database user           |
| `DB_PASSWORD` | `jobtracker` | Database password       |
| `SERVER_PORT` | `8080`       | Backend server port     |

## Development Phases

- [x] Phase 1: Project setup, PostgreSQL, health endpoints
- [ ] Phase 2: Entities, repositories, Flyway migrations
- [ ] Phase 3: Application CRUD APIs
- [ ] Phase 4: JWT authentication
- [ ] Phase 5: Status history, interviews, search/filter
- [ ] Phase 6: Dashboard and analytics
- [ ] Phase 7: React frontend
- [ ] Phase 8: Attachments and email reminders
- [ ] Phase 9: Tests, Docker, deployment
