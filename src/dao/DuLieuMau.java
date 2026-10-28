package dao;

import entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Dữ liệu dùng chung trong một lần chạy. Tắt ứng dụng sẽ mất thay đổi. */
class DuLieuMau {
    static final ArrayList<DanhMuc> danhMuc = new ArrayList<>();
    static final ArrayList<SanPham> sanPham = new ArrayList<>();
    static final ArrayList<HoaDon> hoaDon = new ArrayList<>();
    static final ArrayList<KhachHang> khachHang = new ArrayList<>();
    static final ArrayList<ChinhSachThanhVien> chinhSach = new ArrayList<>();
    static final ArrayList<KhuyenMai> khuyenMai = new ArrayList<>();
    static final ArrayList<LichSuCSKH> lichSuCSKH = new ArrayList<>();
    static int soDanhMuc = 9;
    static int soKhachHang = 135;

    static {
        String[] ten = {"Tủ lạnh", "Máy giặt", "Tivi", "Điều hòa", "Laptop", "Điện thoại", "Gia dụng", "Phụ kiện"};
        for (int i = 0; i < ten.length; i++) {
            danhMuc.add(new DanhMuc(String.format("DM%03d", i + 1), ten[i]));
        }
        String[] sku = {"TL001", "MG001", "TV001", "ML001", "LT001", "DT001", "GD001", "PK001"};
        String[] tenSP = {"Tủ lạnh Samsung Inverter 305L", "Máy giặt LG Inverter 9kg", "Smart Tivi Samsung 55 inch",
            "Điều hòa Daikin Inverter 1HP", "Laptop ASUS VivoBook 15", "Điện thoại Samsung Galaxy A55",
            "Nồi chiên không dầu Lock&Lock", "Tai nghe Sony WH-CH520"};
        String[] hang = {"Samsung", "LG", "Samsung", "Daikin", "ASUS", "Samsung", "Lock&Lock", "Sony"};
        double[] gia = {12000000, 8500000, 15990000, 13500000, 14990000, 9990000, 2190000, 1490000};
        for (int i = 0; i < sku.length; i++) {
            sanPham.add(new SanPham(sku[i], "893000000000" + i, tenSP[i], danhMuc.get(i), hang[i], sku[i] + "-2026",
                gia[i], i < 5 ? 24 : 12, i < 4 ? "Cồng kềnh" : "Gọn nhẹ", "Thông tin sản phẩm mẫu.", "", i != 6));
        }
        themHoaDon("HD2609200001", "KH000123", LocalDate.of(2026, 9, 20), 0, 4);
        themHoaDon("HD2609250001", "KH000123", LocalDate.of(2026, 9, 25), 2, 1);
        themHoaDon("HD2609260001", "KH000124", LocalDate.of(2026, 9, 26), 4, 3);
        themHoaDon("HD2610010001", "KH000125", LocalDate.of(2026, 10, 1), 5, 2);
        themHoaDon("HD2610020001", "KH000126", LocalDate.of(2026, 10, 2), 7, 1);
        themHoaDon("HD2610030001", "KH000127", LocalDate.of(2026, 10, 3), 1, 1);
        themHoaDon("HD2610040001", "KH000128", LocalDate.of(2026, 10, 4), 3, 1);
        themHoaDon("HD2610050001", "KH000129", LocalDate.of(2026, 10, 5), 6, 1);
        String[] tenKH = {"Mai Thanh Lan", "Nguyễn Minh Tú", "Phạm Quốc Huy", "Trần Hoài Nam", "Hoàng Ngọc Liên",
            "Lê Quang Dũng", "Vũ Phương Anh", "Đỗ Trung Kiên", "Nguyễn Ngọc Hà", "Trần Minh Anh", "Lê Quốc Bảo", "Phạm Gia Hân"};
        String[] sdt = {"0903458789", "0987224510", "0902615437", "0948118228", "0905820354", "0869741982",
            "0906204165", "0913507903", "0911223344", "0922334455", "0933445566", "0944556677"};
        for (int i = 0; i < tenKH.length; i++) {
            khachHang.add(new KhachHang(String.format("KH%06d", 123 + i), tenKH[i], sdt[i], "khach" + (i + 1) + "@example.com",
                LocalDate.of(1990 + i, 5, 10), i % 2 == 0 ? "Nữ" : "Nam", "TP. Hồ Chí Minh",
                LocalDate.of(2026, 1, 1).plusDays(i * 7), i != 4));
        }
        chinhSach.add(new ChinhSachThanhVien("Member", 0, 0));
        chinhSach.add(new ChinhSachThanhVien("Silver", 10000000, 3));
        chinhSach.add(new ChinhSachThanhVien("Gold", 30000000, 5));
        chinhSach.add(new ChinhSachThanhVien("Diamond", 60000000, 8));
        khuyenMai.add(new KhuyenMai("KM001", "Ưu đãi điện máy cuối tháng", "Xin chào {ten}, Gia Thành đang có ưu đãi điện máy cuối tháng. Mời quý khách tham khảo tại cửa hàng."));
        khuyenMai.add(new KhuyenMai("KM002", "Tri ân thành viên", "Xin chào {ten}, Gia Thành gửi lời tri ân và mời quý khách tham gia chương trình ưu đãi thành viên."));
        lichSuCSKH.add(new LichSuCSKH("KH000123", LocalDateTime.of(2026, 10, 10, 9, 30), "Ghi chú", "Điện thoại", "Khách hỏi về thời hạn bảo hành tivi.", "Dữ liệu mẫu"));
        lichSuCSKH.add(new LichSuCSKH("KH000124", LocalDateTime.of(2026, 10, 12, 14, 0), "Ghi chú", "Tại cửa hàng", "Đã hướng dẫn sử dụng laptop.", "Dữ liệu mẫu"));
    }

    private static void themHoaDon(String ma, String maKH, LocalDate ngay, int viTriSP, int soLuong) {
        HoaDon hd = new HoaDon(ma, maKH, ngay);
        SanPham sp = sanPham.get(viTriSP);
        hd.themChiTiet(new ChiTietHoaDon(sp, soLuong, sp.getGiaBan()));
        hoaDon.add(hd);
    }
}
