-- Chạy sau 02_BoSungHoaDon.sql trên database QuanLyBanHang.
-- Có thể chạy lại. Không xóa bảng, dữ liệu hoặc thêm khách/sản phẩm minh họa.
USE QuanLyBanHang;
GO
-- Cần các SET option này cho filtered index, kể cả chạy bằng sqlcmd.
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;
BEGIN TRANSACTION;
GO
IF COL_LENGTH('dbo.SanPham','maNoiBo') IS NULL
    ALTER TABLE dbo.SanPham ADD maNoiBo VARCHAR(40) NULL;
IF COL_LENGTH('dbo.SanPham','anhBase64') IS NULL
    ALTER TABLE dbo.SanPham ADD anhBase64 VARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.SanPham','taoLuc') IS NULL
    ALTER TABLE dbo.SanPham ADD taoLuc DATETIME2 NOT NULL CONSTRAINT DF_SP_taoLuc DEFAULT SYSDATETIME();
IF COL_LENGTH('dbo.SanPham','capNhatLuc') IS NULL
    ALTER TABLE dbo.SanPham ADD capNhatLuc DATETIME2 NOT NULL CONSTRAINT DF_SP_capNhatLuc DEFAULT SYSDATETIME();
IF COL_LENGTH('dbo.SanPham','slTon') IS NULL
    ALTER TABLE dbo.SanPham ADD slTon INT NOT NULL CONSTRAINT DF_SP_slTon DEFAULT 0;
GO
UPDATE dbo.SanPham SET maNoiBo=maSKU WHERE maNoiBo IS NULL;
IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.SanPham') AND name='maNoiBo' AND is_nullable=1)
    ALTER TABLE dbo.SanPham ALTER COLUMN maNoiBo VARCHAR(40) NOT NULL;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.SanPham') AND name='UX_SP_maNoiBo')
    CREATE UNIQUE INDEX UX_SP_maNoiBo ON dbo.SanPham(maNoiBo);
IF NOT EXISTS(SELECT 1 FROM sys.default_constraints WHERE parent_object_id=OBJECT_ID('dbo.SanPham') AND parent_column_id=COLUMNPROPERTY(OBJECT_ID('dbo.SanPham'),'maNoiBo','ColumnId'))
    ALTER TABLE dbo.SanPham ADD CONSTRAINT DF_SP_maNoiBo DEFAULT CONVERT(VARCHAR(36),NEWID()) FOR maNoiBo;
ALTER TABLE dbo.SanPham ALTER COLUMN maKM VARCHAR(10) NULL;
ALTER TABLE dbo.SanPham ALTER COLUMN moTa NVARCHAR(3000) NULL;
ALTER TABLE dbo.DanhMuc ALTER COLUMN moTa NVARCHAR(1000) NULL;
-- SQL Server UNIQUE cho phép duy nhất một NULL; index lọc cho phép nhiều sản phẩm không có mã vạch.
IF EXISTS(SELECT 1 FROM sys.key_constraints WHERE parent_object_id=OBJECT_ID('dbo.SanPham') AND name='UQ_SP_maVach')
    ALTER TABLE dbo.SanPham DROP CONSTRAINT UQ_SP_maVach;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.SanPham') AND name='UX_SP_maVach')
    CREATE UNIQUE INDEX UX_SP_maVach ON dbo.SanPham(maVach) WHERE maVach IS NOT NULL;
GO
IF COL_LENGTH('dbo.KhachHang','nhanKhuyenMai') IS NULL
    ALTER TABLE dbo.KhachHang ADD nhanKhuyenMai BIT NOT NULL CONSTRAINT DF_KH_nhanKM DEFAULT 0;
IF COL_LENGTH('dbo.KhachHang','ghiChu') IS NULL
    ALTER TABLE dbo.KhachHang ADD ghiChu NVARCHAR(1000) NULL;
IF COL_LENGTH('dbo.KhachHang','taoLuc') IS NULL
    ALTER TABLE dbo.KhachHang ADD taoLuc DATETIME2 NOT NULL CONSTRAINT DF_KH_taoLuc DEFAULT SYSDATETIME();
IF COL_LENGTH('dbo.KhachHang','capNhatLuc') IS NULL
    ALTER TABLE dbo.KhachHang ADD capNhatLuc DATETIME2 NOT NULL CONSTRAINT DF_KH_capNhatLuc DEFAULT SYSDATETIME();
GO
-- Gỡ riêng index email do script này tạo trước khi nới độ dài cột (lần chạy lại).
IF EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.KhachHang') AND name='UX_KH_email')
    DROP INDEX UX_KH_email ON dbo.KhachHang;
