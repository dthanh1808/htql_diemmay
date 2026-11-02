param([string]$Server = 'localhost,1433')
$ErrorActionPreference = 'Stop'
$configurationPath = Join-Path $PSScriptRoot 'config/database.properties'
if (Test-Path -LiteralPath $configurationPath) {
    throw 'Đã có config/database.properties. Kiểm tra cấu hình hiện tại trước khi tạo tài khoản mới.'
}
Add-Type -AssemblyName System.Data
$connection = New-Object System.Data.SqlClient.SqlConnection
$connection.ConnectionString = "Server=$Server;Database=QuanLyBanHang;Integrated Security=True;Encrypt=True;TrustServerCertificate=True;Connect Timeout=5"
$generatedPassword = 'Gt1!' + [Guid]::NewGuid().ToString('N')
$loginName = 'GiaThanhApp_' + [Guid]::NewGuid().ToString('N').Substring(0,8)
try {
    $connection.Open()
    $transaction = $connection.BeginTransaction()
    try {
        $command = $connection.CreateCommand()
        $command.Transaction = $transaction
        $command.CommandText = @'
DECLARE @sql NVARCHAR(MAX);
SET @sql=N'CREATE LOGIN '+QUOTENAME(@login)+N' WITH PASSWORD='+QUOTENAME(@secret,'''')+N', CHECK_POLICY=ON, DEFAULT_DATABASE=[QuanLyBanHang];';
EXEC sys.sp_executesql @sql;
SET @sql=N'CREATE USER '+QUOTENAME(@login)+N' FOR LOGIN '+QUOTENAME(@login)+N';';
EXEC sys.sp_executesql @sql;
DECLARE @tables TABLE(name SYSNAME);
INSERT INTO @tables VALUES(N'HoaDon'),(N'ChiTietHoaDon'),(N'GiaoDichTraGop'),(N'SanPham'),(N'DanhMuc'),(N'KhachHang'),(N'NhanVien'),(N'CaLamViec'),(N'LichLamViec'),(N'DanhMucCa'),(N'KhuyenMai');
DECLARE @table SYSNAME;
DECLARE table_cursor CURSOR LOCAL FAST_FORWARD FOR SELECT name FROM @tables;
OPEN table_cursor;
FETCH NEXT FROM table_cursor INTO @table;
WHILE @@FETCH_STATUS=0
BEGIN
    SET @sql=N'GRANT SELECT ON dbo.'+QUOTENAME(@table)+N' TO '+QUOTENAME(@login)+N';';
    EXEC sys.sp_executesql @sql;
    FETCH NEXT FROM table_cursor INTO @table;
END;
CLOSE table_cursor;
DEALLOCATE table_cursor;
SET @sql=N'GRANT INSERT ON dbo.HoaDon TO '+QUOTENAME(@login)+N'; GRANT INSERT ON dbo.ChiTietHoaDon TO '+QUOTENAME(@login)+N'; GRANT INSERT ON dbo.GiaoDichTraGop TO '+QUOTENAME(@login)+N'; GRANT UPDATE ON dbo.SanPham TO '+QUOTENAME(@login)+N'; GRANT UPDATE ON dbo.KhachHang TO '+QUOTENAME(@login)+N'; GRANT UPDATE ON dbo.CaLamViec TO '+QUOTENAME(@login)+N';';
EXEC sys.sp_executesql @sql;
'@
        [void]$command.Parameters.Add('@login',[System.Data.SqlDbType]::NVarChar,128)
        [void]$command.Parameters.Add('@secret',[System.Data.SqlDbType]::NVarChar,128)
        $command.Parameters['@login'].Value=$loginName
        $command.Parameters['@secret'].Value=$generatedPassword
        [void]$command.ExecuteNonQuery()
        $jdbcServer = $Server.Replace(',',':').Replace('\','\\')
        $content = "db.url=jdbc:sqlserver://$jdbcServer;databaseName=QuanLyBanHang;encrypt=true;trustServerCertificate=true;loginTimeout=5;socketTimeout=15000;`ndb.user=$loginName`ndb.password=$generatedPassword`n"
        [IO.File]::WriteAllText($configurationPath,$content,(New-Object System.Text.UTF8Encoding($false)))
        $transaction.Commit()
        Write-Host 'Đã tạo tài khoản ứng dụng và lưu config/database.properties. Mật khẩu không hiển thị và file được Git bỏ qua.'
    } catch {
        try { $transaction.Rollback() } catch {}
        if (Test-Path -LiteralPath $configurationPath) { Remove-Item -LiteralPath $configurationPath }
        throw
    }
} finally { $connection.Dispose() }
