package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class PNL_BaoHanh extends JPanel {
    private Color bgMain = new Color(245, 247, 250);
    private Color bgWhite = Color.WHITE;
    private Color textDark = new Color(30, 30, 30);
    private Color textGray = new Color(130, 130, 130);
    private Color colorBlue = new Color(37, 99, 235);
    private Color bgHover = new Color(239, 246, 255);

    class RoundedPanel extends JPanel {
        private int cornerRadius;
        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            setBackground(bgColor);
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JLabel lblView = new JLabel();
        private JLabel lblEdit = new JLabel();
        private JLabel lblMore = new JLabel();

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 8));
            setOpaque(true);
            
            setCustomIcon(lblView, "/icon/ic_xemchitiet.png", 14, 14);
            setCustomIcon(lblEdit, "/icon/ic_chinhsua.png", 14, 14);
            setCustomIcon(lblMore, "/icon/ic_tuychon.png", 14, 14);

            add(lblView);
            add(lblEdit);
            add(lblMore);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    public PNL_BaoHanh() {
        setLayout(new BorderLayout(12, 0));
        setBackground(bgMain);
        setBorder(new EmptyBorder(12, 15, 12, 15));

        // =========================================================================
        // PHẦN BÊN TRÁI: THANH TÌM KIẾM, TAB VÀ BẢNG DANH SÁCH PHIẾU BẢO HÀNH
        // =========================================================================
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 12));
        pnlLeft.setOpaque(false);

        JPanel pnlLeftNorth = new JPanel(new BorderLayout(0, 12));
        pnlLeftNorth.setOpaque(false);

        // --- 1. THANH TÌM KIẾM & BỘ LỌC PHÍA TRÊN ---
        RoundedPanel pnlFilter = new RoundedPanel(12, bgWhite);
        pnlFilter.setLayout(new BorderLayout(8, 0));
        pnlFilter.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel pnlSearchBox = new JPanel(new BorderLayout(6, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230)),
            new EmptyBorder(0, 8, 0, 8)
        ));
        pnlSearchBox.setPreferredSize(new Dimension(240, 32));

        JLabel lblSearchIcon = new JLabel();
        setCustomIcon(lblSearchIcon, "/icon/ic_timkiem.png", 14, 14);

        JTextField txtSearch = new JTextField(" Tìm mã phiếu, mã đơn hàng...");
        txtSearch.setForeground(textGray);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearch.setBorder(null);
        pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        JPanel pnlFilterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlFilterControls.setOpaque(false);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{
            "Trạng thái: Tất cả", "Đang xử lý", "Đã hoàn thành", "Từ chối"
        });
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbStatus.setBackground(Color.WHITE);
        cbStatus.setPreferredSize(new Dimension(135, 32));

        JComboBox<String> cbType = new JComboBox<>(new String[]{
            "Loại: Tất cả", "Bảo hành phần cứng", "Bảo hành phần mềm"
        });
        cbType.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbType.setBackground(Color.WHITE);
        cbType.setPreferredSize(new Dimension(145, 32));

        JTextField txtDate = new JTextField(" 📅 Chọn ngày");
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtDate.setForeground(textGray);
        txtDate.setPreferredSize(new Dimension(120, 32));

        pnlFilterControls.add(cbStatus);
        pnlFilterControls.add(cbType);
        pnlFilterControls.add(txtDate);

        pnlFilter.add(pnlSearchBox, BorderLayout.CENTER);
        pnlFilter.add(pnlFilterControls, BorderLayout.EAST);
        pnlLeftNorth.add(pnlFilter, BorderLayout.NORTH);

        pnlLeft.add(pnlLeftNorth, BorderLayout.NORTH);

        // --- 2. BẢNG DANH SÁCH PHIẾU BẢO HÀNH KÈM TAB CHUYỂN ĐỔI ---
        RoundedPanel pnlTableContainer = new RoundedPanel(12, bgWhite);
        pnlTableContainer.setLayout(new BorderLayout(0, 10));
        pnlTableContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlTabs.setOpaque(false);
        pnlTabs.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 232, 235)));

        JLabel lblTab1 = new JLabel("Danh sách phiếu bảo hành");
        lblTab1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTab1.setForeground(colorBlue);
        lblTab1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, colorBlue),
            new EmptyBorder(0, 0, 8, 0)
        ));

        JLabel lblTab2 = new JLabel("Lịch sử xử lý");
        lblTab2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTab2.setForeground(textGray);
        lblTab2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblTab2.setBorder(new EmptyBorder(0, 0, 8, 0));

        pnlTabs.add(lblTab1);
        pnlTabs.add(lblTab2);
        pnlTableContainer.add(pnlTabs, BorderLayout.NORTH);

        // Đã đổi cột đầu tiên từ Checkbox thành STT
        String[] columns = {"STT", "Mã phiếu", "Ngày tạo", "Khách hàng", "Sản phẩm", "Loại bảo hành", "Trạng thái", "Thao tác"};

        String tagDangXuLy = "<html><span style='background-color:#ffedd5; color:#ea580c; padding:3px 8px; border-radius:10px; font-size:10px;'>● Đang xử lý</span></html>";
        String tagHoanThanh = "<html><span style='background-color:#dcfce7; color:#16a34a; padding:3px 8px; border-radius:10px; font-size:10px;'>● Đã hoàn thành</span></html>";
        String tagTuChoi = "<html><span style='background-color:#fee2e2; color:#dc2626; padding:3px 8px; border-radius:10px; font-size:10px;'>● Từ chối</span></html>";

        Object[][] data = {
            {"1", "BH00018", "27/10/2026", "Nguyễn Thị Lan", "Điện thoại XYZ", "Bảo hành phần cứng", tagDangXuLy, ""},
            {"2", "BH00017", "26/10/2026", "Trần Văn Hùng", "Laptop ABC", "Bảo hành phần cứng", tagDangXuLy, ""},
            {"3", "BH00016", "25/10/2026", "Lê Thị Mai", "Tai nghe TWS", "Bảo hành phần mềm", tagHoanThanh, ""},
            {"4", "BH00015", "24/10/2026", "Phạm Văn Dũng", "Smartwatch GT", "Bảo hành phần cứng", tagHoanThanh, ""},
            {"5", "BH00014", "23/10/2026", "Hoàng Thị Bích", "Điện thoại XYZ", "Bảo hành phần cứng", tagDangXuLy, ""},
            {"6", "BH00013", "22/10/2026", "Nguyễn Minh Anh", "Laptop ABC", "Bảo hành phần mềm", tagHoanThanh, ""},
            {"7", "BH00012", "21/10/2026", "Vũ Đức Long", "Tai nghe TWS", "Bảo hành phần mềm", tagTuChoi, ""},
            {"8", "BH00011", "20/10/2026", "Đỗ Thị Hương", "Smartwatch GT", "Bảo hành phần cứng", tagHoanThanh, ""},
            {"9", "BH00010", "19/10/2026", "Ngô Văn Phúc", "Điện thoại XYZ", "Bảo hành phần cứng", tagDangXuLy, ""},
            {"10", "BH00009", "18/10/2026", "Lý Thị Quyên", "Laptop ABC", "Bảo hành phần cứng", tagHoanThanh, ""}
        };

        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(bgHover);
        table.setSelectionForeground(textDark);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(textDark);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(35);  // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(75);  // Mã phiếu
        table.getColumnModel().getColumn(2).setPreferredWidth(85);  // Ngày tạo
        table.getColumnModel().getColumn(3).setPreferredWidth(115); // Khách hàng
        table.getColumnModel().getColumn(4).setPreferredWidth(105); // Sản phẩm
        table.getColumnModel().getColumn(5).setPreferredWidth(125); // Loại bảo hành
        table.getColumnModel().getColumn(6).setPreferredWidth(110); // Trạng thái
        table.getColumnModel().getColumn(7).setPreferredWidth(75);  // Thao tác

        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 235)));
        scrollTable.getViewport().setBackground(bgWhite);
        pnlTableContainer.add(scrollTable, BorderLayout.CENTER);

        // Phân trang
        JPanel pnlPagination = new JPanel(new BorderLayout());
        pnlPagination.setBackground(bgWhite);
        pnlPagination.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel lblInfo = new JLabel("Hiển thị 1–10 trên 28 phiếu bảo hành");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(textGray);
        pnlPagination.add(lblInfo, BorderLayout.WEST);

        JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlPages.setBackground(bgWhite);

        String[] pages = {"<", "1", "2", "3", ">"};
        for (String p : pages) {
            JButton btnPage = new JButton(p);
            btnPage.setFocusPainted(false);
            btnPage.setPreferredSize(new Dimension(26, 26));
            btnPage.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnPage.setMargin(new Insets(0, 0, 0, 0));
            btnPage.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (p.equals("1")) {
                btnPage.setBackground(colorBlue);
                btnPage.setForeground(Color.WHITE);
                btnPage.setBorderPainted(false);
            } else {
                btnPage.setBackground(bgWhite);
                btnPage.setForeground(textDark);
                btnPage.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
            }
            pnlPages.add(btnPage);
        }

        pnlPagination.add(pnlPages, BorderLayout.EAST);
        pnlTableContainer.add(pnlPagination, BorderLayout.SOUTH);

        pnlLeft.add(pnlTableContainer, BorderLayout.CENTER);
        add(pnlLeft, BorderLayout.CENTER);

        // =========================================================================
        // PHẦN BÊN PHẢI: CHI TIẾT PHIẾU BẢO HÀNH
        // =========================================================================
        RoundedPanel pnlRight = new RoundedPanel(12, bgWhite);
        pnlRight.setPreferredSize(new Dimension(280, 0));
        pnlRight.setLayout(new BorderLayout());
        pnlRight.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlDetailHeader = new JPanel(new BorderLayout());
        pnlDetailHeader.setOpaque(false);
        pnlDetailHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 232, 235)));

        JLabel lblDetailTitle = new JLabel("Chi tiết phiếu bảo hành");
        lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDetailTitle.setForeground(textDark);

        JLabel lblStatusBadge = new JLabel("<html><span style='background-color:#ffedd5; color:#ea580c; padding:2px 5px; border-radius:5px; font-size:8px;'><b>● Đang xử lý</b></span></html>");
        
        pnlDetailHeader.add(lblDetailTitle, BorderLayout.WEST);
        pnlDetailHeader.add(lblStatusBadge, BorderLayout.EAST);
        pnlRight.add(pnlDetailHeader, BorderLayout.NORTH);

        JPanel pnlDetailContent = new JPanel();
        pnlDetailContent.setLayout(new BoxLayout(pnlDetailContent, BoxLayout.Y_AXIS));
        pnlDetailContent.setOpaque(false);
        pnlDetailContent.setBorder(new EmptyBorder(8, 0, 8, 0));

        JPanel pnlBHCard = new JPanel(new BorderLayout());
        pnlBHCard.setBackground(new Color(248, 250, 252));
        pnlBHCard.setBorder(new EmptyBorder(6, 8, 6, 8));
        JLabel lblBHId = new JLabel("BH00018");
        lblBHId.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBHId.setForeground(colorBlue);
        setCustomIcon(lblBHId, "/icon/ic_hoadon.png", 13, 13);
        lblBHId.setIconTextGap(5);

        JLabel lblBHDate = new JLabel("27/10/2026 10:24");
        lblBHDate.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblBHDate.setForeground(textGray);
        pnlBHCard.add(lblBHId, BorderLayout.WEST);
        pnlBHCard.add(lblBHDate, BorderLayout.EAST);
        pnlDetailContent.add(pnlBHCard);
        pnlDetailContent.add(Box.createVerticalStrut(6));

        pnlDetailContent.add(createDetailSectionHeader("Thông tin khách hàng", "/icon/ic_khachhang.png"));
        pnlDetailContent.add(createDetailRow("Tên KH:", "Nguyễn Thị Lan"));
        pnlDetailContent.add(createDetailRow("SĐT:", "0912345678"));
        pnlDetailContent.add(createDetailRow("Địa chỉ:", "123 Nguyễn Văn Cừ, Long Biên, HN"));
        pnlDetailContent.add(Box.createVerticalStrut(6));

        pnlDetailContent.add(createDetailSectionHeader("Thông tin sản phẩm", "/icon/ic_sanpham.png"));
        pnlDetailContent.add(createDetailRow("Sản phẩm:", "Điện thoại XYZ"));
        pnlDetailContent.add(createDetailRow("Mã SP:", "SP0012"));
        pnlDetailContent.add(createDetailRow("IMEI:", "356789012345678"));
        pnlDetailContent.add(createDetailRow("Ngày mua:", "15/05/2026"));
        pnlDetailContent.add(createDetailRow("Loại BH:", "Bảo hành phần cứng"));
        pnlDetailContent.add(createDetailRow("Thời hạn:", "12 tháng"));
        pnlDetailContent.add(Box.createVerticalStrut(6));

        pnlDetailContent.add(createDetailSectionHeader("Mô tả sự cố", "/icon/ic_baohanh.png"));
        JLabel lblIssueText = new JLabel("<html><span style='color:#333333; font-size:9px;'>Máy không lên nguồn, đã kiểm tra sơ bộ và xác định lỗi main. Cần gửi hãng kiểm tra.</span></html>");
        JPanel pnlIssue = new JPanel(new BorderLayout());
        pnlIssue.setBackground(new Color(248, 250, 252));
        pnlIssue.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlIssue.add(lblIssueText, BorderLayout.CENTER);
        pnlDetailContent.add(pnlIssue);
        pnlDetailContent.add(Box.createVerticalStrut(6));

        pnlDetailContent.add(createInlineNoteRow("Ghi chú", "/icon/ic_thongbao.png", "Khách hàng yêu cầu xử lý nhanh."));

        pnlRight.add(pnlDetailContent, BorderLayout.CENTER);

        JPanel pnlDetailBottom = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlDetailBottom.setOpaque(false);
        pnlDetailBottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlDetailBottom.setPreferredSize(new Dimension(0, 38));

        JButton btnPrint = new JButton("In phiếu");
        setCustomIcon(btnPrint, "/icon/ic_baocao.png", 14, 14);
        btnPrint.setBackground(Color.WHITE);
        btnPrint.setForeground(textDark);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPrint.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
        btnPrint.setFocusPainted(false);
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnUpdateStatus = new JButton("Cập nhật");
        setCustomIcon(btnUpdateStatus, "/icon/ic_chinhsua.png", 14, 14);
        btnUpdateStatus.setBackground(colorBlue);
        btnUpdateStatus.setForeground(Color.WHITE);
        btnUpdateStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnUpdateStatus.setBorderPainted(false);
        btnUpdateStatus.setFocusPainted(false);
        btnUpdateStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlDetailBottom.add(btnPrint);
        pnlDetailBottom.add(btnUpdateStatus);
        pnlRight.add(pnlDetailBottom, BorderLayout.SOUTH);

        add(pnlRight, BorderLayout.EAST);
    }

    private void setCustomIcon(JLabel label, String iconPath, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(iconPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {}
    }

    private void setCustomIcon(AbstractButton button, String iconPath, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(iconPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {}
    }

    private JPanel createDetailSectionHeader(String title, String iconPath) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnl.setOpaque(false);
        JLabel lblIcon = new JLabel();
        setCustomIcon(lblIcon, iconPath, 12, 12);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(colorBlue);
        pnl.add(lblIcon);
        pnl.add(lblTitle);
        return pnl;
    }

    private JPanel createInlineNoteRow(String title, String iconPath, String noteText) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnl.setOpaque(false);
        
        JLabel lblIcon = new JLabel();
        setCustomIcon(lblIcon, iconPath, 12, 12);
        
        JLabel lblTitle = new JLabel(title + ":");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(colorBlue);
        
        JLabel lblText = new JLabel(noteText);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblText.setForeground(textDark);

        pnl.add(lblIcon);
        pnl.add(lblTitle);
        pnl.add(lblText);
        return pnl;
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(1, 2, 1, 2));
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblKey.setForeground(textGray);
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVal.setForeground(textDark);
        pnl.add(lblKey, BorderLayout.WEST);
        pnl.add(lblVal, BorderLayout.EAST);
        return pnl;
    }
}