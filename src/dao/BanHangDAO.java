package dao;

import connect.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Locale;

public class BanHangDAO {

    public BanHangDAO() {
    }

    /**
     * Tự động sinh mã hóa đơn tiếp theo theo định dạng chuẩn SQL: HD + 10 chữ số (Ví dụ: HD202606060001)
     */
    public String phatSinhMaHoaDonMoi() {
        String maMoi = "HD0000000001";
        // Lấy hóa đơn mới nhất sắp xếp giảm dần theo mã
        String sql = "SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC";
        
        try (Connection conn = ConnectDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            if (rs.next()) {
                String maCu = rs.getString(1); // Mẫu: HD0000000001
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
     * Lưu toàn bộ giao dịch bán hàng (Hóa đơn + Chi tiết hóa đơn + Trừ tồn kho + Cộng chi tiêu KH)
     * Sử dụng Transaction để đảm bảo tính toàn vẹn dữ liệu (Atomic).
     * 
     * @param maHD          Mã hóa đơn
     * @param maNV          Mã nhân viên lập hóa đơn (Ví dụ: "NV002")
     * @param maKH          Mã khách hàng (Có thể null nếu là khách vãng lai)
     * @param maKM          Mã khuyến mãi áp dụng (Có thể null)
     * @param maCa          Mã ca làm việc đang mở (Ví dụ: "CA2026060601")
     * @param tongTienHang  Tổng tiền hàng chưa giảm giá
     * @param tienGiamGia   Số tiền được giảm (từ khuyến mãi/hạng thành viên)
     * @param tongThanhToan Số tiền cuối cùng khách phải trả
     * @param pttt          Phương thức thanh toán ("Tiền mặt" hoặc "Chuyển khoản")
     * @param yeuCouGiao    Có yêu cầu giao hàng & lắp đặt hay không
     * @param ghiChuGiao    Địa chỉ hoặc ghi chú giao hàng
     * @param chiTietList   Mảng dữ liệu sản phẩm trong bảng (SKU, Số lượng, Đơn giá, Thành tiền)
     * @return true nếu thanh toán thành công, false nếu lỗi.
     */
    public boolean thanhToanHoaDon(
            String maHD, String maNV, String maKH, String maKM, String maCa,
            BigDecimal tongTienHang, BigDecimal tienGiamGia, BigDecimal tongThanhToan,
            String pttt, boolean yeuCouGiao, String ghiChuGiao,
            Object[][] chiTietList) {
        
        Connection conn = null;
        try {
            conn = ConnectDB.getConnection();
            if (conn == null) return false;
            
            // Bắt đầu Transaction
            conn.setAutoCommit(false);

            // 1. INSERT vào bảng HoaDon
            String sqlHD = "INSERT INTO HoaDon (maHD, ngayLap, tongTienHang, tienGiamGia, tongThanhToan, " +
                           "phuongThucThanhToan, yeuCauGiaoHang, ghiChuGiaoHang, trangThai, maNV, maKH, maKM, maCa) " +
                           "VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?, N'Đã thanh toán', ?, ?, ?, ?)";
            
            try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
                psHD.setString(1, maHD);
                psHD.setBigDecimal(2, tongTienHang);
                psHD.setBigDecimal(3, tienGiamGia);
                psHD.setBigDecimal(4, tongThanhToan);
                psHD.setString(5, pttt);
                psHD.setBoolean(6, yeuCouGiao);
                psHD.setString(7, yeuCouGiao ? ghiChuGiao : null);
                psHD.setString(8, maNV);
                
                if (maKH != null && !maKH.isEmpty()) {
                    psHD.setString(9, maKH);
                } else {
                    psHD.setNull(9, Types.VARCHAR);
                }
                
                if (maKM != null && !maKM.isEmpty()) {
                    psHD.setString(10, maKM);
                } else {
                    psHD.setNull(10, Types.VARCHAR);
                }
                
                if (maCa != null && !maCa.isEmpty()) {
                    psHD.setString(11, maCa);
                } else {
                    psHD.setNull(11, Types.VARCHAR);
                }
                
                psHD.executeUpdate();
            }

            // 2. INSERT vào ChiTietHoaDon và trừ tồn kho (slTon) của sản phẩm
            String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHD, maSKU, soLuong, donGia, thanhTien) VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateTon = "UPDATE SanPham SET slTon = slTon - ? WHERE maSKU = ?";

            try (PreparedStatement psCTHD = conn.prepareStatement(sqlCTHD);
                 PreparedStatement psTon = conn.prepareStatement(sqlUpdateTon)) {
                
                for (Object[] row : chiTietList) {
                    // Cấu trúc row từ PNL_BanHang: [0]:STT, [1]:Tên SP (HTML), [2]:SKU, [3]:Đơn giá, [4]:Số lượng, [5]:Thành tiền
                    String maSKU = row[2].toString();
                    int soLuong = Integer.parseInt(row[4].toString());
                    
                    // Làm sạch chuỗi tiền tệ (Xóa chữ " đ", dấu chấm phẩy) để convert sang BigDecimal
                    BigDecimal donGia = new BigDecimal(row[3].toString().replaceAll("[^0-9]", ""));
                    BigDecimal thanhTien = new BigDecimal(row[5].toString().replaceAll("[^0-9]", ""));

                    // Thêm vào ChiTietHoaDon
                    psCTHD.setString(1, maHD);
                    psCTHD.setString(2, maSKU);
                    psCTHD.setInt(3, soLuong);
                    psCTHD.setBigDecimal(4, donGia);
                    psCTHD.setBigDecimal(5, thanhTien);
                    psCTHD.executeUpdate();

                    // Trừ tồn kho sản phẩm
                    psTon.setInt(1, soLuong);
                    psTon.setString(2, maSKU);
                    psTon.executeUpdate();
                }
            }

            // 3. Nếu khách hàng có đăng nhập (maKH khác null), cập nhật tổng chi tiêu tích lũy
            // Hệ thống SQL của bạn đã có Trigger TRG_KH_DongBoHangVaNgayCapNhat tự động nâng hạng VIP
            if (maKH != null && !maKH.isEmpty()) {
                String sqlUpdateKH = "UPDATE KhachHang SET tongChiTieuTichLuy = tongChiTieuTichLuy + ? WHERE maKH = ?";
                try (PreparedStatement psKH = conn.prepareStatement(sqlUpdateKH)) {
                    psKH.setBigDecimal(1, tongThanhToan);
                    psKH.setString(2, maKH);
                    psKH.executeUpdate();
                }
            }

            // Hoàn tất Transaction thành công
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            // Nếu có lỗi bất kỳ, rollback lại toàn bộ dữ liệu để tránh lệch kho/tiền
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}