# studgrasp-api

Backend REST API do **StudGrasp** — plataforma de aprendizado em Ciência da Computação com flashcards, roadmaps interativos e acompanhamento de progresso por orientadores.

## Visão geral da arquitetura

Este serviço faz parte de uma arquitetura de três camadas:

```
studgrasp-frontend  (React + TypeScript)
        │
        │  HTTP REST /api/v1/**
        ▼
studgrasp-api       (Java Spring Boot)  ◄── este repositório
        │
        │  HTTP REST (X-API-Key)
        ▼
studgrasp-ai        (Python FastAPI + Anthropic API)
                    scraper de roadmaps + geração de flashcards
```

O serviço Python popula roadmaps via `POST /api/v1/roadmaps` e `POST /api/v1/roadmap-nodes` usando uma API Key estática (`X-API-Key`). O frontend e todos os outros clientes usam JWT.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Segurança | Spring Security + JWT (jjwt 0.12) + API Key (scraper) |
| Banco de dados | PostgreSQL 16 |
| Cache | Redis 7 (infraestrutura provisionada, uso planejado) |
| Migrations | Flyway |
| ORM | Hibernate / Spring Data JPA |
| Documentação API | SpringDoc OpenAPI (Swagger UI) |
| Testes unitários | JUnit 5 + Mockito |
| Testes de integração | Spring Boot Test + Testcontainers |
| Build | Maven (wrapper `./mvnw` incluso) |
| Containerização | Docker + Docker Compose |

---

## Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven (ou usar `./mvnw`)

---

## Rodando localmente

### 1. Clone o repositório

```bash
git clone https://github.com/vitordealbs/studgrasp-api.git
cd studgrasp-api
```

### 2. Suba a infraestrutura com Docker

```bash
docker compose up -d
```

| Serviço | Porta | Descrição |
|---|---|---|
| PostgreSQL | 5432 | Banco de dados principal |
| Redis | 6379 | Cache (uso planejado) |
| PgAdmin | 5050 | Interface visual do banco |

### 3. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz (nunca versione este arquivo):

```env
# Banco de dados
DB_URL=jdbc:postgresql://localhost:5432/studgrasp
DB_USER=studgrasp
DB_PASS=studgrasp

# Segurança
JWT_SECRET=sua-chave-secreta-minimo-32-caracteres
SCRAPER_API_KEY=chave-usada-pelo-servico-python

# Opcional
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
PORT=8080
LOG_LEVEL=INFO
```

> Em desenvolvimento, todos os valores têm defaults automáticos no `application.yaml`. Nunca use os defaults em produção.

### 4. Rode a aplicação

```bash
./mvnw spring-boot:run
```

A API estará em `http://localhost:8080`.

### 5. Verifique o health check

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### 6. Acesse a documentação interativa

```
http://localhost:8080/swagger-ui.html
```

---

## Autenticação e autorização

### JWT (frontend e usuários)

Todos os endpoints protegidos exigem o token no header:

```
Authorization: Bearer <token>
```

O token é obtido via `POST /api/v1/auth/login` e expira em **24 horas**.

### Roles

| Role | Pode fazer |
|---|---|
| `STUDENT` | Entrar em turmas, estudar, registrar progresso, usar flashcards |
| `ADVISOR` | Tudo do STUDENT + criar turmas e roadmaps |

### API Key (serviço Python / scraper)

O serviço Python usa uma chave estática no header para criar roadmaps e nodes:

```
X-API-Key: <valor de SCRAPER_API_KEY>
```

Endpoints que aceitam API Key: `POST /api/v1/roadmaps` e `POST /api/v1/roadmap-nodes`.

---

## Endpoints

Todos os endpoints usam o prefixo `/api/v1/`. Os endpoints de auth e `GET` de roadmaps são públicos; os demais exigem `Authorization: Bearer <token>`.

### Autenticação

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Público | Cadastro de novo usuário |
| POST | `/api/v1/auth/login` | Público | Login — retorna JWT |

**Registro:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vitor Santos",
    "email": "vitor@studgrasp.com",
    "password": "MinhaSenh@Segura123",
    "role": "STUDENT"
  }'
```

> A senha precisa ter no mínimo 12 caracteres com letras maiúsculas, minúsculas, número e caractere especial.

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "name": "Vitor Santos",
  "email": "vitor@studgrasp.com",
  "role": "STUDENT"
}
```

