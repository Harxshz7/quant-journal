@echo off
setlocal EnableDelayedExpansion

title Quant Journal - Dev Server

:: ============================================================
::  QUANT JOURNAL - Development Startup Script
::  Starts: Backend (Maven) -> Frontend (npm)
::  Assumes PostgreSQL is already running locally.
:: ============================================================

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
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Java is not installed or not in PATH.
    echo          Please install Java 21 JDK: https://adoptium.net/
    pause
    exit /b 1
)

set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "%~dp0backend\apache-maven-3.9.6\bin\mvn.cmd" (
        set "MVN_CMD=%~dp0backend\apache-maven-3.9.6\bin\mvn.cmd"
        echo  [OK] Using bundled Maven.
    ) else (
        echo  [ERROR] Maven (mvn) is not installed or not in PATH.
        echo          Please install Maven: https://maven.apache.org/download.cgi
        pause
        exit /b 1
    )
)

where node >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Node.js is not installed or not in PATH.
    echo          Please install Node.js: https://nodejs.org/
    pause
    exit /b 1
)

where npm >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] npm is not installed or not in PATH.
    echo          Please install Node.js: https://nodejs.org/
    pause
    exit /b 1
)

echo  [OK] All prerequisites found.
echo.

:: -----------------------------------------------------------
:: STEP 2 -- Start Spring Boot Backend and React Frontend
:: -----------------------------------------------------------
echo [2/2] Starting services...
echo  [..] Opening backend in a new window (mvn spring-boot:run)

start "Quant Journal - Backend" cmd /k "cd /d "%~dp0backend" && echo Starting Spring Boot... && "%MVN_CMD%" spring-boot:run"

echo  [OK] Backend window launched. Wait for "Started Application" message there.
echo.

echo  [..] Opening frontend in a new window (npm install && npm start)

start "Quant Journal - Frontend" cmd /k "cd /d "%~dp0frontend" && echo Installing dependencies and starting webpack dev server... && npm install && npm start"

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
echo.
echo  ============================================================
echo.
pause
