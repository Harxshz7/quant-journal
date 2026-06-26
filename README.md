# Trading Journal Application

A production-ready Trading Journal application for traders to track, analyze, and improve their trading performance.

## Project Overview

**Status**: 📋 Architecture & Planning Phase (Ready for Implementation)

This is a comprehensive trading journal platform that enables traders to:
- ✅ Record and manage trade entries
- ✅ Track win rates and P&L metrics
- ✅ Analyze risk/reward ratios
- ✅ Visualize trading performance
- ✅ Upload and organize screenshots

## Tech Stack

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Authentication**: JWT

### Frontend
- **Framework**: React 18+
- **HTTP Client**: Axios
- **Routing**: React Router v6

### DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose (Kubernetes ready)
- **CI/CD**: GitHub Actions

## Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
Presentation Layer (Controllers/REST APIs)
    ↓
Application Layer (Services/Business Logic/DTOs)
    ↓
Domain Layer (Entities/Business Rules)
    ↓
Infrastructure Layer (Repositories/Data Access)
    ↓
Cross-Cutting Concerns (Logging, Exception Handling, Security)
```

**See** [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture documentation.

## Project Structure

```
TradingJournalApp/
├── backend/                    # Spring Boot Application
│   ├── src/
│   │   ├── main/java/         # Source code (organized by layer)
│   │   ├── test/java/         # Test suite
│   │   └── resources/         # Configuration files
│   ├── pom.xml               # Maven dependencies
│   └── Dockerfile            # Docker image
│
├── frontend/                  # React Application
│   ├── src/
│   │   ├── pages/            # Page components
│   │   ├── components/       # Reusable components
│   │   ├── api/              # API integration
│   │   ├── hooks/            # Custom React hooks
│   │   └── utils/            # Utility functions
│   ├── package.json
│   └── Dockerfile
│
├── docs/                      # Documentation
├── scripts/                   # Utility scripts
├── docker-compose.yml         # Local development environment
├── README.md                  # This file
├── ARCHITECTURE.md            # Architecture documentation
├── DATABASE_SCHEMA.md         # Database design
├── FOLDER_STRUCTURE.md        # Directory structure rationale
├── DEVELOPMENT_ROADMAP.md     # Implementation roadmap
├── API_SPECIFICATION.md       # Complete API documentation
└── PROJECT_SUMMARY.md         # Project overview & checklist
```

**See** [FOLDER_STRUCTURE.md](FOLDER_STRUCTURE.md) for detailed structure explanation.

## Database Schema

The application uses PostgreSQL with the following core tables:

- **Users** - User accounts and authentication
- **Trades** - Trade entries with entry/exit data
- **Screenshots** - Trade-related images
- **TradeStatistics** - Aggregated user metrics
- **JournalEntries** - Trade reflections and notes
- **PerformanceHistory** - Historical performance snapshots

**See** [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) for complete database design.

## API Documentation

REST APIs with the following resource endpoints:

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `POST /api/v1/auth/refresh-token` - Refresh JWT token

### Trades
- `POST /api/v1/trades` - Create trade
- `GET /api/v1/trades` - List trades (paginated)
- `GET /api/v1/trades/{id}` - Get trade details
- `PUT /api/v1/trades/{id}` - Update trade
- `DELETE /api/v1/trades/{id}` - Delete trade

### Statistics
- `GET /api/v1/dashboard/overview` - Dashboard summary
- `GET /api/v1/statistics/summary` - Detailed statistics
- `GET /api/v1/statistics/win-rate` - Win rate analysis
- `GET /api/v1/statistics/pnl` - P&L analysis
- `GET /api/v1/statistics/risk-reward` - Risk/reward analysis

### Files
- `POST /api/v1/trades/{tradeId}/screenshots` - Upload screenshot
- `GET /api/v1/trades/{tradeId}/screenshots` - List screenshots
- `DELETE /api/v1/screenshots/{id}` - Delete screenshot

**See** [API_SPECIFICATION.md](API_SPECIFICATION.md) for complete API documentation.

## Development Roadmap

The project is divided into 7 phases spanning 20 weeks:

| Phase | Duration | Focus |
|-------|----------|-------|
| 1 | Weeks 1-4 | Foundation & Infrastructure |
| 2 | Weeks 5-7 | User Management & Auth |
| 3 | Weeks 8-11 | Trade Management |
| 4 | Weeks 12-15 | Statistics & Analytics |
| 5 | Week 16 | File Upload & Screenshots |
| 6 | Weeks 17-18 | Testing & QA |
| 7 | Weeks 19-20 | Deployment & Production |

**See** [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md) for detailed phase breakdown.

## Getting Started

### Prerequisites
- Java 21 JDK
- Node.js 18+ LTS
- Docker & Docker Compose
- Maven 3.8+
- PostgreSQL client (optional)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/TradingJournalApp.git
   cd TradingJournalApp
   ```

