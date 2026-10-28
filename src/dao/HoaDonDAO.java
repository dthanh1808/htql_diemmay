package dao;

import entity.*;
import java.util.ArrayList;

public class HoaDonDAO {
    public ArrayList<HoaDon> layTheoKhachHang(String maKH) {
        ArrayList<HoaDon> ketQua = new ArrayList<>();
        for (HoaDon hd : DuLieuMau.hoaDon) if (hd.getMaKhachHang().equals(maKH)) ketQua.add(hd);
        return ketQua;
    }

    public boolean daMuaDanhMuc(String maKH, String maDM) {
        for (HoaDon hd : layTheoKhachHang(maKH)) {
            for (ChiTietHoaDon ct : hd.getChiTiet()) if (ct.getMaDanhMuc().equals(maDM)) return true;
        }
        return false;
    }

    /** Chỉ mô phỏng hoàn tiền trên hóa đơn mẫu, không thực hiện thanh toán. */
    public void hoanTienMau(String maHD, double soTien) {
        HoaDon hd = null;
        for (HoaDon item : DuLieuMau.hoaDon) if (item.getMaHoaDon().equals(maHD)) hd = item;
        if (hd == null) throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
        if (!Double.isFinite(soTien) || soTien <= 0 || soTien > hd.getChiTieuRong()) {
            throw new IllegalArgumentException("Tiền hoàn phải lớn hơn 0 và không vượt số tiền còn lại của hóa đơn.");
        }
        hd.setTienHoan(hd.getTienHoan() + soTien);
        new KhachHangDAO().capNhatThongKe();
    }
}
