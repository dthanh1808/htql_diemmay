import connect.ConnectDB;
import dao.*;
import entity.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;

/** Chạy tường minh trên DB kiểm thử riêng đã chạy script gốc + 02 + 04. */
public class ProductCustomerJdbcTest {
    private static int checks;
    private static final SanPhamDAO spDao = new SanPhamDAO();
    private static final DanhMucDAO dmDao = new DanhMucDAO();
    private static final KhachHangDAO khDao = new KhachHangDAO();

    private static void check(boolean ok, String text) {
        checks++; if (!ok) throw new AssertionError(text);
    }
    private static void rejects(Runnable action, String text) {
        try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError(text);
    }
    private static void sql(String text, Object... values) throws SQLException {
        try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(text)) {
            for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
            p.executeUpdate();
        }
    }
    private static SanPham sanPham(String sku, String dm, String gia) {
        SanPham sp = new SanPham(); sp.setSku(sku); sp.setTen("Điện máy thử nghiệm");
        sp.setDanhMucId(dm); sp.setThuongHieu("Gia Thành"); sp.setGiaBan(new BigDecimal(gia));
        sp.setBaoHanhThang(12); sp.setDangKinhDoanh(true); sp.setMoTa("Mô tả có dấu");
        return sp;
    }
    private static KhachHang khachHang(String phone) {
        KhachHang kh = new KhachHang(); kh.setHoTen("Đặng Thị Thử"); kh.setSoDienThoai(phone);
        kh.setEmail("thu@example.com"); kh.setDiaChi("Thành phố Hồ Chí Minh");
        kh.setNgaySinh(LocalDate.of(2000, 2, 29)); kh.setNhanKhuyenMai(true);
        kh.setGhiChu("Khách kiểm thử"); return kh;
    }
    private static JButton button(java.awt.Container root, String text) {
        for (java.awt.Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton)c).getText())) return (JButton)c;
            if (c instanceof java.awt.Container) {
                JButton b = button((java.awt.Container)c, text); if (b != null) return b;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        try (Connection c = ConnectDB.openConnection(); Statement p = c.createStatement();
                ResultSet r = p.executeQuery("SELECT DB_NAME()")) {
            r.next();
            if (!r.getString(1).startsWith("GiaThanhJdbcTest_"))
                throw new IllegalStateException("Chỉ chạy trên DB riêng tên GiaThanhJdbcTest_...; không chạy trên dữ liệu thật.");
        }
        check(spDao.layTatCa().isEmpty() && khDao.layTatCa().isEmpty() && dmDao.layTatCa().isEmpty(),
                "DB kiểm thử phải trống, không thêm dữ liệu mẫu");
        DanhMuc dm = dmDao.them(new DanhMuc(null, "Điện máy", "Mô tả danh mục"));
        check(dmDao.timTheoMa(dm.getId()) != null, "Danh mục được lưu SQL");
        rejects(() -> dmDao.them(new DanhMuc(null, "Điện máy", "")), "Chặn trùng danh mục");
        dm.setTen("Điện máy Gia Thành"); dmDao.capNhat(dm);
        check(dmDao.layTen(dm.getId()).equals(dm.getTen()), "Đổi tên danh mục");

        SanPham sp = spDao.them(sanPham("TEST_01", dm.getId(), "5000000"));
        check(spDao.timTheoMa(sp.getId()).getMoTa().equals("Mô tả có dấu"), "Đọc lại qua kết nối mới");
        check(dmDao.demSanPham(dm.getId()) == 1, "Đếm sản phẩm bằng SQL");
        check(spDao.timKiem("dien may gia thanh", dm.getId(), null, 2, 12, true).size() == 1, "Tìm không dấu và lọc kết hợp");
        check(spDao.timKiem("test_01", null, null, 0, null, null).size() == 1, "LIKE coi dấu gạch dưới là ký tự thường");
        check(spDao.timKiem("test%01", null, null, 0, null, null).isEmpty(), "Thoát ký tự %");
        check(spDao.timKiem("' OR 1=1--", null, null, 0, null, null).isEmpty(), "Tham số SQL injection");
        check(spDao.timKiem("", null, null, 1, null, null).isEmpty(), "Biên 5 triệu");
        check(spDao.layThuongHieu().contains("Gia Thành") && spDao.layBaoHanh().contains(12), "Nguồn combobox từ SQL");
        rejects(() -> spDao.them(sanPham("test_01", dm.getId(), "1")), "Chặn SKU trùng hoa thường");
        rejects(() -> spDao.them(sanPham("BAD", dm.getId(), "1.5")), "Chặn giá lẻ");
        rejects(() -> dmDao.xoa(dm.getId()), "Chặn xóa danh mục có sản phẩm");
        rejects(() -> spDao.xoa(sp.getId()), "Chặn xóa sản phẩm đang bán");
        SanPham sua = new SanPham(sp); sua.setSku("TEST_02"); spDao.capNhat(sua);
        check(spDao.timTheoMa(sp.getId()).getSku().equals("TEST_02"), "Đổi SKU chưa phát sinh giao dịch, giữ mã nội bộ");
        SanPham anh = new SanPham(sua);
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image,"png",bytes);
        anh.setAnhBase64(java.util.Base64.getEncoder().encodeToString(bytes.toByteArray())); spDao.capNhat(anh);
        check(spDao.timTheoMa(sp.getId()).getAnhBase64().equals(anh.getAnhBase64()), "Ảnh lưu trong SQL");
        SanPham inactive = new SanPham(anh); inactive.setDangKinhDoanh(false); spDao.capNhat(inactive); spDao.xoa(sp.getId());
        check(spDao.timTheoMa(sp.getId()) == null, "Ngừng và xóa sản phẩm trống");

        KhachHang kh = khDao.them(khachHang("+84 901 234 567"));
        check(kh.getSoDienThoai().equals("0901234567"), "Chuẩn hóa số điện thoại");
        check(khDao.layHang(kh.getId()).getTen().equals("Member") && khDao.tongChiTieu(kh.getId()).signum() == 0, "Khách mới Member, chi tiêu 0");
        check(khDao.layLichSu(kh.getId()).size() == 1, "Nhật ký đăng ký trong SQL");
        rejects(() -> khDao.them(khachHang("0901234567")), "Chặn trùng số điện thoại");
        KhachHang emailTrung = khachHang("0901234568");
        rejects(() -> khDao.them(emailTrung), "Chặn trùng email");
        KhachHang nguoiKhac = khachHang("0901234568"); nguoiKhac.setEmail(""); nguoiKhac.setNhanKhuyenMai(false);
        KhachHang kh2 = khDao.them(nguoiKhac);
        KhachHang nguoiThuBa = khachHang("0901234569"); nguoiThuBa.setEmail(""); nguoiThuBa.setNhanKhuyenMai(false);
        KhachHang kh3 = khDao.them(nguoiThuBa);
        check(khDao.timKiem("dang thi thu", null, true, LocalDate.now(), LocalDate.now()).size() == 3, "Tìm không dấu, biên ngày bao gồm hôm nay");
        rejects(() -> khDao.timKiem("", null, null, LocalDate.now(), LocalDate.now().minusDays(1)), "Ngày lọc ngược");
        KhachHang suaKh = new KhachHang(kh); suaKh.setDiaChi("Địa chỉ mới"); khDao.capNhat(suaKh);
        check(khDao.timTheoMa(kh.getId()).getDiaChi().equals("Địa chỉ mới"), "Cập nhật hồ sơ SQL");

        // Cố ý làm ghi nhật ký thất bại: UPDATE hồ sơ cũng phải rollback.
        sql("CREATE TRIGGER dbo.TRG_TestAudit ON dbo.LichSuKhachHang AFTER INSERT AS THROW 50009,N'Test rollback',1;");
        try {
            KhachHang fail = new KhachHang(suaKh); fail.setDiaChi("Không được lưu");
            rejects(() -> khDao.capNhat(fail), "Rollback nếu không ghi được nhật ký");
        } finally { sql("DROP TRIGGER dbo.TRG_TestAudit"); }
        check(khDao.timTheoMa(kh.getId()).getDiaChi().equals("Địa chỉ mới"), "Không lưu nửa transaction");
        ArrayList<HangThanhVien> policy = khDao.layChinhSach();
        policy.get(1).setPhanTramUuDai(4); khDao.luuChinhSach(policy);
        check(khDao.layChinhSach().get(1).getPhanTramUuDai() == 4, "Chính sách tồn tại qua kết nối mới");
        policy.get(1).setChiTieuToiThieu(BigDecimal.ZERO);
        final ArrayList<HangThanhVien> invalidPolicy = policy;
        rejects(() -> khDao.luuChinhSach(invalidPolicy), "Chặn chính sách ngưỡng không tăng");
        check(khDao.layChinhSach().get(1).getChiTieuToiThieu().signum() > 0, "Không ghi chính sách lỗi");

        sql("INSERT INTO dbo.NhanVien(maNV,hoTen,chucVu) VALUES ('NVTEST',N'Nhân viên test',N'Bán hàng')");
        SanPham ban = spDao.them(sanPham("SALE01", dm.getId(), "2000000"));
        sql("INSERT INTO dbo.HoaDon(maHD,tongTienHang,tienGiamGia,tongThanhToan,phuongThucThanhToan,trangThai,maNV,maKH) "
                + "VALUES ('HD0000000001',2000000,0,2000000,N'Tiền mặt',N'Đã thanh toán','NVTEST',?)",kh.getId());
        sql("INSERT INTO dbo.ChiTietHoaDon(maHD,maSKU,soLuong,donGia,thanhTien) VALUES ('HD0000000001',?,1,2000000,2000000)",ban.getSku());
        khDao.ghiNhanMuaHang(kh.getId(),"HD0000000001",new BigDecimal("2000000"),"Mua hàng test");
        check(khDao.tongChiTieu(kh.getId()).compareTo(new BigDecimal("2000000")) == 0, "Chi tiêu cập nhật");
        check(khDao.layHang(kh.getId()).getTen().equals("Silver"), "Trigger tự đổi hạng");
        check(khDao.layGiaoDich(kh.getId()).size() == 1, "Lịch sử đọc hóa đơn thật");
        rejects(() -> khDao.ghiNhanMuaHang(kh.getId(),"HD0000000001",new BigDecimal("2000000"),""), "Chặn cộng tiền hai lần");
        rejects(() -> khDao.ghiNhanMuaHang(kh.getId(),"KHONGCO",BigDecimal.ONE,""), "Không tạo giao dịch giả");
        check(khDao.tongChiTieu(kh.getId()).compareTo(new BigDecimal("2000000")) == 0, "Chặn trùng không đổi chi tiêu");
        SanPham doiSKU = new SanPham(ban); doiSKU.setSku("SALE02");
        rejects(() -> spDao.capNhat(doiSKU), "Không đổi SKU đã có hóa đơn");
        SanPham ngung = new SanPham(ban); ngung.setDangKinhDoanh(false); spDao.capNhat(ngung);
        rejects(() -> spDao.xoa(ban.getId()), "Giữ sản phẩm có lịch sử");

        check(khDao.guiKhuyenMai(null,"Khuyến mãi thử","Nội dung tiếng Việt") == 1, "Chỉ người đồng ý được nhận");
        check(khDao.layThongBao(kh.getId()).size() == 1 && khDao.layThongBao(kh2.getId()).isEmpty(), "Thông báo lưu đúng người");
        KhachHang ngungKh = khDao.timTheoMa(kh.getId()); ngungKh.setDangHoatDong(false); khDao.capNhat(ngungKh);
        check(!khDao.coTheXoa(kh.getId()), "Khách có nghiệp vụ không thể xóa");
        rejects(() -> khDao.xoa(kh.getId()), "Chặn xóa lịch sử khách");
        rejects(() -> khDao.guiKhuyenMai(null,"Thử","Không ai đủ điều kiện"), "Không gửi người ngừng hoặc từ chối");
        KhachHang xoaKh = khDao.timTheoMa(kh3.getId()); xoaKh.setDangHoatDong(false); khDao.capNhat(xoaKh); khDao.xoa(kh3.getId());
        check(khDao.timTheoMa(kh3.getId()) == null, "Xóa khách chưa có nghiệp vụ và cascade nhật ký");
        policy = khDao.layChinhSach(); policy.get(1).setChiTieuToiThieu(new BigDecimal("2500000")); khDao.luuChinhSach(policy);
        check(khDao.layHang(kh.getId()).getTen().equals("Member"), "Đổi ngưỡng tính lại hạng cả khách ngừng");
        sql("INSERT INTO dbo.SanPham(maSKU,tenSP,donGia,thuongHieu,maDanhMuc) VALUES ('LEGACY',N'Sản phẩm không ảnh',1000,N'Test',?)",dm.getId());
        check(spDao.timKiem("legacy",null,null,0,null,null).get(0).getAnhBase64().isEmpty(), "Đọc dữ liệu SQL cũ có ảnh NULL");
        SwingUtilities.invokeAndWait(() -> {
            check(button(new ui.PNL_SanPham(),"Thêm sản phẩm") != null, "Khởi tạo UI sản phẩm với JDBC");
            check(button(new ui.PNL_KhachHang(),"Đăng ký khách hàng") != null, "Khởi tạo UI khách hàng với JDBC");
        });
        System.out.println("PASS " + checks + " product/customer JDBC checks.");
    }
}
