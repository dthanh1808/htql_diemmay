package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import dao.DuLieuTam;
import dao.KhachHangDAO;
import entity.*;

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

    // Nút tự đo độ rộng để hiển thị đầy đủ nhãn.
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
            setToolTipText("<html>" + html(lines[0]) + "<br>" + html(lines[1]) + "</html>");
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
                setToolTipText(value == null ? null : "<html><div width='420'>" + html(value.toString()) + "</div></html>");
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


    private final KhachHangDAO dao;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel notice = new JLabel(" ");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private JTextField search, fromDate, toDate;
    private JComboBox<String> rankFilter, statusFilter, sort, pageSize;
    private JTable table;
    private DefaultTableModel model;
    private JLabel summary, pageInfo, empty;
    private JButton previous, next;
    private ArrayList<KhachHang> filtered = new ArrayList<>();
    private String keyword = "", rank;
    private Boolean active;
    private LocalDate from, to;
    private int page = 1;

    public UI_KhachHang() { this(DuLieuTam.getMacDinh()); }
    public UI_KhachHang(DuLieuTam data) {
        dao = new KhachHangDAO(data);
        setLayout(new BorderLayout(0, 8));
        setBackground(bgMain);
        setBorder(new EmptyBorder(14, 18, 14, 18));
        content.setOpaque(false);
        notice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notice.setName("customer.notice"); notice.setVisible(false);
        add(notice, BorderLayout.NORTH); add(content, BorderLayout.CENTER);
        initialiseList(); showView("list");
    }

    private void message(String text, boolean success) {
        notice.setText("<html>" + html(text) + "</html>"); notice.setToolTipText(text);
        notice.setForeground(success ? new Color(4, 120, 87) : new Color(185, 28, 28));
        notice.setVisible(true); revalidate();
    }
    private void perform(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException e) { message(e.getMessage(), false); }
    }
    private void install(String key, Component view) {
        for (Component c : content.getComponents()) if (key.equals(c.getName())) content.remove(c);
        view.setName(key); content.add(view, key); showView(key);
    }
    private void showView(String key) { cards.show(content, key); notice.setVisible(false); revalidate(); repaint(); }
    private String money(BigDecimal value) { return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value) + " đ"; }
    private String html(String value) { return (value == null ? "" : value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>"); }
    private LocalDate date(String text, String label) {
        if (text.trim().isEmpty()) return null;
        try { return LocalDate.parse(text.trim(), dateFormat); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException(label + " phải đúng định dạng dd/MM/yyyy và là ngày hợp lệ."); }
    }
    private JPanel actions(JComponent... components) {
        JPanel panel = new JPanel(new WrapLayout()); panel.setOpaque(false);
        for (JComponent c : components) { JPanel slot = new JPanel(new BorderLayout()); slot.setOpaque(false); slot.setBorder(new EmptyBorder(0, 0, 0, 8)); slot.add(c); panel.add(slot); }
        return panel;
    }
    private JButton button(String label, int style, Runnable action) {
        JButton b = createButton(label, null, style); b.addActionListener(e -> perform(action)); return b;
    }
    private JLabel label(String text) { JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.PLAIN, 13)); l.setForeground(textDark); return l; }
    private JTextField input(String name, String value) {
        JTextField f = createInput(""); f.setName(name); f.setText(value); f.setPreferredSize(new Dimension(280, 36)); return f;
    }
    private JTextArea area(String name, String text, int rows) {
        JTextArea a = new JTextArea(text, rows, 25); a.setName(name); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setFont(new Font("Segoe UI", Font.PLAIN, 13)); a.setBorder(new EmptyBorder(8, 8, 8, 8)); return a;
    }
    private static class FormPanel extends JPanel implements Scrollable {
        FormPanel() { super(new GridBagLayout()); setOpaque(false); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 160; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
    private void field(JPanel form, int row, String title, JComponent control) {
        GridBagConstraints c = new GridBagConstraints(); c.gridx = 0; c.gridy = row * 2; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(12, 4, 5, 4); JLabel l = label(title); l.setLabelFor(control); form.add(l, c);
        c.gridy++; c.insets = new Insets(0, 4, 0, 4); form.add(control, c);
    }
    private JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c); sp.setBorder(BorderFactory.createEmptyBorder()); sp.getViewport().setBackground(Color.WHITE); sp.getVerticalScrollBar().setUnitIncrement(24); return sp;
    }
    private JPanel view(String title, Component body, JPanel buttons) {
        RoundedPanel p = new RoundedPanel(); p.setLayout(new BorderLayout(0, 10));
        JLabel heading = label(title); heading.setFont(new Font("Segoe UI", Font.BOLD, 19));
        p.add(heading, BorderLayout.NORTH); p.add(body, BorderLayout.CENTER); p.add(buttons, BorderLayout.SOUTH); return p;
    }
    private void confirm(String title, String text, String accept, int style, Runnable yes, Runnable no) {
        JPanel body = new JPanel(new BorderLayout()); body.setBackground(Color.WHITE);
        JTextArea explanation = area("customer.confirm.text", text, 6); explanation.setEditable(false); body.add(scroll(explanation), BorderLayout.CENTER);
        install("confirm", view(title, body, actions(button("Quay lại", 0, no), button(accept, style, yes))));
    }
    private void watch(JTextField f, Runnable changed) { f.getDocument().addDocumentListener(listener(changed)); }
    private DocumentListener listener(Runnable changed) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { changed.run(); }
            public void removeUpdate(DocumentEvent e) { changed.run(); }
            public void changedUpdate(DocumentEvent e) { changed.run(); }
        };
    }
    private void discard(boolean dirty, Runnable leave, Runnable stay) {
        if (!dirty) leave.run();
        else confirm("Bỏ thay đổi chưa lưu?", "Thông tin đang nhập chưa được lưu. Bạn có muốn bỏ các thay đổi này?", "Bỏ thay đổi", 2, leave, stay);
    }

    private void initialiseList() {
        JPanel listView = new JPanel(new BorderLayout(0, 12)); listView.setOpaque(false);
        RoundedPanel filter = new RoundedPanel(); filter.setLayout(new GridBagLayout());
        JPanel searchBox = createSearch("Nhập mã KH, họ tên, số điện thoại hoặc email");
        search = (JTextField) ((BorderLayout) searchBox.getLayout()).getLayoutComponent(BorderLayout.CENTER); search.setName("customer.search");
        rankFilter = createCombo("Tất cả", "Member", "Silver", "Gold", "Diamond"); rankFilter.setName("customer.rank");
        statusFilter = createCombo("Tất cả", "Đang hoạt động", "Ngừng hoạt động"); statusFilter.setName("customer.status");
        fromDate = input("customer.from", ""); toDate = input("customer.to", "");
        addFilter(filter, searchBox, "Tìm kiếm khách hàng", 0, 0, 2, 360);
        addFilter(filter, rankFilter, "Hạng thành viên", 2, 0, 1, 140);
        addFilter(filter, statusFilter, "Trạng thái", 3, 0, 1, 140);
        addFilter(filter, fromDate, "Đăng ký từ ngày (dd/MM/yyyy)", 0, 1, 1, 174);
        addFilter(filter, toDate, "Đến ngày (dd/MM/yyyy)", 1, 1, 1, 174);
        JPanel filterButtons = new JPanel(new GridLayout(1, 2, 8, 0)); filterButtons.setOpaque(false);
        filterButtons.add(button("Áp dụng bộ lọc", 1, this::applyFilters)); filterButtons.add(button("Xóa bộ lọc", 2, this::resetFilters));
        addFilter(filter, filterButtons, " ", 2, 1, 2, 360);
        for (JTextField f : new JTextField[]{search, fromDate, toDate}) f.addActionListener(e -> perform(this::applyFilters));
        listView.add(filter, BorderLayout.NORTH);
        table = createTable(new Object[0][7], new String[]{"Khách hàng", "Liên hệ", "Hạng thành viên", "Tổng chi tiêu", "Ngày đăng ký", "Trạng thái", "Thao tác"},
                new int[]{170, 170, 110, 120, 105, 130, 195}, 3);
        table.setName("customer.table"); model = (DefaultTableModel) table.getModel();
        table.getColumnModel().getColumn(0).setCellRenderer(new TwoLineRenderer(false));
        table.getColumnModel().getColumn(1).setCellRenderer(new TwoLineRenderer(false));
        table.getColumnModel().getColumn(2).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer("Xem", "Sửa", "Lịch sử"));
        table.getColumnModel().getColumn(6).setCellEditor(new CustomerActionEditor());
        table.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint()); if (e.getClickCount() == 2 && row >= 0 && table.columnAtPoint(e.getPoint()) != 6) details(rowId(row), false);
        }});
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "viewCustomer");
        table.getActionMap().put("viewCustomer", new AbstractAction() { public void actionPerformed(ActionEvent e) { if (table.getSelectedRow() >= 0) details(rowId(table.getSelectedRow()), false); }});
        table.getInputMap().put(KeyStroke.getKeyStroke("F2"), "editCustomer");
        table.getActionMap().put("editCustomer", new AbstractAction() { public void actionPerformed(ActionEvent e) { if (table.getSelectedRow() >= 0) editor(rowId(table.getSelectedRow())); }});
        sort = createCombo("Tên A–Z", "Tên Z–A", "Chi tiêu tăng dần", "Chi tiêu giảm dần", "Mới đăng ký"); sort.setName("customer.sort");
        sort.addActionListener(e -> { page = 1; refreshList(); });
        JPanel heading = createHeader("Danh sách khách hàng", "", group(
                button("Đăng ký khách hàng", 1, () -> editor(null)), button("Xuất CSV", 0, this::exportCsv),
                button("Chính sách hạng thành viên", 0, this::policy), button("Gửi khuyến mãi", 0, this::promotion)), group(label("Sắp xếp:"), sort));
        summary = (JLabel) ((JPanel) heading.getComponent(0)).getComponent(1);
        RoundedPanel list = new RoundedPanel(); list.setLayout(new BorderLayout(0, 8)); list.add(heading, BorderLayout.NORTH);
        JPanel rows = new JPanel(new BorderLayout()); rows.setOpaque(false);
        empty = label("Không tìm thấy khách hàng phù hợp. Hãy đổi từ khóa hoặc xóa bộ lọc."); empty.setHorizontalAlignment(SwingConstants.CENTER); empty.setName("customer.empty");
        rows.add(empty, BorderLayout.NORTH); rows.add(createScrollPane(table), BorderLayout.CENTER); list.add(rows, BorderLayout.CENTER);
        pageInfo = label(""); pageSize = createCombo("10", "25", "50"); pageSize.setName("customer.pageSize");
        pageSize.addActionListener(e -> { page = 1; refreshList(); });
        previous = button("Trang trước", 0, () -> { page--; refreshList(); }); next = button("Trang sau", 0, () -> { page++; refreshList(); });
        list.add(actions(pageInfo, label("Số dòng:"), pageSize, previous, next), BorderLayout.SOUTH);
        listView.add(list, BorderLayout.CENTER); install("list", listView); refreshList();
    }
    private String rowId(int row) { return ((String[]) model.getValueAt(table.convertRowIndexToModel(row), 0))[1]; }
    private class CustomerActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 5));
        private String id;
        CustomerActionEditor() {
            JButton view = button("Xem", 0, () -> { String chosen = id; fireEditingStopped(); details(chosen, false); });
            JButton edit = button("Sửa", 0, () -> { String chosen = id; fireEditingStopped(); editor(chosen); });
            JButton history = button("Lịch sử", 0, () -> { String chosen = id; fireEditingStopped(); details(chosen, true); });
            for (JButton b : new JButton[]{view, edit, history}) { b.setBorder(new EmptyBorder(0, 4, 0, 4)); panel.add(b); }
        }
        public Component getTableCellEditorComponent(JTable t, Object value, boolean selected, int row, int col) { id = rowId(row); return panel; }
        public Object getCellEditorValue() { return ""; }
    }
    private void applyFilters() {
        LocalDate f = date(fromDate.getText(), "Ngày bắt đầu"), t = date(toDate.getText(), "Ngày kết thúc");
        if (f != null && t != null && f.isAfter(t)) throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        keyword = search.getText().trim(); rank = rankFilter.getSelectedIndex() == 0 ? null : (String) rankFilter.getSelectedItem();
        active = statusFilter.getSelectedIndex() == 0 ? null : statusFilter.getSelectedIndex() == 1;
        from = f; to = t; page = 1; refreshList(); notice.setVisible(false);
    }
    private void resetFilters() {
        search.setText(""); fromDate.setText(""); toDate.setText(""); rankFilter.setSelectedIndex(0); statusFilter.setSelectedIndex(0); applyFilters();
    }
    private void refreshList() {
        if (model == null) return;
        if (table.isEditing()) table.getCellEditor().cancelCellEditing();
        filtered = dao.timKiem(keyword, rank, active, from, to); dao.sapXep(filtered, sort.getSelectedIndex());
        int size = Integer.parseInt((String) pageSize.getSelectedItem()); int pages = Math.max(1, (filtered.size() + size - 1) / size);
        page = Math.min(Math.max(1, page), pages); int start = (page - 1) * size, end = Math.min(start + size, filtered.size());
        model.setRowCount(0);
        for (int i = start; i < end; i++) {
            KhachHang kh = filtered.get(i);
            model.addRow(new Object[]{new String[]{kh.getHoTen(), kh.getId()}, new String[]{kh.getSoDienThoai(), kh.getEmail().isEmpty() ? "Chưa có email" : kh.getEmail()},
                    dao.layHang(kh.getId()).getTen(), money(dao.tongChiTieu(kh.getId())), dateFormat.format(kh.getTaoLuc()), kh.isDangHoatDong() ? "Đang hoạt động" : "Ngừng hoạt động", ""});
        }
        summary.setText(filtered.size() + " kết quả / " + dao.layTatCa().size() + " khách hàng");
        pageInfo.setText("Hiển thị " + (filtered.isEmpty() ? 0 : start + 1) + "–" + end + "/" + filtered.size() + " · Trang " + page + "/" + pages);
        previous.setEnabled(page > 1); next.setEnabled(page < pages); empty.setVisible(filtered.isEmpty());
    }
    private void backToList() { refreshList(); showView("list"); }
    private void editor(String id) { install("editor", new CustomerForm(id)); }

    private class CustomerForm extends JPanel {
        private final KhachHang original;
        private final JTextField name, phone, email, address, birthday;
        private final JTextArea note;
        private final JComboBox<String> state;
        private final JCheckBox optIn;
        private boolean dirty;
        CustomerForm(String id) {
            super(new BorderLayout()); setOpaque(false);
            original = id == null ? null : dao.timTheoMa(id);
            if (id != null && original == null) throw new IllegalArgumentException("Khách hàng không còn tồn tại.");
            name = input("customer.form.name", original == null ? "" : original.getHoTen());
            phone = input("customer.form.phone", original == null ? "" : original.getSoDienThoai());
            email = input("customer.form.email", original == null ? "" : original.getEmail());
            address = input("customer.form.address", original == null ? "" : original.getDiaChi());
            birthday = input("customer.form.birthday", original == null || original.getNgaySinh() == null ? "" : dateFormat.format(original.getNgaySinh()));
            note = area("customer.form.note", original == null ? "" : original.getGhiChu(), 3);
            state = createCombo("Đang hoạt động", "Ngừng hoạt động"); state.setName("customer.form.status"); state.setSelectedIndex(original == null || original.isDangHoatDong() ? 0 : 1);
            optIn = new JCheckBox("Đồng ý nhận thông báo khuyến mãi", original != null && original.isNhanKhuyenMai()); optIn.setName("customer.form.optIn"); optIn.setOpaque(false);
            FormPanel form = new FormPanel(); int r = 0;
            field(form, r++, "Họ tên *", name); field(form, r++, "Số điện thoại di động *", phone);
            field(form, r++, "Email (không bắt buộc)", email); field(form, r++, "Địa chỉ", address);
            field(form, r++, "Ngày sinh (dd/MM/yyyy, không bắt buộc)", birthday); field(form, r++, "Ghi chú", new JScrollPane(note));
            if (original != null) field(form, r++, "Trạng thái", state);
            field(form, r++, "Tùy chọn liên hệ", optIn);
            field(form, r, "Lưu ý", label("<html>Chi tiêu và hạng được tính từ lịch sử mua hàng, không sửa trực tiếp tại hồ sơ.</html>"));
            JButton delete = button("Xóa khách hàng", 2, this::delete);
            delete.setEnabled(original != null && dao.coTheXoa(original.getId()));
            delete.setToolTipText("Chỉ xóa khách đã ngừng hoạt động, chưa có giao dịch hoặc thông báo.");
            add(view(original == null ? "Đăng ký khách hàng" : "Chỉnh sửa khách hàng · " + original.getId(), scroll(form), actions(
                    button("Lưu khách hàng", 1, this::save), button("Hủy / Quay lại", 0, this::cancel), delete)));
            for (JTextField f : new JTextField[]{name, phone, email, address, birthday}) watch(f, () -> dirty = true);
            note.getDocument().addDocumentListener(listener(() -> dirty = true));
            state.addActionListener(e -> dirty = true); optIn.addActionListener(e -> dirty = true);
        }
        private void save() {
            KhachHang kh = original == null ? new KhachHang() : new KhachHang(original);
            kh.setHoTen(name.getText()); kh.setSoDienThoai(phone.getText()); kh.setEmail(email.getText()); kh.setDiaChi(address.getText());
            kh.setNgaySinh(date(birthday.getText(), "Ngày sinh")); kh.setGhiChu(note.getText());
            kh.setNhanKhuyenMai(optIn.isSelected()); kh.setDangHoatDong(state.getSelectedIndex() == 0);
            KhachHang saved = original == null ? dao.them(kh) : dao.capNhat(kh);
            details(saved.getId(), false); message("Đã lưu khách hàng " + saved.getId() + ".", true);
        }
        private void cancel() { discard(dirty, () -> { if (original == null) backToList(); else details(original.getId(), false); }, () -> showView("editor")); }
        private void delete() {
            if (original == null) return;
            confirm("Xóa khách hàng?", "Xóa vĩnh viễn hồ sơ " + original.getId() + " – " + original.getHoTen() + ". Không thể hoàn tác.", "Xóa khách hàng", 2,
                    () -> { dao.xoa(original.getId()); backToList(); message("Đã xóa khách hàng.", true); }, () -> showView("editor"));
        }
    }

    private void details(String id, boolean history) {
        KhachHang kh = dao.timTheoMa(id);
        if (kh == null) throw new IllegalArgumentException("Khách hàng không còn tồn tại.");
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13)); tabs.setName("customer.tabs");
        FormPanel info = new FormPanel(); HangThanhVien hang = dao.layHang(id); BigDecimal total = dao.tongChiTieu(id);
        field(info, 0, "Thông tin liên hệ", label("<html>" + html(kh.getHoTen()) + "<br>" + html(kh.getSoDienThoai()) + "<br>" + html(kh.getEmail()) + "<br>" + html(kh.getDiaChi()) + "</html>"));
        field(info, 1, "Ngày sinh", label(kh.getNgaySinh() == null ? "Chưa cung cấp" : dateFormat.format(kh.getNgaySinh())));
        field(info, 2, "Trạng thái", label(kh.isDangHoatDong() ? "Đang hoạt động" : "Ngừng hoạt động"));
        field(info, 3, "Chi tiêu và hạng thành viên", label(money(total) + " · " + hang.getTen() + " · Ưu đãi theo chính sách: " + hang.getPhanTramUuDai() + "%"));
        String progress = "Đã đạt hạng cao nhất.";
        for (HangThanhVien h : dao.layChinhSach()) if (h.getChiTieuToiThieu().compareTo(total) > 0) { progress = "Cần thêm " + money(h.getChiTieuToiThieu().subtract(total)) + " để đạt " + h.getTen() + "."; break; }
        field(info, 4, "Tiến độ thăng hạng", label(progress));
        field(info, 5, "Nhận khuyến mãi", label(kh.isNhanKhuyenMai() ? "Đã đồng ý" : "Chưa đồng ý"));
        field(info, 6, "Đăng ký / cập nhật", label(timeFormat.format(kh.getTaoLuc()) + " / " + timeFormat.format(kh.getCapNhatLuc())));
        field(info, 7, "Ghi chú", label("<html>" + html(kh.getGhiChu()) + "</html>"));
        tabs.addTab("Hồ sơ", scroll(info));
        ArrayList<GiaoDichKhachHang> transactions = dao.layGiaoDich(id); Object[][] rows = new Object[transactions.size()][4];
        for (int i = 0; i < rows.length; i++) { GiaoDichKhachHang gd = transactions.get(i); rows[i] = new Object[]{gd.getMaHoaDon(), timeFormat.format(gd.getThoiGian()), money(gd.getSoTien()), gd.getNoiDung()}; }
        JTable purchases = createTable(rows, new String[]{"Mã hóa đơn", "Thời gian", "Thực thanh toán", "Nội dung"}, new int[]{150, 165, 150, 400}, 2); purchases.setName("customer.transactions");
        JPanel spending = new JPanel(new BorderLayout(0, 8)); spending.setBackground(Color.WHITE);
        spending.add(label(transactions.size() + " giao dịch · Tổng thực thanh toán: " + money(total)), BorderLayout.NORTH); spending.add(createScrollPane(purchases));
        tabs.addTab("Lịch sử mua hàng", spending);
        ArrayList<LichSuKhachHang> events = dao.layLichSu(id); rows = new Object[events.size()][3];
        for (int i = 0; i < rows.length; i++) { LichSuKhachHang ls = events.get(i); rows[i] = new Object[]{timeFormat.format(ls.getThoiGian()), ls.getLoai(), ls.getNoiDung()}; }
        JTable audit = createTable(rows, new String[]{"Thời gian", "Hoạt động", "Nội dung"}, new int[]{165, 120, 650}, -1); audit.setName("customer.history");
        tabs.addTab("Lịch sử hoạt động", createScrollPane(audit));
        ArrayList<ThongBaoKhuyenMai> notifications = dao.layThongBao(id);
        JPanel messages = new JPanel(); messages.setBackground(Color.WHITE); messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
        if (notifications.isEmpty()) messages.add(label("Chưa có thông báo khuyến mãi nội bộ."));
        for (ThongBaoKhuyenMai tb : notifications) {
            JTextArea text = area("customer.notification", tb.getId() + " · " + timeFormat.format(tb.getTaoLuc()) + "\n" + tb.getTieuDe() + "\n" + tb.getNoiDung(), 5);
            text.setEditable(false); messages.add(text); messages.add(Box.createVerticalStrut(12));
        }
        tabs.addTab("Thông báo nội bộ", scroll(messages)); tabs.setSelectedIndex(history ? 2 : 0);
        install("detail", view("Khách hàng · " + kh.getId(), tabs, actions(button("Về danh sách", 0, this::backToList), button("Chỉnh sửa", 1, () -> editor(id)))));
    }

    private void policy() { install("policy", new PolicyForm()); }
    private class PolicyForm extends JPanel {
        private final ArrayList<HangThanhVien> ranks = dao.layChinhSach();
        private final JTextField[] thresholds = new JTextField[4], discounts = new JTextField[4];
        private boolean dirty;
        PolicyForm() {
            super(new BorderLayout()); setOpaque(false); FormPanel form = new FormPanel();
            field(form, 0, "Cách tính", label("<html>Hạng tính theo tổng tiền thực thanh toán tích lũy. Lưu chính sách sẽ tính lại hạng cho mọi khách hàng.<br>Ưu đãi là cấu hình cho module bán hàng; chưa tự áp dụng vào hóa đơn.</html>"));
            for (int i = 0; i < ranks.size(); i++) {
                HangThanhVien h = ranks.get(i);
                thresholds[i] = input("customer.policy.threshold." + i, h.getChiTieuToiThieu().toPlainString());
                discounts[i] = input("customer.policy.discount." + i, "" + h.getPhanTramUuDai());
                thresholds[i].setEditable(i != 0);
                field(form, i * 2 + 1, h.getTen() + " · Chi tiêu tối thiểu (VND nguyên)", thresholds[i]);
                field(form, i * 2 + 2, h.getTen() + " · Ưu đãi (%)", discounts[i]);
                watch(thresholds[i], () -> dirty = true); watch(discounts[i], () -> dirty = true);
            }
            add(view("Chính sách hạng thành viên", scroll(form), actions(button("Lưu chính sách", 1, this::save),
                    button("Hủy / Quay lại", 0, () -> discard(dirty, UI_KhachHang.this::backToList, () -> showView("policy"))))));
        }
        private void save() {
            ArrayList<HangThanhVien> draft = new ArrayList<>();
            try { for (int i = 0; i < ranks.size(); i++) draft.add(new HangThanhVien(ranks.get(i).getTen(), new BigDecimal(thresholds[i].getText().trim()), Integer.parseInt(discounts[i].getText().trim()))); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Ngưỡng chi tiêu và ưu đãi phải là số hợp lệ; nhập tiền không có dấu phân cách."); }
            // DAO kiểm tra toàn bộ trước khi thay thế chính sách, tránh lưu dở dang.
            confirm("Áp dụng chính sách mới?", "Chính sách mới sẽ ảnh hưởng hạng và ưu đãi của toàn bộ khách hàng hiện tại.", "Áp dụng chính sách", 1,
                    () -> { dao.luuChinhSach(draft); backToList(); message("Đã lưu chính sách và tính lại hạng thành viên.", true); }, () -> showView("policy"));
        }
    }

    private void promotion() { install("promotion", new PromotionForm()); }
    private class PromotionForm extends JPanel {
        private final JComboBox<String> target = createCombo("Tất cả", "Member", "Silver", "Gold", "Diamond");
        private final JTextField title = input("customer.promo.title", "");
        private final JTextArea body = area("customer.promo.body", "", 6);
        private final JLabel recipientInfo = label("");
        private final JButton send;
        private boolean dirty;
        PromotionForm() {
            super(new BorderLayout()); setOpaque(false); FormPanel form = new FormPanel(); target.setName("customer.promo.rank");
            field(form, 0, "Kênh thông báo", label("<html>Thông báo nội bộ, chỉ lưu trong phiên chạy này. Chưa tích hợp gửi email/SMS.<br>Chỉ khách đang hoạt động và đã đồng ý nhận khuyến mãi mới được chọn.</html>"));
            field(form, 1, "Nhóm hạng thành viên", target); field(form, 2, "Người nhận đủ điều kiện", recipientInfo);
            field(form, 3, "Tiêu đề * (tối đa 120 ký tự)", title); field(form, 4, "Nội dung * (tối đa 2.000 ký tự)", new JScrollPane(body));
            send = button("Tạo thông báo nội bộ", 1, this::send);
            add(view("Gửi thông báo khuyến mãi", scroll(form), actions(send, button("Hủy / Quay lại", 0,
                    () -> discard(dirty, UI_KhachHang.this::backToList, () -> showView("promotion"))))));
            target.addActionListener(e -> { dirty = true; recipients(); }); watch(title, () -> dirty = true); body.getDocument().addDocumentListener(listener(() -> dirty = true)); recipients();
        }
        private String targetRank() { return target.getSelectedIndex() == 0 ? null : (String) target.getSelectedItem(); }
        private void recipients() {
            ArrayList<KhachHang> list = dao.nguoiNhanKhuyenMai(targetRank());
            StringBuilder names = new StringBuilder("<html>" + list.size() + " khách hàng");
            for (KhachHang kh : list) names.append("<br>").append(html(kh.getId() + " · " + kh.getHoTen()));
            names.append("</html>"); recipientInfo.setText(names.toString()); send.setEnabled(!list.isEmpty());
        }
        private void send() {
            String td = title.getText().trim(), nd = body.getText().trim();
            if (td.isEmpty() || td.length() > 120 || nd.isEmpty() || nd.length() > 2000) throw new IllegalArgumentException("Nhập tiêu đề 1–120 ký tự và nội dung 1–2.000 ký tự.");
            int count = dao.nguoiNhanKhuyenMai(targetRank()).size();
            confirm("Xác nhận tạo thông báo", "Tạo thông báo nội bộ cho " + count + " khách hàng đủ điều kiện.\nTiêu đề: " + td + "\n\n" + nd, "Xác nhận tạo", 1,
                    () -> { int created = dao.guiKhuyenMai(targetRank(), td, nd); backToList(); message("Đã tạo " + created + " thông báo nội bộ. Chưa gửi email/SMS.", true); }, () -> showView("promotion"));
        }
    }

    /** Xuất toàn bộ kết quả đã áp dụng lọc, kể cả các trang chưa hiển thị. */
    public String taoCsv() {
        StringBuilder csv = new StringBuilder("Mã KH,Họ tên,Số điện thoại,Email,Địa chỉ,Hạng,Tổng chi tiêu,Ngày đăng ký,Trạng thái,Nhận khuyến mãi\r\n");
        for (KhachHang kh : filtered) {
            String[] values = {kh.getId(), kh.getHoTen(), kh.getSoDienThoai(), kh.getEmail(), kh.getDiaChi(), dao.layHang(kh.getId()).getTen(),
                    dao.tongChiTieu(kh.getId()).toPlainString(), dateFormat.format(kh.getTaoLuc()), kh.isDangHoatDong() ? "Đang hoạt động" : "Ngừng hoạt động", kh.isNhanKhuyenMai() ? "Có" : "Không"};
            for (int i = 0; i < values.length; i++) { if (i > 0) csv.append(','); String v = values[i];
                if (v.stripLeading().matches("(?s)^[=+@-].*") || v.startsWith("\t") || v.startsWith("\r")) v = "'" + v;
                csv.append('"').append(v.replace("\"", "\"\"")).append('"'); }
            csv.append("\r\n");
        }
        return csv.toString();
    }
    private void exportCsv() {
        if (filtered.isEmpty()) throw new IllegalArgumentException("Không có khách hàng để xuất.");
        JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Xuất danh sách khách hàng (UTF-8 CSV)"); chooser.setSelectedFile(new java.io.File("khach-hang.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.nio.file.Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) path = path.resolveSibling(path.getFileName() + ".csv");
        final java.nio.file.Path destination = path;
        String csv = "\uFEFF" + taoCsv(); Runnable save = () -> {
            try { Files.writeString(destination, csv, StandardCharsets.UTF_8); showView("list"); message("Đã xuất " + filtered.size() + " khách hàng: " + destination.getFileName(), true); }
            catch (IOException e) { message("Không thể ghi file: " + e.getMessage(), false); }
        };
        if (Files.exists(path)) confirm("Ghi đè file?", "File " + path.getFileName() + " đã tồn tại. Ghi đè nội dung hiện có?", "Ghi đè file", 2, save, () -> showView("list"));
        else save.run();
    }
}
