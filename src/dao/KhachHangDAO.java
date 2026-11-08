package dao;

import connect.ConnectDB;
import entity.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/** Hồ sơ, lịch sử, chính sách và thông báo đều lưu trong SQL Server. */
public class KhachHangDAO {
    public KhachHangDAO() { }

    private KhachHang docDong(ResultSet r) throws SQLException {
        Date ngay = r.getDate("ngaySinh");
        KhachHang kh = new KhachHang(r.getString("maKH"), r.getString("tenKH"), r.getString("sdt"),
                chuoi(r.getString("email")), chuoi(r.getString("diaChi")), ngay == null ? null : ngay.toLocalDate(),
                r.getBoolean("trangThai"), r.getBoolean("nhanKhuyenMai"), chuoi(r.getString("ghiChu")),
                r.getTimestamp("taoLuc").toLocalDateTime(), r.getTimestamp("capNhatLuc").toLocalDateTime());
        boolean nam = r.getBoolean("gioiTinh"); kh.setGioiTinh(r.wasNull() ? null : nam);
        kh.setHangTV(r.getString("hangTV")); kh.setTongChiTieu(r.getBigDecimal("tongChiTieuTichLuy"));
        return kh;
    }

    public ArrayList<KhachHang> layTatCa() {
        ArrayList<KhachHang> ds = new ArrayList<>();
        try (Connection c = ConnectDB.openConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM dbo.KhachHang ORDER BY tenKH");
                ResultSet r = p.executeQuery()) {
            while (r.next()) ds.add(docDong(r));
            return ds;
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public int demTatCa() {
        try (Connection c = ConnectDB.openConnection();
                PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM dbo.KhachHang");
                ResultSet r = p.executeQuery()) {
            r.next(); return r.getInt(1);
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    private KhachHang timTheoMa(Connection c, String id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM dbo.KhachHang WHERE maKH=?")) {
            p.setString(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? docDong(r) : null; }
        }
    }

    public KhachHang timTheoMa(String id) {
        try (Connection c = ConnectDB.openConnection()) { return timTheoMa(c, id); }
        catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    private KhachHang batBuoc(Connection c, String id) throws SQLException {
        KhachHang kh = timTheoMa(c, id);
        if (kh == null) throw new IllegalArgumentException("Khách hàng không còn tồn tại.");
        return kh;
    }

    private static String chuoi(String text) { return text == null ? "" : text.trim(); }

    private void kiemTra(Connection c, KhachHang kh) throws SQLException {
        kh.setHoTen(chuoi(kh.getHoTen()).replaceAll("\\s+", " "));
        kh.setSoDienThoai(chuoi(kh.getSoDienThoai()).replaceAll("[ .()-]", ""));
        if (kh.getSoDienThoai().startsWith("+84")) kh.setSoDienThoai("0" + kh.getSoDienThoai().substring(3));
        kh.setEmail(chuoi(kh.getEmail()).toLowerCase(Locale.ROOT));
        kh.setDiaChi(chuoi(kh.getDiaChi())); kh.setGhiChu(chuoi(kh.getGhiChu()));
        if (kh.getHoTen().length() < 2 || kh.getHoTen().length() > 100
                || !kh.getHoTen().matches("[\\p{L}\\p{M} .'-]+") || !kh.getHoTen().matches(".*\\p{L}.*"))
            throw new IllegalArgumentException("Họ tên từ 2–100 ký tự, không chứa chữ số hoặc ký tự lạ.");
        if (!kh.getSoDienThoai().matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số di động gồm 10 chữ số, đầu 03/05/07/08/09 (hoặc +84).");
        if (!kh.getEmail().isEmpty() && (kh.getEmail().length() > 254 || kh.getEmail().startsWith(".")
                || kh.getEmail().contains(".@") || kh.getEmail().contains("..")
                || !kh.getEmail().matches("[A-Za-z0-9.!#$%&'*+/=?^_~{|}-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+")))
            throw new IllegalArgumentException("Email không hợp lệ.");
        if (kh.getDiaChi().length() > 300 || kh.getGhiChu().length() > 1000)
            throw new IllegalArgumentException("Địa chỉ tối đa 300 ký tự, ghi chú tối đa 1.000 ký tự.");
        if (kh.getNgaySinh() != null && (kh.getNgaySinh().isAfter(LocalDate.now())
                || kh.getNgaySinh().isBefore(LocalDate.now().minusYears(120))))
            throw new IllegalArgumentException("Ngày sinh phải trong vòng 120 năm và không ở tương lai.");
        try (PreparedStatement p = c.prepareStatement("SELECT sdt,email FROM dbo.KhachHang WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE maKH<>? AND (sdt=? OR (email COLLATE Latin1_General_100_CI_AS=? AND email<>''))")) {
            p.setString(1, kh.getId() == null ? "" : kh.getId()); p.setString(2, kh.getSoDienThoai());
            p.setString(3, kh.getEmail());
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) throw new IllegalArgumentException("Số điện thoại hoặc email đã được đăng ký, kể cả hồ sơ ngừng hoạt động.");
            }
        }
    }

    private void ganHoSo(PreparedStatement p, KhachHang kh) throws SQLException {
        p.setNString(1, kh.getHoTen()); p.setString(2, kh.getSoDienThoai());
        if (kh.getEmail().isEmpty()) p.setNull(3, Types.VARCHAR); else p.setString(3, kh.getEmail());
        p.setNString(4, kh.getDiaChi());
        p.setDate(5, kh.getNgaySinh() == null ? null : Date.valueOf(kh.getNgaySinh()));
        if (kh.getGioiTinh() == null) p.setNull(6, Types.BIT); else p.setBoolean(6, kh.getGioiTinh());
        p.setBoolean(7, kh.isDangHoatDong()); p.setBoolean(8, kh.isNhanKhuyenMai());
        p.setNString(9, kh.getGhiChu()); p.setString(10, kh.getId());
    }

    public KhachHang them(KhachHang input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập khách hàng.");
        try (Connection c = ConnectDB.openConnection()) {
            c.setAutoCommit(false);
            try {
                KhachHang kh = new KhachHang(input); kh.setId(null); kiemTra(c, kh);
                kh.setId(JdbcHelper.taoMa(c, "SELECT COALESCE(MAX(TRY_CONVERT(BIGINT,SUBSTRING(maKH,3,8))),0) "
                        + "FROM dbo.KhachHang WITH (UPDLOCK,HOLDLOCK) WHERE maKH LIKE 'KH%'", "KH", 6));
                kh.setDangHoatDong(true);
                String sql = "INSERT INTO dbo.KhachHang(tenKH,sdt,email,diaChi,ngaySinh,gioiTinh,trangThai,"
                        + "nhanKhuyenMai,ghiChu,maKH) VALUES (?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement p = c.prepareStatement(sql)) { ganHoSo(p, kh); p.executeUpdate(); }
                ghiLichSu(c, kh.getId(), "Đăng ký", "Đăng ký khách hàng");
                KhachHang daLuu = batBuoc(c, kh.getId()); c.commit(); return daLuu;
            } catch (SQLException | RuntimeException e) { JdbcHelper.rollback(c, e); throw e; }
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public KhachHang capNhat(KhachHang input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập khách hàng.");
        try (Connection c = ConnectDB.openConnection()) {
            c.setAutoCommit(false);
            try {
                KhachHang cu = batBuoc(c, input.getId());
                KhachHang kh = new KhachHang(input); kiemTra(c, kh);
                String sql = "UPDATE dbo.KhachHang SET tenKH=?,sdt=?,email=?,diaChi=?,ngaySinh=?,gioiTinh=?,"
                        + "trangThai=?,nhanKhuyenMai=?,ghiChu=?,capNhatLuc=SYSDATETIME() WHERE maKH=?";
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    ganHoSo(p, kh);
                    if (p.executeUpdate() != 1) throw new IllegalArgumentException("Khách hàng không còn tồn tại.");
                }
                String thayDoi = "Cập nhật hồ sơ";
                if (!Objects.equals(cu.getHoTen(), kh.getHoTen())) thayDoi += "; đổi họ tên";
                if (!Objects.equals(cu.getSoDienThoai(), kh.getSoDienThoai())) thayDoi += "; đổi số điện thoại";
                if (!Objects.equals(cu.getEmail(), kh.getEmail())) thayDoi += "; đổi email";
                if (!Objects.equals(cu.getDiaChi(), kh.getDiaChi())) thayDoi += "; đổi địa chỉ";
                if (!Objects.equals(cu.getNgaySinh(), kh.getNgaySinh())) thayDoi += "; đổi ngày sinh";
                if (!Objects.equals(cu.getGhiChu(), kh.getGhiChu())) thayDoi += "; đổi ghi chú";
                if (cu.isDangHoatDong() != kh.isDangHoatDong())
                    thayDoi += kh.isDangHoatDong() ? "; kích hoạt lại" : "; ngừng hoạt động";
                if (cu.isNhanKhuyenMai() != kh.isNhanKhuyenMai())
                    thayDoi += kh.isNhanKhuyenMai() ? "; đồng ý nhận khuyến mãi" : "; từ chối nhận khuyến mãi";
                ghiLichSu(c, kh.getId(), "Cập nhật", thayDoi);
                KhachHang daLuu = batBuoc(c, kh.getId()); c.commit(); return daLuu;
            } catch (SQLException | RuntimeException e) { JdbcHelper.rollback(c, e); throw e; }
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public boolean coTheXoa(String id) {
        String sql = "SELECT 1 FROM dbo.KhachHang k WHERE k.maKH=? AND k.trangThai=0 AND k.tongChiTieuTichLuy=0 "
                + "AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon WHERE maKH=k.maKH) "
                + "AND NOT EXISTS(SELECT 1 FROM dbo.PhieuBaoHanh WHERE maKH=k.maKH) "
                + "AND NOT EXISTS(SELECT 1 FROM dbo.ThongBaoKhuyenMai WHERE maKH=k.maKH)";
        try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            batBuoc(c, id); p.setString(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public void xoa(String id) {
        String sql = "DELETE FROM dbo.KhachHang WHERE maKH=? AND trangThai=0 AND tongChiTieuTichLuy=0 "
                + "AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.maKH=KhachHang.maKH) "
                + "AND NOT EXISTS(SELECT 1 FROM dbo.PhieuBaoHanh b WHERE b.maKH=KhachHang.maKH) "
                + "AND NOT EXISTS(SELECT 1 FROM dbo.ThongBaoKhuyenMai t WHERE t.maKH=KhachHang.maKH)";
        // Nhật ký hồ sơ có FK ON DELETE CASCADE; lịch sử nghiệp vụ chặn xóa bằng khóa ngoại.
        try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id);
            if (p.executeUpdate() != 1)
                throw new IllegalArgumentException("Chỉ xóa khách đã ngừng hoạt động, chưa có chi tiêu, hóa đơn, bảo hành hoặc thông báo.");
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    private static void ghiLichSu(Connection c, String id, String loai, String noiDung) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO dbo.LichSuKhachHang(maKH,loai,noiDung) VALUES (?,?,?)")) {
            p.setString(1, id); p.setNString(2, loai); p.setNString(3, noiDung); p.executeUpdate();
        }
    }

    public ArrayList<LichSuKhachHang> layLichSu(String id) {
        ArrayList<LichSuKhachHang> ds = new ArrayList<>();
        try (Connection c = ConnectDB.openConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM dbo.LichSuKhachHang WHERE maKH=? ORDER BY thoiGian DESC,id DESC")) {
            batBuoc(c, id); p.setString(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) ds.add(new LichSuKhachHang(id, r.getTimestamp("thoiGian").toLocalDateTime(),
                        r.getString("loai"), r.getString("noiDung")));
            }
            return ds;
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public ArrayList<GiaoDichKhachHang> layGiaoDich(String id) {
        ArrayList<GiaoDichKhachHang> ds = new ArrayList<>();
        String sql = "SELECT maHD,ngayLap,tongThanhToan FROM dbo.HoaDon WHERE maKH=? "
                + "AND trangThai IN (N'Đã thanh toán',N'Trả góp') ORDER BY ngayLap DESC,maHD DESC";
        try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            batBuoc(c, id); p.setString(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) ds.add(new GiaoDichKhachHang(r.getString("maHD"), id,
                        r.getTimestamp("ngayLap").toLocalDateTime(), r.getBigDecimal("tongThanhToan"), "Hóa đơn bán hàng"));
            }
            return ds;
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    /** Chỉ ghi nhận hóa đơn thật, trong cùng transaction lưu hóa đơn. Không tạo chi tiêu giả từ UI. */
    public static void ghiNhanMuaHang(Connection c, String id, String maHoaDon, BigDecimal soTien, String noiDung) throws SQLException {
        if (c.getAutoCommit()) throw new IllegalArgumentException("Ghi nhận mua hàng phải nằm trong transaction lưu hóa đơn.");
        kiemTraTien(soTien, false);
        if (chuoi(noiDung).length() > 500) throw new IllegalArgumentException("Nội dung giao dịch tối đa 500 ký tự.");
        String sql = "SELECT h.tongThanhToan FROM dbo.HoaDon h JOIN dbo.KhachHang k WITH (UPDLOCK,HOLDLOCK) "
                + "ON k.maKH=h.maKH WHERE h.maHD=? AND h.maKH=? AND k.trangThai=1 "
                + "AND h.trangThai IN (N'Đã thanh toán',N'Trả góp')";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maHoaDon); p.setString(2, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next() || r.getBigDecimal(1).compareTo(soTien) != 0)
                    throw new IllegalArgumentException("Hóa đơn không tồn tại, sai khách/số tiền hoặc khách đã ngừng hoạt động.");
            }
        }
        try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM dbo.LichSuKhachHang WITH (UPDLOCK,HOLDLOCK) WHERE maHD=?")) {
            p.setString(1, maHoaDon);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) throw new IllegalArgumentException("Hóa đơn này đã được ghi nhận.");
            }
        }
        try (PreparedStatement p = c.prepareStatement("INSERT INTO dbo.LichSuKhachHang(maKH,loai,noiDung,maHD) VALUES (?,N'Mua hàng',?,?)")) {
            p.setString(1, id); p.setNString(2, "Hóa đơn " + maHoaDon + ": " + soTien.toPlainString() + " VND. " + chuoi(noiDung));
            p.setString(3, maHoaDon); p.executeUpdate();
        }
        try (PreparedStatement p = c.prepareStatement("UPDATE dbo.KhachHang SET tongChiTieuTichLuy=tongChiTieuTichLuy+?,"
                + "capNhatLuc=SYSDATETIME() WHERE maKH=?")) {
            p.setBigDecimal(1, soTien); p.setString(2, id); p.executeUpdate();
        }
    }

    public void ghiNhanMuaHang(String id, String maHoaDon, BigDecimal soTien, String noiDung) {
        try (Connection c = ConnectDB.openConnection()) {
            c.setAutoCommit(false);
            try {
                ghiNhanMuaHang(c, id, chuoi(maHoaDon).toUpperCase(Locale.ROOT), soTien, noiDung); c.commit();
            } catch (SQLException | RuntimeException e) { JdbcHelper.rollback(c, e); throw e; }
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public BigDecimal tongChiTieu(String id) {
        try (Connection c = ConnectDB.openConnection()) { return batBuoc(c, id).getTongChiTieu(); }
        catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public ArrayList<HangThanhVien> layChinhSach() {
        ArrayList<HangThanhVien> ds = new ArrayList<>();
        try (Connection c = ConnectDB.openConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM dbo.HangThanhVien ORDER BY thuTu");
                ResultSet r = p.executeQuery()) {
            while (r.next()) ds.add(new HangThanhVien(r.getString("tenHang"),
                    r.getBigDecimal("chiTieuToiThieu"), r.getInt("phanTramUuDai")));
            if (ds.size() != 4) throw new IllegalArgumentException("Chưa đủ chính sách 4 hạng. Chạy sql/04_SanPhamKhachHang.sql.");
            return ds;
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public HangThanhVien layHang(String id) {
        String sql = "SELECT h.* FROM dbo.HangThanhVien h JOIN dbo.KhachHang k ON k.hangTV=h.tenHang WHERE k.maKH=?";
        try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Không tìm thấy khách hàng hoặc chính sách hạng.");
                return new HangThanhVien(r.getString("tenHang"), r.getBigDecimal("chiTieuToiThieu"), r.getInt("phanTramUuDai"));
            }
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public void luuChinhSach(ArrayList<HangThanhVien> input) {
        String[] ten = {"Member", "Silver", "Gold", "Diamond"};
        if (input == null || input.size() != 4) throw new IllegalArgumentException("Chính sách phải có đủ 4 hạng.");
        for (int i = 0; i < 4; i++) {
            HangThanhVien h = input.get(i);
            if (h == null || !ten[i].equals(h.getTen())) throw new IllegalArgumentException("Thứ tự hạng: Member, Silver, Gold, Diamond.");
            kiemTraTien(h.getChiTieuToiThieu(), true);
            if (i == 0 && h.getChiTieuToiThieu().signum() != 0)
                throw new IllegalArgumentException("Hạng Member bắt đầu từ 0 đ.");
            if (i > 0 && h.getChiTieuToiThieu().compareTo(input.get(i - 1).getChiTieuToiThieu()) <= 0)
                throw new IllegalArgumentException("Ngưỡng chi tiêu phải tăng dần.");
            if (h.getPhanTramUuDai() < 0 || h.getPhanTramUuDai() > 70
                    || (i > 0 && h.getPhanTramUuDai() < input.get(i - 1).getPhanTramUuDai()))
                throw new IllegalArgumentException("Ưu đãi từ 0–70% và không giảm ở hạng cao hơn.");
        }
        try (Connection c = ConnectDB.openConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement("UPDATE dbo.HangThanhVien WITH (UPDLOCK,HOLDLOCK) "
                        + "SET chiTieuToiThieu=?,phanTramUuDai=? WHERE tenHang=?")) {
                    for (HangThanhVien h : input) {
                        p.setBigDecimal(1, h.getChiTieuToiThieu()); p.setInt(2, h.getPhanTramUuDai()); p.setString(3, h.getTen());
                        if (p.executeUpdate() != 1) throw new IllegalArgumentException("Thiếu chính sách hạng trong database.");
                    }
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE k SET hangTV=(SELECT TOP 1 tenHang FROM dbo.HangThanhVien "
                        + "WHERE chiTieuToiThieu<=k.tongChiTieuTichLuy ORDER BY thuTu DESC) FROM dbo.KhachHang k")) { p.executeUpdate(); }
                try (PreparedStatement p = c.prepareStatement("INSERT INTO dbo.LichSuKhachHang(maKH,loai,noiDung) "
                        + "SELECT maKH,N'Chính sách',N'Cập nhật chính sách; tính lại hạng theo tổng chi tiêu' FROM dbo.KhachHang")) { p.executeUpdate(); }
                c.commit();
            } catch (SQLException | RuntimeException e) { JdbcHelper.rollback(c, e); throw e; }
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    private static void kiemTraTien(BigDecimal tien, boolean choPhepKhong) {
        if (tien == null || tien.signum() < (choPhepKhong ? 0 : 1) || tien.stripTrailingZeros().scale() > 0
                || tien.compareTo(new BigDecimal("999999999999")) > 0)
            throw new IllegalArgumentException("Số tiền phải là VND nguyên " + (choPhepKhong ? "không âm" : "dương")
                    + ", tối đa 999.999.999.999 đ.");
    }

    public ArrayList<KhachHang> timKiem(String tuKhoa, String hang, Boolean hoatDong, LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        String sql = "SELECT * FROM dbo.KhachHang WHERE 1=1 ";
        ArrayList<Object> values = new ArrayList<>();
        if (hang != null) { sql += "AND hangTV=? "; values.add(hang); }
        if (hoatDong != null) { sql += "AND trangThai=? "; values.add(hoatDong); }
        if (tuNgay != null) { sql += "AND CAST(taoLuc AS DATE)>=? "; values.add(Date.valueOf(tuNgay)); }
        if (denNgay != null) { sql += "AND CAST(taoLuc AS DATE)<=? "; values.add(Date.valueOf(denNgay)); }
        String q = SanPhamDAO.chuanHoaTimKiem(tuKhoa);
        if (!q.isEmpty()) for (String token : q.split(" ")) {
            sql += "AND REPLACE(REPLACE(CONCAT(maKH,N' ',tenKH,N' ',sdt,N' ',email),N'đ',N'd'),N'Đ',N'D') "
                    + "COLLATE Latin1_General_100_CI_AI LIKE ? ESCAPE '!' ";
            values.add("%" + SanPhamDAO.thoatLike(token) + "%");
        }
        ArrayList<KhachHang> ds = new ArrayList<>();
        try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(sql + "ORDER BY tenKH")) {
            for (int i = 0; i < values.size(); i++) p.setObject(i + 1, values.get(i));
            try (ResultSet r = p.executeQuery()) { while (r.next()) ds.add(docDong(r)); }
            return ds;
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public void sapXep(ArrayList<KhachHang> ds, final int kieu) {
        Collections.sort(ds, new Comparator<KhachHang>() {
            @Override public int compare(KhachHang a, KhachHang b) {
                int c;
                if (kieu == 2 || kieu == 3) {
                    c = a.getTongChiTieu().compareTo(b.getTongChiTieu()); if (kieu == 3) c = -c;
                } else if (kieu == 4) c = b.getTaoLuc().compareTo(a.getTaoLuc());
                else {
                    c = SanPhamDAO.chuanHoaTimKiem(a.getHoTen()).compareTo(SanPhamDAO.chuanHoaTimKiem(b.getHoTen()));
                    if (kieu == 1) c = -c;
                }
                return c == 0 ? a.getId().compareTo(b.getId()) : c;
            }
        });
    }

    public ArrayList<KhachHang> nguoiNhanKhuyenMai(String hang) {
        ArrayList<KhachHang> ds = new ArrayList<>();
        for (KhachHang kh : timKiem("", hang, true, null, null)) if (kh.isNhanKhuyenMai()) ds.add(kh);
        return ds;
    }

    /** Lưu thông báo nội bộ; không coi là đã gửi email/SMS. */
    public int guiKhuyenMai(String hang, String tieuDe, String noiDung) {
        String td = chuoi(tieuDe), nd = chuoi(noiDung);
        if (td.isEmpty() || td.length() > 120 || nd.isEmpty() || nd.length() > 2000)
            throw new IllegalArgumentException("Nhập tiêu đề 1–120 ký tự và nội dung 1–2.000 ký tự.");
        try (Connection c = ConnectDB.openConnection()) {
            c.setAutoCommit(false);
            try {
                ArrayList<String> ids = new ArrayList<>();
                String sql = "SELECT maKH FROM dbo.KhachHang WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE trangThai=1 AND nhanKhuyenMai=1" + (hang == null ? "" : " AND hangTV=?");
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    if (hang != null) p.setString(1, hang);
                    try (ResultSet r = p.executeQuery()) { while (r.next()) ids.add(r.getString(1)); }
                }
                if (ids.isEmpty()) throw new IllegalArgumentException("Không có khách đủ điều kiện nhận thông báo.");
                try (PreparedStatement p = c.prepareStatement("INSERT INTO dbo.ThongBaoKhuyenMai(maKH,tieuDe,noiDung) VALUES (?,?,?)")) {
                    for (String id : ids) {
                        p.setString(1, id); p.setNString(2, td); p.setNString(3, nd); p.executeUpdate();
                        ghiLichSu(c, id, "Khuyến mãi", "Tạo thông báo nội bộ: " + td);
                    }
                }
                c.commit(); return ids.size();
            } catch (SQLException | RuntimeException e) { JdbcHelper.rollback(c, e); throw e; }
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }

    public ArrayList<ThongBaoKhuyenMai> layThongBao(String id) {
        ArrayList<ThongBaoKhuyenMai> ds = new ArrayList<>();
        try (Connection c = ConnectDB.openConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM dbo.ThongBaoKhuyenMai WHERE maKH=? ORDER BY taoLuc DESC,id DESC")) {
            batBuoc(c, id); p.setString(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) ds.add(new ThongBaoKhuyenMai("TB" + r.getLong("id"), id, r.getString("tieuDe"),
                        r.getString("noiDung"), r.getTimestamp("taoLuc").toLocalDateTime()));
            }
            return ds;
        } catch (SQLException e) { throw JdbcHelper.loi(e); }
    }
}
