# Trading Journal Application - Architecture Design

## 1. System Overview

The Trading Journal Application follows a **Clean Architecture** pattern with clear separation of concerns:
- **Frontend**: React SPA with JWT token-based authentication
- **Backend**: Spring Boot 3 REST API with layered architecture
- **Database**: PostgreSQL for persistent data storage
- **Security**: JWT-based authentication and authorization

## 2. Clean Architecture Layers

### Backend Architecture (Spring Boot 3)

```
┌─────────────────────────────────────────┐
│   Presentation Layer (REST Controllers) │
│   - Request/Response Handling           │
│   - Input Validation                    │
└─────────────────────────────────────────┘
            ↓ (Depends On)
┌─────────────────────────────────────────┐
│   Application Layer (Services/DTOs)     │
│   - Business Logic                      │
│   - DTO Transformation                  │
│   - Transaction Management              │
└─────────────────────────────────────────┘
            ↓ (Depends On)
┌─────────────────────────────────────────┐
│   Domain Layer (Entities/Models)        │
│   - Core Business Logic                 │
│   - Domain Rules                        │
│   - Entity Definitions                  │
└─────────────────────────────────────────┘
            ↓ (Depends On)
┌─────────────────────────────────────────┐
│   Infrastructure Layer (Repositories)   │
│   - Data Access                         │
│   - Database Operations                 │
│   - External Services Integration       │
└─────────────────────────────────────────┘
```

### Key Architectural Principles

1. **Dependency Inversion**: High-level modules don't depend on low-level modules; both depend on abstractions
2. **Single Responsibility**: Each class has one reason to change
3. **Open/Closed Principle**: Open for extension, closed for modification
4. **Repository Pattern**: Abstraction over data access layer
5. **DTO Pattern**: Clear contracts between layers
6. **Global Exception Handling**: Centralized error management
7. **Validation**: Input validation at multiple layers

## 3. Design Patterns Used

| Pattern | Usage | Location |
|---------|-------|----------|
| **Repository Pattern** | Abstract data access | Infrastructure Layer |
| **DTO (Data Transfer Object)** | Data exchange between layers | Application & Presentation |
| **Service Layer Pattern** | Encapsulate business logic | Application Layer |
| **Dependency Injection** | Loose coupling | Spring Framework |
| **Factory Pattern** | Create complex objects | Services |
| **Strategy Pattern** | Different calculation algorithms | Risk/Reward Analysis |
| **Observer Pattern** | Event-driven updates | Trade notifications |
| **Singleton Pattern** | JWT Provider, Config | Infrastructure |

## 4. Core Components

### Backend Components

#### a) Authentication & Security
- JWT Token Provider
- Authentication Filter
- Authorization Interceptor
- Password Encryption (BCrypt)
- Role-Based Access Control (RBAC)

#### b) Trade Management
- Trade Service (business logic)
- Trade Repository (data access)
- Trade Entity (domain model)
- TradeDTO (data transfer)
- Trade Controller (REST endpoints)

#### c) Statistics & Analytics
- Statistics Service
- Win Rate Calculator
- P&L Tracker
- Risk/Reward Analyzer
- Dashboard Service

#### d) File Upload Management
- Screenshot Upload Service
- Storage Manager (local/cloud)
- File Validation
- Image Compression

#### e) Cross-Cutting Concerns
- Global Exception Handler
- Logging & Auditing
- Request/Response Interceptors
- CORS Configuration
- Validation Framework

### Frontend Components

#### a) Authentication
- Login Page
- Registration Page
- JWT Token Management
- Protected Routes

#### b) Trade Management UI
- Trade Form (Create/Edit)
- Trade List/History
- Trade Details View
- Delete Confirmation

#### c) Analytics Dashboard
- Win Rate Display
- P&L Chart
- Trade Statistics Table
- Risk/Reward Analysis
- Performance Metrics

#### d) Common Components
- Navigation Bar
- Header/Footer
- Loading Spinners
- Toast Notifications
- Modal Dialogs

## 5. Data Flow Diagram

```
User Request
    ↓
REST Controller
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
PostgreSQL Database
    ↓
Response → DTO → JSON → Frontend
```

