package dao;

import connect.ConnectDB;
import entity.ChiTietHoaDon; // Giả định bạn đã có hoặc sẽ tạo entity này, bên dưới tôi sẽ cung cấp mẫu luôn
import entity.HoaDon;
import entity.SanPham;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDon_DAO {

    private Connection conn = ConnectDB.getConnection();

    public ChiTietHoaDon_DAO() {
    }

    // Helper method để map dữ liệu từ ResultSet sang Object ChiTietHoaDon
    private ChiTietHoaDon docDong(ResultSet rs) throws SQLException {
        ChiTietHoaDon cthd = new ChiTietHoaDon();
        
        // Map Hóa Đơn
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getString("maHD"));
        cthd.setHoaDon(hd);

        // Map Sản Phẩm (Thông qua maSKU)
        SanPham sp = new SanPham();
        sp.setMaSKU(rs.getString("maSKU"));
        sp.setTenSP(rs.getString("tenSP")); // Lấy thêm tên SP nếu có JOIN
        cthd.setSanPham(sp);

        cthd.setSoLuong(rs.getInt("soLuong"));
        cthd.setDonGia(rs.getBigDecimal("donGia"));
        cthd.setThanhTien(rs.getBigDecimal("thanhTien"));

        return cthd;
    }

    /**
     * Lấy danh sách chi tiết của một hóa đơn cụ thể (Dùng cho xem chi tiết hóa đơn)
     */
    public List<ChiTietHoaDon> getByMaHD(String maHD) {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT cthd.*, sp.tenSP FROM ChiTietHoaDon cthd " +
                     "JOIN SanPham sp ON cthd.maSKU = sp.maSKU " +
                     "WHERE cthd.maHD = ?";
        
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maHD);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(docDong(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm một dòng chi tiết hóa đơn (Thường được gọi lặp trong vòng lặp khi thanh toán)
     */
    public boolean themChiTietHoaDon(ChiTietHoaDon cthd) {
        String sql = "INSERT INTO ChiTietHoaDon (maHD, maSKU, soLuong, donGia, thanhTien) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, cthd.getHoaDon() != null ? cthd.getHoaDon().getMaHD() : null);
            pst.setString(2, cthd.getSanPham() != null ? cthd.getSanPham().getMaSKU() : null);
            pst.setInt(3, cthd.getSoLuong());
            pst.setBigDecimal(4, cthd.getDonGia() != null ? cthd.getDonGia() : BigDecimal.ZERO);
            pst.setBigDecimal(5, cthd.getThanhTien() != null ? cthd.getThanhTien() : BigDecimal.ZERO);
            
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa toàn bộ chi tiết hóa đơn theo mã hóa đơn (Dùng trong trường hợp hủy/sửa hóa đơn trước khi chốt)
     */
    public boolean xoaTheoMaHD(String maHD) {
        String sql = "DELETE FROM ChiTietHoaDon WHERE maHD = ?";
        
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maHD);
            return pst.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}