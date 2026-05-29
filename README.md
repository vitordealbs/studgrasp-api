# 📚 StudGrasp API

> A REST API for **StudGrasp** — a Computer Science learning platform built around spaced repetition, interactive roadmaps, and advisor-led progress tracking.

*IMAGEM QUE AINDA VOU COLOCAR AQUI*

---

## 🧭 What is StudGrasp?

StudGrasp helps CS students learn more effectively by combining **spaced repetition flashcards** (SM-2 algorithm), **structured learning roadmaps**, and **real-time advisor dashboards**. Advisors can monitor student retention, identify weak topics, and receive AI-generated teaching insights.

This repository is the **Java Spring Boot backend** — one of three services:

```
studgrasp-frontend  (React + TypeScript)
        │
        │  HTTP REST  /api/v1/**
        ▼
studgrasp-api       (Java Spring Boot)  ◄── you are here
        │
        │  HTTP REST  (X-API-Key)
        ▼
studgrasp-ai        (Python FastAPI + Anthropic API)
                    roadmap scraper + AI flashcard generation
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT + API Key |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5 + Mockito |
| Build | Maven |
| Containers | Docker + Docker Compose |

---

## 🚀 Getting Started

### Prerequisites

- **Docker** and **Docker Compose**
- **Java 21+** (only needed if running outside Docker)

### 1. Clone the repository

```bash
git clone https://github.com/vitordealbs/studgrasp-api.git
cd studgrasp-api
```

### 2. Start with Docker Compose

```bash
docker compose up -d
```

This spins up the API, PostgreSQL, Redis, and PgAdmin. The API will be available at `http://localhost:8080`.

| Service | Port | Description |
|---|---|---|
| **API** | 8080 | Spring Boot REST API |
| **PostgreSQL** | 5432 | Main database |
| **Redis** | 6379 | Cache layer |
| **PgAdmin** | 5050 | Database UI |

### 3. Environment variables

All variables have safe development defaults built in. For production, create a `.env` file:

```env
JWT_SECRET=your-secret-min-32-chars
SCRAPER_API_KEY=your-scraper-api-key
CORS_ALLOWED_ORIGINS=https://yourdomain.com
DB_URL=jdbc:postgresql://localhost:5432/studgrasp
DB_USER=studgrasp
DB_PASS=studgrasp
AI_API_URL=http://studgrasp-ai:8000
```

### 4. Verify the setup

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### 5. Explore the API docs

```
http://localhost:8080/swagger-ui.html
```

*IMAGEM QUE AINDA VOU COLOCAR AQUI*

---

## 🔐 Authentication

### JWT (users)

Obtain a token via `POST /api/v1/auth/login`. Include it in every protected request:

```
Authorization: Bearer <token>
```

### Roles

| Role | Permissions |
|---|---|
| `STUDENT` | Join classrooms, study flashcards, track progress |
| `ADVISOR` | Everything a student can + create classrooms and roadmaps, view class dashboards |

### API Key (Python scraper)

The AI service uses a static key to create roadmaps and nodes:

```
X-API-Key: <SCRAPER_API_KEY value>
```

---

## 📡 API Overview

| Domain | Endpoints |
|---|---|
| Auth | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Classrooms | `POST /api/v1/classrooms`, `POST /api/v1/classrooms/join/{code}`, `GET /api/v1/classrooms` |
| Roadmaps | `GET /api/v1/roadmaps`, `GET /api/v1/roadmaps/{id}`, `POST /api/v1/roadmaps` |
| Roadmap Nodes | `POST /api/v1/roadmap-nodes` |
| Flashcards | `POST /api/v1/flashcards`, `POST /api/v1/flashcards/{id}/attempts`, `GET /api/v1/flashcards/due` |
| Progress | `PUT /api/v1/progress` |
| Study Sessions | `POST /api/v1/study-sessions`, `PATCH /api/v1/study-sessions/{id}/end` |
| Dashboard | `GET /api/dashboard/{userId}`, `GET /api/dashboard/class/{classId}` |

*IMAGEM QUE AINDA VOU COLOCAR AQUI*

---

## 🧪 Running Tests

```bash
# All tests
./mvnw clean test

# Unit tests only (no Docker required)
./mvnw test -Dtest="*Test"

# Integration tests only (requires Docker)
./mvnw test -Dtest="*IntegrationTest,ApiApplicationTests"
```

---

## 🔒 Security

The API implements several layers of defence-in-depth:

### A) IDOR Protection (Insecure Direct Object Reference)
`GET /api/dashboard/{userId}`, `GET /api/attempts/due/{userId}`, and `GET /api/attempts/analysis/{userId}` all validate ownership before returning data.
- **Owner access**: the requesting user's JWT subject must match the `userId` path parameter.
- **Advisor access**: users with the `ADVISOR` role may query any user's data.
- Any other combination results in HTTP **403 Forbidden**.

### B) Security Audit Logging
Structured security events are logged by the `[SECURITY]` prefix in the format:
```
[SECURITY] event=XXX ip=XXX path=XXX user=XXX
```
Events logged:
- `FAILED_LOGIN` — bad credentials in `GlobalExceptionHandler`
- `UNAUTHORIZED_ACCESS` — `AuthorizationDeniedException` in `GlobalExceptionHandler`
- `INVALID_TOKEN` / `EXPIRED_OR_INVALID_TOKEN` — malformed or expired JWT in `JwtAuthFilter`
- `BLACKLISTED_TOKEN_USED` — token used after logout in `JwtAuthFilter`
- `RATE_LIMIT_EXCEEDED` — client exceeded request quota in `RateLimitFilter`

### C) Rate Limiting (Redis fixed-window)
`RateLimitFilter` enforces per-IP request quotas using Redis:
- **Auth endpoints** (`/api/v1/auth/**`): **10 requests / minute**
- **All other endpoints**: **200 requests / minute**
- Exceeded limit → HTTP **429 Too Many Requests** with `Retry-After: 60` header.
- **Fail-open**: if Redis is unavailable, traffic is never blocked.

### D) Security Headers
Every HTTP response includes:
| Header | Value |
|--------|-------|
| `X-Frame-Options` | `DENY` |
| `X-Content-Type-Options` | `nosniff` |
| `Content-Security-Policy` | `default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'` |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` |
| `Referrer-Policy` | `strict-origin-when-cross-origin` |

### E) Logout & Token Blacklisting
`POST /api/v1/auth/logout` (requires valid JWT):
1. Extracts the remaining TTL of the current token via `JwtService.getRemainingValidityMillis()`.
2. Stores the token signature (`token_bl:<signature>`) in Redis with that TTL.
3. Every subsequent request through `JwtAuthFilter` checks the blacklist; a blacklisted token is silently dropped (SecurityContext stays empty → 401/403 for protected resources).
4. **Fail-open**: if Redis is unavailable, blacklist checks return `false` so traffic is not interrupted.

---

## 🔗 Related Repositories

- [studgrasp-frontend](https://github.com/vitordealbs/studgrasp-frontend) — React + TypeScript UI
- [studgrasp-ai](https://github.com/vitordealbs/studgrasp-ai) — Python FastAPI: roadmap scraper + AI flashcard generation
