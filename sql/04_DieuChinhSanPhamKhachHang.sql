/*
   Migration schema Product/Customer cho database đã khởi tạo trước đây.
   Chạy sau QuanLyBanHang.sql và trước 03_DuLieuMau.sql nếu dùng schema cũ.
   Dữ liệu tồn kho cũ được đặt về 0 vì schema cũ không lưu số lượng.
*/
USE QuanLyBanHang;
GO
SET XACT_ABORT ON;
BEGIN TRY
BEGIN TRANSACTION;

IF COL_LENGTH('dbo.SanPham', 'slTon') IS NULL
    ALTER TABLE dbo.SanPham ADD slTon INT NOT NULL
        CONSTRAINT DF_SP_slTon DEFAULT 0 WITH VALUES;
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE parent_object_id = OBJECT_ID('dbo.SanPham') AND name = 'CK_SP_slTon')
    EXEC(N'ALTER TABLE dbo.SanPham ADD CONSTRAINT CK_SP_slTon CHECK (slTon >= 0);');

IF EXISTS (SELECT 1 FROM sys.key_constraints
           WHERE parent_object_id = OBJECT_ID('dbo.SanPham') AND name = 'UQ_SP_maVach')
    ALTER TABLE dbo.SanPham DROP CONSTRAINT UQ_SP_maVach;
IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE object_id = OBJECT_ID('dbo.SanPham') AND name = 'UX_SanPham_maVach_NotNull')
    CREATE UNIQUE INDEX UX_SanPham_maVach_NotNull
        ON dbo.SanPham(maVach) WHERE maVach IS NOT NULL;

IF EXISTS (SELECT 1 FROM sys.foreign_keys
           WHERE parent_object_id = OBJECT_ID('dbo.SanPham') AND name = 'FK_SP_KhuyenMai')
    ALTER TABLE dbo.SanPham DROP CONSTRAINT FK_SP_KhuyenMai;
ALTER TABLE dbo.SanPham ALTER COLUMN maKM VARCHAR(10) NULL;
ALTER TABLE dbo.SanPham ADD CONSTRAINT FK_SP_KhuyenMai
    FOREIGN KEY (maKM) REFERENCES dbo.KhuyenMai(maKM);

IF NOT EXISTS (SELECT 1 FROM sys.default_constraints
               WHERE parent_object_id = OBJECT_ID('dbo.SanPham') AND name = 'DF_SP_nhom')
    ALTER TABLE dbo.SanPham ADD CONSTRAINT DF_SP_nhom DEFAULT N'Gọn nhẹ' FOR nhomSanPham;
UPDATE dbo.SanPham SET nhomSanPham = N'Gọn nhẹ' WHERE nhomSanPham IS NULL;
ALTER TABLE dbo.SanPham ALTER COLUMN nhomSanPham NVARCHAR(100) NOT NULL;

-- Chặn tên danh mục trùng ở tầng database, kể cả khi hai máy lưu đồng thời.
IF EXISTS (SELECT tenDanhMuc FROM dbo.DanhMuc GROUP BY tenDanhMuc HAVING COUNT(*) > 1)
BEGIN
    THROW 51002, N'Có tên danh mục trùng. Cần hợp nhất hoặc đổi tên trước khi chạy migration.', 1;
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.DanhMuc') AND name = 'UX_DanhMuc_ten')
    CREATE UNIQUE INDEX UX_DanhMuc_ten ON dbo.DanhMuc(tenDanhMuc);

-- Chỉ cấp mã mới; không hạ sequence khi chạy lại migration.
DECLARE @nextCategory BIGINT = (SELECT COALESCE(MAX(TRY_CONVERT(BIGINT, SUBSTRING(maDanhMuc,3,20))),0)+1 FROM dbo.DanhMuc WHERE maDanhMuc LIKE 'DM%');
DECLARE @nextCustomer BIGINT = (SELECT COALESCE(MAX(TRY_CONVERT(BIGINT, SUBSTRING(maKH,3,20))),0)+1 FROM dbo.KhachHang WHERE maKH LIKE 'KH%');
DECLARE @sequenceDdl NVARCHAR(MAX);
IF OBJECT_ID('dbo.SeqMaDanhMuc', 'SO') IS NULL
BEGIN
    SET @sequenceDdl = N'CREATE SEQUENCE dbo.SeqMaDanhMuc AS BIGINT START WITH ' + CAST(@nextCategory AS NVARCHAR(20)) + N' INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE;';
    EXEC sys.sp_executesql @sequenceDdl;
END;
IF OBJECT_ID('dbo.SeqMaKhachHang', 'SO') IS NULL
BEGIN
    SET @sequenceDdl = N'CREATE SEQUENCE dbo.SeqMaKhachHang AS BIGINT START WITH ' + CAST(@nextCustomer AS NVARCHAR(20)) + N' INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE;';
    EXEC sys.sp_executesql @sequenceDdl;
END;

IF COL_LENGTH('dbo.KhachHang', 'ghiChu') IS NULL
    ALTER TABLE dbo.KhachHang ADD ghiChu NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.KhachHang', 'nhanKhuyenMai') IS NULL
    ALTER TABLE dbo.KhachHang ADD nhanKhuyenMai BIT NOT NULL
        CONSTRAINT DF_KH_nhanKM DEFAULT 0 WITH VALUES;
IF COL_LENGTH('dbo.KhachHang', 'ngayTao') IS NULL
    ALTER TABLE dbo.KhachHang ADD ngayTao DATETIME2(0) NULL;
-- Ngày tạo hồ sơ cũ không thể suy ra từ lúc chạy migration.
IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE parent_object_id = OBJECT_ID('dbo.KhachHang') AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('dbo.KhachHang'), 'ngayTao', 'ColumnId'))
    ALTER TABLE dbo.KhachHang ADD CONSTRAINT DF_KH_ngayTao DEFAULT SYSDATETIME() FOR ngayTao;
