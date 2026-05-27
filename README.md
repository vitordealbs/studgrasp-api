# studgrasp-api

Backend REST API do **StudGrasp** — plataforma de aprendizado em Ciência da Computação com flashcards gerados por IA, roadmap interativo e acompanhamento de progresso por orientadores.

## O que este serviço faz

- Autenticação segura com JWT (registro e login)
- Gerenciamento de usuários com perfis de Estudante e Orientador
- Gerenciamento de turmas e grupos de estudo com chat em tempo real (WebSocket)
- Controle de progresso por tópico do roadmap
- Cronômetro de sessões de estudo
- Integração com o serviço Python de IA para geração de flashcards

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Segurança | Spring Security + JWT (jjwt 0.12) |
| Banco de dados | PostgreSQL 16 |
| Cache | Redis 7 |
| Migrations | Flyway |
| Testes | JUnit 5 + Mockito + Testcontainers |
| Build | Maven |
| Containerização | Docker + Docker Compose |

## Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven (ou usar o wrapper `./mvnw` incluso)

## Como rodar localmente

### 1. Clone o repositório

```bash
git clone https://github.com/studgrasp/studgrasp-api.git
cd studgrasp-api
```

### 2. Sobe a infraestrutura com Docker

```bash
docker compose up -d
```

Isso sobe os seguintes serviços:

| Serviço | Porta | Descrição |
|---|---|---|
| PostgreSQL | 5432 | Banco de dados principal |
| Redis | 6379 | Cache e sessões |
| PgAdmin | 5050 | Interface visual do banco |

### 3. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (nunca versione este arquivo):

```env
JWT_SECRET=sua-chave-secreta-aqui
```

> Em desenvolvimento, se omitido, o valor padrão `studgrasp-dev-secret-change-in-production` é usado automaticamente.

### 4. Rode a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

### 5. Verifique o health check

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:
```json
{ "status": "UP" }
```

## Endpoints disponíveis

### Autenticação (público)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/register` | Cadastro de novo usuário |
| POST | `/auth/login` | Login e geração de token JWT |

#### Exemplo de registro

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vitor Santos",
    "email": "vitor@studgrasp.com",
    "password": "123456",
    "role": "STUDENT"
  }'
```

Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "name": "Vitor Santos",
  "email": "vitor@studgrasp.com",
  "role": "STUDENT"
}
```

#### Exemplo de login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "vitor@studgrasp.com",
    "password": "123456"
  }'
```

### Endpoints protegidos

Todos os demais endpoints exigem o token JWT no header:

```
Authorization: Bearer <token>
```

## Rodando os testes

```bash
# Todos os testes
./mvnw test

# Apenas unitários
./mvnw test -Dtest="*Test"

# Apenas integração
./mvnw test -Dtest="*IntegrationTest"
```

> Os testes de integração usam **Testcontainers** — o Docker precisa estar rodando.

## Estrutura do projeto

```
src/
├── main/java/com/studgrasp/api/
│   ├── config/          # Configurações Spring (Security, Redis, WebSocket)
│   ├── domain/
│   │   ├── auth/        # Login, registro, JWT DTOs
│   │   ├── user/        # Entidade, repositório
│   │   ├── classroom/   # Turmas
│   │   ├── group/       # Grupos e chat
│   │   ├── roadmap/     # Nós do roadmap
│   │   ├── flashcard/   # Flashcards
│   │   └── progress/    # Progresso e sessões de estudo
│   ├── infra/
│   │   ├── security/    # JwtService, JwtAuthFilter
│   │   └── exception/   # Handler global de erros
│   └── shared/dto/      # DTOs genéricos
└── resources/
    ├── application.yml
    └── db/migration/    # Scripts Flyway (V1 a V6)
```

## Arquitetura

Este serviço segue os princípios de **DDD tático** com organização por domínio de negócio e separação em camadas internas (Controller → Service → Repository). Faz parte de uma arquitetura de dois backends:

```
studgrasp-frontend  (React + TypeScript)
       │
       ▼
studgrasp-api       (Java Spring Boot)  ◄─ este repositório
       │
       ▼
studgrasp-ai        (Python FastAPI + Anthropic API)
```

## Serviços relacionados

- [studgrasp-frontend](https://github.com/studgrasp/studgrasp-frontend) — Interface React
- [studgrasp-ai](https://github.com/studgrasp/studgrasp-ai) — Serviço de IA e scraper
