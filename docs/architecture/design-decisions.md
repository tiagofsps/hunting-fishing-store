# Design Decisions

## DD-001 — Product vs ProductItem

Status: Accepted

Reason: products can have multiple purchasable variants. The `Product` carries
catalog identity (name, brand, category) while `ProductItem` carries the SKU,
price and stock.

---

## DD-002 — Product attributes stored as JSONB

Status: Accepted

Reason: different categories need different technical specifications. A JSONB
column with a GIN index avoids a wide, sparse attribute table without giving up
query performance.

---

## DD-003 — Persistent shopping cart

Status: Accepted

Reason: a cart persisted per user allows recovery across devices and lets
checkout work from any client.

---

## DD-004 — JWT access token + refresh token in an HttpOnly cookie

Status: Accepted

Reason: access tokens stay short-lived and stateless for the API, while the
refresh token is kept out of JavaScript reach (HttpOnly, SameSite) to reduce the
impact of XSS. Refresh tokens are long-lived but revocable.

---

## DD-005 — Redis for revocation, rate limiting and reset tokens

Status: Accepted

Reason: these are ephemeral, high-churn values that benefit from TTLs and atomic
counters. Keeping them out of PostgreSQL avoids write amplification and lets the
app scale horizontally without sticky sessions.

---

## DD-006 — Feature-based modular monolith

Status: Accepted

Reason: the modules share a database and deployment but keep clear boundaries,
so the codebase stays navigable and each feature can evolve independently
without the operational cost of microservices.

---

## DD-007 — Flyway in production, ddl-auto in development

Status: Accepted

Reason: `prod` runs with `ddl-auto: validate` against versioned Flyway
migrations, so schema changes are reviewed and reproducible. Development keeps
`ddl-auto: update` for fast iteration.

---

## DD-008 — Content translations in dedicated tables

Status: Accepted

Reason: `category_translations` / `product_translations` keep one row per
`(entity, locale)` with a unique constraint, which scales to more languages
without widening the entity tables. The canonical `name`/`description` columns
remain as the fallback for clients that do not send a locale.

---

## DD-009 — Pluggable payment gateway

Status: Accepted

Reason: payment flows sit behind a `PaymentGateway` interface with a stub
implementation, so a real PSP can be added without touching order or payment
domain logic. The active implementation is selected via `PAYMENT_GATEWAY`.

---

## DD-010 — Payment amount always comes from the order

Status: Accepted

Reason: trusting a client-supplied amount is a direct path to price
manipulation. The payment service reads the amount from the persisted order.

---

## DD-011 — License enforcement at checkout

Status: Accepted

Reason: regulated products must only be sold to customers who prove the
matching license. The check runs before anything is persisted, so an invalid
cart aborts the whole checkout rather than leaving partial orders.

---

## DD-012 — Best-effort transactional email

Status: Accepted

Reason: email delivery is not part of the transaction. Sending is attempted and
failures are logged, so a down SMTP server never blocks registration, checkout
or password reset.

---

## DD-013 — Spring `Page` envelope for list endpoints

Status: Accepted

Reason: clients need total counts and page metadata for pagination; returning
the standard Spring `Page` shape keeps the API consistent across list endpoints
and documented automatically by springdoc.
