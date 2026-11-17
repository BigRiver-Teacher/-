@echo off
chcp 65001>nul
setlocal
set "ROOT=%~dp0.."
set "SQL1=%ROOT%\backend\src\main\resources\init.sql"
set "SQL2=%ROOT%\backend\src\main\resources\seed_data.sql"

if not exist "%SQL1%" (
  echo [ERROR] init.sql not found: %SQL1%
  pause & exit /b 1
)
where mysql >nul 2>nul
if errorlevel 1 (
  echo [ERROR] mysql not found in PATH.
  echo         Install MySQL 8 and add its bin folder to PATH, e.g.
  echo         set PATH=%%PATH%%;C:\Program Files\MySQL\MySQL Server 8.0\bin
  pause & exit /b 1
)

rem 数据库账号密码：优先使用环境变量 DB_USER / DB_PASSWORD，未设置时用本地演示默认值
if "%DB_USER%"=="" set "DB_USER=root"
if "%DB_PASSWORD%"=="" set "DB_PASSWORD=change-me"

echo [INFO] DB user: %DB_USER%  (override with DB_USER / DB_PASSWORD env vars)
mysql -u%DB_USER% -p%DB_PASSWORD% --default-character-set=utf8mb4 -e "source %SQL1%"
if errorlevel 1 ( echo [FAILED] running init.sql & pause & exit /b 1 )

mysql -u%DB_USER% -p%DB_PASSWORD% --default-character-set=utf8mb4 -e "source %SQL2%"
if errorlevel 1 ( echo [FAILED] running seed_data.sql & pause & exit /b 1 )

echo [OK] database elderly_care initialized with demo data.
pause
