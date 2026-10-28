package ui;

import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;

/** Các thao tác Swing nhỏ dùng chung cho hai màn hình. */
public class UIHelper {
    public static final Color NEN = new Color(245, 247, 250);
    public static final Color XANH = new Color(37, 99, 235);
    public static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static JPanel theTrang() {
        JPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    public static JButton nut(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setForeground(new Color(30, 30, 30));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)), new EmptyBorder(7, 12, 7, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton nutChinh(String text) {
        JButton button = nut(text);
        button.setBackground(XANH);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        return button;
    }

    public static JPanel oLoc(String title, JComponent control) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(30, 30, 30));
        panel.add(label, BorderLayout.NORTH);
        control.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        control.setPreferredSize(new Dimension(130, 36));
        if (control instanceof JTextField) control.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)), new EmptyBorder(0, 8, 0, 8)));
        if (control instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) control;
            combo.setUI(new BasicComboBoxUI());
            combo.setBackground(Color.WHITE);
            combo.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
            for (Component c : combo.getComponents()) {
                if (c instanceof JButton) {
                    ((JButton) c).setBorder(new EmptyBorder(0, 0, 0, 0));
                    c.setBackground(Color.WHITE);
                }
            }
        }
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }

    public static JPanel hangNut(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        for (JButton button : buttons) panel.add(button);
        return panel;
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public static void dinhDangBang(JTable table) {
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer header = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)), new EmptyBorder(0, 8, 0, 8)));
                return this;
            }
        };
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(30, 30, 30));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)), new EmptyBorder(0, 8, 0, 8)));
        table.getTableHeader().setDefaultRenderer(header);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 30, 30));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setBorder(new EmptyBorder(0, 8, 0, 8));
        table.setDefaultRenderer(Object.class, cell);
    }

    public static JScrollPane cuonBang(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    public static ImageIcon icon(String name, int size) {
        java.net.URL url = UIHelper.class.getResource("/icon/" + name + ".png");
        if (url == null) return null;
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    public static JButton nutIcon(String text, String name) {
        JButton button = nut(text);
        button.setIcon(icon(name, 16));
        button.setIconTextGap(8);
        return button;
    }

    public static JButton nutMenu(String text) {
        JButton button = nut(text);
        button.setIcon(new Icon() {
            public int getIconWidth() { return 8; }
            public int getIconHeight() { return 5; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(c.getForeground());
                g.fillPolygon(new int[] {x, x + 8, x + 4}, new int[] {y, y, y + 5}, 3);
            }
        });
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setIconTextGap(8);
        return button;
    }

    public static JLabel tieuDeBang(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(30, 58, 138));
        return label;
    }

    public static class RoundedPanel extends JPanel {
        public RoundedPanel() { setOpaque(false); setBackground(Color.WHITE); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class OGoiy extends JTextField {
        private final String goiY;
        public OGoiy(String goiY) { this.goiY = goiY; setToolTipText(goiY); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont());
                g2.setColor(new Color(130, 130, 130));
                g2.clipRect(getInsets().left, 0, getWidth() - getInsets().left - getInsets().right, getHeight());
                g2.drawString(goiY, getInsets().left + 2, (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        }
    }

    /** Nhãn màu giống giao diện ban đầu, chỉ vẽ phần hiển thị của ô. */
    public static class NhanMau extends JPanel implements TableCellRenderer {
        private final JLabel label = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        public NhanMau() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 10));
            label.setOpaque(false);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            label.setBorder(new EmptyBorder(4, 7, 4, 7));
            add(label);
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
            String text = String.valueOf(value);
            label.setText(text);
            label.setPreferredSize(new Dimension(label.getFontMetrics(label.getFont()).stringWidth(text) + 24, 26));
            Color nen = new Color(241, 245, 249), chu = new Color(71, 85, 105);
            if (text.equals("Diamond") || text.equals("Máy giặt")) { nen = new Color(243, 232, 255); chu = new Color(147, 51, 234); }
            else if (text.equals("Gold") || text.equals("Tivi") || text.startsWith("Ngưng")) { nen = new Color(255, 237, 213); chu = new Color(194, 65, 12); }
            else if (text.equals("Silver") || text.equals("Tủ lạnh")) { nen = new Color(224, 242, 254); chu = new Color(2, 132, 199); }
            else if (text.startsWith("Đang") || text.equals("Điều hòa")) { nen = new Color(220, 252, 231); chu = new Color(22, 163, 74); }
            label.setBackground(nen); label.setForeground(chu);
            setBackground(selected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    public static class ThaoTacRenderer extends JPanel implements TableCellRenderer {
        public ThaoTacRenderer(boolean coSua) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 12, 14));
            JLabel xem = new JLabel(icon("ic_xemchitiet", 16));
            if (xem.getIcon() == null) xem.setText("Xem");
            add(xem);
            if (coSua) {
                JLabel sua = new JLabel(icon("ic_chinhsua", 16));
                if (sua.getIcon() == null) sua.setText("Sửa");
                add(sua);
            }
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
            setBackground(selected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    public static String tien(double value) {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(value) + " đ";
    }

    public static double soTien(String text, String tenTruong) {
        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value) || value < 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(tenTruong + " phải là số không âm (ví dụ: 1500000).");
        }
    }

    public static LocalDate ngay(String text, String tenTruong) {
        if (text.trim().isEmpty()) return null;
        try { return LocalDate.parse(text.trim(), NGAY); }
        catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException(tenTruong + " phải đúng định dạng dd/MM/yyyy.");
        }
    }

    public static void loi(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Kiểm tra thông tin", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean xacNhan(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void themDong(JPanel form, String label, JComponent control) {
        int row = form.getComponentCount() / 2;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(3, 5, 3, 12);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setPreferredSize(new Dimension(240, 32));
        form.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        control.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (control instanceof JTextField || control instanceof JComboBox) control.setPreferredSize(new Dimension(320, 32));
        form.add(control, gbc);
    }

    public static void moPanel(JPanel panel, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(1300, 680);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
