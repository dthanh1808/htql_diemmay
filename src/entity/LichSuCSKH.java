package entity;

import java.time.LocalDateTime;

public class LichSuCSKH {
    private String maKhachHang, loai, kenh, noiDung, trangThai;
    private LocalDateTime thoiGian;

    public LichSuCSKH(String maKhachHang, LocalDateTime thoiGian, String loai, String kenh, String noiDung, String trangThai) {
        this.maKhachHang = maKhachHang;
        this.thoiGian = thoiGian;
        this.loai = loai;
        this.kenh = kenh;
        this.noiDung = noiDung;
        this.trangThai = trangThai;
    }

    public String getMaKhachHang() { return maKhachHang; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public String getLoai() { return loai; }
    public String getKenh() { return kenh; }
    public String getNoiDung() { return noiDung; }
    public String getTrangThai() { return trangThai; }
}
