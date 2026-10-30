# 计算后端源码指纹：pom.xml + src 下所有文件的路径/大小/修改时间
# 源码没变化 -> 指纹不变 -> 启动脚本直接复用 .build-cache\app.jar，跳过 Maven 编译
# 输出：一串 64 位十六进制；出错时输出 ERROR 让批处理回退到正常编译

$ErrorActionPreference = 'Stop'

try {
    $root = (Get-Location).Path

    $targets = @(
        (Join-Path $root 'pom.xml'),
        (Join-Path $root 'src')
    )

    $files = Get-ChildItem -Path $targets -Recurse -File -ErrorAction Stop |
             Sort-Object FullName

    $sb = New-Object System.Text.StringBuilder
    foreach ($f in $files) {
        $rel = $f.FullName.Substring($root.Length)
        [void]$sb.Append($rel)
        [void]$sb.Append('|')
        [void]$sb.Append($f.Length)
        [void]$sb.Append('|')
        [void]$sb.Append($f.LastWriteTimeUtc.Ticks)
        [void]$sb.Append("`n")
    }

    $sha = [System.Security.Cryptography.SHA256]::Create()
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($sb.ToString())
    $hash = ($sha.ComputeHash($bytes) | ForEach-Object { $_.ToString('x2') }) -join ''
    $sha.Dispose()

    Write-Output $hash
}
catch {
    Write-Output 'ERROR'
}
