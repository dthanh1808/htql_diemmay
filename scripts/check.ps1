param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if ($JavaHome) {
    $compiler = Join-Path $JavaHome 'bin/javac.exe'
} else {
    $compilerCommand = Get-Command javac -ErrorAction SilentlyContinue
    if (-not $compilerCommand) { throw 'Khong tim thay JDK. Truyen -JavaHome <thu-muc-JDK-17> hoac dat JAVA_HOME.' }
    $compiler = $compilerCommand.Source
}
if (-not (Test-Path -LiteralPath $compiler)) { throw "Khong tim thay javac: $compiler" }
$runtime = Join-Path (Split-Path -Parent $compiler) 'java.exe'
$classes = Join-Path $repoRoot 'build/classes'
$testData = Join-Path $repoRoot 'build/test-data'
New-Item -ItemType Directory -Path $classes, $testData -Force | Out-Null
$sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $repoRoot 'src'), (Join-Path $repoRoot 'test') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& $compiler -encoding UTF-8 --release 17 -d $classes @sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'Bien dich that bai. Can JDK 17 tro len.' }
foreach ($testClass in @('dao.SanPhamDAOTest', 'ui.SanPhamUiTest', 'dao.KhachHangDAOTest', 'ui.KhachHangUiTest')) {
    & $runtime '-Djava.awt.headless=true' -cp "$classes;$repoRoot" $testClass $testData
    if ($LASTEXITCODE -ne 0) { throw "Kiem tra that bai: $testClass" }
}
Write-Output 'All product and customer checks passed.'
