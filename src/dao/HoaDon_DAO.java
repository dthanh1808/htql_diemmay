package dao;

import connect.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.NhanVien;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon_DAO {

    private Connection conn = ConnectDB.getConnection();

    public HoaDon_DAO() {
    }

    // Helper method để map từ ResultSet sang Object HoaDon (kèm các entity liên quan nếu cần)
    private HoaDon docDong(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getString("maHD"));
        
        Timestamp tsNgayLap = rs.getTimestamp("ngayLap");
        if (tsNgayLap != null) {
            hd.setNgayLap(tsNgayLap.toLocalDateTime());
        }
        
        hd.setTongTienHang(rs.getBigDecimal("tongTienHang"));
        hd.setTienGiamGia(rs.getBigDecimal("tienGiamGia"));
        hd.setTongThanhToan(rs.getBigDecimal("tongThanhToan"));
        hd.setPhuongThucThanhToan(rs.getString("phuongThucThanhToan"));
        hd.setYeuCauGiaoLapDat(rs.getBoolean("yeuCauGiaoHang")); // Map đúng cột yeuCauGiaoHang trong CSDL
        hd.setGhiChuGiaoLapDat(rs.getString("ghiChuGiaoHang")); // Map đúng cột ghiChuGiaoHang trong CSDL
        hd.setTrangThai(rs.getString("trangThai"));

        // Map Nhân Viên
        String maNV = rs.getString("maNV");
        if (maNV != null) {
            NhanVien nv = new NhanVien();
            nv.setMaNV(maNV);
            // Có thể bổ sung truy vấn hoặc set thêm tên nếu bảng NhanVien có kết nối
            hd.setNhanVien(nv);
        }

        // Map Khách Hàng
        String maKH = rs.getString("maKH");
        if (maKH != null) {
            KhachHang kh = new KhachHang();
            kh.setMaKH(maKH);
            kh.setTenKH(rs.getString("tenKH")); // Lấy thêm tên KH nếu query có JOIN
            hd.setKhahHang(kh);
        }

        // Map Khuyến Mãi
        String maKM = rs.getString("maKM");
        if (maKM != null) {
            KhuyenMai km = new KhuyenMai();
            km.setMaKM(maKM);
            hd.setKhuyenMai(km);
        }

        return hd;
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn trong hệ thống
     */
    public List<HoaDon> getAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, kh.tenKH FROM HoaDon hd LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH ORDER BY hd.ngayLap DESC";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(docDong(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm hóa đơn theo Mã Hóa Đơn
     */
    public HoaDon getByMaHD(String maHD) {
        String sql = "SELECT hd.*, kh.tenKH FROM HoaDon hd LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH WHERE hd.maHD = ?";
        
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maHD);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return docDong(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tự động sinh mã hóa đơn mới tiếp theo (Định dạng: HD + 10 chữ số)
     */
    public String phatSinhMaMoi() {
        String maMoi = "HD0000000001";
        String sql = "SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String maCu = rs.getString(1); // Ví dụ: HD0000000005
                if (maCu != null && maCu.startsWith("HD") && maCu.length() == 12) {
                    long so = Long.parseLong(maCu.substring(2)) + 1;
                    maMoi = String.format("HD%010d", so);
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return maMoi;
    }

    /**
     * Thêm mới một hóa đơn vào CSDL
     */
    public boolean themHoaDon(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (maHD, ngayLap, tongTienHang, tienGiamGia, tongThanhToan, " +
                     "phuongThucThanhToan, yeuCauGiaoHang, ghiChuGiaoHang, trangThai, maNV, maKH, maKM) " +
                     "VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, hd.getMaHD());
            pst.setBigDecimal(2, hd.getTongTienHang() != null ? hd.getTongTienHang() : BigDecimal.ZERO);
            pst.setBigDecimal(3, hd.getTienGiamGia() != null ? hd.getTienGiamGia() : BigDecimal.ZERO);
            pst.setBigDecimal(4, hd.getTongThanhToan() != null ? hd.getTongThanhToan() : BigDecimal.ZERO);
            pst.setString(5, hd.getPhuongThucThanhToan());
            pst.setBoolean(6, hd.isYeuCauGiaoLapDat());
            pst.setString(7, hd.getGhiChuGiaoLapDat());
            pst.setString(8, hd.getTrangThai() != null ? hd.getTrangThai() : "Đã thanh toán");
            
            // Khóa ngoại Nhân viên
            if (hd.getNhanVien() != null && hd.getNhanVien().getMaNV() != null) {
                pst.setString(9, hd.getNhanVien().getMaNV());
            } else {
                pst.setNull(9, Types.VARCHAR);
            }

            // Khóa ngoại Khách hàng (Có thể null nếu khách vãng lai)
            if (hd.getKhahHang() != null && hd.getKhahHang().getMaKH() != null) {
                pst.setString(10, hd.getKhahHang().getMaKH());
            } else {
                pst.setNull(10, Types.VARCHAR);
            }

            // Khóa ngoại Khuyến mãi (Có thể null)
            if (hd.getKhuyenMai() != null && hd.getKhuyenMai().getMaKM() != null) {
                pst.setString(11, hd.getKhuyenMai().getMaKM());
            } else {
                pst.setNull(11, Types.VARCHAR);
            }

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật trạng thái hóa đơn (Ví dụ: Hủy hóa đơn, Đã thanh toán,...)
     */
    public boolean capNhatTrangThai(String maHD, String trangThaiMoi) {
        String sql = "UPDATE HoaDon SET trangThai = ? WHERE maHD = ?";
        
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi);
            pst.setString(2, maHD);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}