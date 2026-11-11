package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connect.ConnectDB;
import entity.PhieuBaoHanh;
import entity.SanPham;
import entity.KhachHang;
import entity.NhanVien;
import entity.HoaDon;

public class PhieuBaoHanh_DAO {

    // Helper method để map ResultSet sang Object (Giúp tránh lặp code)
    private PhieuBaoHanh mapResultSetToEntity(ResultSet rs) throws SQLException {
        PhieuBaoHanh phieu = new PhieuBaoHanh();
        phieu.setMaPhieu(rs.getString("maPhieu"));
        phieu.setSoSerial(rs.getString("soSerial"));
        phieu.setLoaiBaoHanh(rs.getString("loaiBaoHanh"));
        phieu.setNgayTiepNhan(rs.getDate("ngayTiepNhan"));
        phieu.setNgayHenTra(rs.getDate("ngayHenTra"));
        phieu.setGhiChu(rs.getString("ghiChu"));
        phieu.setChiPhiSuaChua(rs.getDouble("chiPhiSuaChua"));
        phieu.setTrangThai(rs.getString("trangThai"));
        
        // Khởi tạo các Entity liên quan dựa trên khóa ngoại (Tùy thuộc cấu trúc Entity của bạn)
        SanPham sp = new SanPham(); 
        sp.setMaSKU(rs.getString("maSKU")); 
        phieu.setSanPham(sp);
        
        KhachHang kh = new KhachHang(); 
        kh.setMaKH(rs.getString("maKH")); 
        phieu.setKhachHang(kh);
        
        NhanVien nv = new NhanVien(); 
        nv.setMaNV(rs.getString("maNV")); 
        phieu.setNhanVien(nv);
        
        HoaDon hd = new HoaDon(); 
        hd.setMaHD(rs.getString("maHD")); 
        phieu.setHoaDon(hd);
        
        return phieu;
    }

    // Lấy toàn bộ danh sách phiếu bảo hành
    public List<PhieuBaoHanh> getAll() {
        List<PhieuBaoHanh> list = new ArrayList<>();
        String sql = "SELECT * FROM PhieuBaoHanh"; 
        
        try (Connection conn = ConnectDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Không thể tải danh sách phiếu bảo hành từ cơ sở dữ liệu.", e);
        }
        return list;
    }
    
