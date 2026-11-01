package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class PNL_KhuyenMai extends JPanel {
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

    // ==========================================
    // RENDERER ĐỂ HIỂN THỊ 2 ICON TRONG CỘT THAO TÁC
    // ==========================================
    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JLabel lblView;
        private JLabel lblEdit;

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
            setOpaque(true);
            
            lblView = new JLabel();
            lblEdit = new JLabel();
            
            try {
                java.net.URL urlView = getClass().getResource("/icon/ic_xemchitiet.png");
                if (urlView != null) {
                    Image imgView = new ImageIcon(urlView).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    lblView.setIcon(new ImageIcon(imgView));
                } 
            } catch (Exception e) {}

            try {
                java.net.URL urlEdit = getClass().getResource("/icon/ic_chinhsua.png");
                if (urlEdit != null) {
                    Image imgEdit = new ImageIcon(urlEdit).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    lblEdit.setIcon(new ImageIcon(imgEdit));
                } 
            } catch (Exception e) {}

            add(lblView);
            add(lblEdit);
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

    public PNL_KhuyenMai() {
        setLayout(new BorderLayout(0, 15));
        setBackground(bgMain);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // ==========================================
        // 1. THANH TÌM KIẾM & LỌC
        // ==========================================
        RoundedPanel pnlFilter = new RoundedPanel(12, bgWhite);
        pnlFilter.setLayout(new BorderLayout(0, 10)); 
        pnlFilter.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel pnlFilterTop = new JPanel(new GridBagLayout());
        pnlFilterTop.setOpaque(false);
        pnlFilterTop.setBorder(new EmptyBorder(0, 0, 5, 0));
        pnlFilterTop.setMinimumSize(new Dimension(0, 60));
        pnlFilterTop.setPreferredSize(new Dimension(0, 60));
        
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.fill = GridBagConstraints.BOTH;
        gbcTop.anchor = GridBagConstraints.NORTH;
        gbcTop.insets = new Insets(0, 0, 0, 12);
        gbcTop.weighty = 1.0;

        JPanel pnlSearchBox = new JPanel(new BorderLayout(5, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)), 
            new EmptyBorder(0, 10, 0, 10)
        ));

        // Thêm icon vào ô tìm kiếm
        JLabel lblSearchIcon = new JLabel();
        try {
            java.net.URL searchURL = getClass().getResource("/icon/ic_timkiem.png"); 
            if (searchURL != null) {
                Image imgSearch = new ImageIcon(searchURL).getImage();
                Image scaledSearch = imgSearch.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                lblSearchIcon.setIcon(new ImageIcon(scaledSearch));
            } 
        } catch (Exception e) {}

        JTextField txtSearch = new JTextField(" Tìm theo mã hoặc tên khuyến mãi...");
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(null); 
        txtSearch.setBackground(Color.WHITE);

        pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);
        
        gbcTop.gridx = 0;
        gbcTop.weightx = 2.5; 
        pnlFilterTop.add(createFilterItem(" ", pnlSearchBox, 260), gbcTop);

        gbcTop.weightx = 1.0; 
        
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang diễn ra", "Sắp diễn ra", "Đã kết thúc"});
        cbStatus.setBackground(Color.WHITE);
        gbcTop.gridx = 1;
        pnlFilterTop.add(createFilterItem("Trạng thái", cbStatus, 130), gbcTop);

        JComboBox<String> cbType = new JComboBox<>(new String[]{"Tất cả", "Giảm theo %", "Giảm tiền mặt", "Tặng sản phẩm"});
        cbType.setBackground(Color.WHITE);
        gbcTop.gridx = 2;
        pnlFilterTop.add(createFilterItem("Loại khuyến mãi", cbType, 130), gbcTop);

        JComboBox<String> cbValue = new JComboBox<>(new String[]{"Tất cả", "Dưới 50k", "50k - 200k", "Trên 200k"});
        cbValue.setBackground(Color.WHITE);
        gbcTop.gridx = 3;
        pnlFilterTop.add(createFilterItem("Giá trị giảm", cbValue, 130), gbcTop);
        
        JTextField txtFromDate = new JTextField("01/11/2026");
        txtFromDate.setBackground(Color.WHITE);
        gbcTop.gridx = 4;
        pnlFilterTop.add(createFilterItem("Từ ngày", txtFromDate, 110), gbcTop);

        JTextField txtToDate = new JTextField("30/11/2026");
        txtToDate.setBackground(Color.WHITE);
        gbcTop.gridx = 5;
        gbcTop.insets = new Insets(0, 0, 0, 0); 
        pnlFilterTop.add(createFilterItem("Đến ngày", txtToDate, 110), gbcTop);

        JPanel pnlFilterBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        pnlFilterBottom.setOpaque(false);
        pnlFilterBottom.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        JButton btnAdd = new JButton("Thêm khuyến mãi");
        // Thêm icon vào nút Thêm khuyến mãi
        try {
            java.net.URL addURL = getClass().getResource("/icon/ic_them.png"); 
            if (addURL != null) {
                Image imgAdd = new ImageIcon(addURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                btnAdd.setIcon(new ImageIcon(imgAdd));
                btnAdd.setIconTextGap(8); 
            }
        } catch (Exception e) {}
        
        btnAdd.setBackground(colorBlue);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setPreferredSize(new Dimension(175, 36)); 
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlFilterBottom.add(btnAdd);
        
        pnlFilter.add(pnlFilterTop, BorderLayout.CENTER);
        pnlFilter.add(pnlFilterBottom, BorderLayout.SOUTH);

        add(pnlFilter, BorderLayout.NORTH);

        // ==========================================
        // 2. BẢNG DANH SÁCH KHUYẾN MÃI
        // ==========================================
        RoundedPanel pnlTableContainer = new RoundedPanel(12, bgWhite);
        pnlTableContainer.setLayout(new BorderLayout());
        pnlTableContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTableTitle = new JLabel("<html><span style='color:#1e3a8a; font-size:14px;'><b>Danh sách khuyến mãi</b></span> <span style='color:#6b7280; font-size:11px;'>(Tổng: 24 chương trình)</span></html>");
        lblTableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlTableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] columns = {"STT", "Mã KM", "Tên chương trình", "Loại KM", "Giá trị giảm", "Thời gian áp dụng", "Trạng thái", "Thao tác"};
        
        String tagPhanTram = "<html><span style='background-color:#e0f2fe; color:#0284c7; padding:4px 10px; border-radius:12px;'>Giảm theo %</span></html>";
        String tagTienMat = "<html><span style='background-color:#f3e8ff; color:#9333ea; padding:4px 10px; border-radius:12px;'>Giảm tiền mặt</span></html>";
        String tagTangSP = "<html><span style='background-color:#fee2e2; color:#dc2626; padding:4px 10px; border-radius:12px;'>Tặng sản phẩm</span></html>";

        String tagDangDienRa = "<html><span style='background-color:#dcfce7; color:#16a34a; padding:4px 10px; border-radius:12px;'>Đang diễn ra</span></html>";
        String tagSapDienRa = "<html><span style='background-color:#e0f2fe; color:#0284c7; padding:4px 10px; border-radius:12px;'>Sắp diễn ra</span></html>";
        String tagKetThuc = "<html><span style='background-color:#f1f5f9; color:#64748b; padding:4px 10px; border-radius:12px;'>Đã kết thúc</span></html>";

        Object[][] data = {
            {"1", "KM001", "Khai trương chi nhánh", tagPhanTram, "10%", "01/11/2026 - 15/11/2026", tagDangDienRa, ""},
            {"2", "KM002", "Ưu đãi cuối tuần", tagTienMat, "50.000đ", "31/10/2026 - 02/11/2026", tagSapDienRa, ""},
            {"3", "KM003", "Tri ân khách hàng", tagPhanTram, "15%", "01/10/2026 - 20/10/2026", tagKetThuc, ""},
            {"4", "KM004", "Mua 2 tặng 1", tagTangSP, "Mua 2 tặng 1", "05/11/2026 - 10/11/2026", tagSapDienRa, ""}
        };

        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(bgHover);
        table.setSelectionForeground(textDark);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(textDark);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(75);  // Mã KM
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Tên chương trình
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Loại KM
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Giá trị giảm
        table.getColumnModel().getColumn(5).setPreferredWidth(170); // Thời gian áp dụng
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // Trạng thái
        table.getColumnModel().getColumn(7).setPreferredWidth(85);  // Thao tác

        // Gắn ActionRenderer (hiển thị 2 icon xem & sửa) cho cột cuối cùng
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(bgWhite);
        pnlTableContainer.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. PHÂN TRANG (Pagination)
        // ==========================================
        JPanel pnlPagination = new JPanel(new BorderLayout());
        pnlPagination.setBackground(bgWhite);
        pnlPagination.setBorder(new EmptyBorder(35, 0, 0, 0)); 
        
        JLabel lblInfo = new JLabel("Hiển thị 1 - 4 trong tổng số 24 chương trình");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(textGray);
        pnlPagination.add(lblInfo, BorderLayout.WEST);

        JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlPages.setBackground(bgWhite);
        
        String[] pages = {"<", "1", "2", "3", "4", "5", ">"};
        for (String p : pages) {
            JButton btnPage = new JButton(p);
            btnPage.setFocusPainted(false);
            btnPage.setPreferredSize(new Dimension(32, 32));
            btnPage.setMargin(new Insets(0,0,0,0));
            
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
        
        pnlPagination.add(pnlPages, BorderLayout.EAST);
        pnlTableContainer.add(pnlPagination, BorderLayout.SOUTH);

        add(pnlTableContainer, BorderLayout.CENTER);
    }

    private JPanel createFilterItem(String title, JComponent comp, int width) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);

        JLabel lbl = new JLabel(title.trim().isEmpty() ? " " : title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(textDark);

        comp.setPreferredSize(new Dimension(width, 36));

        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(comp, BorderLayout.CENTER);

        pnl.setMinimumSize(new Dimension(width, 56));
        pnl.setPreferredSize(new Dimension(width, 56));

        return pnl;
    }
}