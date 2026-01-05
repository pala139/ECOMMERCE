# Module-by-Module Explanation

## Module 1: ENTITY LAYER (Database Models)

### Files Created:
- `User.java`
- `Product.java`
- `Order.java`
- `OrderItem.java`
- `Category.java`

### Why This Layer Exists:
Entities represent database tables. They describe WHAT data we store, not HOW we use it. Using Hibernate/JPA, these classes automatically map to database tables.

### Interview Talking Point:
"The entity layer is our contract with the database. It represents the current state of data without any business logic. We use JPA annotations to avoid writing raw SQL."

---

### USER Entity

```
WHY THIS ENTITY EXISTS:
- Store customer account information
- Enable authentication (email + password)
- Track user's address for shipping

INTERVIEW TALKING POINTS:
- Why separate email and name? Email is unique identifier, name is just display info
- Why timestamp fields? Useful for auditing ("when was user created?")
- Why @PrePersist and @PreUpdate? Auto-populate timestamps without manual logic
- Why encrypt password? Never store raw passwords in database

REQUEST FLOW:
1. Client registers with email/password
2. AuthService hashes password with BCrypt
3. User entity is saved with hashed password
4. Later, login compares provided password with hash
```

### PRODUCT Entity

```
WHY THIS ENTITY EXISTS:
- Represent items for sale
- Store pricing and stock information
- Link to categories for organization

INTERVIEW TALKING POINTS:
- Why is price in Product, not just in OrderItem? Product has a current price for display
- Why stockQuantity? Prevents overselling
- Why separate Category? Allows filtering and organization
- Why decimal for price? Floats can cause precision issues with money

REQUEST FLOW:
1. Admin creates product with name, price, stock, category
2. Product saved to database
3. Customers can browse all products
4. When customer orders, price is frozen (stored in OrderItem)
5. If product price changes later, past orders still show old price
```

### ORDER & ORDERITEM Entities

```
WHY TWO SEPARATE ENTITIES?
- Order = header (who, when, total, status)
- OrderItem = line items (what products, quantities, prices)

INTERVIEW TALKING POINTS:
- Why not store items inside Order? Violates normalization
- Why priceAtPurchase in OrderItem? If product price drops next day, customer's order keeps original price
- Why orderNumber instead of ID? IDs are sequential and leak business info
- Why cascade delete? When order deleted, items must be deleted too (can't have orphaned items)

REQUEST FLOW:
1. Customer creates order with 2 items
2. Order created with status PENDING
3. Two OrderItem records created with frozen prices
4. Stock quantities decremented
5. Order moves through states: PENDING → CONFIRMED → SHIPPED → DELIVERED
```

---

## Module 2: REPOSITORY LAYER (Data Access)

### Files Created:
- `UserRepository.java`
- `ProductRepository.java`
- `OrderRepository.java`
- `OrderItemRepository.java`
- `CategoryRepository.java`

### Why This Layer Exists:
Repositories provide type-safe database access without writing SQL. Spring Data JPA generates implementations automatically.

### Interview Talking Point:
"Instead of writing raw JDBC code or SQL queries, Spring Data JPA provides a simple interface. We get basic CRUD for free and can add custom queries as needed."

---

### UserRepository

```java
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

WHY findByEmail()?
- Standard JpaRepository gives us findById(), save(), delete()
- But we authenticate by email, not ID
- Custom query method needed
- Spring generates SQL from method name automatically
- Method name → Query: "find" By "Email" = SELECT * WHERE email = ?
```

### ProductRepository

```java
interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
}

WHY custom queries?
- Browse products by category
- Search products by name
- Spring converts method names to SQL queries
```

### OrderRepository

```java
interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
}

WHY custom queries?
- Each user needs to see their own orders only
- Order number is customer's reference point (not ID)
```

---

## Module 3: JWT AUTHENTICATION LAYER

### Files Created:
- `JwtUtil.java` (generate/validate tokens)
- `JwtAuthenticationFilter.java` (intercept requests)
- `SecurityConfig.java` (configure security rules)

### Why This Layer Exists:
Implements stateless JWT authentication. Client gets token once, includes it in every request.

### Interview Talking Point:
"JWT eliminates the need for sessions. Token is self-contained - it has user info and signature. Server just validates signature without database lookup."

---

### JwtUtil

```
generateToken(email):
1. Create payload with email and expiration time
2. Sign with secret key (ensures token authenticity)
3. Return base64-encoded token

isTokenValid(token):
1. Try to parse and verify signature
2. Check expiration time
3. Return true if valid, false if tampered/expired

extractEmail(token):
1. Parse token (requires valid signature)
2. Return the email from payload

WHY this design?
- Single responsibility: token generation/validation
- No authentication logic mixed in
- Easy to test independently
```

