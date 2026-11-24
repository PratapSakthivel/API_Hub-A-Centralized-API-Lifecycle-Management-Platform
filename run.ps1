# API Hub - PowerShell Launcher
$ErrorActionPreference = "Continue"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "       API HUB - CENTRALIZED LIFECYCLE MANAGEMENT PLATFORM           " -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $rootDir "backend"
$frontendDir = Join-Path $rootDir "frontend"

# Ensure Node is in PATH
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\nodejs") {
        $env:Path = "C:\Program Files\nodejs;" + $env:Path
        Write-Host "[INFO] Added C:\Program Files\nodejs to Path." -ForegroundColor Green
    }
}

Write-Host "[1/3] Launching Spring Boot Backend on http://localhost:8080 ..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k cd /d `"$backendDir`" && mvnw.cmd spring-boot:run" -WindowStyle Normal

Write-Host "[2/3] Launching Vite React Frontend on http://localhost:3000 ..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k set `"PATH=C:\Program Files\nodejs;!PATH!`" && cd /d `"$frontendDir`" && npm run dev" -WindowStyle Normal

Write-Host "[3/3] Waiting for services to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 4

Write-Host ""
Write-Host "=====================================================================" -ForegroundColor Green
Write-Host " Services started in background windows!" -ForegroundColor Green
Write-Host "  - Frontend UI : http://localhost:3000" -ForegroundColor White
Write-Host "  - Backend API : http://localhost:8080/api/v1" -ForegroundColor White
Write-Host "  - Health Check: http://localhost:8080/api/v1/health" -ForegroundColor White
Write-Host ""
Write-Host " Default Credentials:" -ForegroundColor White
Write-Host "  - Email   : admin@apihub.local" -ForegroundColor White
Write-Host "  - Password: Admin@123456" -ForegroundColor White
Write-Host "=====================================================================" -ForegroundColor Green
Write-Host ""

Start-Process "http://localhost:3000"
