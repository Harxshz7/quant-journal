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
:: STEP 2 -- Start Backend and Frontend
:: -----------------------------------------------------------
echo [2/2] Starting services...
echo.

echo  [..] Opening backend...

start "Quant Journal - Backend" cmd /k "cd /d ""%~dp0backend"" && echo Starting Spring Boot... && ""%MVN_CMD%"" spring-boot:run"

echo  [OK] Backend window launched.
echo.

echo  [..] Opening frontend...

start "Quant Journal - Frontend" cmd /k "cd /d ""%~dp0frontend"" && echo Installing dependencies... && npm install && npm start"

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