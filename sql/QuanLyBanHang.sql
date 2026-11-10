-- Script khởi tạo database mới. Database đang dùng chỉ chạy migration 04 để nâng cấp Sản Phẩm/Khách Hàng.
-- Không chạy lại toàn bộ script khởi tạo trên database đã có bảng.
IF DB_ID(N'QuanLyBanHang') IS NULL
    CREATE DATABASE QuanLyBanHang;
GO
USE QuanLyBanHang;
GO

/* ==========================================================
   1. DANH MỤC SẢN PHẨM
   ========================================================== */
CREATE TABLE DanhMuc (
    maDanhMuc   VARCHAR(10)    NOT NULL,
    tenDanhMuc  NVARCHAR(100)  NOT NULL,
    moTa        NVARCHAR(255)  NULL,
    CONSTRAINT PK_DanhMuc PRIMARY KEY (maDanhMuc)
);

CREATE SEQUENCE dbo.SeqMaDanhMuc AS BIGINT START WITH 7 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE;
CREATE UNIQUE INDEX UX_DanhMuc_ten ON dbo.DanhMuc(tenDanhMuc);

/* ==========================================================
   2. KHUYẾN MÃI
   ========================================================== */
CREATE TABLE KhuyenMai (
    maKM            VARCHAR(10)    NOT NULL,
    tenKM           NVARCHAR(150)  NOT NULL,
    giaTriGiam      DECIMAL(18,2)  NOT NULL,
    loaiKM          NVARCHAR(50)   NULL,
    phamViApDung    NVARCHAR(100)  NULL,
    doiTuongApDung  NVARCHAR(100)  NULL,
    thoiGianBatDau  DATETIME       NOT NULL,
    thoiGianKetThuc DATETIME       NOT NULL,
    trangThai       NVARCHAR(30)   NOT NULL CONSTRAINT DF_KM_trangThai DEFAULT N'Chờ kích hoạt',
    CONSTRAINT PK_KhuyenMai         PRIMARY KEY (maKM),
    CONSTRAINT CK_KM_giaTriGiam     CHECK (giaTriGiam >= 0),
    CONSTRAINT CK_KM_thoiGian       CHECK (thoiGianKetThuc >= thoiGianBatDau),
    CONSTRAINT CK_KM_trangThai      CHECK (trangThai IN (N'Chờ kích hoạt', N'Đang kích hoạt', N'Đã kết thúc', N'Đã hủy')),
    CONSTRAINT CK_KM_loaiKM         CHECK (loaiKM IN (N'Giảm %', N'Giảm tiền cố định', N'Combo'))
);

/* ==========================================================
   3. SẢN PHẨM
   ========================================================== */
CREATE TABLE SanPham (
    maSKU           VARCHAR(20)    NOT NULL,
    maVach          VARCHAR(50)    NULL,
    tenSP           NVARCHAR(150)  NOT NULL,
    donGia          DECIMAL(18,0)  NOT NULL,
    thoiGianBaoHanh INT            NULL,
    slTon           INT            NOT NULL CONSTRAINT DF_SP_slTon DEFAULT 0,
    thuongHieu      NVARCHAR(100)  NOT NULL, 
    model           NVARCHAR(100)  NULL,
    moTa            NVARCHAR(500)  NULL,
    nhomSanPham     NVARCHAR(100)  NOT NULL CONSTRAINT DF_SP_nhom DEFAULT N'Gọn nhẹ',
    trangThai       BIT            NOT NULL CONSTRAINT DF_SP_trangThai DEFAULT 1,
    maDanhMuc       VARCHAR(10)    NOT NULL,
    maKM            VARCHAR(10)    NULL,
    CONSTRAINT PK_SanPham       PRIMARY KEY (maSKU),
    CONSTRAINT CK_SP_donGia     CHECK (donGia > 0),
    CONSTRAINT CK_SP_slTon      CHECK (slTon >= 0),
    CONSTRAINT CK_SP_baoHanh    CHECK (thoiGianBaoHanh >= 0),
    CONSTRAINT CK_SP_nhomSP     CHECK (nhomSanPham IN (N'Gọn nhẹ', N'Cồng kềnh')), 
    CONSTRAINT FK_SP_DanhMuc    FOREIGN KEY (maDanhMuc) REFERENCES DanhMuc(maDanhMuc),
    CONSTRAINT FK_SP_KhuyenMai  FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM)
);
-- Chỉ các barcode có giá trị phải duy nhất; sản phẩm chưa có barcode vẫn được thêm tự do.
CREATE UNIQUE INDEX UX_SanPham_maVach_NotNull ON SanPham(maVach) WHERE maVach IS NOT NULL;

/* ==========================================================
   4. DANH MỤC CA
   ========================================================== */
CREATE TABLE DanhMucCa (
    maLoaiCa    VARCHAR(10)    NOT NULL,
    tenCa       NVARCHAR(100)  NOT NULL,
    gioBatDau   TIME           NOT NULL,
    gioKetThuc  TIME           NOT NULL,
    CONSTRAINT PK_DanhMucCa PRIMARY KEY (maLoaiCa)
);

/* ==========================================================
   5. CA LÀM VIỆC
   ========================================================== */
