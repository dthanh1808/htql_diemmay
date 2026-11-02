
IF DB_ID(N'QuanLyBanHang') IS NULL
    CREATE DATABASE QuanLyBanHang;
GO
USE QuanLyBanHang;
GO

/* ==========================================================
   1. DANH MỤC SẢN PHẨM
   RB: Mã danh mục là PK, duy nhất
   ========================================================== */
CREATE TABLE DanhMuc (
    maDanhMuc   VARCHAR(10)    NOT NULL,
    tenDanhMuc  NVARCHAR(100)  NOT NULL,
    moTa        NVARCHAR(255)  NULL,
    CONSTRAINT PK_DanhMuc PRIMARY KEY (maDanhMuc)
);

/* ==========================================================
   2. SẢN PHẨM
   RB miền giá trị:
     - donGia > 0
     - thoiGianBaoHanh >= 0
   RB dữ liệu:
     - maSKU là PK, duy nhất
     - maVach là UNIQUE
     - maDanhMuc FK → DanhMuc
     - SP đã có trong HĐ không được xóa → xử lý ở tầng app
   ========================================================== */
CREATE TABLE SanPham (
    maSKU           VARCHAR(20)    NOT NULL,
    maVach          VARCHAR(50)    NULL,
    tenSP           NVARCHAR(150)  NOT NULL,
    donGia          DECIMAL(18,0)  NOT NULL,
    thoiGianBaoHanh INT            NULL,
    thuongHieu      NVARCHAR(100)  NULL,
    model           NVARCHAR(100)  NULL,
    moTa            NVARCHAR(500)  NULL,
    nhomSanPham     NVARCHAR(100)  NULL,
    slTon           INT            NOT NULL CONSTRAINT DF_SP_slTon DEFAULT 0,
    trangThai       BIT            NOT NULL CONSTRAINT DF_SP_trangThai DEFAULT 1,
    maDanhMuc       VARCHAR(10)    NOT NULL,
    CONSTRAINT PK_SanPham       PRIMARY KEY (maSKU),
    CONSTRAINT UQ_SP_maVach     UNIQUE (maVach),
    CONSTRAINT CK_SP_donGia    CHECK (donGia > 0),
    CONSTRAINT CK_SP_slTon     CHECK (slTon >= 0),
    CONSTRAINT CK_SP_baoHanh   CHECK (thoiGianBaoHanh >= 0),
    CONSTRAINT FK_SP_DanhMuc   FOREIGN KEY (maDanhMuc) REFERENCES DanhMuc(maDanhMuc)
);

/* ==========================================================
   3. DANH MỤC CA (loại ca: sáng / chiều / tối …)
   ========================================================== */
CREATE TABLE DanhMucCa (
    maLoaiCa    VARCHAR(10)    NOT NULL,
    tenCa       NVARCHAR(100)  NOT NULL,
    gioBatDau   TIME           NOT NULL,
    gioKetThuc  TIME           NOT NULL,
    CONSTRAINT PK_DanhMucCa PRIMARY KEY (maLoaiCa)
);

/* ==========================================================
   4. CA LÀM VIỆC (phiên ca thực tế)
   RB: maCa là PK, duy nhất
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
    maLoaiCa        VARCHAR(10)    NULL,
    CONSTRAINT PK_CaLamViec PRIMARY KEY (maCa),
    CONSTRAINT FK_CLV_DanhMucCa FOREIGN KEY (maLoaiCa) REFERENCES DanhMucCa(maLoaiCa)
);

/* ==========================================================
   5. NHÂN VIÊN
   RB miền giá trị:
     - chucVu IN (Quản lý, Bán hàng, Chăm sóc khách hàng)
   RB dữ liệu:
     - maNV là PK, duy nhất
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
    CONSTRAINT CK_NV_chucVu  CHECK (chucVu IN (N'Quản lý', N'Bán hàng', N'Chăm sóc khách hàng'))
);

/* ==========================================================
   6. TÀI KHOẢN (1 – 1 Nhân viên)
   RB dữ liệu:
     - maTK là PK, duy nhất
     - username UNIQUE, bắt buộc = maNV
     - maNV UNIQUE (1-1), FK → NhanVien
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
   7. LỊCH LÀM VIỆC (Phân công ca)
   RB miền giá trị:
     - ngayLamViec >= ngày hiện tại → TRIGGER (CHECK ko dùng GETDATE)
   RB dữ liệu:
     - maLich là PK, duy nhất
     - FK maNV → NhanVien
     - FK maCa → CaLamViec
     - Trùng lịch: 1 NV không xếp 2 ca trùng giờ cùng ngày → TRIGGER
   ========================================================== */
