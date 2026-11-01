package ui;

import dao.*;
import entity.*;
import java.awt.*;
import java.util.function.Predicate;
import javax.swing.*;

/** Chạy các luồng Swing trên EDT, không mở hộp thoại. */
public class KhachHangUiTest {
    private static int checks;
    private static void check(boolean value, String label) { checks++; if (!value) throw new AssertionError(label); }
    private static Component find(Component root, Predicate<Component> predicate) {
        if (!root.isVisible()) return null;
        if (predicate.test(root)) return root;
        if (root instanceof Container) for (Component child : ((Container) root).getComponents()) {
            Component found = find(child, predicate); if (found != null) return found;
        }
        return null;
    }
    private static Component named(Component root, String name) {
        Component c = find(root, item -> name.equals(item.getName()));
        if (c == null) throw new AssertionError("Missing " + name); return c;
    }
    private static JButton button(Component root, String text) {
        JButton b = (JButton) find(root, c -> c instanceof JButton && text.equals(((JButton) c).getText()));
        if (b == null) throw new AssertionError("Missing button " + text); return b;
    }
    private static void click(Component root, String text) { button(root, text).doClick(); }
    private static void set(Component root, String name, String text) { ((JTextField) named(root, name)).setText(text); }
    private static JTable table(Component root) { return (JTable) named(root, "customer.table"); }
    private static void rowClick(UI_KhachHang ui, String caption) { JTable t = table(ui); t.editCellAt(0, 6); click(t.getEditorComponent(), caption); }