### JwtAuthenticationFilter

```
doFilterInternal():

1. Get Authorization header from request
2. Check if starts with "Bearer "
3. Extract token (remove "Bearer " prefix)
4. Validate token with JwtUtil
5. If valid:
   - Extract email from token
   - Create Authentication object with email
   - Set in SecurityContext (Spring Security's holder)
6. Continue to next filter/controller

WHY this design?
- Runs before every request (OncePerRequestFilter)
- Populates SecurityContext before controller
- Controller can retrieve email from Authentication object
- Doesn't throw error if token missing (public endpoints)
- Doesn't throw error if token invalid (controller handles it)
```

### SecurityConfig

```
Configuration:
- CSRF disabled (we use JWT, not cookies)
- Public endpoints: /auth/register, /auth/login, /products/*, /categories/*
- Protected endpoints: /orders/*, /users/*
- Stateless sessions (no cookies, no server-side storage)
- Add JwtAuthenticationFilter before default auth filter

WHY this design?
- Clear separation of public vs protected endpoints
- @Autowired Authentication in controller = injected by Spring
- No CSRF needed (stateless + token is in Authorization header, not cookie)
```

---

## Module 4: SERVICE LAYER (Business Logic)

### Files Created:
- `AuthService.java` (register/login)
- `ProductService.java` (product CRUD)
- `OrderService.java` (order creation)
- `UserService.java` (user profile)

### Why This Layer Exists:
All business rules live here. Controllers delegate to services, repositories handle data.

### Interview Talking Point:
"Services are where the actual work happens. Controllers just handle HTTP. Repositories just handle data access. Services orchestrate everything."

---

### AuthService

```
register(request):
1. Check email doesn't already exist
   Why? Prevent duplicate accounts
2. Create User object
3. Hash password: passwordEncoder.encode(rawPassword)
   Why hash? If database is stolen, passwords aren't exposed
   Why BCrypt? Intentionally slow (prevents brute force attacks)
4. Save user to database
5. Generate JWT token
6. Return token (user is immediately logged in)

login(request):
1. Find user by email
   Why? Email is unique identifier
2. Compare password: passwordEncoder.matches(providedPassword, storedHash)
   Why matches()? It handles BCrypt comparison correctly
3. If password correct: generate token
4. If password wrong: throw exception (controller handles HTTP response)

INTERVIEW TALKING POINTS:
- Why not store raw password? Security breach = compromised accounts
- Why BCrypt? Each hash is different (different salt) even for same password
- Why throw exception instead of returning error? 
  Controllers catch it and return proper HTTP response
```

### ProductService

```
getAllProducts():
1. Query database for all products
2. Convert each Product entity to ProductDTO
3. Return list of DTOs
   Why DTOs? API contract independent of database

getProductById(id):
1. Query database for product
2. Throw exception if not found
3. Convert to DTO and return

createProduct(request):
1. Validate category exists (if provided)
2. Create Product object from request
3. Save to database
4. Convert to DTO and return
   Why convert? API doesn't expose raw entities

searchProducts(keyword):
1. Query for products where name contains keyword
2. Case-insensitive search
3. Convert all to DTOs

decrementStock(productId, quantity):
1. Find product
2. Check stock >= quantity (prevent overselling)
3. Decrement stock
4. Save
   Why separate method? OrderService calls this
```

### OrderService

```
createOrder(userEmail, request):

STEP 1: Get user from email
- Find authenticated user
- Throw exception if not found

STEP 2: Create Order entity
- Set user, generate orderNumber, set shipping address
- Status defaults to PENDING

STEP 3: Process items
For each item in request:
  1. Find product (throw if not found)
  2. Check stock (throw if insufficient)
  3. Create OrderItem with frozen price
  4. Add to order
  5. Add to total amount
  6. Decrement stock (calls ProductService)

STEP 4: Save and return
- Save order (and cascaded items)
- Convert to DTO

INTERVIEW TALKING POINTS:
- Why freeze price? Product price changes shouldn't affect past orders
- Why call ProductService.decrementStock()? Reuse logic, single place to modify stock
- Why generate orderNumber? User-friendly reference (ORD-ABC12345 vs order ID 12345)
- Why throw exceptions? Let GlobalExceptionHandler handle HTTP response
- Why create OrderItem separately? Each item has its own quantity and price
```

---

## Module 5: DTO LAYER (Data Transfer Objects)

