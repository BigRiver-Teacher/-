@echo off
chcp 65001>nul
setlocal
set "ROOT=%~dp0.."

where node >nul 2>nul
if errorlevel 1 (
  echo [ERROR] node not found in PATH.
  echo         Install Node.js 22+ and retry.
  pause & exit /b 1
)

node "%ROOT%\scripts\05-insert-video.mjs" %*
if errorlevel 1 (
  echo.
  echo [FAILED] video insert aborted. See messages above.
  pause & exit /b 1
)
pause
