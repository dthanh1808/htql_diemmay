package entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity KhachHang 
 * Ánh xạ bảng KhachHang trong CSDL QuanLyBanHang.
 */
public class KhachHang {
    private String maKH;
    private String tenKH;        // Sửa từ hoTen -> tenKH
    private String sdt;
    private String email;
    private String diaChi;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;    // NULLable trong SQL nên dùng Boolean
    private String hangTV;
    private BigDecimal tongChiTieuTichLuy;
    private boolean trangThai;   // BIT NOT NULL
    private String ghiChu;
    private boolean nhanKhuyenMai; // BIT NOT NULL
    private LocalDateTime ngayTao; // Sửa từ taoLuc -> ngayTao
    private LocalDateTime ngayCapNhat; // Sửa từ capNhatLuc -> ngayCapNhat

    // --- Constructors ---

    public KhachHang() {
        // Gán các giá trị DEFAULT giống hệt SQL
        this.hangTV = "Member";
        this.tongChiTieuTichLuy = BigDecimal.ZERO;
        this.trangThai = true;
        this.nhanKhuyenMai = false;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = this.ngayTao;
    }

    // Constructor đầy đủ cho mọi trường
    public KhachHang(String maKH, String tenKH, String sdt, String email, String diaChi, LocalDate ngaySinh,
                     Boolean gioiTinh, String hangTV, BigDecimal tongChiTieuTichLuy, boolean trangThai,
                     String ghiChu, boolean nhanKhuyenMai, LocalDateTime ngayTao, LocalDateTime ngayCapNhat) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.sdt = sdt;
        this.email = email;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.hangTV = (hangTV != null && !hangTV.isEmpty()) ? hangTV : "Member";
        this.tongChiTieuTichLuy = tongChiTieuTichLuy == null ? BigDecimal.ZERO
                : tongChiTieuTichLuy.max(BigDecimal.ZERO).setScale(0, java.math.RoundingMode.HALF_UP);
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.nhanKhuyenMai = nhanKhuyenMai;
        this.ngayTao = ngayTao != null ? ngayTao : LocalDateTime.now();
        this.ngayCapNhat = ngayCapNhat != null ? ngayCapNhat : this.ngayTao;
    }

    // Constructor lược giản thường dùng khi Insert từ UI (Các trường có default sẽ tự sinh)
    public KhachHang(String maKH, String tenKH, String sdt, String email, String diaChi, LocalDate ngaySinh,
                     boolean gioiTinh, String hangTV, double tongChiTieuTichLuy, boolean trangThai) {
        this(maKH, tenKH, sdt, email, diaChi, ngaySinh, Boolean.valueOf(gioiTinh), hangTV,
                BigDecimal.valueOf(Math.max(0.0, tongChiTieuTichLuy)), trangThai, null, false, 
                LocalDateTime.now(), LocalDateTime.now());
    }

    // Copy Constructor
    public KhachHang(KhachHang other) {
        this(other.maKH, other.tenKH, other.sdt, other.email, other.diaChi, other.ngaySinh, other.gioiTinh,
             other.hangTV, other.tongChiTieuTichLuy, other.trangThai, other.ghiChu, other.nhanKhuyenMai, 
             other.ngayTao, other.ngayCapNhat);
    }

    // --- Getters / Setters chuẩn DB ---

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }

    // Aliases hỗ trợ tương thích
    public String getHoTen() { return tenKH; }
    public void setHoTen(String hoTen) { this.tenKH = hoTen; }

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
    public BigDecimal getTongChiTieu() { return tongChiTieuTichLuy; }
    public void setTongChiTieu(BigDecimal tongChiTieu) { setTongChiTieuTichLuy(tongChiTieu); }
    public void setTongChiTieuTichLuy(double tongChiTieuTichLuy) {
        this.tongChiTieuTichLuy = BigDecimal.valueOf(Math.max(0.0, tongChiTieuTichLuy))
                .setScale(0, java.math.RoundingMode.HALF_UP);
    }
    public void setTongChiTieuTichLuy(BigDecimal tongChiTieuTichLuy) {
        this.tongChiTieuTichLuy = tongChiTieuTichLuy == null ? BigDecimal.ZERO
                : tongChiTieuTichLuy.max(BigDecimal.ZERO).setScale(0, java.math.RoundingMode.HALF_UP);
    }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu != null ? ghiChu : ""; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isNhanKhuyenMai() { return nhanKhuyenMai; }
    public void setNhanKhuyenMai(boolean nhanKhuyenMai) { this.nhanKhuyenMai = nhanKhuyenMai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public LocalDateTime getTaoLuc() { return ngayTao; }

    public LocalDateTime getNgayCapNhat() { return ngayCapNhat != null ? ngayCapNhat : LocalDateTime.now(); }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
    public LocalDateTime getCapNhatLuc() { return getNgayCapNhat(); }

    // --- Phương thức hỗ trợ UI (Tuỳ chọn) ---
    public String getId() { return maKH; }
    public void setId(String id) { this.maKH = id; }
    public String getSoDienThoai() { return sdt; }
    public void setSoDienThoai(String sdt) { this.sdt = sdt; }
    public boolean isDangHoatDong() { return trangThai; }
    public void setDangHoatDong(boolean dangHoatDong) { this.trangThai = dangHoatDong; }

    // --- Override Base Methods ---

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
        return tenKH != null ? (tenKH + " (" + (sdt != null ? sdt : maKH) + ")") : (maKH != null ? maKH : "");
    }
}