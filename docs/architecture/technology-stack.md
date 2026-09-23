# Technology Stack

## Backend

- Java 21
- Spring Boot 4.1
- Spring Data JPA / Hibernate
- Spring Security (OAuth2 Resource Server, JWT, method-level RBAC)

## Database / Cache

- PostgreSQL (primary)
- Redis (refresh-token revocation, rate limiting, password-reset tokens)

## Validation & Serialization

- Jakarta Bean Validation
- Spring Web / Jackson

## Email

- Spring Mail (`JavaMailSender`, best-effort transactional emails)

## API & Docs

- RESTful API over `/api/v1`
- springdoc-openapi 3.x — OpenAPI / Swagger UI

## Operations

- Spring Boot Actuator (health probes)
- ECS structured logging (`prod` profile)

## Testing

- JUnit 5
- Mockito
- Testcontainers (PostgreSQL & Redis)

## Build Tool

- Maven (wrapper `./mvnw`)