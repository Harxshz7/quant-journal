# Trading Journal Application - Folder Structure

## 1. Complete Project Structure

```
TradingJournalApp/
├── backend/                          # Spring Boot 3 Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/tradingjournal/
│   │   │   │       ├── TradingJournalApplication.java    # Main entry point
│   │   │   │       │
│   │   │   │       ├── config/                            # Configuration Classes
│   │   │   │       │   ├── SecurityConfig.java            # Spring Security & JWT config
│   │   │   │       │   ├── CorsConfig.java                # CORS configuration
│   │   │   │       │   ├── JacksonConfig.java             # JSON serialization
│   │   │   │       │   ├── WebConfig.java                 # Web MVC configuration
│   │   │   │       │   ├── JwtConfig.java                 # JWT properties
│   │   │   │       │   └── FileUploadConfig.java          # File upload settings
│   │   │   │       │
│   │   │   │       ├── presentation/                      # Presentation Layer (Controllers)
│   │   │   │       │   ├── auth/
│   │   │   │       │   │   ├── AuthController.java        # Login, Register, Refresh Token
│   │   │   │       │   │   └── AuthRequest.java           # DTO
│   │   │   │       │   │
│   │   │   │       │   ├── trade/
│   │   │   │       │   │   ├── TradeController.java       # Trade CRUD operations
│   │   │   │       │   │   ├── TradeDTO.java              # Trade data transfer object
│   │   │   │       │   │   ├── CreateTradeRequest.java    # Create trade request DTO
│   │   │   │       │   │   ├── UpdateTradeRequest.java    # Update trade request DTO
│   │   │   │       │   │   └── TradeResponse.java         # Trade response DTO
│   │   │   │       │   │
│   │   │   │       │   ├── screenshot/
│   │   │   │       │   │   ├── ScreenshotController.java  # File upload/download
│   │   │   │       │   │   └── ScreenshotDTO.java         # Screenshot data transfer
│   │   │   │       │   │
│   │   │   │       │   ├── statistics/
│   │   │   │       │   │   ├── StatisticsController.java  # Dashboard stats endpoints
│   │   │   │       │   │   ├── StatisticsDTO.java         # Statistics data transfer
│   │   │   │       │   │   ├── WinRateDTO.java            # Win rate metrics
│   │   │   │       │   │   ├── PnLDTO.java                # Profit/Loss metrics
│   │   │   │       │   │   └── RiskRewardDTO.java         # Risk/Reward metrics
│   │   │   │       │   │
│   │   │   │       │   └── user/
│   │   │   │       │       ├── UserController.java        # User profile endpoints
│   │   │   │       │       ├── UserDTO.java               # User data transfer
│   │   │   │       │       └── UserProfileRequest.java    # Update profile request
│   │   │   │       │
│   │   │   │       ├── application/                       # Application Layer (Services)
│   │   │   │       │   ├── auth/
│   │   │   │       │   │   ├── AuthService.java           # Authentication business logic
│   │   │   │       │   │   ├── JwtTokenProvider.java      # JWT token generation/validation
│   │   │   │       │   │   └── UserDetailsServiceImpl.java # Spring Security user details
│   │   │   │       │   │
│   │   │   │       │   ├── trade/
│   │   │   │       │   │   ├── TradeService.java          # Trade business logic
│   │   │   │       │   │   ├── TradeValidator.java        # Trade validation rules
│   │   │   │       │   │   ├── TradePnLCalculator.java    # P&L calculation logic
│   │   │   │       │   │   └── TradeMapper.java           # Entity ↔ DTO mapping
│   │   │   │       │   │
│   │   │   │       │   ├── statistics/
│   │   │   │       │   │   ├── StatisticsService.java     # Statistics aggregation
│   │   │   │       │   │   ├── WinRateCalculator.java     # Win rate calculation
│   │   │   │       │   │   ├── PnLCalculator.java         # P&L calculation
│   │   │   │       │   │   ├── RiskRewardAnalyzer.java    # Risk/Reward analysis
│   │   │   │       │   │   └── StatisticsMapper.java      # Entity ↔ DTO mapping
│   │   │   │       │   │
│   │   │   │       │   ├── screenshot/
│   │   │   │       │   │   ├── ScreenshotService.java     # File upload/download service
│   │   │   │       │   │   ├── FileStorageService.java    # File storage abstraction
│   │   │   │       │   │   ├── ImageValidator.java        # Image validation
│   │   │   │       │   │   └── ImageCompressor.java       # Image compression
│   │   │   │       │   │
│   │   │   │       │   ├── user/
│   │   │   │       │   │   ├── UserService.java           # User business logic
│   │   │   │       │   │   ├── UserValidator.java         # User validation rules
│   │   │   │       │   │   └── UserMapper.java            # Entity ↔ DTO mapping
│   │   │   │       │   │
│   │   │   │       │   ├── dashboard/
│   │   │   │       │   │   ├── DashboardService.java      # Dashboard data aggregation
│   │   │   │       │   │   └── DashboardMapper.java       # Dashboard response mapping
│   │   │   │       │   │
│   │   │   │       │   └── export/
│   │   │   │       │       ├── ExportService.java         # Data export service
│   │   │   │       │       ├── CSVExporter.java           # CSV export logic
│   │   │   │       │       └── ExcelExporter.java         # Excel export logic
│   │   │   │       │
│   │   │   │       ├── domain/                            # Domain Layer (Entities)
│   │   │   │       │   ├── entity/
│   │   │   │       │   │   ├── User.java                  # User entity
│   │   │   │       │   │   ├── Trade.java                 # Trade entity
│   │   │   │       │   │   ├── Screenshot.java            # Screenshot entity
│   │   │   │       │   │   ├── TradeStatistics.java       # Statistics entity
│   │   │   │       │   │   ├── JournalEntry.java          # Journal entry entity
│   │   │   │       │   │   └── AuditEntity.java           # Base audit entity
│   │   │   │       │   │
│   │   │   │       │   ├── enums/
│   │   │   │       │   │   ├── UserRole.java              # USER, ADMIN
│   │   │   │       │   │   ├── PositionType.java          # LONG, SHORT
│   │   │   │       │   │   ├── TradeStatus.java           # OPEN, CLOSED, PENDING, CANCELLED
│   │   │   │       │   │   ├── WinLoss.java               # WIN, LOSS, BREAK_EVEN
│   │   │   │       │   │   └── AccountStatus.java         # ACTIVE, INACTIVE, SUSPENDED
│   │   │   │       │   │
│   │   │   │       │   ├── event/
│   │   │   │       │   │   ├── DomainEvent.java           # Base domain event
│   │   │   │       │   │   ├── TradeCreatedEvent.java     # Trade creation event
│   │   │   │       │   │   ├── TradeClosedEvent.java      # Trade closure event
│   │   │   │       │   │   └── TradeDeletedEvent.java     # Trade deletion event
│   │   │   │       │   │
│   │   │   │       │   └── exception/
│   │   │   │       │       ├── BusinessException.java     # Base business exception
│   │   │   │       │       ├── TradeNotFoundException.java # Trade not found
│   │   │   │       │       ├── UserNotFoundException.java  # User not found
│   │   │   │       │       ├── ValidationException.java    # Validation error
│   │   │   │       │       ├── JwtException.java           # JWT errors
│   │   │   │       │       ├── FileUploadException.java    # File upload errors
│   │   │   │       │       └── InsufficientDataException.java # Analysis errors
│   │   │   │       │
│   │   │   │       ├── infrastructure/                    # Infrastructure Layer
│   │   │   │       │   ├── repository/
│   │   │   │       │   │   ├── UserRepository.java        # User data access
│   │   │   │       │   │   ├── TradeRepository.java       # Trade data access
│   │   │   │       │   │   ├── ScreenshotRepository.java  # Screenshot data access
│   │   │   │       │   │   ├── StatisticsRepository.java  # Statistics data access
│   │   │   │       │   │   ├── JournalEntryRepository.java# Journal data access
│   │   │   │       │   │   ├── PerformanceHistoryRepository.java # History data access
│   │   │   │       │   │   └── TradeCustomRepository.java # Custom trade queries
│   │   │   │       │   │
│   │   │   │       │   ├── security/
│   │   │   │       │   │   ├── JwtAuthenticationFilter.java # JWT filter
│   │   │   │       │   │   ├── JwtExceptionHandlingFilter.java # JWT exception filter
│   │   │   │       │   │   ├── CustomAuthenticationEntryPoint.java # 401 handling
│   │   │   │       │   │   └── CustomAccessDeniedHandler.java # 403 handling
│   │   │   │       │   │
│   │   │   │       │   ├── fileStorage/
│   │   │   │       │   │   ├── FileStorageProvider.java   # File storage abstraction
│   │   │   │       │   │   ├── LocalFileStorage.java      # Local filesystem storage
│   │   │   │       │   │   ├── S3FileStorage.java         # AWS S3 storage (future)
│   │   │   │       │   │   └── StorageFactory.java        # Storage provider factory
│   │   │   │       │   │
│   │   │   │       │   ├── persistence/
│   │   │   │       │   │   ├── RepositoryBase.java        # Base repository class
│   │   │   │       │   │   └── TradeJpaRepository.java    # Spring Data JPA interface
│   │   │   │       │   │
│   │   │   │       │   └── messaging/
│   │   │   │       │       ├── EventPublisher.java        # Domain event publishing
│   │   │   │       │       └── EventListener.java         # Domain event listening
│   │   │   │       │
│   │   │   │       ├── common/                            # Cross-Cutting Concerns
│   │   │   │       │   ├── exception/
│   │   │   │       │   │   ├── GlobalExceptionHandler.java # Central exception handling
│   │   │   │       │   │   ├── ErrorResponse.java         # Standard error response
│   │   │   │       │   │   └── ValidationErrorResponse.java # Validation error response
│   │   │   │       │   │
│   │   │   │       │   ├── logging/
│   │   │   │       │   │   ├── LoggingAspect.java         # AOP logging
│   │   │   │       │   │   ├── AuditLogger.java           # Audit logging
│   │   │   │       │   │   └── PerformanceLogger.java     # Performance metrics
│   │   │   │       │   │
│   │   │   │       │   ├── pagination/
│   │   │   │       │   │   ├── PaginationRequest.java     # Pagination DTO
│   │   │   │       │   │   ├── PaginatedResponse.java     # Paginated response
│   │   │   │       │   │   └── PageableUtil.java          # Pagination utilities
│   │   │   │       │   │
│   │   │   │       │   ├── validation/
│   │   │   │       │   │   ├── TradeValidator.java        # Trade validation
│   │   │   │       │   │   ├── UserValidator.java         # User validation
│   │   │   │       │   │   └── CustomAnnotations.java     # Custom validation annotations
│   │   │   │       │   │
│   │   │   │       │   ├── security/
│   │   │   │       │   │   ├── SecurityUtil.java          # Security utilities
│   │   │   │       │   │   ├── PasswordEncoding.java      # Password encryption
│   │   │   │       │   │   └── AuditableEntity.java       # Audit entity base
│   │   │   │       │   │
│   │   │   │       │   └── constants/
│   │   │   │       │       ├── AppConstants.java          # Application constants
│   │   │   │       │       ├── ErrorMessages.java         # Error message constants
│   │   │   │       │       ├── ApiEndpoints.java          # API endpoint paths
│   │   │   │       │       └── ValidationMessages.java    # Validation messages
│   │   │   │       │
│   │   │   │       └── util/
│   │   │   │           ├── DateTimeUtil.java              # Date/time utilities
│   │   │   │           ├── CalculationUtil.java           # Calculation utilities
│   │   │   │           ├── StringUtil.java                # String utilities
│   │   │   │           └── FileUtil.java                  # File utilities
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml                        # Main configuration
│   │   │       ├── application-dev.yml                    # Development config
│   │   │       ├── application-prod.yml                   # Production config
│   │   │       ├── application-test.yml                   # Test config
│   │   │       ├── logback-spring.xml                     # Logging configuration
│   │   │       ├── messages.properties                    # Message bundles
│   │   │       └── data.sql                               # Initial data (if needed)
│   │   │
│   │   └── test/
│   │       ├── java/
│   │       │   └── com/tradingjournal/
│   │       │       ├── presentation/
│   │       │       │   ├── auth/
│   │       │       │   │   ├── AuthControllerTest.java
│   │       │       │   │   └── AuthControllerIntegrationTest.java
│   │       │       │   │
│   │       │       │   ├── trade/
│   │       │       │   │   ├── TradeControllerTest.java
│   │       │       │   │   └── TradeControllerIntegrationTest.java
│   │       │       │   │
│   │       │       │   └── statistics/
│   │       │       │       └── StatisticsControllerTest.java
│   │       │       │
│   │       │       ├── application/
│   │       │       │   ├── auth/
│   │       │       │   │   ├── AuthServiceTest.java
│   │       │       │   │   └── JwtTokenProviderTest.java
│   │       │       │   │
│   │       │       │   ├── trade/
│   │       │       │   │   ├── TradeServiceTest.java
│   │       │       │   │   ├── TradePnLCalculatorTest.java
│   │       │       │   │   └── TradeValidatorTest.java
│   │       │       │   │
│   │       │       │   └── statistics/
│   │       │       │       ├── StatisticsServiceTest.java
│   │       │       │       ├── WinRateCalculatorTest.java
│   │       │       │       └── PnLCalculatorTest.java
│   │       │       │
│   │       │       ├── infrastructure/
│   │       │       │   └── repository/
│   │       │       │       ├── TradeRepositoryTest.java
│   │       │       │       └── UserRepositoryTest.java
│   │       │       │
│   │       │       └── common/
│   │       │           ├── exception/
│   │       │           │   └── GlobalExceptionHandlerTest.java
│   │       │           │
│   │       │           └── security/
│   │       │               └── JwtTokenProviderTest.java
│   │       │
│   │       └── resources/
│   │           ├── application-test.yml
│   │           ├── test-data.sql
│   │           └── test.properties
│   │
│   ├── pom.xml                                            # Maven configuration
│   ├── Dockerfile                                         # Docker image
│   └── README.md                                          # Backend documentation
│
├── frontend/                                              # React SPA
│   ├── src/
│   │   ├── index.js                                       # React entry point
│   │   ├── App.jsx                                        # Root component
│   │   ├── App.css                                        # Global styles
│   │   │
│   │   ├── api/                                           # API Integration
│   │   │   ├── client.js                                  # Axios instance
│   │   │   ├── authApi.js                                 # Auth endpoints
│   │   │   ├── tradeApi.js                                # Trade endpoints
│   │   │   ├── statisticsApi.js                           # Statistics endpoints
│   │   │   ├── screenshotApi.js                           # Screenshot endpoints
│   │   │   └── userApi.js                                 # User endpoints
│   │   │
│   │   ├── pages/                                         # Page Components
│   │   │   ├── Login/
│   │   │   │   ├── Login.jsx
│   │   │   │   ├── Login.css
│   │   │   │   └── useLoginForm.js                        # Custom hook
│   │   │   │
│   │   │   ├── Register/
│   │   │   │   ├── Register.jsx
│   │   │   │   ├── Register.css
│   │   │   │   └── useRegisterForm.js
│   │   │   │
│   │   │   ├── Dashboard/
│   │   │   │   ├── Dashboard.jsx
│   │   │   │   ├── Dashboard.css
│   │   │   │   ├── components/
│   │   │   │   │   ├── WinRateCard.jsx
│   │   │   │   │   ├── PnLCard.jsx
│   │   │   │   │   ├── RiskRewardCard.jsx
│   │   │   │   │   ├── TradeChart.jsx
│   │   │   │   │   ├── StatisticsTable.jsx
│   │   │   │   │   └── PerformanceChart.jsx
│   │   │   │   │
│   │   │   │   └── hooks/
│   │   │   │       └── useDashboardData.js
│   │   │   │
│   │   │   ├── TradeHistory/
│   │   │   │   ├── TradeHistory.jsx
│   │   │   │   ├── TradeHistory.css
│   │   │   │   ├── components/
│   │   │   │   │   ├── TradeTable.jsx
│   │   │   │   │   ├── TradeFilters.jsx
│   │   │   │   │   ├── TradeSearchBar.jsx
│   │   │   │   │   ├── TradeDetailsModal.jsx
│   │   │   │   │   └── DeleteConfirmationModal.jsx
│   │   │   │   │
│   │   │   │   └── hooks/
│   │   │   │       └── useTradeHistory.js
│   │   │   │
│   │   │   ├── AddTrade/
│   │   │   │   ├── AddTrade.jsx
│   │   │   │   ├── AddTrade.css
│   │   │   │   ├── components/
│   │   │   │   │   ├── TradeForm.jsx
│   │   │   │   │   ├── PositionSection.jsx
│   │   │   │   │   ├── PnLCalculator.jsx
│   │   │   │   │   ├── ScreenshotUpload.jsx
│   │   │   │   │   ├── RiskRewardInput.jsx
│   │   │   │   │   └── TradePreview.jsx
│   │   │   │   │
│   │   │   │   └── hooks/
│   │   │   │       └── useAddTrade.js
│   │   │   │
│   │   │   ├── EditTrade/
│   │   │   │   ├── EditTrade.jsx
│   │   │   │   ├── EditTrade.css
│   │   │   │   └── hooks/
│   │   │   │       └── useEditTrade.js
│   │   │   │
│   │   │   ├── UserProfile/
│   │   │   │   ├── UserProfile.jsx
│   │   │   │   ├── UserProfile.css
│   │   │   │   ├── components/
│   │   │   │   │   ├── ProfileForm.jsx
│   │   │   │   │   └── PasswordChangeForm.jsx
│   │   │   │   │
│   │   │   │   └── hooks/
│   │   │   │       └── useUserProfile.js
│   │   │   │
│   │   │   ├── Statistics/
│   │   │   │   ├── Statistics.jsx
│   │   │   │   ├── Statistics.css
│   │   │   │   ├── components/
│   │   │   │   │   ├── WinRateAnalysis.jsx
│   │   │   │   │   ├── PnLAnalysis.jsx
│   │   │   │   │   ├── RiskRewardAnalysis.jsx
│   │   │   │   │   └── PerformanceTrends.jsx
│   │   │   │   │
│   │   │   │   └── hooks/
│   │   │   │       └── useStatistics.js
│   │   │   │
│   │   │   └── NotFound/
│   │   │       ├── NotFound.jsx
│   │   │       └── NotFound.css
│   │   │
│   │   ├── components/                                    # Reusable Components
│   │   │   ├── common/
│   │   │   │   ├── Navbar.jsx
│   │   │   │   ├── Sidebar.jsx
│   │   │   │   ├── Footer.jsx
│   │   │   │   ├── Loading.jsx
│   │   │   │   ├── Toast.jsx
│   │   │   │   └── Modal.jsx
│   │   │   │
│   │   │   ├── form/
│   │   │   │   ├── Input.jsx
│   │   │   │   ├── Select.jsx
│   │   │   │   ├── Checkbox.jsx
│   │   │   │   ├── DatePicker.jsx
│   │   │   │   ├── NumberInput.jsx
│   │   │   │   └── FormError.jsx
│   │   │   │
│   │   │   ├── chart/
│   │   │   │   ├── LineChart.jsx
│   │   │   │   ├── BarChart.jsx
│   │   │   │   ├── PieChart.jsx
│   │   │   │   └── AreaChart.jsx
│   │   │   │
│   │   │   └── table/
│   │   │       ├── DataTable.jsx
│   │   │       ├── Pagination.jsx
│   │   │       └── TableFilter.jsx
│   │   │
│   │   ├── hooks/                                         # Custom React Hooks
│   │   │   ├── useAuth.js                                 # Authentication hook
│   │   │   ├── useApi.js                                  # API calls hook
│   │   │   ├── useLocalStorage.js                         # Local storage hook
│   │   │   ├── useDebounce.js                             # Debounce hook
│   │   │   ├── useFetch.js                                # Fetch hook
│   │   │   └── useNotification.js                         # Toast notification hook
│   │   │
│   │   ├── context/                                       # React Context
│   │   │   ├── AuthContext.jsx
│   │   │   ├── ThemeContext.jsx
│   │   │   ├── NotificationContext.jsx
│   │   │   └── LoadingContext.jsx
│   │   │
│   │   ├── utils/                                         # Utility Functions
│   │   │   ├── dateUtils.js
│   │   │   ├── formatUtils.js
│   │   │   ├── validationUtils.js
│   │   │   ├── storageUtils.js
│   │   │   ├── httpClient.js
│   │   │   └── constants.js
│   │   │
│   │   ├── services/                                      # Business Logic
│   │   │   ├── authService.js
│   │   │   ├── tradeService.js
│   │   │   ├── statisticsService.js
│   │   │   └── userService.js
│   │   │
│   │   ├── styles/                                        # Global Styles
│   │   │   ├── index.css
│   │   │   ├── variables.css
│   │   │   ├── responsive.css
│   │   │   └── themes.css
│   │   │
│   │   └── assets/                                        # Static Assets
│   │       ├── images/
│   │       ├── icons/
│   │       └── fonts/
│   │
│   ├── public/
│   │   ├── index.html
│   │   ├── favicon.ico
│   │   └── robots.txt
│   │
│   ├── package.json
│   ├── package-lock.json
│   ├── .env.example
│   ├── .env.development
│   ├── .env.production
│   ├── webpack.config.js (if not using CRA)
│   ├── Dockerfile
│   ├── .dockerignore
│   └── README.md
│
├── docker-compose.yml                                     # Multi-container setup
├── docker-compose.dev.yml                                 # Development setup
├── docker-compose.prod.yml                                # Production setup
│
├── docs/
│   ├── API_DOCUMENTATION.md                               # API specs
│   ├── DEVELOPMENT_GUIDE.md                               # Dev setup
│   ├── DEPLOYMENT_GUIDE.md                                # Deployment steps
│   ├── DATABASE_SCHEMA.md                                 # DB schema docs
│   ├── ARCHITECTURE.md                                    # Architecture docs
│   ├── SECURITY_GUIDE.md                                  # Security info
│   ├── TESTING_STRATEGY.md                                # Testing approach
│   └── TROUBLESHOOTING.md                                 # Common issues
│
├── scripts/
│   ├── setup-dev.sh                                       # Development setup
│   ├── setup-prod.sh                                      # Production setup
│   ├── db-init.sql                                        # Database initialization
│   ├── db-backup.sh                                       # Database backup
│   ├── health-check.sh                                    # Health check script
│   └── deploy.sh                                          # Deployment script
│
├── .github/
│   └── workflows/
│       ├── ci-backend.yml                                 # Backend CI/CD
│       ├── ci-frontend.yml                                # Frontend CI/CD
│       ├── build-and-push-docker.yml                      # Docker build
│       └── deploy-prod.yml                                # Production deployment
│
├── .gitignore
├── README.md                                              # Project overview
├── CONTRIBUTING.md                                        # Contribution guide
├── LICENSE                                                # License file
└── ROADMAP.md                                             # Development roadmap
```

