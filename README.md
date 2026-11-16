# Hunting & Fishing Store

![CI](https://github.com/tiagofsps/hunting-fishing-store/actions/workflows/ci.yml/badge.svg)
![Java 21](https://img.shields.io/badge/Java-21-blue)
![Spring Boot 4.1](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)

A modern e-commerce platform for hunting and fishing equipment, built with Spring Boot 4 and Java 21.

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.1
- **Security**: Spring Security, OAuth2 Resource Server (JWT)
- **Database**: PostgreSQL (Primary), Redis (Token storage/Revocation, password-reset tokens)
- **ORM**: Spring Data JPA / Hibernate
- **Validation**: Jakarta Bean Validation
- **Emails**: Spring Mail (`JavaMailSender`, best-effort and non-blocking)
- **API Docs**: springdoc-openapi 3.x (Swagger UI at `/swagger-ui.html`)
- **Ops**: Spring Boot Actuator (`/actuator/health`), ECS structured logging in `prod`
- **Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL & Redis)
- **Build Tool**: Maven

## Security Configuration

The project implements a robust security architecture based on Spring Security 6:

- **Stateless Authentication**: No server-side session state is maintained. Authentication is performed via JWT (JSON Web Tokens).
- **Dual-Token Strategy**:
  - **Access Tokens**: Short-lived JWTs included in the `Authorization: Bearer` header.
  - **Refresh Tokens**: Long-lived JWTs stored in **secure HttpOnly, SameSite cookies** to prevent XSS-based theft.
- **Stateful Token Revocation**: Uses **Redis** to store and manage refresh token IDs. This allows for instant session revocation on logout or suspicious activity detection.
- **RBAC (Role-Based Access Control)**:
  - Supports `ROLE_USER` and `ROLE_ADMIN`.
  - Granular access control using `@EnableMethodSecurity` (`@PreAuthorize("hasRole('ADMIN')")`).
  - **Public catalog reads**: `GET` on products, brands, categories and promotions is open to everyone; all write operations are admin-only.
- **Rate Limiting**: Redis-backed sliding-window throttling.
  - A servlet filter limits **per-IP** attempts on the public POST endpoints (`/auth/login`, `/auth/register`, `/auth/refresh`, `/auth/forgot-password`, `/newsletter/subscribe`) and returns `429 Too Many Requests`.
  - **Per-email** limits are enforced in the controllers for login, register and forgot-password (no account enumeration).
- **CORS**: Configurable allowed origins (`CORS_ALLOWED_ORIGINS`, comma-separated), credentials enabled, `Authorization`/`Content-Type` headers.
- **Seeding**: On an empty database, 20 demo users are seeded plus two fixed demo accounts (see [Demo Accounts](#demo-accounts)). `ADMIN_EMAIL`/`ADMIN_PASSWORD` override the admin account's credentials (required in `prod`).
- **Encryption**: Passwords are hashed using **BCrypt** with a configurable strength.
- **Protection**:
  - **HSTS** (HTTP Strict Transport Security) enabled.
  - **Frame Options** set to `DENY` to prevent Clickjacking.
  - **CSRF** disabled for the stateless API.

## Demo Accounts

On a fresh database (dev profile), the seeder creates two fixed accounts with the generic password `Password123!`:

| Account | Email | Role | Notes |
|---|---|---|---|
| Admin | `admin@huntingfishing.com` | `ROLE_ADMIN` | Full admin access; credentials overridable via `ADMIN_EMAIL`/`ADMIN_PASSWORD` |
| User | `user@huntingfishing.com` | `ROLE_USER` | Includes demo hunting (`DEMO-HUNT-0001`) and fishing (`DEMO-FISH-0001`) licenses plus shipping/billing addresses |

The 20 additional demo users share the password `password123`.

## Project Structure

The project follows a domain-driven package structure:

```
src/main/java/com/tiagofsps/hunting_fishing_store/
├── auth/           # JWT, Refresh Tokens, Login/Registration
├── cart/           # Shopping Cart logic
├── catalog/        # Products, Brands, Categories, Stock
├── common/         # Shared utilities and exception handling
├── config/         # Security, Redis, and App configurations
├── coupon/         # Coupon management
├── newsletter/     # Newsletter subscriptions
├── order/          # Order processing and checkout
├── payment/        # Payment integration
├── promotion/      # Campaign and promotion logic
└── user/           # User profiles and addresses
```

## API Endpoints

The API base path is `/api/v1`. Interactive docs are available on Swagger UI (`/swagger-ui.html`) and OpenAPI JSON (`/v3/api-docs`).

### Authentication
- `POST /auth/register` - Create a new user account (auto-login; welcome email)
- `POST /auth/login` - Authenticate and receive Access Token (Refresh Token in HttpOnly cookie)
- `POST /auth/refresh` - Refresh Access Token using the refresh cookie
- `POST /auth/logout` - Revoke tokens and end session
- `GET /auth/me` - Current user profile
- `POST /auth/forgot-password` - Send a password-reset email (always 202, rate-limited, no user enumeration)
- `POST /auth/reset-password` - Reset the password with a one-time token (15 min TTL)

### Users
- `GET /users` - [ADMIN] Paginated user list (`page`, `size`, `sort`)
- `GET /users/{id}` - Get user profile (owner or ADMIN; includes addresses and role)
- `POST /users/{id}/addresses` - Add an address to a user
- `PUT /users/{id}/addresses/{addressId}` - Update an address
- `DELETE /users/{id}/addresses/{addressId}` - Delete an address
- `POST /users/{id}/password` - Change password (owner-only)
- `PUT /users/{id}` - Update profile

### Catalog Management

#### Products
- `GET /products` - Paginated product list with filters and sorting
  - Query params: `page`, `size` (1–100), `sort` (`name`|`createdAt`), `direction` (`asc`|`desc`), `categoryId`, `brandId`, `q` (search)
  - Returns a Spring `Page` JSON envelope: `{ "content": [...], "totalElements", "totalPages", "number", "size", ... }`
- `GET /products/{id}` - Get product details
- `POST /products` - [ADMIN] Create a new product
- `PUT /products/{id}` - [ADMIN] Update a product
- `DELETE /products/{id}` - [ADMIN] Delete a product

#### Brands
- `GET /brands` - List all brands
- `GET /brands/{id}` - Get brand details
- `POST /brands` - [ADMIN] Create a new brand
- `PUT /brands/{id}` - [ADMIN] Update a brand
- `DELETE /brands/{id}` - [ADMIN] Delete a brand

#### Categories
- `GET /categories` - List all categories (each includes `productCount`)
- `GET /categories/{id}` - Get category details
- `POST /categories` - [ADMIN] Create a new category
- `PUT /categories/{id}` - [ADMIN] Update a category
- `DELETE /categories/{id}` - [ADMIN] Delete a category

#### Stock & Inventory
- `POST /stock-movements` - [ADMIN] Record a stock movement
- `GET /stock-movements/item/{productItemId}` - [ADMIN] List movements for a product item

### Shopping Cart
- `GET /carts/{userId}` - Retrieve user cart
- `POST /carts/{userId}/items` - Add item to cart
- `PUT /carts/{userId}/items/{productItemId}` - Update quantity of an item in cart
- `DELETE /carts/{userId}/items/{productItemId}` - Remove item from cart
- `DELETE /carts/{userId}` - Clear the entire cart

### Orders & Checkout
- `POST /orders/checkout/{userId}` - Place an order from current cart (confirmation email)
- `GET /orders/user/{userId}` - List user's order history
- `GET /orders/{orderNumber}` - Get order details by order number
- `GET /orders` - [ADMIN] Paginated order list with optional `status` filter (`page`, `size`, `sort`)
- `PATCH /orders/{orderNumber}/status?status=...` - [ADMIN] Transition an order status

Checkout rules:
- **Stock validation**: every line item is checked against available stock before anything is persisted; insufficient stock aborts the whole checkout.
- **License enforcement**: products flagged `requiresLicense` can only be purchased by users whose profile carries the matching license number (`huntingLicenseNumber` for `HUNTING` products, `fishingLicenseNumber` for `FISHING`; legacy products without a `licenseType` accept either).
- **Shipping**: a flat fee (`SHIPPING_FEE`) applies below the free-shipping threshold (`SHIPPING_FREE_THRESHOLD`), computed on the subtotal before coupon discount.
- **Coupons**: an optional `couponCode` is validated and applied; usage is recorded.
- **Status transitions** are enforced: `PENDING → PAID|CANCELLED`, `PAID → SHIPPED|CANCELLED`, `SHIPPED → DELIVERED`; `DELIVERED` and `CANCELLED` are terminal. Cancelling an order records reverse stock movements (`RETURN`) for every line item.

### Payments
- `POST /payments` - Initiate a payment (amount is read from the order server-side, never from the client)
- `GET /payments/{id}` - Get payment status
- `PATCH /payments/{id}/status` - [ADMIN] Update payment status
- `POST /payments/webhook` - Provider webhook callback (signature-verified)

Supported methods: `MBWAY` (requires `mbwayPhone`), `MULTIBANCO` (returns `multibancoEntity`/`multibancoReference` with a 48h expiry), `CREDIT_CARD` and `KLARNA` (returns a hosted `paymentUrl`). The gateway is pluggable via `PAYMENT_GATEWAY` (default `stub`).

Behavior:
- A payment can only be initiated for a `PENDING` order, and only one pending payment per order is allowed.
- Completing a payment (admin update or webhook) transitions the order to `PAID`.
- The webhook is idempotent: re-deliveries after a successful completion are ignored.
- Overdue `PENDING` Multibanco references are marked `EXPIRED` by a scheduled job (default hourly, `PAYMENT_EXPIRY_CRON`).

### Discounts & Marketing

#### Coupons
- `POST /coupons` - [ADMIN] Create a new coupon
- `GET /coupons` - [ADMIN] List all coupons
- `GET /coupons/{id}` - [ADMIN] Get coupon details

#### Promotions
- `POST /promotions` - [ADMIN] Create a new promotion
- `GET /promotions` - List all active promotions
- `GET /promotions/{id}` - Get promotion details
- `DELETE /promotions/{id}` - [ADMIN] Delete a promotion

### Newsletter
- `POST /newsletter/subscribe` - Subscribe (public)
- `DELETE /newsletter/subscribe` - Unsubscribe (public)
- `GET /newsletter/subscriptions` - [ADMIN] Paginated subscription list

### Operations
- `GET /actuator/health` - Health/liveness/readiness probes (public)
- `GET /actuator/metrics` - [ADMIN] App metrics
- `GET /actuator/info` - [ADMIN] Build info
- `GET /v3/api-docs` - OpenAPI JSON
- `GET /swagger-ui.html` - Swagger UI

## Emails

Transactional emails are sent best-effort through Spring Mail in `dev`/`prod` when configured. Sending never blocks or fails the business flow (failures are logged and ignored), and only runs if a host is configured:

- **Welcome email** on registration.
- **Order confirmation** (order number, total, status) on successful checkout.
- **Password reset** email with a one-time reset link.

Set `SPRING_MAIL_HOST`/`SPRING_MAIL_PORT`/`SPRING_MAIL_USERNAME`/`SPRING_MAIL_PASSWORD` and `MAIL_FROM` to enable. If empty, emails are logged to the console instead.

## Observability

- Actuator exposes `health`, `info` and `metrics`. Health probes are enabled for container orchestration (`/actuator/health/liveness`, `/actuator/health/readiness`).
- The `prod` profile enables ECS structured JSON logging via `logging.structured.format.console` (configurable with `STRUCTURED_LOG_FORMAT`).

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `DATABASE_URL` / `DATABASE_USER` / `DATABASE_PASSWORD` | `jdbc:postgresql://localhost:5432/hunting_fishing_store` / `postgres` / `postgres` | PostgreSQL connection |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `localhost` / `6379` / *(empty)* | Redis connection |
| `JWT_SECRET_KEY` | — | HMAC secret for signing JWTs (required in `prod`) |
| `JWT_ACCESS_EXPIRATION` | `15m` | Access-token TTL |
| `JWT_REFRESH_EXPIRATION_SECONDS` | `604800` | Refresh-token TTL (seconds) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |
| `RATE_LIMIT_MAX_ATTEMPTS` / `RATE_LIMIT_WINDOW_SECONDS` | `5` / `900` | Login/register/forgot-password throttle |
| `COOKIE_SECURE` / `COOKIE_SAME_SITE` | `false` / `Strict` | Refresh-token cookie flags |
| `SHIPPING_FREE_THRESHOLD` / `SHIPPING_FEE` | `75` / `4.95` | Free-shipping threshold and flat fee |
| `PASSWORD_RESET_BASE_URL` | `http://localhost:5173/reset-password` | Frontend base URL used in reset emails |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | *(empty)* | Override the seeded admin account's credentials (default `admin@huntingfishing.com` / `Password123!`); required in `prod` |
| `PAYMENT_GATEWAY` | `stub` | Payment gateway implementation (`stub` for now) |
| `PAYMENT_WEBHOOK_SECRET` | `local-webhook-secret-change-me` | Shared secret used to verify webhook signatures |
| `PAYMENT_CHECKOUT_BASE_URL` | `http://localhost:5173` | Base URL for hosted checkout (KLARNA/CREDIT_CARD) |
| `PAYMENT_MULTIBANCO_ENTITY` / `PAYMENT_MULTIBANCO_EXPIRY_HOURS` | `21345` / `48` | Multibanco entity and reference expiry window |
| `PAYMENT_EXPIRY_CRON` | `0 0 * * * *` | Cron schedule for expiring overdue Multibanco references |
| `MAIL_FROM` | — | Sender address for emails |
| `SPRING_MAIL_HOST` / `SPRING_MAIL_PORT` / `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` | *(empty)* | SMTP settings (emails disabled when host empty) |
| `STRUCTURED_LOG_FORMAT` | `ecs` | Structured log format in `prod` |

## Frontend Mapping Notes

Field names in API responses follow backend domain terms. Map them to frontend display fields as needed:

- **Promotions**: `name` → promo title, `discountValue` → discount percentage, `validFrom` → start date, `validTo` → end date.
- **Categories** include `productCount` → admin/dashboard category listings.
- **Products list** returns a Spring `Page` envelope — the frontend must read `content`, `totalElements`, `totalPages` (not a plain array).
- **Payments**: the total is always taken from the server-side order; never display or send a client-supplied amount. `paymentMethod` values are `MBWAY`, `MULTIBANCO`, `CREDIT_CARD`, `KLARNA`; MBWAY payments require `mbwayPhone`.
- **Licenses**: `huntingLicenseNumber`/`fishingLicenseNumber` on the user profile are required to purchase regulated products (`requiresLicense`); prompt users to fill them in before checkout.

## Development Setup

### 1. Start Infrastructure
```bash
docker compose up -d
```
Starts PostgreSQL 17 and Redis 7.

### 2. Run the App
```bash
./mvnw spring-boot:run
```

### 3. Reset / Seed the Database

Convenience targets (require `make` and a `psql` client on the host; no Docker CLI needed):

- `make db-reset` - **Destructive**: drops and recreates the database (data loss). Credentials default to the `.env.example` values and can be overridden via `POSTGRES_*` env vars or a `.env` file.
- `make db-seed` - Reset the database, then start the app so the dev seeders run (demo users + fixed accounts + catalog).
- `make db-setup` - Alias for `db-seed`.
- `make db-shell` - Open an interactive `psql` session.

Seeding only runs on an empty database in the `dev` profile, so `make db-seed` after a reset reproduces a consistent dataset.

## Documentation

- [Development Guide](docs/development.md) - profiles, running the app, database lifecycle, configuration, and common pitfalls.
- [Testing Guide](docs/testing.md) - test tiers, base classes, commands, and how to write tests.
- [Architecture](docs/architecture/README.md) - domain model, technology stack, design decisions, and coding conventions.

## License / Usage

This repository is published for **portfolio and review purposes only** and is
**not open source**. It is proprietary: all rights reserved. See [LICENSE](LICENSE)
for the full notice. No permission is granted to use, copy, modify, or
distribute the code without prior written permission from the copyright holder.