CREATE TABLE LichLamViec (
    maLich              VARCHAR(10)   NOT NULL,
    ngayLamViec         DATE          NOT NULL,
    trangThai           NVARCHAR(30)  NULL,
    maNV                VARCHAR(10)   NOT NULL,
    maCa                VARCHAR(10)   NOT NULL,
    CONSTRAINT PK_LichLamViec      PRIMARY KEY (maLich),
    CONSTRAINT FK_LLV_NhanVien     FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_LLV_CaLamViec    FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa)
);

/* ==========================================================
   8. KHÁCH HÀNG
   RB miền giá trị:
     - SĐT: đúng 10 chữ số
     - tongChiTieuTichLuy khởi tạo = 0, >= 0
     - hangTV IN (Member, Silver, Gold, Diamond)
   RB dữ liệu:
     - maKH là PK, duy nhất
     - SĐT là UNIQUE
     - KH có HĐ không được xóa → xử lý ở tầng app
   ========================================================== */
CREATE TABLE KhachHang (
    maKH                VARCHAR(10)    NOT NULL,
    tenKH               NVARCHAR(100)  NOT NULL,
    sdt                 VARCHAR(15)    NULL,
    email               VARCHAR(100)   NULL,
    diaChi              NVARCHAR(200)  NULL,
    ngaySinh            DATE           NULL,
    gioiTinh            BIT            NULL,
    hangTV              NVARCHAR(30)   NULL,
    tongChiTieuTichLuy  DECIMAL(18,0)  NOT NULL CONSTRAINT DF_KH_tctl DEFAULT 0,
    trangThai           BIT            NOT NULL CONSTRAINT DF_KH_trangThai DEFAULT 1,
    CONSTRAINT PK_KhachHang       PRIMARY KEY (maKH),
    CONSTRAINT UQ_KH_sdt          UNIQUE (sdt),
    CONSTRAINT CK_KH_sdt          CHECK (sdt LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'
                                         AND LEN(sdt) = 10),
    CONSTRAINT CK_KH_tctl         CHECK (tongChiTieuTichLuy >= 0),
    CONSTRAINT CK_KH_hangTV       CHECK (hangTV IN (N'Member', N'Silver', N'Gold', N'Diamond'))
);

/* ==========================================================
   9. KHUYẾN MÃI
   RB miền giá trị:
     - giaTriGiam: mức giảm không vượt quá 50% giá gốc SP → TRIGGER
     - thoiGianKetThuc >= thoiGianBatDau
     - trangThai IN (Chờ kích hoạt, Đang kích hoạt, Đã kết thúc, Đã hủy)
     - trangThai mặc định = 'Chờ kích hoạt'
   RB dữ liệu:
     - maKM là PK, duy nhất
     - Không kích hoạt 2 KM % trùng SP trùng thời gian → TRIGGER
   ========================================================== */
CREATE TABLE KhuyenMai (
    maKM              VARCHAR(10)    NOT NULL,
    tenKM             NVARCHAR(150)  NOT NULL,
    giaTriGiam        DECIMAL(18,2)  NOT NULL,
    loaiKM            NVARCHAR(50)   NULL,
    phamViApDung      NVARCHAR(100)  NULL,
    doiTuongApDung    NVARCHAR(100)  NULL,
    thoiGianBatDau    DATETIME       NOT NULL,
    thoiGianKetThuc   DATETIME       NOT NULL,
    trangThai         NVARCHAR(30)   NOT NULL CONSTRAINT DF_KM_trangThai DEFAULT N'Chờ kích hoạt',
    CONSTRAINT PK_KhuyenMai         PRIMARY KEY (maKM),
    CONSTRAINT CK_KM_giaTriGiam     CHECK (giaTriGiam >= 0),
    CONSTRAINT CK_KM_thoiGian       CHECK (thoiGianKetThuc >= thoiGianBatDau),
    CONSTRAINT CK_KM_trangThai      CHECK (trangThai IN (N'Chờ kích hoạt', N'Đang kích hoạt',
                                                          N'Đã kết thúc', N'Đã hủy'))
);