### Files Created:
- `LoginRequest.java` - What client sends to login
- `RegisterRequest.java` - What client sends to register
- `AuthResponse.java` - What server sends back with token
- `ProductDTO.java` - Product for API response
- `CreateProductRequest.java` - What client sends to create product
- `OrderDTO.java` - Order for API response
- `OrderItemDTO.java` - Order item for API response
- `CreateOrderRequest.java` - What client sends to create order

### Why This Layer Exists:
Decouples API contracts from internal structures. API doesn't expose raw entities.

### Interview Talking Point:
"DTOs are the API contract. If we later change the database schema, we only change the conversion logic, not the API."

---

### Key DTO Patterns

```
REQUEST DTOs (what client sends):
- LoginRequest: email, password
- RegisterRequest: email, password, name, phoneNumber
- CreateProductRequest: name, description, price, stockQuantity, categoryId
- CreateOrderRequest: items[], shippingAddress, shippingCity, state, zipCode

RESPONSE DTOs (what server sends):
- AuthResponse: token, email, name, userId
- ProductDTO: id, name, description, price, stockQuantity, categoryId, categoryName
- OrderDTO: id, orderNumber, totalAmount, status, createdAt, items[]
- OrderItemDTO: id, productId, productName, quantity, priceAtPurchase, subtotal

WHY NOT SEND ENTITIES?
- Entities have @OneToMany relations (circular JSON)
- Entities have @PrePersist timestamps (hide internal metadata)
- API contract tied to database schema
- Can't evolve API without breaking clients
```

---

## Module 6: CONTROLLER LAYER (HTTP Endpoints)

### Files Created:
- `AuthController.java` - /auth endpoints
- `ProductController.java` - /products endpoints
- `OrderController.java` - /orders endpoints
- `UserController.java` - /users endpoints
- `CategoryController.java` - /categories endpoints

### Why This Layer Exists:
Controllers translate HTTP requests to service calls and responses.

### Interview Talking Point:
"Controllers are thin. They receive HTTP requests, delegate to services, and format responses. No business logic here."

---

### AuthController

```
POST /auth/register:
1. Receive RegisterRequest
2. Call authService.register(request)
3. AuthService throws exception if email exists
4. Catch exception, re-throw (GlobalExceptionHandler catches)
5. Return 201 CREATED with AuthResponse (token)

POST /auth/login:
1. Receive LoginRequest
2. Call authService.login(request)
3. AuthService throws exception if password wrong
4. Catch exception, re-throw
5. Return 200 OK with AuthResponse (token)

WHY try-catch?
- Catch RuntimeException
- Re-throw with better message (optional)
- GlobalExceptionHandler catches and returns HTTP error
```

### OrderController

```
POST /orders:
1. Receive CreateOrderRequest
2. Receive Authentication object (injected by Spring Security)
   - Contains email set by JwtAuthenticationFilter
3. Extract email: authentication.getPrincipal().toString()
4. Call orderService.createOrder(email, request)
5. Return 201 CREATED with OrderDTO

GET /orders:
1. Extract email from Authentication
2. Call orderService.getUserOrders(email)
3. Return 200 OK with list of OrderDTOs

PUT /orders/{id}/status:
1. Receive status update
2. Call orderService.updateOrderStatus(id, status)
3. Return 200 OK with updated OrderDTO

KEY PRINCIPLE:
- Controller doesn't fetch user from database
- Service does that (separation of concerns)
- Controller just extracts email and passes to service
```

### ProductController

```
GET /products:
- Return all products (no auth needed)

GET /products/{id}:
- Return product by ID

GET /products/search?keyword=...
- Search products by name

GET /products/category/{categoryId}:
- Get products in category

POST /products:
- Create product (should be admin-only in production)

KEY PRINCIPLE:
- @GetMapping for reading
- @PostMapping for creating
- Return ResponseEntity for control over HTTP status
```

---

## Module 7: EXCEPTION HANDLING

### Files Created:
- `ErrorResponse.java` - Standardized error format
- `GlobalExceptionHandler.java` - Centralized exception handling

### Why This Layer Exists:
Catch exceptions from all controllers and return consistent error responses.

### Interview Talking Point:
"Global exception handler prevents code duplication. Every controller doesn't need try-catch blocks. Exceptions bubble up and are handled in one place."

---

### How It Works

