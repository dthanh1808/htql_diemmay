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

    public DanhMuc themDanhMuc(DanhMuc dm) {
        kiemTra(dm);
        String sql = "INSERT INTO dbo.DanhMuc (maDanhMuc, tenDanhMuc, moTa) VALUES (?, ?, ?)";
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (dm.getMaDanhMuc() == null || dm.getMaDanhMuc().isBlank()) {
                dm.setMaDanhMuc(phatSinhMaMoi(conn));
            }
            ps.setString(1, dm.getMaDanhMuc().trim());
            ps.setNString(2, dm.getTenDanhMuc());
            ps.setNString(3, dm.getMoTa());
            ps.executeUpdate();
            return dm;
        } catch (SQLException e) {
            throw loiLuuDanhMuc(e);
        }
    }

    public DanhMuc capNhatDanhMuc(DanhMuc dm) {
        kiemTra(dm);
        if (dm.getMaDanhMuc() == null || dm.getMaDanhMuc().isBlank()) {
            throw new IllegalArgumentException("Chưa chọn danh mục cần cập nhật.");
        }
        String sql = "UPDATE dbo.DanhMuc SET tenDanhMuc = ?, moTa = ? WHERE maDanhMuc = ?";
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, dm.getTenDanhMuc());
            ps.setNString(2, dm.getMoTa());
            ps.setString(3, dm.getMaDanhMuc().trim());
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
            return dm;
        } catch (SQLException e) {
            throw loiLuuDanhMuc(e);
        }
    }

    public void xoa(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) throw new IllegalArgumentException("Chưa chọn danh mục cần xóa.");
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM dbo.DanhMuc WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc.trim());
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {
                throw new IllegalArgumentException("Danh mục đang được sản phẩm sử dụng.", e);
            }
            throw new IllegalStateException("Không thể xóa danh mục sản phẩm.", e);
        }
    }

    private DanhMuc docDanhMuc(ResultSet rs) throws SQLException {
        return new DanhMuc(rs.getString("maDanhMuc"), rs.getString("tenDanhMuc"), rs.getString("moTa"));
    }

    private String phatSinhMaMoi(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT NEXT VALUE FOR dbo.SeqMaDanhMuc");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new SQLException("Sequence không trả về mã danh mục.");
            return String.format(Locale.ROOT, "DM%02d", rs.getLong(1));
        }
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
