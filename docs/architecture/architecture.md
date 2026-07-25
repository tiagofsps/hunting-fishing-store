# Application Architecture

## Overview

The application follows a **Layered Architecture** organized by **business features** (feature-based packaging).

Instead of grouping the entire codebase by technical layers (such as `controller`, `service`, or `repository`), each business domain owns its own components. This improves cohesion, makes the project easier to navigate, and allows each module to evolve independently.

The project is implemented as a **Modular Monolith**, where all modules are deployed as a single application while maintaining clear boundaries between business domains.

---

# Package Structure

```text
src/main/java/com/tiagofsps/store

├── common/
│   ├── config/
│   ├── exception/
│   ├── security/
│   └── util/
│
├── catalog/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   ├── dto/
│   ├── mapper/
│   └── specification/
│
├── user/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
│
├── cart/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
│
├── order/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
│
├── payment/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
│
├── promotion/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
│
└── HuntingFishingStoreApplication.java
```

---

# Architectural Principles

The application is organized around business capabilities rather than technical concerns.

Each feature contains all the components required to implement its own functionality:

* Controllers
* Services
* Repositories
* Domain models (JPA entities)
* DTOs
* Mappers
* Query specifications (when required)

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

## Model

Contains the domain model represented as JPA entities.

Entities define:

* Business data
* Relationships
* Persistence mappings

They should avoid containing application service logic.

---

## DTO

Data Transfer Objects used for communication with external clients.

DTOs isolate the API contract from the persistence model and prevent exposing JPA entities directly.

---

## Mapper

Responsible for converting between entities and DTOs.

Keeping mapping logic isolated improves readability and maintainability.

---

## Specification

Contains Spring Data JPA Specifications used to build dynamic and composable database queries.

This package is optional and only used when filtering or searching becomes sufficiently complex.

---

# Shared Infrastructure

The `common` package contains infrastructure shared across all modules.

Typical components include:

* Spring configuration
* Security configuration
* JWT authentication
* Global exception handling
* Utility classes
* Shared base entities
* Common constants

The `common` package should not contain business logic.

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