CREATE TABLE CaLamViec (
    maCa            VARCHAR(10)    NOT NULL,
    thoiGianMoCa    DATETIME       NOT NULL,
    thoiGianDongCa  DATETIME       NULL,
    tienQuyDauCa    DECIMAL(18,0)  NULL,
    tongDoanhThu    DECIMAL(18,0)  NULL,
    tienMatThucTe   DECIMAL(18,0)  NULL,
    tienChenhLech   DECIMAL(18,0)  NULL,
    trangThai       NVARCHAR(30)   NULL,
    CONSTRAINT PK_CaLamViec PRIMARY KEY (maCa)
);

/* ==========================================================
   6. NHÂN VIÊN
   ========================================================== */
CREATE TABLE NhanVien (
    maNV        VARCHAR(10)    NOT NULL,
    hoTen       NVARCHAR(100)  NOT NULL,
    sdt         VARCHAR(15)    NULL,
    email       VARCHAR(100)   NULL,
    chucVu      NVARCHAR(50)   NULL,
    ngaySinh    DATE           NULL,
    gioiTinh    BIT            NULL,
    diaChi      NVARCHAR(200)  NULL,
    ngayVaoLam  DATE           NULL,
    trangThai   BIT            NOT NULL CONSTRAINT DF_NV_trangThai DEFAULT 1,
    CONSTRAINT PK_NhanVien    PRIMARY KEY (maNV),
    CONSTRAINT CK_NV_chucVu   CHECK (chucVu IN (N'Quản lý', N'Bán hàng', N'Chăm sóc khách hàng'))
);

/* ==========================================================
   7. TÀI KHOẢN
   ========================================================== */
CREATE TABLE TaiKhoan (
    maTK        VARCHAR(10)    NOT NULL,
    username    VARCHAR(50)    NOT NULL,
    [password]  VARCHAR(255)   NOT NULL,
    quyen       NVARCHAR(30)   NOT NULL,
    trangThai   NVARCHAR(30)   NULL,
    maNV        VARCHAR(10)    NOT NULL,
    CONSTRAINT PK_TaiKhoan         PRIMARY KEY (maTK),
    CONSTRAINT UQ_TK_username      UNIQUE (username),
    CONSTRAINT UQ_TK_maNV          UNIQUE (maNV),
    CONSTRAINT CK_TK_username_maNV CHECK (username = maNV),
    CONSTRAINT FK_TK_NhanVien      FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

/* ==========================================================
   8. LỊCH LÀM VIỆC
   ========================================================== */
CREATE TABLE LichLamViec (
    maLich       VARCHAR(10)    NOT NULL,
    ngayLamViec  DATE           NOT NULL,
    trangThai    NVARCHAR(30)   NULL,
    maNV         VARCHAR(10)    NOT NULL,
    maLoaiCa     VARCHAR(10)    NOT NULL,
    CONSTRAINT PK_LichLamViec      PRIMARY KEY (maLich),
    CONSTRAINT FK_LLV_NhanVien     FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_LLV_DanhMucCa    FOREIGN KEY (maLoaiCa) REFERENCES DanhMucCa(maLoaiCa)
);

/* ==========================================================
   9. KHÁCH HÀNG
   ========================================================== */
CREATE TABLE KhachHang (
    maKH                VARCHAR(10)    NOT NULL,
    tenKH               NVARCHAR(100)  NOT NULL,
    sdt                 VARCHAR(15)    NOT NULL, 
    email               VARCHAR(100)   NULL,
    diaChi              NVARCHAR(200)  NULL,
    ngaySinh            DATE           NULL,
    gioiTinh            BIT            NULL,
    hangTV              NVARCHAR(30)   NOT NULL CONSTRAINT DF_KH_hangTV DEFAULT N'Member', 
    tongChiTieuTichLuy  DECIMAL(18,0)  NOT NULL CONSTRAINT DF_KH_tctl DEFAULT 0,
    trangThai           BIT            NOT NULL CONSTRAINT DF_KH_trangThai DEFAULT 1,
    ghiChu              NVARCHAR(500)  NULL,
    nhanKhuyenMai       BIT            NOT NULL CONSTRAINT DF_KH_nhanKM DEFAULT 0,
    ngayTao             DATETIME2(0)   NOT NULL CONSTRAINT DF_KH_ngayTao DEFAULT SYSDATETIME(),
    ngayCapNhat         DATETIME2(0)   NOT NULL CONSTRAINT DF_KH_ngayCapNhat DEFAULT SYSDATETIME(),
    CONSTRAINT PK_KhachHang        PRIMARY KEY (maKH),
    CONSTRAINT UQ_KH_sdt           UNIQUE (sdt),
    CONSTRAINT CK_KH_sdt           CHECK (sdt LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' AND LEN(sdt) = 10),
    CONSTRAINT CK_KH_tctl          CHECK (tongChiTieuTichLuy >= 0),
    CONSTRAINT CK_KH_hangTV        CHECK (hangTV IN (N'Member', N'Silver', N'Gold', N'Diamond'))
);

CREATE SEQUENCE dbo.SeqMaKhachHang AS BIGINT START WITH 11 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE;

CREATE TABLE LichSuKhachHang (
    maLichSu  BIGINT IDENTITY(1,1) NOT NULL,
    maKH      VARCHAR(10)          NOT NULL,
    thoiGian  DATETIME2(0)         NOT NULL CONSTRAINT DF_LSKH_thoiGian DEFAULT SYSDATETIME(),
    hanhDong  NVARCHAR(50)         NOT NULL,
    noiDung   NVARCHAR(500)        NOT NULL,
    CONSTRAINT PK_LichSuKhachHang PRIMARY KEY (maLichSu),
    CONSTRAINT FK_LSKH_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH) ON DELETE CASCADE
);
CREATE INDEX IX_LichSuKhachHang_maKH_thoiGian
    ON LichSuKhachHang(maKH, thoiGian DESC, maLichSu DESC);

/* ==========================================================
   10. HÓA ĐƠN
   ========================================================== */
CREATE TABLE HoaDon (
    maHD                 VARCHAR(12)    NOT NULL,
    ngayLap              DATETIME       NOT NULL CONSTRAINT DF_HD_ngayLap DEFAULT GETDATE(),
    tongTienHang         DECIMAL(18,0)  NOT NULL CONSTRAINT DF_HD_tth DEFAULT 0,
    tienGiamGia          DECIMAL(18,0)  NOT NULL CONSTRAINT DF_HD_tgg DEFAULT 0,
    tongThanhToan        DECIMAL(18,0)  NOT NULL CONSTRAINT DF_HD_ttt DEFAULT 0,
    phuongThucThanhToan  NVARCHAR(50)   NULL,
    yeuCauGiaoHang       BIT            NOT NULL CONSTRAINT DF_HD_GiaoHang DEFAULT 0, 
    ghiChuGiaoHang       NVARCHAR(500)  NULL, 
    trangThai            NVARCHAR(30)   NULL,
    maNV                 VARCHAR(10)    NOT NULL,
    maKH                 VARCHAR(10)    NULL,
    maKM                 VARCHAR(10)    NULL,
    maCa                 VARCHAR(10)    NULL,
    CONSTRAINT PK_HoaDon             PRIMARY KEY (maHD),
    CONSTRAINT CK_HD_maHD_format     CHECK (LEN(maHD) = 12 AND maHD LIKE 'HD[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_HD_tongTienHang    CHECK (tongTienHang >= 0),
    CONSTRAINT CK_HD_tienGiamGia     CHECK (tienGiamGia >= 0 AND tienGiamGia <= 0.7 * tongTienHang),
    CONSTRAINT CK_HD_tongThanhToan   CHECK (tongThanhToan >= 0),
    CONSTRAINT CK_HD_PTTT            CHECK (phuongThucThanhToan IN (N'Tiền mặt', N'Chuyển khoản', N'Trả góp')), 
    CONSTRAINT FK_HD_NhanVien        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HD_KhachHang       FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HD_KhuyenMai       FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    CONSTRAINT FK_HD_CaLamViec       FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa)
);

/* ==========================================================
   11. CHI TIẾT HÓA ĐƠN
   ========================================================== */
CREATE TABLE ChiTietHoaDon (
    maHD       VARCHAR(12)    NOT NULL,
    maSKU      VARCHAR(20)    NOT NULL,
    soLuong    INT            NOT NULL,
    donGia     DECIMAL(18,0)  NOT NULL CONSTRAINT DF_CTHD_donGia DEFAULT 0,
    thanhTien  DECIMAL(18,0)  NOT NULL CONSTRAINT DF_CTHD_thanhTien DEFAULT 0,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHD, maSKU),
    CONSTRAINT CK_CTHD_soLuong  CHECK (soLuong > 0),
    CONSTRAINT CK_CTHD_donGia   CHECK (donGia >= 0),
    CONSTRAINT FK_CTHD_HoaDon   FOREIGN KEY (maHD)  REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_SanPham  FOREIGN KEY (maSKU) REFERENCES SanPham(maSKU)
);

