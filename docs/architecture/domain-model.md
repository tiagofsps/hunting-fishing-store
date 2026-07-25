# Domain Model

## Audit Fields

All main entities include the following audit fields:

| Field       | Type            | Description                                         |
| ----------- | --------------- | --------------------------------------------------- |
| `createdAt` | `LocalDateTime` | Automatically generated when the entity is created. |
| `updatedAt` | `LocalDateTime` | Automatically updated whenever the entity changes.  |

---

# 1. Product Catalog Domain

## Brand

### Fields

| Field     | Type     | Notes                       |
| --------- | -------- | --------------------------- |
| `id`      | `Long`   | Primary Key                 |
| `name`    | `String` |                             |
| `slug`    | `String` | **Unique** (e.g. `shimano`) |
| `logoUrl` | `String` |                             |

### Relationships

* One-to-Many → `Product`

---

## Category

### Fields

| Field            | Type       | Notes          |
| ---------------- | ---------- | -------------- |
| `id`             | `Long`     | Primary Key    |
| `name`           | `String`   |                |
| `slug`           | `String`   | **Unique**     |
| `description`    | `String`   |                |
| `parentCategory` | `Category` | Self-reference |

### Relationships

* Many-to-One → `Category` (Parent Category)
* One-to-Many → `Category` (Child Categories)

---

## Product

### Fields

| Field             | Type      | Notes                                 |
| ----------------- | --------- | ------------------------------------- |
| `id`              | `Long`    | Primary Key                           |
| `name`            | `String`  |                                       |
| `slug`            | `String`  | **Unique**                            |
| `description`     | `Text`    |                                       |
| `requiresLicense` | `Boolean` | Hunting / ammunition license required |

### Relationships

* Many-to-One → `Category`
* Many-to-One → `Brand`
* One-to-Many → `ProductItem`
* One-to-Many → `ProductImage`

---

## ProductItem (SKU)

### Fields

| Field           | Type         | Notes                                       |
| --------------- | ------------ | ------------------------------------------- |
| `id`            | `Long`       | Primary Key                                 |
| `sku`           | `String`     | **Unique**                                  |
| `price`         | `BigDecimal` | Base catalog price                          |
| `stockQuantity` | `Integer`    |                                             |
| `weight`        | `Double`     |                                             |
| `active`        | `Boolean`    | Soft delete / Catalog visibility            |
| `attributes`    | `JSONB`      | Flexible technical attributes (GIN indexed) |

### Relationships

* Many-to-One → `Product`
* One-to-Many → `StockMovement`
* One-to-Many → `ProductImage` (Optional)

---

## ProductImage

### Fields

| Field          | Type      | Notes       |
| -------------- | --------- | ----------- |
| `id`           | `Long`    | Primary Key |
| `imageUrl`     | `String`  |             |
| `altText`      | `String`  |             |
| `isPrimary`    | `Boolean` |             |
| `displayOrder` | `Integer` |             |

### Relationships

* Many-to-One → `Product` (Required)
* Many-to-One → `ProductItem` (Optional)

---

## StockMovement

### Fields

| Field      | Type      | Notes                              |
| ---------- | --------- | ---------------------------------- |
| `id`       | `Long`    | Primary Key                        |
| `quantity` | `Integer` | Positive or negative               |
| `type`     | `Enum`    | PURCHASE, SALE, RETURN, ADJUSTMENT |
| `notes`    | `String`  |                                    |

### Relationships

* Many-to-One → `ProductItem`
* Many-to-One → `User` (Optional)

---

# 2. Promotion Domain

## Promotion

### Fields

| Field           | Type            | Notes                    |
| --------------- | --------------- | ------------------------ |
| `id`            | `Long`          | Primary Key              |
| `name`          | `String`        |                          |
| `discountType`  | `Enum`          | PERCENTAGE, FIXED_AMOUNT |
| `discountValue` | `BigDecimal`    |                          |
| `priority`      | `Integer`       |                          |
| `stackable`     | `Boolean`       |                          |
| `validFrom`     | `LocalDateTime` |                          |
| `validTo`       | `LocalDateTime` |                          |
| `active`        | `Boolean`       |                          |

### Relationships

* One-to-Many → `PromotionRule`

---

## PromotionRule

### Fields

| Field        | Type   | Notes                      |
| ------------ | ------ | -------------------------- |
| `id`         | `Long` | Primary Key                |
| `targetType` | `Enum` | CATEGORY, BRAND, PRODUCT   |
| `targetId`   | `Long` | Target resource identifier |

### Relationships

* Many-to-One → `Promotion`

---

# 3. User & Security Domain

## User

### Fields

