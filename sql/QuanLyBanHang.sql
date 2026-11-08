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
    thuongHieu      NVARCHAR(100)  NOT NULL, 
    model           NVARCHAR(100)  NULL,
    moTa            NVARCHAR(500)  NULL,
    nhomSanPham     NVARCHAR(100)  NULL,
    trangThai       BIT            NOT NULL CONSTRAINT DF_SP_trangThai DEFAULT 1,
    maDanhMuc       VARCHAR(10)    NOT NULL,
    maKM            VARCHAR(10)    NOT NULL,
    CONSTRAINT PK_SanPham       PRIMARY KEY (maSKU),
    CONSTRAINT UQ_SP_maVach     UNIQUE (maVach),
    CONSTRAINT CK_SP_donGia     CHECK (donGia > 0),
    CONSTRAINT CK_SP_baoHanh    CHECK (thoiGianBaoHanh >= 0),
    CONSTRAINT CK_SP_nhomSP     CHECK (nhomSanPham IN (N'Gọn nhẹ', N'Cồng kềnh')), 
    CONSTRAINT FK_SP_DanhMuc    FOREIGN KEY (maDanhMuc) REFERENCES DanhMuc(maDanhMuc),
    CONSTRAINT FK_SP_KhuyenMai  FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM)
);

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
    CONSTRAINT PK_KhachHang        PRIMARY KEY (maKH),
    CONSTRAINT UQ_KH_sdt           UNIQUE (sdt),
    CONSTRAINT CK_KH_sdt           CHECK (sdt LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' AND LEN(sdt) = 10),
    CONSTRAINT CK_KH_tctl          CHECK (tongChiTieuTichLuy >= 0),
    CONSTRAINT CK_KH_hangTV        CHECK (hangTV IN (N'Member', N'Silver', N'Gold', N'Diamond'))
);

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


USE QuanLyBanHang;
GO
