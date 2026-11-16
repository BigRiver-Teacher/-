@echo off
chcp 65001>nul
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%\backend"

where java >nul 2>nul
if errorlevel 1 ( echo [ERROR] java not found. Install JDK 17/21 and add to PATH. & pause & exit /b 1 )
where mvn >nul 2>nul
if errorlevel 1 ( echo [ERROR] mvn not found. Install Maven 3.6+ and add to PATH. & pause & exit /b 1 )

if not exist "target\javapratice-0.0.1-SNAPSHOT.jar" (
  echo [INFO] building backend (mvn -DskipTests package) ...
  call mvn -DskipTests package
  if errorlevel 1 ( echo [FAILED] maven build & pause & exit /b 1 )
)

echo [INFO] starting backend at http://localhost:8080
echo        health check: http://127.0.0.1:8080/api/stats/overview
echo        api docs    : http://127.0.0.1:8080/doc.html
java -jar "target\javapratice-0.0.1-SNAPSHOT.jar"
pause
