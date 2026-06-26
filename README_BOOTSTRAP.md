# Bootstrap Instructions

## Backend

1. Install Java 21 JDK
2. Install Maven
3. From repository root:

```bash
cd backend
mvn clean package
```

4. Start the application:

```bash
cd backend
mvn spring-boot:run
```

## Frontend

1. Install Node.js 18+
2. From repository root:

```bash
cd frontend
npm install
npm start
```

## Docker Compose

```bash
docker-compose up -d
```

## Notes
- Backend configuration uses `application-dev.yml`
- PostgreSQL defaults are defined in `docker-compose.yml`
- Frontend API URL is configured via `.env.example`
