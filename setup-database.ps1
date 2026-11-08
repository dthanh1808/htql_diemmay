param(
    [string]$Server = '.\SQLEXPRESS',
    [switch]$WithDemoData
)
$ErrorActionPreference = 'Stop'
$sqlcmd = Get-Command sqlcmd.exe -ErrorAction SilentlyContinue
$sqlcmdPath = if ($sqlcmd) { $sqlcmd.Source } else { Join-Path $env:ProgramFiles 'Microsoft SQL Server/Client SDK/ODBC/180/Tools/Binn/SQLCMD.EXE' }
if (-not (Test-Path -LiteralPath $sqlcmdPath)) { throw 'Không tìm thấy sqlcmd.exe. Có thể chạy các file SQL bằng SSMS.' }
function Invoke-DatabaseScript([string]$Path) {
    & $sqlcmdPath -S $Server -E -C -l 5 -b -f 65001 -i $Path
    if ($LASTEXITCODE -ne 0) { throw "Chạy SQL thất bại: $Path" }
}
Push-Location $PSScriptRoot
try {
    $existing = & $sqlcmdPath -S $Server -E -C -l 5 -b -h -1 -W -Q "SET NOCOUNT ON; SELECT CASE WHEN DB_ID(N'QuanLyBanHang') IS NULL THEN 0 ELSE 1 END;"
    if ($LASTEXITCODE -ne 0) { throw 'Không kết nối được SQL Server. Khởi động dịch vụ SQL Server (SQLEXPRESS) bằng quyền Administrator trước.' }
    if (($existing -join '').Trim() -eq '0') {
        Invoke-DatabaseScript (Join-Path $PSScriptRoot 'sql/QuanLyBanHang.sql')
    } else {
        # Không chạy lại script CREATE TABLE trên database đang có.
        $required = & $sqlcmdPath -S $Server -E -C -l 5 -b -h -1 -W -d QuanLyBanHang -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') AND name IN ('HoaDon','ChiTietHoaDon','SanPham','DanhMuc','NhanVien','KhachHang','KhuyenMai','CaLamViec','LichLamViec','DanhMucCa','GiaoDichTraGop');"
        if ($LASTEXITCODE -ne 0 -or ($required -join '').Trim() -ne '11') { throw 'Database đã có nhưng chưa đủ các bảng cần thiết. Kiểm tra script gốc; không tự xóa hoặc tạo lại database.' }
    }
    Invoke-DatabaseScript (Join-Path $PSScriptRoot 'sql/02_BoSungHoaDon.sql')
    Invoke-DatabaseScript (Join-Path $PSScriptRoot 'sql/04_SanPhamKhachHang.sql')
    if ($WithDemoData) { Invoke-DatabaseScript (Join-Path $PSScriptRoot 'sql/03_DuLieuMau.sql') }
    Write-Host 'Đã chuẩn bị database QuanLyBanHang. Điền config/database.properties để ứng dụng kết nối bằng JDBC.'
} finally { Pop-Location }