---

### Turmas (Classrooms)

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| POST | `/api/v1/classrooms` | ADVISOR | Cria uma turma (gera invite code automaticamente) |
| POST | `/api/v1/classrooms/join/{inviteCode}` | Autenticado | Entra em uma turma pelo código |
| GET | `/api/v1/classrooms` | Autenticado | Lista minhas turmas (como orientador ou membro) |
| GET | `/api/v1/classrooms/{id}` | Membro ou Advisor | Detalhes de uma turma |

---

### Grupos

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| POST | `/api/v1/groups` | Autenticado | Cria grupo dentro de uma turma |
| POST | `/api/v1/groups/{groupId}/messages` | Autenticado | Envia mensagem no grupo |

---

### Roadmaps

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/v1/roadmaps` | Público | Lista todos os roadmaps (paginado) |
| GET | `/api/v1/roadmaps/{id}` | Público | Roadmap completo com todos os nós |
| GET | `/api/v1/roadmaps/career/{careerType}` | Público | Roadmap por tipo de carreira (ex: `BACKEND`) |
| POST | `/api/v1/roadmaps` | ADVISOR ou API Key | Cria um roadmap |

**Parâmetros de paginação** para `GET /api/v1/roadmaps`:
```
?page=0&size=20&sort=title,asc
```

---

### Nós do Roadmap

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/v1/roadmap-nodes` | ADVISOR ou API Key | Cria um nó dentro de um roadmap |

---

### Flashcards

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| POST | `/api/v1/flashcards` | Autenticado | Cria flashcard vinculado a um nó |
| POST | `/api/v1/flashcards/{id}/attempts` | Autenticado | Registra tentativa (acerto/erro) |
| GET | `/api/v1/flashcards/due` | Autenticado | Lista flashcards com revisão vencida (spaced repetition) |

O algoritmo de revisão espaçada: acerto → próxima revisão em 3 dias; erro → 1 dia.

---

### Progresso

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| PUT | `/api/v1/progress` | Autenticado | Cria ou atualiza progresso em um nó do roadmap |

O `userId` é extraído automaticamente do token JWT — não é necessário enviar no body.

---

### Sessões de Estudo

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| POST | `/api/v1/study-sessions` | Autenticado | Inicia uma sessão de estudo em um nó |
| PATCH | `/api/v1/study-sessions/{id}/end?endedAt=...` | Autenticado | Encerra a sessão (duração calculada no servidor) |

---

## Estrutura do projeto

```
src/
├── main/
│   ├── java/com/studgrasp/api/
│   │   ├── ApiApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java          # Spring Security, CORS, filtros JWT e API Key
│   │   ├── domain/                          # Bounded contexts — um pacote por domínio
│   │   │   ├── auth/                        # Registro, login, DTOs de autenticação
│   │   │   ├── user/                        # Entidade User, UserRole, UserRepository
│   │   │   ├── classroom/                   # Turmas: CRUD, invite code, acesso
│   │   │   ├── classmember/                 # Join table classroom ↔ user
│   │   │   ├── group/                       # Grupos de estudo e mensagens
│   │   │   ├── roadmap/                     # Roadmaps e nós (entidades + repositórios)
│   │   │   ├── roadmapnode/                 # Criação de nós (Controller + Service)
│   │   │   ├── flashcard/                   # Flashcards e tentativas
│   │   │   ├── progress/                    # Progresso por nó do roadmap
│   │   │   └── session/                     # Sessões de estudo com cronômetro
│   │   └── infra/
│   │       ├── security/
│   │       │   ├── JwtService.java          # Geração e validação de tokens
│   │       │   ├── JwtAuthFilter.java       # Filtro Bearer token
│   │       │   └── ApiKeyAuthFilter.java    # Filtro X-API-Key (scraper Python)
│   │       └── exception/
│   │           ├── GlobalExceptionHandler.java
│   │           ├── ErrorResponse.java
│   │           └── ResourceNotFoundException.java
│   └── resources/
│       ├── application.yaml
│       └── db/migration/
│           ├── V1__create_users_table.sql
│           ├── V2__create_classes_table.sql
│           ├── V3__create_groups_table.sql
│           ├── V4__create_roadmap_table.sql
│           ├── V5__create_flashcards_table.sql
│           └── V6__create_progress_table.sql
└── test/
    └── java/com/studgrasp/api/
        ├── ApiApplicationTests.java                        # Context load (Testcontainers)
        └── domain/
            ├── auth/
            │   ├── AuthServiceTest.java                    # Unit
            │   └── AuthControllerIntegrationTest.java      # Integração (MockMvc + Testcontainers)
            ├── classroom/ClassroomServiceTest.java
            ├── flashcard/FlashcardServiceTest.java
            ├── group/GroupServiceTest.java
            ├── progress/UserProgressServiceTest.java
            ├── roadmap/RoadmapServiceTest.java
            ├── session/StudySessionServiceTest.java
            └── infra/security/JwtServiceTest.java
```

