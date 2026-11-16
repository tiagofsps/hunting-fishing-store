# Testing Guide

How the Hunting & Fishing Store test suite is organized and how to run it.

## Overview

The suite contains **319 `@Test` methods across 53 test classes** (plus four
shared base classes), split into tiers that mirror the feature packages under
`src/test/java/com/tiagofsps/hunting_fishing_store/<feature>/`:

| Tier | Classes | Spring context | Requires Docker |
| --- | --- | --- | --- |
| Unit tests (JUnit 5, Mockito where needed) | 16 | No | No |
| Repository tests (`@DataJpaTest`) | 10 | Sliced | Yes |
| Controller tests (MockMvc) | 11 | Full | Yes |
| Integration tests (`@SpringBootTest`) | 14 | Full | Yes |
| Other container-backed (Flyway, rate limiting) | 2 | Full | Yes |

37 of the 53 test classes are container-backed; the rest are plain unit tests.

## Base Classes

All container-backed tests extend a small hierarchy in `src/test/java/com/tiagofsps/hunting_fishing_store/`:

- **`AbstractContainerBaseTest`** — starts a static `PostgreSQLContainer` (`postgres:16-alpine`, `max_connections=300`) and a static Redis container (`redis:7-alpine`) once per JVM. Both are wired into Spring via `@ServiceConnection`, so no datasource/Redis config is needed in tests.
- **`BaseRepositoryTest`** — `@DataJpaTest` + `@ActiveProfiles("test")` + `@AutoConfigureTestDatabase(replace = NONE)`. For repository-only tests against the real Postgres container.
- **`BaseIntegrationTest`** — `@SpringBootTest(webEnvironment = MOCK)` + `@ActiveProfiles("test")` + `@AutoConfigureMockMvc`. Provides:
  - an autowired `MockMvc`;
  - a `@BeforeEach` cleanup that truncates every table (except `flyway_schema_history`) with `RESTART IDENTITY CASCADE`;
  - JWT helpers `adminJwt()` and `userJwt(email)` for authenticated requests.
- **`BaseControllerTest`** — extends `BaseIntegrationTest` and mocks `UserService`, `AuthorizationUtil`, and `UserDetailsService` with `@MockitoBean`, with default stubs for ownership checks. For controller tests that only exercise web-layer behavior.

## Test Tiers

### Unit tests

Plain JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`), no Spring context — the fastest tier. Example: `auth/service/TokenServiceTest.java` mocks `JwtEncoder` and `RefreshTokenRepository` and verifies claims and Redis persistence with `ArgumentCaptor`.

### Repository tests

Extend `BaseRepositoryTest` and exercise Spring Data JPA queries against the real Postgres container. Example: `catalog/repository/ProductRepositoryTest.java`.

### Controller tests

Extend `BaseControllerTest` and use `MockMvc` with mocked services to verify HTTP behavior (status codes, validation, security). Example: `order/controller/OrderControllerTest.java`.

### Integration tests

Extend `BaseIntegrationTest` and exercise the full stack — real services, repositories, security, and the containerized Postgres/Redis — through `MockMvc`. Example: `integration/UserIntegrationTest.java` registers a user via `/api/v1/auth/register` and then adds an address with `userJwt(email)`.

### Other tests

- `config/security/JwtAuthenticationConverterTest.java` and `SecurityConfigTest.java` — security wiring.
- `FlywayMigrationTest.java` — verifies all Flyway migrations apply cleanly.
- `HuntingFishingStoreApplicationTests.java` — application context loads.

## Running Tests

```bash
# Everything (unit + container-backed tests)
./mvnw test

# Compile + run all tests (CI-style check)
./mvnw clean package

# Compile only
./mvnw clean compile
```

### Without Docker

The 36 container-backed test classes (repository, controller, and integration tiers, plus the security, Flyway, and context tests) require a working Docker daemon. Without one they fail with `Could not initialize class AbstractContainerBaseTest` — this is environmental, not a code failure. To run only the fast unit tests:

```bash
./mvnw test -Dtest='*ServiceTest,*UnitTest'
```

## Writing Tests

- Mirror the main source layout: a test for `catalog/service/ProductService.java` lives in `catalog/service/ProductServiceTest.java`.
- Name test methods `shouldXxx` describing the expected behavior (e.g., `shouldRegisterUser`).
- Use AssertJ (`assertThat`) for assertions and Mockito for stubbing/verification.
- For web-layer tests, prefer the base classes over hand-rolled `@SpringBootTest` setups.
- The `test` profile relaxes rate limiting (`max-attempts: 10000`) so login flows can be tested without tripping the limiter.