## 2. Folder Structure Rationale

### Backend Structure Explanation

#### **Presentation Layer** (`presentation/`)
- **Purpose**: REST API endpoints, request/response handling
- **Contents**: Controllers organized by feature (auth, trade, statistics)
- **Responsibility**: HTTP handling, input validation, response serialization
- **No**: Business logic, database access

#### **Application Layer** (`application/`)
- **Purpose**: Core business logic, orchestration between layers
- **Contents**: Services, DTOs, validators, mappers
- **Responsibility**: Business rules, transaction management, DTO transformation
- **No**: HTTP handling, direct database access

#### **Domain Layer** (`domain/`)
- **Purpose**: Core business entities and rules
- **Contents**: JPA entities, enums, domain events, exceptions
- **Responsibility**: Entity structure, domain validation, business rules
- **No**: Database operations, HTTP handling

#### **Infrastructure Layer** (`infrastructure/`)
- **Purpose**: Technical implementation details
- **Contents**: Repositories, security, file storage, persistence
- **Responsibility**: Database access, external integrations, technical infrastructure
- **No**: Business logic, HTTP handling

#### **Common Layer** (`common/`)
- **Purpose**: Cross-cutting concerns used across layers
- **Contents**: Exception handling, logging, validation, security utilities
- **Responsibility**: Shared infrastructure, centralized error handling

