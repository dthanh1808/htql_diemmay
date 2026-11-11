package ui;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/** Điểm chạy riêng hai mục, không phụ thuộc UI_TrangChu hoặc các module khác. */
public final class UI_SanPhamKhachHang {
    private UI_SanPhamKhachHang() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) { }

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Sản Phẩm", new PNL_SanPham());
            tabs.addTab("Khách Hàng", new PNL_KhachHang());

            JFrame window = new JFrame("Quản lý Sản Phẩm và Khách Hàng");
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            window.setContentPane(tabs);
            window.setMinimumSize(new Dimension(1000, 650));
            window.setSize(1300, 850);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
