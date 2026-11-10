package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity SanPham ánh xạ bảng SanPham trong CSDL QuanLyBanHang.
 * Khóa chính là maSKU (người dùng quản lý bằng mã SKU, không tạo id tự động).
 */
public class SanPham {
    private String maSKU;
    private String maVach;
    private String tenSP;
    private String thuongHieu;
    private BigDecimal donGia;
    private int thoiGianBaoHanh;
    private int slTon;
    private String model;
    private String moTa;
    private String nhomSP;    
    private boolean trangThai;
    private DanhMuc danhMuc;
    private KhuyenMai khuyenMai;

    public SanPham() {
        this.donGia = BigDecimal.ZERO;
        this.trangThai = true;
        this.nhomSP = "Gọn nhẹ";
    }

    public SanPham(String maSKU, String maVach, String tenSP, String thuongHieu, BigDecimal donGia, int thoiGianBaoHanh,
            String model, String moTa, String nhomSP, boolean trangThai, DanhMuc danhMuc, KhuyenMai khuyenMai) {
        this.maSKU = maSKU;
        this.maVach = maVach;
        this.tenSP = tenSP;
        this.thuongHieu = thuongHieu;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
        this.thoiGianBaoHanh = thoiGianBaoHanh;
        this.model = model;
        this.moTa = moTa;
        this.nhomSP = (nhomSP != null && !nhomSP.isEmpty()) ? nhomSP : "Gọn nhẹ";
        this.trangThai = trangThai;
        this.danhMuc = danhMuc;
        this.khuyenMai = khuyenMai;
    }

    public SanPham(SanPham other) {
        this(other.maSKU, other.maVach, other.tenSP, other.thuongHieu, other.donGia,
                other.thoiGianBaoHanh, other.model, other.moTa, other.nhomSP, other.trangThai,
                other.danhMuc, other.khuyenMai);
        this.slTon = other.slTon;
    }

    /** Constructor tương thích dữ liệu mẫu và các gọi cũ */
    public SanPham(String sku, String ten, String danhMucId, String thuongHieu,
            BigDecimal donGia, int thoiGianBaoHanh, boolean trangThai, String moTa,
            String anhBase64, LocalDateTime taoLuc, LocalDateTime capNhatLuc) {
        this.maSKU = sku == null ? null : sku.trim();
        this.tenSP = ten;
        this.thuongHieu = thuongHieu;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
        this.thoiGianBaoHanh = thoiGianBaoHanh;
        this.trangThai = trangThai;
        this.moTa = moTa;
        this.nhomSP = "Gọn nhẹ";
        if (danhMucId != null) {
            this.danhMuc = new DanhMuc(danhMucId, "", "");
        }
    }

    // --- Getters / Setters chuẩn SQL ---

    public String getMaSKU() { return maSKU; }
    public void setMaSKU(String maSKU) { this.maSKU = maSKU; }

    public String getMaVach() { return maVach; }
    public void setMaVach(String maVach) { this.maVach = maVach; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public String getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(String thuongHieu) { this.thuongHieu = thuongHieu; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia != null ? donGia : BigDecimal.ZERO; }

    public int getThoiGianBaoHanh() { return thoiGianBaoHanh; }
    public void setThoiGianBaoHanh(int thoiGianBaoHanh) { this.thoiGianBaoHanh = thoiGianBaoHanh; }

    public int getSlTon() { return slTon; }
    public void setSlTon(int slTon) {
        if (slTon < 0) throw new IllegalArgumentException("Tồn kho không được âm.");
        this.slTon = slTon;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getNhomSP() { return nhomSP; }
    public void setNhomSP(String nhomSP) { this.nhomSP = nhomSP; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }

    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    // --- Aliases thuận tiện cho mã nghiệp vụ & UI ---

    public String getSku() { return maSKU; }
    public void setSku(String sku) { this.maSKU = sku; }

    public String getTen() { return tenSP; }
    public void setTen(String ten) { this.tenSP = ten; }

    public BigDecimal getGiaBan() { return donGia; }
    public void setGiaBan(BigDecimal giaBan) { this.donGia = giaBan != null ? giaBan : BigDecimal.ZERO; }

    public int getBaoHanhThang() { return thoiGianBaoHanh; }
    public void setBaoHanhThang(int baoHanhThang) { this.thoiGianBaoHanh = baoHanhThang; }

    public boolean isDangKinhDoanh() { return trangThai; }
    public void setDangKinhDoanh(boolean dangKinhDoanh) { this.trangThai = dangKinhDoanh; }

    public String getDanhMucId() {
        return danhMuc != null ? danhMuc.getMaDM() : null;
    }

    public void setDanhMucId(String dmId) {
        if (this.danhMuc == null) this.danhMuc = new DanhMuc();
        this.danhMuc.setMaDM(dmId);
    }

    public String getMaKM() {
        return khuyenMai != null ? khuyenMai.getMaKM() : null;
    }

    public void setMaKM(String kmId) {
        if (kmId == null || kmId.isEmpty()) {
            this.khuyenMai = null;
        } else {
            if (this.khuyenMai == null) this.khuyenMai = new KhuyenMai();
            this.khuyenMai.setMaKM(kmId);
        }
    }

    public String getTenDanhMuc() {
        return danhMuc != null && danhMuc.getTenDM() != null ? danhMuc.getTenDM() : "";
    }

    public String getTenKhuyenMai() {
        return khuyenMai != null && khuyenMai.getTenKM() != null ? khuyenMai.getTenKM() : "";
    }

    public String getAnhBase64() { return ""; }
    public void setAnhBase64(String anh) {}

    public LocalDateTime getTaoLuc() { return LocalDateTime.now(); }
    public void setTaoLuc(LocalDateTime luc) {}

    public LocalDateTime getCapNhatLuc() { return LocalDateTime.now(); }
    public void setCapNhatLuc(LocalDateTime luc) {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SanPham sanPham = (SanPham) o;
        return Objects.equals(maSKU, sanPham.maSKU);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSKU);
    }

    @Override
    public String toString() {
        return tenSP != null ? tenSP : (maSKU != null ? maSKU : "");
    }
}
