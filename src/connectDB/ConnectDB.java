package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Khung chuẩn bị cho SQL. Các DAO hiện dùng DuLieuMau, chưa gọi lớp này. */
public class ConnectDB {
    private static final String URL = "";
    private static final String USER = "";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        if (URL.isEmpty()) throw new SQLException("Chưa cấu hình SQL. Ứng dụng đang sử dụng dữ liệu mẫu.");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
