package dao;

import entity.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ChamSocKhachHangDAO {
    public ArrayList<KhuyenMai> layKhuyenMai() { return new ArrayList<>(DuLieuMau.khuyenMai); }

    public ArrayList<LichSuCSKH> layLichSu(String maKH) {
        ArrayList<LichSuCSKH> ketQua = new ArrayList<>();
        for (int i = DuLieuMau.lichSuCSKH.size() - 1; i >= 0; i--) {
            LichSuCSKH item = DuLieuMau.lichSuCSKH.get(i);
            if (item.getMaKhachHang().equals(maKH)) ketQua.add(item);
        }
        return ketQua;
    }

    public void themGhiChu(String maKH, String kenh, String noiDung) {
        if (new KhachHangDAO().timTheoMa(maKH) == null) throw new IllegalArgumentException("Khách hàng không tồn tại.");
        if (noiDung.trim().isEmpty() || kenh.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập kênh liên hệ và nội dung chăm sóc.");
        DuLieuMau.lichSuCSKH.add(new LichSuCSKH(maKH, LocalDateTime.now(), "Ghi chú", kenh.trim(), noiDung.trim(), "Đã ghi nhận"));
    }

    public ArrayList<KhachHang> chonDoiTuong(String tuKhoa, String hang, String maDM) {
        ArrayList<KhachHang> ketQua = new ArrayList<>();
        for (KhachHang kh : new KhachHangDAO().timKiem(tuKhoa, hang, "Đang hoạt động", 0, Double.MAX_VALUE, null, null)) {
            if (maDM.isEmpty() || new HoaDonDAO().daMuaDanhMuc(kh.getMaKhachHang(), maDM)) ketQua.add(kh);
        }
        return ketQua;
    }

    /** Chỉ ghi bản ghi mô phỏng vào bộ nhớ, không gọi SMS, Email hay Zalo. */
    public int guiMoPhong(String maKM, ArrayList<String> danhSachMaKH, String kenh, String noiDung) {
        KhuyenMai km = null;
        for (KhuyenMai item : DuLieuMau.khuyenMai) if (item.getMaKhuyenMai().equals(maKM)) km = item;
        if (km == null) throw new IllegalArgumentException("Vui lòng chọn chương trình khuyến mãi.");
        if (danhSachMaKH.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn khách hàng nhận thông báo.");
        if (noiDung.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập nội dung thông báo.");
        if (!kenh.equals("SMS") && !kenh.equals("Email") && !kenh.equals("Zalo OA")) throw new IllegalArgumentException("Kênh thông báo không hợp lệ.");
        ArrayList<KhachHang> nguoiNhan = new ArrayList<>();
        ArrayList<String> daChon = new ArrayList<>();
        KhachHangDAO khDAO = new KhachHangDAO();
        for (String ma : danhSachMaKH) {
            if (daChon.contains(ma)) continue;
            KhachHang kh = khDAO.timTheoMa(ma);
            if (kh == null || !kh.isHoatDong()) throw new IllegalArgumentException("Khách hàng không tồn tại hoặc đã ngưng hoạt động: " + ma);
            if (kenh.equals("Email") && kh.getEmail().isEmpty()) throw new IllegalArgumentException("Khách hàng chưa có email: " + kh.getHoTen());
            daChon.add(ma);
            nguoiNhan.add(kh);
        }
        // Chỉ thêm lịch sử khi tất cả người nhận đều hợp lệ.
        for (KhachHang kh : nguoiNhan) {
            DuLieuMau.lichSuCSKH.add(new LichSuCSKH(kh.getMaKhachHang(), LocalDateTime.now(), "Khuyến mãi: " + km.getTenKhuyenMai(),
                kenh, noiDung.trim().replace("{ten}", kh.getHoTen()), "Mô phỏng - chưa gửi thực tế"));
        }
        return nguoiNhan.size();
    }
}
