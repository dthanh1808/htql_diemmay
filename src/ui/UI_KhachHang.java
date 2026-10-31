package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class UI_KhachHang extends JPanel {
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

    private JPanel createFilterItem(String title, JComponent comp, int width) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        
        JLabel lbl = new JLabel(title.trim().isEmpty() ? " " : title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(textDark);
        
        Dimension dim = new Dimension(width, 36);
        comp.setPreferredSize(dim);
        comp.setMinimumSize(dim); 
        comp.setMaximumSize(dim);
        
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(comp, BorderLayout.CENTER);
        pnl.setPreferredSize(new Dimension(width, 56));
        pnl.setMinimumSize(new Dimension(width, 56));
        return pnl;
    }

    private JButton createButton(String text, String iconPath, int width, boolean primary) {
        JButton button = new JButton(text);
        java.net.URL url = iconPath == null ? null : getClass().getResource(iconPath);
        if (url != null) {
            java.awt.image.BufferedImage iconImage = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = iconImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(new ImageIcon(url).getImage(), 0, 0, 16, 16, null);
            if (primary) {
                graphics.setComposite(AlphaComposite.SrcIn);
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, 16, 16);
            }
            graphics.dispose();
            button.setIcon(new ImageIcon(iconImage));
            button.setIconTextGap(8);
        }
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(primary ? colorBlue : bgWhite);
        button.setForeground(primary ? Color.WHITE : colorBlue);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(primary ? colorBlue : new Color(210, 210, 210)));
        if (primary) {
            // Giữ nền xanh khi giao diện hệ thống vẽ nút theo chủ đề Windows.
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.setBorderPainted(false);
        }
        button.setPreferredSize(new Dimension(width, 36));
        return button;
    }

    // Các nhãn thao tác chỉ phục vụ trình bày giao diện.
    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JLabel lblView;
        private JLabel lblEdit;
        private JLabel lblHistory;

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 12));
            setOpaque(true);
            
            lblView = new JLabel("Xem");
            lblEdit = new JLabel("Sửa");
            lblHistory = new JLabel("Lịch sử");
            for (JLabel label : new JLabel[]{lblView, lblEdit, lblHistory}) {
                label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                label.setForeground(colorBlue);
            }
            
            try {
                java.net.URL urlView = getClass().getResource("/icon/ic_xemchitiet.png");
                if (urlView != null) {
                    Image imgView = new ImageIcon(urlView).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    lblView.setIcon(new ImageIcon(imgView));
                    lblView.setText("");
                } 
            } catch (Exception e) {}

            java.net.URL editURL = getClass().getResource("/icon/ic_chinhsua.png");
            if (editURL != null) {
                lblEdit.setIcon(new ImageIcon(new ImageIcon(editURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
                lblEdit.setText("");
            }

            add(lblView);
            add(lblEdit);
            add(lblHistory);
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

    public UI_KhachHang() {
        setLayout(new BorderLayout(0, 15));
        setBackground(bgMain);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // ==========================================
        // 1. THANH TÌM KIẾM & BỘ LỌC
        // ==========================================
        RoundedPanel pnlFilter = new RoundedPanel(12, bgWhite);
        pnlFilter.setLayout(new BorderLayout(0, 10)); 
        pnlFilter.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel pnlFilterTop = new JPanel(new GridBagLayout());
        pnlFilterTop.setOpaque(false);
        
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.fill = GridBagConstraints.BOTH; 
        gbcTop.anchor = GridBagConstraints.NORTH;
        gbcTop.insets = new Insets(0, 0, 10, 12); 

        // Ô tìm kiếm
        JPanel pnlSearchBox = new JPanel(new BorderLayout(5, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)), 
            new EmptyBorder(0, 10, 0, 10)
        ));

        JLabel lblSearchIcon = new JLabel();
        try {
            java.net.URL searchURL = getClass().getResource("/icon/ic_timkiem.png"); 
            if (searchURL != null) {
                Image imgSearch = new ImageIcon(searchURL).getImage();
                Image scaledSearch = imgSearch.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                lblSearchIcon.setIcon(new ImageIcon(scaledSearch));
            } 
        } catch (Exception e) {}

        JTextField txtSearch = new JTextField();
        txtSearch.setForeground(textDark);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(null); 
        txtSearch.setBackground(Color.WHITE);

        pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);
        
        gbcTop.gridx = 0;
        gbcTop.gridwidth = 2;
        gbcTop.weightx = 2.5; 
        pnlFilterTop.add(createFilterItem("Tìm theo mã KH, họ tên, số điện thoại hoặc email", pnlSearchBox, 360), gbcTop);

        // Combobox Hạng thành viên
        gbcTop.weightx = 1.0; 
        gbcTop.gridwidth = 1;
        JComboBox<String> cbHangTV = new JComboBox<>(new String[]{"Tất cả", "Diamond", "Gold", "Silver", "Member"});
        cbHangTV.setBackground(Color.WHITE);
        cbHangTV.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbcTop.gridx = 2;
        pnlFilterTop.add(createFilterItem("Hạng thành viên", cbHangTV, 180), gbcTop);

        // Combobox Trạng thái
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang hoạt động", "Ngừng hoạt động"});
        cbTrangThai.setBackground(Color.WHITE);
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbcTop.gridx = 3;
        gbcTop.insets = new Insets(0, 0, 10, 0);
        pnlFilterTop.add(createFilterItem("Trạng thái", cbTrangThai, 180), gbcTop);

        // Ngày đăng ký
        JTextField txtFromDate = new JTextField();
        JTextField txtToDate = new JTextField();
        txtFromDate.setForeground(textDark);
        txtToDate.setForeground(textDark);
        txtFromDate.setBackground(Color.WHITE);
        txtToDate.setBackground(Color.WHITE);
        txtFromDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtToDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFromDate.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)), new EmptyBorder(0, 10, 0, 10)));
        txtToDate.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)), new EmptyBorder(0, 10, 0, 10)));
        gbcTop.gridy = 1;
        gbcTop.gridx = 0;
        gbcTop.insets = new Insets(0, 0, 0, 12);
        pnlFilterTop.add(createFilterItem("Đăng ký từ ngày (dd/MM/yyyy)", txtFromDate, 180), gbcTop);
        gbcTop.gridx = 1;
        pnlFilterTop.add(createFilterItem("Đến ngày (dd/MM/yyyy)", txtToDate, 180), gbcTop);

        JPanel pnlFilterActions = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlFilterActions.setOpaque(false);
        pnlFilterActions.add(createButton("Áp dụng bộ lọc", "/icon/ic_loc.png", 150, true));
        JButton btnClearFilter = createButton("Xóa bộ lọc", null, 125, false);
        btnClearFilter.setForeground(new Color(220, 38, 38));
        btnClearFilter.setBackground(new Color(254, 242, 242));
        btnClearFilter.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38)));
        btnClearFilter.setContentAreaFilled(false);
        btnClearFilter.setOpaque(true);
        pnlFilterActions.add(btnClearFilter);
        gbcTop.gridx = 2;
        gbcTop.gridwidth = 2;
        gbcTop.insets = new Insets(0, 0, 0, 0);
        pnlFilterTop.add(createFilterItem(" ", pnlFilterActions, 360), gbcTop);

        JPanel pnlFilterBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        pnlFilterBottom.setOpaque(false);

        pnlFilterBottom.add(createButton("Đăng ký khách hàng", "/icon/ic_them.png", 185, true));
        pnlFilterBottom.add(Box.createHorizontalStrut(12));
        pnlFilterBottom.add(createButton("Chính sách hạng thành viên", "/icon/ic_khachhang.png", 235, false));
        pnlFilterBottom.add(Box.createHorizontalStrut(12));
        pnlFilterBottom.add(createButton("Gửi khuyến mãi", "/icon/ic_khuyenmai.png", 165, false));

        pnlFilter.add(pnlFilterTop, BorderLayout.CENTER);
        pnlFilter.add(pnlFilterBottom, BorderLayout.SOUTH);

        add(pnlFilter, BorderLayout.NORTH);

        // ==========================================
        // 2. BẢNG DANH SÁCH KHÁCH HÀNG
        // ==========================================
        RoundedPanel pnlTableContainer = new RoundedPanel(12, bgWhite);
        pnlTableContainer.setLayout(new BorderLayout());
        pnlTableContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlTableTop = new JPanel(new BorderLayout());
        pnlTableTop.setOpaque(false);
        JLabel lblTableTitle = new JLabel("<html><span style='color:#1e3a8a; font-size:14px;'><b>Danh sách khách hàng</b></span> <span style='color:#6b7280; font-size:11px;'>(Tổng: 124 khách hàng)</span></html>");
        
        JButton btnExport = new JButton("Xuất file");
        try {
            java.net.URL exportURL = getClass().getResource("/icon/ic_xuatfile.png");
            if (exportURL != null) {
                Image imgExport = new ImageIcon(exportURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                btnExport.setIcon(new ImageIcon(imgExport));
                btnExport.setIconTextGap(8);
            }
        } catch (Exception e) {}
        btnExport.setBackground(bgWhite);
        btnExport.setForeground(colorBlue);
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExport.setFocusPainted(false);
        btnExport.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        btnExport.setPreferredSize(new Dimension(100, 32));

        pnlTableTop.add(lblTableTitle, BorderLayout.WEST);
        pnlTableTop.add(btnExport, BorderLayout.EAST);
        pnlTableTop.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlTableContainer.add(pnlTableTop, BorderLayout.NORTH);

        String[] columns = {"STT", "Mã KH", "Họ và tên", "SĐT", "Email", "Hạng TV", "Tổng chi tiêu", "Ngày đăng ký", "Trạng thái", "Thao tác"};
        
        // Dữ liệu mẫu để trình bày danh sách.
        String tagDiamond = "<html><span style='background-color:#f3e8ff; color:#9333ea;'>Diamond</span></html>";
        String tagGold = "<html><span style='background-color:#ffedd5; color:#ea580c;'>Gold</span></html>";
        String tagSilver = "<html><span style='background-color:#e0f2fe; color:#0284c7;'>Silver</span></html>";
        String tagMember = "<html><span style='background-color:#f1f5f9; color:#475569;'>Member</span></html>";
        
        String tagActive = "<html><span style='background-color:#dcfce7; color:#16a34a;'>Đang hoạt động</span></html>";
        String tagInactive = "<html><span style='background-color:#fee2e2; color:#dc2626;'>Ngừng hoạt động</span></html>";

        Object[][] data = {
            {"1", "KH000123", "Mai Thanh Lan", "0903 458 789", "lanmai@gmail.com", tagDiamond, "12.850.000 đ", "12/03/2024", tagActive, ""},
            {"2", "KH000124", "Nguyễn Minh Tú", "0987 224 510", "tuminh@gmail.com", tagGold, "3.860.000 đ", "15/03/2024", tagActive, ""},
            {"3", "KH000125", "Phạm Quốc Huy", "0902 615 437", "huypham@gmail.com", tagSilver, "1.920.000 đ", "20/03/2024", tagActive, ""},
            {"4", "KH000126", "Trần Hoài Nam", "0948 118 228", "namtran@gmail.com", tagMember, "460.000 đ", "22/03/2024", tagActive, ""},
            {"5", "KH000127", "Hoàng Ngọc Liên", "0905 820 354", "lienhoang@gmail.com", tagGold, "4.520.000 đ", "25/03/2024", tagInactive, ""},
            {"6", "KH000128", "Lê Quang Dũng", "0869 741 982", "dungle@gmail.com", tagSilver, "1.890.000 đ", "27/03/2024", tagActive, ""},
            {"7", "KH000129", "Vũ Phương Anh", "0906 204 165", "anhvu@gmail.com", tagMember, "785.000 đ", "28/03/2024", tagActive, ""},
            {"8", "KH000130", "Đỗ Trung Kiên", "0913 507 903", "kiendo@gmail.com", tagSilver, "2.060.000 đ", "30/03/2024", tagActive, ""}
        };

        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getParent() != null && getPreferredSize().width <= getParent().getWidth();
            }
        };
        
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(bgHover);
        table.setSelectionForeground(textDark);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(textDark);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {30, 72, 140, 100, 160, 80, 104, 89, 123, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        javax.swing.table.DefaultTableCellRenderer textRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, column);
                setBorder(new EmptyBorder(0, 6, 0, 6));
                setHorizontalAlignment(column == 6 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, textRenderer);
        
        // Gắn ActionRenderer vào cột Thao tác cuối cùng
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(bgWhite);
        pnlTableContainer.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. PHÂN TRANG (Pagination)
        // ==========================================
        JPanel pnlPagination = new JPanel(new BorderLayout());
        pnlPagination.setBackground(bgWhite);
        pnlPagination.setBorder(new EmptyBorder(20, 0, 0, 0)); 
        
        JLabel lblInfo = new JLabel("Hiển thị 1 - 8 trong tổng số 124 khách hàng");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(textGray);
        pnlPagination.add(lblInfo, BorderLayout.WEST);

        JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlPages.setBackground(bgWhite);
        
        JButton btnPrev = new JButton("<");
        btnPrev.setFocusPainted(false);
        btnPrev.setPreferredSize(new Dimension(32, 32));
        btnPrev.setBackground(bgWhite);
        btnPrev.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        btnPrev.setEnabled(false);
        pnlPages.add(btnPrev);

        String[] pages = {"1", "2", "3", "...", "16"};
        for (String p : pages) {
            JButton btnPage = new JButton(p);
            btnPage.setFocusPainted(false);
            btnPage.setPreferredSize(new Dimension(32, 32));
            btnPage.setMargin(new Insets(0,0,0,0));
            btnPage.setEnabled(!p.equals("..."));
            
            if (p.equals("1")) { 
                btnPage.setBackground(colorBlue);
                btnPage.setForeground(Color.WHITE);
                btnPage.setBorderPainted(false);
            } else {
                btnPage.setBackground(bgWhite);
                btnPage.setForeground(textDark);
                btnPage.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            }
            pnlPages.add(btnPage);
        }

        JButton btnNext = new JButton(">");
        btnNext.setFocusPainted(false);
        btnNext.setPreferredSize(new Dimension(32, 32));
        btnNext.setBackground(bgWhite);
        btnNext.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        pnlPages.add(btnNext);
        
        pnlPagination.add(pnlPages, BorderLayout.EAST);
        pnlTableContainer.add(pnlPagination, BorderLayout.SOUTH);

        add(pnlTableContainer, BorderLayout.CENTER);
    }
}
