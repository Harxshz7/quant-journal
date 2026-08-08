# Trading Journal Application — Master Technical Specification & Developer Guide

A production-ready, full-stack Trading Journal platform built with Java 21, Spring Boot 3, React 18, and PostgreSQL. Designed using Clean Architecture principles to record trades, analyze risk/reward dynamics, visualize performance, and maintain trading reflections with media upload capabilities.

---

## Table of Contents

1. [Project Overview & Tech Stack](#1-project-overview--tech-stack)
2. [Clean Architecture & System Design](#2-clean-architecture--system-design)
3. [Project Folder & Repository Structure](#3-project-folder--repository-structure)
4. [Database Schema & Data Model](#4-database-schema--data-model)
5. [REST API Specification](#5-rest-api-specification)
6. [Development Roadmap & Implementation Phases](#6-development-roadmap--implementation-phases)
7. [Developer Quick Start & Execution Guide](#7-developer-quick-start--execution-guide)
8. [Code Standards & Best Practices](#8-code-standards--best-practices)

---

## 1. Project Overview & Tech Stack

### Executive Summary
The Trading Journal Application enables quantitative and discretionary traders to:
- Log, monitor, and manage trade entries, exits, position sizing, and order types.
- Calculate key statistical metrics (Win Rate, Profit Factor, Gross/Net P&L, Risk/Reward Ratios).
- Attach technical analysis screenshots and interactive trade setup reflections.
- Monitor historical performance trends via analytics dashboards.

### Core Technology Stack

| Domain | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Backend** | Java | 21 (LTS) | Modern Java runtime featuring Records & Pattern Matching |
| | Spring Boot | 3.x | Enterprise application framework |
| | Spring Security | 6.x | Stateless JWT authentication & RBAC authorization |
| | Spring Data JPA | 3.x | ORM & data persistence via Hibernate |
| | PostgreSQL | 15+ | Relational financial data store |
| | Maven | 3.8+ | Dependency management and build automation |
| **Frontend** | React | 18.x | Component-driven Single Page Application |
| | React Router | 6.x | Client-side routing |
| | Axios | 1.x | Promise-based HTTP client |
| **DevOps** | Docker | 24+ | Containerization engine |
| | Docker Compose | 2.x | Multi-container dev & production deployment orchestration |

---

## 2. Clean Architecture & System Design

The backend enforces strict **Clean Architecture** boundaries. High-level modules do not depend on low-level infrastructure modules; both depend on abstractions.

```
┌───────────────────────────────────────────────────────────┐
│           Presentation Layer (REST Controllers)           │
│           - Request Validation & Response Serialization   │
└─────────────────────────────┬─────────────────────────────┘
                              │ (Depends On)
                              ▼
┌───────────────────────────────────────────────────────────┐
│            Application Layer (Services & DTOs)            │
│            - Transaction Logic & DTO Mapping              │
└─────────────────────────────┬─────────────────────────────┘
                              │ (Depends On)
                              ▼
┌───────────────────────────────────────────────────────────┐
│               Domain Layer (Entities & Rules)             │
│               - Domain Models & Business Logic            │
└─────────────────────────────┬─────────────────────────────┘
                              │ (Depends On)
                              ▼
┌───────────────────────────────────────────────────────────┐
│           Infrastructure Layer (Repositories & DB)        │
│           - Database Access, Spring Data JPA, Storage     │
└───────────────────────────────────────────────────────────┘
```

### Architectural Principles
- **Separation of Concerns**: Controllers only handle HTTP mechanics; Services handle orchestration; Entities encapsulate domain rules; Repositories handle database persistence.
- **Stateless Authentication**: Security is maintained via JWT bearer tokens (1-hour expiration with 7-day refresh cycles).
- **Denormalized Analytical Caching**: Complex statistical aggregations are continuously maintained in a `trade_statistics` table for sub-50ms dashboard loading.

---

## 3. Project Folder & Repository Structure

```
quant-journal/
├── docker-compose.yml             # Local multi-container database & services deployment
├── run.bat                        # Windows execution helper script
├── README.md                      # Unified Master Documentation
├── backend/                       # Spring Boot Application Root
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/tradingjournal/
│       │   │   ├── presentation/   # REST Controllers, API Request/Response DTOs
│       │   │   ├── application/    # Service Implementation, Mappers, Business Logic
│       │   │   ├── domain/         # JPA Entities, Enums, Domain Exceptions
│       │   │   ├── infrastructure/ # JPA Repositories, Security Interceptors, Storage
│       │   │   └── common/         # Global Exception Handlers, Audit Loggers
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       └── application-prod.yml
│       └── test/                   # JUnit 5 & Mockito test suite
└── frontend/                      # React SPA Application Root
    ├── Dockerfile
    ├── package.json
    └── src/
        ├── api/                    # Axios API integration endpoints
        ├── components/             # Reusable UI widgets & navigation elements
        ├── hooks/                  # Custom React state management hooks
        ├── pages/                  # Top-level routes (Dashboard, TradeHistory, Auth)
        └── utils/                  # Formatting & calculation utilities
```

---

## 4. Database Schema & Data Model

### Entity-Relationship Architecture

```
┌──────────────┐          1:M          ┌──────────────┐          1:M          ┌───────────────────┐
│    Users     ├──────────────────────►│    Trades    ├──────────────────────►│ Trade_Screenshots │
└──────┬───────┘                       └──────┬───────┘                       └───────────────────┘
       │ 1:1                                  │ 1:M
       ▼                                      ▼
┌──────────────┐                       ┌──────────────┐
│  Trade_Stats │                       │JournalEntries│
└──────────────┘                       └──────────────┘
```

### DDL Specification (PostgreSQL)

```sql
-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Users Table
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Trades Table
CREATE TABLE trades (
    trade_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    ticker VARCHAR(10) NOT NULL,
    position_type VARCHAR(10) NOT NULL CHECK (position_type IN ('LONG', 'SHORT')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED', 'PENDING', 'CANCELLED')),
    entry_date TIMESTAMP NOT NULL,
    entry_price DECIMAL(15,4) NOT NULL CHECK (entry_price > 0),
    quantity DECIMAL(15,2) NOT NULL CHECK (quantity > 0),
    exit_date TIMESTAMP,
    exit_price DECIMAL(15,4) CHECK (exit_price IS NULL OR exit_price > 0),
    gross_pnl DECIMAL(15,2),
    net_pnl DECIMAL(15,2),
    pnl_percentage DECIMAL(10,4),
    risk_amount DECIMAL(15,2) CHECK (risk_amount IS NULL OR risk_amount >= 0),
    reward_amount DECIMAL(15,2) CHECK (reward_amount IS NULL OR reward_amount >= 0),
    risk_reward_ratio DECIMAL(10,4),
    win_loss VARCHAR(10) CHECK (win_loss IN ('WIN', 'LOSS', 'BREAK_EVEN', 'OPEN')),
    trading_strategy VARCHAR(100),
    setup_description TEXT,
    notes TEXT,
    lessons_learned TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Trade Screenshots Table
CREATE TABLE trade_screenshots (
    screenshot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_id UUID NOT NULL REFERENCES trades(trade_id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size <= 10485760),
    file_type VARCHAR(50) NOT NULL CHECK (file_type IN ('image/jpeg', 'image/png', 'image/gif', 'image/webp')),
    original_width INT,
    original_height INT,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Trade Statistics Table (Denormalized)
CREATE TABLE trade_statistics (
    stats_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    total_trades INT DEFAULT 0,
    winning_trades INT DEFAULT 0,
    losing_trades INT DEFAULT 0,
    break_even_trades INT DEFAULT 0,
    win_rate DECIMAL(5,2) DEFAULT 0 CHECK (win_rate BETWEEN 0 AND 100),
    total_gross_pnl DECIMAL(15,2) DEFAULT 0,
    total_net_pnl DECIMAL(15,2) DEFAULT 0,
    largest_win DECIMAL(15,2),
    largest_loss DECIMAL(15,2),
    average_win DECIMAL(15,2),
    average_loss DECIMAL(15,2),
    profit_factor DECIMAL(10,4),
    total_risk_amount DECIMAL(15,2) DEFAULT 0,
    total_reward_amount DECIMAL(15,2) DEFAULT 0,
    average_risk_reward_ratio DECIMAL(10,4),
    consecutive_wins INT DEFAULT 0,
    consecutive_losses INT DEFAULT 0,
    max_consecutive_wins INT DEFAULT 0,
    max_consecutive_losses INT DEFAULT 0,
    last_calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_trades_user_status ON trades(user_id, status);
CREATE INDEX idx_trades_entry_date ON trades(user_id, entry_date DESC);
CREATE INDEX idx_screenshots_trade ON trade_screenshots(trade_id);
```

---

## 5. REST API Specification

### Base Path: `/api/v1`

#### 5.1 Authentication Endpoints

##### User Registration
- **`POST /auth/register`**
- **Request Payload**:
  ```json
  {
    "username": "trader123",
    "email": "trader@example.com",
    "password": "SecurePassword123!",
    "firstName": "John",
    "lastName": "Doe"
  }
  ```
- **Response `201 Created`**:
  ```json
  {
    "userId": "d3b07384-d113-460a-4c8a-48d011d61a29",
    "username": "trader123",
    "email": "trader@example.com",
    "message": "User registered successfully"
  }
  ```

##### User Login
- **`POST /auth/login`**
- **Request Payload**:
  ```json
  {
    "username": "trader123",
    "password": "SecurePassword123!"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
  ```

#### 5.2 Trade Management Endpoints

##### Create Trade
- **`POST /trades`** *(Requires Auth)*
- **Request Payload**:
  ```json
  {
    "ticker": "AAPL",
    "positionType": "LONG",
    "entryDate": "2026-06-26T10:30:00Z",
    "entryPrice": 150.50,
    "quantity": 100,
    "exitDate": "2026-06-27T14:00:00Z",
    "exitPrice": 152.75,
    "riskAmount": 50.00,
    "rewardAmount": 225.00,
    "tradingStrategy": "Breakout",
    "notes": "Broke out of resistance with strong volume"
  }
  ```
- **Response `201 Created`**:
  ```json
  {
    "tradeId": "c8f1e582-74bf-42d2-a7f2-c9d311f421a1",
    "status": "CLOSED",
    "grossPnL": 225.00,
    "netPnL": 225.00,
    "pnlPercentage": 1.49,
    "winLoss": "WIN",
    "riskRewardRatio": 4.5
  }
  ```

##### List Trades (Paginated & Filterable)
- **`GET /trades?page=0&size=20&sort=entryDate,desc&status=CLOSED&ticker=AAPL`**
- **Response `200 OK`**:
  ```json
  {
    "content": [
      {
        "tradeId": "c8f1e582-74bf-42d2-a7f2-c9d311f421a1",
        "ticker": "AAPL",
        "positionType": "LONG",
        "status": "CLOSED",
        "grossPnL": 225.00,
        "winLoss": "WIN"
      }
    ],
    "pageable": { "pageNumber": 0, "pageSize": 20, "totalElements": 1, "totalPages": 1 }
  }
  ```

#### 5.3 Analytics & Screenshot Endpoints

- **`GET /dashboard/overview`**: Retrieves summary metrics (Total Trades, Win Rate, Profit Factor, Total P&L).
- **`POST /trades/{tradeId}/screenshots`**: Uploads image files (multipart form field `file`, max 10MB).
- **`GET /trades/{tradeId}/screenshots`**: Lists metadata for trade screenshots.

---

## 6. Development Roadmap & Implementation Phases

| Phase | Timeline | Key Modules & Deliverables |
| :--- | :--- | :--- |
| **Phase 1: Foundation** | Weeks 1–4 | Spring Boot setup, PostgreSQL Docker container, Flyway schema, JWT security core |
| **Phase 2: User Management** | Weeks 5–7 | User registration, authentication endpoints, token refresh logic, user isolation |
| **Phase 3: Trade Operations** | Weeks 8–11 | Trade CRUD operations, P&L & Risk/Reward calculation algorithms, pagination |
| **Phase 4: Analytics** | Weeks 12–15 | `TradeStatistics` caching engine, dashboard metrics, time-series performance charts |
| **Phase 5: File Storage** | Week 16 | Screenshot upload service, local/S3 storage manager, mime-type validation |
| **Phase 6: QA & Testing** | Weeks 17–18 | Unit test suite (80%+ target coverage), integration tests, load tests |
| **Phase 7: Deployment** | Weeks 19–20 | Docker multi-stage builds, production configuration hardening, CI/CD pipeline |

---

## 7. Developer Quick Start & Execution Guide

### Prerequisites
- Java 21 JDK
- Node.js 18+ LTS
- Docker & Docker Compose
- Maven 3.8+

### Windows: one-click
Run `run.bat` from the project root. It checks prerequisites, starts Postgres, then opens the backend and frontend in separate windows.

### Manual setup

#### 1. Database
```bash
docker compose up -d postgres
```
DB: `trading_journal`, user: `trading_user`, password: `trading_password`, port `5432`.

> If you ran this before the Aug 2026 schema change (journal entries added), drop the old volume first: `docker compose down -v`, then bring it back up. `ddl-auto: update` won't add a required column to an existing table on its own.

#### 2. Backend
```bash
cd backend
mvn spring-boot:run
```
- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

#### 3. Frontend
```bash
cd frontend
npm install
npm start
```
- App: `http://localhost:3000`

### Current state (v1)
Single-user, no login — every endpoint is open. `POST/GET /api/v1/journal` for daily entries, `POST/GET /api/v1/trades` for trades attached to an entry. Auth returns once there's a real `User` entity.

---

## 8. Code Standards & Best Practices

1. **Transactional Boundaries**: All service methods modifying state must be annotated with `@Transactional`.
2. **DTO Isolation**: Entities must never be returned directly by Controller endpoints; convert via Mapper components to DTOs.
3. **Global Exception Handling**: Throw business-specific domain exceptions (e.g., `TradeNotFoundException`) and let `@ControllerAdvice` catch and format standardized error payloads:
   ```json
   {
     "timestamp": "2026-06-26T10:30:00Z",
     "status": 404,
     "error": "Not Found",
     "message": "Trade not found: c8f1e582-74bf-42d2-a7f2-c9d311f421a1"
   }
   ```
4. **Data Types**: All financial values (prices, quantities, P&L amounts) must use `BigDecimal` to prevent floating-point precision loss.