---

## Arquitetura interna

### Padrão por domínio

Cada domínio de negócio segue a mesma estrutura interna:

```
Controller  →  Service  →  Repository  →  Entity (JPA)
   ↑               ↑
 DTOs (record)   Lógica de negócio
```

- **Controllers**: recebem requests HTTP, validam com `@Valid`, delegam ao service
- **Services**: contêm toda a lógica de negócio, marcados com `@Transactional`
- **Repositories**: Spring Data JPA; queries customizadas com `@Query` onde necessário
- **Entities**: mapeadas com Hibernate; timestamps gerenciados com `@PrePersist/@PreUpdate`

### Segurança

```
Request
  │
  ├─► ApiKeyAuthFilter    (seta ROLE_SCRAPER se X-API-Key válido)
  │
  ├─► JwtAuthFilter       (seta usuário autenticado se Bearer token válido)
  │
  └─► SecurityFilterChain (aplica regras de acesso por role)
```

O `userId` autenticado é sempre extraído do `SecurityContextHolder` via `@AuthenticationPrincipal User user` nos controllers — nunca aceito como parâmetro do cliente.

### Banco de dados

Flyway gerencia as migrations automaticamente ao subir a aplicação. O Hibernate opera em modo `validate` (não altera o schema).

```
users
  └── classes (classroom)
        └── class_members  (users ↔ classes)
        └── groups
              └── group_members  (users ↔ groups)
              └── messages

roadmaps
  └── roadmap_nodes (hierárquico, parent_id nullable)
        └── flashcards
              └── flashcard_attempts (users ↔ flashcards)
        └── user_progress  (users ↔ nodes)
        └── study_sessions (users ↔ nodes)
```

### Tratamento de erros

Todos os erros são tratados por `GlobalExceptionHandler` e retornam o formato padrão:

```json
{
  "status": 400,
  "message": "Validation Error",
  "fields": {
    "email": "must be a well-formed email address",
    "password": "must match the required pattern"
  },
  "timestamp": "2025-05-28T10:00:00"
}
```

---

## Testes

```bash
# Todos os testes (37 ao total)
./mvnw clean test

# Apenas testes unitários (sem Docker)
./mvnw test -Dtest="*Test"

# Apenas testes de integração (requer Docker)
./mvnw test -Dtest="*IntegrationTest,ApiApplicationTests"
```

| Tipo | Quantidade | Ferramenta |
|---|---|---|
| Unitários | 33 | JUnit 5 + Mockito |
| Integração | 4 | Spring Boot Test + Testcontainers |

> Os testes de integração sobem um container PostgreSQL real via Testcontainers — o Docker precisa estar em execução.

---

## Variáveis de ambiente (referência completa)

| Variável | Default (dev) | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/studgrasp` | URL JDBC do banco |
| `DB_USER` | `studgrasp` | Usuário do banco |
| `DB_PASS` | `studgrasp` | Senha do banco |
| `JWT_SECRET` | `studgrasp-dev-secret-change-in-production` | Chave HMAC do JWT — **trocar em prod** |
| `SCRAPER_API_KEY` | `studgrasp-scraper-dev-key-change-in-production` | API Key do serviço Python — **trocar em prod** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origins permitidas pelo CORS |
| `PORT` | `8080` | Porta do servidor |
| `REDIS_HOST` | `localhost` | Host do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `LOG_LEVEL` | `INFO` | Nível de log da aplicação |

---

## Serviços relacionados

- [studgrasp-frontend](https://github.com/vitordealbs/studgrasp-frontend) — Interface React + TypeScript
- [studgrasp-ai](https://github.com/vitordealbs/studgrasp-ai) — Serviço Python: scraper de roadmaps + geração de flashcards com IA
