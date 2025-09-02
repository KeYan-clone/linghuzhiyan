# Stop all Maven services started by start-all-with-maven.ps1

$ErrorActionPreference = 'Stop'

$repoRoot = (Get-Item $PSScriptRoot).Parent.FullName; if (-not $repoRoot) { $repoRoot = (Get-Location).Path }
$pidDir = Join-Path $repoRoot 'scripts/.pids'

if (-not (Test-Path $pidDir)) {
    Write-Host "PID directory not found: $pidDir" -ForegroundColor Yellow
    exit 0
}

$pidFiles = Get-ChildItem -Path $pidDir -Filter '*.pid' -ErrorAction SilentlyContinue
if (-not $pidFiles -or $pidFiles.Count -eq 0) {
    Write-Host "No processes to stop." -ForegroundColor Yellow
    exit 0
}

Write-Host "========== Stop Maven processes ==========" -ForegroundColor Green
foreach ($pf in $pidFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($pf.Name)
    $pidValue  = Get-Content -Path $pf.FullName | Select-Object -First 1
    if (-not [int]::TryParse($pidValue, [ref]([int]$null))) {
        Write-Host "Skip invalid PID file: $($pf.FullName)" -ForegroundColor Yellow
        continue
    }
    try {
        Get-Process -Id $pidValue -ErrorAction Stop | Out-Null
        Write-Host ("Stopping {0} (PID {1})" -f $name, $pidValue) -ForegroundColor Yellow
        Stop-Process -Id $pidValue -Force -ErrorAction Stop
        Remove-Item $pf.FullName -Force -ErrorAction SilentlyContinue
    } catch {
        Write-Host ("Process not found or access denied (PID {0})" -f $pidValue) -ForegroundColor DarkGray
        Remove-Item $pf.FullName -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "========== Done ==========" -ForegroundColor Green
