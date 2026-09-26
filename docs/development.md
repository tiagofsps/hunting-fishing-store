# Development Guide

How to set up, run, and work on the Hunting & Fishing Store backend.

## Prerequisites

- **Java 21** — pinned in `mise.toml` (`java = "21"`).
- **Maven** — use the wrapper `./mvnw` (no local Maven install required).
- **`make`** — for the database lifecycle targets.
- **`psql`** — the `make db-*` scripts talk to Postgres directly on the host, not through Docker.
- **Docker** — only required to run the container-backed tests (see [Testing Guide](testing.md)). The app itself runs against the local Postgres/Redis started by `docker compose`.

## Environment Profiles

The active profile defaults to `dev` and can be overridden with `SPRING_PROFILES_ACTIVE`.

| Profile | Purpose | Notable behavior |
| --- | --- | --- |
| `dev` (default) | Local development | SQL logging enabled, seeders run on an empty database, cookies not marked secure, dev JWT secret |
| `test` | Automated tests | `ddl-auto: create-drop`, Testcontainers provide Postgres/Redis, rate limiting relaxed, cookies not marked secure |
| `prod` | Deployment | `ddl-auto: validate`, cookies marked secure, ECS structured logging, requires `ADMIN_EMAIL`/`ADMIN_PASSWORD` |

## Running the App

1. Start the infrastructure:

   ```bash
   docker compose up -d
   ```

   This starts Postgres 17 (`hunting-fishing-db`, port 5432) and Redis 7 (`hunting-fishing-redis`, port 6379).

2. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   The app listens on `http://localhost:8080`. `spring-boot-devtools` is on the classpath, so code changes trigger an automatic restart.

3. Useful URLs while running:

   - OpenAPI JSON: `http://localhost:8080/v3/api-docs`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Health probes: `http://localhost:8080/actuator/health`
   - Metrics: `http://localhost:8080/actuator/metrics`

## Database Lifecycle

The `make` targets wrap host `psql` and do not require Docker CLI access:

| Command | Effect |
| --- | --- |
| `make db-reset` | **Destructive.** Drops and recreates the `hunting_fishing_store` database |
| `make db-seed` | Runs `db-reset`, then starts the app so Flyway migrations and seeders run |
| `make db-setup` | Alias for `db-seed` |
| `make db-shell` | Opens an interactive `psql` session |

The scripts source an optional `.env` file and honor `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, and `POSTGRES_DB` (defaults: `localhost`, `5432`, `postgres`, `postgres`, `hunting_fishing_store`).

### Schema and Seeding

- **Flyway** applies the baseline schema from `src/main/resources/db/migration` (`V1__init.sql` through `V5__newsletter_subscription.sql`).
- **Seeders** run only in the `dev` profile and only when the database is empty (`UserSeeder` returns early if any user exists). They create:
  - A fixed admin: `admin@huntingfishing.com` / `Password123!` (ROLE_ADMIN, with shipping and billing addresses). Override with `ADMIN_EMAIL`/`ADMIN_PASSWORD`; in `prod` these are mandatory.
  - A fixed demo user: `user@huntingfishing.com` / `Password123!` (ROLE_USER, with demo hunting/fishing licenses).
  - 20 demo users with password `password123`, plus catalog, coupon, and promotion fixtures.

To get a fresh, fully seeded database: `make db-seed` (or `make db-reset` followed by `./mvnw spring-boot:run`).

## Local Services

- **Email**: no SMTP host is configured by default, so transactional emails are best-effort and logged rather than sent. Configure `SPRING_MAIL_HOST`/`SPRING_MAIL_USERNAME`/`SPRING_MAIL_PASSWORD` to enable real delivery.
- **Payments**: the default `PAYMENT_GATEWAY=stub` uses an in-memory stub gateway. Webhook calls use `PAYMENT_WEBHOOK_SECRET` (default `local-webhook-secret-change-me`).
- **Redis**: used for refresh-token revocation, rate limiting, and password-reset tokens. Keys expire on their own; the `make db-*` scripts intentionally do not flush Redis.

## Configuration

All settings live in `src/main/resources/application.yml` and are overridable via environment variables. Key ones:

| Variable | Default | Description |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/hunting_fishing_store` | JDBC URL |
| `DATABASE_USER` / `DATABASE_PASSWORD` | `postgres` / `postgres` | DB credentials |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis connection |
| `JWT_SECRET_KEY` | dev secret (dev profile) | JWT signing key |
| `JWT_ACCESS_EXPIRATION` | `15m` | Access-token lifetime |
| `JWT_REFRESH_EXPIRATION_SECONDS` | `604800` | Refresh-token lifetime (seconds) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |
| `RATE_LIMIT_MAX_ATTEMPTS` / `RATE_LIMIT_WINDOW_SECONDS` | `5` / `900` | Login rate limiting |
| `SHIPPING_FREE_THRESHOLD` / `SHIPPING_FEE` | `75` / `4.95` | Free-shipping threshold and fee |
| `PAYMENT_GATEWAY` | `stub` | Payment gateway implementation |
| `PAYMENT_WEBHOOK_SECRET` | `local-webhook-secret-change-me` | Webhook signature secret |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | — | Fixed admin account (required in `prod`) |

## Common Pitfalls

- **Port 8080 already in use**: a previous `./mvnw spring-boot:run` may still be running. Find and stop the leftover process before starting again.
- **Container tests fail with `Could not initialize class AbstractContainerBaseTest`**: this is environmental — Testcontainers cannot reach a Docker daemon. Unit tests still run; see the [Testing Guide](testing.md).
- **Seeders did not run**: seeding only happens in the `dev` profile and only when the database is empty. Run `make db-reset` first.
- **Schema drift in `prod`**: `ddl-auto` is `validate` in `prod`, so schema changes must go through Flyway migrations, not Hibernate.