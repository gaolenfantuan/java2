param(
    [string]$Mode = "restore"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$projectDir = Split-Path -Parent $PSScriptRoot
$cacheDir = Join-Path $projectDir ".deps-cache"
$zipPath = Join-Path $cacheDir "node_modules.zip"
$srcDir = Join-Path $projectDir "node_modules"
$lockFile = Join-Path $projectDir "package-lock.json"
$logPath = Join-Path $cacheDir "cache.log"

function Write-Log([string]$msg) {
    try {
        if (-not (Test-Path $cacheDir)) { New-Item -ItemType Directory -Path $cacheDir -Force | Out-Null }
        Add-Content -LiteralPath $logPath -Value ((Get-Date -Format "yyyy-MM-dd HH:mm:ss") + "  " + $msg) -Encoding UTF8
    } catch { }
}

try {
    if ($Mode -eq "save") {
        if (-not (Test-Path $srcDir)) { Write-Log "save: node_modules 不存在，跳过"; exit 2 }
        if (-not (Test-Path $cacheDir)) { New-Item -ItemType Directory -Path $cacheDir -Force | Out-Null }
        if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
        # 第四个参数 $true 很关键：压缩包内保留 node_modules 这一层目录，
        # 否则还原时会把依赖直接倒进项目根目录。
        [System.IO.Compression.ZipFile]::CreateFromDirectory($srcDir, $zipPath, [System.IO.Compression.CompressionLevel]::Fastest, $true)
        Write-Log ("save: 成功 " + [math]::Round((Get-Item $zipPath).Length / 1MB, 1) + " MB")
        exit 0
    }

    if ($Mode -eq "restore") {
        if (-not (Test-Path $zipPath)) { Write-Log "restore: 缓存不存在"; exit 3 }
        if (Test-Path $srcDir) { Write-Log "restore: node_modules 已存在，跳过"; exit 4 }
        # 依赖清单比缓存新，说明装过新包，缓存已过期，交给在线安装
        if (Test-Path $lockFile) {
            if ((Get-Item $lockFile).LastWriteTime -gt (Get-Item $zipPath).LastWriteTime) {
                Write-Log "restore: package-lock.json 比缓存新，缓存过期"
                exit 5
            }
        }
        [System.IO.Compression.ZipFile]::ExtractToDirectory($zipPath, $projectDir)
        Write-Log "restore: 成功"
        exit 0
    }

    Write-Log ("未知参数: " + $Mode)
    exit 1
} catch {
    Write-Log ("异常: " + $_.Exception.Message)
    exit 9
}
