# Hunting & Fishing Store

A modern e-commerce platform for hunting and fishing equipment, built with Spring Boot.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- PostgreSQL
- Maven
- Docker

## Project Structure

```
docs/
src/
docker/
```

## Documentation

Project documentation is available under:

```
docs/architecture/
```

## API Endpoints

The API base path is `/api/v1`.

### Catalog
- `GET /products` - List all products
- `GET /products/{id}` - Get product by ID
- `POST /products` - Create a new product
- `PUT /products/{id}` - Update a product
- `DELETE /products/{id}` - Delete a product
- `GET /brands` - List all brands
- `GET /brands/{id}` - Get brand by ID
- `POST /brands` - Create a new brand
- `PUT /brands/{id}` - Update a brand
- `DELETE /brands/{id}` - Delete a brand
- `GET /categories` - List all categories
- `GET /categories/{id}` - Get category by ID
- `POST /categories` - Create a new category
- `PUT /categories/{id}` - Update a category
- `DELETE /categories/{id}` - Delete a category
- `POST /stock-movements` - Record a stock movement
- `GET /stock-movements/item/{productItemId}` - List movements for a product item

### Users
- `POST /users/register` - Register a new user
- `GET /users/{id}` - Get user profile
- `POST /users/{id}/addresses` - Add an address to a user

### Shopping Cart
- `GET /carts/{userId}` - Get user's cart
- `POST /carts/{userId}/items` - Add item to cart
- `PUT /carts/{userId}/items/{productItemId}?quantity={n}` - Update item quantity
- `DELETE /carts/{userId}/items/{productItemId}` - Remove item from cart
- `DELETE /carts/{userId}` - Clear cart

### Orders
- `POST /orders/checkout/{userId}` - Place an order from cart
- `GET /orders/user/{userId}` - List user's orders
- `GET /orders/{orderNumber}` - Get order details by number

### Payments
- `POST /payments` - Initiate a payment
- `GET /payments/{id}` - Get payment status
- `PATCH /payments/{id}/status?status={STATUS}` - Update payment status

### Coupons & Promotions
- `POST /coupons` - Create a coupon
- `GET /coupons` - List all coupons
- `GET /coupons/{id}` - Get coupon details
- `POST /promotions` - Create a promotion
- `GET /promotions` - List all promotions
- `GET /promotions/{id}` - Get promotion details
- `DELETE /promotions/{id}` - Delete a promotion

## Security

The project uses Spring Security for authentication and authorization.

### Current Configuration (Development)
- **Permit All**: All API endpoints are currently accessible without authentication (`permitAll()`).
- **CSRF**: Cross-Site Request Forgery protection is disabled.
- **Password Encoding**: `BCryptPasswordEncoder` is configured for secure password storage.

## Running the Project

### Start PostgreSQL

```bash
docker compose up -d
```

### Run the application

```bash
./mvnw spring-boot:run
```
