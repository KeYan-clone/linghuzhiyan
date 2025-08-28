Param(
  [switch]$ScaleDown,
  [switch]$Delete,
  [switch]$KillPortForward
)

$ErrorActionPreference = 'Stop'

function Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Warn($msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Die($msg)  { Write-Host "[ERR ] $msg"  -ForegroundColor Red; exit 1 }

if (-not ($ScaleDown -or $Delete)) { $ScaleDown = $true }

# Verify prerequisites
Info "Checking kubectl..."
kubectl version --client --output=yaml | Out-Null

# Stop port-forward sessions if requested
if ($KillPortForward) {
  Info "Stopping kubectl port-forward sessions..."
  try {
    $procs = Get-CimInstance Win32_Process -Filter "name='kubectl.exe'"
    $pf = $procs | Where-Object { $_.CommandLine -match 'port-forward' }
    if ($pf) {
      $pf | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
      Info ("Stopped {0} kubectl port-forward process(es)." -f $pf.Count)
    } else {
      Warn "No kubectl port-forward process found."
    }
  } catch { Warn $_ }
}

$deploys = @('discovery-server','config-server','gateway','monitor-service','auth-service','user-service')

# Scale down deployments to zero replicas
if ($ScaleDown) {
  Info "Scaling deployments to 0 replicas..."
  foreach ($d in $deploys) {
    try {
      kubectl scale deployment/$d --replicas=0 | Write-Host
    } catch { Warn ("Failed to scale {0}: {1}" -f $d, $_) }
  }
}

# Delete all resources from the manifest
if ($Delete) {
  $manifest = ".\k8s\all-in-one.yaml"
  if (-not (Test-Path $manifest)) { Die "Manifest not found: $manifest" }
  Info "Deleting all resources from $manifest"
  kubectl delete -f $manifest --ignore-not-found | Write-Host
}

Info "Done."
