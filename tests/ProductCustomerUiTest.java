import java.awt.*;
import javax.swing.*;

/** Không cần server: lỗi JDBC phải được hiển thị trong trang, không làm hỏng khởi tạo UI. */
public class ProductCustomerUiTest {
	private static int checks;
	private static void check(boolean condition, String message) {
		checks++;
		if (!condition)
			throw new AssertionError(message);
	}
	private static JButton button(Container root, String text) {
		for (Component c : root.getComponents()) {
			if (c instanceof JButton && text.equals(((JButton) c).getText()))
				return (JButton) c;
			if (c instanceof Container) {
				JButton b = button((Container) c, text);
				if (b != null)
					return b;
			}
		}
		return null;
	}
	private static boolean notice(Container root) {
		for (Component c : root.getComponents()) {
			if (c instanceof JLabel && c.isVisible() && ((JLabel) c).getText() != null
					&& ((JLabel) c).getText().contains("SQL Server"))
				return true;
			if (c instanceof Container && notice((Container) c))
				return true;
		}
		return false;
	}
	public static void main(String[] args) throws Exception {
		System.setProperty("giathanh.db.url",
				"jdbc:sqlserver://127.0.0.1:1;databaseName=NotUsed;loginTimeout=1;socketTimeout=1000;");
		System.setProperty("giathanh.db.user", "not-used");
		System.setProperty("giathanh.db.password", "not-used");
		SwingUtilities.invokeAndWait(() -> {
			JPanel products = new ui.PNL_SanPham();
			check(notice(products), "Trang sản phẩm báo lỗi kết nối");
			JButton apply = button(products, "Áp dụng bộ lọc");
			check(apply != null, "Nút lọc sản phẩm tồn tại");
			apply.doClick();
			check(notice(products), "Lọc khi mất kết nối vẫn báo lỗi trong trang");
			JPanel customers = new ui.PNL_KhachHang();
			check(notice(customers), "Trang khách hàng báo lỗi kết nối");
			JButton register = button(customers, "Đăng ký khách hàng");
			check(register != null, "Nút đăng ký vẫn tồn tại");
			register.doClick();
			check(button(customers, "Lưu khách hàng") != null, "Mở form đăng ký khi chưa kết nối");
		});
		System.out.println("PASS " + checks + " JDBC UI failure checks.");
	}
}
