Param(
  [switch]$SkipBuild,
  [switch]$WaitReady,
  [switch]$PortForward,
  [switch]$Down
)

$ErrorActionPreference = 'Stop'

function Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Warn($msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Die($msg)  { Write-Host "[ERR ] $msg"  -ForegroundColor Red; exit 1 }

# Verify prerequisites
Info "Checking docker and kubectl..."
docker --version | Out-Null
kubectl version --client --output=yaml | Out-Null

# Short-circuit: Down mode (scale deployments to 0 via stop-and-clean.ps1)
if ($Down) {
  Info "Down requested. Scaling all deployments to 0 replicas..."
  $stopScript = Join-Path $PSScriptRoot 'stop-and-clean.ps1'
  if (-not (Test-Path $stopScript)) { Die "Stop script not found: $stopScript" }
  & $stopScript -ScaleDown
  Info "Down completed."
  return
}

# Build images
$modules = @(
  @{ name='discovery-server'; path='linghuzhiyan-discovery-server' },
  @{ name='config-server';    path='linghuzhiyan-config-server' },
  @{ name='gateway';          path='linghuzhiyan-gateway' },
  @{ name='monitor-service';  path='linghuzhiyan-monitor-service' },
  @{ name='auth-service';     path='linghuzhiyan-auth-service' },
  @{ name='user-service';     path='linghuzhiyan-user-service' }
)

if (-not $SkipBuild) {
  foreach ($m in $modules) {
    $img = "$($m.name):j21"
    $ctx = ".\$($m.path)"
    Info "Building $img from $ctx"
    docker build -t $img $ctx
  }
} else {
  Warn "SkipBuild specified. Skipping docker build."
}

# Apply k8s manifest
$manifest = ".\k8s\all-in-one.yaml"
if (-not (Test-Path $manifest)) { Die "Manifest not found: $manifest" }
Info "Applying manifest $manifest"
kubectl apply -f $manifest | Write-Host

# Optionally wait for deployments ready
if ($WaitReady) {
  $deploys = @('discovery-server','config-server','gateway','monitor-service','auth-service','user-service')
  foreach ($d in $deploys) {
    Info "Waiting for deployment/$d to be ready..."
    kubectl rollout status deployment/$d --timeout=180s
  }
}

# Optional port-forward for local access
if ($PortForward) {
  Info "Starting port-forward sessions (Ctrl+C to stop)..."
  Start-Process powershell -ArgumentList "-NoExit","-Command","kubectl port-forward svc/discovery-server 8761:8761"
  Start-Process powershell -ArgumentList "-NoExit","-Command","kubectl port-forward svc/config-server 8888:8888"
  Start-Process powershell -ArgumentList "-NoExit","-Command","kubectl port-forward svc/gateway 8080:80"
  Start-Process powershell -ArgumentList "-NoExit","-Command","kubectl port-forward svc/monitor-service 8090:8090"
}

Info "Done."
