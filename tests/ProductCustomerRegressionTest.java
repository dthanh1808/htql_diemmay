package ui;

import dao.DanhMucDAO;
import dao.KhachHangDAO;
import dao.SanPhamDAO;
import entity.DanhMuc;
import entity.KhachHang;
import entity.SanPham;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

/** Hồi quy độc lập SQL Server: kiểm tra UI/logic và hợp đồng tham số JDBC. */
public final class ProductCustomerRegressionTest {
    private static final AtomicInteger connections = new AtomicInteger();
    private static volatile Map<Integer, Object> insertedProduct;
    private static volatile String deletedCategory;
    private static volatile boolean databaseUnavailable;
    private static volatile boolean productInactive;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("giathanh.db.url", "jdbc:product-customer-test:");
        DriverManager.registerDriver(new TestDriver());

        SanPham product = new SanPham(); product.setMaSKU("TL-001");
        check("TL-001".equals(product.getSku()), "SKU phải là khóa duy nhất của sản phẩm");
        try { SanPham.class.getMethod("getId"); throw new AssertionError("Còn API product ID"); }
        catch (NoSuchMethodException expected) { }
        try { product.setSlTon(-1); throw new AssertionError("Nhận tồn kho âm"); }
        catch (IllegalArgumentException expected) { }

        KhachHang unknownBirthday = customer("KH001", null, "0");
        KhachHang inRange = customer("KH002", LocalDate.of(2000, 6, 15), "35000000");
        KhachHang outside = customer("KH003", LocalDate.of(1980, 1, 1), "70000000");
        KhachHangDAO customers = new KhachHangDAO();
        var filtered = customers.timKiem(List.of(unknownBirthday, inRange, outside), "", null, null,
                LocalDate.of(1999, 1, 1), LocalDate.of(2001, 1, 1));
        check(filtered.size() == 1 && filtered.get(0) == inRange, "Lọc ngày sinh phải loại hồ sơ chưa có ngày sinh");
        check("Gold".equals(customers.layHang(inRange).getTen()), "Sai ngưỡng hạng Gold");
        check("Diamond".equals(customers.layHang(outside).getTen()), "Sai ngưỡng hạng Diamond");

        new DanhMucDAO().xoa("  DM_TEST  ");
        check("DM_TEST".equals(deletedCategory), "DELETE danh mục phải bind mã đã trim");

        PNL_SanPham[] products = new PNL_SanPham[1];
        PNL_KhachHang[] customerPanel = new PNL_KhachHang[1];
        SwingUtilities.invokeAndWait(() -> { products[0] = new PNL_SanPham(); customerPanel[0] = new PNL_KhachHang(); });
        await(products[0]); await(customerPanel[0]);
        SwingUtilities.invokeAndWait(() -> {
            JTable table = (JTable) named(products[0], "product.table");
            check(!table.isCellEditable(0, 6), "Trạng thái sản phẩm không được sửa trực tiếp trên bảng");
            check(table.isCellEditable(0, 7), "Cột thao tác phải mở được editor chứa nút Xem/Sửa");
        });

        // Gõ số vào editor rồi bấm Lưu ngay, không chuyển focus hay bấm mũi tên spinner.
        SwingUtilities.invokeAndWait(() -> {
            try {
                Class<?> dataType = Class.forName("ui.PNL_SanPham$ProductFormData");
                Constructor<?> dataCtor = dataType.getDeclaredConstructors()[0]; dataCtor.setAccessible(true);
                Object data = dataCtor.newInstance(null, new ArrayList<>(List.of(new DanhMuc("DM_TEST", "Danh mục thử", ""))), new ArrayList<>());
                Class<?> formType = Class.forName("ui.PNL_SanPham$SanPhamForm");
                Constructor<?> formCtor = formType.getDeclaredConstructor(PNL_SanPham.class, dataType); formCtor.setAccessible(true);
                Container form = (Container) formCtor.newInstance(products[0], data);
                ((JTextField) named(form, "product.form.sku")).setText("TEST-SKU");
                ((JTextField) named(form, "product.form.name")).setText("Sản phẩm thử");
                ((JTextField) named(form, "product.form.brand")).setText("Brand");
                ((JTextField) named(form, "product.form.price")).setText("1000000");
                JSpinner stock = (JSpinner) named(form, "product.form.stock");
                ((JSpinner.DefaultEditor) stock.getEditor()).getTextField().setText("17");
                Method save = formType.getDeclaredMethod("luu"); save.setAccessible(true); save.invoke(form);
            } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
        });
        await(products[0]);
        check(insertedProduct != null && Integer.valueOf(17).equals(insertedProduct.get(6)), "Mất tồn kho vừa gõ khi lưu");
        check("TEST-SKU".equals(insertedProduct.get(1)), "INSERT phải dùng SKU người dùng nhập");
        productInactive = true;
        try { new SanPhamDAO().xoa("TEST-SKU"); throw new AssertionError("Xóa sản phẩm còn tồn kho"); }
        catch (IllegalArgumentException expected) { check(expected.getMessage().contains("tồn kho"), "Phải chặn do tồn kho còn hàng"); }

