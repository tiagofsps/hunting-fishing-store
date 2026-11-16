# Coding Conventions

## Package layout

- One package per business capability under
  `com.tiagofsps.hunting_fishing_store/<feature>/`.
- Every feature uses `controller/`, `service/`, `repository/`, `entity/` and
  `dto/{request,response}/`.
- Cross-feature infrastructure lives in `common/` (exceptions, shared DTOs,
  seeders, mail) or `config/` (security, global properties, OpenAPI).

## Java

- Java 21; use records for all request/response DTOs.
- 4-space indentation, UTF-8, LF line endings.
- `PascalCase` for classes and records, `camelCase` for methods and fields,
  `UPPER_SNAKE_CASE` for constants.
- Entities use Lombok `@Getter`, `@Setter` and `@NoArgsConstructor` and extend
  `BaseEntity` for `id`/`createdAt`/`updatedAt`.

## Layering

- Controllers stay thin: validate with `@Valid`, delegate to services, never
  touch repositories.
- Business rules and entity ↔ DTO mapping live entirely in the service layer.
- Repositories extend `JpaRepository` and use derived query methods; dynamic
  filtering is expressed as an inline `Specification` in the repository.
- Services that only read are annotated `@Transactional(readOnly = true)`.

## Validation

- Validate request records with Jakarta Bean Validation (`@NotBlank`, `@Size`,
  `@NotNull`, `@Valid`, …).
- Controllers annotate request bodies with `@Valid`.
- Domain violations raise a dedicated exception from `common/exception` and are
  translated by `GlobalExceptionHandler` into the shared `ErrorResponse` shape.

## REST API

- Base path `/api/v1`.
- Plural, noun-based resource paths (`/products`, `/orders`).
- `201 Created` for creation, `204 No Content` for deletion, `202 Accepted` for
  fire-and-forget flows such as password reset.
- Admin-only operations use `@PreAuthorize("hasRole('ADMIN')")` and are marked
  `[ADMIN]` in the README; ownership checks go through `AuthorizationUtil`.
- List endpoints that can grow use the Spring `Page` envelope with `page`,
  `size`, `sort` and `direction` parameters.

## Database

- Snake case for tables and columns.
- Schema changes are Flyway migrations under `src/main/resources/db/migration`
  (`V<n>__description.sql`); `prod` validates the schema, `dev` may use
  `ddl-auto: update`.
- Cache and session state (refresh-token IDs, rate-limit counters, reset tokens)
  belongs in Redis, not in the relational schema.

## Testing

- Unit tests: JUnit 5 + Mockito, no Spring context.
- Repository tests extend `BaseRepositoryTest`; controller tests extend
  `BaseControllerTest`; integration tests extend `BaseIntegrationTest`.
- Container-backed tiers need Docker (Testcontainers provisions PostgreSQL and
  Redis).
- Name test classes `<ClassUnderTest>Test` and keep the package aligned with the
  production class.

## Git

- Conventional Commits: `feat(scope): …`, `fix(scope): …`, `docs: …`,
  `chore: …`, `build: …`, `ci: …`.
- Commit bodies explain the *why* (the problem and the chosen approach), not a
  line-by-line restatement of the diff.
- Keep commits focused on one change.