/* ==========================================================
   10. HÓA ĐƠN
   RB miền giá trị:
     - maHD: 12 ký tự, format HD + YYMMDD + 4 số (VD: HD2609200001)
     - tongTienHang, tienGiamGia, tongThanhToan >= 0
     - tienGiamGia <= 70% tongTienHang
   RB dữ liệu:
     - maHD là PK, duy nhất
     - FK maNV → NhanVien (bắt buộc)
     - FK maKH → KhachHang (có thể NULL)
     - FK maKM → KhuyenMai (có thể NULL)
     - FK maCa → CaLamViec
     - NV chỉ lập HĐ khi ca đã "Mở" → xử lý ở tầng app
     - Không chọn SP trạng thái "Ngưng KD" → xử lý ở tầng app
   ========================================================== */
CREATE TABLE HoaDon (
    maHD                  VARCHAR(12)    NOT NULL,
    ngayLap               DATETIME       NOT NULL CONSTRAINT DF_HD_ngayLap DEFAULT GETDATE(),
    tongTienHang          DECIMAL(18,0)  NOT NULL CONSTRAINT DF_HD_tth DEFAULT 0,
    tienGiamGia           DECIMAL(18,0)  NOT NULL CONSTRAINT DF_HD_tgg DEFAULT 0,
    tongThanhToan         DECIMAL(18,0)  NOT NULL CONSTRAINT DF_HD_ttt DEFAULT 0,
    phuongThucThanhToan   NVARCHAR(50)   NULL,
    yeuCauGiaoLapDat      NVARCHAR(100)  NULL,
    ghiChuGiaoLapDat      NVARCHAR(500)  NULL,
    trangThai             NVARCHAR(30)   NULL,
    maNV                  VARCHAR(10)    NOT NULL,
    maKH                  VARCHAR(10)    NULL,
    maKM                  VARCHAR(10)    NULL,
    maCa                  VARCHAR(10)    NULL,
    CONSTRAINT PK_HoaDon           PRIMARY KEY (maHD),
    CONSTRAINT CK_HD_maHD_format   CHECK (LEN(maHD) = 12
                                          AND maHD LIKE 'HD[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_HD_tongTienHang  CHECK (tongTienHang >= 0),
    CONSTRAINT CK_HD_tienGiamGia   CHECK (tienGiamGia >= 0),
    CONSTRAINT CK_HD_tongThanhToan CHECK (tongThanhToan >= 0),
    CONSTRAINT CK_HD_giamGia70     CHECK (tienGiamGia <= 0.7 * tongTienHang),
    CONSTRAINT FK_HD_NhanVien      FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HD_KhachHang     FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HD_KhuyenMai     FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    CONSTRAINT FK_HD_CaLamViec     FOREIGN KEY (maCa) REFERENCES CaLamViec(maCa)
);

/* ==========================================================
   11. CHI TIẾT HÓA ĐƠN (n – n: Hóa đơn — Sản phẩm)
   RB miền giá trị:
     - soLuong > 0
     - donGia >= 0
   RB dữ liệu:
     - PK phức hợp (maHD, maSKU)
     - FK maHD → HoaDon
     - FK maSKU → SanPham
   ========================================================== */
CREATE TABLE ChiTietHoaDon (
    maHD        VARCHAR(12)    NOT NULL,
    maSKU       VARCHAR(20)    NOT NULL,
    soLuong     INT            NOT NULL,
    donGia      DECIMAL(18,0)  NOT NULL CONSTRAINT DF_CTHD_donGia DEFAULT 0,
    thanhTien   DECIMAL(18,0)  NOT NULL CONSTRAINT DF_CTHD_thanhTien DEFAULT 0,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHD, maSKU),
    CONSTRAINT CK_CTHD_soLuong  CHECK (soLuong > 0),
    CONSTRAINT CK_CTHD_donGia   CHECK (donGia >= 0),
    CONSTRAINT FK_CTHD_HoaDon   FOREIGN KEY (maHD)  REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_SanPham  FOREIGN KEY (maSKU) REFERENCES SanPham(maSKU)
);

