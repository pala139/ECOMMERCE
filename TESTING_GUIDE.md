# API Testing Guide

Complete guide for testing the e-commerce REST API with real examples.

## Setup

### 1. Start the Application

```bash
cd /home/pala/Documents/ecommerce
mvn clean package
java -jar target/ecommerce-backend-1.0.0.jar
```

Server runs on: `http://localhost:8080`

### 2. Create Database

```bash
mysql -u root -p
CREATE DATABASE ecommerce_db;
```

Or if using your configured user:
```bash
mysql -u root -p ecommerce_db < schema.sql
```

### 3. Create Some Initial Data

```bash
mysql -u root -p ecommerce_db
INSERT INTO categories (name, description, created_at) VALUES 
  ('Electronics', 'Electronic devices', NOW()),
  ('Books', 'Various books', NOW());

INSERT INTO products (name, description, price, stock_quantity, category_id, created_at, updated_at) VALUES
  ('Laptop', 'High-performance laptop', 999.99, 10, 1, NOW(), NOW()),
  ('Mouse', 'Wireless mouse', 29.99, 50, 1, NOW(), NOW()),
  ('Keyboard', 'Mechanical keyboard', 79.99, 25, 1, NOW(), NOW()),
  ('Java Book', 'Effective Java', 49.99, 15, 2, NOW(), NOW());
```

---

## Testing with cURL

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123",
    "name": "John Doe",
    "phoneNumber": "555-1234"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john@example.com",
  "name": "John Doe",
  "userId": 1
}
```

**Save the token:**
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

---

### 2. Test Duplicate Registration

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "anotherPassword",
    "name": "Jane Doe"
  }'
```

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2026-01-05T10:30:00",
  "status": 400,
  "message": "Registration failed: Email already registered",
  "path": "/auth/register"
}
```

---

### 3. Login with Correct Password

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

**Expected Response:** 200 OK
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john@example.com",
  "name": "John Doe",
  "userId": 1
}
```

---

### 4. Test Wrong Password

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "wrongPassword"
  }'
```

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2026-01-05T10:30:00",
  "status": 400,
  "message": "Login failed: Invalid password",
  "path": "/auth/login"
}
```

---

## Testing Products (Public Endpoints)

### 5. Get All Products

```bash
curl -X GET http://localhost:8080/products
```

**Expected Response:** 200 OK
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "stockQuantity": 10,
    "categoryId": 1,
    "categoryName": "Electronics"
  },
  {
    "id": 2,
    "name": "Mouse",
    "description": "Wireless mouse",
    "price": 29.99,
    "stockQuantity": 50,
    "categoryId": 1,
    "categoryName": "Electronics"
  }
]
```

---

### 6. Get Single Product

```bash
curl -X GET http://localhost:8080/products/1
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "stockQuantity": 10,
  "categoryId": 1,
  "categoryName": "Electronics"
}
```

---

### 7. Search Products

```bash
curl -X GET "http://localhost:8080/products/search?keyword=laptop"
```

**Expected Response:** 200 OK
```json
[
  {
    "id": 1,
    "name": "Laptop",
    ...
  }
]
```

---

### 8. Get Products by Category

```bash
curl -X GET http://localhost:8080/products/category/1
```

**Expected Response:** 200 OK (all products in Electronics category)
```json
[
  { "id": 1, "name": "Laptop", ... },
  { "id": 2, "name": "Mouse", ... },
  { "id": 3, "name": "Keyboard", ... }
]
```

---

### 9. Create Product (with auth)

```bash
curl -X POST http://localhost:8080/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monitor",
    "description": "4K Monitor",
    "price": 399.99,
    "stockQuantity": 5,
    "categoryId": 1
  }'
```

**Expected Response:** 201 Created
```json
{
  "id": 5,
  "name": "Monitor",
  "description": "4K Monitor",
  "price": 399.99,
  "stockQuantity": 5,
  "categoryId": 1,
  "categoryName": "Electronics"
}
```

---

## Testing Orders (Protected Endpoints)

### 10. Create an Order

```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productId": 1,
        "quantity": 1
      },
      {
        "productId": 2,
        "quantity": 2
      }
    ],
    "shippingAddress": "123 Main Street",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZipCode": "10001"
  }'
