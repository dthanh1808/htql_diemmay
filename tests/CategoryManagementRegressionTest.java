package ui;

import dao.DanhMucDAO;
import entity.DanhMuc;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/** Hồi quy kiểm tra Quản lý Danh mục: DAO và giao diện DanhMucPanel */
public final class CategoryManagementRegressionTest {
    private static final AtomicInteger connections = new AtomicInteger();
    private static final List<DanhMuc> mockDb = new ArrayList<>();
    private static final Map<String, Long> mockCounts = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("giathanh.db.url", "jdbc:category-test:");
        DriverManager.registerDriver(new TestDriver());

        mockDb.clear();
        mockCounts.clear();
        mockDb.add(new DanhMuc("DM01", "Tủ lạnh", "Các loại tủ lạnh"));
        mockDb.add(new DanhMuc("DM02", "Máy giặt", "Các loại máy giặt"));
        mockCounts.put("DM01", 5L);
        mockCounts.put("DM02", 0L);

        DanhMucDAO dao = new DanhMucDAO();

        // 1. Kiểm tra layTatCa
        ArrayList<DanhMuc> all = dao.layTatCa();
        check(all.size() == 2, "layTatCa phải trả về 2 danh mục");
        check("Tủ lạnh".equals(all.get(0).getTenDanhMuc()), "Sai tên danh mục DM01");

        // 2. Kiểm tra layDanhSachQuanLy
        var mgmtList = dao.layDanhSachQuanLy();
        check(mgmtList.size() == 2, "layDanhSachQuanLy phải trả về 2 dòng");
        check(mgmtList.get(0).soSanPham() == 5L, "DM01 phải có 5 sản phẩm");
        check(mgmtList.get(1).soSanPham() == 0L, "DM02 phải có 0 sản phẩm");

