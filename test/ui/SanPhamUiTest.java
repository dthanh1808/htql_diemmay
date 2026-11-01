package ui;

import dao.DuLieuTam;
import dao.SanPhamDAO;
import dao.DanhMucDAO;
import entity.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;

/** Thực hiện các luồng trên Swing EDT, không mở cửa sổ hoặc hộp thoại. */
public final class SanPhamUiTest {
    private static int checks;
    private static void check(boolean result, String label) {
        checks++; if (!result) throw new AssertionError(label);
    }
    private static Component find(Component root, java.util.function.Predicate<Component> predicate) {
        if (!root.isVisible()) return null;
        if (predicate.test(root)) return root;
        if (root instanceof Container parent) for (Component child : parent.getComponents()) {
            Component found = find(child, predicate); if (found != null) return found;
        }
        return null;
    }
    private static JButton button(Component root, String text) {
        JButton result = (JButton) find(root, c -> c instanceof JButton b && b.getText().equals(text));
        if (result == null) throw new AssertionError("Missing button: " + text);
        return result;
    }
    private static Component named(Component root, String name) {
        Component result = find(root, c -> name.equals(c.getName()));
        if (result == null) throw new AssertionError("Missing field: " + name);
        return result;
    }
    private static void set(Component root, String field, String value) { ((JTextField) named(root, field)).setText(value); }
    private static int count(SanPhamDAO dao) { return dao.layTatCa().size(); }

