import dao.*;
import entity.*;
import java.time.LocalDate;
import java.util.ArrayList;
import ui.UIHelper;

/** Chạy bằng Java, không cần JUnit, SQL hay thư viện ngoài. */
public class ChucNangTest {
    private static int soKiemTra;

    private static void dung(boolean dieuKien, String ten) {
        soKiemTra++;
        if (!dieuKien) throw new AssertionError(ten);
    }

    private static void phaiBaoLoi(Runnable thaoTac, String ten) {
        boolean coLoi = false;
        try { thaoTac.run(); }
        catch (IllegalArgumentException e) { coLoi = true; }
        dung(coLoi, ten);
    }

    private static SanPham sanPham(String sku, String vach, double gia, int baoHanh) {
        return new SanPham(sku, vach, "Sản phẩm kiểm tra", new DanhMucDAO().layTatCa().get(0), "Hãng thử", "MODEL-TEST",
            gia, baoHanh, "Gọn nhẹ", "Mô tả", "", true);
    }

    private static KhachHang khachHang(String sdt, String email) {
        return new KhachHang("", "Khách kiểm tra", sdt, email, LocalDate.of(2000, 1, 1), "Khác", "TP.HCM", LocalDate.now(), true);
    }

    public static void main(String[] args) {
        DanhMucDAO dmDAO = new DanhMucDAO();
        SanPhamDAO spDAO = new SanPhamDAO();
        KhachHangDAO khDAO = new KhachHangDAO();
        HoaDonDAO hdDAO = new HoaDonDAO();
        ChinhSachThanhVienDAO csDAO = new ChinhSachThanhVienDAO();
        ChamSocKhachHangDAO cskhDAO = new ChamSocKhachHangDAO();

        // Danh mục: kiểm tra trùng, tham chiếu và xóa.
        int soDM = dmDAO.layTatCa().size();
        dmDAO.them("Danh mục thử");
        DanhMuc dm = dmDAO.layTatCa().get(soDM);
        dung(dmDAO.layTatCa().size() == soDM + 1, "Thêm danh mục");
        phaiBaoLoi(() -> dmDAO.them(" danh mục thử "), "Chặn tên danh mục trùng");
        phaiBaoLoi(() -> dmDAO.them("  "), "Chặn tên danh mục trống");
        dmDAO.sua(dm.getMaDanhMuc(), "Đổi tên danh mục");
        dung(dm.getTenDanhMuc().equals("Đổi tên danh mục"), "Sửa danh mục");
        dmDAO.xoa(dm.getMaDanhMuc());
        dung(dmDAO.layTatCa().size() == soDM, "Xóa danh mục chưa dùng");
        phaiBaoLoi(() -> dmDAO.xoa("DM001"), "Chặn xóa danh mục có sản phẩm");
        dmDAO.sua("DM001", "Tủ lạnh cập nhật");
        dung(spDAO.timTheoMa("TL001").getDanhMuc().getTenDanhMuc().equals("Tủ lạnh cập nhật"), "Tên danh mục cập nhật trên sản phẩm");
        dmDAO.sua("DM001", "Tủ lạnh");

        // Sản phẩm: dữ liệu hợp lệ, không trùng SKU/mã vạch, lọc và xóa an toàn.
        spDAO.them(sanPham("TEST001", "TEST-BARCODE", 1000000, 12));
        dung(new SanPhamDAO().timTheoMa("TEST001") != null, "Dữ liệu sản phẩm dùng chung giữa các DAO");
        phaiBaoLoi(() -> spDAO.them(sanPham("test001", "NEW-BARCODE", 1000000, 12)), "Chặn SKU trùng không phân biệt hoa thường");
        phaiBaoLoi(() -> spDAO.them(sanPham("TEST002", "test-barcode", 1000000, 12)), "Chặn mã vạch trùng");
        phaiBaoLoi(() -> spDAO.them(sanPham("TEST002", "NEW-BARCODE", 0, 12)), "Chặn giá bằng 0");
        phaiBaoLoi(() -> spDAO.them(sanPham("TEST002", "NEW-BARCODE", Double.NaN, 12)), "Chặn giá NaN");
        phaiBaoLoi(() -> spDAO.them(sanPham("TEST002", "NEW-BARCODE", 1000000, -1)), "Chặn bảo hành âm");
        dung(spDAO.timKiem("TEST-BARCODE", "DM001", "Hãng thử", 1000000, 1000000, "Đang kinh doanh", 12).size() == 1, "Lọc kết hợp và tìm theo mã vạch");
        SanPham suaSP = sanPham("TEST001", "TEST-BARCODE", 2000000, 24);
        spDAO.sua(suaSP);
        dung(spDAO.timTheoMa("TEST001").getGiaBan() == 2000000, "Sửa sản phẩm");
        spDAO.doiTrangThai("TEST001");
        dung(!spDAO.timTheoMa("TEST001").isKinhDoanh(), "Ngưng kinh doanh");
        dung(spDAO.xoa("TEST001") && spDAO.timTheoMa("TEST001") == null, "Xóa sản phẩm chưa có hóa đơn");
        dung(!spDAO.xoa("TL001") && spDAO.timTheoMa("TL001") != null && !spDAO.timTheoMa("TL001").isKinhDoanh(), "Sản phẩm có hóa đơn chỉ được ngưng kinh doanh");
        spDAO.doiTrangThai("TL001");

        // Khách hàng: đăng ký, cập nhật, kiểm tra dữ liệu và tìm kiếm.
        KhachHang kh = khachHang("0999999901", "test@example.com");
        khDAO.them(kh);
        String maKH = kh.getMaKhachHang();
        dung(kh.getTongChiTieu() == 0 && kh.getHangThanhVien().equals("Member"), "Khách mới mặc định Member, chi tiêu 0");
        dung(kh.getMaTheThanhVien().startsWith("TV"), "Sinh mã thẻ thành viên");
        phaiBaoLoi(() -> khDAO.them(khachHang("0999999901", "")), "Chặn SĐT trùng");
        phaiBaoLoi(() -> khDAO.them(khachHang("123", "")), "Chặn SĐT sai định dạng");
        phaiBaoLoi(() -> khDAO.them(khachHang("0999999902", "abc")), "Chặn email sai định dạng");
        KhachHang suaKH = new KhachHang(maKH, "Khách đã cập nhật", kh.getSoDienThoai(), "update@example.com", null, "Nam", "Địa chỉ mới", LocalDate.of(2001, 1, 1), true);
        khDAO.sua(suaKH);
        dung(khDAO.timTheoMa(maKH).getHoTen().equals("Khách đã cập nhật"), "Sửa khách hàng");
        dung(khDAO.timTheoMa(maKH).getNgayDangKy().equals(kh.getNgayDangKy()), "Giữ ngày đăng ký khi sửa");
        dung(khDAO.timKiem(kh.getMaTheThanhVien(), "Member", "Đang hoạt động", 0, 0, null, null).size() == 1, "Tìm kiếm mã thẻ và bộ lọc khách hàng");
        dung(khDAO.timKiem("", "Tất cả", "Tất cả", 0, Double.MAX_VALUE, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)).size() == 1, "Lọc ngày đăng ký gồm cả hai biên");
        dung(!khDAO.xoa("KH000123") && !khDAO.timTheoMa("KH000123").isHoatDong(), "Khách có hóa đơn chỉ được ngưng hoạt động");
        khDAO.doiTrangThai("KH000123");

