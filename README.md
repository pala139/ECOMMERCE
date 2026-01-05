# Spring Boot E-Commerce Backend API

A production-ready e-commerce REST API built with Spring Boot, featuring JWT authentication, product catalog, and order management.

## Architecture Overview

```
ecommerce-backend/
├── src/main/java/com/ecommerce/
│   ├── entity/           # JPA entities (User, Product, Order, etc.)
│   ├── repository/       # Spring Data JPA repositories
│   ├── service/          # Business logic layer
│   ├── controller/       # REST API endpoints
│   ├── dto/              # Data Transfer Objects
│   ├── config/           # Spring security and JWT configuration
│   ├── util/             # JWT utilities
│   ├── exception/        # Global exception handler
│   └── EcommerceApplication.java
└── src/main/resources/
    └── application.properties
```

## Key Design Principles

### 1. **Separation of Concerns**
- **Entity**: Database models only
- **Repository**: Data access only
- **Service**: Business logic only
- **Controller**: HTTP request/response handling only

### 2. **Single Responsibility**
Each class has ONE reason to change:
- `AuthService` handles authentication logic
- `ProductService` handles product operations
- `OrderService` handles order logic
- No bloated "god classes"

### 3. **DTOs Over Entities**
- API contracts are separate from database schema
- Prevents exposing internal fields (timestamps, relations)
- Easier to version APIs without breaking clients

### 4. **Stateless Authentication**
- JWT tokens eliminate need for session storage
- Each request is self-contained
- Scales horizontally (multiple servers work fine)

---

## API Endpoints

### Authentication (Public)

```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secure123",
  "name": "John Doe",
  "phoneNumber": "1234567890"
}

Response: 201 CREATED
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com",
  "name": "John Doe",
  "userId": 1
}
```

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secure123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com",
  "name": "John Doe",
  "userId": 1
}
```

### Products (Public)

```http
GET /products
GET /products/{id}
GET /products/search?keyword=laptop
GET /products/category/{categoryId}
POST /products  # Create product (requires auth in production)
```

### Categories (Public)

```http
GET /categories
GET /categories/{id}
POST /categories
```

### Orders (Protected - Requires JWT)

```http
POST /orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ],
  "shippingAddress": "123 Main St",
  "shippingCity": "New York",
  "shippingState": "NY",
  "shippingZipCode": "10001"
}

Response: 201 CREATED
{
  "id": 1,
  "orderNumber": "ORD-ABC12345",
  "totalAmount": 150.00,
  "status": "PENDING",
  "createdAt": "2026-01-05T10:30:00",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop",
      "quantity": 2,
      "priceAtPurchase": 50.00,
      "subtotal": 100.00
    }
  ]
}
```

```http
GET /orders
Authorization: Bearer <token>

GET /orders/{id}
Authorization: Bearer <token>

PUT /orders/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

### User Profile (Protected)

```http
GET /users/profile
Authorization: Bearer <token>

PUT /users/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Jane Doe",
  "phoneNumber": "9876543210",
  "address": "456 Oak Ave",
  "city": "Boston",
  "state": "MA",
  "zipCode": "02101"
}
```

---

## Request/Response Flow

### Example: User Registration → Product Browse → Order Creation

```
1. REGISTER
   ├─ Client POST /auth/register with credentials
   ├─ AuthService validates email uniqueness
   ├─ AuthService hashes password with BCrypt
   ├─ UserRepository saves user to MySQL
   ├─ JwtUtil generates JWT token
   └─ Return token to client

2. BROWSE PRODUCTS
   ├─ Client GET /products
   ├─ ProductController calls ProductService
   ├─ ProductRepository queries MySQL
   ├─ ProductService converts entities to DTOs
   └─ Return product list (no auth needed)

3. CREATE ORDER
   ├─ Client POST /orders with JWT in Authorization header
   ├─ JwtAuthenticationFilter validates token
   ├─ Filter extracts email from token, sets in SecurityContext
   ├─ OrderController receives Authentication object
   ├─ OrderService fetches user by email
   ├─ For each item:
   │  ├─ Check product exists
   │  ├─ Check stock available
   │  ├─ Freeze product price at time of purchase
   │  ├─ Create OrderItem
   │  ├─ Decrement product stock
   ├─ Calculate total amount
   ├─ OrderRepository saves order
   └─ Return order confirmation to client
```

---

## Authentication Mechanism

### How JWT Works

```
1. User logs in with email/password
2. Server validates credentials
3. Server creates JWT token:
   - Header: { "alg": "HS256", "typ": "JWT" }
   - Payload: { "sub": "user@example.com", "iat": 1234567890, "exp": 1234654290 }
   - Signature: HMACSHA256(header.payload, secret_key)
   - Result: header.payload.signature (base64 encoded)

4. Client stores token (localStorage, cookie, etc.)
5. Client sends every request with: Authorization: Bearer <token>
6. Server validates signature and expiration
7. If valid, request is processed
8. If invalid, return 401 Unauthorized
```

### Why JWT is Better Than Sessions

| Feature | Sessions | JWT |
|---------|----------|-----|
| Storage | Server-side (memory/database) | Client-side (token) |
| Scalability | Database needed on every server | No server state needed |
| Mobile-friendly | Requires cookie handling | Works with any client |
| Microservices | Difficult to share state | Easy to validate token |

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,  -- BCrypt hashed
  name VARCHAR(255) NOT NULL,
  phone_number VARCHAR(20),
  address VARCHAR(255),
  city VARCHAR(100),
  state VARCHAR(50),
  zip_code VARCHAR(20),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