| Field                  | Type      | Notes       |
| ---------------------- | --------- | ----------- |
| `id`                   | `Long`    | Primary Key |
| `email`                | `String`  | **Unique**  |
| `password`             | `String`  |             |
| `firstName`            | `String`  |             |
| `lastName`             | `String`  |             |
| `phone`                | `String`  |             |
| `huntingLicenseNumber` | `String`  | Optional    |
| `fishingLicenseNumber` | `String`  | Optional    |
| `enabled`              | `Boolean` |             |

### Relationships

* One-to-Many → `Address`
* One-to-Many → `Order`
* One-to-Many → `CouponUsage`

---

## Address

### Fields

| Field            | Type              | Notes              |
| ---------------- | ----------------- | ------------------ |
| `id`             | `Long`            | Primary Key        |
| `recipientName`  | `String`          |                    |
| `recipientPhone` | `String`          |                    |
| `street`         | `String`          |                    |
| `city`           | `String`          |                    |
| `postalCode`     | `String`          |                    |
| `countryCode`    | `String` / `Enum` | ISO 3166-1 alpha-2 |
| `addressType`    | `Enum`            | SHIPPING, BILLING  |

### Relationships

* Many-to-One → `User`

---

# 4. Sales, Checkout & Payment Domain

## Coupon

### Fields

| Field               | Type            | Notes                    |
| ------------------- | --------------- | ------------------------ |
| `id`                | `Long`          | Primary Key              |
| `code`              | `String`        | **Unique**               |
| `discountType`      | `Enum`          | PERCENTAGE, FIXED_AMOUNT |
| `discountValue`     | `BigDecimal`    |                          |
| `minOrderAmount`    | `BigDecimal`    |                          |
| `usageLimit`        | `Integer`       |                          |
| `usageLimitPerUser` | `Integer`       |                          |
| `validFrom`         | `LocalDateTime` |                          |
| `validTo`           | `LocalDateTime` |                          |

### Relationships

* One-to-Many → `CouponUsage`

---

## CouponUsage

### Fields

| Field    | Type            | Notes       |
| -------- | --------------- | ----------- |
| `id`     | `Long`          | Primary Key |
| `usedAt` | `LocalDateTime` |             |

### Relationships

* Many-to-One → `Coupon`
* Many-to-One → `User`
* Many-to-One → `Order`

---

## Cart

### Fields

| Field       | Type            | Notes       |
| ----------- | --------------- | ----------- |
| `id`        | `Long`          | Primary Key |
| `createdAt` | `LocalDateTime` |             |
| `updatedAt` | `LocalDateTime` |             |

### Relationships

* One-to-One → `User`
* One-to-Many → `CartItem`

---

## CartItem

### Fields

| Field      | Type      | Notes       |
| ---------- | --------- | ----------- |
| `id`       | `Long`    | Primary Key |
| `quantity` | `Integer` |             |

### Relationships

* Many-to-One → `Cart`
* Many-to-One → `ProductItem`

---

## Order

### Fields

| Field            | Type            | Notes                                        |
| ---------------- | --------------- | -------------------------------------------- |
| `id`             | `Long`          | Primary Key                                  |
| `orderNumber`    | `String`        | **Unique**                                   |
| `orderDate`      | `LocalDateTime` |                                              |
| `status`         | `Enum`          | PENDING, PAID, SHIPPED, DELIVERED, CANCELLED |
| `totalAmount`    | `BigDecimal`    |                                              |
| `discountAmount` | `BigDecimal`    |                                              |
| `shippingCost`   | `BigDecimal`    |                                              |

### Relationships

* Many-to-One → `User`
* Many-to-One → `Address` (Shipping Address)
* Many-to-One → `Address` (Billing Address)
* One-to-Many → `OrderItem`
* One-to-Many → `Payment`

---

## OrderItem

### Fields

| Field       | Type         | Notes              |
| ----------- | ------------ | ------------------ |
| `id`        | `Long`       | Primary Key        |
| `quantity`  | `Integer`    |                    |
| `unitPrice` | `BigDecimal` | Frozen at checkout |

### Relationships

* Many-to-One → `Order`
* Many-to-One → `ProductItem`

---

## Payment

### Fields

| Field           | Type         | Notes                                         |
| --------------- | ------------ | --------------------------------------------- |
| `id`            | `Long`       | Primary Key                                   |
| `paymentMethod` | `Enum`       | MBWAY, MULTIBANCO, CREDIT_CARD, PAYPAL        |
| `status`        | `Enum`       | PENDING, COMPLETED, FAILED, EXPIRED, REFUNDED |
| `amount`        | `BigDecimal` |                                               |
| `transactionId` | `String`     |                                               |

### Relationships

* Many-to-One → `Order`
