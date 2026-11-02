package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Dữ liệu trong phiên chạy; DAO trả về bản sao để tránh thay đổi ngoài ý muốn. */
public class GiaoDichKhachHang {
    private String maHoaDon;
    private String khachHangId;
    private LocalDateTime thoiGian;
    private BigDecimal soTien;
    private String noiDung;

    public GiaoDichKhachHang() { }

    public GiaoDichKhachHang(String maHoaDon, String khachHangId, LocalDateTime thoiGian, BigDecimal soTien, String noiDung) {
        this.maHoaDon = maHoaDon;
        this.khachHangId = khachHangId;
        this.thoiGian = thoiGian;
        this.soTien = soTien;
        this.noiDung = noiDung;
    }

    public GiaoDichKhachHang(GiaoDichKhachHang other) {
        this(other.maHoaDon, other.khachHangId, other.thoiGian, other.soTien, other.noiDung);
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
}
