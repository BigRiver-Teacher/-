@echo off
chcp 65001>nul
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp099-verify.ps1"
pause
