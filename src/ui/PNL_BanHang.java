package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class PNL_BanHang extends JPanel {
    private Color bgMain = new Color(245, 247, 250);
    private Color bgWhite = Color.WHITE;
    private Color textDark = new Color(30, 30, 30);
    private Color textGray = new Color(130, 130, 130);
    private Color colorBlue = new Color(37, 99, 235);
    private Color bgHover = new Color(239, 246, 255);
    private JPanel pnlCustCenter;

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

    // Renderer cho cột số lượng (- [ 1 ] +)
    class QuantityRenderer extends JPanel implements TableCellRenderer {
        private JButton btnMinus = new JButton("-");
        private JTextField txtQty = new JTextField("1");
        private JButton btnPlus = new JButton("+");

        public QuantityRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 10));
            setOpaque(true);
            setBackground(Color.WHITE);

            btnMinus.setPreferredSize(new Dimension(24, 24));
            btnPlus.setPreferredSize(new Dimension(24, 24));
            txtQty.setPreferredSize(new Dimension(30, 24));
            txtQty.setHorizontalAlignment(JTextField.CENTER);
            txtQty.setFont(new Font("Segoe UI", Font.BOLD, 11));

            for (JButton b : new JButton[]{btnMinus, btnPlus}) {
                b.setFocusPainted(false);
                b.setBackground(new Color(248, 250, 252));
                b.setForeground(colorBlue);
                b.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }

            add(btnMinus);
            add(txtQty);
            add(btnPlus);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) {
                txtQty.setText(value.toString());
            }
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    // Renderer cho cột Thao tác (icon thùng rác)
    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JLabel lblDelete;

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
            setOpaque(true);
            lblDelete = new JLabel();
            try {
                java.net.URL url = getClass().getResource("/icon/ic_xoa.png");
                if (url != null) {
                    Image img = new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    lblDelete.setIcon(new ImageIcon(img));
                }
            } catch(Exception e) {}
            add(lblDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) setBackground(table.getSelectionBackground());
            else setBackground(table.getBackground());
            return this;
        }
    }

    public PNL_BanHang() {
        setLayout(new BorderLayout(15, 0));
        setBackground(bgMain);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // =========================================================================
        // PHẦN BÊN TRÁI: KHU VỰC TÌM KIẾM, KHÁCH HÀNG VÀ BẢNG SẢN PHẨM
        // =========================================================================
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setOpaque(false);

        // 1. THÔNG TIN KHÁCH HÀNG
        RoundedPanel pnlCustomer = new RoundedPanel(12, bgWhite);
        pnlCustomer.setLayout(new BorderLayout(0, 10));
        pnlCustomer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlCustTop = new JPanel(new BorderLayout());
        pnlCustTop.setOpaque(false);
        JLabel lblCustTitle = new JLabel("Thông tin khách hàng");
        lblCustTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCustTitle.setForeground(textDark);

        JPanel pnlCustActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlCustActions.setOpaque(false);
        
        JRadioButton radVangLai = new JRadioButton("Khách hàng vãng lai");
        radVangLai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radVangLai.setOpaque(false);
        radVangLai.setFocusPainted(false);

        JButton btnAddCust = new JButton(" Đăng ký khách hàng mới");
        try {
            java.net.URL addURL = getClass().getResource("/icon/ic_them.png"); 
            if (addURL != null) {
                Image imgAdd = new ImageIcon(addURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                btnAddCust.setIcon(new ImageIcon(imgAdd));
                btnAddCust.setIconTextGap(8); 
            }
        } catch (Exception e) {}
        btnAddCust.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddCust.setBackground(colorBlue);
        btnAddCust.setForeground(Color.WHITE);
        btnAddCust.setFocusPainted(false);
        btnAddCust.setBorderPainted(false);

        pnlCustActions.add(radVangLai);
        pnlCustActions.add(btnAddCust);
        pnlCustTop.add(lblCustTitle, BorderLayout.WEST);
        pnlCustTop.add(pnlCustActions, BorderLayout.EAST);
        pnlCustomer.add(pnlCustTop, BorderLayout.NORTH);

        // Ô tìm kiếm khách hàng
        JPanel pnlCustSearch = new JPanel(new BorderLayout(5, 0));
        pnlCustSearch.setBackground(Color.WHITE);
        pnlCustSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            new EmptyBorder(0, 10, 0, 10)
        ));
        JLabel lblSearchCustIcon = new JLabel();
        try {
            java.net.URL searchURL = getClass().getResource("/icon/ic_timkiem.png"); 
            if (searchURL != null) {
                Image imgSearch = new ImageIcon(searchURL).getImage();
                Image scaledSearch = imgSearch.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                lblSearchCustIcon.setIcon(new ImageIcon(scaledSearch));
            } 
        } catch (Exception e) {}
        
        JTextField txtSearchCust = new JTextField(" Tìm theo tên, SĐT, mã khách hàng...");
        txtSearchCust.setBorder(null);
        txtSearchCust.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchCust.setForeground(Color.GRAY);
        pnlCustSearch.add(lblSearchCustIcon, BorderLayout.WEST);
        pnlCustSearch.add(txtSearchCust, BorderLayout.CENTER);
        pnlCustSearch.setPreferredSize(new Dimension(0, 36));

        pnlCustCenter = new JPanel(new BorderLayout(0, 10));
        pnlCustCenter.setOpaque(false);
        pnlCustCenter.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlCustCenter.add(pnlCustSearch, BorderLayout.NORTH);

        // Thẻ thông tin Mai Thanh Lan
        JPanel pnlCustSelected = new JPanel(new BorderLayout());
        pnlCustSelected.setBackground(new Color(248, 250, 252));
        pnlCustSelected.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel pnlCustInfoLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlCustInfoLeft.setOpaque(false);
        JLabel lblAvatar = new JLabel("ML", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(40, 40));
        lblAvatar.setOpaque(false); 
        lblAvatar.setBackground(new Color(253, 230, 238));
        lblAvatar.setForeground(new Color(219, 39, 119));
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel pnlCustDetails = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlCustDetails.setOpaque(false);
        JLabel lblCustName = new JLabel("Mai Thanh Lan");
        lblCustName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblCustID = new JLabel("KH000123");
        lblCustID.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCustID.setForeground(textGray);
        pnlCustDetails.add(lblCustName);
        pnlCustDetails.add(lblCustID);

        pnlCustInfoLeft.add(lblAvatar);
        pnlCustInfoLeft.add(pnlCustDetails);

        JPanel pnlCustInfoCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        pnlCustInfoCenter.setOpaque(false);
        JPanel pnlContact = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlContact.setOpaque(false);
        JLabel lblPhone = new JLabel("📞 0903 458 789");
        JLabel lblEmail = new JLabel("✉ lanmai@gmail.com");
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlContact.add(lblPhone);
        pnlContact.add(lblEmail);
        pnlCustInfoCenter.add(pnlContact);

        JPanel pnlCustInfoRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnlCustInfoRight.setOpaque(false);
        JLabel lblBadgeDiamond = new JLabel("<html><span style='background-color:#f3e8ff; color:#9333ea; padding:3px 10px; border-radius:10px;'><b>Diamond</b></span></html>");
        JPanel pnlPoints = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlPoints.setOpaque(false);
        JLabel lblPointsVal = new JLabel("12.850.000 đ");
        lblPointsVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPointsVal.setForeground(colorBlue);
        JLabel lblPointsLabel = new JLabel("Chi tiêu tích lũy");
        lblPointsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblPointsLabel.setForeground(textGray);
        pnlPoints.add(lblPointsVal);
        pnlPoints.add(lblPointsLabel);

        JButton btnClearCust = new JButton("✕");
        btnClearCust.setBorderPainted(false);
        btnClearCust.setContentAreaFilled(false);
        btnClearCust.setForeground(textGray);

        pnlCustInfoRight.add(lblBadgeDiamond);
        pnlCustInfoRight.add(pnlPoints);
        pnlCustInfoRight.add(btnClearCust);

        pnlCustSelected.add(pnlCustInfoLeft, BorderLayout.WEST);
        pnlCustSelected.add(pnlCustInfoCenter, BorderLayout.CENTER);
        pnlCustSelected.add(pnlCustInfoRight, BorderLayout.EAST);
        
        pnlCustCenter.add(pnlCustSelected, BorderLayout.SOUTH);
        pnlCustomer.add(pnlCustCenter, BorderLayout.CENTER);
        
        pnlCustomer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        pnlLeft.add(pnlCustomer);
        pnlLeft.add(Box.createVerticalStrut(12));

        // Sự kiện Radio ẩn thông tin khách
        radVangLai.addItemListener(e -> {
            pnlCustCenter.setVisible(!radVangLai.isSelected());
            revalidate();
            repaint();
        });


        // 2. KHUNG CHUNG: SẢN PHẨM TRONG HÓA ĐƠN
        RoundedPanel pnlProductSection = new RoundedPanel(12, bgWhite);
        pnlProductSection.setLayout(new BorderLayout(0, 12));
        pnlProductSection.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Tiêu đề khung ---
        JLabel lblSectionTitle = new JLabel("Sản phẩm trong hóa đơn");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSectionTitle.setForeground(textDark);
        pnlProductSection.add(lblSectionTitle, BorderLayout.NORTH);

        // --- Khu vực bên trong khung ---
        JPanel pnlProductInner = new JPanel(new BorderLayout(0, 12));
        pnlProductInner.setOpaque(false);

        // Thanh tìm kiếm và quét mã vạch
        JPanel pnlUnifiedSearch = new JPanel(new BorderLayout(10, 0));
        pnlUnifiedSearch.setBackground(Color.WHITE);
        pnlUnifiedSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            new EmptyBorder(0, 10, 0, 5)
        ));
        
        JLabel lblSearchIcon = new JLabel(); 
        try {
            java.net.URL searchURL = getClass().getResource("/icon/ic_timkiem.png"); 
            if (searchURL != null) {
                Image imgSearch = new ImageIcon(searchURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                lblSearchIcon.setIcon(new ImageIcon(imgSearch));
            } 
        } catch (Exception e) {}

        JTextField txtUnifiedSearch = new JTextField(" Quét mã vạch hoặc tìm theo tên sản phẩm / mã SKU...");
        txtUnifiedSearch.setBorder(null);
        txtUnifiedSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUnifiedSearch.setForeground(Color.GRAY);

        JPanel pnlSearchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlSearchActions.setOpaque(false);
        
        JButton btnBarcode = new JButton(); 
        try {
            java.net.URL barcodeUrl = getClass().getResource("/icon/ic_barcode.png");
            if (barcodeUrl != null) {
                btnBarcode.setIcon(new ImageIcon(new ImageIcon(barcodeUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
            } else {
                btnBarcode.setText("🎫");
            }
        } catch(Exception e) { btnBarcode.setText("🎫"); }
        btnBarcode.setContentAreaFilled(false);
        btnBarcode.setBorderPainted(false);
        btnBarcode.setFocusPainted(false);
        btnBarcode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBarcode.setPreferredSize(new Dimension(32, 32));
        pnlSearchActions.add(btnBarcode);

        pnlUnifiedSearch.add(lblSearchIcon, BorderLayout.WEST);
        pnlUnifiedSearch.add(txtUnifiedSearch, BorderLayout.CENTER);
        pnlUnifiedSearch.add(pnlSearchActions, BorderLayout.EAST);
        pnlUnifiedSearch.setPreferredSize(new Dimension(0, 40));

        // Panel bọc thanh search + bảng sản phẩm
        JPanel pnlTableAndSearchWrap = new JPanel(new BorderLayout(0, 12));
        pnlTableAndSearchWrap.setOpaque(false);
        pnlTableAndSearchWrap.add(pnlUnifiedSearch, BorderLayout.NORTH);

        // --- Bảng sản phẩm ---
        String[] columns = {"STT", "Sản phẩm", "SKU", "Đơn giá", "Số lượng", "Khuyến mãi", "Thành tiền", "Xóa"};
        Object[][] data = {
            {"1", "<html><b>Tủ lạnh Samsung</b><br><span style='color:#888; font-size:9px;'>RT38 - Inverter 380 lít</span></html>", "TL-SS-001", "12.990.000", "1", "—", "12.990.000", ""},
            {"2", "<html><b>Máy giặt LG</b><br><span style='color:#888; font-size:9px;'>FV1409 - Inverter 9 kg</span></html>", "MG-LG-002", "9.490.000", "1", "—", "9.490.000", ""},
            {"3", "<html><b>Tivi Sony</b><br><span style='color:#888; font-size:9px;'>KD-55X75K - 4K 55 inch</span></html>", "TV-SN-003", "14.990.000", "1", "—", "14.990.000", ""},
            {"4", "<html><b>Máy lạnh Panasonic</b><br><span style='color:#888; font-size:9px;'>CU/CS-PU9 - Inverter 1 HP</span></html>", "ML-PN-004", "10.990.000", "1", "—", "10.990.000", ""},
            {"5", "<html><b>Nồi chiên không dầu Philips</b><br><span style='color:#888; font-size:9px;'>HD9252 - Dung tích 4,1 lít</span></html>", "NC-PH-005", "2.490.000", "1", "<html><span style='color:#16a34a;'>-500.000</span></html>", "<html><span style='color:#1e3a8a; font-weight:bold;'>1.992.000</span></html>", ""},
            {"6", "<html><b>Máy lọc nước Karofi</b><br><span style='color:#888; font-size:9px;'>KAD-D66 - RO 10 lõi</span></html>", "LN-KR-006", "6.490.000", "1", "—", "6.490.000", ""}
        };

        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table.setRowHeight(52);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(bgHover);
        table.setSelectionForeground(textDark);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11)); 

        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(new Color(248, 250, 252));
                label.setForeground(textGray);
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));
                label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                
                if (column == 3 || column == 5 || column == 6) {
                    label.setHorizontalAlignment(SwingConstants.RIGHT);
                } else if(column == 0 || column == 4 || column == 7){
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                }
                return label;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(85);
        table.getColumnModel().getColumn(7).setPreferredWidth(40);

        table.getColumnModel().getColumn(4).setCellRenderer(new QuantityRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        scrollPane.getViewport().setBackground(bgWhite);
        pnlTableAndSearchWrap.add(scrollPane, BorderLayout.CENTER);

        pnlProductInner.add(pnlTableAndSearchWrap, BorderLayout.CENTER);
        pnlProductSection.add(pnlProductInner, BorderLayout.CENTER);

        pnlLeft.add(pnlProductSection);
        
        JScrollPane scrollLeftArea = new JScrollPane(pnlLeft);
        scrollLeftArea.setBorder(BorderFactory.createEmptyBorder());
        scrollLeftArea.getViewport().setBackground(bgMain);
        add(scrollLeftArea, BorderLayout.CENTER); // Đã đặt đúng về CENTER

        // =========================================================================
        // PHẦN BÊN PHẢI: THÔNG TIN HÓA ĐƠN, TÓM TẮT & NÚT THANH TOÁN RIÊNG BIỆT
        // =========================================================================
        JPanel pnlRight = new JPanel(new BorderLayout(0, 12));
        pnlRight.setPreferredSize(new Dimension(310, 0));
        pnlRight.setOpaque(false);

        // Panel chứa phần nội dung phía trên (Thông tin hóa đơn + Tóm tắt)
        JPanel pnlRightTopContent = new JPanel();
        pnlRightTopContent.setLayout(new BoxLayout(pnlRightTopContent, BoxLayout.Y_AXIS));
        pnlRightTopContent.setOpaque(false);

        // 1. Thông tin hóa đơn
        RoundedPanel pnlInvoiceInfo = new RoundedPanel(12, bgWhite);
        pnlInvoiceInfo.setLayout(new GridLayout(4, 1, 0, 4));
        pnlInvoiceInfo.setBorder(new EmptyBorder(12, 15, 12, 15));
        pnlInvoiceInfo.setMaximumSize(new Dimension(310, 135));

        JLabel lblInvTitle = new JLabel("Thông tin hóa đơn");
        lblInvTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInvTitle.setForeground(textDark);
        
        JPanel pnlInvRow1 = new JPanel(new BorderLayout());
        pnlInvRow1.setOpaque(false);
        pnlInvRow1.add(new JLabel("Mã hóa đơn"), BorderLayout.WEST);
        JLabel lblInvCode = new JLabel("HD-20251025-0012");
        lblInvCode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlInvRow1.add(lblInvCode, BorderLayout.EAST);

        JPanel pnlInvRow2 = new JPanel(new BorderLayout());
        pnlInvRow2.setOpaque(false);
        pnlInvRow2.add(new JLabel("Ngày lập"), BorderLayout.WEST);
        pnlInvRow2.add(new JLabel("25/10/2025 10:24"), BorderLayout.EAST);

        JPanel pnlInvRow3 = new JPanel(new BorderLayout());
        pnlInvRow3.setOpaque(false);
        pnlInvRow3.add(new JLabel("Nhân viên lập"), BorderLayout.WEST);
        pnlInvRow3.add(new JLabel("Nguyễn Văn A"), BorderLayout.EAST);

        for (JPanel row : new JPanel[]{pnlInvRow1, pnlInvRow2, pnlInvRow3}) {
            for (Component c : row.getComponents()) {
                if (c instanceof JLabel && c != lblInvCode) {
                    ((JLabel) c).setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    ((JLabel) c).setForeground(textGray);
                }
            }
        }
        
        pnlInvoiceInfo.add(lblInvTitle);
        pnlInvoiceInfo.add(pnlInvRow1);
        pnlInvoiceInfo.add(pnlInvRow2);
        pnlInvoiceInfo.add(pnlInvRow3);
        pnlRightTopContent.add(pnlInvoiceInfo);
        pnlRightTopContent.add(Box.createVerticalStrut(12));

        // 2. Tóm tắt thanh toán (Đã sửa lại layout BorderLayout để căn sát lề trái chuẩn xác)
        RoundedPanel pnlSummary = new RoundedPanel(12, bgWhite);
        pnlSummary.setLayout(new BorderLayout(0, 12));
        pnlSummary.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlSummary.setMaximumSize(new Dimension(310, 240));

        JLabel lblSumTitle = new JLabel("Tóm tắt thanh toán");
        lblSumTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSumTitle.setForeground(textDark);
        pnlSummary.add(lblSumTitle, BorderLayout.NORTH);

        JPanel pnlSummaryDetails = new JPanel();
        pnlSummaryDetails.setLayout(new BoxLayout(pnlSummaryDetails, BoxLayout.Y_AXIS));
        pnlSummaryDetails.setOpaque(false);

        String[][] summaryRows = {
            {"Tạm tính (6 sản phẩm)", "56.942.000 đ"},
            {"Giảm giá sản phẩm", "498.000 đ"},
            {"Khuyến mãi (Khách Diamond)", "-2.822.200 đ"},
            {"Phí vận chuyển", "0 đ"}
        };

        for (String[] rowData : summaryRows) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            JLabel lblKey = new JLabel(rowData[0]);
            JLabel lblVal = new JLabel(rowData[1]);
            lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblKey.setForeground(textGray);
            lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblVal.setForeground(textDark);
            row.add(lblKey, BorderLayout.WEST);
            row.add(lblVal, BorderLayout.EAST);
            row.setBorder(new EmptyBorder(4, 0, 4, 0));
            pnlSummaryDetails.add(row);
        }

        pnlSummaryDetails.add(Box.createVerticalStrut(5));
        JPanel pnlTotalRow = new JPanel(new BorderLayout());
        pnlTotalRow.setBackground(new Color(239, 246, 255));
        pnlTotalRow.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblTotalLabel = new JLabel("Tổng tiền thanh toán");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalLabel.setForeground(colorBlue);
        JLabel lblTotalVal = new JLabel("53.621.800 đ");
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalVal.setForeground(colorBlue);
        pnlTotalRow.add(lblTotalLabel, BorderLayout.WEST);
        pnlTotalRow.add(lblTotalVal, BorderLayout.EAST);
        pnlSummaryDetails.add(pnlTotalRow);

        pnlSummary.add(pnlSummaryDetails, BorderLayout.CENTER);
        pnlRightTopContent.add(pnlSummary);
        
        // Ráp phần nội dung phía trên vào NORTH của pnlRight
        pnlRight.add(pnlRightTopContent, BorderLayout.NORTH);

        // 3. KHUNG RIÊNG CHO NÚT THANH TOÁN Ở DƯỚI CÙNG (SOUTH)
        RoundedPanel pnlCheckoutSection = new RoundedPanel(12, bgWhite);
        pnlCheckoutSection.setLayout(new BorderLayout());
        pnlCheckoutSection.setBorder(new EmptyBorder(12, 12, 12, 12));

        JButton btnCheckout = new JButton("Thanh toán →");
        btnCheckout.setBackground(colorBlue);
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCheckout.setFocusPainted(false);
        btnCheckout.setBorderPainted(false);
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckout.setPreferredSize(new Dimension(0, 46));

        pnlCheckoutSection.add(btnCheckout, BorderLayout.CENTER);
        
        pnlRight.add(pnlCheckoutSection, BorderLayout.SOUTH);

        add(pnlRight, BorderLayout.EAST);
    }
}