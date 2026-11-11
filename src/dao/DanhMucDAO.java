package dao;

import connect.ConnectDB;
import entity.DanhMuc;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DanhMucDAO {
    // Gọi class kết nối DB của bạn. 
    // Nếu bị lỗi đỏ dòng này, hãy đổi thành: ConnectDB.getInstance().getConnection();
    private Connection conn = ConnectDB.getConnection(); 

    public DanhMucDAO() {
    }

    /**
     * Lấy toàn bộ danh sách Danh Mục từ SQL (Đã đổi tên thành layTatCa cho khớp UI)
     */
    public ArrayList<DanhMuc> layTatCa() {
        ArrayList<DanhMuc> list = new ArrayList<>();
        String sql = "SELECT * FROM DanhMuc ORDER BY maDanhMuc"; 
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setMaDanhMuc(rs.getString("maDanhMuc"));
                dm.setTenDanhMuc(rs.getString("tenDanhMuc"));
                dm.setMoTa(rs.getString("moTa"));
                list.add(dm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Đếm số lượng sản phẩm theo từng danh mục
     */
    public Map<String, Integer> demTheoDanhMuc() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT maDanhMuc, COUNT(*) as soLuong FROM SanPham GROUP BY maDanhMuc";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                counts.put(rs.getString("maDanhMuc"), rs.getInt("soLuong"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Tìm danh mục theo Mã
     */
    public DanhMuc getByMa(String maDanhMuc) {
        String sql = "SELECT * FROM DanhMuc WHERE maDanhMuc = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maDanhMuc);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    DanhMuc dm = new DanhMuc();
                    dm.setMaDanhMuc(rs.getString("maDanhMuc"));
                    dm.setTenDanhMuc(rs.getString("tenDanhMuc"));
                    dm.setMoTa(rs.getString("moTa"));
                    return dm;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tự động sinh mã danh mục mới (Dùng Sequence trong SQL Server)
     */
    public String phatSinhMaMoi() {
        String sql = "SELECT NEXT VALUE FOR dbo.SeqMaDanhMuc";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return String.format("DM%02d", rs.getLong(1)); // Ví dụ: DM07
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

    /**
     * Thêm danh mục mới vào SQL (Trả về chính DanhMuc đó thay vì boolean)
     */
    public DanhMuc themDanhMuc(DanhMuc dm) {
        // Tự động sinh mã mới nếu chưa có
        if (dm.getMaDanhMuc() == null || dm.getMaDanhMuc().isEmpty()) {
            dm.setMaDanhMuc(phatSinhMaMoi());
        }
        
        String sql = "INSERT INTO DanhMuc (maDanhMuc, tenDanhMuc, moTa) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, dm.getMaDanhMuc());
            pst.setString(2, dm.getTenDanhMuc());
            pst.setString(3, dm.getMoTa());
            pst.executeUpdate();
            return dm; // Trả về đối tượng vừa lưu thành công
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật thông tin danh mục theo Mã
     */
    public DanhMuc capNhatDanhMuc(DanhMuc dm) {
        String sql = "UPDATE DanhMuc SET tenDanhMuc = ?, moTa = ? WHERE maDanhMuc = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, dm.getTenDanhMuc());
            pst.setString(2, dm.getMoTa());
            pst.setString(3, dm.getMaDanhMuc());
            pst.executeUpdate();
            return dm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Xóa danh mục theo Mã
     */
    public void xoa(String maDanhMuc) {
        String sql = "DELETE FROM DanhMuc WHERE maDanhMuc = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maDanhMuc);
            pst.executeUpdate();
        } catch (SQLException e) {
            // Ném lỗi để UI (PNL_SanPham) bắt và hiển thị thông báo đỏ
            throw new RuntimeException("Không thể xóa danh mục này do đang chứa sản phẩm!", e);
        }
    }
}