```

**Expected Response:** 201 Created
```json
{
  "id": 1,
  "orderNumber": "ORD-ABC12345",
  "totalAmount": 1059.97,
  "status": "PENDING",
  "createdAt": "2026-01-05T10:30:00",
  "shippingAddress": "123 Main Street",
  "shippingCity": "New York",
  "shippingState": "NY",
  "shippingZipCode": "10001",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop",
      "quantity": 1,
      "priceAtPurchase": 999.99,
      "subtotal": 999.99
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Mouse",
      "quantity": 2,
      "priceAtPurchase": 29.99,
      "subtotal": 59.98
    }
  ]
}
```

---

### 11. Get All User Orders

```bash
curl -X GET http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** 200 OK
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-ABC12345",
    "totalAmount": 1059.97,
    "status": "PENDING",
    ...
  }
]
```

---

### 12. Get Specific Order

```bash
curl -X GET http://localhost:8080/orders/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** 200 OK (full order details with items)

---

### 13. Update Order Status

```bash
curl -X PUT http://localhost:8080/orders/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "orderNumber": "ORD-ABC12345",
  "status": "CONFIRMED",
  ...
}
```

Valid statuses:
- PENDING (default)
- CONFIRMED
- SHIPPED
- DELIVERED
- CANCELLED

---

## Testing User Profile

### 14. Get User Profile

```bash
curl -X GET http://localhost:8080/users/profile \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "email": "john@example.com",
  "name": "John Doe",
  "phoneNumber": "555-1234",
  "address": null,
  "city": null,
  "state": null,
  "zipCode": null,
  "createdAt": "2026-01-05T10:30:00",
  "updatedAt": "2026-01-05T10:30:00"
}
```

---

### 15. Update User Profile

```bash
curl -X PUT http://localhost:8080/users/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe Updated",
    "phoneNumber": "555-5678",
    "address": "456 Oak Avenue",
    "city": "Boston",
    "state": "MA",
    "zipCode": "02101"
  }'
```

**Expected Response:** 200 OK (updated user data)

---

## Testing Categories

### 16. Get All Categories

```bash
curl -X GET http://localhost:8080/categories
```

**Expected Response:** 200 OK
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices",
    "createdAt": "2026-01-05T10:30:00"
  },
  {
    "id": 2,
    "name": "Books",
    "description": "Various books",
    "createdAt": "2026-01-05T10:30:00"
  }
]
```

---

### 17. Create Category

```bash
curl -X POST http://localhost:8080/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Clothing",
    "description": "Apparel and accessories"
  }'
```

**Expected Response:** 201 Created

---

## Testing Authentication/Authorization

### 18. Test Missing Token

```bash
curl -X GET http://localhost:8080/orders
```

**Expected Response:** 401 Unauthorized
(Endpoint requires authentication but no token provided)

---

### 19. Test Invalid Token

```bash
curl -X GET http://localhost:8080/orders \
  -H "Authorization: Bearer invalidtoken123"
```

**Expected Response:** 401 Unauthorized
(Token signature doesn't match)

---

### 20. Test Expired Token

If you wait long enough or modify JWT secret in application.properties:

```bash
curl -X GET http://localhost:8080/orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Expected Response:** 401 Unauthorized
(Token expired)

---

## Scenario: Complete Purchase Flow

This scenario walks through a complete customer journey:

### Step 1: Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "Password123!",
    "name": "Jane Customer",
    "phoneNumber": "555-0000"
  }'

# Save token
TOKEN="<token_from_response>"
```

### Step 2: Browse Products
```bash
curl -X GET http://localhost:8080/products
# Review product list, find items you like
```

### Step 3: Search for Specific Product
```bash
curl -X GET "http://localhost:8080/products/search?keyword=keyboard"
```

### Step 4: View Product Details
```bash
curl -X GET http://localhost:8080/products/3
```

### Step 5: Create Order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 3, "quantity": 1}
    ],
    "shippingAddress": "789 Oak Street",
    "shippingCity": "Los Angeles",
    "shippingState": "CA",
    "shippingZipCode": "90001"
  }'
```