2. **Start PostgreSQL (Docker)**
   ```bash
   docker-compose up -d postgres
   ```

3. **Backend Setup**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```
   Backend runs on: `http://localhost:8080`

4. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Frontend runs on: `http://localhost:3000`

5. **Access the Application**
   - Frontend: http://localhost:3000
   - API Docs: http://localhost:8080/swagger-ui.html

### Docker Compose (All Services)
```bash
docker-compose up       # Start all services
docker-compose down     # Stop all services
docker-compose logs -f  # View logs
```

## Configuration

### Backend Configuration
- Development: `backend/src/main/resources/application-dev.yml`
- Production: `backend/src/main/resources/application-prod.yml`
- Test: `backend/src/main/resources/application-test.yml`

### Frontend Configuration
- Development: `frontend/.env.development`
- Production: `frontend/.env.production`

## Testing

### Backend Tests
```bash
cd backend
mvn test                    # Run all tests
mvn test -Dtest=AuthServiceTest  # Run specific test
mvn clean jacoco:report    # Generate coverage report
```

### Frontend Tests
```bash
cd frontend
npm test                    # Run all tests
npm test -- --coverage     # Generate coverage report
```

## Building for Production

### Backend
```bash
cd backend
mvn clean package -DskipTests
# JAR file: target/tradingjournal-app.jar
```

### Frontend
```bash
cd frontend
npm run build
# Production build: build/
```

### Docker Images
```bash
# Build both images
docker build -t tradingjournal-backend:latest ./backend
docker build -t tradingjournal-frontend:latest ./frontend

# Or use docker-compose
docker-compose build
```

## Security Features

✅ **Authentication**: JWT tokens with 1-hour expiration  
✅ **Authorization**: Role-based access control (USER, ADMIN)  
✅ **Password Security**: BCrypt hashing (min 10 rounds)  
✅ **API Security**: CORS whitelist, CSRF protection  
✅ **Data Protection**: SQL injection prevention, input validation  
✅ **Audit Trail**: User action tracking and logging  
✅ **File Upload**: Type validation, size limits (10MB max)  

## Performance Optimization

- **Database Indexes**: Optimized for common queries
- **Denormalization**: TradeStatistics table for fast dashboard
- **Pagination**: Large result set handling
- **Caching**: Ready for Redis integration
- **API Rate Limiting**: 1000 requests/hour per user

## Deployment

The application is containerized and ready for deployment to:
- ✅ Docker/Docker Compose
- ✅ Kubernetes (manifests provided)
- ✅ Cloud platforms (AWS, GCP, Azure)

**See** `docs/DEPLOYMENT_GUIDE.md` for detailed deployment instructions.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture & design patterns
- [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) - Database schema & relationships
- [FOLDER_STRUCTURE.md](FOLDER_STRUCTURE.md) - Directory structure explanation
- [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md) - Implementation phases
- [API_SPECIFICATION.md](API_SPECIFICATION.md) - Complete API reference
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - Overview & checklist

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

**See** CONTRIBUTING.md for detailed guidelines.

## Code Standards

- **Backend**: Google Java Style Guide
- **Frontend**: AirBnB JavaScript Style Guide
- **Naming**: Clear, descriptive names
- **Comments**: Document "why", not "what"
- **Testing**: Aim for 80%+ coverage

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080
kill -9 <PID>
```

### Database Connection Error
```bash
# Check PostgreSQL container
docker-compose ps
docker-compose logs postgres

# Recreate database
docker-compose down
docker-compose up -d postgres
```

### Frontend Build Issues
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
npm start
```

## Monitoring & Logging

- **Backend Logging**: SLF4J + Logback (configurable levels)
- **Frontend Logging**: Browser console + Sentry (optional)
- **Performance Metrics**: Spring Boot Actuator (`/actuator/metrics`)
- **API Documentation**: Swagger UI (`/swagger-ui.html`)

## Future Enhancements

- Real-time market data integration
- Machine learning for trade prediction
- Social trading features
- Mobile app (Flutter/React Native)
- Backtesting engine
- Multi-tenant SaaS support
- Slack/Email notifications

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues, questions, or suggestions:
1. Check the documentation in `/docs`
2. Search existing GitHub issues
3. Create a new issue with detailed information
4. Contact: support@tradingjournal.com

## Authors

- Your Name - Architecture & Design
- Team Members - Development

## Acknowledgments

- Clean Architecture principles (Uncle Bob)
- Spring Boot community
- React community
- PostgreSQL documentation

---

**Last Updated**: 2026-06-26  
**Version**: 1.0 (Planning Phase)  
**Status**: 📋 Ready for Implementation

