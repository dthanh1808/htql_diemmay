param([switch]$SqlIntegration)
$ErrorActionPreference = 'Stop'
$compiler = Get-Command javac.exe -ErrorAction SilentlyContinue
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    $javac = Join-Path $env:JAVA_HOME 'bin\javac.exe'
} elseif (Test-Path 'C:\Program Files\Java\jdk-23\bin\javac.exe') {
    $javac = 'C:\Program Files\Java\jdk-23\bin\javac.exe'
} elseif ($compiler) {
    $javac = $compiler.Source
} else {
    throw 'Cần cài JDK 21 trở lên và thêm thư mục bin của JDK vào PATH.'
}
$java = Join-Path (Split-Path $javac) 'java.exe'
Push-Location $PSScriptRoot
try {
    $sources = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
    & $javac --release 21 -encoding UTF-8 -cp 'lib/*' -d bin $sources
    if ($LASTEXITCODE -ne 0) { throw 'Biên dịch ứng dụng thất bại.' }
    $testSources = @(Get-ChildItem tests -Filter *.java | ForEach-Object { $_.FullName })
    & $javac --release 21 -encoding UTF-8 -cp 'bin;lib/*' -d build/test-classes $testSources
    if ($LASTEXITCODE -ne 0) { throw 'Biên dịch kiểm thử thất bại.' }
    & $java '-Djava.awt.headless=true' -cp 'build/test-classes;bin;src;lib/*' BusinessFeatureTest
    if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử thất bại.' }
    & $java '-Djava.awt.headless=true' -cp 'build/test-classes;bin;src;lib/*' ProductCustomerUiTest
    if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử UI khi mất kết nối thất bại.' }
    if ($SqlIntegration) {
        & $java -cp 'build/test-classes;bin;src;lib/*' SqlIntegrationTest
        if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử SQL Server thất bại.' }
    }
} finally {
    Pop-Location
}
