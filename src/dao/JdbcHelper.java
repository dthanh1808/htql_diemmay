package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Các việc JDBC nhỏ dùng chung; không chứa dữ liệu hoặc kết nối dùng chung. */
final class JdbcHelper {
    private JdbcHelper() { }

    // Chỉ gọi trong transaction. Câu SQL truyền vào là hằng số trong DAO.
    static String taoMa(Connection c, String sql, String prefix, int soChuSo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            r.next();
            long so = r.getLong(1) + 1;
            String phanSo = String.format(java.util.Locale.ROOT, "%0" + soChuSo + "d", so);
            if (prefix.length() + phanSo.length() > 10) throw new SQLException("Đã hết mã " + prefix + ".");
            return prefix + phanSo;
        }
    }

    static void rollback(Connection c, Exception loi) {
        try { c.rollback(); } catch (SQLException e) { loi.addSuppressed(e); }
    }

    static IllegalArgumentException loi(SQLException e) {
        String message;
        if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627)
            message = "Mã, tên, số điện thoại hoặc email đã tồn tại. Hãy kiểm tra lại.";
        else if (e.getErrorCode() == 547)
            message = "Dữ liệu đang được sử dụng hoặc không đúng ràng buộc. Hãy tải lại danh sách.";
        else if (e.getErrorCode() == 207 || e.getErrorCode() == 208)
            message = "Database thiếu bảng/cột. Hãy chạy sql/04_SanPhamKhachHang.sql.";
        else message = "Không thao tác được SQL Server. Kiểm tra config/database.properties. " + e.getMessage();
        return new IllegalArgumentException(message, e);
    }
}
