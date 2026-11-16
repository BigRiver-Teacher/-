# Self-check for zhaoxi-huxing (ASCII output for compatibility)
function Test-Url([string]$name, [string]$url) {
  try {
    $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 4
    Write-Host ("[OK]   {0,-22} {1} -> HTTP {2}" -f $name, $url, [int]$r.StatusCode) -ForegroundColor Green
  } catch {
    Write-Host ("[WAIT] {0,-22} {1} -> not ready" -f $name, $url) -ForegroundColor Yellow
  }
}
Write-Host "=== zhaoxi-huxing self check ==="
Test-Url "backend-stats"    "http://127.0.0.1:8080/api/stats/overview"
Test-Url "backend-doc"      "http://127.0.0.1:8080/doc.html"
Test-Url "frontend-dev"     "http://127.0.0.1:5173/"
Test-Url "algorithm-health" "http://127.0.0.1:8001/api/fall-detection/health"
Write-Host ""
Write-Host "Note: algorithm service runs in DEGRADED mode unless algorithm/models is complete."
Write-Host "Expect health 'loaded=false' in degraded mode; detect endpoints will return an explicit error."