    private static void flows() {
        DuLieuTam data = new DuLieuTam(); KhachHangDAO dao = new KhachHangDAO(data); UI_KhachHang ui = new UI_KhachHang(data);
        check(table(ui).getRowCount() == 8, "Initial sample rows");
        click(ui, "Đăng ký khách hàng");
        set(ui, "customer.form.name", "Khách Giao Diện"); set(ui, "customer.form.phone", "bad"); click(ui, "Lưu khách hàng");
        check(dao.layTatCa().size() == 8 && named(ui, "customer.form.phone") != null, "Invalid form stays and saves nothing");
        set(ui, "customer.form.phone", "0912345678"); set(ui, "customer.form.birthday", "31/02/2000"); click(ui, "Lưu khách hàng");
        check(dao.layTatCa().size() == 8, "Strict date validation");
        set(ui, "customer.form.birthday", "15/06/2000"); click(ui, "Lưu khách hàng");
        KhachHang added = dao.timKiem("0912345678", null, null, null, null).get(0); String id = added.getId();
        check(dao.layTatCa().size() == 9 && ((JTabbedPane) named(ui, "customer.tabs")).getTabCount() == 4, "Create opens full detail");
        check(!added.isNhanKhuyenMai(), "New customer consent is not preselected");
        click(ui, "Chỉnh sửa"); check(!button(ui, "Xóa khách hàng").isEnabled(), "Active delete disabled");
        ((JComboBox<?>) named(ui, "customer.form.status")).setSelectedIndex(1); click(ui, "Lưu khách hàng");
        check(!dao.timTheoMa(id).isDangHoatDong(), "Stop customer through editor");
        click(ui, "Chỉnh sửa"); check(button(ui, "Xóa khách hàng").isEnabled(), "Saved inactive unused customer deletable");
        click(ui, "Xóa khách hàng"); click(ui, "Quay lại"); check(dao.timTheoMa(id) != null, "Cancel deletion");
        click(ui, "Xóa khách hàng"); click(ui, "Xóa khách hàng"); check(dao.timTheoMa(id) == null, "Confirmed deletion");
        click(ui, "Đăng ký khách hàng"); set(ui, "customer.form.name", "Bản Nháp"); click(ui, "Hủy / Quay lại"); click(ui, "Quay lại");
        check(((JTextField) named(ui, "customer.form.name")).getText().equals("Bản Nháp"), "Cancel discard keeps draft");
        click(ui, "Hủy / Quay lại"); click(ui, "Bỏ thay đổi"); check(table(ui).getRowCount() == 8, "Discard no write");
        set(ui, "customer.search", "NGUYEN TU"); ((JTextField) named(ui, "customer.search")).postActionEvent();
        check(table(ui).getRowCount() == 1 && ((String[]) table(ui).getValueAt(0, 0))[0].equals("Nguyễn Minh Tú"), "Search Enter wired");
        set(ui, "customer.from", "31/02/2026"); click(ui, "Áp dụng bộ lọc"); check(table(ui).getRowCount() == 1, "Invalid filters preserve applied result");
        click(ui, "Xóa bộ lọc"); check(table(ui).getRowCount() == 8, "Reset all filters");
        rowClick(ui, "Lịch sử"); check(((JTabbedPane) named(ui, "customer.tabs")).getSelectedIndex() == 2, "History action opens activity tab");
        ((JTabbedPane) named(ui, "customer.tabs")).setSelectedIndex(1);
        check(((JTable) named(ui, "customer.transactions")).getRowCount() == 1, "Purchase history displayed");
        click(ui, "Chỉnh sửa"); check(!button(ui, "Xóa khách hàng").isEnabled(), "Transaction customer cannot delete");
        click(ui, "Hủy / Quay lại"); click(ui, "Về danh sách");
        rowClick(ui, "Xem"); check(named(ui, "customer.tabs") != null, "View row action works"); click(ui, "Về danh sách");
        rowClick(ui, "Sửa"); check(named(ui, "customer.form.phone") != null, "Edit row action works"); click(ui, "Hủy / Quay lại"); click(ui, "Về danh sách");
        click(ui, "Chính sách hạng thành viên"); set(ui, "customer.policy.threshold.1", "3000000"); click(ui, "Lưu chính sách"); click(ui, "Áp dụng chính sách");
        check(dao.layChinhSach().get(1).getChiTieuToiThieu().intValueExact() == 1000000, "Invalid policy atomic through UI");
        click(ui, "Quay lại"); set(ui, "customer.policy.threshold.1", "2000000"); click(ui, "Lưu chính sách"); click(ui, "Áp dụng chính sách");
        check(dao.layChinhSach().get(1).getChiTieuToiThieu().intValueExact() == 2000000, "Valid policy persists");
        click(ui, "Gửi khuyến mãi"); click(ui, "Tạo thông báo nội bộ"); check(named(ui, "customer.promo.title") != null, "Empty promotion blocked");
        set(ui, "customer.promo.title", "Ưu đãi cuối tuần"); ((JTextArea) named(ui, "customer.promo.body")).setText("Giảm giá cho thành viên");
        click(ui, "Tạo thông báo nội bộ"); click(ui, "Quay lại"); check(dao.layThongBao(dao.layTatCa().get(0).getId()).isEmpty(), "Cancel promotion sends nothing");
        click(ui, "Tạo thông báo nội bộ"); click(ui, "Xác nhận tạo"); check(dao.layThongBao(dao.layTatCa().get(0).getId()).size() == 1, "Confirm creates notification once");
        for (int i = 0; i < 15; i++) dao.them(new KhachHang(null, "Khách Phân Trang", String.format("09123456%02d", i), "", "", null, true, false, "", null, null));
        click(ui, "Xóa bộ lọc"); check(table(ui).getRowCount() == 10 && button(ui, "Trang sau").isEnabled(), "Page size 10");
        click(ui, "Trang sau"); click(ui, "Trang sau"); check(table(ui).getRowCount() == 3 && !button(ui, "Trang sau").isEnabled(), "Last page");
        check(ui.taoCsv().split("\r\n").length == 24, "Export includes all filtered pages");
        ((JComboBox<?>) named(ui, "customer.pageSize")).setSelectedItem("25"); check(table(ui).getRowCount() == 23 && !button(ui, "Trang trước").isEnabled(), "Page size resets page");
        ((JComboBox<?>) named(ui, "customer.sort")).setSelectedIndex(3); check(((String[]) table(ui).getValueAt(0, 0))[0].equals("Mai Thanh Lan"), "Spending sort numeric");
        set(ui, "customer.search", "khong-co-khach"); click(ui, "Áp dụng bộ lọc");
        check(table(ui).getRowCount() == 0 && !button(ui, "Trang sau").isEnabled() && !button(ui, "Trang trước").isEnabled(), "Empty result navigation");
        check(ui.taoCsv().split("\r\n").length == 1, "Empty export header only");
        KhachHang exportCustomer = dao.them(new KhachHang(null, "Khách Xuất File", "0912345699", "", "=SUM(1,2)\nDòng hai", null, true, false, "", null, null));
        set(ui, "customer.search", exportCustomer.getId()); click(ui, "Áp dụng bộ lọc");
        check(ui.taoCsv().contains("\"'=SUM(1,2)\nDòng hai\""), "CSV quotes multiline fields and prevents formula execution");
        System.out.println("KhachHangUiTest: " + checks + " checks passed");
    }
    public static void main(String[] args) throws Exception { SwingUtilities.invokeAndWait(KhachHangUiTest::flows); }
}
