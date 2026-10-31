package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import dao.XmlKhoSanPham;
import entity.DanhMuc;
import entity.SanPham;
import service.BoLocSanPham;
import service.SanPhamService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class UI_SanPham extends JPanel {

    private SanPhamService service;
    private final CardLayout viewLayout = new CardLayout();
    private final JPanel views = new JPanel(viewLayout);
    private final JLabel notice = new JLabel();
    private final JPanel noticePanel = new JPanel(new BorderLayout());
    private final Map<UUID, Icon> thumbnails = new HashMap<>();
    private BoLocSanPham appliedFilter = BoLocSanPham.tatCa();
    private SanPhamService.Trang currentPage;
    private JTable productTable;
    private DefaultTableModel productModel;
    private JTextField searchInput;
    private JComboBox<CategoryChoice> categoryFilter;
    private JComboBox<String> brandFilter, priceFilter, warrantyFilter, statusFilter, sortInput;
    private JComboBox<Integer> pageSizeInput;
    private JLabel listCount, pageInfo, pageNumber;
    private JButton previousPage, nextPage;
    private final CardLayout resultsLayout = new CardLayout();
    private JPanel results;
    private int page = 1;

    private record CategoryChoice(UUID id, String name) {
        @Override public String toString() { return name; }
    }

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

    // Các nút tự đo chữ để không bị cắt nhãn khi đổi cỡ chữ hoặc DPI.
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

    @SafeVarargs
    private final <T> JComboBox<T> createCombo(T... values) {
        JComboBox<T> combo = new JComboBox<>(values);
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
        constraints.weightx = x == 3 && span == 1 ? 0 : span;
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
            if (java.util.Arrays.stream(actions.getComponents()).anyMatch(c -> !(c instanceof JButton))) {
                header.add(actions);
                continue;
            }
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
        private JLabel thumbnail;

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
                thumbnail = new JLabel(loadIcon("/icon/ic_sanpham.png", textGray, 24));
                thumbnail.setOpaque(true);
                thumbnail.setBackground(new Color(241, 245, 249));
                thumbnail.setHorizontalAlignment(SwingConstants.CENTER);
                thumbnail.setPreferredSize(new Dimension(36, 36));
                add(thumbnail, BorderLayout.WEST);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            String[] lines = value instanceof SanPham product ? new String[]{product.ten(), "SKU: " + product.sku()} : (String[]) value;
            if (thumbnail != null && value instanceof SanPham product) {
                thumbnail.setIcon(thumbnails.computeIfAbsent(product.id(), id -> productImage(product, 32)));
            }
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
        JTable table = new JTable(new DefaultTableModel(data, columns)) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 6; }

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

public UI_SanPham() {
        initialiseShell();
        try {
            service = new SanPhamService(XmlKhoSanPham.macDinh());
            initialiseList();
        } catch (IOException e) { showStartupError(e); }
    }

    public UI_SanPham(SanPhamService service) {
        this.service = java.util.Objects.requireNonNull(service);
        initialiseShell();
        initialiseList();
    }

    private void initialiseShell() {
        setLayout(new BorderLayout(0, 10));
        setBackground(bgMain);
        setBorder(new EmptyBorder(14, 18, 14, 18));
        views.setOpaque(false);
        noticePanel.setBorder(new EmptyBorder(10, 12, 10, 12));
        notice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noticePanel.add(notice);
        noticePanel.setVisible(false);
        add(noticePanel, BorderLayout.NORTH);
        add(views, BorderLayout.CENTER);
    }

    private void showStartupError(IOException error) {
        JPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 16));
        JLabel info = new JLabel("<html><b>Không mở được danh sách sản phẩm</b><br>"
                + html(error.getMessage()) + "<br>Ứng dụng không tự xóa hoặc đặt lại file dữ liệu.</html>");
        info.setForeground(textDark);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(info, BorderLayout.NORTH);
        JButton retry = createButton("Thử tải lại dữ liệu", null, 1);
        retry.addActionListener(e -> {
            try {
                service = new SanPhamService(XmlKhoSanPham.macDinh());
                views.removeAll();
                initialiseList();
            } catch (IOException failure) { showStartupError(failure); }
        });
        panel.add(group(retry), BorderLayout.SOUTH);
        install("startup", panel);
    }

    private void install(String name, JPanel panel) {
        for (Component old : views.getComponents()) {
            if (name.equals(old.getName())) views.remove(old);
        }
        panel.setName(name);
        views.add(panel, name);
        viewLayout.show(views, name);
        views.revalidate();
        views.repaint();
    }

    private void message(String text, boolean success) {
        notice.setText("<html>" + html(text) + "</html>");
        notice.setForeground(success ? new Color(4, 120, 87) : new Color(153, 27, 27));
        noticePanel.setBackground(success ? new Color(236, 253, 245) : new Color(254, 242, 242));
        noticePanel.setVisible(true);
        revalidate();
    }

    private void clearMessage() { noticePanel.setVisible(false); }
    private String html(String text) {
        return (text == null ? "" : text).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("\n", "<br>");
    }
    private String money(BigDecimal price) { return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(price) + " đ"; }
    private String warranty(int months) { return months == 0 ? "Không bảo hành" : months + " tháng"; }

    @FunctionalInterface private interface IOAction { void run() throws IOException; }
    private void perform(IOAction action) {
        try { action.run(); }
        catch (IllegalArgumentException | IOException e) { message(e.getMessage(), false); }
    }

    private void initialiseList() {
        JPanel listView = new JPanel(new BorderLayout(0, 12));
        listView.setOpaque(false);
        RoundedPanel filter = new RoundedPanel();
        filter.setLayout(new GridBagLayout());
        JPanel search = createSearch("Nhập tên, SKU, thương hiệu hoặc danh mục");
        searchInput = (JTextField) ((BorderLayout) search.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        searchInput.setName("product.search");
        categoryFilter = createCombo(new CategoryChoice(null, "Tất cả"));
        brandFilter = createCombo("Tất cả");
        priceFilter = createCombo("Tất cả", "Dưới 5 triệu", "Từ 5 đến 15 triệu", "Trên 15 triệu");
        warrantyFilter = createCombo("Tất cả");
        statusFilter = createCombo("Tất cả", "Đang kinh doanh", "Ngừng kinh doanh");
        addFilter(filter, search, "Tìm kiếm sản phẩm", 0, 0, 2, 360);
        addFilter(filter, categoryFilter, "Danh mục", 2, 0, 1, 150);
        addFilter(filter, brandFilter, "Thương hiệu", 3, 0, 1, 150);
        addFilter(filter, priceFilter, "Khoảng giá bán", 0, 1, 1, 150);
        addFilter(filter, warrantyFilter, "Bảo hành", 1, 1, 1, 150);
        addFilter(filter, statusFilter, "Trạng thái", 2, 1, 1, 150);
        JPanel filterActions = new JPanel(new GridLayout(1, 2, 8, 0));
        filterActions.setOpaque(false);
        JButton apply = createButton("Áp dụng bộ lọc", null, 1);
        JButton reset = createButton("Xóa bộ lọc", null, 2);
        apply.addActionListener(e -> applyFilters());
        searchInput.addActionListener(e -> applyFilters());
        reset.addActionListener(e -> resetFilters());
        filterActions.add(apply); filterActions.add(reset);
        addFilter(filter, filterActions, " ", 3, 1, 1, 260);
        listView.add(filter, BorderLayout.NORTH);

        String[] columns = {"Sản phẩm", "Danh mục", "Thương hiệu", "Giá bán", "Bảo hành", "Trạng thái", "Thao tác"};
        productTable = createTable(new Object[0][7], columns, new int[]{290, 100, 100, 115, 110, 145, 145}, 3);
        productTable.setName("product.table");
        productModel = (DefaultTableModel) productTable.getModel();
        productTable.getColumnModel().getColumn(0).setCellRenderer(new TwoLineRenderer(true));
        productTable.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());
        productTable.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer("Xem", "Sửa"));
        productTable.getColumnModel().getColumn(6).setCellEditor(new ProductActionEditor());
        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = productTable.rowAtPoint(e.getPoint());
                int column = productTable.columnAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && row >= 0 && column != 6) showDetails(productAt(row).id());
            }
        });
        productTable.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "viewProduct");
        productTable.getActionMap().put("viewProduct", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (productTable.getSelectedRow() >= 0) showDetails(productAt(productTable.getSelectedRow()).id());
            }
        });
        productTable.getInputMap().put(KeyStroke.getKeyStroke("F2"), "editProduct");
        productTable.getActionMap().put("editProduct", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (productTable.getSelectedRow() >= 0) showEditor(productAt(productTable.getSelectedRow()).id());
            }
        });

        JButton add = createButton("Thêm sản phẩm", "/icon/ic_them.png", 1);
        add.addActionListener(e -> showEditor(null));
        JButton categories = createButton("Quản lý danh mục", "/icon/ic_danhmuc.png", 0);
        categories.addActionListener(e -> showCategories(null));
        JButton reload = createButton("Tải lại dữ liệu", null, 0);
        reload.addActionListener(e -> perform(() -> {
            service.taiLai(); thumbnails.clear(); refreshChoices(); refreshList();
            message("Đã tải lại dữ liệu từ file.", true);
        }));
        sortInput = createCombo("Tên A–Z", "Tên Z–A", "Giá tăng dần", "Giá giảm dần", "Mới thêm");
        sortInput.setName("product.sort");
        sortInput.setPreferredSize(new Dimension(138, 34));
        sortInput.addActionListener(e -> { page = 1; refreshList(); });
        JLabel sortLabel = new JLabel("Sắp xếp:");
        sortLabel.setLabelFor(sortInput);
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel heading = createHeader("Danh sách sản phẩm", "", group(add, categories, reload), group(sortLabel, sortInput));
        listCount = (JLabel) ((JPanel) heading.getComponent(0)).getComponent(1);
        RoundedPanel list = new RoundedPanel();
        list.setLayout(new BorderLayout());
        list.add(heading, BorderLayout.NORTH);
        results = new JPanel(resultsLayout);
        results.setOpaque(false);
        results.add(createScrollPane(productTable), "table");
        JPanel empty = new JPanel(new GridBagLayout());
        empty.setBackground(Color.WHITE);
        JLabel noResults = new JLabel("<html><center>Không tìm thấy sản phẩm phù hợp.<br>Hãy đổi từ khóa hoặc xóa bộ lọc.</center></html>");
        noResults.setForeground(textGray);
        noResults.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        empty.add(noResults);
        results.add(empty, "empty");
        list.add(results, BorderLayout.CENTER);
        list.add(createListFooter(), BorderLayout.SOUTH);
        listView.add(list, BorderLayout.CENTER);
        refreshChoices();
        install("list", listView);
        refreshList();
    }

    private SanPham productAt(int viewRow) {
        return (SanPham) productModel.getValueAt(productTable.convertRowIndexToModel(viewRow), 0);
    }

    private class ProductActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private UUID id;
        ProductActionEditor() {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            buttons.setOpaque(false);
            JButton view = createButton("Xem", null, 0), edit = createButton("Sửa", null, 0);
            for (JButton button : new JButton[]{view, edit}) {
                button.setBorder(new EmptyBorder(0, 5, 0, 5));
                buttons.add(button);
            }
            view.addActionListener(e -> { UUID chosen = id; fireEditingStopped(); showDetails(chosen); });
            edit.addActionListener(e -> { UUID chosen = id; fireEditingStopped(); showEditor(chosen); });
            panel.add(buttons);
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            id = productAt(row).id(); panel.setBackground(selectionColor); return panel;
        }
    }

    private JPanel createListFooter() {
        JPanel footer = new JPanel(new WrapLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor), new EmptyBorder(8, 0, 0, 0)));
        pageInfo = new JLabel();
        pageInfo.setForeground(textGray); pageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageSizeInput = createCombo(10, 25, 50);
        pageSizeInput.setPreferredSize(new Dimension(68, 30));
        pageSizeInput.addActionListener(e -> { page = 1; refreshList(); });
        JLabel sizeLabel = new JLabel("Số dòng:");
        sizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previousPage = createButton("Trang trước", null, 0);
        nextPage = createButton("Trang sau", null, 0);
        pageNumber = new JLabel();
        pageNumber.setFont(new Font("Segoe UI", Font.BOLD, 12));
        previousPage.addActionListener(e -> { page--; refreshList(); });
        nextPage.addActionListener(e -> { page++; refreshList(); });
        footer.add(group(pageInfo)); footer.add(group(sizeLabel, pageSizeInput));
        footer.add(group(previousPage, pageNumber, nextPage));
        return footer;
    }

    private void refreshChoices() {
        UUID category = appliedFilter.danhMucId();
        String brand = appliedFilter.thuongHieu();
        categoryFilter.removeAllItems();
        categoryFilter.addItem(new CategoryChoice(null, "Tất cả"));
        for (DanhMuc c : service.danhMuc()) categoryFilter.addItem(new CategoryChoice(c.id(), c.ten()));
        selectCategory(categoryFilter, category);
        brandFilter.removeAllItems(); brandFilter.addItem("Tất cả");
        for (String value : service.thuongHieu()) brandFilter.addItem(value);
        if (brand != null && service.thuongHieu().contains(brand)) brandFilter.setSelectedItem(brand);
        warrantyFilter.removeAllItems(); warrantyFilter.addItem("Tất cả");
        List<Integer> months = new ArrayList<>(service.baoHanh());
        for (int value : new int[]{0, 12, 24, 36}) if (!months.contains(value)) months.add(value);
        months.sort(Integer::compareTo);
        for (int value : months) warrantyFilter.addItem(warranty(value));
        if (appliedFilter.baoHanhThang() != null) warrantyFilter.setSelectedItem(warranty(appliedFilter.baoHanhThang()));
        searchInput.setText(appliedFilter.tuKhoa());
        priceFilter.setSelectedIndex(appliedFilter.khoangGia().ordinal());
        statusFilter.setSelectedIndex(appliedFilter.dangKinhDoanh() == null ? 0 : appliedFilter.dangKinhDoanh() ? 1 : 2);
        appliedFilter = readFilters();
    }

    private void selectCategory(JComboBox<CategoryChoice> combo, UUID id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (java.util.Objects.equals(combo.getItemAt(i).id(), id)) { combo.setSelectedIndex(i); return; }
        }
        if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
    }

    private BoLocSanPham readFilters() {
        CategoryChoice category = (CategoryChoice) categoryFilter.getSelectedItem();
        Integer months = null;
        if (warrantyFilter.getSelectedIndex() > 0) {
            String selected = (String) warrantyFilter.getSelectedItem();
            months = selected.equals("Không bảo hành") ? 0 : Integer.parseInt(selected.split(" ")[0]);
        }
        return new BoLocSanPham(searchInput.getText().strip(), category == null ? null : category.id(),
                brandFilter.getSelectedIndex() <= 0 ? null : (String) brandFilter.getSelectedItem(),
                BoLocSanPham.KhoangGia.values()[priceFilter.getSelectedIndex()], months,
                statusFilter.getSelectedIndex() == 0 ? null : statusFilter.getSelectedIndex() == 1);
    }

    private void applyFilters() { appliedFilter = readFilters(); page = 1; clearMessage(); refreshList(); }
    private void resetFilters() { appliedFilter = BoLocSanPham.tatCa(); page = 1; refreshChoices(); clearMessage(); refreshList(); }

    private void refreshList() {
        if (productTable == null || pageSizeInput == null) return;
        if (productTable.isEditing()) productTable.getCellEditor().cancelCellEditing();
        currentPage = service.traCuu(appliedFilter, SanPhamService.SapXep.values()[sortInput.getSelectedIndex()],
                page, (Integer) pageSizeInput.getSelectedItem());
        page = currentPage.trang();
        productModel.setRowCount(0);
        for (SanPham p : currentPage.sanPham()) {
            productModel.addRow(new Object[]{p, service.tenDanhMuc(p.danhMucId()), p.thuongHieu(), money(p.giaBan()),
                    warranty(p.baoHanhThang()), p.dangKinhDoanh() ? "Đang kinh doanh" : "Ngừng kinh doanh", ""});
        }
        listCount.setText(currentPage.tongKetQua() + " kết quả / " + currentPage.tongSanPham() + " sản phẩm");
        int size = (Integer) pageSizeInput.getSelectedItem();
        int start = currentPage.tongKetQua() == 0 ? 0 : (page - 1) * size + 1;
        int end = (page - 1) * size + currentPage.sanPham().size();
        pageInfo.setText("Hiển thị " + start + "–" + end + " trong " + currentPage.tongKetQua() + " kết quả");
        pageNumber.setText("Trang " + page + " / " + currentPage.soTrang());
        previousPage.setEnabled(page > 1); nextPage.setEnabled(page < currentPage.soTrang());
        resultsLayout.show(results, currentPage.sanPham().isEmpty() ? "empty" : "table");
        revalidate(); repaint();
    }

    private void showList() { clearMessage(); refreshList(); viewLayout.show(views, "list"); }

    private Icon productImage(SanPham product, int size) {
        if (product.anhBase64().isEmpty()) return loadIcon("/icon/ic_sanpham.png", textGray, Math.min(size, 48));
        ImageIcon original = new ImageIcon(Base64.getDecoder().decode(product.anhBase64()));
        double ratio = Math.min((double) size / original.getIconWidth(), (double) size / original.getIconHeight());
        return new ImageIcon(original.getImage().getScaledInstance(Math.max(1, (int) (original.getIconWidth() * ratio)),
                Math.max(1, (int) (original.getIconHeight() * ratio)), Image.SCALE_SMOOTH));
    }

    private static class WidthTrackingPanel extends JPanel implements Scrollable {
        WidthTrackingPanel(JPanel content) { super(new BorderLayout()); add(content, BorderLayout.NORTH); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return Math.max(24, visible.height - 24); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private JPanel pageBody(String title, String subtitle, JPanel body, JButton... buttons) {
        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.add(createHeader(title, subtitle, group(buttons)), BorderLayout.NORTH);
        JPanel topAligned = new WidthTrackingPanel(body);
        topAligned.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(topAligned);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void formItem(JPanel form, String label, JComponent component, int x, int y, int span, int height) {
        JPanel item = new JPanel(new BorderLayout(0, 6));
        item.setOpaque(false);
        JLabel heading = new JLabel(label);
        heading.setForeground(textDark); heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heading.setLabelFor(component);
        item.add(heading, BorderLayout.NORTH); item.add(component, BorderLayout.CENTER);
        component.setPreferredSize(new Dimension(span == 2 ? 600 : 290, height));
        item.setMinimumSize(new Dimension(0, height + 22));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x; constraints.gridy = y; constraints.gridwidth = span; constraints.weightx = span;
        constraints.fill = GridBagConstraints.BOTH; constraints.anchor = GridBagConstraints.NORTH;
        constraints.insets = new Insets(0, 0, 14, x + span == 2 ? 0 : 14);
        form.add(item, constraints);
    }

    private JTextArea textArea(String value, boolean editable) {
        JTextArea text = new JTextArea(value);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(textDark); text.setBackground(Color.WHITE);
        text.setLineWrap(true); text.setWrapStyleWord(true); text.setEditable(editable);
        text.setBorder(new EmptyBorder(8, 10, 8, 10));
        return text;
    }

    private void showDetails(UUID id) {
        SanPham p = service.tim(id).orElse(null);
        if (p == null) { showList(); message("Sản phẩm không còn tồn tại.", false); return; }
        clearMessage();
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        JLabel image = new JLabel(productImage(p, 160)); image.setHorizontalAlignment(SwingConstants.CENTER);
        formItem(form, "Hình ảnh", image, 0, 0, 2, 170);
        detailItem(form, "Tên sản phẩm", p.ten(), 0, 1, 2);
        detailItem(form, "SKU", p.sku(), 0, 2, 1);
        detailItem(form, "Danh mục", service.tenDanhMuc(p.danhMucId()), 1, 2, 1);
        detailItem(form, "Thương hiệu", p.thuongHieu(), 0, 3, 1);
        detailItem(form, "Giá bán", money(p.giaBan()), 1, 3, 1);
        detailItem(form, "Bảo hành", warranty(p.baoHanhThang()), 0, 4, 1);
        detailItem(form, "Trạng thái", p.dangKinhDoanh() ? "Đang kinh doanh" : "Ngừng kinh doanh", 1, 4, 1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        detailItem(form, "Ngày tạo", format.format(p.taoLuc()), 0, 5, 1);
        detailItem(form, "Cập nhật lần cuối", format.format(p.capNhatLuc()), 1, 5, 1);
        formItem(form, "Mô tả / thông số kỹ thuật", textArea(p.moTa().isBlank() ? "Chưa có mô tả." : p.moTa(), false), 0, 6, 2, 110);
        JButton back = createButton("Về danh sách", null, 0), edit = createButton("Chỉnh sửa", "/icon/ic_chinhsua.png", 1);
        back.addActionListener(e -> showList()); edit.addActionListener(e -> showEditor(id));
        install("detail", pageBody("Chi tiết sản phẩm", p.sku(), form, back, edit));
    }

    private void detailItem(JPanel form, String label, String value, int x, int y, int span) {
        JTextField field = createInput(""); field.setText(value); field.setEditable(false);
        formItem(form, label, field, x, y, span, 34);
    }

    private void showEditor(UUID id) {
        if (service.danhMuc().isEmpty()) {
            showCategories(null); message("Hãy tạo ít nhất một danh mục trước khi thêm sản phẩm.", false); return;
        }
        SanPham original = id == null ? null : service.tim(id).orElse(null);
        if (id != null && original == null) { showList(); message("Sản phẩm không còn tồn tại.", false); return; }
        clearMessage();
        JTextField sku = createInput("Ví dụ: TL002"), name = createInput("Tên sản phẩm, tối đa 150 ký tự"),
                brand = createInput("Tên thương hiệu"), price = createInput("Số nguyên VND, ví dụ 12000000");
        sku.setName("product.form.sku"); name.setName("product.form.name"); brand.setName("product.form.brand");
        price.setName("product.form.price");
        price.setHorizontalAlignment(SwingConstants.RIGHT);
        CategoryChoice[] choices = service.danhMuc().stream().map(c -> new CategoryChoice(c.id(), c.ten())).toArray(CategoryChoice[]::new);
        JComboBox<CategoryChoice> category = createCombo(choices);
        category.setName("product.form.category");
        JSpinner months = new JSpinner(new SpinnerNumberModel(12, 0, 120, 1));
        months.setName("product.form.warranty"); months.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComboBox<String> state = createCombo("Đang kinh doanh", "Ngừng kinh doanh");
        state.setName("product.form.status");
        JTextArea description = textArea("", true);
        description.setName("product.form.description");
        String[] imageValue = {original == null ? "" : original.anhBase64()};
        JLabel imagePreview = new JLabel(original == null ? loadIcon("/icon/ic_sanpham.png", textGray, 48) : productImage(original, 100));
        imagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        if (original != null) {
            sku.setText(original.sku()); name.setText(original.ten()); brand.setText(original.thuongHieu());
            price.setText(original.giaBan().toPlainString()); months.setValue(original.baoHanhThang());
            selectCategory(category, original.danhMucId()); state.setSelectedIndex(original.dangKinhDoanh() ? 0 : 1);
            description.setText(original.moTa());
        }
        JButton chooseImage = createButton("Chọn ảnh PNG / JPEG", null, 0), removeImage = createButton("Bỏ ảnh", null, 2);
        chooseImage.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh PNG, JPEG (tối đa 2 MB)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) perform(() -> {
                imageValue[0] = SanPhamService.docAnh(chooser.getSelectedFile().toPath());
                ImageIcon icon = new ImageIcon(Base64.getDecoder().decode(imageValue[0]));
                double ratio = Math.min(100.0 / icon.getIconWidth(), 100.0 / icon.getIconHeight());
                imagePreview.setIcon(new ImageIcon(icon.getImage().getScaledInstance(Math.max(1, (int) (icon.getIconWidth() * ratio)),
                        Math.max(1, (int) (icon.getIconHeight() * ratio)), Image.SCALE_SMOOTH)));
                clearMessage();
            });
        });
        removeImage.addActionListener(e -> { imageValue[0] = ""; imagePreview.setIcon(loadIcon("/icon/ic_sanpham.png", textGray, 48)); });
        JPanel imagePanel = new JPanel(new BorderLayout(8, 0)); imagePanel.setOpaque(false);
        imagePanel.add(imagePreview, BorderLayout.CENTER); imagePanel.add(group(chooseImage, removeImage), BorderLayout.SOUTH);
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        formItem(form, "Tên sản phẩm *", name, 0, 0, 2, 34);
        formItem(form, "SKU * (duy nhất, tối đa 40 ký tự)", sku, 0, 1, 1, 34);
        formItem(form, "Danh mục *", category, 1, 1, 1, 34);
        formItem(form, "Thương hiệu *", brand, 0, 2, 1, 34);
        formItem(form, "Giá bán * (VND, không nhập dấu phân cách)", price, 1, 2, 1, 34);
        formItem(form, "Bảo hành (0–120 tháng; 0 là không bảo hành)", months, 0, 3, 1, 34);
        formItem(form, "Trạng thái kinh doanh", state, 1, 3, 1, 34);
        formItem(form, "Mô tả / thông số kỹ thuật (tối đa 3.000 ký tự)", new JScrollPane(description), 0, 4, 2, 110);
        formItem(form, "Hình ảnh (không bắt buộc, tối đa 2 MB và 16 triệu điểm ảnh)", imagePanel, 0, 5, 2, 150);
        java.util.function.Supplier<List<Object>> values = () -> List.of(sku.getText(), name.getText(), brand.getText(),
                price.getText(), category.getSelectedItem() == null ? "" : category.getSelectedItem(),
                ((JSpinner.DefaultEditor) months.getEditor()).getTextField().getText(), state.getSelectedIndex(),
                description.getText(), imageValue[0]);
        List<Object> initial = values.get();
        Runnable back = () -> { if (original == null) showList(); else showDetails(id); };
        JButton cancel = createButton("Hủy / Quay lại", null, 0), save = createButton("Lưu sản phẩm", null, 1);
        cancel.addActionListener(e -> {
            if (initial.equals(values.get())) back.run();
            else confirm("Bỏ các thay đổi chưa lưu?", "Những thông tin đang nhập sẽ không được lưu.", "Bỏ thay đổi",
                    back, () -> viewLayout.show(views, "editor"));
        });
        save.addActionListener(e -> perform(() -> {
            try { months.commitEdit(); }
            catch (java.text.ParseException failure) { throw new IllegalArgumentException("Bảo hành phải là số nguyên từ 0 đến 120 tháng."); }
            String amount = price.getText().strip();
            if (!amount.matches("[0-9]{1,12}")) throw new IllegalArgumentException("Giá bán phải là số nguyên VND, không có dấu phân cách, tối đa 12 chữ số.");
            CategoryChoice selected = (CategoryChoice) category.getSelectedItem();
            SanPhamService.NhapSanPham input = new SanPhamService.NhapSanPham(sku.getText(), name.getText(),
                    selected == null ? null : selected.id(), brand.getText(), new BigDecimal(amount),
                    ((Number) months.getValue()).intValue(), state.getSelectedIndex() == 0, description.getText(), imageValue[0]);
            SanPham saved = original == null ? service.them(input) : service.capNhat(id, input);
            thumbnails.remove(saved.id()); refreshChoices(); refreshList(); showDetails(saved.id());
            message(original == null ? "Đã thêm sản phẩm " + saved.sku() + "." : "Đã cập nhật sản phẩm " + saved.sku() + ".", true);
        }));
        List<JButton> buttons = new ArrayList<>(List.of(cancel, save));
        if (original != null) {
            JButton delete = createButton("Xóa vĩnh viễn", "/icon/ic_xoa.png", 2);
            delete.setEnabled(!original.dangKinhDoanh());
            delete.addActionListener(e -> confirm("Xóa sản phẩm " + original.sku() + "?",
                    "Sản phẩm sẽ bị xóa vĩnh viễn. Nếu chỉ tạm dừng bán, hãy giữ trạng thái Ngừng kinh doanh.",
                    "Xóa vĩnh viễn", () -> perform(() -> {
                        service.xoa(id); thumbnails.remove(id); refreshChoices(); showList(); message("Đã xóa sản phẩm " + original.sku() + ".", true);
                    }), () -> viewLayout.show(views, "editor")));
            buttons.add(delete);
            JLabel help = new JLabel("<html>Để xóa, hãy chọn <b>Ngừng kinh doanh</b> và lưu sản phẩm trước. Có thể kích hoạt lại bằng cách chỉnh sửa trạng thái.</html>");
            help.setForeground(textGray); help.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            formItem(form, "Lưu ý về trạng thái", help, 0, 6, 2, 42);
        }
        install("editor", pageBody(original == null ? "Thêm sản phẩm" : "Chỉnh sửa sản phẩm", "* Thông tin bắt buộc",
                form, buttons.toArray(JButton[]::new)));
        name.requestFocusInWindow();
    }

    private void confirm(String title, String description, String action, Runnable yes, Runnable no) {
        clearMessage();
        JTextArea info = textArea(description, false);
        info.setPreferredSize(new Dimension(600, 110));
        JButton accept = createButton(action, null, 2), cancel = createButton("Quay lại", null, 0);
        accept.addActionListener(e -> yes.run()); cancel.addActionListener(e -> no.run());
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false); body.add(info);
        install("confirm", pageBody(title, "Vui lòng kiểm tra trước khi tiếp tục", body, cancel, accept));
    }

    private void showCategories(UUID selectedId) {
        clearMessage();
        List<DanhMuc> categories = service.danhMuc();
        Object[][] rows = categories.stream().map(c -> new Object[]{c.ten(), service.soSanPham(c.id()), c.moTa()}).toArray(Object[][]::new);
        JTable table = createTable(rows, new String[]{"Danh mục", "Số sản phẩm", "Mô tả"}, new int[]{160, 100, 200}, 1);
        table.setName("category.table");
        table.setRowHeight(38); table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setSelectionBackground(selectionColor);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getColumnModel().getColumn(0).setPreferredWidth(145);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setShowVerticalLines(false); table.setGridColor(borderColor); table.setFillsViewportHeight(true);
        JTextField name = createInput("Tên danh mục, tối đa 80 ký tự");
        name.setName("category.form.name");
        JTextArea description = textArea("", true); description.setName("category.form.description");
        JLabel mode = new JLabel("Thêm danh mục"), usage = new JLabel("Danh mục mới chưa có sản phẩm.");
        mode.setFont(new Font("Segoe UI", Font.BOLD, 15));
        usage.setFont(new Font("Segoe UI", Font.PLAIN, 12)); usage.setForeground(textGray);
        UUID[] editId = {null};
        String[] initialName = {""}, initialDescription = {""};
        int[] previousSelection = {-1};
        boolean[] selecting = {false};
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        formItem(form, " ", mode, 0, 0, 2, 24);
        formItem(form, "Tên danh mục *", name, 0, 1, 2, 34);
        formItem(form, "Mô tả (tối đa 1.000 ký tự)", new JScrollPane(description), 0, 2, 2, 110);
        formItem(form, "Sử dụng", usage, 0, 3, 2, 42);
        JButton save = createButton("Lưu danh mục", null, 1), delete = createButton("Xóa danh mục", null, 2);
        delete.setEnabled(false);
        java.util.function.IntConsumer loadCategory = index -> {
            DanhMuc category = categories.get(table.convertRowIndexToModel(index));
            previousSelection[0] = index;
            editId[0] = category.id(); name.setText(category.ten()); description.setText(category.moTa());
            initialName[0] = category.ten(); initialDescription[0] = category.moTa();
            mode.setText("Chỉnh sửa danh mục");
            long count = service.soSanPham(category.id());
            usage.setText("<html>" + count + " sản phẩm. " + (count == 0 ? "Có thể xóa danh mục trống." : "Không thể xóa khi còn sản phẩm.") + "</html>");
            delete.setEnabled(count == 0);
        };
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || selecting[0] || table.getSelectedRow() < 0) return;
            int target = table.getSelectedRow();
            if (!name.getText().equals(initialName[0]) || !description.getText().equals(initialDescription[0])) {
                selecting[0] = true;
                if (previousSelection[0] < 0) table.clearSelection();
                else table.setRowSelectionInterval(previousSelection[0], previousSelection[0]);
                selecting[0] = false;
                confirm("Bỏ thay đổi danh mục?", "Thông tin đang nhập chưa được lưu. Bạn muốn chuyển sang danh mục khác?", "Bỏ thay đổi", () -> {
                    selecting[0] = true; table.setRowSelectionInterval(target, target); selecting[0] = false;
                    loadCategory.accept(target); viewLayout.show(views, "categories");
                }, () -> viewLayout.show(views, "categories"));
            } else loadCategory.accept(target);
        });
        save.addActionListener(e -> perform(() -> {
            DanhMuc saved = service.luuDanhMuc(editId[0], name.getText(), description.getText());
            refreshChoices(); refreshList(); showCategories(saved.id()); message("Đã lưu danh mục " + saved.ten() + ".", true);
        }));
        delete.addActionListener(e -> {
            UUID id = editId[0];
            if (id != null) confirm("Xóa danh mục?", "Chỉ xóa danh mục không chứa sản phẩm. Thao tác này không thể hoàn tác.", "Xóa danh mục",
                    () -> perform(() -> { service.xoaDanhMuc(id); refreshChoices(); refreshList(); showCategories(null); message("Đã xóa danh mục.", true); }),
                    () -> viewLayout.show(views, "categories"));
        });
        JPanel actions = new JPanel(new WrapLayout()); actions.setOpaque(false);
        actions.add(group(save, delete));
        formItem(form, " ", actions, 0, 4, 2, 84);
        JPanel right = new WidthTrackingPanel(form); right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(0, 14, 0, 0));
        JScrollPane formScroll = new JScrollPane(right);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getViewport().setBackground(Color.WHITE);
        formScroll.getVerticalScrollBar().setUnitIncrement(24);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createScrollPane(table), formScroll);
        split.setBorder(BorderFactory.createEmptyBorder()); split.setResizeWeight(0.53); split.setContinuousLayout(true);
        split.setPreferredSize(new Dimension(800, 420));
        java.util.function.BooleanSupplier dirty = () -> !name.getText().equals(initialName[0]) || !description.getText().equals(initialDescription[0]);
        JButton back = createButton("Về danh sách sản phẩm", null, 0), add = createButton("Thêm danh mục", "/icon/ic_them.png", 1);
        back.addActionListener(e -> {
            if (!dirty.getAsBoolean()) showList();
            else confirm("Bỏ thay đổi danh mục?", "Thông tin danh mục đang nhập chưa được lưu.", "Bỏ thay đổi",
                    this::showList, () -> viewLayout.show(views, "categories"));
        });
        add.addActionListener(e -> {
            Runnable clear = () -> { table.clearSelection(); previousSelection[0] = -1; editId[0] = null; name.setText(""); description.setText("");
                initialName[0] = ""; initialDescription[0] = ""; mode.setText("Thêm danh mục");
                usage.setText("Danh mục mới chưa có sản phẩm."); delete.setEnabled(false); name.requestFocusInWindow(); };
            if (!dirty.getAsBoolean()) clear.run();
            else confirm("Bỏ thay đổi danh mục?", "Thông tin đang nhập chưa được lưu.", "Bỏ thay đổi",
                    () -> { clear.run(); viewLayout.show(views, "categories"); }, () -> viewLayout.show(views, "categories"));
        });
        RoundedPanel panel = new RoundedPanel(); panel.setLayout(new BorderLayout(0, 12));
        panel.add(createHeader("Quản lý danh mục", categories.size() + " danh mục", group(back, add)), BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        install("categories", panel);
        if (selectedId != null) {
            for (int i = 0; i < categories.size(); i++) if (categories.get(i).id().equals(selectedId)) table.setRowSelectionInterval(i, i);
        }
    }
}