        // 3. Kiểm tra validation khi thêm
        try {
            dao.themDanhMuc(new DanhMuc(null, "A", ""));
            throw new AssertionError("Không được cho phép tên < 2 ký tự");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("2 đến 100"), "Thông báo lỗi độ dài không đúng");
        }

        try {
            dao.themDanhMuc(new DanhMuc(null, "Tủ lạnh", ""));
            throw new AssertionError("Không được cho phép trùng tên danh mục");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("đã tồn tại"), "Thông báo trùng tên không đúng");
        }

        // 4. Thêm danh mục mới thành công (tự sinh mã DM03)
        DanhMuc dm3 = dao.themDanhMuc(new DanhMuc(null, "Điều hòa", "Máy lạnh và điều hòa nhiệt độ"));
        check("DM03".equals(dm3.getMaDanhMuc()), "Mã tự sinh phải là DM03, thực tế: " + dm3.getMaDanhMuc());
        check(mockDb.size() == 3, "Database phải có 3 danh mục");

        // 5. Cập nhật danh mục
        dm3.setTenDanhMuc("Máy điều hòa");
        dm3.setMoTa("Mô tả mới");
        dao.capNhatDanhMuc(dm3);
        DanhMuc updated = dao.getByMa("DM03");
        check(updated != null && "Máy điều hòa".equals(updated.getTenDanhMuc()), "Cập nhật tên danh mục thất bại");

        // 6. Xóa danh mục còn sản phẩm (DM01 có 5 sp -> phải chặn)
        try {
            dao.xoa("DM01");
            throw new AssertionError("Không được xóa danh mục còn sản phẩm");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("sản phẩm"), "Phải thông báo do còn sản phẩm");
        }

        // 7. Xóa danh mục không còn sản phẩm (DM02 có 0 sp -> cho phép xóa)
        dao.xoa("  DM02  ");
        check(mockDb.stream().noneMatch(d -> "DM02".equals(d.getMaDanhMuc())), "DM02 phải bị xóa khỏi CSDL");

        // 8. Kiểm tra giao diện PNL_SanPham và DanhMucPanel
        PNL_SanPham[] panelHolder = new PNL_SanPham[1];
        SwingUtilities.invokeAndWait(() -> {
            panelHolder[0] = new PNL_SanPham();
        });
        await(panelHolder[0]);

        // Mở DanhMucPanel
        SwingUtilities.invokeAndWait(() -> {
            try {
                Method showCat = PNL_SanPham.class.getDeclaredMethod("showCategories", String.class);
                showCat.setAccessible(true);
                showCat.invoke(panelHolder[0], (String) null);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
        await(panelHolder[0]);

        // Kiểm tra các component trong giao diện Danh mục
        SwingUtilities.invokeAndWait(() -> {
            JTable catTable = (JTable) named(panelHolder[0], "category.table");
            check(catTable != null, "Không tìm thấy bảng category.table");
            check(catTable.getColumnCount() == 4, "Bảng danh mục phải có 4 cột");
            check("Mã DM".equals(catTable.getColumnName(0)), "Cột 0 phải là Mã DM");
            check("Tên danh mục".equals(catTable.getColumnName(1)), "Cột 1 phải là Tên danh mục");
            check("Số sản phẩm".equals(catTable.getColumnName(2)), "Cột 2 phải là Số sản phẩm");
            check("Mô tả".equals(catTable.getColumnName(3)), "Cột 3 phải là Mô tả");

            JTextField codeField = (JTextField) named(panelHolder[0], "category.form.code");
            JTextField nameField = (JTextField) named(panelHolder[0], "category.form.name");
            JTextArea descField = (JTextArea) named(panelHolder[0], "category.form.description");
            check(codeField != null, "Không tìm thấy category.form.code");
            check(nameField != null, "Không tìm thấy category.form.name");
            check(descField != null, "Không tìm thấy category.form.description");
            check(!codeField.isEditable(), "Mã danh mục không được cho phép sửa trực tiếp");
        });

        System.out.println("PASS: Toàn bộ kiểm thử Quản lý Danh mục (DAO, phát sinh mã, kiểm tra trùng, xóa, bảng UI, form UI) thành công!");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static Component named(Container root, String name) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container c) {
                Component found = named(c, name);
                if (found != null) return found;
            }
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

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object zero(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }

    private static ResultSet rows(List<Map<String, Object>> data) {
        final Iterator<Map<String, Object>> it = data.iterator();
        final Map<String, Object>[] current = new Map[1];
        return proxy(ResultSet.class, (p, m, a) -> {
            if (m.getName().equals("next")) {
                if (it.hasNext()) {
                    current[0] = it.next();
                    return true;
                }
                current[0] = null;
                return false;
            }
            Object val = (current[0] != null && a != null && a.length > 0) ? current[0].get(String.valueOf(a[0])) : null;
            return switch (m.getName()) {
                case "getString", "getNString" -> val == null ? null : String.valueOf(val);
                case "getLong" -> val == null ? 0L : ((Number) val).longValue();
                case "getInt" -> val == null ? 0 : ((Number) val).intValue();
                default -> zero(m.getReturnType());
            };
        });
    }

    private static final class TestDriver implements Driver {
        public Connection connect(String url, Properties properties) throws SQLException {
            if (!acceptsURL(url)) return null;
            check(!SwingUtilities.isEventDispatchThread(), "JDBC đang chặn EDT");
            connections.incrementAndGet();
            return proxy(Connection.class, (p, m, a) -> {
                if (!m.getName().equals("prepareStatement")) return zero(m.getReturnType());
                String sql = (String) a[0];
                Map<Integer, Object> params = new HashMap<>();
                return proxy(PreparedStatement.class, (stmt, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer i) {
                        params.put(i, name.equals("setNull") ? null : args[1]);
                        return null;
                    }
                    if (name.equals("executeUpdate")) {
                        if (sql.startsWith("INSERT INTO dbo.DanhMuc")) {
                            String code = (String) params.get(1);
                            String ten = (String) params.get(2);
                            String moTa = (String) params.get(3);
                            mockDb.add(new DanhMuc(code, ten, moTa));
                            return 1;
                        }
                        if (sql.startsWith("UPDATE dbo.DanhMuc")) {
                            String ten = (String) params.get(1);
                            String moTa = (String) params.get(2);
                            String code = (String) params.get(3);
                            for (DanhMuc d : mockDb) {
                                if (d.getMaDanhMuc().equals(code)) {
                                    d.setTenDanhMuc(ten);
                                    d.setMoTa(moTa);
                                    break;
                                }
                            }
                            return 1;
                        }
                        if (sql.startsWith("DELETE FROM DanhMuc")) {
                            String code = (String) params.get(1);
                            mockDb.removeIf(d -> d.getMaDanhMuc().equals(code));
                            return 1;
                        }
                        return 1;
                    }
                    if (name.equals("executeQuery")) {
                        if (sql.contains("SELECT 1 FROM dbo.DanhMuc WHERE maDanhMuc = ?")) {
                            String code = (String) params.get(1);
                            boolean exists = mockDb.stream().anyMatch(d -> d.getMaDanhMuc().equals(code));
                            return rows(exists ? List.of(Map.of("1", 1)) : List.of());
                        }
                        if (sql.contains("LOWER(tenDanhMuc) = LOWER(?)")) {
                            String ten = (String) params.get(1);
                            String exclude = (String) params.get(2);
                            boolean exists = mockDb.stream().anyMatch(d ->
                                    d.getTenDanhMuc().equalsIgnoreCase(ten) && !Objects.equals(d.getMaDanhMuc(), exclude));
                            return rows(exists ? List.of(Map.of("1", 1)) : List.of());
                        }
                        if (sql.contains("SELECT COUNT(*) FROM dbo.SanPham WHERE maDanhMuc = ?")) {
                            String code = (String) params.get(1);
                            long cnt = mockCounts.getOrDefault(code, 0L);
                            return rows(List.of(Map.of("1", cnt)));
                        }
                        if (sql.contains("SELECT maDanhMuc FROM dbo.DanhMuc WHERE maDanhMuc LIKE 'DM%'")) {
                            List<Map<String, Object>> r = new ArrayList<>();
                            for (DanhMuc d : mockDb) {
                                if (d.getMaDanhMuc().startsWith("DM")) {
                                    r.add(Map.of("maDanhMuc", d.getMaDanhMuc()));
                                }
                            }
                            return rows(r);
                        }
                        if (sql.contains("layDanhSachQuanLy") || sql.contains("COALESCE(sp.soLuong, 0)")) {
                            List<Map<String, Object>> r = new ArrayList<>();
                            for (DanhMuc d : mockDb) {
                                r.add(Map.of(
                                        "maDanhMuc", d.getMaDanhMuc(),
                                        "tenDanhMuc", d.getTenDanhMuc(),
                                        "moTa", d.getMoTa() == null ? "" : d.getMoTa(),
                                        "soLuong", mockCounts.getOrDefault(d.getMaDanhMuc(), 0L)
                                ));
                            }
                            return rows(r);
                        }
                        if (sql.contains("WHERE maDanhMuc = ?")) {
                            String code = (String) params.get(1);
                            List<Map<String, Object>> r = new ArrayList<>();
                            for (DanhMuc d : mockDb) {
                                if (d.getMaDanhMuc().equals(code)) {
                                    r.add(Map.of(
                                            "maDanhMuc", d.getMaDanhMuc(),
                                            "tenDanhMuc", d.getTenDanhMuc(),
                                            "moTa", d.getMoTa() == null ? "" : d.getMoTa()
                                    ));
                                    break;
                                }
                            }
                            return rows(r);
                        }
                        if (sql.contains("FROM dbo.DanhMuc")) {
                            List<Map<String, Object>> r = new ArrayList<>();
                            for (DanhMuc d : mockDb) {
                                r.add(Map.of(
                                        "maDanhMuc", d.getMaDanhMuc(),
                                        "tenDanhMuc", d.getTenDanhMuc(),
                                        "moTa", d.getMoTa() == null ? "" : d.getMoTa()
                                ));
                            }
                            return rows(r);
                        }
                        return rows(List.of());
                    }
                    return zero(method.getReturnType());
                });
            });
        }
        public boolean acceptsURL(String url) { return url.startsWith("jdbc:category-test:"); }
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) { return new DriverPropertyInfo[0]; }
        public int getMajorVersion() { return 1; }
        public int getMinorVersion() { return 0; }
        public boolean jdbcCompliant() { return false; }
        public Logger getParentLogger() { return Logger.getGlobal(); }
    }
}
