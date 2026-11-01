package entity;

import java.time.LocalDateTime;

/** Dữ liệu trong phiên chạy; DAO trả về bản sao để tránh thay đổi ngoài ý muốn. */
public class LichSuKhachHang {
    private String khachHangId;
    private LocalDateTime thoiGian;
    private String loai;
    private String noiDung;

    public LichSuKhachHang() { }

    public LichSuKhachHang(String khachHangId, LocalDateTime thoiGian, String loai, String noiDung) {
        this.khachHangId = khachHangId;
        this.thoiGian = thoiGian;
        this.loai = loai;
        this.noiDung = noiDung;
    }

    public LichSuKhachHang(LichSuKhachHang other) {
        this(other.khachHangId, other.thoiGian, other.loai, other.noiDung);
    }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
}
