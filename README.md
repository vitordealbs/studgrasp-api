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

## 🔗 Related Repositories

- [studgrasp-frontend](https://github.com/vitordealbs/studgrasp-frontend) — React + TypeScript UI
- [studgrasp-ai](https://github.com/vitordealbs/studgrasp-ai) — Python FastAPI: roadmap scraper + AI flashcard generation