/* ==========================================================
   12. PHIẾU BẢO HÀNH
   ========================================================== */
CREATE TABLE PhieuBaoHanh (
    maPhieu        VARCHAR(10)    NOT NULL,
    soSerial       VARCHAR(50)    NULL,
    ngayTiepNhan   DATE           NOT NULL,
    ngayHenTra     DATE           NULL,
    moTa           NVARCHAR(255)  NULL,
    chiPhiSuaChua  DECIMAL(18,0)  NULL,
    trangThai      NVARCHAR(30)   NULL,
    maHD           VARCHAR(12)    NOT NULL,
    maSKU          VARCHAR(20)    NULL,
    maKH           VARCHAR(10)    NULL,
    CONSTRAINT PK_PhieuBaoHanh        PRIMARY KEY (maPhieu),
    CONSTRAINT FK_PBH_ChiTietHoaDon   FOREIGN KEY (maHD, maSKU) REFERENCES ChiTietHoaDon(maHD, maSKU),
    CONSTRAINT FK_PBH_KhachHang       FOREIGN KEY (maKH)  REFERENCES KhachHang(maKH)
);

/* ==========================================================
   13. GIAO DỊCH TRẢ GÓP
   ========================================================== */
CREATE TABLE GiaoDichTraGop (
    maTraGop        VARCHAR(10)    NOT NULL,
    doiTacTaiChinh  NVARCHAR(100)  NULL,
    kyHan           INT            NOT NULL,
    tienTraTruoc    DECIMAL(18,0)  NOT NULL CONSTRAINT DF_GDTG_ttr DEFAULT 0,
    tienGopMoiThang DECIMAL(18,0)  NOT NULL,
    soTienConLai    DECIMAL(18,0)  NOT NULL,
    trangThai       NVARCHAR(30)   NULL,
    maHD            VARCHAR(12)    NOT NULL,
    CONSTRAINT PK_GiaoDichTraGop    PRIMARY KEY (maTraGop),
    CONSTRAINT UQ_GDTG_maHD         UNIQUE (maHD),
    CONSTRAINT CK_GDTG_kyHan        CHECK (kyHan IN (6, 9, 12, 18, 24)),
    CONSTRAINT CK_GDTG_tienTraTruoc CHECK (tienTraTruoc >= 0),
    CONSTRAINT FK_GDTG_HoaDon       FOREIGN KEY (maHD) REFERENCES HoaDon(maHD)
);
GO

