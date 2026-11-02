-- Chạy sau QuanLyBanHang.sql. Chỉ thêm cột nullable, không xóa hoặc sửa dữ liệu hiện có.
USE QuanLyBanHang;
GO
IF COL_LENGTH('dbo.HoaDon','tienKhachDua') IS NULL
    ALTER TABLE dbo.HoaDon ADD tienKhachDua DECIMAL(18,0) NULL;
IF COL_LENGTH('dbo.HoaDon','hangThanhVienLucMua') IS NULL
    ALTER TABLE dbo.HoaDon ADD hangThanhVienLucMua NVARCHAR(30) NULL;
IF COL_LENGTH('dbo.HoaDon','tenKhachHangLucMua') IS NULL
    ALTER TABLE dbo.HoaDon ADD tenKhachHangLucMua NVARCHAR(100) NULL;
IF COL_LENGTH('dbo.HoaDon','sdtKhachHangLucMua') IS NULL
    ALTER TABLE dbo.HoaDon ADD sdtKhachHangLucMua VARCHAR(15) NULL;
IF COL_LENGTH('dbo.HoaDon','tenKhuyenMaiLucMua') IS NULL
    ALTER TABLE dbo.HoaDon ADD tenKhuyenMaiLucMua NVARCHAR(150) NULL;
IF COL_LENGTH('dbo.ChiTietHoaDon','tenSPLucBan') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon ADD tenSPLucBan NVARCHAR(150) NULL;
IF COL_LENGTH('dbo.ChiTietHoaDon','tenDanhMucLucBan') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon ADD tenDanhMucLucBan NVARCHAR(100) NULL;
GO
