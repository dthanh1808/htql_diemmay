package dao;

import connect.ConnectDB;
import entity.GiaoDichKhachHang;
import entity.HangThanhVien;
import entity.KhachHang;
import entity.LichSuKhachHang;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object cho KhachHang dựa trên CSDL QuanLyBanHang.
 * Quản lý khách hàng, lịch sử giao dịch và hạng thành viên.
 * Đọc và ghi theo đúng các cột của bảng KhachHang trong CSDL.
 */
public class KhachHangDAO {

    private static final String SELECT_BASE =
            "SELECT maKH, tenKH, sdt, email, diaChi, ngaySinh, gioiTinh, hangTV, tongChiTieuTichLuy, trangThai " +
            "FROM KhachHang ";

    public KhachHangDAO() { }

    public KhachHangDAO(DuLieuTam duLieu) {
        this();
    }

    private KhachHang docDong(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        kh.setHoTen(rs.getString("tenKH"));
        kh.setSdt(rs.getString("sdt"));
        kh.setEmail(rs.getString("email"));
        kh.setDiaChi(rs.getString("diaChi"));
        java.sql.Date d = rs.getDate("ngaySinh");
        if (d != null) kh.setNgaySinh(d.toLocalDate());
        boolean gioiTinh = rs.getBoolean("gioiTinh");
        kh.setGioiTinh(rs.wasNull() ? null : gioiTinh);
        kh.setHangTV(rs.getString("hangTV"));
        kh.setTongChiTieu(rs.getBigDecimal("tongChiTieuTichLuy"));
        kh.setHangTV(hangTheoChiTieu(kh.getTongChiTieuTichLuyDecimal()));
        kh.setTrangThai(rs.getBoolean("trangThai"));
        return kh;
    }

    /** Lấy toàn bộ danh sách khách hàng */
    public ArrayList<KhachHang> layTatCa() {
        ArrayList<KhachHang> ketQua = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " ORDER BY maKH");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ketQua.add(docDong(rs));
            }
            return ketQua;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách khách hàng từ cơ sở dữ liệu.", e);
        }
    }

    /** Tìm khách hàng theo mã khách hàng */
    public KhachHang timTheoMa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return null;
        String ma = maKH.trim();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE maKH = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docDong(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm khách hàng theo mã " + ma + ".", e);
        }
        return null;
    }

    /** Tìm khách hàng theo số điện thoại */
    public KhachHang timTheoSDT(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        String p = sdt.trim().replaceAll("[ .()-]", "");
        if (p.startsWith("+84")) p = "0" + p.substring(3);

        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE sdt = ?")) {
            ps.setString(1, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docDong(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm khách hàng theo số điện thoại.", e);
        }
        return null;
    }

    /** Tạo mã khách hàng tiếp theo tự động từ CSDL */
    public synchronized String taoMaKhachHang() {
        int maxIndex = 0;
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT maKH FROM KhachHang WHERE maKH LIKE 'KH%'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxIndex) maxIndex = num;
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tạo mã khách hàng từ cơ sở dữ liệu.", e);
        }
        String ma = String.format(Locale.ROOT, "KH%03d", maxIndex + 1);
        if (ma.length() > 10) throw new IllegalStateException("Đã hết mã khách hàng có thể tạo.");
        return ma;
    }

    /** Thêm khách hàng mới */
    public KhachHang them(KhachHang input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập thông tin khách hàng.");
        KhachHang kh = new KhachHang(input);
        kiemTra(kh, true);
        kh.setHangTV(hangTheoChiTieu(kh.getTongChiTieuTichLuyDecimal()));

        if (kh.getMaKH() == null || kh.getMaKH().trim().isEmpty()) {
            kh.setMaKH(taoMaKhachHang());
        }
        if (kh.getMaKH().trim().length() > 10) {
            throw new IllegalArgumentException("Mã khách hàng tối đa 10 ký tự.");
        }

        try (Connection conn = ConnectDB.openConnection()) {
            // Kiểm tra số điện thoại duy nhất
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM KhachHang WHERE sdt = ?")) {
                check.setString(1, kh.getSdt());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException("Số điện thoại '" + kh.getSdt() + "' đã được đăng ký.");
                    }
                }
            }

            String sql = "INSERT INTO KhachHang (maKH, tenKH, sdt, email, diaChi, ngaySinh, gioiTinh, hangTV, tongChiTieuTichLuy, trangThai) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kh.getMaKH());
                ps.setString(2, kh.getHoTen());
                ps.setString(3, kh.getSdt());
                ps.setString(4, (kh.getEmail() == null || kh.getEmail().isEmpty()) ? null : kh.getEmail());
                ps.setString(5, (kh.getDiaChi() == null || kh.getDiaChi().isEmpty()) ? null : kh.getDiaChi());
                ps.setDate(6, kh.getNgaySinh() != null ? java.sql.Date.valueOf(kh.getNgaySinh()) : null);
                if (kh.getGioiTinhNullable() == null) ps.setNull(7, Types.BIT);
                else ps.setBoolean(7, kh.getGioiTinhNullable());
                ps.setString(8, (kh.getHangTV() == null || kh.getHangTV().isEmpty()) ? "Member" : kh.getHangTV());
                ps.setBigDecimal(9, kh.getTongChiTieuTichLuyDecimal());
                ps.setBoolean(10, kh.isTrangThai());
                ps.executeUpdate();
            }
            return timTheoMa(kh.getMaKH());
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Mã khách hàng hoặc số điện thoại đã tồn tại.", e);
            }
            throw new IllegalStateException("Không thể lưu khách hàng vào cơ sở dữ liệu.", e);
        }
    }

    /** Cập nhật thông tin khách hàng */
    public KhachHang capNhat(KhachHang input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập thông tin khách hàng.");
        KhachHang kh = new KhachHang(input);
        if (kh.getMaKH() == null || kh.getMaKH().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được để trống.");
        }
        kiemTra(kh, false);
        kh.setHangTV(hangTheoChiTieu(kh.getTongChiTieuTichLuyDecimal()));

        try (Connection conn = ConnectDB.openConnection()) {
            // Kiểm tra trùng SĐT với người khác
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM KhachHang WHERE sdt = ? AND maKH <> ?")) {
                check.setString(1, kh.getSdt());
                check.setString(2, kh.getMaKH());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException("Số điện thoại '" + kh.getSdt() + "' đã được dùng bởi khách hàng khác.");
                    }
                }
            }

            String sql = "UPDATE KhachHang SET tenKH=?, sdt=?, email=?, diaChi=?, ngaySinh=?, gioiTinh=?, trangThai=? " +
                         "WHERE maKH=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kh.getHoTen());
                ps.setString(2, kh.getSdt());
                ps.setString(3, (kh.getEmail() == null || kh.getEmail().isEmpty()) ? null : kh.getEmail());
                ps.setString(4, (kh.getDiaChi() == null || kh.getDiaChi().isEmpty()) ? null : kh.getDiaChi());
                ps.setDate(5, kh.getNgaySinh() != null ? java.sql.Date.valueOf(kh.getNgaySinh()) : null);
                if (kh.getGioiTinhNullable() == null) ps.setNull(6, Types.BIT);
                else ps.setBoolean(6, kh.getGioiTinhNullable());
                ps.setBoolean(7, kh.isTrangThai());
                ps.setString(8, kh.getMaKH());
                int rows = ps.executeUpdate();
                if (rows == 0) throw new IllegalArgumentException("Khách hàng không còn tồn tại trong hệ thống.");
            }
            return timTheoMa(kh.getMaKH());
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Số điện thoại hoặc hạng thành viên không hợp lệ.", e);
            }
            throw new IllegalStateException("Không thể cập nhật khách hàng vào cơ sở dữ liệu.", e);
        }
    }

    /** Chuyển đổi trạng thái hoạt động của khách hàng */
    public void chuyenTrangThai(String maKH, boolean dangHoatDong) {
        if (maKH == null || maKH.trim().isEmpty()) return;
        String ma = maKH.trim();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE KhachHang SET trangThai = ? WHERE maKH = ?")) {
            ps.setBoolean(1, dangHoatDong);
            ps.setString(2, ma);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Khách hàng không còn tồn tại trong hệ thống.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật trạng thái khách hàng.", e);
        }
    }

    /** Kiểm tra xem khách hàng có thể xóa an toàn không */
    public boolean coTheXoa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return false;
        String ma = maKH.trim();
        KhachHang kh = timTheoMa(ma);
        if (kh == null || kh.isTrangThai()) return false;

        try (Connection conn = ConnectDB.openConnection()) {
            try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM HoaDon WHERE maKH = ?")) {
                check.setString(1, ma);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return false;
                }
            }
            try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM PhieuBaoHanh WHERE maKH = ?")) {
                check.setString(1, ma);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return false;
                }
            }
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra giao dịch liên quan đến khách hàng.", e);
        }
    }

    /**
     * Xóa khách hàng khỏi hệ thống nếu không có ràng buộc nghiệp vụ.
     */
    public void xoa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) throw new IllegalArgumentException("Chưa chọn khách hàng.");
        String ma = maKH.trim();
        KhachHang kh = timTheoMa(ma);
        if (kh == null) throw new IllegalArgumentException("Khách hàng không còn tồn tại.");
        if (kh.isTrangThai()) {
            throw new IllegalArgumentException("Hãy chuyển trạng thái khách hàng sang 'Ngừng hoạt động' trước khi xóa.");
        }

        try (Connection conn = ConnectDB.openConnection()) {
            // Kiểm tra hóa đơn
            try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM HoaDon WHERE maKH = ?")) {
                check.setString(1, ma);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalArgumentException("Khách hàng đã có " + rs.getInt(1) +
                                " hóa đơn trong hệ thống. Không thể xóa để bảo toàn dữ liệu tài chính, hãy để 'Ngừng hoạt động'.");
                    }
                }
            }
            // Kiểm tra bảo hành
            try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM PhieuBaoHanh WHERE maKH = ?")) {
                check.setString(1, ma);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalArgumentException("Khách hàng đã có phiếu bảo hành. Không thể xóa hồ sơ.");
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM KhachHang WHERE maKH = ?")) {
                ps.setString(1, ma);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Lỗi khi xóa khách hàng: " + e.getMessage());
        }

    }

    /** Kiểm tra hợp lệ thông tin khách hàng */
    private void kiemTra(KhachHang kh, boolean taoMoi) {
        kh.setHoTen(chuoi(kh.getHoTen()).replaceAll("\\s+", " "));
        String sdt = chuoi(kh.getSdt()).replaceAll("[ .()-]", "");
        if (sdt.startsWith("+84")) sdt = "0" + sdt.substring(3);
        kh.setSdt(sdt);
        kh.setEmail(chuoi(kh.getEmail()).toLowerCase(Locale.ROOT));
        kh.setDiaChi(chuoi(kh.getDiaChi()));

        if (kh.getHoTen().length() < 2 || kh.getHoTen().length() > 100) {
            throw new IllegalArgumentException("Họ tên khách hàng phải có từ 2 đến 100 ký tự.");
        }
        if (!kh.getSdt().matches("0[0-9]{9}")) {
            throw new IllegalArgumentException("Số điện thoại di động phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        }
        if (!kh.getEmail().isEmpty()) {
            if (kh.getEmail().length() > 100 || !kh.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                throw new IllegalArgumentException("Định dạng email không hợp lệ (tối đa 100 ký tự).");
            }
        }
        if (kh.getDiaChi().length() > 200) {
            throw new IllegalArgumentException("Địa chỉ tối đa 200 ký tự.");
        }
        if (kh.getTongChiTieuTichLuyDecimal().compareTo(new BigDecimal("999999999999999999")) > 0) {
            throw new IllegalArgumentException("Tổng chi tiêu vượt quá giới hạn lưu trữ của cơ sở dữ liệu.");
        }
        if (kh.getNgaySinh() != null) {
            if (kh.getNgaySinh().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày sinh không thể ở tương lai.");
            }
            if (kh.getNgaySinh().isBefore(LocalDate.now().minusYears(120))) {
                throw new IllegalArgumentException("Ngày sinh không hợp lệ (vượt quá 120 năm).");
            }
        }
        if (kh.getHangTV() == null || kh.getHangTV().trim().isEmpty()) {
            kh.setHangTV("Member");
        } else if (!List.of("Member", "Silver", "Gold", "Diamond").contains(kh.getHangTV())) {
            kh.setHangTV("Member");
        }
    }

    private static String chuoi(String text) {
        return text == null ? "" : text.trim();
    }

    private static String hangTheoChiTieu(BigDecimal tong) {
        if (tong.compareTo(new BigDecimal("70000000")) >= 0) return "Diamond";
        if (tong.compareTo(new BigDecimal("35000000")) >= 0) return "Gold";
        if (tong.compareTo(new BigDecimal("15000000")) >= 0) return "Silver";
        return "Member";
    }

    /** Lấy tổng chi tiêu tích lũy của khách hàng */
    public BigDecimal tongChiTieu(String maKH) {
        KhachHang kh = timTheoMa(maKH);
        if (kh == null) return BigDecimal.ZERO;
        return kh.getTongChiTieuTichLuyDecimal();
    }

    /** Lấy đối tượng HangThanhVien của khách hàng */
    public HangThanhVien layHang(String maKH) {
        KhachHang kh = timTheoMa(maKH);
        String tenHang = kh == null ? "Member" : hangTheoChiTieu(kh.getTongChiTieuTichLuyDecimal());
        for (HangThanhVien h : layChinhSach()) {
            if (h.getTen().equalsIgnoreCase(tenHang)) return h;
        }
        return new HangThanhVien(tenHang, BigDecimal.ZERO, 0);
    }

    /** Lấy chính sách 4 hạng thành viên chuẩn theo SQL */
    public ArrayList<HangThanhVien> layChinhSach() {
        ArrayList<HangThanhVien> cs = new ArrayList<>();
        cs.add(new HangThanhVien("Member", BigDecimal.ZERO, 0));
        cs.add(new HangThanhVien("Silver", new BigDecimal("15000000"), 2));
        cs.add(new HangThanhVien("Gold", new BigDecimal("35000000"), 5));
        cs.add(new HangThanhVien("Diamond", new BigDecimal("70000000"), 10));
        return cs;
    }

    /** Cập nhật chi tiêu nguyên tử để không ghi đè giao dịch được lưu cùng lúc. */
    public void capNhatChiTieuVaHang(String maKH, BigDecimal soTienThem) {
        if (maKH == null || soTienThem == null || soTienThem.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal tien = soTienThem.setScale(0, java.math.RoundingMode.HALF_UP);
        String sql = "UPDATE KhachHang SET tongChiTieuTichLuy = tongChiTieuTichLuy + ?, " +
                "hangTV = CASE WHEN tongChiTieuTichLuy + ? >= 70000000 THEN N'Diamond' " +
                "WHEN tongChiTieuTichLuy + ? >= 35000000 THEN N'Gold' " +
                "WHEN tongChiTieuTichLuy + ? >= 15000000 THEN N'Silver' ELSE N'Member' END " +
                "WHERE maKH = ?";
        try (Connection conn = ConnectDB.openConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, tien);
            ps.setBigDecimal(2, tien);
            ps.setBigDecimal(3, tien);
            ps.setBigDecimal(4, tien);
            ps.setString(5, maKH.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật tổng chi tiêu và hạng thành viên.", e);
        }
    }

    /** Lấy danh sách hóa đơn / giao dịch của khách hàng */
    public ArrayList<GiaoDichKhachHang> layGiaoDich(String maKH) {
        ArrayList<GiaoDichKhachHang> ds = new ArrayList<>();
        if (maKH == null || maKH.trim().isEmpty()) return ds;
        String ma = maKH.trim();

        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT maHD, ngayLap, tongThanhToan, ghiChuGiaoHang, trangThai " +
                     "FROM HoaDon WHERE maKH = ? ORDER BY ngayLap DESC")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHD = rs.getString("maHD");
                    Timestamp ts = rs.getTimestamp("ngayLap");
                    LocalDateTime tg = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    BigDecimal tien = rs.getBigDecimal("tongThanhToan");
                    String note = rs.getString("ghiChuGiaoHang");
                    String tt = rs.getString("trangThai");
                    ds.add(new GiaoDichKhachHang(maHD, ma, tg, tien != null ? tien : BigDecimal.ZERO,
                            (tt != null ? "[" + tt + "] " : "") + (note != null ? note : "")));
                }
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải lịch sử hóa đơn của khách hàng.", e);
        }
    }

    /** Lấy lịch sử hoạt động khách hàng */
    public ArrayList<LichSuKhachHang> layLichSu(String maKH) {
        ArrayList<LichSuKhachHang> ds = new ArrayList<>();
        if (maKH == null || maKH.trim().isEmpty()) return ds;
        KhachHang kh = timTheoMa(maKH);
        if (kh == null) return ds;

        // Lấy từ giao dịch
        for (GiaoDichKhachHang gd : layGiaoDich(maKH)) {
            ds.add(new LichSuKhachHang(maKH, gd.getThoiGian(), "Mua hàng",
                    "Hóa đơn " + gd.getMaHoaDon() + ": " + gd.getSoTien().toPlainString() + " đ"));
        }
        ds.sort(Comparator.comparing(LichSuKhachHang::getThoiGian).reversed());
        return ds;
    }

    /** Ghi nhận giao dịch mua hàng */
    public void ghiNhanMuaHang(String maKH, String maHoaDon, BigDecimal soTien, String noiDung) {
        capNhatChiTieuVaHang(maKH, soTien);
    }

    /** Tìm kiếm và lọc khách hàng */
    public ArrayList<KhachHang> timKiem(String tuKhoa, String hang, Boolean hoatDong, LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        }
        ArrayList<KhachHang> tatCa = layTatCa();
        ArrayList<KhachHang> ketQua = new ArrayList<>();
        String query = SanPhamDAO.chuanHoaTimKiem(chuoi(tuKhoa));

        for (KhachHang kh : tatCa) {
            if (hoatDong != null && hoatDong != kh.isTrangThai()) continue;
            if (hang != null && !hang.trim().isEmpty() && !hang.equalsIgnoreCase("Tất cả") &&
                    !hang.equalsIgnoreCase(kh.getHangTV())) continue;

            if (kh.getNgaySinh() != null) {
                if (tuNgay != null && kh.getNgaySinh().isBefore(tuNgay)) continue;
                if (denNgay != null && kh.getNgaySinh().isAfter(denNgay)) continue;
            }

            if (!query.isEmpty()) {
                String text = SanPhamDAO.chuanHoaTimKiem(
                        kh.getMaKH() + " " + kh.getHoTen() + " " + kh.getSdt() + " " +
                        (kh.getEmail() != null ? kh.getEmail() : "") + " " +
                        (kh.getDiaChi() != null ? kh.getDiaChi() : ""));
                boolean match = true;
                for (String token : query.split("\\s+")) {
                    if (!text.contains(token)) {
                        match = false;
                        break;
                    }
                }
                if (!match) continue;
            }
            ketQua.add(kh);
        }
        return ketQua;
    }

    /** Sắp xếp danh sách khách hàng */
    public void sapXep(ArrayList<KhachHang> ds, int kieu) {
        // 0: Tên A-Z, 1: Tên Z-A, 2: Chi tiêu tăng dần, 3: Chi tiêu giảm dần, 4: Mã KH
        Collections.sort(ds, (a, b) -> {
            int c;
            if (kieu == 2) {
                c = a.getTongChiTieuTichLuyDecimal().compareTo(b.getTongChiTieuTichLuyDecimal());
            } else if (kieu == 3) {
                c = b.getTongChiTieuTichLuyDecimal().compareTo(a.getTongChiTieuTichLuyDecimal());
            } else if (kieu == 4) {
                c = a.getMaKH().compareTo(b.getMaKH());
            } else {
                c = SanPhamDAO.chuanHoaTimKiem(a.getHoTen()).compareTo(SanPhamDAO.chuanHoaTimKiem(b.getHoTen()));
                if (kieu == 1) c = -c;
            }
            if (c == 0) c = a.getMaKH().compareTo(b.getMaKH());
            return c;
        });
    }

}
