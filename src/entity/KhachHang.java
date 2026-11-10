package entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity KhachHang ánh xạ bảng KhachHang trong CSDL QuanLyBanHang.
 */
public class KhachHang {
    private String maKH;
    private String hoTen;
    private String sdt;
    private String email;
    private String diaChi;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private String hangTV;
    private BigDecimal tongChiTieuTichLuy;
    private boolean trangThai;

    // Các thuộc tính phụ tương thích UI/lịch sử cũ
    private String ghiChu;
    private boolean nhanKhuyenMai;
    private LocalDateTime taoLuc;
    private LocalDateTime capNhatLuc;

    public KhachHang() {
        this.hangTV = "Member";
        this.tongChiTieuTichLuy = BigDecimal.ZERO;
        this.trangThai = true;
        this.gioiTinh = true;
        this.nhanKhuyenMai = false;
        this.taoLuc = LocalDateTime.now();
        this.capNhatLuc = this.taoLuc;
    }

    public KhachHang(String maKH, String hoTen, String sdt, String email, String diaChi, LocalDate ngaySinh,
            boolean gioiTinh, String hangTV, double tongChiTieuTichLuy, boolean trangThai) {
        this(maKH, hoTen, sdt, email, diaChi, ngaySinh, Boolean.valueOf(gioiTinh), hangTV,
                BigDecimal.valueOf(Math.max(0.0, tongChiTieuTichLuy)), trangThai);
    }

    public KhachHang(String maKH, String hoTen, String sdt, String email, String diaChi, LocalDate ngaySinh,
            Boolean gioiTinh, String hangTV, BigDecimal tongChiTieuTichLuy, boolean trangThai) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.hangTV = (hangTV != null && !hangTV.isEmpty()) ? hangTV : "Member";
        this.tongChiTieuTichLuy = tongChiTieuTichLuy == null ? BigDecimal.ZERO
                : tongChiTieuTichLuy.max(BigDecimal.ZERO).setScale(0, java.math.RoundingMode.HALF_UP);
        this.trangThai = trangThai;
        this.nhanKhuyenMai = false;
        this.taoLuc = LocalDateTime.now();
        this.capNhatLuc = this.taoLuc;
    }

    public KhachHang(KhachHang other) {
        this(other.maKH, other.hoTen, other.sdt, other.email, other.diaChi, other.ngaySinh, other.gioiTinh,
                other.hangTV, other.tongChiTieuTichLuy, other.trangThai);
        this.ghiChu = other.ghiChu;
        this.nhanKhuyenMai = other.nhanKhuyenMai;
        this.taoLuc = other.taoLuc;
        this.capNhatLuc = other.capNhatLuc;
    }

    /** Constructor tương thích dữ liệu mẫu cũ */
    public KhachHang(String id, String ten, String sdt, String email, String diaChi,
            LocalDate ngaySinh, int hang, boolean hoatDong, boolean nhanKM,
            String ghiChu, LocalDateTime taoLuc, LocalDateTime capNhatLuc) {
        this.maKH = id;
        this.hoTen = ten;
        this.sdt = sdt;
        this.email = email;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = true;
        this.hangTV = hang == 3 ? "Diamond" : hang == 2 ? "Gold" : hang == 1 ? "Silver" : "Member";
        this.tongChiTieuTichLuy = BigDecimal.ZERO;
        this.trangThai = hoatDong;
        this.nhanKhuyenMai = nhanKM;
        this.ghiChu = ghiChu;
        this.taoLuc = taoLuc != null ? taoLuc : LocalDateTime.now();
        this.capNhatLuc = capNhatLuc != null ? capNhatLuc : this.taoLuc;
    }

    // --- Getters / Setters chuẩn SQL ---

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public boolean isGioiTinh() { return Boolean.TRUE.equals(gioiTinh); }
    public Boolean getGioiTinhNullable() { return gioiTinh; }
    public void setGioiTinh(boolean gioiTinh) { this.gioiTinh = gioiTinh; }
    public void setGioiTinh(Boolean gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getHangTV() { return hangTV; }
    public void setHangTV(String hangTV) { this.hangTV = hangTV; }

    public double getTongChiTieuTichLuy() { return tongChiTieuTichLuy.doubleValue(); }
    public BigDecimal getTongChiTieuTichLuyDecimal() { return tongChiTieuTichLuy; }
    public void setTongChiTieuTichLuy(double tongChiTieuTichLuy) {
        this.tongChiTieuTichLuy = BigDecimal.valueOf(Math.max(0.0, tongChiTieuTichLuy))
                .setScale(0, java.math.RoundingMode.HALF_UP);
    }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    // --- Aliases thuận tiện cho mã nghiệp vụ & UI ---

    public String getId() { return maKH; }
    public void setId(String id) { this.maKH = id; }

    public String getSoDienThoai() { return sdt; }
    public void setSoDienThoai(String sdt) { this.sdt = sdt; }

    public boolean isDangHoatDong() { return trangThai; }
    public void setDangHoatDong(boolean dangHoatDong) { this.trangThai = dangHoatDong; }

    public BigDecimal getTongChiTieu() {
        return tongChiTieuTichLuy;
    }
    public void setTongChiTieu(BigDecimal tong) {
        this.tongChiTieuTichLuy = tong == null ? BigDecimal.ZERO
                : tong.max(BigDecimal.ZERO).setScale(0, java.math.RoundingMode.HALF_UP);
    }

    public String getGhiChu() { return ghiChu != null ? ghiChu : ""; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isNhanKhuyenMai() { return nhanKhuyenMai; }
    public void setNhanKhuyenMai(boolean nhanKM) { this.nhanKhuyenMai = nhanKM; }

    public LocalDateTime getTaoLuc() { return taoLuc; }
    public void setTaoLuc(LocalDateTime luc) { this.taoLuc = luc; }

    public LocalDateTime getCapNhatLuc() { return capNhatLuc != null ? capNhatLuc : LocalDateTime.now(); }
    public void setCapNhatLuc(LocalDateTime luc) { this.capNhatLuc = luc; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhachHang khachHang = (KhachHang) o;
        return Objects.equals(maKH, khachHang.maKH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKH);
    }

    @Override
    public String toString() {
        return hoTen != null ? (hoTen + " (" + (sdt != null ? sdt : maKH) + ")") : (maKH != null ? maKH : "");
    }
}
