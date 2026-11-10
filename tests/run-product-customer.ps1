param([string]$JdkHome)
$ErrorActionPreference = 'Stop'
if (-not $JdkHome) { $JdkHome = $env:JAVA_HOME }
if ($JdkHome) {
    $taskJavac = Join-Path $JdkHome 'bin\javac.exe'
    $taskJava = Join-Path $JdkHome 'bin\java.exe'
} else {
    $taskJavac = (Get-Command javac.exe -ErrorAction Stop).Source
    $taskJava = Join-Path (Split-Path $taskJavac) 'java.exe'
}
$taskBuild = Join-Path ([IO.Path]::GetTempPath()) ('product-customer-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $taskBuild | Out-Null
Push-Location (Split-Path $PSScriptRoot)
try {
    & $taskJavac --release 17 -encoding UTF-8 -cp 'lib/*' -sourcepath src -d $taskBuild `
        src/dao/DanhMucDAO.java src/dao/SanPhamDAO.java src/dao/KhachHangDAO.java `
        src/ui/PNL_SanPham.java src/ui/PNL_KhachHang.java tests/ProductCustomerRegressionTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Biên dịch Sản Phẩm/Khách Hàng thất bại.' }
    & $taskJava '-Djava.awt.headless=true' -cp "$taskBuild;src;lib/*" ui.ProductCustomerRegressionTest
    if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử hồi quy Sản Phẩm/Khách Hàng thất bại.' }
} finally {
    Pop-Location
}
