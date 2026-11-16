# Application Architecture

## Overview

The application follows a **Layered Architecture** organized by **business features** (feature-based packaging).

Instead of grouping the entire codebase by technical layers (such as `controller`, `service`, or `repository`), each business domain owns its own components. This improves cohesion, makes the project easier to navigate, and allows each module to evolve independently.

The project is implemented as a **Modular Monolith**, where all modules are deployed as a single application while maintaining clear boundaries between business domains.

---

# Package Structure

```text
src/main/java/com/tiagofsps/hunting_fishing_store
├── HuntingFishingStoreApplication.java
├── common/
│   ├── dto/            # Shared response records
│   ├── exception/      # Domain exceptions + global handler
│   ├── mapper/         # Reusable mappers (addresses)
│   ├── seeding/        # Seeder contract
│   └── service/        # Shared services (mail)
├── config/
│   ├── security/       # Filter chain, JWT, rate limiting, cookies
│   ├── OpenApiConfig.java
│   ├── PaymentProperties.java
│   ├── SeedDataConfig.java
│   └── ShippingProperties.java
├── auth/               # controller · service · repository · entity · dto
├── cart/
├── catalog/            # + seeding/ (demo catalog)
├── coupon/
├── newsletter/
├── order/
├── payment/            # + gateway/ (pluggable payment gateway)
├── promotion/
└── user/               # + seeding/ (demo users)
```

Every feature package uses the same sub-structure:

```text
<feature>/{controller, service, repository, entity, dto/{request,response}}
```


---

# Architectural Principles

The application is organized around business capabilities rather than technical concerns.

Each feature contains all the components required to implement its own functionality:

* Controllers
* Services
* Repositories
* JPA entities
* Request/response DTOs

This approach keeps related code close together and reduces navigation across unrelated packages.

---

# Layer Responsibilities

## Controller

Responsible for exposing REST endpoints.

Controllers should:

* Validate incoming requests.
* Delegate business logic to services.
* Return HTTP responses.
* Never access repositories directly.

---

## Service

Contains the application's business logic.

Services coordinate:

* Validation
* Domain rules
* Transactions
* Repository interactions

Business logic should reside here rather than in controllers.

---

## Repository

Responsible for data persistence.

Repositories should:

* Extend Spring Data JPA repositories.
* Execute database queries.
* Remain free of business logic.

---

## Entity

Contains the domain model represented as JPA entities under the feature's
`entity/` package.

Entities define:

* Business data
* Relationships
* Persistence mappings

They should avoid containing application service logic.

---

## DTO

Data Transfer Objects used for communication with external clients, under
`dto/request/` and `dto/response/`.

DTOs are Java records and isolate the API contract from the persistence model;
JPA entities are never exposed directly.

---

## Mapping

Entity ↔ DTO mapping lives in the service layer, next to the business rules
that produce it. A small shared mapper under `common/mapper` covers shapes used
across features (currently the address mapper).

---

## Query specifications

Dynamic filtering — for example product search — is expressed as a Spring Data
JPA `Specification` built inline in the repository interface, keeping the
criteria close to the query they describe.

---

# Shared Infrastructure

The `common` package contains infrastructure shared across all modules, and the
`config` package holds application wiring that is global rather than per-feature.

Typical components include:

* Shared DTOs and the `BaseEntity` audit fields
* Domain exceptions and the global exception handler
* Shared services (mail) and the seeding contract
* Spring Security configuration (filter chain, JWT, rate limiting, cookies)
* Payment/shipping properties and seed orchestration

Neither package should contain feature business logic.

---

# Request Flow

A typical HTTP request follows this flow:

```text
HTTP Request
        │
        ▼
 Controller
        │
        ▼
   Service
        │
        ▼
 Repository
        │
        ▼
 PostgreSQL
```

The response follows the reverse path back to the client.

---

# Design Goals

The chosen architecture aims to achieve the following goals:

* High cohesion within each business module.
* Clear separation of responsibilities.
* Easy navigation throughout the codebase.
* Maintainability as the project grows.
* Scalability without introducing unnecessary complexity.
* Alignment with modern Spring Boot development practices.

While the application is developed as a single deployable unit (Modular Monolith), its modular organization facilitates future evolution and keeps business domains well isolated.
