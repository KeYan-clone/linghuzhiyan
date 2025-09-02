<#
Start all microservices using Maven (spring-boot:run), in order: infrastructure -> business.
Assumptions: external deps (MySQL/Redis/MinIO) are already available (e.g. via Docker Compose).
Requirements: JDK + Maven, Windows PowerShell 5.1+
#>

param(
    [string]$EnvFile = ".env.development",
    [switch]$InfraOnly,
    [string[]]$Only = @(),
    [int]$DelaySeconds = 25,
    [switch]$NoHealthCheck,
    [switch]$NoEnvLoad,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

function Test-MavenInstalled {
    try { Get-Command mvn -ErrorAction Stop | Out-Null }
    catch { throw "Maven (mvn) not found. Please install Maven and add it to PATH." }
}

function Import-EnvFile($envFile) {
    if ($NoEnvLoad) { return }
    $loader = Join-Path $repoRoot 'load-env.ps1'
    if (Test-Path $loader) {
        Write-Host "Load env file ($envFile)" -ForegroundColor Cyan
        & $loader -EnvFile $envFile
    } else {
        Write-Host "load-env.ps1 not found, skip env loading" -ForegroundColor Yellow
    }
}

function New-PidStore() {
    $pidDir = Join-Path $repoRoot 'scripts/.pids'
    if (-not (Test-Path $pidDir)) { New-Item -ItemType Directory -Path $pidDir | Out-Null }
    return $pidDir
}

function New-LogStore() {
    $logDir = Join-Path $repoRoot 'scripts/.logs'
    if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir | Out-Null }
    return $logDir
}

function Start-MavenService([hashtable]$svc, [string]$profiles = 'local') {
    $svcPath = Join-Path $repoRoot $svc.Name
    if (-not (Test-Path $svcPath)) { throw "Module not found: $($svc.Name) ($svcPath)" }

    Write-Host "Starting $($svc.Desc) [$($svc.Name)]" -ForegroundColor Yellow
    if ($svc.ContainsKey('Port') -and $svc.Port) {
        Write-Host ("  Port: {0}" -f $svc.Port) -ForegroundColor DarkGray
    }

    Push-Location $svcPath

    # For Config Server: enable native profile and point to repo folder
    $origProfiles = $env:SPRING_PROFILES_ACTIVE
    $origSearchLocations = $env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS
    $needConfigOverrides = ($svc.Name -eq 'linghuzhiyan-config-server')
    if ($needConfigOverrides) {
        $profiles = 'native,local'
        $nativeDir = Join-Path $repoRoot 'config-repo'
        $nativeUri = 'file:' + ($nativeDir -replace '\\','/')
        $env:SPRING_PROFILES_ACTIVE = $profiles
        $env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS = $nativeUri
        Write-Host "  Config native repo: $nativeUri" -ForegroundColor DarkGray
    }

    $mvnArgs = "-DskipTests -Dspring-boot.run.profiles=$profiles spring-boot:run"
    $logDir = New-LogStore
    $logFileOut = Join-Path $logDir ("{0}.out.log" -f $svc.Name)
    $logFileErr = Join-Path $logDir ("{0}.err.log" -f $svc.Name)
    if ($DryRun) {
        Write-Host "  [DryRun] mvn $mvnArgs" -ForegroundColor DarkGray
        $proc = $null
    } else {
        $proc = Start-Process -FilePath "mvn" -ArgumentList $mvnArgs -WorkingDirectory (Get-Location) -PassThru -NoNewWindow -RedirectStandardOutput $logFileOut -RedirectStandardError $logFileErr
    }

    Pop-Location

    if ($needConfigOverrides) {
        $env:SPRING_PROFILES_ACTIVE = $origProfiles
        if ($null -ne $origSearchLocations -and $origSearchLocations -ne '') {
            $env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS = $origSearchLocations
        } else {
            Remove-Item Env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS -ErrorAction SilentlyContinue
        }
    }

    if (-not $DryRun) {
        $pidDir = New-PidStore
        $pidFile = Join-Path $pidDir ("{0}.pid" -f $svc.Name)
        Set-Content -Path $pidFile -Value $proc.Id -Encoding ascii
        Write-Host ("  PID: {0}" -f $proc.Id) -ForegroundColor DarkGray
        Write-Host ("  Log(out): {0}" -f $logFileOut) -ForegroundColor DarkGray
        Write-Host ("  Log(err): {0}" -f $logFileErr) -ForegroundColor DarkGray
    }

    # 等待并探测健康端点
    if (-not $DryRun) {
        Start-Sleep -Seconds $DelaySeconds
        if (-not $NoHealthCheck -and $svc.ContainsKey('Port') -and $svc.Port) {
            try {
                $healthUrl = "http://localhost:$($svc.Port)/actuator/health"
                $resp = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 8
                if ($resp.StatusCode -eq 200) {
                    Write-Host "  Health: OK" -ForegroundColor Green
                } else {
                    Write-Host ("  Health: {0}" -f $resp.StatusCode) -ForegroundColor Yellow
                }
            } catch {
                Write-Host "  Health: request failed (service may still be starting)" -ForegroundColor Yellow
            }
        }
    }
}

# ---- main ----
$repoRoot = (Get-Item $PSScriptRoot).Parent.FullName; if (-not $repoRoot) { $repoRoot = (Get-Location).Path }
Write-Host "========== Start project with Maven ==========" -ForegroundColor Green
Test-MavenInstalled
Import-EnvFile -envFile $EnvFile

# Define order: infra then business services
$infra = @(
    @{ Name = 'linghuzhiyan-discovery-server'; Desc = 'Eureka Server'; Port = 8761 },
    @{ Name = 'linghuzhiyan-config-server';    Desc = 'Config Server'; Port = 8888 },
    @{ Name = 'linghuzhiyan-gateway';          Desc = 'API Gateway';   Port = 8080 },
    @{ Name = 'linghuzhiyan-monitor-service';  Desc = 'Monitor';       Port = 8090 }
)

$biz = @(
    @{ Name = 'linghuzhiyan-auth-service';        Desc = 'Auth Service' },
    @{ Name = 'linghuzhiyan-user-service';        Desc = 'User Service' },
    @{ Name = 'linghuzhiyan-experiment-service';  Desc = 'Experiment Service' },
    @{ Name = 'linghuzhiyan-resource-service';    Desc = 'Resource Service' },
    @{ Name = 'linghuzhiyan-message-service';     Desc = 'Message Service' },
    @{ Name = 'linghuzhiyan-discussion-service';  Desc = 'Discussion Service' }
)

$all = @($infra + $(if ($InfraOnly) { @() } else { $biz }))

if ($Only.Count -gt 0) {
    $set = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    $Only | ForEach-Object { [void]$set.Add($_) }
    $all = $all | Where-Object { $set.Contains($_.Name) }
}

if ($all.Count -eq 0) {
    Write-Host "No modules selected to start (check names/filters)." -ForegroundColor Red
    exit 1
}

Write-Host "Modules to start:" -ForegroundColor Cyan
foreach ($s in $all) { Write-Host (" - {0}" -f $s.Name) -ForegroundColor White }
Write-Host "------------------------------------------"

foreach ($svc in $all) { Start-MavenService -svc $svc }

Write-Host "========== Done ==========" -ForegroundColor Green
Write-Host "Tip: use scripts/stop-all-with-maven.ps1 to stop these processes." -ForegroundColor Yellow
