@echo off
title Stop Docker Compose Services
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

:: ====== NAVIGATE TO PROJECT DIRECTORY ======
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

:: ====== STOP SERVICES ======
echo Stopping Docker Compose services...
docker-compose -f docker-compose.yaml down
if errorlevel 1 (
    echo Failed to stop Docker Compose services.
    echo Check logs with: docker-compose -f docker-compose.yaml logs
    pause
    exit /b 1
)

:: ====== OPTIONALLY REMOVE VOLUMES ======
set /p REMOVE_VOLUMES=Do you want to remove the database volume (postgres_data)? This will delete all database data. (y/n): 
if /i "%REMOVE_VOLUMES%"=="y" (
    echo Removing volumes (including postgres_data)...
    docker-compose -f docker-compose.yaml down -v
    if errorlevel 1 (
        echo Failed to remove volumes.
        pause
        exit /b 1
    )
    echo Volumes removed successfully.
) else (
    echo Volumes preserved (database data retained).
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
echo Services have been stopped.
echo If volumes were not removed, database data is preserved for the next run.
echo To start services again, run run-docker-compose.bat.
echo.
echo Press any key to keep this terminal open for further commands...
pause >nul
cmd /k