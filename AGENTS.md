# Hunting & Fishing Store OpenCode Setup

A modern e-commerce platform for hunting and fishing equipment, built with Spring Boot 4 and Java 21. Domain-driven architecture where each domain feature lives in its own package under `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/`.

## Commands

- Build tool is **Maven** via the wrapper `./mvnw`.
- First-time setup: `docker compose up -d` (Postgres on port 5432, Redis on port 6379), `./mvnw clean install`, `./mvnw spring-boot:run`.
- Verification/Build check: `./mvnw clean package` runs compile and unit/integration tests (via Testcontainers). Note: there is no dedicated lint plugin; correctness is guarded by compilation and tests.
- Running tests: `./mvnw test`.

## Architecture

- Feature Layout: `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/` containing:
  - `controller/`: REST API endpoints annotated with `@RestController`, `@RequestMapping("/api/v1/...")`. Handles validation via `@Valid`.
  - `service/`: Contains `@Service` components implementing business logic. Methods return Response DTOs.
  - `repository/`: Spring Data JPA interfaces extending `JpaRepository<Entity, Long>`.
  - `entity/`: JPA entities extending `BaseEntity` from `common/`.
  - `dto/request/`: Java record classes for incoming requests with Jakarta validation annotations.
  - `dto/response/`: Java record classes representing API responses.
- Single shared base class: `BaseEntity` in `com.tiagofsps.hunting_fishing_store.common` handles automatic `id`, `createdAt`, and `updatedAt` tracking.
- Exceptions: Domain-specific exceptions reside in `com.tiagofsps.hunting_fishing_store.common.exception` (e.g., `BrandNotFoundException`) and are handled globally.

## Security Configuration

- **Stateless Authentication**: Performed via JWT Access Tokens (included in `Authorization: Bearer` header) and long-lived Refresh Tokens stored in HttpOnly cookies.
- **Stateful Token Revocation**: Uses Redis to store active/revoked refresh token IDs.
- **RBAC (Role-Based Access Control)**: Supports `ROLE_USER` and `ROLE_ADMIN`. Use `@PreAuthorize("hasRole('ADMIN')")` or `SecurityConfig` for granular method or route protection.

## Gotchas

- Database Schema: Hibernate `ddl-auto: update` handles schema synchronization automatically in dev mode. No explicit migration tools are required.
- Environment Profiles: Default profile is `dev`, which activates SQL logging. `prod` and `test` profiles exist for deployment and testing.
