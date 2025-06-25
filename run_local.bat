@echo off
title Launch Backend & Frontend
color 0A

:: ====== BACKEND ======
echo Starting Backend...
cd backend_spring
if errorlevel 1 (
    echo Failed to navigate to backend_spring directory
    pause
    exit /b 1
)
:: Check if pom.xml exists to confirm it's a Maven project
if not exist pom.xml (
    echo No pom.xml found in backend_spring directory
    pause
    exit /b 1
)
:: Check if mvnw exists to confirm Maven Wrapper is present
if not exist mvnw (
    echo No mvnw found in backend_spring directory
    pause
    exit /b 1
)
start cmd /k "mvnw spring-boot:run" || (
    echo Failed to start Backend
    pause
    exit /b 1
)
cd ..
if errorlevel 1 (
    echo Failed to return to parent directory
    pause
    exit /b 1
)

:: ====== FRONTEND ======
echo Starting Frontend...
cd frontend_angular19
if errorlevel 1 (
    echo Failed to navigate to frontend_angular19 directory
    pause
    exit /b 1
)
:: Check if package.json exists to confirm it's an Angular project
if not exist package.json (
    echo No package.json found in frontend_angular19 directory
    pause
    exit /b 1
)
start cmd /k "ng serve" || (
    echo Failed to start Frontend
    pause
    exit /b 1
)
cd ..
if errorlevel 1 (
    echo Failed to return to parent directory
    pause
    exit /b 1
)

echo Both Backend and Frontend should be running.
pause