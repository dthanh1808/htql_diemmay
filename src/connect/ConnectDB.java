package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance;
    private static Connection connection;

    private ConnectDB() {
        // Kết nối được mở khi cần; lỗi được chuyển cho bên gọi.
    }

    public static ConnectDB getInstance() {
        if (instance == null) {
            instance = new ConnectDB();
        }
        return instance;
    }

    public static synchronized Connection getConnection() {
        try {
            if(connection==null || connection.isClosed()) connection=openConnection();
            return connection;
        }catch(SQLException e){throw new IllegalStateException("Không kết nối được SQL Server.",e);}
    }
    /** Mỗi transaction có kết nối riêng. */
    public static Connection openConnection() throws SQLException {
        java.util.Properties settings=new java.util.Properties();
        java.nio.file.Path file=java.nio.file.Path.of(System.getProperty("giathanh.db.config","config/database.properties"));
        if(java.nio.file.Files.exists(file))try(var reader=java.nio.file.Files.newBufferedReader(file,java.nio.charset.StandardCharsets.UTF_8)){settings.load(reader);}
        catch(java.io.IOException e){throw new SQLException("Không đọc được cấu hình "+file,e);}
        String url=config("giathanh.db.url","GIATHANH_DB_URL",
            settings.getProperty("db.url","jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanHang;encrypt=true;trustServerCertificate=true;loginTimeout=5;socketTimeout=15000;"));
        java.util.Properties properties=new java.util.Properties();
        properties.setProperty("loginTimeout","5");properties.setProperty("socketTimeout","15000");
        if(!url.toLowerCase(java.util.Locale.ROOT).contains("integratedsecurity=true")) {
            properties.setProperty("user",config("giathanh.db.user","GIATHANH_DB_USER",settings.getProperty("db.user","")));
            properties.setProperty("password",config("giathanh.db.password","GIATHANH_DB_PASSWORD",settings.getProperty("db.password","")));
        }
        return DriverManager.getConnection(url,properties);
    }
    private static String config(String key,String env,String fallback) {
        String property=System.getProperty(key);if(property!=null)return property;
        String value=System.getenv(env);return value==null?fallback:value;
    }
}