        // Hạng, chính sách và hoàn tiền.
        dung(csDAO.xacDinhHang(9999999).getTenHang().equals("Member"), "Dưới 10 triệu: Member");
        dung(csDAO.xacDinhHang(10000000).getTenHang().equals("Silver"), "Đúng 10 triệu: Silver");
        dung(csDAO.xacDinhHang(30000000).getTenHang().equals("Gold"), "Đúng 30 triệu: Gold");
        dung(csDAO.xacDinhHang(60000000).getTenHang().equals("Diamond"), "Đúng 60 triệu: Diamond");
        dung(khDAO.timTheoMa("KH000123").getTongChiTieu() == 63990000, "Tổng chi tiêu tính từ các hóa đơn");
        hdDAO.hoanTienMau("HD2609250001", 4000000);
        dung(khDAO.timTheoMa("KH000123").getHangThanhVien().equals("Gold"), "Hoàn tiền cập nhật lại hạng");
        dung(khDAO.timTheoMa("KH000123").getTongChiTieu() == 59990000, "Hoàn tiền giảm tổng chi tiêu");
        phaiBaoLoi(() -> hdDAO.hoanTienMau("HD2609250001", 999999999), "Chặn hoàn quá tiền còn lại");
        phaiBaoLoi(() -> hdDAO.hoanTienMau("HD2609250001", 0), "Chặn hoàn tiền 0");
        ArrayList<ChinhSachThanhVien> cu = csDAO.layTatCa();
        ArrayList<ChinhSachThanhVien> moi = csDAO.layTatCa();
        moi.set(1, new ChinhSachThanhVien("Silver", 20000000, 4));
        csDAO.luu(moi);
        dung(khDAO.timTheoMa("KH000125").getHangThanhVien().equals("Member"), "Đổi chính sách tính lại tất cả hạng");
        dung(csDAO.xacDinhHang(20000000).getPhanTramGiam() == 4, "Đổi ưu đãi thành viên");
        ArrayList<ChinhSachThanhVien> sai = csDAO.layTatCa();
        sai.set(2, new ChinhSachThanhVien("Gold", 1000000, 5));
        phaiBaoLoi(() -> csDAO.luu(sai), "Chặn ngưỡng hạng không tăng dần");
        dung(csDAO.layTatCa().get(1).getChiTieuToiThieu() == 20000000, "Chính sách sai không làm thay đổi dữ liệu");
        csDAO.luu(cu);