/* ==========================================================
   12. PHIẾU BẢO HÀNH
   RB miền giá trị:
     - ngayTiepNhan >= HoaDon.ngayLap → TRIGGER
   RB dữ liệu:
     - maPhieu là PK, duy nhất
     - FK maHD → HoaDon, FK maSKU → SanPham,
       FK maKH → KhachHang, FK maNV → NhanVien
   ========================================================== */
CREATE TABLE PhieuBaoHanh (
    maPhieu       VARCHAR(10)    NOT NULL,
    soSerial      VARCHAR(50)    NULL,
    ngayTiepNhan  DATE           NOT NULL,
    ngayHenTra    DATE           NULL,
    moTa          NVARCHAR(255)  NULL,
    chiPhiSuaChua DECIMAL(18,0)  NULL,
    trangThai     NVARCHAR(30)   NULL,
    maHD          VARCHAR(12)    NOT NULL,
    maSKU         VARCHAR(20)    NULL,
    maKH          VARCHAR(10)    NULL,
    maNV          VARCHAR(10)    NULL,
    CONSTRAINT PK_PhieuBaoHanh   PRIMARY KEY (maPhieu),
    CONSTRAINT FK_PBH_HoaDon     FOREIGN KEY (maHD)  REFERENCES HoaDon(maHD),
    CONSTRAINT FK_PBH_SanPham    FOREIGN KEY (maSKU) REFERENCES SanPham(maSKU),
    CONSTRAINT FK_PBH_KhachHang  FOREIGN KEY (maKH)  REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PBH_NhanVien   FOREIGN KEY (maNV)  REFERENCES NhanVien(maNV)
);

/* ==========================================================
   13. GIAO DỊCH TRẢ GÓP
   RB miền giá trị:
     - kyHan IN (6, 9, 12, 18, 24)
     - tienTraTruoc >= 0
   RB dữ liệu:
     - maTraGop là PK, duy nhất
     - FK maHD → HoaDon
     - UNIQUE maHD (mỗi HĐ tối đa 1 GDTG)
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
   TRIGGER: Ràng buộc liên bảng
   ========================================================== */

/* --- TR1: Lịch làm việc – ngayLamViec >= ngày hiện tại --- */
GO
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
        RAISERROR(N'Ngày phân công phải >= ngày hiện tại (không được đặt lịch cho quá khứ).', 16, 1);
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
        RAISERROR(N'Chỉ được lập hóa đơn khi ca làm việc đang ở trạng thái "Đang mở".', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

/* --- TR4: Chi tiết HĐ – Không cho thêm SP "Ngưng kinh doanh" --- */
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
        WHERE sp.trangThai = 0          -- 0 = Ngưng kinh doanh
    )
    BEGIN
        RAISERROR(N'Không được thêm sản phẩm đang "Ngưng kinh doanh" vào hóa đơn.', 16, 1);
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
        JOIN CaLamViec c1 ON i.maCa  = c1.maCa
        JOIN CaLamViec c2 ON llv.maCa = c2.maCa
        JOIN DanhMucCa d1 ON c1.maLoaiCa = d1.maLoaiCa
        JOIN DanhMucCa d2 ON c2.maLoaiCa = d2.maLoaiCa
        WHERE d1.gioBatDau < d2.gioKetThuc
          AND d2.gioBatDau < d1.gioKetThuc
    )
    BEGIN
        RAISERROR(N'Nhân viên đã có ca làm việc trùng thời gian trong ngày này.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO
