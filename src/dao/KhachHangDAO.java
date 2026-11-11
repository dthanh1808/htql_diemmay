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
            "SELECT maKH, tenKH, sdt, email, diaChi, ngaySinh, gioiTinh, hangTV, tongChiTieuTichLuy, trangThai, " +
            "ghiChu, nhanKhuyenMai, ngayTao, ngayCapNhat " +
            "FROM KhachHang ";

    public KhachHangDAO() { }

    private KhachHang docDong(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        kh.setTenKH(rs.getString("tenKH")); 
        kh.setSdt(rs.getString("sdt"));
        kh.setEmail(rs.getString("email"));
        kh.setDiaChi(rs.getString("diaChi"));
        
        java.sql.Date d = rs.getDate("ngaySinh");
        if (d != null) kh.setNgaySinh(d.toLocalDate());
        
        boolean gioiTinh = rs.getBoolean("gioiTinh");
        kh.setGioiTinh(rs.wasNull() ? null : gioiTinh);
        
        kh.setHangTV(rs.getString("hangTV"));
        kh.setTongChiTieuTichLuy(rs.getBigDecimal("tongChiTieuTichLuy"));
        kh.setTrangThai(rs.getBoolean("trangThai"));
        kh.setGhiChu(rs.getString("ghiChu"));
        kh.setNhanKhuyenMai(rs.getBoolean("nhanKhuyenMai"));
        
        Timestamp created = rs.getTimestamp("ngayTao");
        Timestamp updated = rs.getTimestamp("ngayCapNhat");
        kh.setNgayTao(created == null ? null : created.toLocalDateTime()); 
        if (updated != null) kh.setNgayCapNhat(updated.toLocalDateTime()); 
        
        return kh;
    }

    /** Lấy toàn bộ danh sách khách hàng */
    public ArrayList<KhachHang> layTatCa() {
        ArrayList<KhachHang> ketQua = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
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
        try (Connection conn = ConnectDB.getConnection();
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

        try (Connection conn = ConnectDB.getConnection();
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

    /** Mã KH do SQL Server cấp, an toàn giữa các máy và không tái sử dụng mã đã xóa. */
    public String taoMaKhachHang() {
        try (Connection conn = ConnectDB.getConnection()) {
            return taoMaKhachHang(conn);
        } catch (SQLException e) { 
            throw new IllegalStateException("Không thể cấp mã khách hàng.", e); 
        }
    }

    private String taoMaKhachHang(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT NEXT VALUE FOR dbo.SeqMaKhachHang");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format(Locale.ROOT, "KH%03d", rs.getLong(1));
        }
    }

    /** Thêm khách hàng mới */
    public KhachHang them(KhachHang input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập thông tin khách hàng.");
        KhachHang kh = new KhachHang(input);
        kiemTra(kh, true);
        
        try (Connection conn = ConnectDB.getConnection()) {
            kh.setMaKH(taoMaKhachHang(conn));
            
            // Kiểm tra số điện thoại duy nhất
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM KhachHang WHERE sdt = ?")) {
                check.setString(1, kh.getSdt());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException("Số điện thoại '" + kh.getSdt() + "' đã được đăng ký.");
                    }
                }
            }

            // INSERT không cần hangTV, ngayTao, ngayCapNhat, tongChiTieu vì SQL Server đã có Default & Trigger xử lý
            String sql = "INSERT INTO KhachHang (maKH, tenKH, sdt, email, diaChi, ngaySinh, gioiTinh, trangThai, ghiChu, nhanKhuyenMai) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kh.getMaKH());
                ps.setString(2, kh.getTenKH()); // Đã sửa getHoTen -> getTenKH
                ps.setString(3, kh.getSdt());
                ps.setString(4, (kh.getEmail() == null || kh.getEmail().isEmpty()) ? null : kh.getEmail());
                ps.setString(5, (kh.getDiaChi() == null || kh.getDiaChi().isEmpty()) ? null : kh.getDiaChi());
                ps.setDate(6, kh.getNgaySinh() != null ? java.sql.Date.valueOf(kh.getNgaySinh()) : null);
                
                if (kh.getGioiTinhNullable() == null) ps.setNull(7, Types.BIT);
                else ps.setBoolean(7, kh.getGioiTinhNullable());
                
                ps.setBoolean(8, kh.isTrangThai());
                ps.setString(9, (kh.getGhiChu() == null || kh.getGhiChu().trim().isEmpty()) ? null : kh.getGhiChu());
                ps.setBoolean(10, kh.isNhanKhuyenMai());
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

        try (Connection conn = ConnectDB.getConnection()) {
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

            String sql = "UPDATE KhachHang SET tenKH=?, sdt=?, email=?, diaChi=?, ngaySinh=?, gioiTinh=?, trangThai=?, ghiChu=?, nhanKhuyenMai=? " +
                         "WHERE maKH=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kh.getTenKH()); // Đã sửa
                ps.setString(2, kh.getSdt());
                ps.setString(3, (kh.getEmail() == null || kh.getEmail().isEmpty()) ? null : kh.getEmail());
                ps.setString(4, (kh.getDiaChi() == null || kh.getDiaChi().isEmpty()) ? null : kh.getDiaChi());
                ps.setDate(5, kh.getNgaySinh() != null ? java.sql.Date.valueOf(kh.getNgaySinh()) : null);
                
                if (kh.getGioiTinhNullable() == null) ps.setNull(6, Types.BIT);
                else ps.setBoolean(6, kh.getGioiTinhNullable());
                
                ps.setBoolean(7, kh.isTrangThai());
                ps.setString(8, (kh.getGhiChu() == null || kh.getGhiChu().trim().isEmpty()) ? null : kh.getGhiChu());
                ps.setBoolean(9, kh.isNhanKhuyenMai());
                ps.setString(10, kh.getMaKH());
                
                int rows = ps.executeUpdate();
                if (rows == 0) throw new IllegalArgumentException("Khách hàng không còn tồn tại trong hệ thống.");
            }
            return timTheoMa(kh.getMaKH());
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Dữ liệu cập nhật không hợp lệ (lỗi ràng buộc).", e);
            }
            throw new IllegalStateException("Không thể cập nhật khách hàng vào cơ sở dữ liệu.", e);
        }
    }

    /** Chuyển đổi trạng thái hoạt động của khách hàng */
    public void chuyenTrangThai(String maKH, boolean dangHoatDong) {
        if (maKH == null || maKH.trim().isEmpty()) return;
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE KhachHang SET trangThai = ? WHERE maKH = ?")) {
            ps.setBoolean(1, dangHoatDong);
            ps.setString(2, maKH.trim());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Khách hàng không còn tồn tại trong hệ thống.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật trạng thái khách hàng.", e);
        }
    }

    /** Kiểm tra hợp lệ thông tin khách hàng */
    private void kiemTra(KhachHang kh, boolean taoMoi) {
        kh.setTenKH(chuoi(kh.getTenKH()).replaceAll("\\s+", " ")); // Sửa từ getHoTen
        String sdt = chuoi(kh.getSdt()).replaceAll("[ .()-]", "");
        if (sdt.startsWith("+84")) sdt = "0" + sdt.substring(3);
        kh.setSdt(sdt);
        kh.setEmail(chuoi(kh.getEmail()).toLowerCase(Locale.ROOT));
        kh.setDiaChi(chuoi(kh.getDiaChi()));
        if (kh.getGhiChu() != null) kh.setGhiChu(kh.getGhiChu().trim());

        if (kh.getTenKH().length() < 2 || kh.getTenKH().length() > 100) {
            throw new IllegalArgumentException("Họ tên khách hàng phải có từ 2 đến 100 ký tự.");
        }
        if (!kh.getSdt().matches("0[0-9]{9}")) {
            throw new IllegalArgumentException("Số điện thoại di động phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        }
        if (!kh.getEmail().isEmpty()) {
            if (kh.getEmail().length() > 100 || !kh.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,24}$")) {
                throw new IllegalArgumentException("Định dạng email không hợp lệ (tối đa 100 ký tự).");
            }
        }
        if (kh.getDiaChi().length() > 200) {
            throw new IllegalArgumentException("Địa chỉ tối đa 200 ký tự.");
        }
        if (kh.getGhiChu() != null && kh.getGhiChu().length() > 500) {
            throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự.");
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
        
        // Hạng TV được Trigger xử lý nhưng vẫn validate nhẹ nhàng tại Java
        if (kh.getHangTV() == null || kh.getHangTV().trim().isEmpty() ||
            !List.of("Member", "Silver", "Gold", "Diamond").contains(kh.getHangTV())) {
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
        return kh == null ? BigDecimal.ZERO : kh.getTongChiTieuTichLuyDecimal();
    }

    /** Lấy đối tượng HangThanhVien của khách hàng */
    public HangThanhVien layHang(String maKH) {
        return layHang(timTheoMa(maKH));
    }

    public HangThanhVien layHang(KhachHang kh) {
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

    /** 
     * Cập nhật chi tiêu nguyên tử. 
     * Trigger trong SQL Server (TRG_KH_DongBoHangVaNgayCapNhat) sẽ tự động bắt sự thay đổi của cột này 
     * để nâng Hạng Thành Viên tương ứng, update ngayCapNhat và ghi lại Lịch sử.
     */
    public void capNhatChiTieuVaHang(String maKH, BigDecimal soTienThem) {
        if (maKH == null || soTienThem == null || soTienThem.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal tien = soTienThem.setScale(0, java.math.RoundingMode.HALF_UP);
        String sql = "UPDATE KhachHang SET tongChiTieuTichLuy = tongChiTieuTichLuy + ? WHERE maKH = ?";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, tien);
            ps.setString(2, maKH.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật tổng chi tiêu.", e);
        }
    }

    /** Lấy danh sách hóa đơn / giao dịch của khách hàng */
    public ArrayList<GiaoDichKhachHang> layGiaoDich(String maKH) {
        ArrayList<GiaoDichKhachHang> ds = new ArrayList<>();
        if (maKH == null || maKH.trim().isEmpty()) return ds;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT maHD, ngayLap, tongThanhToan, ghiChuGiaoHang, trangThai " +
                     "FROM HoaDon WHERE maKH = ? ORDER BY ngayLap DESC")) {
            ps.setString(1, maKH.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHD = rs.getString("maHD");
                    Timestamp ts = rs.getTimestamp("ngayLap");
                    LocalDateTime tg = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    BigDecimal tien = rs.getBigDecimal("tongThanhToan");
                    String note = rs.getString("ghiChuGiaoHang");
                    String tt = rs.getString("trangThai");
                    ds.add(new GiaoDichKhachHang(maHD, maKH.trim(), tg, tien != null ? tien : BigDecimal.ZERO,
                            (tt != null ? "[" + tt + "] " : "") + (note != null ? note : "")));
                }
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải lịch sử hóa đơn của khách hàng.", e);
        }
    }

    /** Lấy lịch sử hoạt động khách hàng (Lịch sử này được sinh tự động bằng Trigger SQL) */
    public ArrayList<LichSuKhachHang> layLichSu(String maKH) {
        ArrayList<LichSuKhachHang> ds = new ArrayList<>();
        if (maKH == null || maKH.trim().isEmpty()) return ds;
        String sql = "SELECT thoiGian, hanhDong, noiDung FROM LichSuKhachHang " +
                     "WHERE maKH = ? ORDER BY thoiGian DESC, maLichSu DESC";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKH.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp time = rs.getTimestamp("thoiGian");
                    ds.add(new LichSuKhachHang(maKH.trim(), time == null ? LocalDateTime.now() : time.toLocalDateTime(),
                            rs.getString("hanhDong"), rs.getString("noiDung")));
                }
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải lịch sử hoạt động khách hàng.", e);
        }
    }

    /** Ghi nhận giao dịch mua hàng */
    public void ghiNhanMuaHang(String maKH, String maHoaDon, BigDecimal soTien, String noiDung) {
        capNhatChiTieuVaHang(maKH, soTien);
    }

    /** Tìm kiếm và lọc khách hàng */
    public ArrayList<KhachHang> timKiem(String tuKhoa, String hang, Boolean hoatDong, LocalDate tuNgay, LocalDate denNgay) {
        return timKiem(layTatCa(), tuKhoa, hang, hoatDong, tuNgay, denNgay);
    }

    public ArrayList<KhachHang> timKiem(java.util.List<KhachHang> tatCa, String tuKhoa, String hang, Boolean hoatDong, LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        }

        ArrayList<KhachHang> ketQua = new ArrayList<>();
        // Giả sử SanPhamDAO.chuanHoaTimKiem là hàm bỏ dấu tiếng Việt bạn đã viết
        String query = SanPhamDAO.chuanHoaTimKiem(chuoi(tuKhoa)); 

        for (KhachHang kh : tatCa) {
            if (hoatDong != null && hoatDong != kh.isTrangThai()) continue;
            if (hang != null && !hang.trim().isEmpty() && !hang.equalsIgnoreCase("Tất cả") &&
                    !hang.equalsIgnoreCase(kh.getHangTV())) continue;

            if ((tuNgay != null || denNgay != null) && kh.getNgaySinh() == null) continue;
            if (kh.getNgaySinh() != null) {
                if (tuNgay != null && kh.getNgaySinh().isBefore(tuNgay)) continue;
                if (denNgay != null && kh.getNgaySinh().isAfter(denNgay)) continue;
            }

            if (!query.isEmpty()) {
                String text = SanPhamDAO.chuanHoaTimKiem(
                        kh.getMaKH() + " " + kh.getTenKH() + " " + kh.getSdt() + " " +
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
                c = SanPhamDAO.chuanHoaTimKiem(a.getTenKH()).compareTo(SanPhamDAO.chuanHoaTimKiem(b.getTenKH()));
                if (kieu == 1) c = -c; // Đảo ngược nếu là Z-A
            }
            if (c == 0) c = a.getMaKH().compareTo(b.getMaKH());
            return c;
        });
    }
}