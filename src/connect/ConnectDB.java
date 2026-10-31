package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance;
    private static Connection connection;

    private ConnectDB() {
        try {
            // Thay đổi cấu hình này theo SQL Server của bạn
        	String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanHang;encrypt=true;trustServerCertificate=true;";            
        	String user = "sa"; // User SQL của bạn
            String password = "123456"; // Password SQL của bạn
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ConnectDB getInstance() {
        if (instance == null) {
            instance = new ConnectDB();
        }
        return instance;
    }

    public static Connection getConnection() {
        return connection;
    }
}