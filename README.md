# Hunting & Fishing Store

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
  - Granular access control using `@EnableMethodSecurity`.
- **Encryption**: Passwords are hashed using **BCrypt** with a configurable strength.
- **Protection**:
  - **HSTS** (HTTP Strict Transport Security) enabled.
  - **Frame Options** set to `DENY` to prevent Clickjacking.
  - **CSRF** disabled for the stateless API.

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
- `GET /users/{id}` - Get user profile
- `GET /users/{id}/orders` - List a user's orders
- `POST /users/{id}/addresses` - Add an address to a user
- `GET /users/{id}/addresses` - List a user's addresses
- `DELETE /users/{id}/addresses/{addressId}` - Delete an address
- `PATCH /users/{id}/password` - Change password
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
- `GET /stock-movements/item/{productItemId}` - List movements for a product item

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

### Payments
- `POST /payments` - Initiate a payment (amount is read from the order server-side, never from the client)
- `GET /payments/{id}` - Get payment status
- `PATCH /payments/{id}/status` - [ADMIN] Update payment status
- `POST /payments/webhook` - Provider webhook callback (signature-verified)

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
| `MAIL_FROM` | — | Sender address for emails |
| `SPRING_MAIL_HOST` / `SPRING_MAIL_PORT` / `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` | *(empty)* | SMTP settings (emails disabled when host empty) |
| `STRUCTURED_LOG_FORMAT` | `ecs` | Structured log format in `prod` |

## Frontend Mapping Notes

Field names in API responses follow backend domain terms. Map them to frontend display fields as needed:

- **Promotions**: `name` → promo title, `discountValue` → discount percentage, `validFrom` → start date, `validTo` → end date.
- **Categories** include `productCount` → admin/dashboard category listings.
- **Products list** returns a Spring `Page` envelope — the frontend must read `content`, `totalElements`, `totalPages` (not a plain array).
- **Payments**: the total is always taken from the server-side order; never display or send a client-supplied amount.

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
