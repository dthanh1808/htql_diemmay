package dao;

import entity.DanhMuc;
import entity.SanPham;
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

    public DuLieuTam() { taoDuLieuMau(); }

    public static DuLieuTam getMacDinh() { return MAC_DINH; }

    // Chỉ DAO trong cùng package được thay đổi hai ArrayList này.
    ArrayList<DanhMuc> getDanhMuc() { return danhMuc; }
    ArrayList<SanPham> getSanPham() { return sanPham; }
    String taoMaSanPham() { return String.format("SP%04d", soSanPham++); }
    String taoMaDanhMuc() { return String.format("DM%03d", soDanhMuc++); }

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
