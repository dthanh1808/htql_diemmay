package entity;

import java.time.LocalDate;

public class KhachHang {
    private String maKhachHang, hoTen, soDienThoai, email, gioiTinh, diaChi;
    private LocalDate ngaySinh, ngayDangKy;
    private boolean hoatDong;
    private double tongChiTieu;
    private String hangThanhVien = "Member";

    public KhachHang(String maKhachHang, String hoTen, String soDienThoai, String email,
            LocalDate ngaySinh, String gioiTinh, String diaChi, LocalDate ngayDangKy, boolean hoatDong) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen.trim();
        this.soDienThoai = soDienThoai.trim();
        this.email = email.trim();
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi.trim();
        this.ngayDangKy = ngayDangKy;
        this.hoatDong = hoatDong;
    }

    public String getMaKhachHang() { return maKhachHang; }
    public String getMaTheThanhVien() { return maKhachHang.isEmpty() ? "" : "TV" + maKhachHang.substring(2); }
    public void setMaKhachHang(String ma) { maKhachHang = ma; }
    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getEmail() { return email; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public String getDiaChi() { return diaChi; }
    public LocalDate getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(LocalDate ngay) { ngayDangKy = ngay; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean value) { hoatDong = value; }
    public double getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(double value) { tongChiTieu = value; }
    public String getHangThanhVien() { return hangThanhVien; }
    public void setHangThanhVien(String value) { hangThanhVien = value; }
}
