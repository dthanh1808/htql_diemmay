package dao;

import entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Dữ liệu tạm dùng chung cho các DAO trong một lần chạy ứng dụng. */
public class DuLieuTam {
    private static final DuLieuTam MAC_DINH = new DuLieuTam();
    private final ArrayList<DanhMuc> danhMuc = new ArrayList<>();
    private final ArrayList<SanPham> sanPham = new ArrayList<>();
    private int soSanPham = 1;
    private int soDanhMuc = 1;
    private final ArrayList<KhachHang> khachHang = new ArrayList<>();
    private final ArrayList<HangThanhVien> hangThanhVien = new ArrayList<>();
    private final ArrayList<GiaoDichKhachHang> giaoDichKhachHang = new ArrayList<>();
    private final ArrayList<LichSuKhachHang> lichSuKhachHang = new ArrayList<>();
    private final ArrayList<ThongBaoKhuyenMai> thongBaoKhuyenMai = new ArrayList<>();
    private int soKhachHang = 1;
    private int soThongBao = 1;

    public DuLieuTam() { taoDuLieuMau(); taoKhachHangMau(); }

    public static DuLieuTam getMacDinh() { return MAC_DINH; }

    // Chỉ DAO trong cùng package được thay đổi dữ liệu trong các ArrayList.
    ArrayList<DanhMuc> getDanhMuc() { return danhMuc; }
    ArrayList<SanPham> getSanPham() { return sanPham; }
    String taoMaSanPham() { return String.format("SP%04d", soSanPham++); }
    String taoMaDanhMuc() { return String.format("DM%03d", soDanhMuc++); }
    ArrayList<KhachHang> getKhachHang() { return khachHang; }
    ArrayList<HangThanhVien> getHangThanhVien() { return hangThanhVien; }
    ArrayList<GiaoDichKhachHang> getGiaoDichKhachHang() { return giaoDichKhachHang; }
    ArrayList<LichSuKhachHang> getLichSuKhachHang() { return lichSuKhachHang; }
    ArrayList<ThongBaoKhuyenMai> getThongBaoKhuyenMai() { return thongBaoKhuyenMai; }
    String taoMaKhachHang() { return String.format("KH%06d", soKhachHang++); }
    String taoMaThongBao() { return String.format("TB%06d", soThongBao++); }

    private void taoKhachHangMau() {
        hangThanhVien.add(new HangThanhVien("Member", BigDecimal.ZERO, 0));
        hangThanhVien.add(new HangThanhVien("Silver", new BigDecimal("1000000"), 2));
        hangThanhVien.add(new HangThanhVien("Gold", new BigDecimal("3000000"), 5));
        hangThanhVien.add(new HangThanhVien("Diamond", new BigDecimal("10000000"), 8));
        String[] ten = {"Mai Thanh Lan", "Nguyễn Minh Tú", "Phạm Quốc Huy", "Trần Hoài Nam",
                "Hoàng Ngọc Liên", "Lê Quang Dũng", "Vũ Phương Anh", "Đỗ Trung Kiên"};
        String[] sdt = {"0903458789", "0987224510", "0902615437", "0948118228",
                "0905820354", "0869741982", "0906204165", "0913507903"};
        String[] tien = {"12850000", "3860000", "1920000", "460000", "4520000", "1890000", "785000", "2060000"};
        for (int i = 0; i < ten.length; i++) {
            LocalDateTime luc = LocalDateTime.now().minusDays(30 - i);
            String id = taoMaKhachHang();
            khachHang.add(new KhachHang(id, ten[i], sdt[i], "khach" + (i + 1) + "@example.com",
                    "TP. Hồ Chí Minh", null, i != 4, i != 6, "Dữ liệu minh họa", luc, luc));
            lichSuKhachHang.add(new LichSuKhachHang(id, luc, "Đăng ký", "Tạo khách hàng mẫu"));
            giaoDichKhachHang.add(new GiaoDichKhachHang("HD-MAU-" + (i + 1), id,
                    luc.plusDays(1), new BigDecimal(tien[i]), "Mua hàng mẫu"));
            lichSuKhachHang.add(new LichSuKhachHang(id, luc.plusDays(1), "Mua hàng", "Hóa đơn HD-MAU-" + (i + 1)));
        }
    }

    private void taoDuLieuMau() {
        String[] tenDanhMuc = {"Tủ lạnh", "Máy giặt", "Tivi", "Máy lạnh", "Laptop", "Điện thoại", "Gia dụng", "Phụ kiện"};
        String[][] duLieu = {
            {"TL001", "Tủ lạnh Samsung Inverter 236 lít", "Samsung", "12000000", "24"},
            {"MG001", "Máy giặt LG Inverter FV1408", "LG", "8500000", "12"},
            {"TV001", "Smart Tivi Samsung 55 inch", "Samsung", "15990000", "24"},
            {"ML001", "Máy lạnh Daikin Inverter 1 HP", "Daikin", "13500000", "24"},
            {"LT001", "Laptop ASUS VivoBook 15", "ASUS", "14990000", "24"},
            {"DT001", "Điện thoại Samsung Galaxy A35", "Samsung", "9990000", "12"},
            {"GD001", "Nồi chiên không dầu Lock&Lock 5 lít", "Lock&Lock", "2190000", "12"},
            {"PK001", "Tai nghe Sony WH-CH520", "Sony", "1490000", "12"}
        };
        for (int i = 0; i < tenDanhMuc.length; i++) {
            DanhMuc dm = new DanhMuc(taoMaDanhMuc(), tenDanhMuc[i], "");
            danhMuc.add(dm);
            String[] dong = duLieu[i];
            LocalDateTime bayGio = LocalDateTime.now();
            sanPham.add(new SanPham(taoMaSanPham(), dong[0], dong[1], dm.getId(), dong[2],
                    new BigDecimal(dong[3]), Integer.parseInt(dong[4]), i != 6, "", "", bayGio, bayGio));
        }
    }
}
