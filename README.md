# E-Commerce Platform (Microservices Architecture)

A modular e-commerce system built with Spring Boot microservices, API Gateway, Eureka Service Discovery, and Angular frontend. Each domain is isolated with its own database schema to ensure loose coupling, scalability, and maintainability.

---
## 1. System Overview

| Layer | Component | Port | Description |
|-------|-----------|------|-------------|
| Discovery | eureka-service | 8761 | Service registry for all microservices |
| Edge | gateway-service | 8080 | Single entry point (routing, CORS, auth propagation) |
| Domain | user-service | 8085 | Authentication, user profiles, admin user management |
| Domain | product-service | 8084 | Product catalog, categories, filtering, admin CRUD |
| Domain | order-service | 8087 | Cart operations, order lifecycle, admin order management |
| Domain | analytics-service | 8088 | Aggregated KPIs, sales reports, dashboard stats |
| Domain | wishlist-service | 8089 | User wishlist collections and items |
| Frontend | Angular App | 4200 | Admin dashboard + customer UI |

Databases (MySQL 8):
- ecommerce_users
- ecommerce_products
- ecommerce_orders
- ecommerce_analytics
- ecommerce_wishlist

---
## 2. Technology Stack

**Backend:** Java 17, Spring Boot 3.5.5, Spring Data JPA, Spring Security (JWT), Spring Cloud Gateway, Spring Cloud Netflix Eureka, Maven, MySQL 8.

**Frontend:** Angular 20.0 (TypeScript), modular standalone components.

**Cross-Cutting:** DTO pattern, layered architecture (controller → service → repository), JPA/Hibernate, optional OpenAPI (springdoc), Actuator endpoints.

---
## 3. Microservice Responsibilities

### user-service (Auth & Accounts)
- Register / login (JWT issuance)
- Profile read/update
- Admin: list, block/unblock, delete, stats
- Roles: ADMIN, CUSTOMER; Status: ACTIVE, BLOCKED

### product-service (Catalog)
- Admin CRUD + status toggle (active/inactive)
- Public listing with filters (category, price range, search, pagination, sorting)
- Category enumeration & statistics

### order-service (Cart & Orders)
- One persistent cart per user
- Add/update/remove/clear items with stock validation
- Create orders (order number generation, status flow: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED / CANCELLED)
- Order & payment status tracking
- Admin order filtering + statistics

### analytics-service (KPIs & Reports)
- Aggregates data from user/order/product services
- Dashboard stats (total revenue, order counts, AOV, top products)
- Sales report (period grouping)

### wishlist-service (User Lists)
- Multiple collections per user
- Add/remove items (product snapshots)
- Stats + existence checks

