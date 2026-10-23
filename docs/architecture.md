# DevTrack architecture

## Service layout

```mermaid
flowchart LR
  Browser[React + Vite] -->|same-origin /api/v1, HttpOnly cookie| Proxy[Nginx or Cloudflare Pages Function]
  Proxy --> API[Spring Boot 3.5 REST API]
  API --> Auth[Spring Security + BCrypt + short-lived JWT]
  API --> JPA[Spring Data JPA]
  JPA --> DB[(PostgreSQL)]
  API --> Migrations[Flyway]
```

The API is a modular monolith. Controllers validate HTTP input and call services; services enforce rules and ownership; repositories perform persistence. API responses are DTOs, never JPA entities.

## Database ER diagram

```mermaid
erDiagram
  USERS ||--o{ DSA_PROBLEMS : owns
  USERS ||--o{ PROJECTS : owns
  PROJECTS ||--o{ PROJECT_TECHNOLOGIES : includes
  USERS ||--o{ SKILLS : owns
  USERS ||--o{ ACTIVITY_LOGS : generates

  USERS {
    uuid id PK
    string name
    string email UK
    string password_hash
    timestamptz created_at
    timestamptz updated_at
  }
  DSA_PROBLEMS {
    uuid id PK
    uuid user_id FK
    string title
    string topic
    string difficulty
    string platform
    string problem_url
    string status
    text notes
    timestamptz solved_at
    timestamptz created_at
    timestamptz updated_at
  }
  PROJECTS {
    uuid id PK
    uuid user_id FK
    string title
    text description
    string status
    string repository_url
    string demo_url
    date start_date
    date target_date
    timestamptz created_at
    timestamptz updated_at
  }
  PROJECT_TECHNOLOGIES {
    uuid project_id FK
    string technology
  }
  SKILLS {
    uuid id PK
    uuid user_id FK
    string name
    string category
    smallint proficiency_level
    smallint target_level
    timestamptz created_at
    timestamptz updated_at
  }
  ACTIVITY_LOGS {
    uuid id PK
    uuid user_id FK
    string action_type
    string entity_type
    uuid entity_id
    timestamptz created_at
  }
```

## Security decisions

- The client never supplies an owner ID. Every private service call receives the UUID from the validated security principal.
- Passwords use BCrypt; credentials and JWTs are never written to logs or browser storage.
- The access JWT expires after 15 minutes by default and is stored in an HttpOnly, SameSite cookie.
- Production must use HTTPS, `COOKIE_SECURE=true`, a dedicated 32-byte-or-longer `JWT_SECRET`, and exact allowed origins.
- CORS is credential-aware and uses explicit origins, not `*`.
- Record lookups combine record ID and current user ID so a foreign ID returns 404.
- Flyway owns schema changes; Hibernate validates the schema at startup.

## Source of truth

- API contract: `lib/api-spec/openapi.yaml`
- Database migration: `backend/src/main/resources/db/migration/`
- Backend: `backend/src/main/java/com/devtrack/`
- Frontend: `artifacts/devtrack/src/`
