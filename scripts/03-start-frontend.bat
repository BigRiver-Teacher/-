@echo off
chcp 65001>nul
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%\frontend"

where npm >nul 2>nul
if errorlevel 1 ( echo [ERROR] npm not found. Install Node.js 22+ and add to PATH. & pause & exit /b 1 )

if not exist "node_modules" (
  echo [INFO] installing frontend dependencies (first run) ...
  call npm install
  if errorlevel 1 ( echo [FAILED] npm install & pause & exit /b 1 )
)

echo [INFO] starting Vite dev server at http://localhost:5173
echo        /api is proxied to http://localhost:8080 (see vite.config.ts)
call npm run dev
pause
