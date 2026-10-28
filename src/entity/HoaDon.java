package entity;

import java.time.LocalDate;
import java.util.ArrayList;

/** Hóa đơn mẫu đã thanh toán, phục vụ tra cứu và tính chi tiêu thành viên. */
public class HoaDon {
    private String maHoaDon, maKhachHang;
    private LocalDate ngayLap;
    private ArrayList<ChiTietHoaDon> chiTiet = new ArrayList<>();
    private double tienHoan;

    public HoaDon(String maHoaDon, String maKhachHang, LocalDate ngayLap) {
        this.maHoaDon = maHoaDon;
        this.maKhachHang = maKhachHang;
        this.ngayLap = ngayLap;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public String getMaKhachHang() { return maKhachHang; }
    public LocalDate getNgayLap() { return ngayLap; }
    public ArrayList<ChiTietHoaDon> getChiTiet() { return new ArrayList<>(chiTiet); }
    public void themChiTiet(ChiTietHoaDon ct) { chiTiet.add(ct); }
    public double getTienHoan() { return tienHoan; }
    public void setTienHoan(double tienHoan) { this.tienHoan = tienHoan; }

    public double getTongTien() {
        double tong = 0;
        for (ChiTietHoaDon ct : chiTiet) tong += ct.getThanhTien();
        return tong;
    }

    public double getChiTieuRong() { return getTongTien() - tienHoan; }
}
