-- TÙY CHỌN: dữ liệu demo cho database mới; không tạo hóa đơn hoặc sửa giá/tồn kho đã có.
USE QuanLyBanHang;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF NOT EXISTS(SELECT 1 FROM dbo.DanhMuc WHERE maDanhMuc='DM01')
    INSERT INTO dbo.DanhMuc(maDanhMuc,tenDanhMuc) VALUES('DM01',N'Tủ lạnh');
IF NOT EXISTS(SELECT 1 FROM dbo.DanhMuc WHERE maDanhMuc='DM02')
    INSERT INTO dbo.DanhMuc(maDanhMuc,tenDanhMuc) VALUES('DM02',N'Máy giặt');
IF NOT EXISTS(SELECT 1 FROM dbo.KhuyenMai WHERE maKM='KM01')
    INSERT INTO dbo.KhuyenMai(maKM,tenKM,giaTriGiam,loaiKM,thoiGianBatDau,thoiGianKetThuc,trangThai)
    VALUES('KM01',N'Khuyến mãi demo',0,N'Giảm tiền cố định',GETDATE(),DATEADD(YEAR,1,GETDATE()),N'Đang kích hoạt');
IF NOT EXISTS(SELECT 1 FROM dbo.SanPham WHERE maSKU='SP001')
    INSERT INTO dbo.SanPham(maSKU,maVach,tenSP,donGia,thoiGianBaoHanh,thuongHieu,slTon,maDanhMuc,maKM)
    VALUES('SP001','8930000000001',N'Tủ lạnh Samsung demo',10000000,24,N'Samsung',20,'DM01','KM01');
IF NOT EXISTS(SELECT 1 FROM dbo.SanPham WHERE maSKU='SP002')
    INSERT INTO dbo.SanPham(maSKU,maVach,tenSP,donGia,thoiGianBaoHanh,thuongHieu,slTon,maDanhMuc,maKM)
    VALUES('SP002','8930000000002',N'Máy giặt LG demo',8000000,24,N'LG',20,'DM02','KM01');
IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien WHERE maNV='NV001')
    INSERT INTO dbo.NhanVien(maNV,hoTen,chucVu,ngayVaoLam) VALUES('NV001',N'Nhân viên bán hàng demo',N'Bán hàng',CAST(GETDATE() AS DATE));
IF NOT EXISTS(SELECT 1 FROM dbo.KhachHang WHERE maKH='KH001') AND NOT EXISTS(SELECT 1 FROM dbo.KhachHang WHERE sdt='0901234567')
    INSERT INTO dbo.KhachHang(maKH,tenKH,sdt,hangTV) VALUES('KH001',N'Khách hàng demo','0901234567',N'Member');
IF NOT EXISTS(SELECT 1 FROM dbo.DanhMucCa WHERE maLoaiCa='SANG')
    INSERT INTO dbo.DanhMucCa(maLoaiCa,tenCa,gioBatDau,gioKetThuc) VALUES('SANG',N'Ca sáng','07:30','14:30');
DECLARE @today DATE=CAST(GETDATE() AS DATE);
DECLARE @shift VARCHAR(10)='CA'+CONVERT(CHAR(6),@today,12);
DECLARE @schedule VARCHAR(10)='LC'+CONVERT(CHAR(6),@today,12);
IF NOT EXISTS(SELECT 1 FROM dbo.CaLamViec WHERE maCa=@shift)
    INSERT INTO dbo.CaLamViec(maCa,thoiGianMoCa,trangThai,maLoaiCa,tongDoanhThu,tienQuyDauCa) VALUES(@shift,GETDATE(),N'Đang mở','SANG',0,0);
IF NOT EXISTS(SELECT 1 FROM dbo.LichLamViec WHERE maNV='NV001' AND ngayLamViec=@today)
    INSERT INTO dbo.LichLamViec(maLich,ngayLamViec,trangThai,maNV,maCa) VALUES(@schedule,@today,N'Đã phân công','NV001',@shift);
COMMIT;
GO
