# 加载环境变量的PowerShell脚本
# 使用方法：在启动应用前运行此脚本

param(
    [string]$EnvFile = ".env"
)

Write-Host "正在加载环境变量..." -ForegroundColor Green

# 检查环境变量文件是否存在
if (-not (Test-Path $EnvFile)) {
    Write-Host "环境变量文件 $EnvFile 不存在！" -ForegroundColor Red
    Write-Host "请复制 .env.example 为 .env 并填入正确的配置信息" -ForegroundColor Yellow
    exit 1
}

# 读取并设置环境变量
Get-Content $EnvFile | ForEach-Object {
    if ($_ -match "^\s*([^#][^=]*)\s*=\s*(.*)\s*$") {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        
        # 移除值周围的引号（如果有）
        if ($value -match '^"(.*)"$' -or $value -match "^'(.*)'$") {
            $value = $matches[1]
        }
        
        # 设置环境变量
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
        Write-Host "设置环境变量: $name" -ForegroundColor Cyan
    }
}

Write-Host "环境变量加载完成！" -ForegroundColor Green
Write-Host "当前会话中的环境变量已设置，可以启动应用了。" -ForegroundColor Yellow