/* ==========================================================
   14. CÁC TRIGGER RÀNG BUỘC NGHIỆP VỤ
   ========================================================== */

/* --- TR1: Lịch làm việc – ngayLamViec >= ngày hiện tại --- */
CREATE TRIGGER TRG_LLV_NgayTuongLai
ON LichLamViec
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM inserted
        WHERE ngayLamViec < CAST(GETDATE() AS DATE)
    )
    BEGIN
        RAISERROR(N'Ngày phân công phải >= ngày hiện tại.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

/* --- TR2: Phiếu bảo hành – ngayTiepNhan >= HoaDon.ngayLap --- */
CREATE TRIGGER TRG_PBH_NgayTiepNhan
ON PhieuBaoHanh
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN HoaDon h ON i.maHD = h.maHD
        WHERE i.ngayTiepNhan < CAST(h.ngayLap AS DATE)
    )
    BEGIN
        RAISERROR(N'Ngày tiếp nhận bảo hành phải >= ngày lập hóa đơn.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

/* --- TR3: Hóa đơn – NV chỉ lập HĐ khi ca đã "Mở" --- */
CREATE TRIGGER TRG_HD_CaDaMoCa
ON HoaDon
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN CaLamViec c ON i.maCa = c.maCa
        WHERE c.trangThai <> N'Đang mở'
    )
    BEGIN
        RAISERROR(N'Chỉ được lập hóa đơn khi ca làm việc đang "Đang mở".', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

/* --- TR4: Chi tiết HĐ – Không cho thêm SP "Ngừng kinh doanh" --- */
CREATE TRIGGER TRG_CTHD_SPConKinhDoanh
ON ChiTietHoaDon
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN SanPham sp ON i.maSKU = sp.maSKU
        WHERE sp.trangThai = 0  
    )
    BEGIN
        RAISERROR(N'Không được thêm sản phẩm đang "Ngừng kinh doanh" vào hóa đơn.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

/* --- TR5: Lịch làm việc – không xếp 2 ca trùng giờ cùng ngày cho 1 NV --- */
CREATE TRIGGER TRG_LLV_TrungCa
ON LichLamViec
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN LichLamViec llv ON i.maNV = llv.maNV
                            AND i.ngayLamViec = llv.ngayLamViec
                            AND i.maLich <> llv.maLich
        JOIN DanhMucCa d1 ON i.maLoaiCa = d1.maLoaiCa
        JOIN DanhMucCa d2 ON llv.maLoaiCa = d2.maLoaiCa
        WHERE d1.gioBatDau < d2.gioKetThuc
          AND d2.gioBatDau < d1.gioKetThuc
    )
    BEGIN
        RAISERROR(N'Nhân viên đã có ca làm việc trùng thời gian trong ngày này.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

/* --- Khách hàng: đồng bộ hạng từ chi tiêu và ghi nhận lần cập nhật --- */
CREATE TRIGGER TRG_KH_DongBoHangVaNgayCapNhat
ON KhachHang
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF TRIGGER_NESTLEVEL() > 1 RETURN;

    INSERT INTO LichSuKhachHang (maKH, hanhDong, noiDung)
    SELECT i.maKH,
        CASE
            WHEN d.maKH IS NULL THEN N'Tạo hồ sơ'
            WHEN i.nhanKhuyenMai <> d.nhanKhuyenMai THEN N'Đổi tùy chọn tiếp thị'
            WHEN i.trangThai <> d.trangThai THEN N'Đổi trạng thái'
            WHEN i.tongChiTieuTichLuy <> d.tongChiTieuTichLuy THEN N'Ghi nhận chi tiêu'
            WHEN i.hangTV <> d.hangTV THEN N'Đồng bộ hạng thành viên'
            ELSE N'Cập nhật hồ sơ'
        END,
        CASE
            WHEN d.maKH IS NULL THEN N'Hồ sơ khách hàng được tạo.'
            WHEN i.nhanKhuyenMai <> d.nhanKhuyenMai THEN N'Tùy chọn nhận thông tin khuyến mãi đã thay đổi.'
            WHEN i.trangThai <> d.trangThai THEN N'Trạng thái hoạt động đã thay đổi.'
            WHEN i.tongChiTieuTichLuy <> d.tongChiTieuTichLuy THEN N'Tổng chi tiêu tích lũy đã thay đổi.'
            WHEN i.hangTV <> d.hangTV THEN N'Hạng thành viên được đồng bộ theo chi tiêu.'
            ELSE N'Thông tin hồ sơ khách hàng đã được cập nhật.'
        END
    FROM inserted i
    LEFT JOIN deleted d ON d.maKH = i.maKH;

    UPDATE kh
    SET hangTV = CASE
            WHEN kh.tongChiTieuTichLuy >= 70000000 THEN N'Diamond'
            WHEN kh.tongChiTieuTichLuy >= 35000000 THEN N'Gold'
            WHEN kh.tongChiTieuTichLuy >= 15000000 THEN N'Silver'
            ELSE N'Member'
        END,
        ngayCapNhat = CASE WHEN EXISTS (SELECT 1 FROM deleted)
                           THEN SYSDATETIME() ELSE kh.ngayCapNhat END
    FROM KhachHang kh
    JOIN inserted i ON i.maKH = kh.maKH;
END;
GO


USE QuanLyBanHang;
GO

-- Dữ liệu mẫu dành cho database mới, không xóa dữ liệu đang có.
-- Database đang dùng: chỉ chạy 04_DieuChinhSanPhamKhachHang.sql để nâng cấp hai mục.
/* ==========================================================
   1. DANH MỤC SẢN PHẨM (6 dòng)
   ========================================================== */
INSERT INTO DanhMuc (maDanhMuc, tenDanhMuc, moTa) VALUES
('DM01', N'Laptop & Máy tính xách tay', N'Các dòng laptop văn phòng, gaming, đồ họa'),
('DM02', N'Điện thoại & Phụ kiện', N'Smartphone chính hãng và các phụ kiện đi kèm'),
('DM03', N'Thiết bị âm thanh', N'Tai nghe, loa bluetooth, soundbar cao cấp'),
('DM04', N'Màn hình & Thiết bị văn phòng', N'Màn hình máy tính, máy in, máy chiếu'),
('DM05', N'Đồng hồ thông minh & Thiết bị đeo', N'Smartwatch thể thao và thời trang'),
('DM06', N'Thiết bị mạng & Router', N'Router Wifi, switch, thiết bị phát sóng');

/* ==========================================================
   2. KHUYẾN MÃI (5 dòng)
   ========================================================== */
INSERT INTO KhuyenMai (maKM, tenKM, giaTriGiam, loaiKM, phamViApDung, doiTuongApDung, thoiGianBatDau, thoiGianKetThuc, trangThai) VALUES
('KM01', N'Khai trương cửa hàng mới - Giảm sốc', 1000000, N'Giảm tiền cố định', N'Toàn hệ thống', N'Tất cả khách hàng', '2026-01-01 00:00:00', '2026-12-31 23:59:59', N'Đang kích hoạt'),
('KM02', N'Chào hè rực rỡ - Giảm giá phụ kiện', 15, N'Giảm %', N'Danh mục Phụ kiện', N'Thành viên Silver trở lên', '2026-03-01 00:00:00', '2026-06-30 23:59:59', N'Đang kích hoạt'),
('KM03', N'Tri ân khách hàng VIP Diamond', 500000, N'Giảm tiền cố định', N'Toàn hệ thống', N'Hạng Diamond', '2026-05-01 00:00:00', '2026-12-31 23:59:59', N'Chờ kích hoạt'),
('KM04', N'Mùa tựu trường rộn ràng', 2000000, N'Giảm tiền cố định', N'Danh mục Laptop', N'Học sinh - Sinh viên', '2026-08-01 00:00:00', '2026-09-30 23:59:59', N'Chờ kích hoạt'),
('KM05', N'Flash Sale Cuối Tuần', 10, N'Giảm %', N'Toàn hệ thống', N'Tất cả khách hàng', '2026-06-01 00:00:00', '2026-12-31 23:59:59', N'Đang kích hoạt');

/* ==========================================================
   3. SẢN PHẨM (10 dòng)
   ========================================================== */
INSERT INTO SanPham (maSKU, maVach, tenSP, donGia, thoiGianBaoHanh, thuongHieu, model, moTa, nhomSanPham, trangThai, slTon, maDanhMuc, maKM) VALUES
('SKU001', '8935001230011', N'Laptop ASUS ROG Strix G15', 28990000, 24, N'ASUS', 'G15 G513', N'Laptop Gaming mạnh mẽ, RTX 3060', N'Cồng kềnh', 1, 10, 'DM01', 'KM01'),
('SKU002', '8935001230028', N'Laptop MacBook Air M2 2022', 26990000, 12, N'Apple', 'MacBook Air M2', N'Siêu mỏng nhẹ, pin 18 tiếng', N'Gọn nhẹ', 1, 10, 'DM01', NULL),
('SKU003', '8935001230035', N'Điện thoại iPhone 15 Pro Max 256GB', 33990000, 12, N'Apple', 'iPhone 15 Pro Max', N'Titan tự nhiên, chip A17 Pro', N'Gọn nhẹ', 1, 10, 'DM02', 'KM01'),
('SKU004', '8935001230042', N'Tai nghe Sony WH-1000XM5', 7490000, 12, N'Sony', 'WH-1000XM5', N'Chống ồn chủ động đỉnh cao', N'Gọn nhẹ', 1, 10, 'DM03', 'KM02'),
('SKU005', '8935001230059', N'Màn hình Dell UltraSharp U2723QE', 13990000, 36, N'Dell', 'U2723QE', N'Màn hình đồ họa 4K IPS Black', N'Cồng kềnh', 0, 10, 'DM04', NULL),
('SKU006', '8935001230066', N'Đồng hồ Apple Watch Series 9', 10490000, 12, N'Apple', 'Series 9 GPS', N'Màn hình sáng hơn, chip S9', N'Gọn nhẹ', 1, 10, 'DM05', 'KM02'),
('SKU007', '8935001230073', N'Router Wifi 6 ASUS RT-AX58U', 2490000, 24, N'ASUS', 'RT-AX58U', N'Tốc độ cao, phủ sóng rộng', N'Gọn nhẹ', 1, 10, 'DM06', 'KM05'),
('SKU008', '8935001230080', N'Loa Bluetooth JBL Charge 5', 3690000, 12, N'JBL', 'Charge 5', N'Chống nước IP67, pin 20 giờ', N'Gọn nhẹ', 1, 10, 'DM03', NULL),
('SKU009', '8935001230097', N'Điện thoại Samsung Galaxy S24 Ultra', 29990000, 12, N'Samsung', 'S24 Ultra', N'Tích hợp AI thông minh', N'Gọn nhẹ', 1, 10, 'DM02', 'KM01'),
('SKU010', '8935001230103', N'Laptop Lenovo Legion 5 Pro', 31990000, 24, N'Lenovo', 'Legion 5 Pro', N'Hiệu năng khủng cho game thủ', N'Cồng kềnh', 1, 10, 'DM01', NULL);

/* ==========================================================
   4. DANH MỤC CA (4 dòng)
   ========================================================== */
INSERT INTO DanhMucCa (maLoaiCa, tenCa, gioBatDau, gioKetThuc) VALUES
('CA_SANG', N'Ca Sáng', '08:00:00', '12:00:00'),
('CA_CHIEU', N'Ca Chiều', '13:00:00', '17:00:00'),
('CA_TOI', N'Ca Tối', '18:00:00', '22:00:00'),
('CA_DEM', N'Ca Đêm', '22:00:00', '02:00:00');

/* ==========================================================
   5. CA LÀM VIỆC (6 dòng - Đảm bảo có ca đang mở cho hóa đơn)
   ========================================================== */
INSERT INTO CaLamViec (maCa, thoiGianMoCa, thoiGianDongCa, tienQuyDauCa, tongDoanhThu, tienMatThucTe, tienChenhLech, trangThai) VALUES
('CA2026060601', '2026-06-06 08:00:00', NULL, 2000000, 0, NULL, NULL, N'Đang mở'),
('CA2026060602', '2026-06-06 13:00:00', '2026-06-06 17:00:00', 2000000, 15000000, 15000000, 0, N'Đã đóng'),
('CA2026060701', '2026-06-07 08:00:00', NULL, 2000000, 0, NULL, NULL, N'Đóng'),
('CA2026060702', '2026-06-07 13:00:00', NULL, 2000000, 0, NULL, NULL, N'Đóng'),
('CA2026060801', '2026-06-08 08:00:00', NULL, 2000000, 0, NULL, NULL, N'Đóng'),
('CA2026060802', '2026-06-08 13:00:00', NULL, 2000000, 0, NULL, NULL, N'Đóng');

/* ==========================================================
   6. NHÂN VIÊN (8 dòng)
   ========================================================== */
INSERT INTO NhanVien (maNV, hoTen, sdt, email, chucVu, ngaySinh, gioiTinh, diaChi, ngayVaoLam, trangThai) VALUES
('NV001', N'Nguyễn Văn Quản Lý', '0901112233', 'quanly@shop.vn', N'Quản lý', '1985-05-12', 1, N'123 Nguyễn Trãi, Q.5, TP.HCM', '2022-01-10', 1),
('NV002', N'Trần Thị Bán Hàng', '0904445566', 'banhang@shop.vn', N'Bán hàng', '1998-08-20', 0, N'456 Lê Văn Việt, TP. Thủ Đức', '2023-03-15', 1),
('NV003', N'Lê Văn Chăm Sóc', '0907778899', 'cskh@shop.vn', N'Chăm sóc khách hàng', '2000-11-05', 1, N'789 Cách Mạng Tháng 8, Q.10', '2024-06-01', 1),
('NV004', N'Phạm Minh Tuấn', '0903332211', 'tuan.pham@shop.vn', N'Bán hàng', '1999-02-14', 1, N'12 Hoàng Văn Thụ, Tân Bình', '2023-05-20', 1),
('NV005', N'Hoàng Thu Hằng', '0908889900', 'hang.hoang@shop.vn', N'Bán hàng', '2001-07-19', 0, N'55 Quang Trung, Gò Vấp', '2024-01-15', 1),
('NV006', N'Ngô Văn Thành', '0905556677', 'thanh.ngo@shop.vn', N'Chăm sóc khách hàng', '1997-12-03', 1, N'88 Tôn Đức Thắng, Q.1', '2022-11-10', 1),
('NV007', N'Vũ Thị Lan', '0902223344', 'lan.vu@shop.vn', N'Bán hàng', '2000-04-25', 0, N'30 Nguyễn Thị Minh Khai, Q.3', '2024-03-01', 1),
('NV008', N'Đặng Quốc Bảo', '0909998877', 'bao.dang@shop.vn', N'Quản lý', '1988-09-09', 1, N'99 Phan Xích Long, Phú Nhuận', '2021-06-15', 1);

/* ==========================================================
   7. TÀI KHOẢN (8 dòng - Username trùng maNV)
   ========================================================== */
INSERT INTO TaiKhoan (maTK, username, [password], quyen, trangThai, maNV) VALUES
('TK001', 'NV001', '123456', N'Admin', N'Hoạt động', 'NV001'),
('TK002', 'NV002', '123456', N'Staff', N'Hoạt động', 'NV002'),
('TK003', 'NV003', '123456', N'Staff', N'Hoạt động', 'NV003'),
('TK004', 'NV004', '123456', N'Staff', N'Hoạt động', 'NV004'),
('TK005', 'NV005', '123456', N'Staff', N'Hoạt động', 'NV005'),
('TK006', 'NV006', '123456', N'Staff', N'Hoạt động', 'NV006'),
('TK007', 'NV007', '123456', N'Staff', N'Hoạt động', 'NV007'),
('TK008', 'NV008', '123456', N'Admin', N'Hoạt động', 'NV008');

/* ==========================================================
   8. LỊCH LÀM VIỆC (8 dòng - Ngày tương lai từ 2026-06-06 trở đi)
   ========================================================== */
INSERT INTO LichLamViec (maLich, ngayLamViec, trangThai, maNV, maLoaiCa) VALUES
('LICH01', '2026-06-06', N'Đã phân công', 'NV002', 'CA_SANG'),
('LICH02', '2026-06-06', N'Đã phân công', 'NV003', 'CA_CHIEU'),
('LICH03', '2026-06-06', N'Đã phân công', 'NV004', 'CA_SANG'),
('LICH04', '2026-06-07', N'Đã phân công', 'NV002', 'CA_CHIEU'),
('LICH05', '2026-06-07', N'Đã phân công', 'NV005', 'CA_SANG'),
('LICH06', '2026-06-08', N'Đã phân công', 'NV003', 'CA_SANG'),
('LICH07', '2026-06-08', N'Đã phân công', 'NV006', 'CA_CHIEU'),
('LICH08', '2026-06-08', N'Đã phân công', 'NV004', 'CA_TOI');

/* ==========================================================
   9. KHÁCH HÀNG (10 dòng)
   ========================================================== */
INSERT INTO KhachHang (maKH, tenKH, sdt, email, diaChi, ngaySinh, gioiTinh, hangTV, tongChiTieuTichLuy, trangThai) VALUES
('KH001', N'Phạm Hoàng Long', '0988123456', 'long.pham@gmail.com', N'12 Điện Biên Phủ, Bình Thạnh', '1995-02-15', 1, N'Gold', 35000000, 1),
('KH002', N'Hoàng Thị Mai', '0977654321', 'mai.hoang@gmail.com', N'88 Hai Bà Trưng, Quận 1', '1992-09-10', 0, N'Diamond', 75000000, 1),
('KH003', N'Đỗ Minh Quân', '0912345678', 'quan.do@gmail.com', N'15 Nguyễn Kiệm, Gò Vấp', '2001-12-01', 1, N'Member', 5000000, 1),
('KH004', N'Nguyễn Thị Ngọc', '0933111222', 'ngoc.nguyen@gmail.com', N'45 Lê Duẩn, Quận 1', '1996-04-12', 0, N'Silver', 15000000, 1),
('KH005', N'Trần Văn Hùng', '0905554433', 'hung.tran@gmail.com', N'102 CMT8, Quận 3', '1989-11-20', 1, N'Member', 2000000, 1),
('KH006', N'Lê Hoàng Nam', '0966778899', 'nam.le@gmail.com', N'77 Võ Văn Ngân, Thủ Đức', '1994-06-18', 1, N'Gold', 42000000, 1),
('KH007', N'Bùi Thị Lan Chi', '0944332211', 'chi.bui@gmail.com', N'33 Nguyễn Trãi, Quận 5', '1998-03-05', 0, N'Diamond', 88000000, 1),
('KH008', N'Phan Minh Triết', '0922114455', 'triet.phan@gmail.com', N'50 Lê Văn Sỹ, Phú Nhuận', '2002-08-14', 1, N'Member', 4500000, 1),
('KH009', N'Vũ Thanh Hằng', '0911998877', 'hang.vu@gmail.com', N'19 Thành Thái, Quận 10', '1993-10-30', 0, N'Silver', 18000000, 1),
('KH010', N'Trương Văn Đức', '0955667788', 'duc.truong@gmail.com', N'68 Đinh Tiên Hoàng, Quận 1', '1990-01-22', 1, N'Member', 3000000, 1);

/* ==========================================================
   10. HÓA ĐƠN (10 dòng - Trùng ca đang mở 'CA2026060601')
   ========================================================== */
INSERT INTO HoaDon (maHD, ngayLap, tongTienHang, tienGiamGia, tongThanhToan, phuongThucThanhToan, yeuCauGiaoHang, ghiChuGiaoHang, trangThai, maNV, maKH, maKM, maCa) VALUES
('HD0000000001', '2026-06-06 09:30:00', 33990000, 1000000, 32990000, N'Chuyển khoản', 1, N'Giao giờ hành chính', N'Đã thanh toán', 'NV002', 'KH001', 'KM01', 'CA2026060601'),
('HD0000000002', '2026-06-06 10:15:00', 28990000, 1000000, 27990000, N'Trả góp', 0, NULL, N'Trả góp', 'NV002', 'KH002', 'KM01', 'CA2026060601'),
('HD0000000003', '2026-06-06 10:45:00', 26990000, 1000000, 25990000, N'Tiền mặt', 1, N'Gọi trước khi giao', N'Đã thanh toán', 'NV004', 'KH003', 'KM01', 'CA2026060601'),
('HD0000000004', '2026-06-06 11:00:00', 7490000, 112350, 7377650, N'Chuyển khoản', 0, NULL, N'Đã thanh toán', 'NV004', 'KH004', 'KM02', 'CA2026060601'),
('HD0000000005', '2026-06-06 11:20:00', 10490000, 157350, 10332650, N'Tiền mặt', 0, NULL, N'Đã thanh toán', 'NV002', 'KH005', 'KM02', 'CA2026060601'),
('HD0000000006', '2026-06-06 11:35:00', 2490000, 0, 2490000, N'Tiền mặt', 0, NULL, N'Đã thanh toán', 'NV005', 'KH006', 'KM05', 'CA2026060601'),
('HD0000000007', '2026-06-06 11:50:00', 3690000, 0, 3690000, N'Chuyển khoản', 1, N'Giao hàng cẩn thận', N'Đã thanh toán', 'NV005', 'KH007', 'KM05', 'CA2026060601'),
('HD0000000008', '2026-06-06 12:00:00', 29990000, 1000000, 28990000, N'Trả góp', 0, NULL, N'Trả góp', 'NV002', 'KH008', 'KM01', 'CA2026060601'),
('HD0000000009', '2026-06-06 12:15:00', 31990000, 2000000, 29990000, N'Chuyển khoản', 1, N'Giao lầu 2', N'Đã thanh toán', 'NV004', 'KH009', 'KM04', 'CA2026060601'),
('HD0000000010', '2026-06-06 12:30:00', 7490000, 112350, 7377650, N'Tiền mặt', 0, NULL, N'Đã thanh toán', 'NV005', 'KH010', 'KM02', 'CA2026060601');

/* ==========================================================
   11. CHI TIẾT HÓA ĐƠN (12 dòng)
   ========================================================== */
INSERT INTO ChiTietHoaDon (maHD, maSKU, soLuong, donGia, thanhTien) VALUES
('HD0000000001', 'SKU003', 1, 33990000, 33990000),
('HD0000000002', 'SKU001', 1, 28990000, 28990000),
('HD0000000003', 'SKU002', 1, 26990000, 26990000),
('HD0000000004', 'SKU004', 1, 7490000, 7490000),
('HD0000000005', 'SKU006', 1, 10490000, 10490000),
('HD0000000006', 'SKU007', 1, 2490000, 2490000),
('HD0000000007', 'SKU008', 1, 3690000, 3690000),
('HD0000000008', 'SKU009', 1, 29990000, 29990000),
('HD0000000009', 'SKU010', 1, 31990000, 31990000),
('HD0000000010', 'SKU004', 1, 7490000, 7490000),
('HD0000000001', 'SKU007', 1, 2490000, 2490000),
('HD0000000003', 'SKU008', 1, 3690000, 3690000);

/* ==========================================================
   12. PHIẾU BẢO HÀNH (6 dòng)
   ========================================================== */
INSERT INTO PhieuBaoHanh (maPhieu, soSerial, ngayTiepNhan, ngayHenTra, moTa, chiPhiSuaChua, trangThai, maHD, maSKU, maKH) VALUES
('PBH01', 'SN_IP15PM_01', '2026-06-06 13:00:00', '2026-06-10', N'Kiểm tra camera sau', 0, N'Đang xử lý', 'HD0000000001', 'SKU003', 'KH001'),
('PBH02', 'SN_ASUS_02', '2026-06-06 13:30:00', '2026-06-12', N'Lỗi quạt tản nhiệt', 0, N'Đang tiếp nhận', 'HD0000000002', 'SKU001', 'KH002'),
('PBH03', 'SN_MAC_03', '2026-06-06 14:00:00', '2026-06-08', N'Cài đặt phần mềm hệ thống', 0, N'Hoàn thành', 'HD0000000003', 'SKU002', 'KH003'),
('PBH04', 'SN_SONY_04', '2026-06-06 14:30:00', '2026-06-11', N'Mất đệm tai nghe', 150000, N'Đang xử lý', 'HD0000000004', 'SKU004', 'KH004'),
('PBH05', 'SN_AW9_05', '2026-06-06 15:00:00', '2026-06-09', N'Lỗi kết nối Bluetooth', 0, N'Đang tiếp nhận', 'HD0000000005', 'SKU006', 'KH005'),
('PBH06', 'SN_JBL_06', '2026-06-06 15:30:00', '2026-06-13', N'Không sạc được pin', 0, N'Đang xử lý', 'HD0000000007', 'SKU008', 'KH007');

/* ==========================================================
   13. GIAO DỊCH TRẢ GÓP (4 dòng - Khớp với các hóa đơn trả góp)
   ========================================================== */
INSERT INTO GiaoDichTraGop (maTraGop, doiTacTaiChinh, kyHan, tienTraTruoc, tienGopMoiThang, soTienConLai, trangThai, maHD) VALUES
('TG01', N'HD Saison', 12, 7990000, 1850000, 20000000, N'Đang trả góp', 'HD0000000002'),
('TG02', N'Home Credit', 6, 9990000, 3500000, 20000000, N'Đang trả góp', 'HD0000000008'),
('TG03', N'FE Credit', 12, 5990000, 2100000, 24000000, N'Đang trả góp', 'HD0000000009'),
('TG04', N'Mcredit', 9, 4990000, 2600000, 18000000, N'Đang trả góp', 'HD0000000003');
GO
