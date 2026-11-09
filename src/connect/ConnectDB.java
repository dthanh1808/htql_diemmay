package connect;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

/**
 * Đọc cấu hình và mở kết nối tới SQL Server.
 */
public class ConnectDB {
    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=QuanLyBanHang;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "loginTimeout=5;"
            + "socketTimeout=15000;";

    private static ConnectDB instance;
    private static Connection connection;

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        if (instance == null) {
            instance = new ConnectDB();
        }

        return instance;
    }

    /**
     * Giữ lại cách lấy kết nối cũ cho các DAO khác trong project.
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = openConnection();
            }

            return connection;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không kết nối được SQL Server.",
                    exception);
        }
    }

    /**
     * Mở một kết nối mới. DAO nên dùng hàm này trong try-with-resources.
     */
    public static Connection openConnection() throws SQLException {
        Properties fileSettings = loadFileSettings();

        String url = readSetting(
                "giathanh.db.url",
                "GIATHANH_DB_URL",
                fileSettings.getProperty("db.url", DEFAULT_URL));

        if (usesWindowsAuthentication(url)) {
            return DriverManager.getConnection(url);
        }

        String username = readSetting(
                "giathanh.db.user",
                "GIATHANH_DB_USER",
                fileSettings.getProperty("db.user", ""));

        String password = readSetting(
                "giathanh.db.password",
                "GIATHANH_DB_PASSWORD",
                fileSettings.getProperty("db.password", ""));

        if (username.isBlank()) {
            throw new SQLException(
                    "Chưa cấu hình db.user trong config/database.properties.");
        }

        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadFileSettings() throws SQLException {
        Properties settings = new Properties();
        String configuredPath = System.getProperty(
                "giathanh.db.config",
                "config/database.properties");
        Path file = Path.of(configuredPath);

        if (!Files.exists(file)) {
            return settings;
        }

        try (Reader reader = Files.newBufferedReader(
                file,
                StandardCharsets.UTF_8)) {
            settings.load(reader);
            return settings;
        } catch (IOException exception) {
            throw new SQLException(
                    "Không đọc được cấu hình " + file,
                    exception);
        }
    }

    private static String readSetting(
            String propertyName,
            String environmentName,
            String defaultValue) {
        String propertyValue = System.getProperty(propertyName);

        if (propertyValue != null) {
            return propertyValue;
        }

        String environmentValue = System.getenv(environmentName);

        if (environmentValue != null) {
            return environmentValue;
        }

        return defaultValue;
    }

    private static boolean usesWindowsAuthentication(String url) {
        String lowerUrl = url.toLowerCase(Locale.ROOT);
        return lowerUrl.contains("integratedsecurity=true");
    }
}
