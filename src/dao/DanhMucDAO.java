package dao;

import connect.ConnectDB;
import entity.DanhMuc;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DanhMucDAO {
    private static final String SELECT_COLUMNS = "SELECT maDanhMuc, tenDanhMuc, moTa FROM dbo.DanhMuc";

    public record DongDanhMuc(DanhMuc danhMuc, long soSanPham) { }

    public ArrayList<DanhMuc> layTatCa() {
        ArrayList<DanhMuc> list = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COLUMNS + " ORDER BY maDanhMuc");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(docDanhMuc(rs));
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách danh mục sản phẩm.", e);
        }
    }

    public ArrayList<DongDanhMuc> layDanhSachQuanLy() {
        ArrayList<DongDanhMuc> rows = new ArrayList<>();
        String sql = """
                SELECT dm.maDanhMuc, dm.tenDanhMuc, dm.moTa, COALESCE(sp.soLuong, 0) AS soLuong
                FROM dbo.DanhMuc dm
                LEFT JOIN (
                    SELECT maDanhMuc, COUNT(*) AS soLuong
                    FROM dbo.SanPham GROUP BY maDanhMuc
                ) sp ON sp.maDanhMuc = dm.maDanhMuc
                ORDER BY dm.tenDanhMuc, dm.maDanhMuc
                """;
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new DongDanhMuc(docDanhMuc(rs), rs.getLong("soLuong")));
            return rows;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh mục và số sản phẩm. Kiểm tra kết nối SQL Server và schema.", e);
        }
    }

    public Map<String, Integer> demTheoDanhMuc() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT maDanhMuc, COUNT(*) AS soLuong FROM dbo.SanPham GROUP BY maDanhMuc";
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) counts.put(rs.getString("maDanhMuc"), rs.getInt("soLuong"));
            return counts;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm sản phẩm trong danh mục.", e);
        }
    }

    public DanhMuc getByMa(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) return null;
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COLUMNS + " WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docDanhMuc(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh mục sản phẩm.", e);
        }
    }

    public String phatSinhMaMoi() {
        try (Connection conn = ConnectDB.openConnection()) {
            return phatSinhMaMoi(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cấp mã danh mục sản phẩm.", e);
        }
    }

    public DanhMuc them(DanhMuc dm) {
        return themDanhMuc(dm);
    }

    public DanhMuc themDanhMuc(DanhMuc dm) {
        kiemTra(dm);
        String sql = "INSERT INTO dbo.DanhMuc (maDanhMuc, tenDanhMuc, moTa) VALUES (?, ?, ?)";
        try (Connection conn = ConnectDB.openConnection()) {
            if (daTonTaiTen(conn, dm.getTenDanhMuc(), null)) {
                throw new IllegalArgumentException("Tên danh mục '" + dm.getTenDanhMuc() + "' đã tồn tại.");
            }
            if (dm.getMaDanhMuc() == null || dm.getMaDanhMuc().isBlank()) {
                dm.setMaDanhMuc(phatSinhMaMoi(conn));
            } else {
                dm.setMaDanhMuc(dm.getMaDanhMuc().trim().toUpperCase(Locale.ROOT));
                if (daTonTaiMa(conn, dm.getMaDanhMuc())) {
                    throw new IllegalArgumentException("Mã danh mục '" + dm.getMaDanhMuc() + "' đã tồn tại.");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dm.getMaDanhMuc());
                ps.setNString(2, dm.getTenDanhMuc());
                ps.setNString(3, dm.getMoTa());
                if (ps.executeUpdate() == 0) throw new SQLException("INSERT không tạo danh mục.");
                return dm;
            }
        } catch (SQLException e) {
            throw loiLuuDanhMuc(e);
        }
    }

    public DanhMuc capNhat(DanhMuc dm) {
        return capNhatDanhMuc(dm);
    }

    public DanhMuc capNhatDanhMuc(DanhMuc dm) {
        kiemTra(dm);
        if (dm.getMaDanhMuc() == null || dm.getMaDanhMuc().isBlank()) {
            throw new IllegalArgumentException("Chưa chọn danh mục cần cập nhật.");
        }
        String ma = dm.getMaDanhMuc().trim();
        String sql = "UPDATE dbo.DanhMuc SET tenDanhMuc = ?, moTa = ? WHERE maDanhMuc = ?";
        try (Connection conn = ConnectDB.openConnection()) {
            if (daTonTaiTen(conn, dm.getTenDanhMuc(), ma)) {
                throw new IllegalArgumentException("Tên danh mục '" + dm.getTenDanhMuc() + "' đã được dùng bởi danh mục khác.");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setNString(1, dm.getTenDanhMuc());
                ps.setNString(2, dm.getMoTa());
                ps.setString(3, ma);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
                return dm;
            }
        } catch (SQLException e) {
            throw loiLuuDanhMuc(e);
        }
    }

    public void xoa(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) throw new IllegalArgumentException("Chưa chọn danh mục cần xóa.");
        String ma = maDanhMuc.trim();
        String sql = "DELETE FROM DanhMuc WHERE maDanhMuc = ?";
        try (Connection conn = ConnectDB.openConnection()) {
            // Kiểm tra ràng buộc sản phẩm trước
            if (demSanPhamTheoMa(conn, ma) > 0) {
                throw new IllegalArgumentException("Danh mục đang được sản phẩm sử dụng.");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ma);
                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Danh mục không còn tồn tại hoặc đã bị xóa.");
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {
                throw new IllegalArgumentException("Danh mục đang được sản phẩm sử dụng.", e);
            }
            throw new IllegalStateException("Không thể xóa danh mục sản phẩm.", e);
        }
    }

    private long demSanPhamTheoMa(Connection conn, String maDanhMuc) {
        String sql = "SELECT COUNT(*) FROM dbo.SanPham WHERE maDanhMuc = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDanhMuc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            return 0L;
        }
    }

    private boolean daTonTaiMa(Connection conn, String maDanhMuc) {
        String sql = "SELECT 1 FROM dbo.DanhMuc WHERE maDanhMuc = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDanhMuc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean daTonTaiTen(Connection conn, String tenDanhMuc, String maBoQua) {
        String sql = maBoQua == null
                ? "SELECT 1 FROM dbo.DanhMuc WHERE LOWER(tenDanhMuc) = LOWER(?)"
                : "SELECT 1 FROM dbo.DanhMuc WHERE LOWER(tenDanhMuc) = LOWER(?) AND maDanhMuc <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDanhMuc.trim());
            if (maBoQua != null) ps.setString(2, maBoQua.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private DanhMuc docDanhMuc(ResultSet rs) throws SQLException {
        return new DanhMuc(rs.getString("maDanhMuc"), rs.getString("tenDanhMuc"), rs.getString("moTa"));
    }

    private String phatSinhMaMoi(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT NEXT VALUE FOR dbo.SeqMaDanhMuc");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long val = rs.getLong(1);
                String candidate = String.format(Locale.ROOT, "DM%02d", val);
                if (!daTonTaiMa(conn, candidate)) {
                    return candidate;
                }
            }
        } catch (SQLException ignored) {
            // Sequence không tồn tại hoặc lỗi, fallback sang tính MAX
        }

        String sql = "SELECT maDanhMuc FROM dbo.DanhMuc WHERE maDanhMuc LIKE 'DM%'";
        long max = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maDanhMuc");
                if (ma != null && ma.startsWith("DM")) {
                    try {
                        long num = Long.parseLong(ma.substring(2).trim());
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) { }
                }
            }
        }
        long next = max + 1;
        while (daTonTaiMa(conn, String.format(Locale.ROOT, "DM%02d", next))) {
            next++;
        }
        return String.format(Locale.ROOT, "DM%02d", next);
    }

    private void kiemTra(DanhMuc dm) {
        if (dm == null) throw new IllegalArgumentException("Chưa nhập danh mục.");
        String ten = dm.getTenDanhMuc() == null ? "" : dm.getTenDanhMuc().trim().replaceAll("\\s+", " ");
        String moTa = dm.getMoTa() == null ? "" : dm.getMoTa().trim();
        if (ten.length() < 2 || ten.length() > 100) throw new IllegalArgumentException("Tên danh mục phải có từ 2 đến 100 ký tự.");
        if (moTa.length() > 255) throw new IllegalArgumentException("Mô tả danh mục tối đa 255 ký tự.");
        dm.setTenDanhMuc(ten);
        dm.setMoTa(moTa.isEmpty() ? null : moTa);
    }

    private RuntimeException loiLuuDanhMuc(SQLException e) {
        if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627) {
            return new IllegalArgumentException("Mã hoặc tên danh mục đã tồn tại.", e);
        }
        return new IllegalStateException("Không thể lưu danh mục sản phẩm.", e);
    }
}
