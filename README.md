# Hunting & Fishing Store

A modern e-commerce platform for hunting and fishing equipment, built with Spring Boot 3 and Java 21.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.4
- **Security**: Spring Security, OAuth2 Resource Server (JWT)
- **Database**: PostgreSQL (Primary), Redis (Token storage/Revocation)
- **ORM**: Spring Data JPA / Hibernate
- **Validation**: Jakarta Bean Validation
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

The API base path is `/api/v1`.

### Authentication
- `POST /auth/register` - Create a new user account
- `POST /auth/login` - Authenticate and receive Access Token (Refresh Token in HttpOnly cookie)
- `POST /auth/refresh` - Refresh Access Token using the refresh cookie
- `POST /auth/logout` - Revoke tokens and end session

### Users
- `GET /users/{id}` - Get user profile
- `POST /users/{id}/addresses` - Add an address to a user

### Catalog Management

#### Products
- `GET /products` - List all products
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
- `GET /categories` - List all categories
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
- `POST /orders/checkout/{userId}` - Place an order from current cart
- `GET /orders/user/{userId}` - List user's order history
- `GET /orders/{orderNumber}` - Get order details by order number

### Payments
- `POST /payments` - Initiate a payment
- `GET /payments/{id}` - Get payment status
- `PATCH /payments/{id}/status` - Update payment status

### Discounts & Marketing

#### Coupons
- `POST /coupons` - [ADMIN] Create a new coupon
- `GET /coupons` - List all coupons
- `GET /coupons/{id}` - Get coupon details

#### Promotions
- `POST /promotions` - [ADMIN] Create a new promotion
- `GET /promotions` - List all promotions
- `GET /promotions/{id}` - Get promotion details
- `DELETE /promotions/{id}` - [ADMIN] Delete a promotion

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
