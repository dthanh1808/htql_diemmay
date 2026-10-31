package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class UI_KhachHang extends JPanel {

    private final Color bgMain = new Color(245, 247, 251);
    private final Color textDark = new Color(30, 41, 59);
    private final Color textGray = new Color(100, 116, 139);
    private final Color colorBlue = new Color(37, 99, 235);
    private final Color borderColor = new Color(226, 232, 240);
    private final Color selectionColor = new Color(239, 246, 255);

    private class RoundedPanel extends JPanel {
        RoundedPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(14, 14, 14, 14));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.setColor(borderColor);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    // FlowLayout có tính chiều cao khi thanh công cụ xuống hàng ở cửa sổ hẹp.
    private static class WrapLayout extends FlowLayout {
        WrapLayout() { super(FlowLayout.LEFT, 0, 6); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            synchronized (target.getTreeLock()) {
                int width = target.getWidth();
                Container parent = target.getParent();
                while (width <= 0 && parent != null) {
                    width = parent.getWidth();
                    parent = parent.getParent();
                }
                Insets insets = target.getInsets();
                int available = width > 0 ? width - insets.left - insets.right : Integer.MAX_VALUE;
                int rowWidth = 0, rowHeight = 0, maxWidth = 0, height = 0;
                for (Component child : target.getComponents()) {
                    if (!child.isVisible()) continue;
                    Dimension size = child.getPreferredSize();
                    if (rowWidth > 0 && rowWidth + size.width > available) {
                        maxWidth = Math.max(maxWidth, rowWidth);
                        height += rowHeight + getVgap();
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    rowWidth += size.width;
                    rowHeight = Math.max(rowHeight, size.height);
                }
                return new Dimension(Math.max(maxWidth, rowWidth) + insets.left + insets.right,
                        height + rowHeight + insets.top + insets.bottom + 2 * getVgap());
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return new Dimension(0, preferredLayoutSize(target).height);
        }
    }

    private Icon loadIcon(String path, Color tint, int size) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) return null;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(new ImageIcon(url).getImage(), 0, 0, size, size, null);
        if (tint != null) {
            g.setComposite(AlphaComposite.SrcIn);
            g.setColor(tint);
            g.fillRect(0, 0, size, size);
        }
        g.dispose();
        return new ImageIcon(image);
    }

    // Chỉ vẽ giao diện nút; chưa gắn xử lý nghiệp vụ.
    private JButton createButton(String text, String iconPath, int style) {
        Color foreground = style == 1 ? Color.WHITE : style == 2 ? new Color(185, 28, 28) : textDark;
        Color fill = style == 1 ? colorBlue : style == 2 ? new Color(254, 242, 242) : Color.WHITE;
        Color outline = style == 1 ? colorBlue : style == 2 ? new Color(252, 165, 165) : borderColor;
        JButton button = new JButton(text) {
            @Override
            public Dimension getPreferredSize() {
                Dimension preferred = super.getPreferredSize();
                if (isPreferredSizeSet() || getFont() == null) return preferred;
                FontMetrics metrics = getFontMetrics(getFont());
                Insets insets = getInsets();
                Icon icon = getIcon();
                String label = getText() == null ? "" : getText();
                int iconWidth = icon == null ? 0 : icon.getIconWidth();
                int gap = icon == null || label.isEmpty() ? 0 : getIconTextGap();
                int width = metrics.stringWidth(label) + iconWidth + gap + insets.left + insets.right + 18;
                int height = Math.max(metrics.getHeight(), icon == null ? 0 : icon.getIconHeight())
                        + insets.top + insets.bottom + 10;
                return new Dimension(Math.max(preferred.width, width), Math.max(34, height));
            }

            @Override
            public Dimension getMinimumSize() { return getPreferredSize(); }

            @Override
            public Dimension getMaximumSize() { return getPreferredSize(); }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color background = fill;
                if (!isEnabled()) background = new Color(248, 250, 252);
                else if (getModel().isPressed()) background = style == 1 ? new Color(29, 78, 216)
                        : style == 2 ? new Color(254, 202, 202) : new Color(241, 245, 249);
                else if (getModel().isRollover()) background = style == 1 ? new Color(29, 78, 216)
                        : style == 2 ? new Color(254, 226, 226) : new Color(248, 250, 252);
                g.setColor(background);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g.dispose();
                super.paintComponent(graphics);
            }

            @Override
            protected void paintBorder(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isEnabled() ? outline : borderColor);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (isFocusOwner()) {
                    g.setColor(style == 2 ? new Color(185, 28, 28) : colorBlue);
                    g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 6, 6);
                }
                g.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(fill);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(0, 12, 0, 12));
        if (iconPath != null) {
            button.setIcon(loadIcon(iconPath, foreground, 15));
            button.setIconTextGap(6);
        }
        return button;
    }

    private JTextField createInput(String hint) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setColor(new Color(148, 163, 184));
                    g.setFont(getFont());
                    Insets insets = getInsets();
                    g.clipRect(insets.left, 0, Math.max(0, getWidth() - insets.left - insets.right), getHeight());
                    FontMetrics metrics = g.getFontMetrics();
                    g.drawString(hint, insets.left, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
                    g.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(textDark);
        field.setBackground(Color.WHITE);
        field.setCaretColor(colorBlue);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), new EmptyBorder(0, 10, 0, 10)));
        field.getAccessibleContext().setAccessibleName(hint);
        return field;
    }

    private JPanel createSearch(String hint) {
        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setBackground(Color.WHITE);
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), new EmptyBorder(0, 10, 0, 10)));
        search.add(new JLabel(loadIcon("/icon/ic_timkiem.png", textGray, 16)), BorderLayout.WEST);
        JTextField input = createInput(hint);
        input.setBorder(null);
        search.add(input, BorderLayout.CENTER);
        return search;
    }

    private JComboBox<String> createCombo(String... values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(textDark);
        combo.setBackground(Color.WHITE);
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(textGray);
                        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int x = getWidth() / 2, y = getHeight() / 2;
                        g.drawPolyline(new int[]{x - 4, x, x + 4}, new int[]{y - 2, y + 2, y - 2}, 3);
                        g.dispose();
                    }
                };
                arrow.setPreferredSize(new Dimension(26, 26));
                arrow.setBorder(BorderFactory.createEmptyBorder());
                arrow.setContentAreaFilled(false);
                arrow.setFocusable(false);
                return arrow;
            }
        });
        combo.setBorder(BorderFactory.createLineBorder(borderColor));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return combo;
    }

    private JPanel createFilterItem(String title, JComponent input, int width) {
        boolean buttonGroup = input.getLayout() instanceof GridLayout || input.getLayout() instanceof BoxLayout;
        JPanel item = new JPanel(new BorderLayout(0, 6)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension preferred = super.getPreferredSize();
                if (!buttonGroup || getComponentCount() == 0) return preferred;
                Dimension controls = input.getMinimumSize();
                int height = controls.height + getComponent(0).getPreferredSize().height + 6;
                return new Dimension(Math.max(width, controls.width), Math.max(56, height));
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(buttonGroup ? input.getMinimumSize().width : 0, getPreferredSize().height);
            }
        };
        item.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(textDark);
        label.setLabelFor(input);
        int minimumWidth = buttonGroup ? input.getMinimumSize().width : 0;
        input.setPreferredSize(new Dimension(Math.max(width, minimumWidth), 34));
        item.add(label, BorderLayout.NORTH);
        item.add(input, BorderLayout.CENTER);
        item.setPreferredSize(new Dimension(Math.max(width, minimumWidth), 56));
        return item;
    }

    private void addFilter(JPanel panel, JComponent input, String title, int x, int y, int span, int width) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = span;
        constraints.weightx = span;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(y == 0 ? 0 : 10, 0, 0, x + span == 4 ? 0 : 12);
        panel.add(createFilterItem(title, input, width), constraints);
    }

    private JPanel group(JComponent... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        for (JComponent component : components) panel.add(component);
        panel.setBorder(new EmptyBorder(0, 0, 0, 12));
        return panel;
    }

    private JPanel createHeader(String title, String count, JPanel... actionGroups) {
        JPanel header = new JPanel(new WrapLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 3));
        labels.setOpaque(false);
        labels.setBorder(new EmptyBorder(0, 0, 0, 18));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(textDark);
        JLabel summary = new JLabel(count);
        summary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summary.setForeground(textGray);
        labels.add(heading);
        labels.add(summary);
        header.add(labels);
        for (JPanel actions : actionGroups) {
            for (Component action : actions.getComponents()) {
                JPanel slot = new JPanel(new BorderLayout());
                slot.setOpaque(false);
                slot.setBorder(new EmptyBorder(0, 0, 0, 8));
                slot.add(action, BorderLayout.CENTER);
                header.add(slot);
            }
        }
        header.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) { header.revalidate(); }
        });
        return header;
    }

    private Color rowBackground(int row, boolean selected) {
        return selected ? selectionColor : row % 2 == 0 ? Color.WHITE : new Color(250, 251, 253);
    }

    private class TwoLineRenderer extends JPanel implements TableCellRenderer {
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();

        TwoLineRenderer(boolean product) {
            super(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(6, 10, 6, 10));
            JPanel labels = new JPanel(new GridLayout(2, 1, 0, 2));
            labels.setOpaque(false);
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            labels.add(title);
            labels.add(subtitle);
            add(labels, BorderLayout.CENTER);
            if (product) {
                JLabel thumbnail = new JLabel(loadIcon("/icon/ic_sanpham.png", textGray, 24));
                thumbnail.setOpaque(true);
                thumbnail.setBackground(new Color(241, 245, 249));
                thumbnail.setHorizontalAlignment(SwingConstants.CENTER);
                thumbnail.setPreferredSize(new Dimension(36, 36));
                add(thumbnail, BorderLayout.WEST);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            String[] lines = (String[]) value;
            title.setText(lines[0]);
            subtitle.setText(lines[1]);
            getAccessibleContext().setAccessibleName(lines[0] + ", " + lines[1]);
            title.setForeground(textDark);
            title.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 13));
            subtitle.setForeground(textGray);
            setBackground(rowBackground(row, selected));
            return this;
        }
    }

    // Vẽ nhãn bo góc bằng Swing thay vì CSS mà JLabel không hỗ trợ đầy đủ.
    private class BadgeRenderer extends JPanel implements TableCellRenderer {
        private String text = "";
        private Color fill, foreground;

        BadgeRenderer() { setFont(new Font("Segoe UI", Font.PLAIN, 12)); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            text = value.toString();
            if (text.startsWith("Ngừng")) {
                fill = new Color(255, 247, 237); foreground = new Color(154, 52, 18);
            } else if (text.equals("Diamond")) {
                fill = new Color(245, 243, 255); foreground = new Color(109, 40, 217);
            } else if (text.equals("Gold")) {
                fill = new Color(255, 251, 235); foreground = new Color(146, 64, 14);
            } else if (text.equals("Silver")) {
                fill = new Color(239, 246, 255); foreground = new Color(29, 78, 216);
            } else if (text.equals("Member")) {
                fill = new Color(241, 245, 249); foreground = textGray;
            } else {
                fill = new Color(236, 253, 245); foreground = new Color(4, 120, 87);
            }
            setBackground(rowBackground(row, selected));
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(getFont());
            FontMetrics metrics = g.getFontMetrics();
            int width = Math.min(getWidth() - 8, metrics.stringWidth(text) + 18);
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - 26) / 2;
            g.setColor(fill);
            g.fillRoundRect(x, y, width, 26, 12, 12);
            g.setColor(foreground);
            g.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2, y + (26 - metrics.getHeight()) / 2 + metrics.getAscent());
            g.dispose();
        }
    }

    private class ActionRenderer extends JPanel implements TableCellRenderer {
        ActionRenderer(String... actions) {
            super(new GridBagLayout());
            JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            links.setOpaque(false);
            for (String action : actions) {
                JLabel label = new JLabel(action);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setForeground(colorBlue);
                links.add(label);
            }
            add(links);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            setBackground(rowBackground(row, selected));
            return this;
        }
    }

    private JTable createTable(Object[][] data, String[] columns, int[] widths, int moneyColumn) {
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getParent() != null && getPreferredSize().width <= getParent().getWidth();
            }
        };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(52);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(textDark);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(selectionColor);
        table.setSelectionForeground(textDark);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(238, 242, 247));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, column);
                int modelColumn = t.convertColumnIndexToModel(column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setBackground(rowBackground(row, selected));
                setForeground(textDark);
                setHorizontalAlignment(modelColumn == moneyColumn ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setFont(new Font("Segoe UI", modelColumn == moneyColumn ? Font.BOLD : Font.PLAIN, 12));
                return this;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBackground(new Color(241, 245, 249));
                setForeground(new Color(71, 85, 105));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor), new EmptyBorder(0, 10, 0, 10)));
                String heading = value.toString();
                boolean centered = heading.equals("Trạng thái") || heading.equals("Thao tác") || heading.contains("thành viên");
                setHorizontalAlignment(column == moneyColumn ? SwingConstants.RIGHT
                        : centered ? SwingConstants.CENTER : SwingConstants.LEFT);
                return this;
            }
        });
        return table;
    }

    private JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(table.getRowHeight());
        return scrollPane;
    }

    private JPanel createFooter(int count, String unit) {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor), new EmptyBorder(12, 0, 0, 0)));
        JLabel info = new JLabel("Hiển thị 1–" + count + " trong " + count + " " + unit);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(textGray);
        footer.add(info, BorderLayout.WEST);
        JPanel pages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pages.setOpaque(false);
        JButton previous = createButton("‹", null, 0);
        JButton current = createButton("1", null, 1);
        JButton next = createButton("›", null, 0);
        previous.getAccessibleContext().setAccessibleName("Trang trước");
        current.getAccessibleContext().setAccessibleName("Trang hiện tại: 1");
        next.getAccessibleContext().setAccessibleName("Trang sau");
        previous.setEnabled(false);
        next.setEnabled(false);
        previous.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        next.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        for (JButton button : new JButton[]{previous, current, next}) {
            button.setBorder(new EmptyBorder(0, 4, 0, 4));
            button.setPreferredSize(new Dimension(32, 30));
            if (!button.isEnabled()) button.setCursor(Cursor.getDefaultCursor());
            pages.add(button);
        }
        footer.add(pages, BorderLayout.EAST);
        return footer;
    }

    public UI_KhachHang() {
        setLayout(new BorderLayout(0, 12));
        setBackground(bgMain);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        RoundedPanel filter = new RoundedPanel();
        filter.setLayout(new GridBagLayout());
        addFilter(filter, createSearch("Nhập mã KH, họ tên, số điện thoại hoặc email"), "Tìm kiếm khách hàng", 0, 0, 2, 360);
        addFilter(filter, createCombo("Tất cả", "Diamond", "Gold", "Silver", "Member"),
                "Hạng thành viên", 2, 0, 1, 150);
        addFilter(filter, createCombo("Tất cả", "Đang hoạt động", "Ngừng hoạt động"),
                "Trạng thái", 3, 0, 1, 150);
        addFilter(filter, createInput("dd/MM/yyyy"), "Đăng ký từ ngày", 0, 1, 1, 174);
        addFilter(filter, createInput("dd/MM/yyyy"), "Đến ngày", 1, 1, 1, 174);
        JPanel filterActions = new JPanel();
        filterActions.setLayout(new BoxLayout(filterActions, BoxLayout.X_AXIS));
        filterActions.setOpaque(false);
        filterActions.add(Box.createHorizontalGlue());
        filterActions.add(createButton("Áp dụng bộ lọc", "/icon/ic_loc.png", 1));
        filterActions.add(Box.createHorizontalStrut(8));
        filterActions.add(createButton("Xóa bộ lọc", null, 2));
        addFilter(filter, filterActions, " ", 2, 1, 2, 360);
        add(filter, BorderLayout.NORTH);

        String[] columns = {"Khách hàng", "Liên hệ", "<html>Hạng<br>thành viên</html>", "Tổng chi tiêu", "Ngày đăng ký", "Trạng thái", "Thao tác"};
        // Mã và liên hệ vẫn hiển thị đầy đủ, chỉ gộp để tránh bảng quá nhiều cột.
        Object[][] data = {
            {new String[]{"Mai Thanh Lan", "KH000123"}, new String[]{"0903 458 789", "lanmai@gmail.com"}, "Diamond", "12.850.000 đ", "12/03/2024", "Đang hoạt động", ""},
            {new String[]{"Nguyễn Minh Tú", "KH000124"}, new String[]{"0987 224 510", "tuminh@gmail.com"}, "Gold", "3.860.000 đ", "15/03/2024", "Đang hoạt động", ""},
            {new String[]{"Phạm Quốc Huy", "KH000125"}, new String[]{"0902 615 437", "huypham@gmail.com"}, "Silver", "1.920.000 đ", "20/03/2024", "Đang hoạt động", ""},
            {new String[]{"Trần Hoài Nam", "KH000126"}, new String[]{"0948 118 228", "namtran@gmail.com"}, "Member", "460.000 đ", "22/03/2024", "Đang hoạt động", ""},
            {new String[]{"Hoàng Ngọc Liên", "KH000127"}, new String[]{"0905 820 354", "lienhoang@gmail.com"}, "Gold", "4.520.000 đ", "25/03/2024", "Ngừng hoạt động", ""},
            {new String[]{"Lê Quang Dũng", "KH000128"}, new String[]{"0869 741 982", "dungle@gmail.com"}, "Silver", "1.890.000 đ", "27/03/2024", "Đang hoạt động", ""},
            {new String[]{"Vũ Phương Anh", "KH000129"}, new String[]{"0906 204 165", "anhvu@gmail.com"}, "Member", "785.000 đ", "28/03/2024", "Đang hoạt động", ""},
            {new String[]{"Đỗ Trung Kiên", "KH000130"}, new String[]{"0913 507 903", "kiendo@gmail.com"}, "Silver", "2.060.000 đ", "30/03/2024", "Đang hoạt động", ""}
        };
        JTable table = createTable(data, columns, new int[]{175, 180, 95, 115, 108, 140, 135}, 3);
        table.getColumnModel().getColumn(0).setCellRenderer(new TwoLineRenderer(false));
        table.getColumnModel().getColumn(1).setCellRenderer(new TwoLineRenderer(false));
        table.getColumnModel().getColumn(2).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer("Xem", "Sửa", "Lịch sử"));

        RoundedPanel list = new RoundedPanel();
        list.setLayout(new BorderLayout());
        list.add(createHeader("Danh sách khách hàng", data.length + " khách hàng",
                group(createButton("Đăng ký khách hàng", "/icon/ic_them.png", 1),
                        createButton("Xuất file", "/icon/ic_xuatfile.png", 0)),
                group(createButton("Chính sách hạng thành viên", "/icon/ic_khachhang.png", 0),
                        createButton("Gửi khuyến mãi", "/icon/ic_khuyenmai.png", 0))), BorderLayout.NORTH);
        list.add(createScrollPane(table), BorderLayout.CENTER);
        list.add(createFooter(data.length, "khách hàng"), BorderLayout.SOUTH);
        add(list, BorderLayout.CENTER);
    }
}
