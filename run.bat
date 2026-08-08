@echo off
setlocal EnableDelayedExpansion

title Quant Journal - Dev Server

:: ============================================================
::  QUANT JOURNAL - Development Startup Script
::  Starts: PostgreSQL (Docker) -> Backend (Maven) -> Frontend (npm)
:: ============================================================

echo.
echo  ============================================================
echo   QUANT JOURNAL ^| Development Environment
echo  ============================================================
echo.

:: -----------------------------------------------------------
:: STEP 1 -- Check prerequisites
:: -----------------------------------------------------------
echo [1/3] Checking prerequisites...

where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Docker is not installed or not in PATH.
    echo          Please install Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Maven (mvn) is not installed or not in PATH.
    echo          Please install Maven: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

where npm >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Node.js / npm is not installed or not in PATH.
    echo          Please install Node.js: https://nodejs.org/
    pause
    exit /b 1
)

echo  [OK] All prerequisites found.
echo.

:: -----------------------------------------------------------
:: STEP 2 -- Start PostgreSQL via Docker Compose
:: -----------------------------------------------------------
echo [2/3] Starting PostgreSQL database (Docker)...

docker compose up -d postgres
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Failed to start PostgreSQL container.
    echo          Make sure Docker Desktop is running and try again.
    pause
    exit /b 1
)

echo  [OK] PostgreSQL container started.
echo  [..] Waiting for database to be ready...
timeout /t 5 /nobreak >nul

:: Poll until postgres is healthy (max ~36s)
set /a RETRIES=0
:WAIT_DB
docker compose exec -T postgres pg_isready -U trading_user >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    set /a RETRIES+=1
    if !RETRIES! GEQ 12 (
        echo  [WARN] Database health check timed out. Proceeding anyway...
        goto DB_READY
    )
    timeout /t 3 /nobreak >nul
    goto WAIT_DB
)
:DB_READY
echo  [OK] PostgreSQL is ready.
echo.

:: -----------------------------------------------------------
:: STEP 3 -- Start Spring Boot Backend
:: -----------------------------------------------------------
echo [3/3] Starting Spring Boot backend...
echo  [..] Opening backend in a new window (mvn spring-boot:run)

start "Quant Journal - Backend" cmd /k "cd /d "%~dp0backend" && echo Starting Spring Boot... && mvn spring-boot:run"

echo  [OK] Backend window launched. Wait for "Started Application" message there.
echo.

:: -----------------------------------------------------------
:: STEP 4 -- Install frontend deps (if needed) and start React
:: -----------------------------------------------------------
echo [+]  Starting React frontend...

:: Install node_modules only if missing
if not exist "%~dp0frontend\node_modules" (
    echo  [..] node_modules not found. Running npm install first...
    start "Quant Journal - npm install" /wait cmd /c "cd /d "%~dp0frontend" && npm install"
    echo  [OK] Dependencies installed.
)

start "Quant Journal - Frontend" cmd /k "cd /d "%~dp0frontend" && echo Starting webpack dev server... && npm start"

echo  [OK] Frontend window launched.
echo.

:: -----------------------------------------------------------
:: Summary
:: -----------------------------------------------------------
echo  ============================================================
echo   All services are starting up!
echo  ============================================================
echo.
echo   Service        URL
echo   -------------- -------------------------------------------
echo   React App      http://localhost:3000
echo   Spring Boot    http://localhost:8080
echo   Swagger        http://localhost:8080/swagger-ui.html
echo   PostgreSQL     localhost:5432  (DB: trading_journal)
echo.
echo   To STOP all services:
echo     - Close the Backend and Frontend windows
echo     - Run:  docker compose down
echo.
echo  ============================================================
echo.
pause