### Products Table
```sql
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10, 2) NOT NULL,
  stock_quantity INT NOT NULL,
  category_id BIGINT FOREIGN KEY,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

### Orders Table
```sql
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT FOREIGN KEY NOT NULL,
  order_number VARCHAR(50) UNIQUE NOT NULL,
  total_amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(50) NOT NULL,  -- PENDING, CONFIRMED, SHIPPED, DELIVERED
  shipping_address VARCHAR(255),
  shipping_city VARCHAR(100),
  shipping_state VARCHAR(50),
  shipping_zip_code VARCHAR(20),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

### Order Items Table
```sql
CREATE TABLE order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT FOREIGN KEY NOT NULL,
  product_id BIGINT FOREIGN KEY NOT NULL,
  quantity INT NOT NULL,
  price_at_purchase DECIMAL(10, 2) NOT NULL
);
```

---

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Create Database

```bash
mysql -u root -p
CREATE DATABASE ecommerce_db;
```

### 2. Configure Database

Edit `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Change JWT Secret

Edit `application.properties`:
```properties
jwt.secret=your-very-long-and-secure-secret-key-for-production
jwt.expiration=86400000  # 24 hours in milliseconds
```

### 4. Build and Run

```bash
mvn clean package
java -jar target/ecommerce-backend-1.0.0.jar
```

Server runs on `http://localhost:8080`

---

## Interview Talking Points

### Why This Architecture?

1. **Layered Architecture**
   - Each layer has clear responsibilities
   - Easy to test (mock services, repositories)
   - Changes in database don't affect controllers

2. **DTOs Instead of Entities**
   - API contract is independent of database
   - Can evolve database without breaking clients
   - Hides internal details (timestamps, relations)

3. **JWT Authentication**
   - Stateless: scales horizontally
   - No session storage needed
   - Works with mobile, web, and third-party clients

4. **Service Layer**
   - Centralizes business logic
   - Easy to reuse logic from multiple endpoints
   - Easy to test in isolation

5. **Exception Handling**
   - Global handler prevents code duplication
   - Consistent error format for all APIs
   - Hides implementation details from clients

### Common Interview Questions

**Q: Why separate entities from DTOs?**
A: DTOs give us control over the API contract. If we return entities directly, API structure is locked to database schema. DTOs let us change database without breaking clients.

**Q: How does JWT authentication work?**
A: JWT contains user info signed with a secret key. Server validates the signature, ensuring token wasn't tampered with. No database lookup needed per request (stateless).

**Q: What happens if someone modifies the JWT token?**
A: The signature will be invalid. When server tries to verify the signature, it fails and rejects the request.

**Q: How do you prevent race conditions when placing orders?**
A: Database transactions ensure either all items are ordered or none. JPA's default behavior provides transaction support. In high-traffic systems, use pessimistic locking.

**Q: Why generate orderNumber instead of using ID?**
A: Order IDs are sequential and expose business info (order count, growth). Random orderNumbers are user-friendly and hide internal metrics.

**Q: How would you scale this system?**
A:
- Add read replicas for database
- Cache products in Redis
- Use message queues (RabbitMQ) for order processing
- Split services into microservices
- Add load balancer (Nginx)
- Use connection pooling (HikariCP)

---

## Production Checklist

- [ ] Use environment variables for sensitive config (JWT secret, database password)
- [ ] Enable HTTPS/SSL in production
- [ ] Add rate limiting to prevent brute force attacks
- [ ] Add logging and monitoring (ELK stack, Datadog)
- [ ] Add input validation (javax.validation)
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Add pagination for list endpoints
- [ ] Add caching (Redis) for frequently accessed data
- [ ] Add database connection pooling
- [ ] Set up CI/CD pipeline (GitHub Actions, Jenkins)
- [ ] Add unit and integration tests
- [ ] Implement proper error logging (not just messages)
- [ ] Add CORS configuration specific to your domain
- [ ] Implement refresh token mechanism
- [ ] Add role-based access control (RBAC)

---

## Testing the API

### Using cURL

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123","name":"Test User"}'

# Copy the token from response

# Create Product
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,"stockQuantity":10,"categoryId":1}'

# Create Order (replace TOKEN with actual JWT)
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items":[{"productId":1,"quantity":2}],
    "shippingAddress":"123 Main St",
    "shippingCity":"NYC",
    "shippingState":"NY",
    "shippingZipCode":"10001"
  }'
```

### Using Postman

1. Download [Postman](https://www.postman.com/downloads/)
2. Create requests for each endpoint
3. In Auth tab, select "Bearer Token" and paste JWT
4. Organize requests in collections

---

## Future Enhancements

1. **Search & Filtering**: Add Elasticsearch for product search
2. **Payment Integration**: Add Stripe/PayPal for payments
3. **Notifications**: Send email confirmations for orders
4. **Reviews**: Let customers review products
5. **Wishlist**: Users can save favorite products
6. **Admin Dashboard**: Statistics, reports, order management
7. **Two-Factor Authentication**: Extra security
8. **API Versioning**: Support /v1/ and /v2/ endpoints
9. **GraphQL API**: Alternative to REST
10. **Inventory Management**: Low stock alerts, reorder points

---

## Troubleshooting

**Problem: "Connection refused" to MySQL**
- Check MySQL is running: `mysql --version`
- Verify database and credentials in application.properties
- Create database: `CREATE DATABASE ecommerce_db;`

**Problem: JWT token invalid/expired**
- Token expires after 24 hours (configured in application.properties)
- Login again to get a new token
- Check server time is synchronized

**Problem: "Email already registered"**
- Clean approach: delete from users table and try again
- In production: implement "forgot password" flow

**Problem: "Insufficient stock"**
- Check `stock_quantity` in products table
- Update: `UPDATE products SET stock_quantity = 100 WHERE id = 1;`

---

## License

MIT License - feel free to use this as a learning resource or starter template.
