package dao;

import entity.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

public class KhachHangDAO {
    public ArrayList<KhachHang> layTatCa() {
        capNhatThongKe();
        return new ArrayList<>(DuLieuMau.khachHang);
    }

    public KhachHang timTheoMa(String ma) {
        capNhatThongKe();
        for (KhachHang kh : DuLieuMau.khachHang) if (kh.getMaKhachHang().equals(ma)) return kh;
        return null;
    }

    public KhachHang timTheoSDT(String sdt) {
        for (KhachHang kh : layTatCa()) if (kh.getSoDienThoai().equals(sdt.trim())) return kh;
        return null;
    }

    public void them(KhachHang kh) {
        kiemTra(kh, false);
        kh.setMaKhachHang(String.format("KH%06d", DuLieuMau.soKhachHang++));
        kh.setNgayDangKy(LocalDate.now());
        kh.setHoatDong(true);
        kh.setTongChiTieu(0);
        kh.setHangThanhVien("Member");
        DuLieuMau.khachHang.add(kh);
    }

    public void sua(KhachHang kh) {
        kiemTra(kh, true);
        KhachHang cu = timTheoMa(kh.getMaKhachHang());
        kh.setNgayDangKy(cu.getNgayDangKy());
        DuLieuMau.khachHang.set(DuLieuMau.khachHang.indexOf(cu), kh);
        capNhatThongKe();
    }

    public boolean coHoaDon(String maKH) { return !new HoaDonDAO().layTheoKhachHang(maKH).isEmpty(); }

    public boolean xoa(String maKH) {
        KhachHang kh = timTheoMa(maKH);
        if (kh == null) throw new IllegalArgumentException("Khách hàng không tồn tại.");
        if (coHoaDon(maKH)) { kh.setHoatDong(false); return false; }
        DuLieuMau.khachHang.remove(kh);
        for (int i = DuLieuMau.lichSuCSKH.size() - 1; i >= 0; i--) {
            if (DuLieuMau.lichSuCSKH.get(i).getMaKhachHang().equals(maKH)) DuLieuMau.lichSuCSKH.remove(i);
        }
        return true;
    }

    public void doiTrangThai(String maKH) {
        KhachHang kh = timTheoMa(maKH);
        if (kh == null) throw new IllegalArgumentException("Khách hàng không tồn tại.");
        kh.setHoatDong(!kh.isHoatDong());
    }

    public void capNhatThongKe() {
        HoaDonDAO hdDAO = new HoaDonDAO();
        ChinhSachThanhVienDAO csDAO = new ChinhSachThanhVienDAO();
        for (KhachHang kh : DuLieuMau.khachHang) {
            double tong = 0;
            for (HoaDon hd : hdDAO.layTheoKhachHang(kh.getMaKhachHang())) tong += hd.getChiTieuRong();
            kh.setTongChiTieu(tong);
            kh.setHangThanhVien(csDAO.xacDinhHang(tong).getTenHang());
        }
    }

    public ArrayList<KhachHang> timKiem(String tuKhoa, String hang, String trangThai, double chiTu, double chiDen,
            LocalDate tuNgay, LocalDate denNgay) {
        ArrayList<KhachHang> ketQua = new ArrayList<>();
        String key = tuKhoa.trim().toLowerCase(Locale.ROOT);
        for (KhachHang kh : layTatCa()) {
            String text = kh.getMaKhachHang() + " " + kh.getMaTheThanhVien() + " " + kh.getHoTen() + " " + kh.getSoDienThoai() + " " + kh.getEmail();
            if (!text.toLowerCase(Locale.ROOT).contains(key)) continue;
            if (!hang.equals("Tất cả") && !kh.getHangThanhVien().equals(hang)) continue;
            if (trangThai.equals("Đang hoạt động") && !kh.isHoatDong()) continue;
            if (trangThai.equals("Ngưng hoạt động") && kh.isHoatDong()) continue;
            if (kh.getTongChiTieu() < chiTu || kh.getTongChiTieu() > chiDen) continue;
            if (tuNgay != null && kh.getNgayDangKy().isBefore(tuNgay)) continue;
            if (denNgay != null && kh.getNgayDangKy().isAfter(denNgay)) continue;
            ketQua.add(kh);
        }
        return ketQua;
    }

    private void kiemTra(KhachHang kh, boolean sua) {
        if (kh.getHoTen().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập họ tên khách hàng.");
        if (!kh.getSoDienThoai().matches("[0-9]{10}")) throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số.");
        if (!kh.getEmail().isEmpty() && !kh.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        if (kh.getNgaySinh() != null && kh.getNgaySinh().isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        if (sua && timTheoMa(kh.getMaKhachHang()) == null) throw new IllegalArgumentException("Khách hàng không tồn tại.");
        for (KhachHang other : DuLieuMau.khachHang) {
            if ((!sua || !other.getMaKhachHang().equals(kh.getMaKhachHang())) && other.getSoDienThoai().equals(kh.getSoDienThoai())) {
                throw new IllegalArgumentException("SĐT đã tồn tại: " + other.getMaKhachHang() + " - " + other.getHoTen() + ". Vui lòng tra cứu hồ sơ hiện có.");
            }
        }
    }
}
