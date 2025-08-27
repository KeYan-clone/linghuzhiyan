# 启动基础设施服务的PowerShell脚本（包含环境变量加载）

param(
    [string]$EnvFile = ".env.development"
)

Write-Host "========== Start LingHuZhiYan Infrastructure ==========" -ForegroundColor Green

# Load environment variables
Write-Host "Step 1: Load environment variables ($EnvFile)" -ForegroundColor Cyan
try {
    & .\load-env.ps1 -EnvFile $EnvFile
}
catch {
    Write-Host "Failed to load env vars: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Services to start (order matters)
$services = @(
    @{Name="linghuzhiyan-discovery-server"; Port=8761; Desc="Eureka Server"},
    @{Name="linghuzhiyan-config-server"; Port=8888; Desc="Config Server"},
    @{Name="linghuzhiyan-gateway"; Port=8080; Desc="API Gateway"},
    @{Name="linghuzhiyan-monitor-service"; Port=8090; Desc="Monitor"}
)

foreach ($service in $services) {
    Write-Host "Starting $($service.Desc) ($($service.Name))" -ForegroundColor Yellow
    Write-Host "Port: $($service.Port)" -ForegroundColor Gray
    
    Set-Location $service.Name
    
    # For Config Server, force native profile and correct search-locations
    $origProfiles = $env:SPRING_PROFILES_ACTIVE
    $origSearchLocations = $env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS
    if ($service.Name -eq "linghuzhiyan-config-server") {
        $repoRoot = $PSScriptRoot
        if (-not $repoRoot -or $repoRoot -eq "") { $repoRoot = (Get-Location).Path }
        # Build file URI with forward slashes
        $nativePath = "file:" + ($repoRoot -replace '\\','/') + "/config-repo"
        $env:SPRING_PROFILES_ACTIVE = "native,local"
        $env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS = $nativePath
        Write-Host "Config Server search-locations: $nativePath" -ForegroundColor Gray
    }

    # Start service in background
    Start-Process -NoNewWindow -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory (Get-Location)
    
    Set-Location ..
    
    # Restore env overrides (keep parent's env clean for next service)
    $env:SPRING_PROFILES_ACTIVE = $origProfiles
    if ($null -ne $origSearchLocations -and $origSearchLocations -ne "") {
        $env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS = $origSearchLocations
    } else {
        Remove-Item Env:SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS -ErrorAction SilentlyContinue
    }
    
    # Wait for service to boot
    Write-Host "Waiting for service to start..." -ForegroundColor Gray
    Start-Sleep 30
    
    # Check health endpoint
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -UseBasicParsing -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($service.Desc) is up" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "⚠ $($service.Desc) may not be fully up, check logs." -ForegroundColor Yellow
    }
    
    Write-Host ""
}

Write-Host "========== Infrastructure started ==========" -ForegroundColor Green
Write-Host "Endpoints:" -ForegroundColor Cyan
Write-Host "  Eureka:  http://localhost:8761" -ForegroundColor White
Write-Host "  Config:  http://localhost:8888" -ForegroundColor White  
Write-Host "  Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  Monitor: http://localhost:8090" -ForegroundColor White