### Frontend Structure Explanation

#### **Pages** (`pages/`)
- One folder per page/route
- Contains page component + subcomponents + custom hooks specific to that page
- Example: `TradeHistory/` contains table, filters, modals, and hooks

#### **Components** (`components/`)
- Reusable components shared across pages
- Organized by type: `common/`, `form/`, `chart/`, `table/`
- No page-specific logic

#### **API** (`api/`)
- Centralized API calls
- One file per resource (auth, trade, statistics)
- Axios configuration and interceptors

#### **Context** (`context/`)
- Global state management
- One file per context (Auth, Theme, Notifications)

#### **Services** (`services/`)
- Business logic for frontend
- Data transformation, calculations
- Separate from API calls

## 3. Naming Conventions

### Backend
```
Controllers:      *Controller.java          (TradeController.java)
Services:         *Service.java             (TradeService.java)
Repositories:     *Repository.java          (TradeRepository.java)
DTOs:             *DTO.java or *Request/Response.java
Entities:         *Entity.java or simple name (Trade.java)
Exceptions:       *Exception.java           (TradeNotFoundException.java)
Validators:       *Validator.java           (TradeValidator.java)
Enums:            *Enum.java or simple name (PositionType.java)
Config:           *Config.java              (SecurityConfig.java)
Mappers:          *Mapper.java              (TradeMapper.java)
```

