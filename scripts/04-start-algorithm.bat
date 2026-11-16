@echo off
chcp 65001>nul
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%\algorithm"

where conda >nul 2>nul
if errorlevel 1 (
  echo [WARN] conda not found in PATH. Use "Anaconda Prompt" or add conda to PATH.
) else (
  call conda activate fall-detect
  if errorlevel 1 (
    echo [WARN] conda env "fall-detect" not found. Create it first:
    echo        conda create -n fall-detect python=3.10 -y
  )
)

python -c "import fastapi, uvicorn" >nul 2>nul
if errorlevel 1 (
  echo [WARN] python deps missing. Run: pip install -r requirements.txt
)

echo [INFO] starting fall detection service at http://localhost:8001
echo        health : http://127.0.0.1:8001/api/fall-detection/health
echo        docs   : http://127.0.0.1:8001/docs
echo        NOTE   : degraded mode unless algorithm\models is complete.
python -m uvicorn main:app --host 0.0.0.0 --port 8001
pause
