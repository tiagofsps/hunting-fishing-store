# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project follows [Semantic Versioning](https://semver.org/).

## [1.0.0] - 2026-11-16

Initial release of the Hunting & Fishing Store backend.

### Added

- **Authentication & accounts** — registration with auto-login and welcome
  email, JWT login/refresh/logout, `/auth/me`, password reset with one-time
  tokens, profile updates, address book, `ROLE_USER` / `ROLE_ADMIN` RBAC.
- **Security hardening** — refresh tokens in HttpOnly cookies with Redis-backed
  revocation, Redis sliding-window rate limiting, configurable CORS, HSTS and
  frame-options protection.
- **Catalog** — products with purchasable variants, brands, hierarchical
  categories, product images, stock movements, pagination/filtering/sorting and
  localized product search.
- **Localized content** — per-locale category and product translations
  (`en`/`pt`) with the canonical columns as fallback.
- **Cart & checkout** — persistent per-user cart, stock validation, license
  enforcement for regulated products, shipping rules, coupons and enforced
  order status transitions with reverse stock movements on cancellation.
- **Payments** — pluggable gateway with MBWAY, Multibanco, credit card and
  Klarna flows, signature-verified idempotent webhooks, and a scheduled job that
  expires overdue Multibanco references.
- **Marketing** — coupons with usage limits, promotions, newsletter
  subscribe/unsubscribe plus admin filtering, stats, status toggle and CSV
  export.
- **Operations** — Flyway migrations, Actuator health/metrics, OpenAPI metadata
  with bearer auth, ECS structured logging in `prod`, multi-stage Docker image,
  full-stack Docker Compose profile and a GitHub Actions CI workflow.
- **Testing** — unit, repository, controller and integration tiers backed by
  Testcontainers (PostgreSQL + Redis).