IF COL_LENGTH('dbo.KhachHang', 'ngayCapNhat') IS NULL
    ALTER TABLE dbo.KhachHang ADD ngayCapNhat DATETIME2(0) NOT NULL
        CONSTRAINT DF_KH_ngayCapNhat DEFAULT SYSDATETIME() WITH VALUES;

IF EXISTS (SELECT 1 FROM sys.check_constraints
           WHERE parent_object_id = OBJECT_ID('dbo.KhachHang') AND name = 'CK_KH_sdt')
    ALTER TABLE dbo.KhachHang DROP CONSTRAINT CK_KH_sdt;
IF EXISTS (SELECT 1 FROM dbo.KhachHang
           WHERE sdt NOT LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' OR LEN(sdt) <> 10)
BEGIN
    THROW 51001, N'Có số điện thoại khách hàng hiện tại không theo chuẩn 10 số bắt đầu bằng 0; cần chuẩn hóa dữ liệu trước khi chạy migration.', 1;
END;
ALTER TABLE dbo.KhachHang ADD CONSTRAINT CK_KH_sdt
    CHECK (sdt LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' AND LEN(sdt) = 10);

IF OBJECT_ID('dbo.LichSuKhachHang', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.LichSuKhachHang (
        maLichSu BIGINT IDENTITY(1,1) NOT NULL,
        maKH VARCHAR(10) NOT NULL,
        thoiGian DATETIME2(0) NOT NULL CONSTRAINT DF_LSKH_thoiGian DEFAULT SYSDATETIME(),
        hanhDong NVARCHAR(50) NOT NULL,
        noiDung NVARCHAR(500) NOT NULL,
        CONSTRAINT PK_LichSuKhachHang PRIMARY KEY (maLichSu),
        CONSTRAINT FK_LSKH_KhachHang FOREIGN KEY (maKH) REFERENCES dbo.KhachHang(maKH) ON DELETE CASCADE
    );
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE object_id = OBJECT_ID('dbo.LichSuKhachHang') AND name = 'IX_LichSuKhachHang_maKH_thoiGian')
    CREATE INDEX IX_LichSuKhachHang_maKH_thoiGian
        ON dbo.LichSuKhachHang(maKH, thoiGian DESC, maLichSu DESC);

EXEC(N'
CREATE OR ALTER TRIGGER dbo.TRG_KH_DongBoHangVaNgayCapNhat
ON dbo.KhachHang
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF TRIGGER_NESTLEVEL() > 1 RETURN;

    INSERT INTO dbo.LichSuKhachHang (maKH, hanhDong, noiDung)
    SELECT i.maKH,
        CASE
            WHEN d.maKH IS NULL THEN N''Tạo hồ sơ''
            WHEN i.nhanKhuyenMai <> d.nhanKhuyenMai THEN N''Đổi tùy chọn tiếp thị''
            WHEN i.trangThai <> d.trangThai THEN N''Đổi trạng thái''
            WHEN i.tongChiTieuTichLuy <> d.tongChiTieuTichLuy THEN N''Ghi nhận chi tiêu''
            WHEN i.hangTV <> d.hangTV THEN N''Đồng bộ hạng thành viên''
            ELSE N''Cập nhật hồ sơ''
        END,
        CASE
            WHEN d.maKH IS NULL THEN N''Hồ sơ khách hàng được tạo.''
            WHEN i.nhanKhuyenMai <> d.nhanKhuyenMai THEN N''Tùy chọn nhận thông tin khuyến mãi đã thay đổi.''
            WHEN i.trangThai <> d.trangThai THEN N''Trạng thái hoạt động đã thay đổi.''
            WHEN i.tongChiTieuTichLuy <> d.tongChiTieuTichLuy THEN N''Tổng chi tiêu tích lũy đã thay đổi.''
            WHEN i.hangTV <> d.hangTV THEN N''Hạng thành viên được đồng bộ theo chi tiêu.''
            ELSE N''Thông tin hồ sơ khách hàng đã được cập nhật.''
        END
    FROM inserted i
    LEFT JOIN deleted d ON d.maKH = i.maKH;

    UPDATE kh
    SET hangTV = CASE
            WHEN kh.tongChiTieuTichLuy >= 70000000 THEN N''Diamond''
            WHEN kh.tongChiTieuTichLuy >= 35000000 THEN N''Gold''
            WHEN kh.tongChiTieuTichLuy >= 15000000 THEN N''Silver''
            ELSE N''Member''
        END,
        ngayCapNhat = CASE WHEN EXISTS (SELECT 1 FROM deleted)
                           THEN SYSDATETIME() ELSE kh.ngayCapNhat END
    FROM dbo.KhachHang kh
    JOIN inserted i ON i.maKH = kh.maKH;
END;');

-- Chuẩn hóa hạng hiện tại theo tổng chi tiêu, không thay đổi số tiền đã tích lũy.
UPDATE kh
SET hangTV = CASE
        WHEN tongChiTieuTichLuy >= 70000000 THEN N'Diamond'
        WHEN tongChiTieuTichLuy >= 35000000 THEN N'Gold'
        WHEN tongChiTieuTichLuy >= 15000000 THEN N'Silver'
        ELSE N'Member'
    END
FROM dbo.KhachHang kh
WHERE hangTV <> CASE
        WHEN tongChiTieuTichLuy >= 70000000 THEN N'Diamond'
        WHEN tongChiTieuTichLuy >= 35000000 THEN N'Gold'
        WHEN tongChiTieuTichLuy >= 15000000 THEN N'Silver'
        ELSE N'Member'
    END;

COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
END CATCH;
GO
