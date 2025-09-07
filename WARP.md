# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a microservices-based online canteen ordering system (高校食堂订餐系统) built with Spring Boot and Vue.js. The system supports student ordering, merchant management, and administrative functions.

## Technology Stack

- **Backend**: Java 17, Spring Boot 2.7.18, Spring Cloud 2021.0.8, MyBatis Plus
- **Database**: MySQL 8.0.33
- **Authentication**: JWT 0.11.5
- **Frontend**: Vue 3, Element Plus, Vite
- **Merchant Client**: Java Swing desktop application
- **Build Tools**: Maven, npm

## Architecture Overview

### Microservices Structure
The system follows a microservices architecture with these components:

- **canteen-common**: Shared utilities, DTOs, exception handling, JWT utils, and common controllers
- **canteen-user-service** (Port 8081): User management, authentication, admin/merchant/customer roles
- **canteen-product-service** (Port 8082): Food item management, categories, inventory
- **canteen-order-service** (Port 8083): Order processing, payment handling
- **canteen-recommend-service** (Port 8084): Recommendation algorithms and hot items
- **canteen-gateway** (Port 8080): API gateway for routing and load balancing
- **canteen-web-app**: Vue.js frontend application for customers
- **canteen-merchant-client**: Java Swing desktop app for merchants

### Key Architectural Patterns
- **Domain-Driven Design**: Each service has its own domain with controllers, services, DTOs, and mappers
- **JWT Authentication**: Shared authentication through common module
- **REST API**: RESTful endpoints with standardized response format (Result wrapper)
- **Service Discovery**: Uses Spring Cloud service discovery (configured for Nacos)
- **Cross-Origin Support**: CORS configuration in each service

### Database Structure
- Single MySQL database `canteen_system` shared across microservices
- MyBatis Plus for ORM with mapper interfaces
- Test data initialization through SQL scripts in `database/` folder

## Common Development Commands

### Backend Development

#### Build and Test
```bash
# Clean and build entire project
mvn clean install

# Build specific service
cd canteen-user-service && mvn clean package

# Run tests for entire project
mvn test

# Run tests for specific service
cd canteen-user-service && mvn test
```

#### Running Services
Services must be started in dependency order:

```bash
# Start individual service with Maven
cd canteen-user-service && mvn spring-boot:run

# Start all backend services (run each in separate terminal)
cd canteen-user-service && mvn spring-boot:run
cd canteen-product-service && mvn spring-boot:run  
cd canteen-order-service && mvn spring-boot:run
cd canteen-recommend-service && mvn spring-boot:run
cd canteen-gateway && mvn spring-boot:run
```

#### Alternative JAR execution
```bash
# Build and run as JAR
mvn package
java -jar target/canteen-user-service-1.0.0.jar
```

### Frontend Development

#### Web Application (Vue.js)
```bash
cd canteen-web-app

# Install dependencies
npm install

# Start development server (runs on http://localhost:5173)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

#### Merchant Desktop Client
```bash
cd canteen-merchant-client

# Build desktop application
mvn package

# Run desktop application
java -jar target/canteen-merchant-client-1.0.0.jar
```

### Database Setup
```bash
# Initialize database (run in MySQL)
mysql -u root -p < database/init.sql

# Load test data
mysql -u root -p < database/test_users.sql
```

### API Testing
```bash
# Test user registration
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456", "email": "test@example.com", "realName": "测试用户"}'

# Test user login
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "123456"}'

# Get product list
curl "http://localhost:8082/api/products?current=1&size=10"

# Get product categories
curl http://localhost:8082/api/products/categories
```

## Development Notes

### Service Ports
- Gateway: 8080 (main API entry point)
- User Service: 8081
- Product Service: 8082  
- Order Service: 8083
- Recommend Service: 8084
- Frontend: 5173 (development)

### Default Test Accounts
- Admin: `admin` / `admin123`
- Merchant: `merchant` / `admin123`
- User: `user1` / `admin123`

### Configuration Requirements
Each service requires database configuration in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/canteen_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### Code Structure Patterns
- **Controllers**: Handle HTTP requests and responses using Result wrapper
- **Services**: Business logic implementation with @Service annotation
- **DTOs**: Data transfer objects for API communication
- **Entities**: MyBatis Plus database entities with annotations
- **Mappers**: MyBatis Plus mapper interfaces extending BaseMapper

### Common Issues
- Ensure MySQL is running and accessible
- Check that required ports (8080-8084, 5173) are not occupied
- Verify Java 17 is being used
- Services have inter-dependencies, start in correct order
- Frontend API calls route through gateway (port 8080) in production

### Authentication Flow
- JWT tokens generated in user-service
- JwtUtils in canteen-common handles token validation
- JwtAuthenticationFilter in each service validates requests
- Tokens include user role information (ADMIN, MERCHANT, USER)
