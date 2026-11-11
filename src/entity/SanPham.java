package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity SanPham 
 * Ánh xạ bảng SanPham trong CSDL QuanLyBanHang.
 * Khóa chính là maSKU (người dùng quản lý bằng mã SKU, không tạo id tự động).
 */
public class SanPham {
    private String maSKU;
    private String maVach;
    private String tenSP;
    private BigDecimal donGia;
    private int thoiGianBaoHanh;
    private int slTon;
    private String thuongHieu;
    private String model;
    private String moTa;
    private String nhomSP;    
    private boolean trangThai;
    private DanhMuc danhMuc;
    private KhuyenMai khuyenMai;
    
    // Thuộc tính mới
    private String hinhAnh;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    public SanPham() {
        this.donGia = BigDecimal.ZERO;
        this.trangThai = true;
        this.slTon = 0;
        this.nhomSP = "Gọn nhẹ";
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = this.ngayTao;
    }

    public SanPham(String maSKU, String maVach, String tenSP, BigDecimal donGia, int thoiGianBaoHanh, 
            int slTon, String thuongHieu, String model, String moTa, String nhomSP, 
            boolean trangThai, DanhMuc danhMuc, KhuyenMai khuyenMai,
            String hinhAnh, LocalDateTime ngayTao, LocalDateTime ngayCapNhat) {
        this.maSKU = maSKU;
        this.maVach = maVach;
        this.tenSP = tenSP;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
        this.thoiGianBaoHanh = thoiGianBaoHanh;
        this.slTon = slTon;
        this.thuongHieu = thuongHieu;
        this.model = model;
        this.moTa = moTa;
        this.nhomSP = (nhomSP != null && !nhomSP.isEmpty()) ? nhomSP : "Gọn nhẹ";
        this.trangThai = trangThai;
        this.danhMuc = danhMuc;
        this.khuyenMai = khuyenMai;
        this.hinhAnh = hinhAnh;
        this.ngayTao = ngayTao != null ? ngayTao : LocalDateTime.now();
        this.ngayCapNhat = ngayCapNhat != null ? ngayCapNhat : this.ngayTao;
    }

    // Copy Constructor
    public SanPham(SanPham other) {
        this(other.maSKU, other.maVach, other.tenSP, other.donGia, other.thoiGianBaoHanh,
             other.slTon, other.thuongHieu, other.model, other.moTa, other.nhomSP, 
             other.trangThai, other.danhMuc, other.khuyenMai,
             other.hinhAnh, other.ngayTao, other.ngayCapNhat);
    }

    // --- Getters / Setters chuẩn DB ---

    public String getMaSKU() { return maSKU; }
    public void setMaSKU(String maSKU) { this.maSKU = maSKU; }

    public String getMaVach() { return maVach; }
    public void setMaVach(String maVach) { this.maVach = maVach; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia != null ? donGia : BigDecimal.ZERO; }

    public int getThoiGianBaoHanh() { return thoiGianBaoHanh; }
    public void setThoiGianBaoHanh(int thoiGianBaoHanh) { this.thoiGianBaoHanh = thoiGianBaoHanh; }

    public int getSlTon() { return slTon; }
    public void setSlTon(int slTon) {
        if (slTon < 0) throw new IllegalArgumentException("Tồn kho không được âm.");
        this.slTon = slTon;
    }

    public String getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(String thuongHieu) { this.thuongHieu = thuongHieu; }

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

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    // --- Phương thức hỗ trợ UI (Tuỳ chọn) ---

    public String getSku() { return maSKU; }
    public String getTen() { return tenSP; }
    public BigDecimal getGiaBan() { return donGia; }
    public int getBaoHanhThang() { return thoiGianBaoHanh; }
    public boolean isDangKinhDoanh() { return trangThai; }

    public String getMaDanhMuc() {
        return danhMuc != null ? danhMuc.getMaDanhMuc() : null;
    }

    public void setMaDanhMuc(String maDM) {
        if (this.danhMuc == null) this.danhMuc = new DanhMuc();
        this.danhMuc.setMaDanhMuc(maDM); 
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
        return danhMuc != null && danhMuc.getTenDanhMuc() != null ? danhMuc.getTenDanhMuc() : "";
    }

    public String getTenKhuyenMai() {
        return khuyenMai != null && khuyenMai.getTenKM() != null ? khuyenMai.getTenKM() : "";
    }

    // --- Override Base Methods ---

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