        // CSKH và thông báo mô phỏng: không có lời gọi gửi ra ngoài.
        cskhDAO.themGhiChu(maKH, "Điện thoại", "Khách cần tư vấn thêm");
        dung(cskhDAO.layLichSu(maKH).size() == 1, "Ghi nhận lịch sử CSKH");
        phaiBaoLoi(() -> cskhDAO.themGhiChu(maKH, "Điện thoại", "  "), "Chặn ghi chú trống");
        dung(cskhDAO.chonDoiTuong("", "Tất cả", "DM001").size() == 1, "Lọc đối tượng theo danh mục đã mua");
        ArrayList<String> nguoiNhan = new ArrayList<>(); nguoiNhan.add(maKH); nguoiNhan.add(maKH);
        dung(cskhDAO.guiMoPhong("KM001", nguoiNhan, "Email", "Xin chào {ten}") == 1, "Không gửi trùng người nhận");
        LichSuCSKH log = cskhDAO.layLichSu(maKH).get(0);
        dung(log.getTrangThai().contains("Mô phỏng") && log.getNoiDung().contains("Khách đã cập nhật"), "Lịch sử ghi rõ mô phỏng và thay tên người nhận");
        KhachHang khKhongEmail = khachHang("0999999902", ""); khDAO.them(khKhongEmail);
        int soLog = cskhDAO.layLichSu(maKH).size();
        nguoiNhan.clear(); nguoiNhan.add(maKH); nguoiNhan.add(khKhongEmail.getMaKhachHang());
        phaiBaoLoi(() -> cskhDAO.guiMoPhong("KM001", nguoiNhan, "Email", "Test"), "Chặn gửi email cho khách chưa có email");
        dung(cskhDAO.layLichSu(maKH).size() == soLog, "Không lưu dở một phần khi người nhận không hợp lệ");
        nguoiNhan.clear(); nguoiNhan.add("KH000127");
        phaiBaoLoi(() -> cskhDAO.guiMoPhong("KM001", nguoiNhan, "SMS", "Test"), "Chặn gửi cho khách ngưng hoạt động");
        dung(khDAO.xoa(maKH) && cskhDAO.layLichSu(maKH).isEmpty(), "Xóa khách chưa giao dịch và dọn ghi chú liên quan");
        dung(khDAO.xoa(khKhongEmail.getMaKhachHang()), "Xóa khách mẫu chưa giao dịch");

        phaiBaoLoi(() -> UIHelper.ngay("31/02/2026", "Ngày"), "Chặn ngày không có thật");
        phaiBaoLoi(() -> UIHelper.soTien("Infinity", "Tiền"), "Chặn số tiền vô cực");
        dung(UIHelper.ngay("29/02/2024", "Ngày").equals(LocalDate.of(2024, 2, 29)), "Chấp nhận ngày nhuận hợp lệ");
        System.out.println("PASS: " + soKiemTra + " kiểm tra nghiệp vụ.");
    }
}
