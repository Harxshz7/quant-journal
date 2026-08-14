@echo off
setlocal EnableDelayedExpansion

title Quant Journal - Dev Server

echo.
echo  ============================================================
echo   QUANT JOURNAL ^| Development Environment
echo  ============================================================
echo.

:: -----------------------------------------------------------
:: STEP 1 -- Check prerequisites
:: -----------------------------------------------------------
echo [1/2] Checking prerequisites...

where java >nul 2>&1
if errorlevel 1 (
    echo  [ERROR] Java is not installed or not in PATH.
    pause
    exit /b 1
)

echo  [OK] Java found.

set "MVN_CMD=mvn"
set "MAVEN_REPO_LOCAL=%~dp0.m2\repository"

where mvn >nul 2>&1
if errorlevel 1 (
    if exist "%~dp0backend\apache-maven-3.9.6\bin\mvn.cmd" (
        set "MVN_CMD=%~dp0backend\apache-maven-3.9.6\bin\mvn.cmd"
        echo  [OK] Using bundled Maven.
    ) else (
        echo  [ERROR] Maven is not installed or not in PATH.
        echo          Install Maven and add it to PATH.
        pause
        exit /b 1
    )
)

echo  [OK] Maven found.

where node >nul 2>&1
if errorlevel 1 (
    echo  [ERROR] Node.js is not installed or not in PATH.
    pause
    exit /b 1
)

echo  [OK] Node.js found.

where npm >nul 2>&1
if errorlevel 1 (
    echo  [ERROR] npm is not installed or not in PATH.
    pause
    exit /b 1
)

echo  [OK] npm found.
echo.
echo  [OK] All prerequisites found.
echo.

:: -----------------------------------------------------------
:: STEP 2 -- Kill existing Java processes
:: -----------------------------------------------------------
echo [2/4] Cleaning up existing processes...

taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 /nobreak >nul 2>&1

echo  [OK] Existing Java processes terminated.
echo.

:: -----------------------------------------------------------
:: STEP 3 -- Build Backend
:: -----------------------------------------------------------
echo [3/4] Building backend...
echo.

call "%MVN_CMD%" -Dmaven.repo.local=%MAVEN_REPO_LOCAL% -f "%~dp0backend\pom.xml" clean package -DskipTests

if errorlevel 1 (
    echo  [ERROR] Backend build failed.
    pause
    exit /b 1
)

echo  [OK] Backend built successfully.
echo.

:: -----------------------------------------------------------
:: STEP 4 -- Start Backend and Frontend
:: -----------------------------------------------------------
echo [4/4] Starting services...
echo.

start "Quant Journal - Backend" /D "%~dp0" cmd /k "echo Starting Spring Boot... && java -jar ""%~dp0backend\target\trading-journal-backend-0.1.0-SNAPSHOT.jar"""

echo  [OK] Backend window launched.
echo.

echo  [..] Opening frontend...

start "Quant Journal - Frontend" /D "%~dp0frontend" cmd /k "echo Installing dependencies... && npm install && npm start"

echo  [OK] Frontend window launched.
echo.

echo  ============================================================
echo   All services are starting up!
echo  ============================================================
echo.
echo   React App      http://localhost:3000
echo   Spring Boot    http://localhost:8080
echo   PostgreSQL     localhost:5432
echo.
echo  ============================================================
echo.

pause
