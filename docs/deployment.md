# Deployment guide

## Recommended demo topology

- **Frontend:** Cloudflare Pages, serving the built React app.
- **API:** Render Docker Web Service, built from `backend/Dockerfile`.
- **Database:** Neon PostgreSQL, using its pooled or direct JDBC-compatible connection as appropriate.
- **Browser API path:** Cloudflare Pages Function at `functions/api/[[path]].ts` proxies `/api/*` to the API origin. The user-facing app and cookie remain on one origin.

This topology keeps the API in Java and the database in PostgreSQL. The provider accounts and deploys must be created by the project owner; this repository has not been deployed.

## Frontend on Cloudflare Pages

1. Connect the GitHub repository to a Cloudflare Pages project.
2. Build command: `corepack enable && pnpm install --frozen-lockfile && pnpm --filter @workspace/api-spec run codegen && PORT=4173 BASE_PATH=/ pnpm --filter @workspace/devtrack run build`.
3. Build output directory: `artifacts/devtrack/dist/public`.
4. Add the Pages environment variable `DEVTRACK_API_ORIGIN` with the HTTPS origin of the deployed API, without a path suffix (for example, `https://devtrack-api.example-host.com`).
5. Configure a preview deployment's API origin separately from production so previews do not write to the production database.

The Pages Function forwards `/api/*`, including the browser's HttpOnly cookie, to the configured API. Do not expose a general-purpose proxy or permit arbitrary upstream URLs.

## Java API on Render

1. Create a Web Service from the repository and select the Docker runtime.
2. Set Dockerfile path to `backend/Dockerfile`; the build context must be the repository root because the Dockerfile copies `backend/`.
3. Configure `DB_URL` as `jdbc:postgresql://<host>/<database>?sslmode=require`, plus `DB_USERNAME` and `DB_PASSWORD` from Neon.
4. Add `JWT_SECRET` as a secret environment variable (generate a distinct random value of 32 bytes or longer).
5. Set `COOKIE_SECURE=true`, `COOKIE_SAME_SITE=Strict`, `ACCESS_TOKEN_MINUTES=15`, and `CORS_ALLOWED_ORIGINS` to the public Pages origin.
6. Set the service health-check path to `/api/healthz`.
7. Keep automatic Flyway migrations enabled at application startup. Review schema migrations before production release and back up important data before applying a new migration.

Render's free web service can spin down after inactivity, so the first request may be delayed. Render's free PostgreSQL expires after 30 days; do not use it for data that must persist. Confirm free service availability and outbound database policies in the host dashboard before using the free plan.

## PostgreSQL on Neon

Create a PostgreSQL project and use the connection details from its dashboard. Store the JDBC URL and credentials as backend environment variables; do not commit them. Neon documents a $0 Free plan with 1 GB of storage per project, 100 compute-hours per month, and 5 GB public transfer per project at the time this guide was written. Compute can scale to zero and usage limits can suspend compute until reset; plan for backups and a paid tier if the app becomes important.

## Local Compose

```sh
cp .env.example .env
# Replace JWT_SECRET with a fresh random secret.
docker compose -f infra/docker-compose.yml up --build
```

The API waits for PostgreSQL health before startup. Flyway runs migrations before JPA schema validation. PostgreSQL is only exposed on the internal Compose network; the browser reaches the API through Nginx.

## Release checklist

- Set separate production and preview database credentials and JWT secrets.
- HTTPS is enabled on the public frontend and API host.
- Cookie has `Secure`, `HttpOnly`, `SameSite=Strict`, and a narrow `/api/v1` path.
- CORS origin exactly matches the public frontend origin.
- `/api/healthz` is healthy and Swagger is not exposing internal stack traces.
- Verify registration, duplicate email rejection, login, logout, CRUD, ownership checks, dashboard calculations, and persisted data.
- Check migration logs and confirm backups before releasing schema changes.
- Check provider limits and billing settings; a nominally free tier may sleep, suspend, or expire data.

## Current free-tier references

These provider details were checked on 2026-10-23 and can change:

- [Render free services](https://render.com/docs/free): free web services spin down on idle; its free PostgreSQL has a 30-day limit.
- [Render Docker support](https://render.com/docs/docker): Dockerfile-based services are supported.
- [Neon Free plan quotas](https://neon.com/faqs/free-plan-limits-and-quotas): free PostgreSQL remains available, subject to compute, storage, and transfer limits.
- [Cloudflare Pages limits](https://developers.cloudflare.com/pages/platform/limits/): free Pages builds and static-file quotas.
