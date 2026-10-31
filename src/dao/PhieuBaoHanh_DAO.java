//package dao;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//import connect.ConnectDB;
//import entity.PhieuBaoHanh;
//
//public class PhieuBaoHanh_DAO {
//    // Giả sử bạn có class Database kết nối JDBC
//    private Connection conn = ConnectDB.getConnection(); 
//
//    // Lấy toàn bộ danh sách phiếu bảo hành
//    public List<PhieuBaoHanh> getAll() {
//        List<PhieuBaoHanh> list = new ArrayList<>();
//        String sql = "SELECT * FROM PhieuBaoHanh"; 
//        try (Statement st = conn.createStatement();
//             ResultSet rs = st.executeQuery(sql)) {
//            while (rs.next()) {
//                PhieuBaoHanh phieu = new PhieuBaoHanh();
//                phieu.setMaPhieu(rs.getString("maPhieu"));
//                phieu.setNgayTiepNhan(rs.getDate("ngayTiepNhan"));
//                phieu.setLoaiBaoHanh(rs.getString("loaiBaoHanh"));
//                phieu.setTrangThai(rs.getString("trangThai"));
//                // Map thêm KhachHang, SanPham...
//                list.add(phieu);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//    
//    public boolean themPhieuBaoHanh(PhieuBaoHanh phieu) {
//        // Tùy thuộc vào thiết kế Database của bạn, câu SQL có thể thay đổi tên cột cho phù hợp
//        String sql = "INSERT INTO PhieuBaoHanh (maPhieu, soSerial, maSP, maKH, maNV, ngayTiepNhan, ngayHenTra, moTa, chiPhiSuaChua, trangThai, loaiBaoHanh, ghiChu) "
//                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        
//        try (Connection conn = ConnectDB.getConnection(); // Thay Database.getConnection() bằng class kết nối của bạn
//             PreparedStatement pst = conn.prepareStatement(sql)) {
//            
//            pst.setString(1, phieu.getMaPhieu());
//            pst.setString(2, phieu.getSoSerial());
//            pst.setString(3, phieu.getSp() != null ? phieu.getSp().getMaSKU() : null); // Lấy mã SP
//            pst.setString(4, phieu.getKh() != null ? phieu.getKh().getMaKH() : null);  // Lấy mã KH
//            pst.setString(5, phieu.getNv() != null ? phieu.getNv().getMaNV() : null);  // Lấy mã NV
//            
//            // Xử lý ngày tháng (Chuyển từ java.util.Date sang java.sql.Date)
//            pst.setDate(6, phieu.getNgayTiepNhan() != null ? new java.sql.Date(phieu.getNgayTiepNhan().getTime()) : null);
//            pst.setDate(7, phieu.getNgayHenTra() != null ? new java.sql.Date(phieu.getNgayHenTra().getTime()) : null);
//            
//            pst.setString(8, phieu.getGhiChu());
//            pst.setDouble(9, phieu.getChiPhiSuaChua());
//            pst.setString(10, phieu.getTrangThai());
//            pst.setString(11, phieu.getLoaiBaoHanh());
//
//            return pst.executeUpdate() > 0;
//            
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    /**
//     * Cập nhật thông tin phiếu bảo hành (Dựa vào maPhieu)
//     */
//    public boolean capNhatPhieuBaoHanh(PhieuBaoHanh phieu) {
//        String sql = "UPDATE PhieuBaoHanh SET soSerial = ?, maSP = ?, maKH = ?, maNV = ?, "
//                   + "ngayTiepNhan = ?, ngayHenTra = ?, moTa = ?, chiPhiSuaChua = ?, trangThai = ?, "
//                   + "loaiBaoHanh = ?, ghiChu = ? WHERE maPhieu = ?";
//        
//        try (Connection conn = ConnectDB.getConnection();
//             PreparedStatement pst = conn.prepareStatement(sql)) {
//            
//            pst.setString(1, phieu.getSoSerial());
//            pst.setString(2, phieu.getSp() != null ? phieu.getSp().getMaSKU() : null);
//            pst.setString(3, phieu.getKh() != null ? phieu.getKh().getMaKH() : null);
//            pst.setString(4, phieu.getNv() != null ? phieu.getNv().getMaNV() : null);
//            
//            pst.setDate(5, phieu.getNgayTiepNhan() != null ? new java.sql.Date(phieu.getNgayTiepNhan().getTime()) : null);
//            pst.setDate(6, phieu.getNgayHenTra() != null ? new java.sql.Date(phieu.getNgayHenTra().getTime()) : null);
//            
//            pst.setString(7, phieu.getGhiChu());
//            pst.setDouble(8, phieu.getChiPhiSuaChua());
//            pst.setString(9, phieu.getTrangThai());
//            pst.setString(10, phieu.getLoaiBaoHanh());
//            
//            // WHERE maPhieu = ?
//            pst.setString(11, phieu.getMaPhieu());
//
//            return pst.executeUpdate() > 0;
//            
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    // Cập nhật trạng thái phiếu (Đang xử lý -> Đã hoàn thành)
//    public boolean capNhatTrangThai(String maPhieu, String trangThaiMoi) {
//        String sql = "UPDATE PhieuBaoHanh SET trangThai = ? WHERE maPhieu = ?";
//        try (PreparedStatement pst = conn.prepareStatement(sql)) {
//            pst.setString(1, trangThaiMoi);
//            pst.setString(2, maPhieu);
//            return pst.executeUpdate() > 0;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//}
