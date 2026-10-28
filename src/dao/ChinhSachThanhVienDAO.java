package dao;

import entity.ChinhSachThanhVien;
import java.util.ArrayList;

public class ChinhSachThanhVienDAO {
    public ArrayList<ChinhSachThanhVien> layTatCa() { return new ArrayList<>(DuLieuMau.chinhSach); }

    public ChinhSachThanhVien xacDinhHang(double chiTieu) {
        ChinhSachThanhVien ketQua = DuLieuMau.chinhSach.get(0);
        for (ChinhSachThanhVien cs : DuLieuMau.chinhSach) {
            if (chiTieu >= cs.getChiTieuToiThieu()) ketQua = cs;
        }
        return ketQua;
    }

    public void luu(ArrayList<ChinhSachThanhVien> danhSach) {
        String[] ten = {"Member", "Silver", "Gold", "Diamond"};
        if (danhSach.size() != 4) throw new IllegalArgumentException("Cần đủ bốn hạng thành viên.");
        double mocTruoc = -1;
        for (int i = 0; i < danhSach.size(); i++) {
            ChinhSachThanhVien cs = danhSach.get(i);
            if (!ten[i].equals(cs.getTenHang())) throw new IllegalArgumentException("Thứ tự hạng: Member, Silver, Gold, Diamond.");
            if (!Double.isFinite(cs.getChiTieuToiThieu()) || cs.getChiTieuToiThieu() < 0 || cs.getChiTieuToiThieu() <= mocTruoc) {
                throw new IllegalArgumentException("Ngưỡng chi tiêu phải không âm và tăng dần theo hạng.");
            }
            if (!Double.isFinite(cs.getPhanTramGiam()) || cs.getPhanTramGiam() < 0 || cs.getPhanTramGiam() > 70) {
                throw new IllegalArgumentException("Ưu đãi phải từ 0% đến 70% theo giới hạn giảm giá hóa đơn.");
            }
            if (i == 0 && (cs.getChiTieuToiThieu() != 0 || cs.getPhanTramGiam() != 0)) {
                throw new IllegalArgumentException("Hạng Member có ngưỡng 0 và ưu đãi 0%.");
            }
            mocTruoc = cs.getChiTieuToiThieu();
        }
        DuLieuMau.chinhSach.clear();
        DuLieuMau.chinhSach.addAll(danhSach);
        new KhachHangDAO().capNhatThongKe();
    }
}
