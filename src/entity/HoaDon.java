package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Giá và ưu đãi là bản chụp tại thời điểm bán, không phụ thuộc bảng giá hiện tại. */
public record HoaDon(String ma, LocalDateTime ngay, String nhanVien, String ca,
        String khachHang, String sdt, String hang, String phuongThuc, String trangThai,
        BigDecimal giamHoaDon, BigDecimal khachDua, String giaoHang, String khuyenMai,
        String doiTac, int kyHan, List<Dong> chiTiet) {
    public static final String DA_TT = "Đã thanh toán", TRA_GOP = "Trả góp";
    public HoaDon { chiTiet = List.copyOf(chiTiet); }
    public record Dong(String sku, String ten, String danhMuc, int soLuong,
            BigDecimal donGia, BigDecimal giamGia) {
        public BigDecimal tienHang() { return donGia.multiply(BigDecimal.valueOf(soLuong)); }
        public BigDecimal thanhTien() { return tienHang().subtract(giamGia); }
    }
    public BigDecimal tienHang() {
        return chiTiet.stream().map(Dong::tienHang).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal giamGia() {
        return chiTiet.stream().map(Dong::giamGia).reduce(giamHoaDon, BigDecimal::add);
    }
    public BigDecimal tongTien() { return tienHang().subtract(giamGia()); }
    public boolean ghiNhanDoanhThu() { return DA_TT.equals(trangThai) || TRA_GOP.equals(trangThai); }
    public HoaDon voiMa(String id) {
        return new HoaDon(id, ngay, nhanVien, ca, khachHang, sdt, hang, phuongThuc,
                trangThai, giamHoaDon, khachDua, giaoHang, khuyenMai, doiTac, kyHan, chiTiet);
    }
}
