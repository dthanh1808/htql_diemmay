param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot

if (-not $JavaHome) {
    $jdkFolder = Join-Path $env:USERPROFILE '.jdks'
    if (Test-Path -LiteralPath $jdkFolder) {
        $jdk = Get-ChildItem -LiteralPath $jdkFolder -Directory |
            Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin/javac.exe') } |
            Sort-Object Name -Descending | Select-Object -First 1
        if ($jdk) { $JavaHome = $jdk.FullName }
    }
}
if ($JavaHome) {
    $compiler = Join-Path $JavaHome 'bin/javac.exe'
} else {
    $compilerCommand = Get-Command javac -ErrorAction SilentlyContinue
    if (-not $compilerCommand) { throw 'Khong tim thay JDK. Truyen -JavaHome <thu-muc-JDK-17>.' }
    $compiler = $compilerCommand.Source
}
if (-not (Test-Path -LiteralPath $compiler)) { throw "Khong tim thay javac: $compiler" }

$runtime = Join-Path (Split-Path -Parent $compiler) 'java.exe'
$classes = Join-Path $repoRoot 'bin'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $repoRoot 'src') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& $compiler -encoding UTF-8 --release 17 -d $classes @sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'Bien dich that bai. Can JDK 17 tro len.' }

# Luôn biên dịch mã nguồn mới nhất trước khi mở chương trình.
& $runtime -cp "$classes;$repoRoot" ui.UI_TrangChu
if ($LASTEXITCODE -ne 0) { throw 'Chuong trinh ket thuc voi loi.' }
