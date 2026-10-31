package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class UI_SanPham extends JPanel {
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

        // Chiều cao ô nhập là 36px
        comp.setPreferredSize(new Dimension(width, 36));

        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(comp, BorderLayout.CENTER);

        pnl.setMinimumSize(new Dimension(width, 56));
        pnl.setPreferredSize(new Dimension(width, 56));

        return pnl;
    }

    private JButton createFilterButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgWhite);
        button.setForeground(colorBlue);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        button.setPreferredSize(new Dimension(115, 36));
        return button;
    }

    // Các nhãn thao tác chỉ phục vụ trình bày giao diện.
    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JLabel lblView;
        private JLabel lblEdit;

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 12));
            setOpaque(true);
            
            lblView = new JLabel("Xem");
            lblEdit = new JLabel("Sửa");
            lblView.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblView.setForeground(colorBlue);
            lblEdit.setForeground(colorBlue);
            
            // Sử dụng các biểu tượng hiện có trong dự án.
            try {
                java.net.URL urlView = getClass().getResource("/icon/ic_xemchitiet.png");
                if (urlView != null) {
                    Image imgView = new ImageIcon(urlView).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    lblView.setIcon(new ImageIcon(imgView));
                    lblView.setText("");
                } 
            } catch (Exception e) {}

            try {
                java.net.URL urlEdit = getClass().getResource("/icon/ic_chinhsua.png");
                if (urlEdit != null) {
                    Image imgEdit = new ImageIcon(urlEdit).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    lblEdit.setIcon(new ImageIcon(imgEdit));
                    lblEdit.setText("");
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

    public UI_SanPham() {
        setLayout(new BorderLayout(0, 15));
        setBackground(bgMain);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // ==========================================
        // 1. THANH TÌM KIẾM & LỌC 
        // ==========================================
        RoundedPanel pnlFilter = new RoundedPanel(12, bgWhite);
        pnlFilter.setLayout(new BorderLayout(0, 10)); 
        pnlFilter.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // --- HÀNG TRÊN (Chứa ô Search và Combobox) ---
        JPanel pnlFilterTop = new JPanel(new GridBagLayout());
        pnlFilterTop.setOpaque(false);
        pnlFilterTop.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        GridBagConstraints gbcTop = new GridBagConstraints();

        gbcTop.fill = GridBagConstraints.BOTH;
        gbcTop.anchor = GridBagConstraints.NORTH;
        gbcTop.insets = new Insets(0, 0, 10, 12);
        gbcTop.weighty = 1.0;

        // Khung Search
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
        
        pnlFilterTop.add(createFilterItem("Tìm theo tên sản phẩm hoặc SKU", pnlSearchBox, 360), gbcTop);

        gbcTop.gridwidth = 1;
        gbcTop.weightx = 1.0; 
        
        // [2] CHỈNH CHIỀU NGANG CÁC Ô COMBOBOX
        JComboBox<String> cbDanhMuc = new JComboBox<>(new String[]{"Tất cả", "Tủ lạnh", "Máy giặt", "Tivi", "Máy lạnh", "Laptop", "Điện thoại", "Gia dụng", "Phụ kiện"});
        cbDanhMuc.setBackground(Color.WHITE);
        gbcTop.gridx = 2;
        pnlFilterTop.add(createFilterItem("Danh mục", cbDanhMuc, 180), gbcTop);

        JComboBox<String> cbThuongHieu = new JComboBox<>(new String[]{"Tất cả", "Samsung", "LG", "Daikin", "Sony", "ASUS", "Lock&Lock"});
        cbThuongHieu.setBackground(Color.WHITE);
        gbcTop.gridx = 3;
        gbcTop.insets = new Insets(0, 0, 10, 0);
        pnlFilterTop.add(createFilterItem("Thương hiệu", cbThuongHieu, 180), gbcTop);

        JComboBox<String> cbGia = new JComboBox<>(new String[]{"Tất cả", "Dưới 5 triệu", "5 - 15 triệu", "Trên 15 triệu"});
        cbGia.setBackground(Color.WHITE);
        gbcTop.gridy = 1;
        gbcTop.gridx = 0;
        gbcTop.insets = new Insets(0, 0, 0, 12);
        pnlFilterTop.add(createFilterItem("Giá bán", cbGia, 180), gbcTop);
        
        JComboBox<String> cbBaoHanh = new JComboBox<>(new String[]{"Tất cả", "12 tháng", "24 tháng", "36 tháng"});
        cbBaoHanh.setBackground(Color.WHITE);
        gbcTop.gridx = 1;
        pnlFilterTop.add(createFilterItem("Bảo hành", cbBaoHanh, 180), gbcTop);

        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang kinh doanh", "Ngừng kinh doanh"});
        cbTrangThai.setBackground(Color.WHITE);
        gbcTop.gridx = 2;
        pnlFilterTop.add(createFilterItem("Trạng thái", cbTrangThai, 180), gbcTop);

        JPanel pnlFilterActions = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlFilterActions.setOpaque(false);
        JButton btnApplyFilter = createFilterButton("Áp dụng bộ lọc");
        btnApplyFilter.setBackground(colorBlue);
        btnApplyFilter.setForeground(Color.WHITE);
        btnApplyFilter.setContentAreaFilled(false);
        btnApplyFilter.setOpaque(true);
        btnApplyFilter.setBorderPainted(false);
        pnlFilterActions.add(btnApplyFilter);
        JButton btnClearFilter = createFilterButton("Xóa bộ lọc");
        btnClearFilter.setForeground(new Color(220, 38, 38));
        btnClearFilter.setBackground(new Color(254, 242, 242));
        btnClearFilter.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38)));
        btnClearFilter.setContentAreaFilled(false);
        btnClearFilter.setOpaque(true);
        pnlFilterActions.add(btnClearFilter);
        gbcTop.gridx = 3;
        gbcTop.insets = new Insets(0, 0, 0, 0);
        pnlFilterTop.add(createFilterItem(" ", pnlFilterActions, 240), gbcTop);

        // --- HÀNG DƯỚI (Chứa các nút chức năng) ---
        JPanel pnlFilterBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        pnlFilterBottom.setOpaque(false);

        JButton btnAdd = new JButton("Thêm sản phẩm");
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
        btnAdd.setPreferredSize(new Dimension(160, 36)); 

        JButton btnManage = new JButton("Quản lý danh mục");        
        try {
            java.net.URL manageURL = getClass().getResource("/icon/ic_danhmuc.png");
            if (manageURL != null) {
                Image imgManage = new ImageIcon(manageURL).getImage();
                Image scaledManage = imgManage.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                btnManage.setIcon(new ImageIcon(scaledManage));
                btnManage.setIconTextGap(8); 
            }
        } catch (Exception e) {}

        btnManage.setBackground(new Color(245, 247, 250));
        btnManage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnManage.setFocusPainted(false);
        btnManage.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btnManage.setPreferredSize(new Dimension(185, 36));

        pnlFilterBottom.add(btnAdd);
        pnlFilterBottom.add(Box.createHorizontalStrut(15)); 
        pnlFilterBottom.add(btnManage);
        
        pnlFilter.add(pnlFilterTop, BorderLayout.CENTER);
        pnlFilter.add(pnlFilterBottom, BorderLayout.SOUTH);

        add(pnlFilter, BorderLayout.NORTH);

        // ==========================================
        // 2. BẢNG DANH SÁCH SẢN PHẨM
        // ==========================================
        RoundedPanel pnlTableContainer = new RoundedPanel(12, bgWhite);
        pnlTableContainer.setLayout(new BorderLayout());
        pnlTableContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTableTitle = new JLabel("<html><span style='color:#1e3a8a; font-size:14px;'><b>Danh sách sản phẩm</b></span> <span style='color:#6b7280; font-size:11px;'>(Tổng: 125 sản phẩm)</span></html>");
        lblTableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlTableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] columns = {"STT", "Ảnh", "Tên sản phẩm", "Danh mục", "Thương hiệu", "SKU", "Giá bán", "Bảo hành", "Trạng thái", "Thao tác"};
        
        String tagKinhDoanh = "<html><span style='background-color:#dcfce7; color:#16a34a; padding:4px 10px; border-radius:12px;'>Đang kinh doanh</span></html>";
        String tagNgung = "<html><span style='background-color:#ffedd5; color:#ea580c; padding:4px 10px; border-radius:12px;'>Ngừng kinh doanh</span></html>";
        
        String tagTuLanh = "<html><span style='background-color:#e0f2fe; color:#0284c7; padding:4px 10px; border-radius:12px;'>Tủ lạnh</span></html>";
        String tagMayGiat = "<html><span style='background-color:#f3e8ff; color:#9333ea; padding:4px 10px; border-radius:12px;'>Máy giặt</span></html>";
        String tagTivi = "<html><span style='background-color:#ffedd5; color:#ea580c; padding:4px 10px; border-radius:12px;'>Tivi</span></html>";
        String tagDieuHoa = "<html><span style='background-color:#dcfce7; color:#16a34a; padding:4px 10px; border-radius:12px;'>Máy lạnh</span></html>";
        String tagLaptop = "<html><span style='background-color:#fee2e2; color:#dc2626; padding:4px 10px; border-radius:12px;'>Laptop</span></html>";

        // Cột cuối cùng để giá trị trống ("") vì đã có ActionRenderer vẽ icon đè lên
        Object[][] data = {
            {"1", "", "Tủ lạnh Samsung Inverter 236 lít", tagTuLanh, "Samsung", "TL001", "12.000.000 đ", "24 tháng", tagKinhDoanh, ""},
            {"2", "", "Máy giặt LG Inverter FV1408", tagMayGiat, "LG", "MG001", "8.500.000 đ", "12 tháng", tagKinhDoanh, ""},
            {"3", "", "Smart Tivi Samsung 55 inch", tagTivi, "Samsung", "TV001", "15.990.000 đ", "24 tháng", tagKinhDoanh, ""},
            {"4", "", "Máy lạnh Daikin Inverter 1 HP", tagDieuHoa, "Daikin", "ML001", "13.500.000 đ", "24 tháng", tagKinhDoanh, ""},
            {"5", "", "Laptop ASUS VivoBook 15", tagLaptop, "ASUS", "LT001", "14.990.000 đ", "24 tháng", tagKinhDoanh, ""},
            {"6", "", "Điện thoại Samsung Galaxy A35", "<html><span style='color:#9333ea;'>Điện thoại</span></html>", "Samsung", "DT001", "9.990.000 đ", "12 tháng", tagKinhDoanh, ""},
            {"7", "", "Nồi chiên không dầu Lock&Lock 5 lít", "<html><span style='color:#dc2626;'>Gia dụng</span></html>", "Lock&Lock", "GD001", "2.190.000 đ", "12 tháng", tagNgung, ""},
            {"8", "", "Tai nghe Sony WH-CH520", "<html><span style='color:#0284c7;'>Phụ kiện</span></html>", "Sony", "PK001", "1.490.000 đ", "12 tháng", tagKinhDoanh, ""}
        };

        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

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
        int[] widths = {32, 50, 245, 88, 98, 65, 110, 83, 147, 80};
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
        java.net.URL imageURL = getClass().getResource("/icon/ic_sanpham.png");
        Icon productIcon = imageURL == null ? null : new ImageIcon(new ImageIcon(imageURL).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        table.getColumnModel().getColumn(1).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, column);
                setText(productIcon == null ? "—" : "");
                setIcon(productIcon);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });
        
        // GẮN RENDERER VÀO CỘT THAO TÁC (Cột cuối cùng - index 9)
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
        pnlPagination.setBorder(new EmptyBorder(15, 0, 0, 0)); 
        
        JLabel lblInfo = new JLabel("Hiển thị 1 - 8 trong tổng số 125 sản phẩm");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(textGray);
        pnlPagination.add(lblInfo, BorderLayout.WEST);

        JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlPages.setBackground(bgWhite);
        
        String[] pages = {"<", "1", "2", "3", "...", "16", ">"};
        for (String p : pages) {
            JButton btnPage = new JButton(p);
            btnPage.setFocusPainted(false);
            btnPage.setPreferredSize(new Dimension(32, 32));
            btnPage.setMargin(new Insets(0,0,0,0));
            btnPage.setEnabled(!p.equals("<") && !p.equals("..."));
            
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
}
