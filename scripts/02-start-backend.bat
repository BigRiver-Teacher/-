@echo off
chcp 65001>nul
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%\backend"

where java >nul 2>nul
if errorlevel 1 ( echo [ERROR] java not found. Install JDK 17/21 and add to PATH. & pause & exit /b 1 )
where mvn >nul 2>nul
if errorlevel 1 ( echo [ERROR] mvn not found. Install Maven 3.6+ and add to PATH. & pause & exit /b 1 )

rem ===== 数据库密码：本地演示默认 123456，可用环境变量覆盖 =====
if not defined DB_PASSWORD set "DB_PASSWORD=123456"
if not defined DB_USERNAME set "DB_USERNAME=root"

rem ===== JWT 签名密钥：绝不写入仓库 =====
rem 优先级：环境变量 JWT_SECRET > 本地密钥文件 .jwt-secret.local > 自动生成并持久化
if not defined JWT_SECRET (
  if exist ".jwt-secret.local" (
    set /p JWT_SECRET=<".jwt-secret.local"
  ) else (
    echo [INFO] 未设置 JWT_SECRET 且无本地密钥文件，正在生成随机密钥...
    for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command "([guid]::NewGuid().ToString()+[guid]::NewGuid().ToString())"`) do set "JWT_SECRET=%%i"
    >".jwt-secret.local" echo %JWT_SECRET%
    echo [INFO] 已生成并保存到 backend\.jwt-secret.local（该文件已被 .gitignore 排除，请勿提交）
  )
)
if not defined JWT_SECRET ( echo [ERROR] JWT_SECRET 生成失败，请手动设置环境变量后重试。 & pause & exit /b 1 )

if not exist "target\javapratice-0.0.1-SNAPSHOT.jar" (
  echo [INFO] building backend (mvn -DskipTests package) ...
  call mvn -DskipTests package
  if errorlevel 1 ( echo [FAILED] maven build & pause & exit /b 1 )
)

echo [INFO] starting backend at http://localhost:8080
echo        health check: http://127.0.0.1:8080/api/stats/overview  （需登录态，未登录返回 401 属正常）
echo        api docs    : http://127.0.0.1:8080/doc.html
java -jar "target\javapratice-0.0.1-SNAPSHOT.jar"
pause
