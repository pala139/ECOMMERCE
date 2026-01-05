# Quick Reference Guide

## Project Structure at a Glance

```
entity/           → Database models (@Entity classes)
repository/       → Data access layer (JpaRepository interfaces)
service/          → Business logic (all the "how" goes here)
controller/       → HTTP endpoints (request/response handling)
dto/              → Data Transfer Objects (API contracts)
config/           → Security, filters, bean configurations
util/             → Helper utilities (JWT, converters)
exception/        → Exception handlers and custom exceptions
```

## Typical Request/Response Flow

```
Client
  ↓ (HTTP Request)
Controller (receives request)
  ↓ (calls method)
Service (executes business logic)
  ↓ (queries data)
Repository (talks to database)
  ↓ (returns entity)
Service (converts entity → DTO)
  ↓ (returns DTO)
Controller (creates HTTP response)
  ↓ (HTTP Response)
Client (receives response)
```

## When to Use DTOs vs Entities

| Scenario | Use |
|----------|-----|
| Saving to database | Entity |
| Returning from API | DTO |
| Internal service calls | Entity or DTO |
| Client sends request | Request DTO |
| API response | Response DTO |
| Database query result | Entity |

## Authentication Flow

```
1. User registers/logs in
2. Server validates credentials
3. Server generates JWT token
4. Client stores token

5. Client sends request with Authorization header
   Authorization: Bearer <token>

6. JwtAuthenticationFilter:
   - Extracts token from header
   - Validates signature and expiration
   - Extracts email from token
   - Sets email in SecurityContext

7. Controller gets Authentication object:
   String email = authentication.getPrincipal().toString()

8. Use email to fetch user from database
```

## Adding a New Feature (Step by Step)

### Example: Add Product Reviews

**Step 1: Create Entity**
```java
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Product product;
    
    @ManyToOne
    private User user;
    
    private String comment;
    private Integer rating; // 1-5
    private LocalDateTime createdAt;
}
```

**Step 2: Create Repository**
```java
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
}
```

**Step 3: Create DTO**
```java
public class ReviewDTO {
    private Long id;
    private Long productId;
    private String userName;
    private String comment;
    private Integer rating;
    private LocalDateTime createdAt;
}
```

**Step 4: Create Service**
```java
@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    
    public ReviewDTO createReview(String userEmail, Long productId, ReviewRequest request) {
        User user = userRepository.findByEmail(userEmail)...;
        Product product = productRepository.findById(productId)...;
        
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setComment(request.getComment());
        review.setRating(request.getRating());
        
        review = reviewRepository.save(review);
        return convertToDTO(review);
    }
    
    public List<ReviewDTO> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}
```

**Step 5: Create Controller**
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @RequestBody ReviewRequest request,
            @RequestParam Long productId,
            Authentication auth) {
        String email = auth.getPrincipal().toString();
        ReviewDTO review = reviewService.createReview(email, productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        List<ReviewDTO> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }
}
```

## Debugging Tips

### Enable SQL Logging
In `application.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Check Database
```bash
mysql -u root -p ecommerce_db
SHOW TABLES;
SELECT * FROM users;
SELECT * FROM orders;
```

### Test Endpoints
```bash
# Test without auth
curl http://localhost:8080/products

# Test with auth
TOKEN="your_jwt_token_here"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/orders
```

## Common Errors & Solutions

### Error: "User not found"
**Cause**: Email in JWT doesn't match database
**Solution**: Clear database and register new user

### Error: "Invalid password"
**Cause**: BCrypt comparison failed
**Solution**: Password encoding must use same PasswordEncoder instance

### Error: "Constraint Violation"
**Cause**: Duplicate email or null required field
**Solution**: Check uniqueness constraints and validate input

### Error: "Cannot assign request parameter to null"
**Cause**: Required parameter missing
**Solution**: Add all required fields to request body

## Performance Tips

1. **Use Pagination for List Endpoints**
   ```java
   Page<Product> findAll(Pageable pageable);
   // Usage: GET /products?page=0&size=10
   ```

2. **Add Indexes to Frequently Queried Columns**
   ```java
   @Column(unique = true, nullable = false)
   @Index(name = "idx_email")
   private String email;
   ```

3. **Use Lazy Loading for Large Relations**
   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   private Category category;
   ```

4. **Avoid N+1 Query Problems**
   ```java
   // Bad: N+1 queries (loads each product's category separately)
   List<Product> products = productRepository.findAll();
   products.stream().map(p -> p.getCategory().getName()); // N queries!
   
   // Good: Use JOIN FETCH
   @Query("SELECT p FROM Product p JOIN FETCH p.category")
   List<Product> findAllWithCategories();
   ```

5. **Cache Frequently Accessed Data**
   ```java
   @Cacheable("products")
   public List<ProductDTO> getAllProducts() { ... }
   ```

## Security Checklist

- [ ] Never log passwords
- [ ] Never send passwords in responses
- [ ] Always hash passwords (use BCrypt, never plaintext)
- [ ] Validate all input
- [ ] Sanitize error messages (don't expose database details)
- [ ] Use HTTPS in production
- [ ] Use strong JWT secret (at least 32 characters)
- [ ] Set reasonable token expiration
- [ ] Implement rate limiting
- [ ] Use CORS carefully
- [ ] Validate file uploads
- [ ] Don't expose internal IDs when possible

## Testing Template

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private PasswordEncoder passwordEncoder;
    
    @Test
    void testGetUserByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));
        
        // Act
        User result = userService.getUserByEmail("test@example.com");
        
        // Assert
        assertEquals("test@example.com", result.getEmail());
    }
}
```

## Key Classes Explained

| Class | Purpose | Key Methods |
|-------|---------|-------------|
| `User` | Database user record | getId(), getEmail() |
| `UserRepository` | User database queries | findByEmail() |
| `AuthService` | Login/register logic | register(), login() |
| `JwtUtil` | Token generation/validation | generateToken(), isTokenValid() |
| `JwtAuthenticationFilter` | Intercepts requests | doFilterInternal() |
| `AuthController` | Auth endpoints | POST /register, POST /login |
| `ProductService` | Product business logic | getAllProducts(), createProduct() |
| `OrderService` | Order creation logic | createOrder(), getUserOrders() |
| `GlobalExceptionHandler` | Exception handling | handleRuntimeException() |

---

Happy coding! 🚀
