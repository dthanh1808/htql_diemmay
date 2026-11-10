package dao;

import connect.ConnectDB;
import entity.DanhMuc;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

/** Danh mục sản phẩm được đọc/ghi thống nhất trong SQL Server. */
public class DanhMucDAO {
    public DanhMucDAO() { }
    public DanhMucDAO(DuLieuTam ignored) { this(); }

    public DanhMuc timTheoMa(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) return null;
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maDanhMuc, tenDanhMuc, moTa FROM dbo.DanhMuc WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new DanhMuc(rs.getString(1), rs.getString(2), rs.getString(3)) : null;
            }
        } catch (SQLException e) { throw new IllegalStateException("Không thể tải danh mục.", e); }
    }

    /** Lấy toàn bộ danh mục từ CSDL */
    public ArrayList<DanhMuc> layTatCa() {
        ArrayList<DanhMuc> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maDanhMuc, tenDanhMuc, moTa FROM DanhMuc ORDER BY maDanhMuc");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new DanhMuc(rs.getString("maDanhMuc"), rs.getString("tenDanhMuc"), rs.getString("moTa")));
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách danh mục.", e);
        }
    }

    public String layTen(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) return "";
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT tenDanhMuc FROM DanhMuc WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đọc tên danh mục sản phẩm.", e);
        }
    }

    public java.util.Map<String, Integer> demTheoDanhMuc() {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maDanhMuc, COUNT(*) FROM dbo.SanPham GROUP BY maDanhMuc");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) counts.put(rs.getString(1), rs.getInt(2));
            return counts;
        } catch (SQLException e) { throw new IllegalStateException("Không thể đếm sản phẩm trong danh mục.", e); }
    }

    public int demSanPham(String maDanhMuc) {
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM SanPham WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm sản phẩm trong danh mục.", e);
        }
    }

    public DanhMuc them(DanhMuc input) {
        DanhMuc dm = kiemTra(input);
        try (Connection conn = ConnectDB.openConnection()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM DanhMuc WHERE LOWER(tenDanhMuc) = LOWER(?)")) {
                check.setString(1, dm.getTenDM());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
                }
            }
            dm.setMaDM(taoMaDanhMuc(conn));
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO DanhMuc (maDanhMuc, tenDanhMuc, moTa) VALUES (?, ?, ?)")) {
                ps.setString(1, dm.getMaDM());
                ps.setString(2, dm.getTenDM());
                ps.setString(3, dm.getMoTa().isEmpty() ? null : dm.getMoTa());
                ps.executeUpdate();
            }
            return dm;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm danh mục sản phẩm.", e);
        }
    }

    public DanhMuc capNhat(DanhMuc input) {
        DanhMuc dm = kiemTra(input);
        if (dm.getMaDM() == null || dm.getMaDM().isBlank())
            throw new IllegalArgumentException("Mã danh mục không được để trống.");
        try (Connection conn = ConnectDB.openConnection()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM DanhMuc WHERE LOWER(tenDanhMuc) = LOWER(?) AND maDanhMuc <> ?")) {
                check.setString(1, dm.getTenDM());
                check.setString(2, dm.getMaDM());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE DanhMuc SET tenDanhMuc = ?, moTa = ? WHERE maDanhMuc = ?")) {
                ps.setString(1, dm.getTenDM());
                ps.setString(2, dm.getMoTa().isEmpty() ? null : dm.getMoTa());
                ps.setString(3, dm.getMaDM());
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
            }
            return dm;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật danh mục sản phẩm.", e);
        }
    }

    public void xoa(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) throw new IllegalArgumentException("Chưa chọn danh mục.");
        if (demSanPham(maDanhMuc) > 0)
            throw new IllegalArgumentException("Không thể xóa danh mục đang được sản phẩm sử dụng.");
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM DanhMuc WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc.trim());
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa danh mục sản phẩm.", e);
        }
    }

    private DanhMuc kiemTra(DanhMuc input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập danh mục.");
        DanhMuc dm = new DanhMuc(input);
        dm.setTenDM(dm.getTenDM() == null ? "" : dm.getTenDM().trim().replaceAll("\\s+", " "));
        dm.setMoTa(dm.getMoTa() == null ? "" : dm.getMoTa().trim());
        if (dm.getTenDM().length() < 2 || dm.getTenDM().length() > 100)
            throw new IllegalArgumentException("Tên danh mục phải có từ 2 đến 100 ký tự.");
        if (dm.getMoTa().length() > 255)
            throw new IllegalArgumentException("Mô tả danh mục tối đa 255 ký tự.");
        return dm;
    }

    private String taoMaDanhMuc(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT NEXT VALUE FOR dbo.SeqMaDanhMuc");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format(Locale.ROOT, "DM%02d", rs.getLong(1));
        }
    }

}