### Frontend
```
Components:       *Component.jsx or *.jsx   (Login.jsx)
Pages:            index.jsx                 (pages/Login/index.jsx)
Hooks:            use*.js                   (useAuth.js)
API clients:      *Api.js                   (tradeApi.js)
Services:         *Service.js               (authService.js)
Context:          *Context.jsx              (AuthContext.jsx)
Utils:            *Utils.js                 (formatUtils.js)
Styles:           *.css or *.module.css
```

## 4. Cross-Layer Dependencies

```
Presentation → Application → Domain
                ↓
         Infrastructure
                
Common utilities (Exception, Logging, Validation) 
  are used by all layers
```

**Rule**: 
- Each layer only imports from layers below it
- Exceptions: Common layer can be imported by all
- DTO transformation happens at layer boundaries

## 5. Feature-Based Foldering Alternative

If preferring feature-based (recommended for larger teams):

```
src/
├── features/
│   ├── auth/
│   │   ├── presentation/
│   │   ├── application/
│   │   ├── domain/
│   │   └── infrastructure/
│   │
│   ├── trade/
│   │   ├── presentation/
│   │   ├── application/
│   │   ├── domain/
│   │   └── infrastructure/
│   │
│   └── statistics/
│       ├── presentation/
│       ├── application/
│       ├── domain/
│       └── infrastructure/
│
└── common/
    ├── exception/
    ├── logging/
    ├── security/
    └── validation/
```

**Advantages**:
- Feature co-location
- Easier to find all related code
- Simpler team assignments
- Better for large projects

**Current recommendation**: Layer-based (cleaner for medium projects)