## 6. Technology Stack Justification

| Technology | Purpose | Justification |
|------------|---------|---------------|
| Java 21 | Backend Language | Latest LTS with records, pattern matching, improved performance |
| Spring Boot 3 | Framework | Enterprise-ready, extensive ecosystem, excellent security |
| PostgreSQL | Database | Robust, ACID-compliant, excellent for financial data |
| React | Frontend | Component-based, excellent tooling, large ecosystem |
| JWT | Authentication | Stateless, scalable, industry standard |
| Maven | Build Tool | Dependency management, standardized build lifecycle |
| JUnit 5 | Testing | Modern testing framework, Spring integration |
| Mockito | Mocking | Effective for unit testing service layer |
| Docker | Containerization | Consistency across environments |

## 7. Security Considerations

1. **Authentication**: JWT tokens with expiration and refresh mechanism
2. **Authorization**: Role-based access control (User, Admin)
3. **Data Protection**: BCrypt password hashing, encryption of sensitive data
4. **API Security**: CORS configuration, CSRF protection, input validation
5. **Database Security**: SQL injection prevention via parameterized queries/ORM
6. **Audit Trail**: Logging of all trade operations
7. **Rate Limiting**: Prevent brute force attacks
8. **HTTPS**: SSL/TLS encryption in production

## 8. Scalability Considerations

1. **Database Indexing**: On frequently queried columns (userId, date, ticker)
2. **Caching**: Redis for user sessions and statistics
3. **Pagination**: Large trade history queries
4. **Async Processing**: Background jobs for calculations/file uploads
5. **Connection Pooling**: HikariCP for efficient DB connections
6. **API Versioning**: /api/v1/, /api/v2/ for backward compatibility
7. **Microservices Ready**: Service boundaries allow future extraction

## 9. Deployment Architecture

```
User Browser (React SPA)
        ↓ HTTPS
Load Balancer
        ↓
Spring Boot API Servers (Multiple instances)
        ↓
PostgreSQL Master-Replica
        ↓
S3/Cloud Storage (Screenshots)
```

## 10. Testing Strategy

| Test Type | Coverage | Tools |
|-----------|----------|-------|
| Unit Tests | 80%+ | JUnit 5, Mockito |
| Integration Tests | Services & Repositories | Spring Boot Test, TestContainers |
| API Tests | REST Endpoints | MockMvc, RestAssured |
| Frontend Tests | React Components | Jest, React Testing Library |
| E2E Tests | Full user workflows | Cypress/Playwright |

## 11. Logging & Monitoring

1. **Application Logging**: SLF4J + Logback
2. **Performance Monitoring**: Spring Boot Actuator
3. **Metrics**: Micrometer for application metrics
4. **Error Tracking**: Sentry/ELK Stack for production
5. **Audit Logs**: Track all trade operations with timestamps and user info

## 12. Configuration Management

- **application.yml**: Spring Boot configuration
- **application-dev.yml**: Development environment
- **application-prod.yml**: Production environment
- **Environment Variables**: For sensitive data (DB passwords, API keys)
- **Profiles**: Maven profiles for different builds

## 13. API Versioning Strategy

- Version in URL: `/api/v1/trades`
- Version in Header: `Accept: application/vnd.tradingjournal.v1+json`
- Backward compatibility maintained for previous versions
- Deprecation warnings in responses

## 14. Database Connection & Transaction Management

```
Spring Data JPA
    ↓
Hibernate ORM
    ↓
HikariCP Connection Pool
    ↓
PostgreSQL Driver
    ↓
PostgreSQL Database
```

- **Transaction Scope**: Service layer
- **Lazy Loading**: Configured for optimal performance
- **Connection Pool Size**: 10-20 connections based on load
- **Query Optimization**: Native queries for complex analytics

## 15. Error Handling Strategy

```
Exception
    ↓
Global Exception Handler
    ↓
Error Response (Standardized)
    ↓
Logging
    ↓
HTTP Status Code + Error Message
```

Standard Error Response Format:
```json
{
  "timestamp": "2026-06-26T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "quantity",
      "message": "Quantity must be greater than 0"
    }
  ]
}
```

