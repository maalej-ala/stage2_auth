@echo off
title Launch Docker Compose Services
color 0A

:: ====== CHECK PREREQUISITES ======
echo Checking for Docker and Docker Compose...
docker --version >nul 2>&1
if errorlevel 1 (
    echo Docker is not installed or not running. Please install Docker Desktop and ensure it is running.
    pause
    exit /b 1
)
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo Docker Compose is not installed. Please install Docker Desktop, which includes Docker Compose.
    pause
    exit /b 1
)

:: ====== DISPLAY AND NAVIGATE TO PROJECT DIRECTORY ======
echo Current directory: %CD%
echo Navigating to project directory...
cd /d "C:\Users\Lenovo\Desktop\stge2\stage"
if errorlevel 1 (
    echo Failed to navigate to C:\Users\Lenovo\Desktop\stge2\stage
    pause
    exit /b 1
)
echo Current directory after navigation: %CD%

:: ====== CHECK DOCKER-COMPOSE.YAML ======
echo Checking for docker-compose.yaml...
if not exist docker-compose.yaml (
    echo docker-compose.yaml not found in C:\Users\Lenovo\Desktop\stge2\stage
    echo Please ensure docker-compose.yaml exists in this directory.
    pause
    exit /b 1
)

:: ====== CHECK PROJECT DIRECTORIES AND DOCKERFILES ======
echo Checking for backend_spring and frontend_angular19 directories...
if not exist backend_spring (
    echo backend_spring directory not found in C:\Users\Lenovo\Desktop\stge2\stage
    pause
    exit /b 1
)
if not exist backend_spring\Dockerfile (
    echo Dockerfile not found in backend_spring directory
    pause
    exit /b 1
)
if not exist frontend_angular19 (
    echo frontend_angular19 directory not found in C:\Users\Lenovo\Desktop\stge2\stage
    pause
    exit /b 1
)
if not exist frontend_angular19\Dockerfile (
    echo Dockerfile not found in frontend_angular19 directory
    pause
    exit /b 1
)

:: ====== BUILD AND START SERVICES ======
echo Building and starting Docker Compose services...
docker-compose -f docker-compose.yaml up --build -d
if errorlevel 1 (
    echo Failed to build or start Docker Compose services.
    echo Check Docker logs with: docker-compose -f docker-compose.yaml logs
    pause
    exit /b 1
)

:: ====== DISPLAY SERVICE STATUS ======
echo Checking service status...
docker-compose -f docker-compose.yaml ps
if errorlevel 1 (
    echo Failed to display service status.
    pause
    exit /b 1
)

echo.
echo Services should be running:
echo - Frontend: http://localhost:4200
echo - Backend: http://localhost:8080
echo - Database: localhost:5432 (PostgreSQL, user: postgres, password: ala112003, database: mydb)
echo.
echo To view logs: docker-compose -f docker-compose.yaml logs
echo To stop services: docker-compose -f docker-compose.yaml down
echo To stop and remove volumes: docker-compose -f docker-compose.yaml down -v
pause