-- Tùy chọn: dữ liệu mẫu riêng Sản Phẩm / Danh Mục / Khách Hàng.
-- Database cũ: chạy 04_DieuChinhSanPhamKhachHang.sql trước file này.
/*USE QuanLyBanHang;
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    IF NOT EXISTS (SELECT 1 FROM dbo.DanhMuc WHERE maDanhMuc = 'DEMO_TL')
        INSERT INTO dbo.DanhMuc(maDanhMuc,tenDanhMuc) VALUES('DEMO_TL',N'Tủ lạnh demo');
    IF NOT EXISTS (SELECT 1 FROM dbo.DanhMuc WHERE maDanhMuc = 'DEMO_MG')
        INSERT INTO dbo.DanhMuc(maDanhMuc,tenDanhMuc) VALUES('DEMO_MG',N'Máy giặt demo');
    IF NOT EXISTS (SELECT 1 FROM dbo.SanPham WHERE maSKU = 'DEMO-TL001')
        INSERT INTO dbo.SanPham(maSKU,maVach,tenSP,donGia,thoiGianBaoHanh,thuongHieu,slTon,maDanhMuc,maKM)
        VALUES('DEMO-TL001',NULL,N'Tủ lạnh Samsung demo',10000000,24,N'Samsung',20,'DEMO_TL',NULL);
    IF NOT EXISTS (SELECT 1 FROM dbo.SanPham WHERE maSKU = 'DEMO-MG001')
        INSERT INTO dbo.SanPham(maSKU,maVach,tenSP,donGia,thoiGianBaoHanh,thuongHieu,slTon,maDanhMuc,maKM)
        VALUES('DEMO-MG001',NULL,N'Máy giặt LG demo',8000000,24,N'LG',20,'DEMO_MG',NULL);
    IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKH = 'DEMO_KH')
       AND NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE sdt = '0901234567')
        INSERT INTO dbo.KhachHang(maKH,tenKH,sdt)
        VALUES('DEMO_KH',N'Khách hàng demo','0901234567');
    COMMIT;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
END CATCH;
GO

