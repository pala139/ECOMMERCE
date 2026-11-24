# E-Commerce Backend

Java 17 / Spring Boot REST API for catalog, customer accounts, checkout, and inventory management.

## Architecture

```mermaid
flowchart LR
    Client --> Security[JWT / RBAC]
    Security --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> MySQL
```

A layered monolith keeps deployment and transaction boundaries simple. Controllers validate HTTP requests, services own business rules, and repositories handle persistence. DTOs separate API contracts from database entities.

## Run locally

Prerequisites: Docker and Docker Compose. Copy `.env.example` to `.env`, replace the passwords, and generate a random JWT secret of at least 32 bytes (for example, `openssl rand -hex 32`).

```sh
docker compose up --build
```

- API: http://localhost:8080
- OpenAPI UI: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

Flyway initializes a **new, empty MySQL database**. Do not point this version at a legacy database created with Hibernate auto-update without a reviewed migration and backup. Hibernate validates the schema rather than changing it automatically.

## API

| Method | Endpoint | Access |
|---|---|---|
| POST | /auth/register, /auth/login | Public |
| GET | /products?page=0&size=20&sort=price&direction=asc | Public |
| GET | /products?keyword=book or /products?categoryId=1 | Public |
| GET | /products/{id}, /categories, /categories/{id} | Public |
| POST | /products, /categories | Admin |
| GET / PUT | /users/profile | Authenticated |
| POST | /orders | Authenticated; Idempotency-Key required |
| GET | /orders, /orders/{id} | Owner |
| PUT | /orders/{id}/status | Admin |

Register a customer through `POST /auth/register` and send the returned token as `Authorization: Bearer TOKEN`. Registration always assigns CUSTOMER. For local development only, create an account and promote it using a privileged database connection:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

Checkout accepts:

```json
{
  "items": [{"productId": 1, "quantity": 2}],
  "shippingAddress": "123 Main Street",
  "shippingCity": "Hyderabad",
  "shippingState": "Telangana",
  "shippingZipCode": "500001"
}
```

## Engineering decisions

- JWT authentication is stateless; role and account existence are checked against the database. Product/category writes and order status changes require ADMIN. Order reads are scoped to their owner.
- Checkout is a single transaction. Pessimistic row locks are acquired in ascending product ID order; concurrent buyers cannot reserve the same stock. Duplicate product lines are merged. A failed checkout rolls back inventory changes.
- A customer-scoped idempotency key and request fingerprint prevent duplicate orders on retries. Reusing a key with a changed payload returns a conflict. Replays return the existing order, including its current status.
- Prices are captured at purchase using BigDecimal. Cancellation restores stock under locks; allowed status transitions are explicit.
- Catalog requests have bounded pages and allowlisted sorting. Category data is fetched with an entity graph. Leading-wildcard search is suitable for small catalogs, not a replacement for full-text search.
- Flyway owns schema changes. Secrets are supplied through environment variables. Open Session in View is disabled.

## Tests

Prerequisites: Java 17, Maven and Docker (for Testcontainers). No external database credentials are required for the tests.

```sh
mvn -B verify
```

The suite includes service unit tests and MySQL-backed tests for anonymous access, ownership, validation, concurrent checkout, rollback, idempotency, and cancellation. JaCoCo reports are generated under `target/site/jacoco/`.

## Limitations

This is a portfolio application, not a payment-processing service. It does not yet implement payment, refresh tokens, distributed caching, rate limiting, or production load benchmarks. Order history is currently unpaginated. Database locking favors correctness over maximum checkout throughput. External deployment requires a compatible database and environment configuration.
