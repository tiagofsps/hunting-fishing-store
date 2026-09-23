---
name: store-conventions
description: Coding standards, package organization, validation conventions, and security rules for the Hunting & Fishing Store project
---

## Code Style & Formatting

- Indentation: Use standard Java spacing and formatting guidelines (default 4-space).
- Lombok: Use `@Getter`, `@Setter`, and `@NoArgsConstructor` annotations for JPA Entities to reduce boilerplate.
- Record Classes: Always use Java `record` for DTOs (Requests and Responses) as they are immutable and clean.

## Architecture & Package Conventions

- Follow the domain-driven structure under `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/`.
- Controllers must remain thin, only validating the input with `@Valid` and delegating to Services.
- Business logic and mapping logic (converting Entities to Response DTOs) live entirely inside the Services layer.
- Repositories must inherit from `JpaRepository` and define custom query methods cleanly using Spring Data query derivation conventions.

## Validation Conventions

- Input validation must be applied to request records using Jakarta Bean Validation annotations (`@NotBlank`, `@Size`, `@Min`, `@NotNull`, etc.).
- Controllers must use `@Valid` on `@RequestBody` arguments to enforce validation rules.

## Security Rules

- Endpoints under `/api/admin/**` are secured for admins.
- Use `@PreAuthorize("hasRole('ADMIN')")` or method security where necessary for fine-grained role-based protection.
- Inject `AuthorizationUtil` to check ownership of resources when a regular user can only access their own data.
