# DevTrack

DevTrack is a developer career and progress tracker for students and aspiring software engineers. Track DSA practice, projects, technical skills, and learning targets in one private workspace.

## Stack

- Frontend: React, Vite, TypeScript, Tailwind CSS, React Router
- Backend: Java 21, Spring Boot 3.5, Maven, Spring Security
- API: REST/JSON under `/api/v1`, documented with OpenAPI and Swagger UI
- Data: PostgreSQL, Spring Data JPA, Flyway migrations
- Auth: BCrypt password hashes and short-lived JWTs in HttpOnly cookies
- Local infrastructure: Docker Compose, Nginx, PostgreSQL
- CI: GitHub Actions

The Replit preview runs the React app and the Spring Boot API as separate services. The frontend directory is `artifacts/devtrack/` so it can be previewed as a Replit web artifact; supporting repository files live at the root.

## Features

- Personal dashboard with live problem, project, and skill totals
- DSA problem CRUD, search, topic/difficulty/status filters, sorting, and pagination
- Project CRUD, status tracking, technology tags, and safe external links
- Skill CRUD, category filtering, proficiency estimates, and target levels
- Activity history and DSA/activity analytics calculated from the signed-in user's records
- Registration, login, logout, account-scoped authorization, and protected routes
- Accessible loading, validation, empty, not-found, and API error states

Skill levels are user-managed estimates; DevTrack does not infer real-world ability.

## Run locally with Docker Compose

Requirements: Docker Engine with Compose v2, and a Git checkout of this repository.

```sh
cp .env.example .env
```

Replace `JWT_SECRET` with a new random value of at least 32 bytes. For example, generate one locally with `openssl rand -base64 48` and paste it into `.env`. Do not commit `.env`.

```sh
docker compose -f infra/docker-compose.yml up --build
```

Open <http://localhost:5173>. The API health check is at <http://localhost:5173/api/healthz>; Swagger UI is at <http://localhost:5173/api/swagger-ui.html>.

Useful commands:

```sh
docker compose -f infra/docker-compose.yml logs -f
docker compose -f infra/docker-compose.yml down
docker compose -f infra/docker-compose.yml down -v
```

`down` preserves the named PostgreSQL volume. **`down -v` deletes the local database volume and all data in it.**

## Run services without Compose

Use Java 21, Maven 3.9+, Node.js 22+, pnpm 10+, and a running PostgreSQL database.

```sh
export DB_URL=jdbc:postgresql://localhost:5432/devtrack
export DB_USERNAME=devtrack
export DB_PASSWORD='your-local-database-password'
export JWT_SECRET='a-new-random-secret-at-least-32-bytes-long'
export COOKIE_SECURE=false
mvn -f backend/pom.xml spring-boot:run
```

In another terminal:

```sh
pnpm install --frozen-lockfile
PORT=4173 BASE_PATH=/ pnpm --filter @workspace/devtrack run dev
```

The backend applies Flyway migrations on startup. It uses `ddl-auto=validate`; Hibernate does not create production tables.

## Tests and checks

```sh
mvn -f backend/pom.xml test
pnpm --filter @workspace/api-spec run codegen
pnpm run typecheck
PORT=4173 BASE_PATH=/ pnpm --filter @workspace/devtrack run build
```

Backend integration tests use an in-memory PostgreSQL-compatible H2 database for registration, cookie auth, validation, CRUD persistence, dashboard calculation, and cross-user access checks. Docker Compose starts PostgreSQL 16 and applies the real Flyway migration for local database verification.

## API

The contract source is `lib/api-spec/openapi.yaml`; generated TypeScript hooks and validation schemas live under `lib/api-client-react/` and `lib/api-zod/`.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | Register and sign in |
| POST | `/api/v1/auth/login` | Sign in |
| POST | `/api/v1/auth/logout` | Clear the access cookie |
| GET | `/api/v1/auth/me` | Current user |
| GET | `/api/v1/dashboard/summary` | User-scoped dashboard aggregates |
| GET/POST | `/api/v1/problems` | Search/list or create DSA problems |
| GET/PUT/DELETE | `/api/v1/problems/{id}` | Read, update, or delete an owned problem |
| GET/POST | `/api/v1/projects` | Search/list or create projects |
| GET/PUT/DELETE | `/api/v1/projects/{id}` | Read, update, or delete an owned project |
| GET/POST | `/api/v1/skills` | Filter/list or create skills |
| PUT/DELETE | `/api/v1/skills/{id}` | Update or delete an owned skill |
| GET | `/api/v1/analytics/dsa` | Difficulty and topic summaries |
| GET | `/api/v1/analytics/activity?days=30` | Daily activity totals |
| GET | `/api/healthz` | Health check |

Every private lookup scopes by the authenticated user's ID. IDs supplied by clients never determine ownership.

## Configuration

See `.env.example`. Key variables:

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC PostgreSQL URL |
| `DB_USERNAME`, `DB_PASSWORD` | Database credentials |
| `JWT_SECRET` | HMAC signing secret; keep outside source control |
| `ACCESS_TOKEN_MINUTES` | Access-token lifetime (default 15) |
| `COOKIE_SECURE` | Set `true` behind HTTPS |
| `COOKIE_SAME_SITE` | Cookie SameSite policy (default `Strict`) |
| `CORS_ALLOWED_ORIGINS` | Exact comma-separated browser origins |
| `PORT` | API listen port (default 8080) |

The API falls back to Replit's `SESSION_SECRET` only when `JWT_SECRET` is not set in this environment. For local or production use, configure a dedicated `JWT_SECRET`.

## Deploying

The Dockerfiles are compatible with Docker-based web-service hosts. A practical split is:

1. Host the React static output on Cloudflare Pages.
2. Run the Java API container as a web service on Render.
3. Use Neon PostgreSQL instead of Render's free Postgres if data must last beyond a short demo.
4. Add the Pages Function proxy in `functions/api/[[path]].ts` and set `DEVTRACK_API_ORIGIN` to the API service origin. This keeps browser API calls and the HttpOnly cookie same-origin.
5. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `COOKIE_SECURE=true`, `COOKIE_SAME_SITE=Strict`, and `CORS_ALLOWED_ORIGINS` in the backend host's secret/environment settings. Never bake secrets into Docker build arguments.

This repository contains deployment instructions and container configuration, but has **not** been published to a provider or verified with a public URL. Free tiers change: as checked on 2026-10-23, Render's free web services can spin down after inactivity and its free PostgreSQL expires after 30 days; Neon documents a permanent Free plan with compute, storage, and network-transfer quotas. Check current limits before launch.

Current provider references:

- [Render free services and databases](https://render.com/docs/free)
- [Render Docker deployments](https://render.com/docs/docker)
- [Neon Free plan limits](https://neon.com/faqs/free-plan-limits-and-quotas)
- [Cloudflare Pages limits](https://developers.cloudflare.com/pages/platform/limits/)

See [`docs/deployment.md`](docs/deployment.md) for provider setup, migration, and verification steps.

## GitHub and CI

The workflow at `.github/workflows/ci.yml` runs Java tests, TypeScript checks, and the frontend production build. The project files are ready to push, but a GitHub repository has not been created or pushed from this workspace.

## Documentation

- [Architecture and ER diagram](docs/architecture.md)
- [Deployment guide](docs/deployment.md)
- [Screenshot checklist](docs/screenshot-checklist.md)
- [LinkedIn project description and launch draft](docs/project-launch.md)
- [Postman collection](postman/DevTrack.postman_collection.json)
