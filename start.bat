@echo off
REM Task Manager - Quick Start Script for Windows

echo ==========================================
echo Task Manager - Full Stack Application
echo ==========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo X Docker is not installed. Please install Docker Desktop first.
    echo Visit: https://docs.docker.com/desktop/install/windows-install/
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo X Docker Compose is not installed. Please install Docker Desktop with Compose.
    pause
    exit /b 1
)

echo + Docker and Docker Compose are installed
echo.

REM Check if Docker daemon is running
docker info >nul 2>&1
if errorlevel 1 (
    echo X Docker daemon is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

echo + Docker daemon is running
echo.

REM Create frontend .env if it doesn't exist
if not exist "frontend\.env" (
    echo Creating frontend\.env from .env.example...
    copy frontend\.env.example frontend\.env >nul
)

REM Stop any existing containers
echo Stopping any existing containers...
docker-compose down

echo.
echo Starting all services...
echo    - PostgreSQL Database
echo    - Spring Boot Backend
echo    - React Frontend
echo.

REM Build and start all services
docker-compose up -d --build

REM Wait for services to be healthy
echo.
echo Waiting for services to start...
timeout /t 5 /nobreak >nul

REM Check service status
echo.
echo Service Status:
docker-compose ps

echo.
echo ==========================================
echo Application is starting!
echo ==========================================
echo.
echo Please wait 30-60 seconds for all services to be fully ready.
echo.
echo Access the application at:
echo   Frontend:  http://localhost:3000
echo   Backend:   http://localhost:8080/api/v1
echo   Swagger:   http://localhost:8080/api/v1/swagger-ui.html
echo   Database:  localhost:5432 (postgres/postgres)
echo.
echo To view logs:
echo   docker-compose logs -f
echo.
echo To stop all services:
echo   docker-compose down
echo.
echo ==========================================
echo.

REM Ask to view logs
set /p viewlogs="Do you want to view logs? (y/n): "
if /i "%viewlogs%"=="y" (
    docker-compose logs -f
)

pause