    private static void flows(UI_SanPham panel, SanPhamDAO dao, DanhMucDAO danhMucDAO) throws Exception {
        check(((JTable) named(panel, "product.table")).getRowCount() == 8, "Load sample rows");
        button(panel, "Thêm sản phẩm").doClick();
        set(panel, "product.form.sku", "UI-NEW"); set(panel, "product.form.name", "Sản phẩm từ giao diện");
        set(panel, "product.form.brand", "UI Brand"); set(panel, "product.form.price", "invalid");
        button(panel, "Lưu sản phẩm").doClick();
        check(count(dao) == 8 && named(panel, "product.form.price") != null, "Invalid form remains, nothing saved");
        set(panel, "product.form.price", "15000000"); button(panel, "Lưu sản phẩm").doClick();
        check(count(dao) == 9 && button(panel, "Chỉnh sửa") != null, "Create opens detail");
        SanPham added = dao.timKiem("UI-NEW", null, null, 0, null, null).get(0);
        button(panel, "Chỉnh sửa").doClick();
        check(!button(panel, "Xóa sản phẩm").isEnabled(), "Delete disabled for active product");
        ((JComboBox<?>) named(panel, "product.form.status")).setSelectedIndex(1);
        button(panel, "Lưu sản phẩm").doClick();
        check(!dao.timTheoMa(added.getId()).isDangKinhDoanh(), "Stop via editor");
        button(panel, "Chỉnh sửa").doClick();
        check(button(panel, "Xóa sản phẩm").isEnabled(), "Delete enabled only after saved stop");
        button(panel, "Xóa sản phẩm").doClick();
        button(panel, "Quay lại").doClick();
        check(dao.timTheoMa(added.getId()) != null, "Cancel deletion preserves product");
        button(panel, "Xóa sản phẩm").doClick(); button(panel, "Xóa sản phẩm").doClick();
        check(dao.timTheoMa(added.getId()) == null && count(dao) == 8, "Confirm permanent deletion");

        set(panel, "product.search", "MAY GIAT"); ((JTextField) named(panel, "product.search")).postActionEvent();
        JTable table = (JTable) named(panel, "product.table");
        check(table.getRowCount() == 1 && ((SanPham) table.getValueAt(0, 0)).getSku().equals("MG001"), "Enter applies search");
        button(panel, "Xóa bộ lọc").doClick(); check(table.getRowCount() == 8, "Clear restores filters");
        table.editCellAt(0, 6);
        button(table.getEditorComponent(), "Xem").doClick();
        check(button(panel, "Chỉnh sửa") != null, "Row action wired to detail");
        button(panel, "Về danh sách").doClick();

        button(panel, "Thêm sản phẩm").doClick(); set(panel, "product.form.name", "Bản nháp");
        button(panel, "Hủy / Quay lại").doClick(); button(panel, "Quay lại").doClick();
        check(((JTextField) named(panel, "product.form.name")).getText().equals("Bản nháp"), "Cancel discard retains draft");
        button(panel, "Hủy / Quay lại").doClick(); button(panel, "Bỏ thay đổi").doClick();
        check(count(dao) == 8 && named(panel, "product.table") != null, "Discard unsaved draft");

        String categoryId = danhMucDAO.layTatCa().get(0).getId();
        for (int i = 0; i < 15; i++) {
            SanPham sp = new SanPham(null, "PAGE-" + i, "Sản phẩm phân trang " + i, categoryId,
                    "Paging", BigDecimal.valueOf(i + 1), 0, true, "", "", null, null);
            dao.them(sp);
        }
        button(panel, "Làm mới danh sách").doClick();
        table = (JTable) named(panel, "product.table");
        check(table.getRowCount() == 10 && button(panel, "Trang sau").isEnabled(), "Page size limits rows");
        button(panel, "Trang sau").doClick(); check(table.getRowCount() == 10 && button(panel, "Trang trước").isEnabled(), "Next page");
        button(panel, "Trang sau").doClick(); check(table.getRowCount() == 3 && !button(panel, "Trang sau").isEnabled(), "Last page");
        set(panel, "product.search", "not-found-anywhere"); button(panel, "Áp dụng bộ lọc").doClick();
        check(!button(panel, "Trang sau").isEnabled() && !button(panel, "Trang trước").isEnabled(), "Empty results disable navigation");
        button(panel, "Xóa bộ lọc").doClick();

        button(panel, "Quản lý danh mục").doClick();
        set(panel, "category.form.name", "Danh mục UI"); button(panel, "Lưu danh mục").doClick();
        check(danhMucDAO.layTatCa().size() == 9, "Category create");
        set(panel, "category.form.name", "Tên chưa lưu");
        JTable categories = (JTable) named(panel, "category.table");
        int target = categories.getSelectedRow() == 0 ? 1 : 0;
        categories.setRowSelectionInterval(target, target);
        button(panel, "Quay lại").doClick();
        check(((JTextField) named(panel, "category.form.name")).getText().equals("Tên chưa lưu"), "Changing selection does not lose category draft");
        set(panel, "category.form.name", "Danh mục UI");
        check(button(panel, "Xóa danh mục").isEnabled(), "Empty category can be deleted");
        button(panel, "Xóa danh mục").doClick(); button(panel, "Xóa danh mục").doClick();
        check(danhMucDAO.layTatCa().size() == 8, "Category delete confirmed");
        categories = (JTable) named(panel, "category.table"); categories.setRowSelectionInterval(0, 0);
        check(!button(panel, "Xóa danh mục").isEnabled(), "Used category delete disabled");
        button(panel, "Về danh sách sản phẩm").doClick();
        ((JComboBox<?>) named(panel, "product.sort")).setSelectedIndex(2);
        table = (JTable) named(panel, "product.table");
        check(((SanPham) table.getValueAt(0, 0)).getGiaBan().equals(BigDecimal.ONE), "Numeric sort wired");
    }

    public static void main(String[] args) throws Exception {
        DuLieuTam duLieu = new DuLieuTam();
        SanPhamDAO dao = new SanPhamDAO(duLieu);
        DanhMucDAO danhMucDAO = new DanhMucDAO(duLieu);
        SwingUtilities.invokeAndWait(() -> {
            try { flows(new UI_SanPham(duLieu), dao, danhMucDAO); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("SanPhamUiTest: " + checks + " checks passed");
    }
}