        int beforeCsv = connections.get();
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field f = PNL_KhachHang.class.getDeclaredField("filtered"); f.setAccessible(true);
                f.set(customerPanel[0], new ArrayList<>(List.of(inRange)));
                check(customerPanel[0].taoCsv().contains("35000000"), "CSV thiếu chi tiêu trong snapshot");
            } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
        });
        check(connections.get() == beforeCsv, "Xuất CSV không được truy vấn lại từng khách hàng");

        databaseUnavailable = true;
        SwingUtilities.invokeAndWait(() -> { products[0] = new PNL_SanPham(); customerPanel[0] = new PNL_KhachHang(); });
        await(products[0]); await(customerPanel[0]);
        SwingUtilities.invokeAndWait(() -> check(named(customerPanel[0], "customer.notice").isVisible(), "Phải hiển thị lỗi kết nối thay vì làm vỡ constructor UI"));
        System.out.println("PASS: SKU, tồn kho, lọc ngày sinh, hạng, DELETE danh mục, cột thao tác, CSV, JDBC ngoài EDT và mất kết nối.");
    }

    private static KhachHang customer(String code, LocalDate dob, String spending) {
        KhachHang kh = new KhachHang(); kh.setMaKH(code); kh.setHoTen("Nguyễn Văn An");
        kh.setSdt("0901234567"); kh.setNgaySinh(dob); kh.setTongChiTieu(new BigDecimal(spending)); return kh;
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static Component named(Container root, String name) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container c) { Component found = named(c, name); if (found != null) return found; }
        }
        return null;
    }
    private static void await(JComponent panel) throws Exception {
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (System.nanoTime() < deadline) {
            boolean[] busy = {true};
            SwingUtilities.invokeAndWait(() -> busy[0] = Boolean.TRUE.equals(panel.getClientProperty("productCustomer.busy")));
            if (!busy[0]) return;
            Thread.sleep(20);
        }
        throw new AssertionError("Worker không kết thúc");
    }
    @SuppressWarnings("unchecked") private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }
    private static Object zero(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
    private static ResultSet rows(Map<String, Object> row) {
        boolean[] advanced = {false};
        return proxy(ResultSet.class, (p, m, a) -> {
            if (m.getName().equals("next")) { if (advanced[0] || row == null) return false; advanced[0] = true; return true; }
            Object value = a != null && a.length > 0 && row != null ? row.get(String.valueOf(a[0])) : null;
            return switch (m.getName()) {
                case "getString" -> value;
                case "getInt" -> value == null ? 0 : ((Number) value).intValue();
                case "getBigDecimal" -> value == null ? BigDecimal.ZERO : value;
                case "getBoolean" -> value != null && (Boolean) value;
                default -> zero(m.getReturnType());
            };
        });
    }
    private static final class TestDriver implements Driver {
        public Connection connect(String url, Properties properties) throws SQLException {
            if (!acceptsURL(url)) return null;
            check(!SwingUtilities.isEventDispatchThread(), "JDBC đang chặn EDT");
            connections.incrementAndGet();
            if (databaseUnavailable) throw new SQLException("Database thử nghiệm không khả dụng");
            return proxy(Connection.class, (p, m, a) -> {
                if (!m.getName().equals("prepareStatement")) return zero(m.getReturnType());
                String sql = (String) a[0]; Map<Integer, Object> params = new HashMap<>();
                return proxy(PreparedStatement.class, (stmt, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer i) {
                        params.put(i, name.equals("setNull") ? null : args[1]); return null;
                    }
                    if (name.equals("executeUpdate")) {
                        long placeholders = sql.chars().filter(c -> c == '?').count();
                        for (int i = 1; i <= placeholders; i++) check(params.containsKey(i), "Chưa bind tham số " + i + " của " + sql);
                        if (sql.startsWith("DELETE FROM DanhMuc")) deletedCategory = (String) params.get(1);
                        if (sql.startsWith("INSERT INTO SanPham")) insertedProduct = new HashMap<>(params);
                        return 1;
                    }
                    if (name.equals("executeQuery")) {
                        if (sql.contains("COUNT(*)")) return rows(Map.of("1", 0));
                        if (sql.contains("FROM SanPham sp") && insertedProduct != null) return rows(Map.of(
                                "maSKU", "TEST-SKU", "tenSP", "Sản phẩm thử", "thuongHieu", "Brand", "donGia", new BigDecimal("1000000"),
                                "slTon", 17, "trangThai", !productInactive, "maDanhMuc", "DM_TEST"));
                        return rows(null);
                    }
                    return zero(method.getReturnType());
                });
            });
        }
        public boolean acceptsURL(String url) { return url.startsWith("jdbc:product-customer-test:"); }
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) { return new DriverPropertyInfo[0]; }
        public int getMajorVersion() { return 1; }
        public int getMinorVersion() { return 0; }
        public boolean jdbcCompliant() { return false; }
        public Logger getParentLogger() { return Logger.getGlobal(); }
    }
}
