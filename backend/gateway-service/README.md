# API Gateway Service

This service acts as the single entry point for all client requests in the e-commerce microservices architecture, providing routing, load balancing, and cross-cutting concerns.

## Overview

The API Gateway provides:
- Centralized routing to microservices
- Load balancing with service discovery
- Cross-cutting concerns (CORS, logging, monitoring)
- Single entry point for clients
- Service abstraction for frontend applications

## Configuration

### Application Properties
- **Port**: 8080 (main entry point)
- **Application Name**: gateway-service
- **Eureka Integration**: Enabled for service discovery

### Gateway Routes

The gateway is configured with the following routes:

| Service | Path Pattern | Target Service | Description |
|---------|-------------|----------------|-------------|
| User Service | `/api/users/**`, `/api/admin/users/**` | `lb://user-service` | User management and authentication |
| Product Service | `/api/products/**`, `/api/admin/products/**` | `lb://product-service` | Product catalog management |
| Order Service | `/api/orders/**`, `/api/cart/**`, `/api/admin/orders/**` | `lb://order-service` | Order and cart management |
| Analytics Service | `/api/analytics/**` | `lb://analytics-service` | Analytics and reporting |
| Wishlist Service | `/api/wishlist/**` | `lb://wishlist-service` | Wishlist management |

### Key Features
- **Load Balancing**: Uses `lb://` prefix for automatic load balancing
- **Service Discovery**: Integrates with Eureka for dynamic service discovery
- **Route Locator**: Automatically discovers services and creates routes
- **Health Monitoring**: Exposes actuator endpoints for monitoring

## Running the Service

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Eureka Server running on port 8761

### Build and Run
```bash
# Navigate to gateway-service directory
cd gateway-service

# Build the project
mvn clean compile

# Run the service
mvn spring-boot:run
```

### Alternative: Run JAR
```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/gateway-service-1.0.0.jar
```

## API Endpoints

### Service Routing

All API requests should be made through the gateway:

```bash
# Instead of calling services directly:
# http://localhost:8085/api/users/1

# Call through gateway:
http://localhost:8080/api/users/1
```

### Health and Monitoring

```bash
# Gateway health
GET http://localhost:8080/actuator/health

# Gateway info
GET http://localhost:8080/actuator/info

# Gateway routes (shows all configured routes)
GET http://localhost:8080/actuator/gateway/routes
```

## Service Integration

### Frontend Integration

Configure your frontend application to use the gateway as the base URL:

```typescript
// Angular environment configuration
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### Service Discovery

The gateway automatically discovers services registered with Eureka:

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
```

## Architecture Flow

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │───▶│  Gateway Service │───▶│  User Service   │
│  (Port 4200)    │    │   (Port 8080)    │    │  (Port 8085)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ├───────────────────▶┌─────────────────┐
                                │                    │ Product Service │
                                │                    │  (Port 8084)    │
                                │                    └─────────────────┘
                                │
                                ├───────────────────▶┌─────────────────┐
                                │                    │  Order Service  │
                                │                    │  (Port 8087)    │
                                │                    └─────────────────┘
                                │
                                ├───────────────────▶┌─────────────────┐
                                │                    │Analytics Service│
                                │                    │  (Port 8088)    │
                                │                    └─────────────────┘
                                │
                                └───────────────────▶┌─────────────────┐
                                                     │Wishlist Service │
                                                     │  (Port 8086)    │
                                                     └─────────────────┘
                                            ▲
                                            │
                                    ┌──────────────────┐
                                    │  Eureka Server   │
                                    │   (Port 8761)    │
                                    └──────────────────┘
```

## Configuration Details

### Route Configuration (application.yml)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**, /api/admin/users/**
          filters:
            - StripPrefix=0
```

### Load Balancing

The gateway uses Spring Cloud LoadBalancer with Eureka for:
- Round-robin load balancing
- Health-based routing
- Automatic failover
- Service instance discovery

## Troubleshooting

### Common Issues

1. **Service not found (404)**
   - Check if target service is registered with Eureka
   - Verify service name matches Eureka registration
   - Ensure route configuration is correct

2. **Gateway not starting**
   - Check if port 8080 is available
   - Verify Eureka server is running and accessible
   - Check application.yml syntax

3. **Routes not working**
   - Check path patterns in route configuration
   - Verify predicates match incoming requests
   - Check if StripPrefix filter is correctly configured

4. **Load balancing issues**
   - Ensure multiple instances of services are running
   - Check Eureka dashboard for service instances
   - Verify load balancer configuration

### Debug Mode

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
```

### Monitoring Routes

Check active routes:
```bash
curl http://localhost:8080/actuator/gateway/routes | jq
```

## Security Considerations

For production deployment:
1. Implement authentication and authorization
2. Add rate limiting
3. Configure HTTPS/TLS
4. Set up request/response filtering
5. Implement circuit breaker patterns
6. Add request logging and monitoring

## Performance Tuning

1. **Connection Pooling**: Configure WebClient connection pools
2. **Timeout Settings**: Set appropriate timeout values
3. **Buffer Sizes**: Adjust buffer sizes for large payloads
4. **Circuit Breakers**: Implement circuit breaker patterns
5. **Caching**: Add response caching where appropriate

## Development Tips

1. **Service Testing**: Test individual services before gateway integration
2. **Route Testing**: Use actuator endpoints to verify route configuration
3. **Eureka Dashboard**: Monitor service registration status
4. **Logging**: Enable detailed logging during development
5. **Health Checks**: Regularly check service and gateway health 