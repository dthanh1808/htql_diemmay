package dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connect.ConnectDB;
import entity.PhieuBaoHanh;

public class PhieuBaoHanh_DAO {
    // Giả sử bạn có class Database kết nối JDBC
    private Connection conn = ConnectDB.getConnection(); 

    // Lấy toàn bộ danh sách phiếu bảo hành
    public List<PhieuBaoHanh> getAll() {
        List<PhieuBaoHanh> list = new ArrayList<>();
        String sql = "SELECT * FROM PhieuBaoHanh"; 
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PhieuBaoHanh phieu = new PhieuBaoHanh();
                phieu.setMaPhieu(rs.getString("maPhieu"));
                phieu.setNgayTiepNhan(rs.getDate("ngayTiepNhan"));
                phieu.setLoaiBaoHanh(rs.getString("loaiBaoHanh"));
                phieu.setTrangThai(rs.getString("trangThai"));
                // Map thêm KhachHang, SanPham...
                list.add(phieu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public boolean themPhieuBaoHanh(PhieuBaoHanh phieu) {
        // Tùy thuộc vào thiết kế Database của bạn, câu SQL có thể thay đổi tên cột cho phù hợp
        String sql = "INSERT INTO PhieuBaoHanh (maPhieu, soSerial, maSP, maKH, maNV, ngayTiepNhan, ngayHenTra, moTa, chiPhiSuaChua, trangThai, loaiBaoHanh, ghiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectDB.getConnection(); // Thay Database.getConnection() bằng class kết nối của bạn
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, phieu.getMaPhieu());
            pst.setString(2, phieu.getSoSerial());
            pst.setString(3, phieu.getSanPham() != null ? phieu.getSanPham().getSku() : null); // Lấy mã SP
            pst.setString(4, phieu.getKhachHang() != null ? phieu.getKhachHang().getId() : null);  // Lấy mã KH
            pst.setString(5, phieu.getNhanVien() != null ? phieu.getNhanVien().getMaNV() : null);  // Lấy mã NV
            
            // Xử lý ngày tháng (Chuyển từ java.util.Date sang java.sql.Date)
            pst.setDate(6, phieu.getNgayTiepNhan() != null ? new java.sql.Date(phieu.getNgayTiepNhan().getTime()) : null);
            pst.setDate(7, phieu.getNgayHenTra() != null ? new java.sql.Date(phieu.getNgayHenTra().getTime()) : null);
            
            pst.setString(8, phieu.getGhiChu());
            pst.setDouble(9, phieu.getChiPhiSuaChua());
            pst.setString(10, phieu.getTrangThai());
            pst.setString(11, phieu.getLoaiBaoHanh());

            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin phiếu bảo hành (Dựa vào maPhieu)
     */
    public boolean capNhatPhieuBaoHanh(PhieuBaoHanh phieu) {
        String sql = "UPDATE PhieuBaoHanh SET soSerial = ?, maSP = ?, maKH = ?, maNV = ?, "
                   + "ngayTiepNhan = ?, ngayHenTra = ?, moTa = ?, chiPhiSuaChua = ?, trangThai = ?, "
                   + "loaiBaoHanh = ?, ghiChu = ? WHERE maPhieu = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, phieu.getSoSerial());
            pst.setString(2, phieu.getSanPham() != null ? phieu.getSanPham().getSku() : null);
            pst.setString(3, phieu.getKhachHang() != null ? phieu.getKhachHang().getId() : null);
            pst.setString(4, phieu.getNhanVien() != null ? phieu.getNhanVien().getMaNV() : null);
            
            pst.setDate(5, phieu.getNgayTiepNhan() != null ? new java.sql.Date(phieu.getNgayTiepNhan().getTime()) : null);
            pst.setDate(6, phieu.getNgayHenTra() != null ? new java.sql.Date(phieu.getNgayHenTra().getTime()) : null);
            
            pst.setString(7, phieu.getGhiChu());
            pst.setDouble(8, phieu.getChiPhiSuaChua());
            pst.setString(9, phieu.getTrangThai());
            pst.setString(10, phieu.getLoaiBaoHanh());
            
            // WHERE maPhieu = ?
            pst.setString(11, phieu.getMaPhieu());

            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật trạng thái phiếu (Đang xử lý -> Đã hoàn thành)
    public boolean capNhatTrangThai(String maPhieu, String trangThaiMoi) {
        String sql = "UPDATE PhieuBaoHanh SET trangThai = ? WHERE maPhieu = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi);
            pst.setString(2, maPhieu);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
 // =========================================================================
    // CÁC HÀM BỔ SUNG CHO NGHIỆP VỤ LẬP PHIẾU VÀ QUẢN LÝ
    // =========================================================================

    // 1. Lấy thông tin phiếu bảo hành theo Mã Phiếu (Dùng cho chức năng Xem chi tiết)
    public PhieuBaoHanh getByMaPhieu(String maPhieu) {
        String sql = "SELECT * FROM PhieuBaoHanh WHERE maPhieu = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    PhieuBaoHanh phieu = new PhieuBaoHanh();
                    phieu.setMaPhieu(rs.getString("maPhieu"));
                    phieu.setSoSerial(rs.getString("soSerial"));
                    phieu.setLoaiBaoHanh(rs.getString("loaiBaoHanh"));
                    phieu.setNgayTiepNhan(rs.getDate("ngayTiepNhan"));
                    phieu.setNgayHenTra(rs.getDate("ngayHenTra"));
                    phieu.setGhiChu(rs.getString("ghiChu"));
                    phieu.setChiPhiSuaChua(rs.getDouble("chiPhiSuaChua"));
                    phieu.setTrangThai(rs.getString("trangThai"));
                    // Lưu ý: Map thêm SanPham và KhachHang nếu CSDL có lưu khóa ngoại
                    return phieu;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. Kiểm tra thông tin hóa đơn hoặc Serial ở Bước 1 (Trả về thông tin khách hàng/sản phẩm để load lên form)
    // Bạn có thể thay đổi câu lệnh SQL nối bảng (JOIN) với bảng HoaDon / ChiTietHoaDon / SanPham / KhachHang của bạn
    public ResultSet checkThongTinSanPham(String key) {
        String sql = "TOP 1 hd.maHD, cthd.soSerial, kh.maKH, kh.tenKH, kh.sdt, kh.diaChi, sp.maSKU, sp.tenSP "
                   + "FROM HoaDon hd "
                   + "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD "
                   + "JOIN KhachHang kh ON hd.maKH = kh.maKH "
                   + "JOIN SanPham sp ON cthd.maSKU = sp.maSKU "
                   + "WHERE hd.maHD = ? OR cthd.soSerial = ?";
        // Lưu ý: Nếu dùng MySQL thì thay "TOP 1" bằng "LIMIT 1" ở cuối câu lệnh SQL
        try {
            PreparedStatement pst = conn.prepareStatement(sql.replace("TOP 1", "")); // Chỉnh lại cú pháp SELECT tuỳ theo SQL Server hay MySQL
            pst.setString(1, key);
            pst.setString(2, key);
            return pst.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. Tự động sinh mã phiếu bảo hành tiếp theo (Ví dụ: BH00019, BH00020...)
    public String phatSinhMaPhieuMoi() {
        String maMoi = "BH00001";
        String sql = "SELECT TOP 1 maPhieu FROM PhieuBaoHanh ORDER BY maPhieu DESC"; // (MySQL dùng: ORDER BY maPhieu DESC LIMIT 1)
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String maCu = rs.getString(1); // Ví dụ: BH00018
                int so = Integer.parseInt(maCu.substring(2)) + 1;
                maMoi = String.format("BH%05d", so);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maMoi;
    }
}
