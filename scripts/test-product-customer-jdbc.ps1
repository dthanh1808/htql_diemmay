param(
    [string]$Server = '.\SQLEXPRESS',
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $JavaHome) {
    $compiler = Get-Command javac.exe -ErrorAction Stop
    $JavaHome = Split-Path -Parent (Split-Path -Parent $compiler.Source)
}
$java = Join-Path $JavaHome 'bin/java.exe'
if (-not (Test-Path -LiteralPath $java)) { throw 'Truyền -JavaHome tới JDK 21 trở lên.' }
Add-Type -AssemblyName System.Data
$testSuffix = [Guid]::NewGuid().ToString('N')
$testDatabase = 'GiaThanhJdbcTest_' + $testSuffix
$testLogin = 'GiaThanhJdbcTest_' + $testSuffix
$testSecret = 'Gt1!' + [Guid]::NewGuid().ToString('N')
$dbCreated = $false
$loginCreated = $false
$master = New-Object System.Data.SqlClient.SqlConnection
$builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
$builder['Data Source'] = $Server
$builder['Initial Catalog'] = 'master'
$builder['Integrated Security'] = $true
$builder['Encrypt'] = $true
$builder['TrustServerCertificate'] = $true
$builder['Connect Timeout'] = 5
$master.ConnectionString = $builder.ConnectionString
$oldUrl = $env:GIATHANH_DB_URL
$oldUser = $env:GIATHANH_DB_USER
$oldPassword = $env:GIATHANH_DB_PASSWORD
function Execute-TestSql([string]$Sql) {
    $command = $master.CreateCommand()
    $command.CommandTimeout = 60
    $command.CommandText = $Sql
    try { [void]$command.ExecuteNonQuery() } finally { $command.Dispose() }
}
try {
    $master.Open()
    $check = $master.CreateCommand()
    $check.CommandText = "SELECT CAST(SERVERPROPERTY('IsIntegratedSecurityOnly') AS INT)"
    if ($check.ExecuteScalar() -eq 1) { throw 'SQL Server cần bật SQL Server and Windows Authentication để JDBC dùng tài khoản test riêng.' }
    $check.Dispose()
    Execute-TestSql "CREATE DATABASE [$testDatabase]"
    $dbCreated = $true
    $master.ChangeDatabase($testDatabase)
    # Chỉ phần DDL của script gốc; không chạy khối DELETE và dữ liệu minh họa.
    $schema = [IO.File]::ReadAllText((Join-Path $repoRoot 'sql/QuanLyBanHang.sql'))
    $schema = $schema.Substring($schema.IndexOf('USE QuanLyBanHang;'))
    $legacyData = $schema.IndexOf('-- Xóa dữ liệu cũ')
    if ($legacyData -ge 0) { $schema = $schema.Substring(0, $legacyData) }
    $scripts = @(
        $schema,
        [IO.File]::ReadAllText((Join-Path $repoRoot 'sql/02_BoSungHoaDon.sql')),
        [IO.File]::ReadAllText((Join-Path $repoRoot 'sql/04_SanPhamKhachHang.sql'))
    )
    foreach ($script in $scripts) {
        $script = $script.Replace('USE QuanLyBanHang;', "USE [$testDatabase];")
        foreach ($batch in [regex]::Split($script, '(?im)^\s*GO\s*$')) {
            if ($batch.Trim()) { Execute-TestSql $batch }
        }
    }
    # Chạy lại migration để phát hiện lỗi không idempotent.
    $migration = $scripts[2].Replace('USE QuanLyBanHang;', "USE [$testDatabase];")
    foreach ($batch in [regex]::Split($migration, '(?im)^\s*GO\s*$')) {
        if ($batch.Trim()) { Execute-TestSql $batch }
    }
    $master.ChangeDatabase('master')
    $command = $master.CreateCommand()
    $command.CommandText = "DECLARE @sql NVARCHAR(MAX)=N'CREATE LOGIN '+QUOTENAME(@login)+N' WITH PASSWORD='+QUOTENAME(@secret,'''')+N',CHECK_POLICY=ON;'; EXEC sys.sp_executesql @sql;"
    [void]$command.Parameters.AddWithValue('@login',$testLogin)
    [void]$command.Parameters.AddWithValue('@secret',$testSecret)
    [void]$command.ExecuteNonQuery()
    $command.Dispose()
    $loginCreated = $true
    $master.ChangeDatabase($testDatabase)
    Execute-TestSql "CREATE USER [$testLogin] FOR LOGIN [$testLogin]; ALTER ROLE db_owner ADD MEMBER [$testLogin];"
    # Quyền db_owner này chỉ tồn tại trong DB test sẽ bị xóa trong finally.
    # JDBC không hiểu host '.' và không cần phụ thuộc SQL Browser nếu biết cổng TCP.
    $portCommand = $master.CreateCommand()
    $portCommand.CommandText = "SELECT TOP 1 port FROM sys.dm_tcp_listener_states WHERE type_desc='TSQL' AND state_desc='ONLINE' ORDER BY port"
    $tcpPort = $portCommand.ExecuteScalar()
    $portCommand.Dispose()
    if (-not $tcpPort) { throw 'SQL Server chưa bật TCP/IP. Bật TCP/IP trong SQL Server Configuration Manager rồi khởi động lại dịch vụ.' }
    $jdbcServer = ([regex]::Split($Server,'[\\,]'))[0].Replace('tcp:','')
    if ($jdbcServer -eq '.' -or $jdbcServer -eq '(local)') { $jdbcServer = 'localhost' }
    $env:GIATHANH_DB_URL = "jdbc:sqlserver://${jdbcServer}:$tcpPort;databaseName=$testDatabase;encrypt=true;trustServerCertificate=true;loginTimeout=5;socketTimeout=15000;"
    $env:GIATHANH_DB_USER = $testLogin
    $env:GIATHANH_DB_PASSWORD = $testSecret
    Push-Location $repoRoot
    try {
        & (Join-Path $JavaHome 'bin/javac.exe') --release 21 -encoding UTF-8 -cp 'bin;lib/*' -d build/test-classes tests/ProductCustomerJdbcTest.java tests/SqlIntegrationTest.java
        if ($LASTEXITCODE -ne 0) { throw 'Biên dịch JDBC test thất bại. Chạy build-and-test.ps1 trước.' }
        & $java -cp 'build/test-classes;bin;src;lib/*' SqlIntegrationTest
        if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử nối hóa đơn/chi tiêu thất bại.' }
        & $java '-Djava.awt.headless=true' -cp 'build/test-classes;bin;src;lib/*' ProductCustomerJdbcTest
        if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử JDBC thất bại.' }
    } finally { Pop-Location }
} finally {
    $env:GIATHANH_DB_URL = $oldUrl
    $env:GIATHANH_DB_USER = $oldUser
    $env:GIATHANH_DB_PASSWORD = $oldPassword
    if ($master.State -eq 'Open') {
        $master.ChangeDatabase('master')
        # Chỉ dọn DB/login do chính lần chạy này tạo; tên GUID được kiểm tra trước khi DROP.
        if ($dbCreated -and $testDatabase -match '^GiaThanhJdbcTest_[a-f0-9]{32}$') {
            Execute-TestSql "ALTER DATABASE [$testDatabase] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$testDatabase];"
        }
        if ($loginCreated -and $testLogin -match '^GiaThanhJdbcTest_[a-f0-9]{32}$') {
            Execute-TestSql "DROP LOGIN [$testLogin];"
        }
    }
    $master.Dispose()
}