### gateway-service (Edge)
- Central routing (/api/** → downstream services)
- CORS / potential auth filter
- Service discovery integration

### eureka-service (Discovery)
- Registry for dynamic location & load balancing
- Health status tracking of instances

---
## 4. Data Model (Simplified)

User: id, email, passwordHash, firstName, lastName, role, status, createdAt, lastLogin

Product: id, name, description, category, price, stockQuantity, status, imageUrl, createdAt

Cart: id, userId, items[*], totalAmount (derived)

Order: id, orderNumber, userId, status, paymentStatus, totals, shippingAddress, billingAddress, createdAt

WishlistCollection: id, userId, name, createdAt

WishlistItem: id, collectionId, productId, snapshotName, snapshotPrice, addedAt

Analytics Snapshot (example): id, periodDate, totalRevenue, orderCount, createdAt

---
## 5. Security & Auth

- Stateless JWT tokens returned on login (Authorization: Bearer <token>)
- Password hashing (BCrypt recommended)
- Route patterns: `/api/admin/**` restricted to ADMIN
- CORS: origin http://localhost:4200 allowed (configurable)
- JWT properties (sample): `jwt.secret`, `jwt.expiration`

**Error JSON Format:**
```json
{
  "timestamp": "2025-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/..."
}
```

---
## 6. Key API Endpoints (Condensed)

### User
- POST `/api/users/register`
- POST `/api/users/login`
- GET `/api/users/{id}`
- PUT `/api/users/{id}`
- GET `/api/users/email/{email}`
- Admin: GET `/api/admin/users`, GET `/api/admin/users/{id}`, PUT `/api/admin/users/{id}`, POST `/api/admin/users/{id}/block`, POST `/api/admin/users/{id}/unblock`, DELETE `/api/admin/users/{id}`, GET `/api/admin/users/stats`

### Product
- Public: GET `/api/products`, GET `/api/products/{id}`, GET `/api/products/categories`
- Admin: GET `/api/admin/products`, GET `/api/admin/products/all`, GET `/api/admin/products/{id}`, POST `/api/admin/products`, PUT `/api/admin/products/{id}`, DELETE `/api/admin/products/{id}`, GET `/api/admin/products/stats`

### Cart & Orders
- Cart: GET `/api/cart/{userId}`, POST `/api/cart/{userId}/items`, PUT `/api/cart/{userId}/items/{itemId}`, DELETE `/api/cart/{userId}/items/{itemId}`, DELETE `/api/cart/{userId}`, GET `/api/cart/{userId}/count`, GET `/api/cart/{userId}/validate`
- Orders (user): POST `/api/orders/users/{userId}`, GET `/api/orders/users/{userId}`, GET `/api/orders/users/{userId}/orders/{orderId}`, GET `/api/orders/users/{userId}/orders/number/{orderNumber}`, PUT `/api/orders/users/{userId}/orders/{orderId}/cancel`
- Orders (admin): GET `/api/admin/orders`, GET `/api/admin/orders/{orderId}`, PUT `/api/admin/orders/{orderId}/status`, GET `/api/admin/orders/status/{status}`, GET `/api/admin/orders/statistics`, GET `/api/admin/orders/users/{userId}`

### Wishlist
- Collections: GET `/api/wishlist/user/{userId}/collections`, POST `/api/wishlist/collections`, GET `/api/wishlist/collections/{collectionId}/user/{userId}`, PUT `/api/wishlist/collections/{collectionId}/user/{userId}`, DELETE `/api/wishlist/collections/{collectionId}/user/{userId}`
- Items: POST `/api/wishlist/collections/{collectionId}/user/{userId}/items`, DELETE `/api/wishlist/collections/{collectionId}/user/{userId}/items/{itemId}`, GET `/api/wishlist/user/{userId}/items`
- Utility: GET `/api/wishlist/user/{userId}/product/{productId}/exists`, GET `/api/wishlist/user/{userId}/stats`

### Analytics
- GET `/api/analytics/dashboard-stats`
- GET `/api/analytics/sales-report`
- GET `/api/analytics/health`

---
## 7. Environment & Configuration

Shared local dev values (override for prod):
```
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASS=root
JWT_SECRET=change-me-in-prod
```
Typical Spring properties per service:
```
spring.datasource.url=jdbc:mysql://localhost:3306/{schema}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
Production adjustments:
- Move secrets to ENV / Vault
- Use `ddl-auto=validate` + Flyway/Liquibase
- Disable SQL logging

---
## 8. Local Startup Sequence

1. Start MySQL (port 3306, ensure schemas auto-create)
2. `cd backend/eureka-service && mvn spring-boot:run`
3. `cd backend/gateway-service && mvn spring-boot:run`
4. Start domain services (parallel ok): user, product, order, analytics, wishlist
5. `cd frontend && npm install && ng serve`
6. Visit:
   - Eureka: http://localhost:8761
   - Gateway health: http://localhost:8080/actuator/health
   - Frontend: http://localhost:4200

Batch script: `start-all-services.bat` (sequential with delays)

---
## 9. Build & Test

Backend:
```
mvn clean verify
```
Frontend:
```
cd frontend
npm install
ng test
```
(If e2e configured: `ng e2e`)

---
## 10. Logging & Monitoring

- Default Spring Boot logging (INFO)
- Optional: elevate Hibernate logs only while debugging
- Add Actuator metrics for readiness/liveness (future)
- Future extensions: Prometheus + Grafana, centralized log aggregation (ELK / OpenSearch)

---
## 11. Error Handling & Validation

- ControllerAdvice (recommended) for consistent error model
- Bean Validation (Jakarta) on DTO inputs
- Defensive checks in service layer (ownership, entity existence)

---
## 12. Performance & Scalability (Roadmap)

Planned improvements:
- Redis caching (hot products, auth tokens blacklist)
- Async event-driven aggregation (Kafka or RabbitMQ)
- Rate limiting / throttling at gateway
- Containerization & orchestration (Docker + Kubernetes)
- Blue/Green or Canary deployments

---
## 13. Recommended Enhancements

| Area | Enhancement |
|------|-------------|
| Security | Refresh tokens, role-based method security |
| Observability | Distributed tracing (OpenTelemetry) |
| Resilience | Circuit breakers / retries for inter-service calls |
| Data | Flyway migrations for schema evolution |
| UX | Real-time updates (WebSockets / SSE) |

---
## 14. Swagger / OpenAPI (Optional)
Add to each service `pom.xml`:
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.5.0</version>
</dependency>
```
Then access: `http://localhost:{port}/swagger-ui.html`

---
## 15. Troubleshooting

| Symptom | Cause | Resolution |
|---------|-------|------------|
| Service not in Eureka | Wrong discovery URL | Verify `eureka.client.service-url.defaultZone` |
| 503 from Gateway | Target service starting | Retry after service registers |
| DB connection error | MySQL down/credentials wrong | Start MySQL / confirm root/root |
| JWT rejected | Expired / tampered token | Re-login to obtain new token |
| CORS blocked | Origin mismatch | Update allowed-origins in properties |

---
## 16. Development Conventions

- Branch naming: `feature/*`, `bugfix/*`
- Commit style: Conventional (feat, fix, docs, refactor, chore)
- Layered structure, no business logic in controllers
- DTOs isolate persistence layer from API outputs

---
## 17. Future Roadmap

- Payment gateway integration (Stripe / Razorpay)
- Inventory reservation & eventual consistency
- Notification service (email / SMS)
- Recommendation engine (collaborative filtering)
- Multi-currency & localization

---
## 18. Quick Reference URLs

| Purpose | URL |
|---------|-----|
| Eureka Dashboard | http://localhost:8761 |
| Gateway Health | http://localhost:8080/actuator/health |
| Frontend | http://localhost:4200 |
| User Swagger (if enabled) | http://localhost:8085/swagger-ui.html |
| Product Swagger | http://localhost:8084/swagger-ui.html |
| Order Swagger | http://localhost:8087/swagger-ui.html |
| Analytics Swagger | http://localhost:8088/swagger-ui.html |
| Wishlist Swagger | http://localhost:8089/swagger-ui.html |


---
**End of Documentation**