    // Thêm mới phiếu bảo hành
    public boolean themPhieuBaoHanh(PhieuBaoHanh phieu) {
        String sql = "INSERT INTO PhieuBaoHanh (maPhieu, soSerial, maSKU, maKH, maNV, maHD, ngayTiepNhan, ngayHenTra, chiPhiSuaChua, trangThai, loaiBaoHanh, ghiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, phieu.getMaPhieu());
            pst.setString(2, phieu.getSoSerial());
            pst.setString(3, phieu.getSanPham() != null ? phieu.getSanPham().getMaSKU() : null); 
            pst.setString(4, phieu.getKhachHang() != null ? phieu.getKhachHang().getMaKH() : null);  
            pst.setString(5, phieu.getNhanVien() != null ? phieu.getNhanVien().getMaNV() : null); 
            pst.setString(6, phieu.getHoaDon() != null ? phieu.getHoaDon().getMaHD() : null); 
            
            pst.setDate(7, phieu.getNgayTiepNhan() != null ? new java.sql.Date(phieu.getNgayTiepNhan().getTime()) : null);
            pst.setDate(8, phieu.getNgayHenTra() != null ? new java.sql.Date(phieu.getNgayHenTra().getTime()) : null);
            
            pst.setDouble(9, phieu.getChiPhiSuaChua());
            pst.setString(10, phieu.getTrangThai());
            pst.setString(11, phieu.getLoaiBaoHanh());
            pst.setString(12, phieu.getGhiChu());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin phiếu bảo hành (Dựa vào maPhieu)
    public boolean capNhatPhieuBaoHanh(PhieuBaoHanh phieu) {
        String sql = "UPDATE PhieuBaoHanh SET soSerial = ?, maSKU = ?, maKH = ?, maNV = ?, maHD = ?, "
                   + "ngayTiepNhan = ?, ngayHenTra = ?, chiPhiSuaChua = ?, trangThai = ?, "
                   + "loaiBaoHanh = ?, ghiChu = ? WHERE maPhieu = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, phieu.getSoSerial());
            pst.setString(2, phieu.getSanPham() != null ? phieu.getSanPham().getMaSKU() : null);
            pst.setString(3, phieu.getKhachHang() != null ? phieu.getKhachHang().getMaKH() : null);
            pst.setString(4, phieu.getNhanVien() != null ? phieu.getNhanVien().getMaNV() : null);
            pst.setString(5, phieu.getHoaDon() != null ? phieu.getHoaDon().getMaHD() : null);
            
            pst.setDate(6, phieu.getNgayTiepNhan() != null ? new java.sql.Date(phieu.getNgayTiepNhan().getTime()) : null);
            pst.setDate(7, phieu.getNgayHenTra() != null ? new java.sql.Date(phieu.getNgayHenTra().getTime()) : null);
            
            pst.setDouble(8, phieu.getChiPhiSuaChua());
            pst.setString(9, phieu.getTrangThai());
            pst.setString(10, phieu.getLoaiBaoHanh());
            pst.setString(11, phieu.getGhiChu());
            pst.setString(12, phieu.getMaPhieu()); 

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật riêng trạng thái phiếu (Ví dụ: Đang xử lý -> Đã hoàn thành)
    public boolean capNhatTrangThai(String maPhieu, String trangThaiMoi) {
        String sql = "UPDATE PhieuBaoHanh SET trangThai = ? WHERE maPhieu = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, trangThaiMoi);
            pst.setString(2, maPhieu);
            
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // =========================================================================
    // CÁC HÀM BỔ SUNG CHO NGHIỆP VỤ LẬP PHIẾU VÀ QUẢN LÝ
    // =========================================================================

    // 1. Lấy thông tin phiếu bảo hành theo Mã Phiếu
    public PhieuBaoHanh getByMaPhieu(String maPhieu) {
        String sql = "SELECT * FROM PhieuBaoHanh WHERE maPhieu = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. Lấy thông tin đối tượng liên quan qua Hóa Đơn hoặc Số Serial
    // Thay vì trả về ResultSet, ta gói dữ liệu vào một đối tượng PhieuBaoHanh mang tính chất DTO
    public PhieuBaoHanh layThongTinLapPhieu(String key) {
        // Sử dụng TOP 1 nếu là SQL Server, dùng LIMIT 1 ở cuối nếu là MySQL
    	String sql = "SELECT TOP 1 hd.maHD, kh.maKH, kh.tenKH, kh.sdt, "
    		    + "kh.diaChi, sp.maSKU, sp.tenSP "
    		    + "FROM HoaDon hd "
    		    + "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD "
    		    + "JOIN KhachHang kh ON hd.maKH = kh.maKH "
    		    + "JOIN SanPham sp ON cthd.maSKU = sp.maSKU "
    		    + "WHERE hd.maHD = ?";
                   
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, key);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    PhieuBaoHanh thongTin = new PhieuBaoHanh();
                    
                    HoaDon hd = new HoaDon();
                    hd.setMaHD(rs.getString("maHD"));
                    thongTin.setHoaDon(hd);
                    
                    KhachHang kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    // Tùy thuộc vào class KhachHang của bạn có các thuộc tính này không
                    // kh.setTenKH(rs.getString("tenKH"));
                    // kh.setSdt(rs.getString("sdt")); 
                    thongTin.setKhachHang(kh);
                    
                    SanPham sp = new SanPham();
                    sp.setMaSKU(rs.getString("maSKU"));
                    // sp.setTenSP(rs.getString("tenSP"));
                    thongTin.setSanPham(sp);
                    
                    return thongTin;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. Tự động sinh mã phiếu bảo hành (Dành cho SQL Server)
    public String phatSinhMaPhieuMoi() {
        String maMoi = "BH00001";
        String sql = "SELECT TOP 1 maPhieu FROM PhieuBaoHanh ORDER BY maPhieu DESC"; 
        
        try (Connection conn = ConnectDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            if (rs.next()) {
                String maCu = rs.getString(1); // Mẫu: BH00018
                if (maCu != null && maCu.length() > 2) {
                    int so = Integer.parseInt(maCu.substring(2)) + 1;
                    maMoi = String.format("BH%05d", so);
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return maMoi;
    }
}