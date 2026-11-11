param(
    [string]$JdkHome,
    [string]$DatabaseConfig,
    [switch]$CompileOnly
)
$ErrorActionPreference = 'Stop'
if (-not $JdkHome) { $JdkHome = $env:JAVA_HOME }
if (-not $JdkHome -and (Test-Path 'C:\Users\ADMIN\.gradle\jdks\azul_systems__inc_-21-amd64-windows.2')) {
    $JdkHome = 'C:\Users\ADMIN\.gradle\jdks\azul_systems__inc_-21-amd64-windows.2'
}
if ($JdkHome) {
    $taskJavac = Join-Path $JdkHome 'bin\javac.exe'
    $taskJava = Join-Path $JdkHome 'bin\java.exe'
} else {
    $taskJavac = (Get-Command javac.exe -ErrorAction Stop).Source
    $taskJava = Join-Path (Split-Path $taskJavac) 'java.exe'
}
if (-not (Test-Path -LiteralPath $taskJavac)) { throw 'Can JDK 17 tro len. Truyen -JdkHome la thu muc goc cua JDK.' }
if ($DatabaseConfig) { $DatabaseConfig = (Resolve-Path -LiteralPath $DatabaseConfig).Path }
$taskBuild = Join-Path ([IO.Path]::GetTempPath()) ('product-customer-app-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $taskBuild | Out-Null
Push-Location $PSScriptRoot
try {
    & $taskJavac --release 17 -encoding UTF-8 -cp 'lib/*' -sourcepath src -d $taskBuild src/ui/UI_SanPhamKhachHang.java
    if ($LASTEXITCODE -ne 0) { throw 'Bien dich San Pham/Khach Hang that bai.' }
    Write-Host 'Da bien dich rieng San Pham va Khach Hang.'
    if (-not $CompileOnly) {
        $taskJavaArgs = @('-Dfile.encoding=UTF-8')
        if ($DatabaseConfig) { $taskJavaArgs += "-Dgiathanh.db.config=$DatabaseConfig" }
        $taskJavaArgs += @('-cp', "$taskBuild;src;lib/*", 'ui.UI_SanPhamKhachHang')
        & $taskJava @taskJavaArgs
        if ($LASTEXITCODE -ne 0) { throw 'Chay San Pham/Khach Hang that bai.' }
    }
} finally {
    Pop-Location
}
