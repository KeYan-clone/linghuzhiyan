# 启动基础设施服务的PowerShell脚本（包含环境变量加载）

Write-Host "========== 启动灵狐智验基础设施服务 ==========" -ForegroundColor Green

# 加载环境变量
Write-Host "步骤1: 加载环境变量" -ForegroundColor Cyan
.\load-env.ps1

if ($LASTEXITCODE -ne 0) {
    Write-Host "环境变量加载失败，终止启动" -ForegroundColor Red
    exit 1
}

# 启动顺序
$services = @(
    @{Name="linghuzhiyan-discovery-server"; Port=8761; Desc="服务发现中心"},
    @{Name="linghuzhiyan-config-server"; Port=8888; Desc="配置中心"},
    @{Name="linghuzhiyan-gateway"; Port=8080; Desc="API网关"},
    @{Name="linghuzhiyan-monitor-service"; Port=8090; Desc="监控服务"}
)

foreach ($service in $services) {
    Write-Host "步骤: 启动 $($service.Desc) ($($service.Name))" -ForegroundColor Yellow
    Write-Host "端口: $($service.Port)" -ForegroundColor Gray
    
    Set-Location $service.Name
    
    # 在后台启动服务
    Start-Process -NoNewWindow -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory (Get-Location)
    
    Set-Location ..
    
    # 等待服务启动
    Write-Host "等待服务启动..." -ForegroundColor Gray
    Start-Sleep 30
    
    # 检查服务是否启动成功
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -UseBasicParsing -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($service.Desc) 启动成功" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "⚠ $($service.Desc) 可能未完全启动，请检查日志" -ForegroundColor Yellow
    }
    
    Write-Host ""
}

Write-Host "========== 基础设施服务启动完成 ==========" -ForegroundColor Green
Write-Host "访问地址:" -ForegroundColor Cyan
Write-Host "  服务发现中心: http://localhost:8761" -ForegroundColor White
Write-Host "  配置中心: http://localhost:8888" -ForegroundColor White  
Write-Host "  API网关: http://localhost:8080" -ForegroundColor White
Write-Host "  监控服务: http://localhost:8090" -ForegroundColor White
