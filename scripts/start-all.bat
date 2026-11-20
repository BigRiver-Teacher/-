@echo off
chcp 65001>nul
setlocal
set "ROOT=%~dp0.."

echo ============================================================
echo  zhaoxi-huxing launcher
echo  Order: MySQL(init) -^> backend 8080 -^> frontend 5173 -^> algorithm 8001
echo  Run scripts\01-init-db.bat first if the database is not ready.
echo ============================================================

start "zhaoxi-backend" cmd /k "%ROOT%\scripts\02-start-backend.bat"
timeout /t 6 /nobreak >nul
start "zhaoxi-frontend" cmd /k "%ROOT%\scripts\03-start-frontend.bat"
timeout /t 6 /nobreak >nul
start "zhaoxi-algorithm" cmd /k "%ROOT%\scripts\04-start-algorithm.bat"

echo Launched 3 windows (backend / frontend / algorithm).
echo NOTE: DB/JWT credentials are injected by 02-start-backend.bat (JWT_SECRET auto-generated
echo       into backend\.jwt-secret.local on first run; that file is gitignored).
echo Algorithm runs in degraded mode unless algorithm\models is complete.
pause
