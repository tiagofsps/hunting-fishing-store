---
name: store-commands
description: Maven build wrapper commands, Docker infrastructure setup, and test workflows for the Hunting & Fishing Store project
---

## Infrastructure

- `docker compose up -d` — Starts Postgres 17 (port 5432) and Redis 7 (port 6379).
- `docker compose down` — Stops infrastructure.

## Build and Run

- `./mvnw spring-boot:run` — Runs the Spring Boot application locally.
- `./mvnw clean compile` — Compiles the source files.
- `./mvnw clean package` — Compiles, runs tests, and packages the application into a runnable JAR.

## Testing

- `./mvnw test` — Runs all unit and integration tests.
- Integration tests use **Testcontainers** to spin up lightweight, isolated Postgres and Redis instances automatically. Docker must be running.

## Project Setup Workflow

1. Start infrastructure: `docker compose up -d`
2. Run the application: `./mvnw spring-boot:run`
3. Execute tests to verify: `./mvnw test`
