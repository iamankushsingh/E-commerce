# Eureka Service Discovery Server

This service acts as the central service registry for the e-commerce microservices architecture, enabling service discovery and load balancing.

## Overview

The Eureka Server provides:
- Service registration and discovery
- Health monitoring of registered services
- Load balancing capabilities
- Fault tolerance and resilience

## Configuration

### Application Properties
- **Port**: 8761 (standard Eureka port)
- **Application Name**: eureka-service
- **Self Registration**: Disabled (server doesn't register with itself)
- **Fetch Registry**: Disabled (server doesn't fetch from itself)

### Key Settings
```properties
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.enable-self-preservation=false
eureka.server.eviction-interval-timer-in-ms=4000
```

## Running the Service

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build and Run
```bash
# Navigate to eureka-service directory
cd eureka-service

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
java -jar target/eureka-service-1.0.0.jar
```

## Accessing the Dashboard

Once running, access the Eureka dashboard at:
```
http://localhost:8761
```

The dashboard shows:
- Registered services
- Service instances
- Health status
- Service metadata

## Registered Services

The following services will register with this Eureka server:
- **analytics-service** (Port: 8088)
- **order-service** (Port: 8087)  
- **product-service** (Port: 8084)
- **user-service** (Port: 8085)
- **wishlist-service** (Port: 8089)
- **gateway-service** (Port: 8080)

## Service URLs

All services are configured to connect to Eureka at:
```
http://localhost:8761/eureka/
```

## Health Check

The Eureka server exposes health endpoints for monitoring:
- Health: `http://localhost:8761/actuator/health`

## Troubleshooting

### Common Issues

1. **Services not registering**
   - Check if Eureka server is running on port 8761
   - Verify service configuration points to correct Eureka URL
   - Check network connectivity

2. **Self-preservation mode**
   - Disabled by default for development
   - In production, consider enabling for better fault tolerance

3. **Port conflicts**
   - Ensure port 8761 is available
   - Check for other applications using the same port

### Logs
Monitor logs for service registration/deregistration events:
```bash
# View logs
mvn spring-boot:run | grep -E "(Registered|Deregistered|Cancelled)"
```

## Architecture Integration

The Eureka server is the foundation of the microservices architecture:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │───▶│  Gateway Service │───▶│  Microservices  │
│  (Angular)      │    │    (Port 8080)   │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │  Eureka Server   │◀───│ Service Registry│
                       │   (Port 8761)    │    │   & Discovery   │
                       └──────────────────┘    └─────────────────┘
```

## Production Considerations

For production deployment:
1. Enable self-preservation mode
2. Configure multiple Eureka instances for high availability
3. Set up proper security (authentication/authorization)
4. Configure appropriate timeout and retry settings
5. Set up monitoring and alerting 