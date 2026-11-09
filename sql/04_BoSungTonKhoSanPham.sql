/* Migration riêng cho bảng SanPham trên database đã tạo trước đây. */
USE QuanLyBanHang;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF COL_LENGTH('dbo.SanPham', 'slTon') IS NULL
    ALTER TABLE dbo.SanPham ADD slTon INT NOT NULL
        CONSTRAINT DF_SP_slTon DEFAULT 0 WITH VALUES;

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE parent_object_id = OBJECT_ID('dbo.SanPham') AND name = 'CK_SP_slTon')
    ALTER TABLE dbo.SanPham ADD CONSTRAINT CK_SP_slTon CHECK (slTon >= 0);

-- Mã vạch có giá trị phải duy nhất; nhiều sản phẩm được phép chưa có mã vạch.
IF EXISTS (SELECT 1 FROM sys.key_constraints
           WHERE parent_object_id = OBJECT_ID('dbo.SanPham') AND name = 'UQ_SP_maVach')
    ALTER TABLE dbo.SanPham DROP CONSTRAINT UQ_SP_maVach;

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE object_id = OBJECT_ID('dbo.SanPham') AND name = 'UX_SanPham_maVach_NotNull')
    CREATE UNIQUE INDEX UX_SanPham_maVach_NotNull
        ON dbo.SanPham(maVach) WHERE maVach IS NOT NULL;

COMMIT TRANSACTION;
GO