```
EXCEPTION FLOW:
1. Service throws RuntimeException("User not found")
2. Controller doesn't catch it
3. Exception bubbles up to GlobalExceptionHandler
4. @ExceptionHandler(RuntimeException.class) matches
5. Method returns ErrorResponse with 400 Bad Request
6. Client receives standardized error JSON

ERROR RESPONSE FORMAT:
{
  "timestamp": "2026-01-05T10:30:00",
  "status": 400,
  "message": "User not found",
  "path": "/auth/login"
}

BENEFITS:
- Consistent error format across all endpoints
- Hide implementation details (stack traces)
- Easy to add new exception types
- No boilerplate in controllers
```

---

## Module 8: CONFIGURATION

### Files Created:
- `application.properties` - Database, JWT, logging config
- `SecurityConfig.java` - Spring Security rules
- `EcommerceApplication.java` - Main Spring Boot app

### Why This Layer Exists:
Configure Spring Boot framework without hardcoding in classes.

---

## Complete Request Example: User Registration → Order Creation

```
STEP 1: REGISTER
┌─────────────────────────────────────┐
│ POST /auth/register                 │
│ Body: {email, password, name}       │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ AuthController.register()           │
│ - Receives RegisterRequest          │
│ - Calls authService.register()      │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ AuthService.register()              │
│ 1. Check email doesn't exist        │
│ 2. Hash password with BCrypt        │
│ 3. Create User entity               │
│ 4. Call userRepository.save()       │
│ 5. Generate JWT with JwtUtil        │
│ 6. Return AuthResponse (token)      │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ 201 CREATED                         │
│ {                                   │
│   "token": "eyJhbGc...",            │
│   "email": "user@example.com",      │
│   "userId": 1                       │
│ }                                   │
└─────────────────────────────────────┘


STEP 2: BROWSE PRODUCTS (public, no auth needed)
┌─────────────────────────────────────┐
│ GET /products                       │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ JwtAuthenticationFilter              │
│ - No Authorization header           │
│ - Continue to controller (unauthenticated)
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ ProductController.getAllProducts()  │
│ - Calls productService.getAllProducts()
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ ProductService                      │
│ 1. Call productRepository.findAll() │
│ 2. Convert each to ProductDTO       │
│ 3. Return list of DTOs              │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ 200 OK                              │
│ [                                   │
│   {id, name, price, stock},         │
│   {id, name, price, stock}          │
│ ]                                   │
└─────────────────────────────────────┘


STEP 3: CREATE ORDER (protected - requires auth)
┌─────────────────────────────────────┐
│ POST /orders                        │
│ Authorization: Bearer <token>       │
│ Body: {items, shippingAddress}      │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ JwtAuthenticationFilter              │
│ 1. Extract token from Authorization │
│ 2. Validate signature & expiration  │
│ 3. Extract email from token         │
│ 4. Create Authentication object     │
│ 5. Set in SecurityContext           │
│ 6. Continue to controller           │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ OrderController.createOrder()       │
│ - Receives CreateOrderRequest       │
│ - Receives Authentication (injected)│
│ - Extracts email from Authentication
│ - Calls orderService.createOrder()  │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ OrderService.createOrder()          │
│ 1. Find user by email               │
│ 2. Create Order entity              │
│ 3. For each item:                   │
│    - Find product                   │
│    - Check stock                    │
│    - Create OrderItem (frozen price)│
│    - Decrement stock                │
│    - Add to total amount            │
│ 4. Save order (cascade saves items) │
│ 5. Convert to OrderDTO              │
│ 6. Return OrderDTO                  │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ 201 CREATED                         │
│ {                                   │
│   "id": 1,                          │
│   "orderNumber": "ORD-ABC123",      │
│   "totalAmount": 150.00,            │
│   "status": "PENDING",              │
│   "items": [...]                    │
│ }                                   │
└─────────────────────────────────────┘
```

---

## Summary Table

| Layer | Files | Purpose | Key Principle |
|-------|-------|---------|---------------|
| Entity | User, Product, Order, etc. | Database models | What we store |
| Repository | UserRepository, etc. | Data access | How we query |
| Service | AuthService, ProductService, etc. | Business logic | How we process |
| Controller | AuthController, etc. | HTTP endpoints | How clients communicate |
| DTO | LoginRequest, AuthResponse, etc. | API contracts | What we expose |
| Config | SecurityConfig, etc. | Framework setup | How Spring is configured |
| Exception | GlobalExceptionHandler | Error handling | How we respond to errors |

---

This architecture is:
✅ **Simple**: Easy to understand and modify
✅ **Testable**: Each layer can be tested independently
✅ **Maintainable**: Single responsibility per class
✅ **Scalable**: Easy to add new features
✅ **Interview-friendly**: Clear design patterns that interviewers recognize
