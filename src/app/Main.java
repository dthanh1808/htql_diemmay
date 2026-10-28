package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ui.UI_TrangChu;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { System.err.println("Sử dụng giao diện Swing mặc định."); }
        SwingUtilities.invokeLater(() -> new UI_TrangChu().setVisible(true));
    }
}
