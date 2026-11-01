package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Dữ liệu trong phiên chạy; DAO trả về bản sao để tránh thay đổi ngoài ý muốn. */
public class KhachHang {
    private String id;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private LocalDate ngaySinh;
    private boolean dangHoatDong;
    private boolean nhanKhuyenMai;
    private String ghiChu;
    private LocalDateTime taoLuc;
    private LocalDateTime capNhatLuc;

    public KhachHang() { }

    public KhachHang(String id, String hoTen, String soDienThoai, String email, String diaChi, LocalDate ngaySinh, boolean dangHoatDong, boolean nhanKhuyenMai, String ghiChu, LocalDateTime taoLuc, LocalDateTime capNhatLuc) {
        this.id = id;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.dangHoatDong = dangHoatDong;
        this.nhanKhuyenMai = nhanKhuyenMai;
        this.ghiChu = ghiChu;
        this.taoLuc = taoLuc;
        this.capNhatLuc = capNhatLuc;
    }

    public KhachHang(KhachHang other) {
        this(other.id, other.hoTen, other.soDienThoai, other.email, other.diaChi, other.ngaySinh, other.dangHoatDong, other.nhanKhuyenMai, other.ghiChu, other.taoLuc, other.capNhatLuc);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public boolean isDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }

    public boolean isNhanKhuyenMai() { return nhanKhuyenMai; }
    public void setNhanKhuyenMai(boolean nhanKhuyenMai) { this.nhanKhuyenMai = nhanKhuyenMai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getTaoLuc() { return taoLuc; }
    public void setTaoLuc(LocalDateTime taoLuc) { this.taoLuc = taoLuc; }

    public LocalDateTime getCapNhatLuc() { return capNhatLuc; }
    public void setCapNhatLuc(LocalDateTime capNhatLuc) { this.capNhatLuc = capNhatLuc; }
}
