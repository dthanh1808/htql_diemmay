package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import dao.SanPhamDAO;
import dao.DanhMucDAO;
import entity.DanhMuc;
import entity.KhuyenMai;
import entity.SanPham;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class PNL_SanPham extends JPanel {

    private final SanPhamKhachHangWorker database = new SanPhamKhachHangWorker(this, e -> message(e.getMessage(), false));
    private int listRequest;
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();
    private final DanhMucDAO danhMucDAO = new DanhMucDAO();
    private final CardLayout viewLayout = new CardLayout();
    private final JPanel views = new JPanel(viewLayout);
    private final JLabel notice = new JLabel();
    private final JPanel noticePanel = new JPanel(new BorderLayout());
    private final Map<String, Icon> thumbnails = new HashMap<>();
    private BoLoc appliedFilter = new BoLoc();
    private JTable productTable;
    private DefaultTableModel productModel;
    private JTextField searchInput;
    private JComboBox<DanhMuc> categoryFilter;
    private JComboBox<String> brandFilter, priceFilter, warrantyFilter, statusFilter, sortInput;
    private JComboBox<Integer> pageSizeInput;
    private JLabel listCount, pageInfo, pageNumber;
    private JButton previousPage, nextPage;
    private final CardLayout resultsLayout = new CardLayout();
    private JPanel results;
    private int page = 1;

    private static class BoLoc {
        String tuKhoa = "";
        String danhMucId;
        String thuongHieu;
        int khoangGia;
        Integer baoHanhThang;
        Boolean dangKinhDoanh;
    }

    private final Color bgMain = new Color(245, 247, 251);
    private final Color textDark = new Color(30, 41, 59);
    private final Color textGray = new Color(100, 116, 139);
    private final Color colorBlue = new Color(37, 99, 235);
    private final Color borderColor = new Color(226, 232, 240);
    private final Color selectionColor = new Color(239, 246, 255);

    public PNL_SanPham() {
        initialiseShell();
        initialiseList();
    }

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
        JPanel item = new JPanel(new BorderLayout(0, 6));
        item.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(textDark);
        input.setPreferredSize(new Dimension(width, 34));
        item.add(label, BorderLayout.NORTH);
        item.add(input, BorderLayout.CENTER);
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
        for (JPanel actions : actionGroups) header.add(actions);
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
            String[] lines;
            if (value instanceof SanPham product) {
                lines = new String[]{product.getTenSP(), "SKU: " + product.getMaSKU()};
                if (thumbnail != null) {
                    Icon icon = thumbnails.get(product.getMaSKU());
                    if (icon == null) {
                        icon = productImage(product, 32);
                        thumbnails.put(product.getMaSKU(), icon);
                    }
                    thumbnail.setIcon(icon);
                }
            } else {
                lines = (String[]) value;
            }
            title.setText(lines[0]);
            subtitle.setText(lines[1]);
            title.setForeground(textDark);
            subtitle.setForeground(textGray);
            setBackground(rowBackground(row, selected));
            return this;
        }
    }

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
            public boolean isCellEditable(int row, int column) { return "Thao tác".equals(getColumnName(column)); }
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
                setHorizontalAlignment(modelColumn == moneyColumn ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    private JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
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
        notice.setText("<html>" + (text == null ? "" : text).replace("\n", "<br>") + "</html>");
        notice.setForeground(success ? new Color(4, 120, 87) : new Color(153, 27, 27));
        noticePanel.setBackground(success ? new Color(236, 253, 245) : new Color(254, 242, 242));
        noticePanel.setVisible(true);
        revalidate();
    }

    private void clearMessage() { noticePanel.setVisible(false); }

    private String money(BigDecimal price) { return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(price) + " đ"; }
    private String warranty(int months) { return months == 0 ? "Không bảo hành" : months + " tháng"; }

    private void perform(Runnable action) {
        try { action.run(); }
        catch (RuntimeException e) { message(e.getMessage(), false); }
    }

    private void initialiseList() {
        JPanel listView = new JPanel(new BorderLayout(0, 12));
        listView.setOpaque(false);
        RoundedPanel filter = new RoundedPanel();
        filter.setLayout(new GridBagLayout());
        JPanel search = createSearch("Nhập tên, SKU, thương hiệu hoặc danh mục");
        searchInput = (JTextField) ((BorderLayout) search.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        categoryFilter = createCombo(new DanhMuc(null, "Tất cả", ""));
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
        JButton apply = createButton("Áp dụng", null, 1);
        JButton reset = createButton("Xóa lọc", null, 2);
        apply.addActionListener(e -> applyFilters());
        reset.addActionListener(e -> resetFilters());
        filterActions.add(apply); filterActions.add(reset);
        addFilter(filter, filterActions, " ", 3, 1, 1, 260);
        listView.add(filter, BorderLayout.NORTH);

        String[] columns = {"Sản phẩm", "Danh mục", "Thương hiệu", "Giá bán", "Bảo hành", "Tồn kho", "Trạng thái", "Thao tác"};
        productTable = createTable(new Object[0][8], columns, new int[]{270, 100, 100, 115, 100, 80, 130, 145}, 3);
        productModel = (DefaultTableModel) productTable.getModel();
        productTable.getColumnModel().getColumn(0).setCellRenderer(new TwoLineRenderer(true));
        productTable.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer());
        productTable.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer("Xem", "Sửa"));
        productTable.getColumnModel().getColumn(7).setCellEditor(new ProductActionEditor());

        JButton add = createButton("Thêm sản phẩm", "/icon/ic_them.png", 1);
        add.addActionListener(e -> showEditor(null));
        JButton categories = createButton("Danh mục", "/icon/ic_danhmuc.png", 0);
        categories.addActionListener(e -> showCategories(null));
        JButton reload = createButton("Làm mới", null, 0);
        reload.addActionListener(e -> perform(() -> {
            thumbnails.clear(); refreshChoices(); refreshList();
        }));
        sortInput = createCombo("Tên A–Z", "Tên Z–A", "Giá tăng dần", "Giá giảm dần", "SKU A–Z");
        sortInput.addActionListener(e -> { page = 1; refreshList(); });
        JPanel heading = createHeader("Danh sách sản phẩm", "", group(add, categories, reload), group(new JLabel("Sắp xếp:"), sortInput));
        listCount = (JLabel) ((JPanel) heading.getComponent(0)).getComponent(1);
        RoundedPanel list = new RoundedPanel();
        list.setLayout(new BorderLayout());
        list.add(heading, BorderLayout.NORTH);
        results = new JPanel(resultsLayout);
        results.setOpaque(false);
        results.add(createScrollPane(productTable), "table");
        JPanel empty = new JPanel(new GridBagLayout());
        empty.add(new JLabel("Không tìm thấy sản phẩm."));
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
        private String maSKU;
        ProductActionEditor() {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            buttons.setOpaque(false);
            JButton view = createButton("Xem", null, 0), edit = createButton("Sửa", null, 0);
            view.addActionListener(e -> { String chosen = maSKU; fireEditingStopped(); showDetails(chosen); });
            edit.addActionListener(e -> { String chosen = maSKU; fireEditingStopped(); showEditor(chosen); });
            buttons.add(view); buttons.add(edit);
            panel.add(buttons);
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            maSKU = productAt(row).getMaSKU(); panel.setBackground(selectionColor); return panel;
        }
    }

    private JPanel createListFooter() {
        JPanel footer = new JPanel(new WrapLayout());
        footer.setOpaque(false);
        pageInfo = new JLabel();
        pageSizeInput = createCombo(10, 25, 50);
        pageSizeInput.addActionListener(e -> { page = 1; refreshList(); });
        previousPage = createButton("Trang trước", null, 0);
        nextPage = createButton("Trang sau", null, 0);
        pageNumber = new JLabel();
        previousPage.addActionListener(e -> { page--; refreshList(); });
        nextPage.addActionListener(e -> { page++; refreshList(); });
        footer.add(group(pageInfo)); footer.add(group(new JLabel("Số dòng:"), pageSizeInput));
        footer.add(group(previousPage, pageNumber, nextPage));
        return footer;
    }

    private record Choices(ArrayList<DanhMuc> categories, ArrayList<String> brands, ArrayList<Integer> warranties) { }
    private void refreshChoices() {
        database.run(() -> new Choices(danhMucDAO.layTatCa(), sanPhamDAO.layThuongHieu(), sanPhamDAO.layBaoHanh()), choices -> {
            String category = appliedFilter.danhMucId;
            categoryFilter.removeAllItems();
            categoryFilter.addItem(new DanhMuc(null, "Tất cả", ""));
            for (DanhMuc c : choices.categories()) categoryFilter.addItem(c);
            selectCategory(categoryFilter, category);
            
            brandFilter.removeAllItems(); brandFilter.addItem("Tất cả");
            for (String value : choices.brands()) brandFilter.addItem(value);
            
            warrantyFilter.removeAllItems(); warrantyFilter.addItem("Tất cả");
            for (int value : choices.warranties()) warrantyFilter.addItem(warranty(value));
            
            appliedFilter = readFilters();
        });
    }

    private void selectCategory(JComboBox<DanhMuc> combo, String maDanhMuc) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (java.util.Objects.equals(combo.getItemAt(i).getMaDanhMuc(), maDanhMuc)) { combo.setSelectedIndex(i); return; }
        }
    }

    private BoLoc readFilters() {
        BoLoc loc = new BoLoc();
        DanhMuc dm = (DanhMuc) categoryFilter.getSelectedItem();
        loc.tuKhoa = searchInput.getText().trim();
        loc.danhMucId = dm == null ? null : dm.getMaDanhMuc();
        if (brandFilter.getSelectedIndex() > 0) loc.thuongHieu = (String) brandFilter.getSelectedItem();
        loc.khoangGia = priceFilter.getSelectedIndex();
        if (statusFilter.getSelectedIndex() > 0) loc.dangKinhDoanh = statusFilter.getSelectedIndex() == 1;
        return loc;
    }

    private void applyFilters() { appliedFilter = readFilters(); page = 1; refreshList(); }
    private void resetFilters() { appliedFilter = new BoLoc(); page = 1; refreshChoices(); refreshList(); }

    private record ProductRows(ArrayList<SanPham> rows, int total) { }
    private void refreshList() {
        if (productTable == null || pageSizeInput == null) return;
        if (productTable.isEditing()) productTable.getCellEditor().cancelCellEditing();
        BoLoc loc = appliedFilter;
        int order = sortInput.getSelectedIndex();
        int request = ++listRequest;
        database.run(() -> {
            ArrayList<SanPham> all = sanPhamDAO.layTatCa();
            ArrayList<SanPham> rows = sanPhamDAO.timKiem(all, loc.tuKhoa, loc.danhMucId, loc.thuongHieu,
                    loc.khoangGia, loc.baoHanhThang, loc.dangKinhDoanh);
            sanPhamDAO.sapXep(rows, order);
            return new ProductRows(rows, all.size());
        }, snapshot -> {
            if (request != listRequest) return;
            ArrayList<SanPham> ketQua = snapshot.rows();
            int size = (Integer) pageSizeInput.getSelectedItem();
            int soTrang = Math.max(1, (ketQua.size() + size - 1) / size);
            page = Math.max(1, Math.min(page, soTrang));
            int batDau = (page - 1) * size;
            int ketThuc = Math.min(batDau + size, ketQua.size());
            productModel.setRowCount(0);
            for (int i = batDau; i < ketThuc; i++) {
                SanPham sp = ketQua.get(i);
                productModel.addRow(new Object[]{sp, sp.getTenDanhMuc(), sp.getThuongHieu(),
                        money(sp.getDonGia()), warranty(sp.getThoiGianBaoHanh()), sp.getSlTon(),
                        sp.isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh", ""});
            }
            listCount.setText(ketQua.size() + " kết quả / " + snapshot.total() + " sản phẩm");
            pageInfo.setText("Hiển thị " + (ketQua.isEmpty() ? 0 : batDau + 1) + "–" + ketThuc + " trong " + ketQua.size() + " kết quả");
            pageNumber.setText("Trang " + page + " / " + soTrang);
            previousPage.setEnabled(page > 1);
            nextPage.setEnabled(page < soTrang);
            resultsLayout.show(results, ketQua.isEmpty() ? "empty" : "table");
        });
    }

    private void showList() { clearMessage(); refreshList(); viewLayout.show(views, "list"); }

    private Icon productImage(SanPham product, int size) {
        if (product.getHinhAnh() == null || product.getHinhAnh().isEmpty()) 
            return loadIcon("/icon/ic_sanpham.png", textGray, Math.min(size, 48));
        try {
            ImageIcon original = new ImageIcon(Base64.getDecoder().decode(product.getHinhAnh()));
            double ratio = Math.min((double) size / original.getIconWidth(), (double) size / original.getIconHeight());
            return new ImageIcon(original.getImage().getScaledInstance(
                    Math.max(1, (int) (original.getIconWidth() * ratio)),
                    Math.max(1, (int) (original.getIconHeight() * ratio)), Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return loadIcon("/icon/ic_sanpham.png", textGray, Math.min(size, 48));
        }
    }

    private JPanel pageBody(String title, String subtitle, JPanel body, JButton... buttons) {
        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.add(createHeader(title, subtitle, group(buttons)), BorderLayout.NORTH);
        JPanel topAligned = new JPanel(new BorderLayout()); 
        topAligned.add(body, BorderLayout.NORTH);
        topAligned.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(topAligned);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void formItem(JPanel form, String label, JComponent component, int x, int y, int span, int height) {
        JPanel item = new JPanel(new BorderLayout(0, 6));
        item.setOpaque(false);
        JLabel heading = new JLabel(label);
        heading.setForeground(textDark); heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        item.add(heading, BorderLayout.NORTH); item.add(component, BorderLayout.CENTER);
        component.setPreferredSize(new Dimension(span == 2 ? 600 : 290, height));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x; constraints.gridy = y; constraints.gridwidth = span; constraints.weightx = span;
        constraints.fill = GridBagConstraints.BOTH; constraints.insets = new Insets(0, 0, 14, x + span == 2 ? 0 : 14);
        form.add(item, constraints);
    }

    private JTextArea textArea(String value, boolean editable) {
        JTextArea text = new JTextArea(value);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setLineWrap(true); text.setWrapStyleWord(true); text.setEditable(editable);
        text.setBorder(new EmptyBorder(8, 10, 8, 10));
        return text;
    }

    private void showDetails(String maSKU) {
        database.run(() -> sanPhamDAO.timTheoMa(maSKU), p -> renderDetails(maSKU, p));
    }

    private void renderDetails(String maSKU, SanPham p) {
        if (p == null) { showList(); message("Sản phẩm không còn tồn tại.", false); return; }
        clearMessage();
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        detailItem(form, "Tên sản phẩm", p.getTenSP(), 0, 0, 2);
        detailItem(form, "SKU", p.getMaSKU(), 0, 1, 1);
        detailItem(form, "Danh mục", p.getTenDanhMuc(), 1, 1, 1);
        detailItem(form, "Mã vạch", p.getMaVach(), 0, 2, 1);
        detailItem(form, "Model", p.getModel(), 1, 2, 1);
        detailItem(form, "Thương hiệu", p.getThuongHieu(), 0, 3, 1);
        detailItem(form, "Giá bán", money(p.getDonGia()), 1, 3, 1);
        detailItem(form, "Bảo hành", warranty(p.getThoiGianBaoHanh()), 0, 4, 1);
        detailItem(form, "Khuyến mãi", p.getTenKhuyenMai(), 1, 4, 1);
        detailItem(form, "Trạng thái", p.isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh", 0, 5, 1);
        detailItem(form, "Nhóm sản phẩm", p.getNhomSP(), 1, 5, 1);
        detailItem(form, "Tồn kho", Integer.toString(p.getSlTon()), 0, 6, 1);
        String moTa = p.getMoTa();
        formItem(form, "Mô tả / thông số kỹ thuật", textArea(moTa == null || moTa.isBlank() ? "Chưa có mô tả." : moTa, false), 0, 7, 2, 110);
        
        JButton back = createButton("Về danh sách", null, 0);
        JButton edit = createButton("Chỉnh sửa", "/icon/ic_chinhsua.png", 1);
        JButton receive = createButton("Nhập kho", null, 0);
        back.addActionListener(e -> showList()); edit.addActionListener(e -> showEditor(maSKU));
        receive.addActionListener(e -> perform(() -> receiveStock(maSKU)));
        install("detail", pageBody("Chi tiết sản phẩm", p.getMaSKU(), form, back, edit, receive));
    }

    private void receiveStock(String sku) {
        String input = JOptionPane.showInputDialog(this, "Số lượng nhập thêm cho SKU " + sku + ":", "Nhập kho", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        String normalized = input.trim();
        if (!normalized.matches("[0-9]{1,10}")) throw new IllegalArgumentException("Số lượng phải là số nguyên dương.");
        int quantity = Integer.parseInt(normalized);
        database.run(() -> sanPhamDAO.nhapKho(sku, quantity), updated -> {
            renderDetails(updated.getMaSKU(), updated);
            message("Đã nhập kho thành công.", true);
        });
    }

    private void detailItem(JPanel form, String label, String value, int x, int y, int span) {
        JTextField field = createInput(""); field.setText(value); field.setEditable(false);
        formItem(form, label, field, x, y, span, 34);
    }

    private record ProductFormData(SanPham product, ArrayList<DanhMuc> categories, ArrayList<KhuyenMai> promotions) { }
    private void showEditor(String maSKU) {
        database.run(() -> new ProductFormData(sanPhamDAO.timTheoMa(maSKU), danhMucDAO.layTatCa(), sanPhamDAO.layDanhSachKhuyenMai()), data -> {
            if (data.categories().isEmpty()) { showCategories(null); message("Hãy tạo danh mục trước khi thêm sản phẩm.", false); return; }
            if (maSKU != null && data.product() == null) { message("Sản phẩm không còn tồn tại.", false); return; }
            clearMessage();
            install("editor", new SanPhamForm(data));
        });
    }

    private class SanPhamForm extends JPanel {
        private final SanPham banGoc;
        private final JTextField sku = createInput("Ví dụ: TL002");
        private final JTextField maVach = createInput("Mã vạch (không bắt buộc)");
        private final JTextField ten = createInput("Tên sản phẩm");
        private final JTextField model = createInput("Model sản phẩm");
        private final JTextField thuongHieu = createInput("Tên thương hiệu");
        private final JTextField gia = createInput("Số nguyên VND");
        private final JComboBox<DanhMuc> danhMuc = createCombo(new DanhMuc[0]);
        private final JComboBox<KhuyenMai> khuyenMai = createCombo(new KhuyenMai[0]);
        private final JComboBox<String> nhomSP = createCombo("Gọn nhẹ", "Cồng kềnh");
        private final JSpinner baoHanh = new JSpinner(new SpinnerNumberModel(12, 0, 120, 1));
        private final JSpinner tonKho = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        private final JComboBox<String> trangThai = createCombo("Đang kinh doanh", "Ngừng kinh doanh");
        private final JTextArea moTa = textArea("", true);
        private ArrayList<String> noiDungBanDau;

        SanPhamForm(ProductFormData data) {
            super(new BorderLayout());
            setOpaque(false);
            banGoc = data.product();
            for (DanhMuc dm : data.categories()) danhMuc.addItem(dm);
            khuyenMai.addItem(null); 
            for (KhuyenMai km : data.promotions()) khuyenMai.addItem(km);
            khuyenMai.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    setText(value instanceof KhuyenMai km ? km.getMaKM() + " · " + km.getTenKM() : "Không áp dụng");
                    return this;
                }
            });
            sku.setEditable(banGoc == null);
            sku.setName("product.form.sku");
            ten.setName("product.form.name");
            thuongHieu.setName("product.form.brand");
            gia.setName("product.form.price");
            tonKho.setName("product.form.stock");
            gia.setHorizontalAlignment(SwingConstants.RIGHT);
            napDuLieu();

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            formItem(form, "SKU * (duy nhất, không đổi sau khi tạo)", sku, 0, 0, 1, 34);
            formItem(form, "Tên sản phẩm *", ten, 1, 0, 1, 34);
            formItem(form, "Mã vạch", maVach, 0, 1, 1, 34);
            formItem(form, "Model", model, 1, 1, 1, 34);
            formItem(form, "Danh mục *", danhMuc, 0, 2, 1, 34);
            formItem(form, "Khuyến mãi", khuyenMai, 1, 2, 1, 34);
            formItem(form, "Thương hiệu *", thuongHieu, 0, 3, 1, 34);
            formItem(form, "Giá bán * (VND)", gia, 1, 3, 1, 34);
            formItem(form, "Bảo hành (tháng)", baoHanh, 0, 4, 1, 34);
            formItem(form, "Nhóm sản phẩm *", nhomSP, 1, 4, 1, 34);
            formItem(form, "Trạng thái", trangThai, 0, 5, 1, 34);
            formItem(form, banGoc == null ? "Tồn kho ban đầu" : "Tồn kho (sửa bằng Nhập kho)", tonKho, 1, 5, 1, 34);
            formItem(form, "Mô tả", new JScrollPane(moTa), 0, 7, 2, 110);

            JButton huy = createButton("Hủy", null, 0);
            JButton luu = createButton("Lưu sản phẩm", null, 1);
            huy.addActionListener(e -> quayLai());
            luu.addActionListener(e -> perform(() -> luu()));
            
            ArrayList<JButton> nut = new ArrayList<>();
            nut.add(huy); nut.add(luu);
            if (banGoc != null) {
                JButton xoa = createButton("Xóa sản phẩm", "/icon/ic_xoa.png", 2);
                xoa.setEnabled(!banGoc.isTrangThai() && banGoc.getSlTon() == 0);
                xoa.addActionListener(e -> perform(() -> {
                    database.run(() -> { sanPhamDAO.xoa(banGoc.getMaSKU()); return true; }, ignored -> showList());
                }));
                nut.add(xoa);
            }
            add(pageBody(banGoc == null ? "Thêm sản phẩm" : "Sửa sản phẩm", "", form, nut.toArray(new JButton[0])), BorderLayout.CENTER);
            noiDungBanDau = layNoiDung();
        }

        private void napDuLieu() {
            if (banGoc == null) return;
            sku.setText(banGoc.getMaSKU());
            tonKho.setValue(banGoc.getSlTon());
            tonKho.setEnabled(false);
            maVach.setText(banGoc.getMaVach());
            ten.setText(banGoc.getTenSP());
            model.setText(banGoc.getModel());
            thuongHieu.setText(banGoc.getThuongHieu());
            gia.setText(banGoc.getDonGia().toPlainString());
            selectCategory(danhMuc, banGoc.getMaDanhMuc());
            for (int i = 0; i < khuyenMai.getItemCount(); i++) {
                KhuyenMai item = khuyenMai.getItemAt(i);
                if (item == null ? banGoc.getMaKM() == null : java.util.Objects.equals(item.getMaKM(), banGoc.getMaKM())) {
                    khuyenMai.setSelectedIndex(i); break;
                }
            }
            nhomSP.setSelectedItem(banGoc.getNhomSP());
            baoHanh.setValue(banGoc.getThoiGianBaoHanh());
            trangThai.setSelectedIndex(banGoc.isTrangThai() ? 0 : 1);
            moTa.setText(banGoc.getMoTa());
        }

        private ArrayList<String> layNoiDung() {
            ArrayList<String> noiDung = new ArrayList<>();
            noiDung.add(sku.getText()); noiDung.add(ten.getText()); noiDung.add(gia.getText());
            return noiDung;
        }

        private void luu() {
            try {
                baoHanh.commitEdit();
                if (banGoc == null) tonKho.commitEdit();
            } catch (java.text.ParseException e) {
                throw new IllegalArgumentException("Bảo hành hoặc tồn kho không hợp lệ. Vui lòng nhập số nguyên trong giới hạn.", e);
            }
            String giaNhap = gia.getText().trim();
            if (!giaNhap.matches("[0-9]{1,18}")) {
                throw new IllegalArgumentException("Giá bán phải là số nguyên VND, tối đa 18 chữ số.");
            }
            SanPham sp = banGoc == null ? new SanPham() : new SanPham(banGoc);
            if (banGoc != null) sp.setMaSKU(banGoc.getMaSKU());
            else sp.setMaSKU(sku.getText());
            
            sp.setMaVach(maVach.getText().trim().isEmpty() ? null : maVach.getText().trim());
            sp.setTenSP(ten.getText());
            sp.setModel(model.getText());
            DanhMuc dm = (DanhMuc) danhMuc.getSelectedItem();
            sp.setDanhMuc(dm);
            KhuyenMai km = (KhuyenMai) khuyenMai.getSelectedItem();
            sp.setKhuyenMai(km);
            sp.setThuongHieu(thuongHieu.getText());
            sp.setDonGia(new BigDecimal(giaNhap));
            sp.setThoiGianBaoHanh(((Number) baoHanh.getValue()).intValue());
            sp.setTrangThai(trangThai.getSelectedIndex() == 0);
            sp.setMoTa(moTa.getText());
            sp.setNhomSP((String) nhomSP.getSelectedItem());
            if (banGoc == null) sp.setSlTon(((Number) tonKho.getValue()).intValue());

            database.run(() -> banGoc == null ? sanPhamDAO.them(sp) : sanPhamDAO.capNhat(sp), daLuu -> {
                refreshChoices();
                renderDetails(daLuu.getMaSKU(), daLuu);
                message("Đã lưu sản phẩm " + daLuu.getMaSKU() + ".", true);
            });
        }

        private void quayLai() {
            if (banGoc == null) showList();
            else showDetails(banGoc.getMaSKU());
        }
    }

    private void showCategories(String selectedId) {
        clearMessage();
        database.run(() -> {
            ArrayList<DanhMuc> categories = danhMucDAO.layTatCa();
            Map<String, Integer> counts = danhMucDAO.demTheoDanhMuc();
            return new CategoryData(categories, counts);
        }, data -> install("categories", new DanhMucPanel(selectedId, data)));
    }

    private record CategoryData(ArrayList<DanhMuc> categories, Map<String, Integer> counts) { }
    private class DanhMucPanel extends JPanel {
        private final ArrayList<DanhMuc> danhSach;
        private final Map<String, Integer> counts;
        private final JTable table;
        private final JTextField ten = createInput("Tên danh mục");
        private final JTextArea moTa = textArea("", true);
        private String maDangSua;

        DanhMucPanel(String selectedId, CategoryData data) {
            super(new BorderLayout());
            danhSach = data.categories(); counts = data.counts();
            setOpaque(false);
            Object[][] rows = new Object[danhSach.size()][3];
            for (int i = 0; i < danhSach.size(); i++) {
                DanhMuc dm = danhSach.get(i);
                rows[i] = new Object[]{dm.getTenDanhMuc(), counts.getOrDefault(dm.getMaDanhMuc(), 0), dm.getMoTa()};
            }
            table = createTable(rows, new String[]{"Danh mục", "Số SP", "Mô tả"}, new int[]{145, 80, 150}, 1);
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                    DanhMuc dm = danhSach.get(table.convertRowIndexToModel(table.getSelectedRow()));
                    maDangSua = dm.getMaDanhMuc();
                    ten.setText(dm.getTenDanhMuc());
                    moTa.setText(dm.getMoTa());
                }
            });

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            formItem(form, "Tên danh mục *", ten, 0, 1, 2, 34);
            formItem(form, "Mô tả", new JScrollPane(moTa), 0, 2, 2, 110);
            
            JButton luu = createButton("Lưu", null, 1);
            luu.addActionListener(e -> perform(() -> {
                DanhMuc dm = new DanhMuc(maDangSua, ten.getText(), moTa.getText());
                database.run(() -> maDangSua == null ? danhMucDAO.themDanhMuc(dm) : danhMucDAO.capNhatDanhMuc(dm), daLuu -> {
                    refreshChoices(); showCategories(daLuu.getMaDanhMuc());
                });
            }));
            
            JButton xoa = createButton("Xóa", null, 2);
            xoa.addActionListener(e -> perform(() -> {
                if (maDangSua != null) database.run(() -> { danhMucDAO.xoa(maDangSua); return true; }, ignored -> showCategories(null));
            }));
            
            JPanel actions = new JPanel(new WrapLayout());
            actions.setOpaque(false);
            actions.add(group(luu, xoa));
            formItem(form, " ", actions, 0, 3, 2, 50);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createScrollPane(table), form);
            split.setBorder(BorderFactory.createEmptyBorder());
            split.setResizeWeight(0.5);

            JButton back = createButton("Về DS sản phẩm", null, 0);
            back.addActionListener(e -> showList());
            
            RoundedPanel panel = new RoundedPanel();
            panel.setLayout(new BorderLayout(0, 12));
            panel.add(createHeader("Quản lý danh mục", "", group(back)), BorderLayout.NORTH);
            panel.add(split, BorderLayout.CENTER);
            add(panel, BorderLayout.CENTER);
        }
    }
}
