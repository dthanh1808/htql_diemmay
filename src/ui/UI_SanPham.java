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
import dao.DuLieuTam;
import dao.SanPhamDAO;
import dao.DanhMucDAO;
import entity.DanhMuc;
import entity.SanPham;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class UI_SanPham extends JPanel {

    private final SanPhamDAO sanPhamDAO;
    private final DanhMucDAO danhMucDAO;
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
            boolean chiCoNut = true;
            for (Component component : actions.getComponents()) {
                if (!(component instanceof JButton)) chiCoNut = false;
            }
            if (!chiCoNut) {
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
            String[] lines;
            if (value instanceof SanPham) {
                SanPham product = (SanPham) value;
                lines = new String[]{product.getTen(), "SKU: " + product.getSku()};
                if (thumbnail != null) {
                    Icon icon = thumbnails.get(product.getId());
                    if (icon == null) {
                        icon = productImage(product, 32);
                        thumbnails.put(product.getId(), icon);
                    }
                    thumbnail.setIcon(icon);
                }
            } else lines = (String[]) value;
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

    public UI_SanPham() { this(DuLieuTam.getMacDinh()); }

    public UI_SanPham(DuLieuTam duLieu) {
        sanPhamDAO = new SanPhamDAO(duLieu);
        danhMucDAO = new DanhMucDAO(duLieu);
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

    private void perform(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException e) { message(e.getMessage(), false); }
    }

    private void initialiseList() {
        JPanel listView = new JPanel(new BorderLayout(0, 12));
        listView.setOpaque(false);
        RoundedPanel filter = new RoundedPanel();
        filter.setLayout(new GridBagLayout());
        JPanel search = createSearch("Nhập tên, SKU, thương hiệu hoặc danh mục");
        searchInput = (JTextField) ((BorderLayout) search.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        searchInput.setName("product.search");
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
                if (e.getClickCount() == 2 && row >= 0 && column != 6) showDetails(productAt(row).getId());
            }
        });
        productTable.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "viewProduct");
        productTable.getActionMap().put("viewProduct", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (productTable.getSelectedRow() >= 0) showDetails(productAt(productTable.getSelectedRow()).getId());
            }
        });
        productTable.getInputMap().put(KeyStroke.getKeyStroke("F2"), "editProduct");
        productTable.getActionMap().put("editProduct", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (productTable.getSelectedRow() >= 0) showEditor(productAt(productTable.getSelectedRow()).getId());
            }
        });

        JButton add = createButton("Thêm sản phẩm", "/icon/ic_them.png", 1);
        add.addActionListener(e -> showEditor(null));
        JButton categories = createButton("Quản lý danh mục", "/icon/ic_danhmuc.png", 0);
        categories.addActionListener(e -> showCategories(null));
        JButton reload = createButton("Làm mới danh sách", null, 0);
        reload.addActionListener(e -> perform(() -> {
            thumbnails.clear(); refreshChoices(); refreshList();
            message("Đã làm mới danh sách sản phẩm.", true);
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
        private String id;
        ProductActionEditor() {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            buttons.setOpaque(false);
            JButton view = createButton("Xem", null, 0), edit = createButton("Sửa", null, 0);
            for (JButton button : new JButton[]{view, edit}) {
                button.setBorder(new EmptyBorder(0, 5, 0, 5));
                buttons.add(button);
            }
            view.addActionListener(e -> { String chosen = id; fireEditingStopped(); showDetails(chosen); });
            edit.addActionListener(e -> { String chosen = id; fireEditingStopped(); showEditor(chosen); });
            panel.add(buttons);
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            id = productAt(row).getId(); panel.setBackground(selectionColor); return panel;
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
        String category = appliedFilter.danhMucId;
        String brand = appliedFilter.thuongHieu;
        categoryFilter.removeAllItems();
        categoryFilter.addItem(new DanhMuc(null, "Tất cả", ""));
        for (DanhMuc c : danhMucDAO.layTatCa()) categoryFilter.addItem(c);
        selectCategory(categoryFilter, category);
        brandFilter.removeAllItems(); brandFilter.addItem("Tất cả");
        for (String value : sanPhamDAO.layThuongHieu()) brandFilter.addItem(value);
        if (brand != null && sanPhamDAO.layThuongHieu().contains(brand)) brandFilter.setSelectedItem(brand);
        warrantyFilter.removeAllItems(); warrantyFilter.addItem("Tất cả");
        List<Integer> months = new ArrayList<>(sanPhamDAO.layBaoHanh());
        for (int value : new int[]{0, 12, 24, 36}) if (!months.contains(value)) months.add(value);
        java.util.Collections.sort(months);
        for (int value : months) warrantyFilter.addItem(warranty(value));
        if (appliedFilter.baoHanhThang != null) warrantyFilter.setSelectedItem(warranty(appliedFilter.baoHanhThang));
        searchInput.setText(appliedFilter.tuKhoa);
        priceFilter.setSelectedIndex(appliedFilter.khoangGia);
        statusFilter.setSelectedIndex(appliedFilter.dangKinhDoanh == null ? 0 : appliedFilter.dangKinhDoanh ? 1 : 2);
        appliedFilter = readFilters();
    }

    private void selectCategory(JComboBox<DanhMuc> combo, String id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (java.util.Objects.equals(combo.getItemAt(i).getId(), id)) { combo.setSelectedIndex(i); return; }
        }
        if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
    }

    private BoLoc readFilters() {
        BoLoc loc = new BoLoc();
        DanhMuc dm = (DanhMuc) categoryFilter.getSelectedItem();
        loc.tuKhoa = searchInput.getText().trim();
        loc.danhMucId = dm == null ? null : dm.getId();
        if (brandFilter.getSelectedIndex() > 0) loc.thuongHieu = (String) brandFilter.getSelectedItem();
        loc.khoangGia = priceFilter.getSelectedIndex();
        if (warrantyFilter.getSelectedIndex() > 0) {
            String giaTri = (String) warrantyFilter.getSelectedItem();
            loc.baoHanhThang = giaTri.equals("Không bảo hành") ? 0 : Integer.parseInt(giaTri.split(" ")[0]);
        }
        if (statusFilter.getSelectedIndex() > 0) loc.dangKinhDoanh = statusFilter.getSelectedIndex() == 1;
        return loc;
    }

    private void applyFilters() {
        appliedFilter = readFilters();
        page = 1;
        clearMessage();
        refreshList();
    }

    private void resetFilters() {
        appliedFilter = new BoLoc();
        page = 1;
        refreshChoices();
        clearMessage();
        refreshList();
    }

    private void refreshList() {
        if (productTable == null || pageSizeInput == null) return;
        if (productTable.isEditing()) productTable.getCellEditor().cancelCellEditing();
        ArrayList<SanPham> ketQua = sanPhamDAO.timKiem(appliedFilter.tuKhoa, appliedFilter.danhMucId,
                appliedFilter.thuongHieu, appliedFilter.khoangGia, appliedFilter.baoHanhThang, appliedFilter.dangKinhDoanh);
        sanPhamDAO.sapXep(ketQua, sortInput.getSelectedIndex());
        int size = (Integer) pageSizeInput.getSelectedItem();
        int soTrang = Math.max(1, (ketQua.size() + size - 1) / size);
        page = Math.max(1, Math.min(page, soTrang));
        int batDau = (page - 1) * size;
        int ketThuc = Math.min(batDau + size, ketQua.size());
        productModel.setRowCount(0);
        for (int i = batDau; i < ketThuc; i++) {
            SanPham sp = ketQua.get(i);
            productModel.addRow(new Object[]{sp, danhMucDAO.layTen(sp.getDanhMucId()), sp.getThuongHieu(),
                    money(sp.getGiaBan()), warranty(sp.getBaoHanhThang()),
                    sp.isDangKinhDoanh() ? "Đang kinh doanh" : "Ngừng kinh doanh", ""});
        }
        listCount.setText(ketQua.size() + " kết quả / " + sanPhamDAO.layTatCa().size() + " sản phẩm");
        pageInfo.setText("Hiển thị " + (ketQua.isEmpty() ? 0 : batDau + 1) + "–" + ketThuc + " trong " + ketQua.size() + " kết quả");
        pageNumber.setText("Trang " + page + " / " + soTrang);
        previousPage.setEnabled(page > 1);
        nextPage.setEnabled(page < soTrang);
        resultsLayout.show(results, ketQua.isEmpty() ? "empty" : "table");
        revalidate();
        repaint();
    }

    private void showList() { clearMessage(); refreshList(); viewLayout.show(views, "list"); }

    private Icon productImage(SanPham product, int size) {
        if (product.getAnhBase64().isEmpty()) return loadIcon("/icon/ic_sanpham.png", textGray, Math.min(size, 48));
        ImageIcon original = new ImageIcon(Base64.getDecoder().decode(product.getAnhBase64()));
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

    private void showDetails(String id) {
        SanPham p = sanPhamDAO.timTheoMa(id);
        if (p == null) { showList(); message("Sản phẩm không còn tồn tại.", false); return; }
        clearMessage();
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        JLabel image = new JLabel(productImage(p, 160)); image.setHorizontalAlignment(SwingConstants.CENTER);
        formItem(form, "Hình ảnh", image, 0, 0, 2, 170);
        detailItem(form, "Tên sản phẩm", p.getTen(), 0, 1, 2);
        detailItem(form, "SKU", p.getSku(), 0, 2, 1);
        detailItem(form, "Danh mục", danhMucDAO.layTen(p.getDanhMucId()), 1, 2, 1);
        detailItem(form, "Thương hiệu", p.getThuongHieu(), 0, 3, 1);
        detailItem(form, "Giá bán", money(p.getGiaBan()), 1, 3, 1);
        detailItem(form, "Bảo hành", warranty(p.getBaoHanhThang()), 0, 4, 1);
        detailItem(form, "Trạng thái", p.isDangKinhDoanh() ? "Đang kinh doanh" : "Ngừng kinh doanh", 1, 4, 1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        detailItem(form, "Ngày tạo", format.format(p.getTaoLuc()), 0, 5, 1);
        detailItem(form, "Cập nhật lần cuối", format.format(p.getCapNhatLuc()), 1, 5, 1);
        formItem(form, "Mô tả / thông số kỹ thuật", textArea(p.getMoTa().isBlank() ? "Chưa có mô tả." : p.getMoTa(), false), 0, 6, 2, 110);
        JButton back = createButton("Về danh sách", null, 0), edit = createButton("Chỉnh sửa", "/icon/ic_chinhsua.png", 1);
        back.addActionListener(e -> showList()); edit.addActionListener(e -> showEditor(id));
        install("detail", pageBody("Chi tiết sản phẩm", p.getSku(), form, back, edit));
    }

    private void detailItem(JPanel form, String label, String value, int x, int y, int span) {
        JTextField field = createInput(""); field.setText(value); field.setEditable(false);
        formItem(form, label, field, x, y, span, 34);
    }

private void showEditor(String id) {
        if (danhMucDAO.layTatCa().isEmpty()) {
            showCategories(null);
            message("Hãy tạo ít nhất một danh mục trước khi thêm sản phẩm.", false);
            return;
        }
        if (id != null && sanPhamDAO.timTheoMa(id) == null) {
            showList();
            message("Sản phẩm không còn tồn tại.", false);
            return;
        }
        clearMessage();
        SanPhamForm form = new SanPhamForm(id);
        install("editor", form);
        form.ten.requestFocusInWindow();
    }

    /** Form có các thuộc tính và phương thức riêng, không dùng mảng để giữ trạng thái. */
    private class SanPhamForm extends JPanel {
        private final SanPham banGoc;
        private final JTextField sku = createInput("Ví dụ: TL002");
        private final JTextField ten = createInput("Tên sản phẩm, tối đa 150 ký tự");
        private final JTextField thuongHieu = createInput("Tên thương hiệu");
        private final JTextField gia = createInput("Số nguyên VND, ví dụ 12000000");
        private final JComboBox<DanhMuc> danhMuc = createCombo(new DanhMuc[0]);
        private final JSpinner baoHanh = new JSpinner(new SpinnerNumberModel(12, 0, 120, 1));
        private final JComboBox<String> trangThai = createCombo("Đang kinh doanh", "Ngừng kinh doanh");
        private final JTextArea moTa = textArea("", true);
        private final JLabel xemAnh = new JLabel();
        private String anh = "";
        private ArrayList<String> noiDungBanDau;

        SanPhamForm(String id) {
            super(new BorderLayout());
            setOpaque(false);
            banGoc = sanPhamDAO.timTheoMa(id);
            for (DanhMuc dm : danhMucDAO.layTatCa()) danhMuc.addItem(dm);
            sku.setName("product.form.sku");
            ten.setName("product.form.name");
            thuongHieu.setName("product.form.brand");
            gia.setName("product.form.price");
            danhMuc.setName("product.form.category");
            baoHanh.setName("product.form.warranty");
            trangThai.setName("product.form.status");
            moTa.setName("product.form.description");
            gia.setHorizontalAlignment(SwingConstants.RIGHT);
            baoHanh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            xemAnh.setHorizontalAlignment(SwingConstants.CENTER);
            napDuLieu();

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            formItem(form, "Tên sản phẩm *", ten, 0, 0, 2, 34);
            formItem(form, "SKU * (duy nhất, tối đa 40 ký tự)", sku, 0, 1, 1, 34);
            formItem(form, "Danh mục *", danhMuc, 1, 1, 1, 34);
            formItem(form, "Thương hiệu *", thuongHieu, 0, 2, 1, 34);
            formItem(form, "Giá bán * (VND, không dấu phân cách)", gia, 1, 2, 1, 34);
            formItem(form, "Bảo hành (0–120 tháng; 0 là không bảo hành)", baoHanh, 0, 3, 1, 34);
            formItem(form, "Trạng thái kinh doanh", trangThai, 1, 3, 1, 34);
            formItem(form, "Mô tả / thông số kỹ thuật (tối đa 3.000 ký tự)", new JScrollPane(moTa), 0, 4, 2, 110);

            JButton chonAnh = createButton("Chọn ảnh PNG / JPEG", null, 0);
            JButton boAnh = createButton("Bỏ ảnh", null, 2);
            chonAnh.addActionListener(e -> chonAnh());
            boAnh.addActionListener(e -> {
                anh = "";
                xemAnh.setIcon(loadIcon("/icon/ic_sanpham.png", textGray, 48));
            });
            JPanel khungAnh = new JPanel(new BorderLayout(8, 0));
            khungAnh.setOpaque(false);
            khungAnh.add(xemAnh, BorderLayout.CENTER);
            khungAnh.add(group(chonAnh, boAnh), BorderLayout.SOUTH);
            formItem(form, "Ảnh không bắt buộc, tối đa 2 MB và 16 triệu điểm ảnh", khungAnh, 0, 5, 2, 150);

            JButton huy = createButton("Hủy / Quay lại", null, 0);
            JButton luu = createButton("Lưu sản phẩm", null, 1);
            huy.addActionListener(e -> huy());
            luu.addActionListener(e -> perform(() -> luu()));
            ArrayList<JButton> nut = new ArrayList<>();
            nut.add(huy);
            nut.add(luu);
            if (banGoc != null) {
                JButton xoa = createButton("Xóa sản phẩm", "/icon/ic_xoa.png", 2);
                xoa.setEnabled(!banGoc.isDangKinhDoanh());
                xoa.addActionListener(e -> xoa());
                nut.add(xoa);
                JLabel huongDan = new JLabel("<html>Để xóa, hãy chọn <b>Ngừng kinh doanh</b> và lưu sản phẩm trước. Có thể kích hoạt lại bằng cách chỉnh sửa trạng thái.</html>");
                huongDan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                huongDan.setForeground(textGray);
                formItem(form, "Lưu ý về trạng thái", huongDan, 0, 6, 2, 42);
            }
            add(pageBody(banGoc == null ? "Thêm sản phẩm" : "Chỉnh sửa sản phẩm", "* Thông tin bắt buộc",
                    form, nut.toArray(new JButton[0])), BorderLayout.CENTER);
            noiDungBanDau = layNoiDung();
        }

        private void napDuLieu() {
            if (banGoc == null) {
                xemAnh.setIcon(loadIcon("/icon/ic_sanpham.png", textGray, 48));
                return;
            }
            sku.setText(banGoc.getSku());
            ten.setText(banGoc.getTen());
            thuongHieu.setText(banGoc.getThuongHieu());
            gia.setText(banGoc.getGiaBan().toPlainString());
            selectCategory(danhMuc, banGoc.getDanhMucId());
            baoHanh.setValue(banGoc.getBaoHanhThang());
            trangThai.setSelectedIndex(banGoc.isDangKinhDoanh() ? 0 : 1);
            moTa.setText(banGoc.getMoTa());
            anh = banGoc.getAnhBase64();
            xemAnh.setIcon(productImage(banGoc, 100));
        }

        private ArrayList<String> layNoiDung() {
            ArrayList<String> noiDung = new ArrayList<>();
            noiDung.add(sku.getText());
            noiDung.add(ten.getText());
            noiDung.add(thuongHieu.getText());
            noiDung.add(gia.getText());
            DanhMuc dm = (DanhMuc) danhMuc.getSelectedItem();
            noiDung.add(dm == null ? "" : dm.getId());
            noiDung.add(((JSpinner.DefaultEditor) baoHanh.getEditor()).getTextField().getText());
            noiDung.add(String.valueOf(trangThai.getSelectedIndex()));
            noiDung.add(moTa.getText());
            noiDung.add(anh);
            return noiDung;
        }

        private void luu() {
            try { baoHanh.commitEdit(); }
            catch (java.text.ParseException e) { throw new IllegalArgumentException("Bảo hành phải là số nguyên từ 0 đến 120 tháng."); }
            if (!gia.getText().trim().matches("[0-9]{1,12}")) {
                throw new IllegalArgumentException("Giá bán phải là số nguyên VND, không có dấu phân cách, tối đa 12 chữ số.");
            }
            DanhMuc dm = (DanhMuc) danhMuc.getSelectedItem();
            SanPham sp = new SanPham();
            if (banGoc != null) sp.setId(banGoc.getId());
            sp.setSku(sku.getText());
            sp.setTen(ten.getText());
            sp.setDanhMucId(dm == null ? null : dm.getId());
            sp.setThuongHieu(thuongHieu.getText());
            sp.setGiaBan(new BigDecimal(gia.getText().trim()));
            sp.setBaoHanhThang(((Number) baoHanh.getValue()).intValue());
            sp.setDangKinhDoanh(trangThai.getSelectedIndex() == 0);
            sp.setMoTa(moTa.getText());
            sp.setAnhBase64(anh);

            SanPham daLuu;
            if (banGoc == null) daLuu = sanPhamDAO.them(sp);
            else daLuu = sanPhamDAO.capNhat(sp);
            thumbnails.remove(daLuu.getId());
            refreshChoices();
            refreshList();
            showDetails(daLuu.getId());
            message("Đã lưu sản phẩm " + daLuu.getSku() + ".", true);
        }

        private void quayLai() {
            if (banGoc == null) showList();
            else showDetails(banGoc.getId());
        }

        private void huy() {
            if (noiDungBanDau.equals(layNoiDung())) quayLai();
            else confirm("Bỏ các thay đổi chưa lưu?", "Những thông tin đang nhập sẽ không được lưu.", "Bỏ thay đổi",
                    () -> quayLai(), () -> viewLayout.show(views, "editor"));
        }

        private void xoa() {
            confirm("Xóa sản phẩm " + banGoc.getSku() + "?",
                    "Sản phẩm sẽ bị xóa khỏi danh sách hiện tại. Nếu chỉ tạm dừng bán, hãy giữ trạng thái Ngừng kinh doanh.",
                    "Xóa sản phẩm", () -> perform(() -> {
                        sanPhamDAO.xoa(banGoc.getId());
                        thumbnails.remove(banGoc.getId());
                        refreshChoices();
                        showList();
                        message("Đã xóa sản phẩm " + banGoc.getSku() + ".", true);
                    }), () -> viewLayout.show(views, "editor"));
        }

        private void chonAnh() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh PNG, JPEG (tối đa 2 MB)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(UI_SanPham.this) != JFileChooser.APPROVE_OPTION) return;
            try {
                String anhMoi = SanPhamDAO.docAnh(chooser.getSelectedFile().toPath());
                ImageIcon icon = new ImageIcon(Base64.getDecoder().decode(anhMoi));
                double tyLe = Math.min(100.0 / icon.getIconWidth(), 100.0 / icon.getIconHeight());
                xemAnh.setIcon(new ImageIcon(icon.getImage().getScaledInstance(
                        Math.max(1, (int) (icon.getIconWidth() * tyLe)),
                        Math.max(1, (int) (icon.getIconHeight() * tyLe)), Image.SCALE_SMOOTH)));
                anh = anhMoi;
                clearMessage();
            } catch (IOException | IllegalArgumentException e) { message(e.getMessage(), false); }
        }
    }

    private void confirm(String title, String description, String action, Runnable yes, Runnable no) {
        clearMessage();
        JTextArea info = textArea(description, false);
        info.setPreferredSize(new Dimension(600, 110));
        JButton accept = createButton(action, null, 2);
        JButton cancel = createButton("Quay lại", null, 0);
        accept.addActionListener(e -> yes.run());
        cancel.addActionListener(e -> no.run());
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(info);
        install("confirm", pageBody(title, "Vui lòng kiểm tra trước khi tiếp tục", body, cancel, accept));
    }

    private void showCategories(String selectedId) {
        clearMessage();
        install("categories", new DanhMucPanel(selectedId));
    }

    private class DanhMucPanel extends JPanel {
        private final ArrayList<DanhMuc> danhSach = danhMucDAO.layTatCa();
        private final JTable table;
        private final JTextField ten = createInput("Tên danh mục, tối đa 80 ký tự");
        private final JTextArea moTa = textArea("", true);
        private final JLabel cheDo = new JLabel("Thêm danh mục");
        private final JLabel suDung = new JLabel("Danh mục mới chưa có sản phẩm.");
        private final JButton xoa = createButton("Xóa danh mục", null, 2);
        private String maDangSua;
        private String tenBanDau = "";
        private String moTaBanDau = "";
        private int dongTruoc = -1;
        private boolean dangChon;

        DanhMucPanel(String selectedId) {
            super(new BorderLayout());
            setOpaque(false);
            Object[][] rows = new Object[danhSach.size()][3];
            for (int i = 0; i < danhSach.size(); i++) {
                DanhMuc dm = danhSach.get(i);
                rows[i] = new Object[]{dm.getTen(), danhMucDAO.demSanPham(dm.getId()), dm.getMoTa()};
            }
            table = createTable(rows, new String[]{"Danh mục", "Số sản phẩm", "Mô tả"}, new int[]{145, 110, 150}, 1);
            table.setName("category.table");
            table.setRowHeight(38);
            ten.setName("category.form.name");
            moTa.setName("category.form.description");
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) chonDanhMuc();
            });
            cheDo.setFont(new Font("Segoe UI", Font.BOLD, 15));
            suDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            suDung.setForeground(textGray);
            xoa.setEnabled(false);
            xoa.addActionListener(e -> xoa());

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            formItem(form, " ", cheDo, 0, 0, 2, 24);
            formItem(form, "Tên danh mục *", ten, 0, 1, 2, 34);
            formItem(form, "Mô tả (tối đa 1.000 ký tự)", new JScrollPane(moTa), 0, 2, 2, 110);
            formItem(form, "Sử dụng", suDung, 0, 3, 2, 42);
            JButton luu = createButton("Lưu danh mục", null, 1);
            luu.addActionListener(e -> perform(() -> luu()));
            JPanel actions = new JPanel(new WrapLayout());
            actions.setOpaque(false);
            actions.add(group(luu, xoa));
            formItem(form, " ", actions, 0, 4, 2, 84);
            JPanel right = new WidthTrackingPanel(form);
            right.setBackground(Color.WHITE);
            right.setBorder(new EmptyBorder(0, 14, 0, 0));
            JScrollPane formScroll = new JScrollPane(right);
            formScroll.setBorder(BorderFactory.createEmptyBorder());
            formScroll.getViewport().setBackground(Color.WHITE);
            formScroll.getVerticalScrollBar().setUnitIncrement(24);
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createScrollPane(table), formScroll);
            split.setBorder(BorderFactory.createEmptyBorder());
            split.setResizeWeight(0.53);
            split.setContinuousLayout(true);
            split.setPreferredSize(new Dimension(800, 420));

            JButton back = createButton("Về danh sách sản phẩm", null, 0);
            JButton add = createButton("Thêm danh mục", "/icon/ic_them.png", 1);
            back.addActionListener(e -> quayLai());
            add.addActionListener(e -> themMoi());
            RoundedPanel panel = new RoundedPanel();
            panel.setLayout(new BorderLayout(0, 12));
            panel.add(createHeader("Quản lý danh mục", danhSach.size() + " danh mục", group(back, add)), BorderLayout.NORTH);
            panel.add(split, BorderLayout.CENTER);
            add(panel, BorderLayout.CENTER);
            if (selectedId != null) {
                for (int i = 0; i < danhSach.size(); i++) {
                    if (danhSach.get(i).getId().equals(selectedId)) { table.setRowSelectionInterval(i, i); break; }
                }
            }
        }

        private boolean coThayDoi() {
            return !ten.getText().equals(tenBanDau) || !moTa.getText().equals(moTaBanDau);
        }

        private void napDanhMuc(int dong) {
            DanhMuc dm = danhSach.get(table.convertRowIndexToModel(dong));
            dongTruoc = dong;
            maDangSua = dm.getId();
            ten.setText(dm.getTen());
            moTa.setText(dm.getMoTa());
            tenBanDau = dm.getTen();
            moTaBanDau = dm.getMoTa();
            cheDo.setText("Chỉnh sửa danh mục");
            int soLuong = danhMucDAO.demSanPham(dm.getId());
            suDung.setText("<html>" + soLuong + " sản phẩm. "
                    + (soLuong == 0 ? "Có thể xóa danh mục trống." : "Không thể xóa khi còn sản phẩm.") + "</html>");
            xoa.setEnabled(soLuong == 0);
        }

        private void chonDanhMuc() {
            if (dangChon || table.getSelectedRow() < 0) return;
            final int dongMoi = table.getSelectedRow();
            if (!coThayDoi()) { napDanhMuc(dongMoi); return; }
            dangChon = true;
            if (dongTruoc < 0) table.clearSelection();
            else table.setRowSelectionInterval(dongTruoc, dongTruoc);
            dangChon = false;
            confirm("Bỏ thay đổi danh mục?", "Thông tin đang nhập chưa được lưu. Bạn muốn chuyển sang danh mục khác?",
                    "Bỏ thay đổi", () -> {
                        dangChon = true;
                        table.setRowSelectionInterval(dongMoi, dongMoi);
                        dangChon = false;
                        napDanhMuc(dongMoi);
                        viewLayout.show(views, "categories");
                    }, () -> viewLayout.show(views, "categories"));
        }

        private void luu() {
            DanhMuc dm = new DanhMuc(maDangSua, ten.getText(), moTa.getText());
            DanhMuc daLuu;
            if (maDangSua == null) daLuu = danhMucDAO.them(dm);
            else daLuu = danhMucDAO.capNhat(dm);
            refreshChoices();
            refreshList();
            showCategories(daLuu.getId());
            message("Đã lưu danh mục " + daLuu.getTen() + ".", true);
        }

        private void xoa() {
            if (maDangSua == null) return;
            confirm("Xóa danh mục?", "Chỉ xóa danh mục không chứa sản phẩm.", "Xóa danh mục",
                    () -> perform(() -> {
                        danhMucDAO.xoa(maDangSua);
                        refreshChoices();
                        refreshList();
                        showCategories(null);
                        message("Đã xóa danh mục.", true);
                    }), () -> viewLayout.show(views, "categories"));
        }

        private void xoaForm() {
            dangChon = true;
            table.clearSelection();
            dangChon = false;
            dongTruoc = -1;
            maDangSua = null;
            ten.setText("");
            moTa.setText("");
            tenBanDau = "";
            moTaBanDau = "";
            cheDo.setText("Thêm danh mục");
            suDung.setText("Danh mục mới chưa có sản phẩm.");
            xoa.setEnabled(false);
            ten.requestFocusInWindow();
        }

        private void themMoi() {
            if (!coThayDoi()) { xoaForm(); return; }
            confirm("Bỏ thay đổi danh mục?", "Thông tin đang nhập chưa được lưu.", "Bỏ thay đổi",
                    () -> { xoaForm(); viewLayout.show(views, "categories"); },
                    () -> viewLayout.show(views, "categories"));
        }

        private void quayLai() {
            if (!coThayDoi()) { showList(); return; }
            confirm("Bỏ thay đổi danh mục?", "Thông tin danh mục đang nhập chưa được lưu.", "Bỏ thay đổi",
                    () -> showList(), () -> viewLayout.show(views, "categories"));
        }
    }
}
