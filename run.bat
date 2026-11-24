@echo off
setlocal enabledelayedexpansion
title API Hub - Launcher

echo =====================================================================
echo           API HUB - CENTRALIZED LIFECYCLE MANAGEMENT PLATFORM        
echo =====================================================================
echo.

:: 1. Verify Node.js
where node >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\nodejs\node.exe" (
        set "PATH=C:\Program Files\nodejs;!PATH!"
        echo [INFO] Detected Node.js at C:\Program Files\nodejs
    ) else (
        echo [WARN] Node.js was not found in standard paths. Frontend might fail if Node is missing.
    )
)

:: 2. Verify Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [WARN] Java not found in PATH. Ensure JDK 21+ or Java 25 is installed.
)

set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%backend"
set "FRONTEND_DIR=%ROOT_DIR%frontend"

echo [1/3] Launching Spring Boot Backend on http://localhost:8080 ...
start "API Hub - Backend (Spring Boot)" cmd /k "cd /d "%BACKEND_DIR%" && mvnw.cmd spring-boot:run"

echo [2/3] Launching Vite React Frontend on http://localhost:3000 ...
start "API Hub - Frontend (Vite React)" cmd /k "set "PATH=C:\Program Files\nodejs;!PATH!" && cd /d "%FRONTEND_DIR%" && npm run dev"

echo [3/3] Waiting for servers to initialize...
timeout /t 5 /nobreak >nul

echo.
echo =====================================================================
echo  Services started successfully!
echo.
echo  - Frontend UI : http://localhost:3000
echo  - Backend API : http://localhost:8080/api/v1
echo  - Health Check: http://localhost:8080/api/v1/health
echo.
echo  Default Credentials:
echo  - Email   : admin@apihub.local
echo  - Password: Admin@123456
echo =====================================================================
echo.

:: Automatically open user browser
start http://localhost:3000

pause