### Step 6: Check Order Status
```bash
curl -X GET http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN"
```

### Step 7: View Order Details
```bash
curl -X GET http://localhost:8080/orders/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Step 8: Update Profile with Shipping Address
```bash
curl -X PUT http://localhost:8080/users/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "address": "789 Oak Street",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90001"
  }'
```

---

## Testing with Postman

1. **Download Postman** from https://www.postman.com/downloads/

2. **Create Collection**: "Ecommerce API"

3. **Create Requests**:
   - Auth Register
   - Auth Login
   - Get Products
   - Create Order
   - Get User Orders
   - Update Profile

4. **Setup Authorization**:
   - Go to request → Authorization tab
   - Type: Bearer Token
   - Token: `{{TOKEN}}`

5. **Setup Environment Variables**:
   - Click Settings/Environments
   - Create "Ecommerce Dev" environment
   - Variables:
     - `base_url` = `http://localhost:8080`
     - `TOKEN` = (paste token from login response)

6. **Use in Requests**:
   - URL: `{{base_url}}/products`
   - Header: `Authorization: Bearer {{TOKEN}}`

---

## Stress Testing

### Generate Multiple Orders

```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/orders \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "items": [{"productId": 2, "quantity": 1}],
      "shippingAddress": "Test Street",
      "shippingCity": "City",
      "shippingState": "ST",
      "shippingZipCode": "12345"
    }'
  echo "Order $i created"
done
```

---

## Debugging Tips

### Check Database Data

```bash
mysql -u root -p ecommerce_db

# View users
SELECT id, email, name FROM users;

# View products
SELECT id, name, price, stock_quantity FROM products;

# View orders
SELECT id, order_number, total_amount, status FROM orders;

# View order items
SELECT * FROM order_items WHERE order_id = 1;

# Check stock after order
SELECT id, name, stock_quantity FROM products WHERE id = 1;
```

### View Application Logs

Add to `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.ecommerce=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

Then restart app and watch console for SQL queries.

### Test Database Connection

```bash
mysql -u root -p ecommerce_db -e "SELECT 1;"
```

---

## Common Testing Issues & Solutions

### Issue: "Connection refused"
**Solution**: Make sure MySQL is running
```bash
mysql --version  # Check if installed
# Start MySQL service
```

### Issue: "Column 'created_at' doesn't have a default value"
**Solution**: JPA should auto-generate with @PrePersist, but manually ensure timestamps:
```bash
mysql> UPDATE products SET created_at = NOW(), updated_at = NOW();
```

### Issue: Token shows "invalid signature"
**Solution**: 
- Make sure you're using the exact token from response
- Don't modify JWT secret in application.properties after login
- Try logging in again for new token

### Issue: "Duplicate entry for key 'email'"
**Solution**: Email is unique, clear users table:
```bash
mysql> TRUNCATE users;
```

### Issue: Order creation fails with "Insufficient stock"
**Solution**: Check product stock and update:
```bash
mysql> UPDATE products SET stock_quantity = 100 WHERE id = 1;
```

---

## HTTP Status Codes Reference

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK | GET /products successful |
| 201 | Created | POST /orders successful |
| 400 | Bad Request | Wrong password, duplicate email |
| 401 | Unauthorized | Missing/invalid JWT token |
| 404 | Not Found | Product ID doesn't exist |
| 500 | Server Error | Unexpected database error |

---

## Tips for Success

✅ **Always include** `Content-Type: application/json`
✅ **Always include** `Authorization: Bearer <TOKEN>` for protected endpoints
✅ **Copy token exactly** from login/register response (no extra spaces)
✅ **Check database** to verify data persistence
✅ **Read error messages** - they explain what went wrong
✅ **Use Postman** for complex testing workflows
✅ **Save tokens** in environment variable: `TOKEN="..."`

---

Happy testing! 🧪