ALTER TABLE dbo.KhachHang ALTER COLUMN email VARCHAR(254) NULL;
ALTER TABLE dbo.KhachHang ALTER COLUMN diaChi NVARCHAR(300) NULL;
IF NOT EXISTS(SELECT 1 FROM dbo.KhachHang WHERE email IS NOT NULL AND email<>'' GROUP BY email HAVING COUNT(*)>1)
    CREATE UNIQUE INDEX UX_KH_email ON dbo.KhachHang(email) WHERE email IS NOT NULL AND email<>'';
ELSE
    THROW 50001,N'Email khách hàng đang trùng. Hãy xử lý dữ liệu trùng rồi chạy lại script 04.',1;
GO
IF OBJECT_ID('dbo.HangThanhVien','U') IS NULL
BEGIN
    CREATE TABLE dbo.HangThanhVien (
        tenHang NVARCHAR(30) NOT NULL PRIMARY KEY,
        thuTu INT NOT NULL UNIQUE,
        chiTieuToiThieu DECIMAL(18,0) NOT NULL CHECK(chiTieuToiThieu>=0),
        phanTramUuDai INT NOT NULL CHECK(phanTramUuDai BETWEEN 0 AND 70)
    );
    INSERT INTO dbo.HangThanhVien VALUES
        (N'Member',0,0,0),(N'Silver',1,1000000,3),(N'Gold',2,3000000,5),(N'Diamond',3,10000000,8);
END;
GO
IF OBJECT_ID('dbo.LichSuKhachHang','U') IS NULL
BEGIN
    CREATE TABLE dbo.LichSuKhachHang (
        id BIGINT IDENTITY PRIMARY KEY,
        maKH VARCHAR(10) NOT NULL,
        thoiGian DATETIME2 NOT NULL CONSTRAINT DF_LSKH_thoiGian DEFAULT SYSDATETIME(),
        loai NVARCHAR(50) NOT NULL,
        noiDung NVARCHAR(2000) NOT NULL,
        maHD VARCHAR(12) NULL,
        CONSTRAINT FK_LSKH_KhachHang FOREIGN KEY(maKH) REFERENCES dbo.KhachHang(maKH) ON DELETE CASCADE,
        CONSTRAINT FK_LSKH_HoaDon FOREIGN KEY(maHD) REFERENCES dbo.HoaDon(maHD)
    );
    CREATE INDEX IX_LSKH_KhachHang ON dbo.LichSuKhachHang(maKH,thoiGian DESC,id DESC);
    CREATE UNIQUE INDEX UX_LSKH_HoaDon ON dbo.LichSuKhachHang(maHD) WHERE maHD IS NOT NULL;
END;
IF OBJECT_ID('dbo.ThongBaoKhuyenMai','U') IS NULL
BEGIN
    CREATE TABLE dbo.ThongBaoKhuyenMai (
        id BIGINT IDENTITY PRIMARY KEY,
        maKH VARCHAR(10) NOT NULL,
        tieuDe NVARCHAR(120) NOT NULL,
        noiDung NVARCHAR(2000) NOT NULL,
        taoLuc DATETIME2 NOT NULL CONSTRAINT DF_TBKM_taoLuc DEFAULT SYSDATETIME(),
        CONSTRAINT FK_TBKM_KhachHang FOREIGN KEY(maKH) REFERENCES dbo.KhachHang(maKH)
    );
    CREATE INDEX IX_TBKM_KhachHang ON dbo.ThongBaoKhuyenMai(maKH,taoLuc DESC,id DESC);
END;
GO
-- Ghi nhật ký hóa đơn cũ nhưng KHÔNG cộng lại chi tiêu đã có.
INSERT INTO dbo.LichSuKhachHang(maKH,thoiGian,loai,noiDung,maHD)
SELECT h.maKH,h.ngayLap,N'Mua hàng',N'Hóa đơn '+h.maHD,h.maHD
FROM dbo.HoaDon h
WHERE h.maKH IS NOT NULL AND h.trangThai IN (N'Đã thanh toán',N'Trả góp')
AND NOT EXISTS(SELECT 1 FROM dbo.LichSuKhachHang l WHERE l.maHD=h.maHD);
GO
-- Hạng luôn theo chi tiêu, kể cả module khác cập nhật chi tiêu bằng SQL.
CREATE OR ALTER TRIGGER dbo.TRG_KH_TinhHang
ON dbo.KhachHang AFTER INSERT,UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(tongChiTieuTichLuy) RETURN;
    UPDATE k SET hangTV=(SELECT TOP 1 h.tenHang FROM dbo.HangThanhVien h
        WHERE h.chiTieuToiThieu<=k.tongChiTieuTichLuy ORDER BY h.thuTu DESC)
    FROM dbo.KhachHang k JOIN inserted i ON i.maKH=k.maKH;
END;
GO
UPDATE k SET hangTV=(SELECT TOP 1 h.tenHang FROM dbo.HangThanhVien h
    WHERE h.chiTieuToiThieu<=k.tongChiTieuTichLuy ORDER BY h.thuTu DESC)
FROM dbo.KhachHang k;
COMMIT TRANSACTION;
GO
