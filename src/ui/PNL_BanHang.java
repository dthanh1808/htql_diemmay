package ui;

import dao.KhachHangDAO;
import dao.SanPhamDAO;
import entity.KhachHang;
import entity.HangThanhVien;
import entity.SanPham;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class PNL_BanHang extends JPanel {
    private Color bgMain = new Color(245, 247, 250);
    private Color bgWhite = Color.WHITE;
    private Color textDark = new Color(30, 30, 30);
    private Color textGray = new Color(130, 130, 130);
    private Color colorBlue = new Color(37, 99, 235);
    private Color bgHover = new Color(239, 246, 255);
    
    private JPanel pnlCustCenter;
    
    // --- Các hành động (Callbacks) chuyển màn hình ---
    private Runnable invoiceAction;
    private Runnable addCustomerAction = () -> JOptionPane.showMessageDialog(this, "Chuyển sang tab Khách Hàng để thêm mới.");

    // --- Biến Khách Hàng ---
    private JTextField txtSearchCust;
    private JPanel pnlCustSelected;
    private JLabel lblAvatar, lblCustName, lblCustID, lblPhone, lblEmail, lblPointsVal, lblBadgeDiamond;
    private JRadioButton radVangLai;
    private JButton btnClearCust;
    private JPopupMenu popupSearch;
    private JList<KhachHang> listSearch;
    private DefaultListModel<KhachHang> listModel;
    private KhachHangDAO khachHangDAO;
    private KhachHang currentKhachHang = null; 
    private final String PLACEHOLDER = " Tìm theo tên, SĐT, mã khách hàng...";

    // --- Biến Sản Phẩm & Bảng ---
    private JTextField txtUnifiedSearch;
    private JPopupMenu popupProductSearch;
    private JList<SanPham> listProductSearch;
    private DefaultListModel<SanPham> listProductModel;
    private SanPhamDAO sanPhamDAO;
    private DefaultTableModel tableModel;
    private JTable table;
    private final String PRODUCT_PLACEHOLDER = " Quét mã vạch hoặc tìm theo tên sản phẩm / mã SKU...";

    // --- Biến Tóm tắt thanh toán ---
    private JLabel lblTamTinhVal;
    private JLabel lblKhuyenMaiKey;
    private JLabel lblKhuyenMaiVal;
    private JLabel lblTotalVal;

    // --- Biến Giao hàng & Thanh toán ---
    private JCheckBox chkDelivery;
    private JTextField txtAddress;
    private JRadioButton radTienMat, radChuyenKhoan;

    public PNL_BanHang(Runnable invoiceAction, Runnable addCustomerAction) {
        this();
        if(invoiceAction != null) this.invoiceAction = invoiceAction;
        if(addCustomerAction != null) this.addCustomerAction = addCustomerAction;
    }
    
    public PNL_BanHang(Runnable invoiceAction) {
        this(invoiceAction, null);
    }

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
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            add(btnMinus); add(txtQty); add(btnPlus);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) txtQty.setText(value.toString());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JLabel lblDelete;
        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
            setOpaque(true);
            lblDelete = new JLabel();
            lblDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            try {
                java.net.URL url = getClass().getResource("/icon/ic_xoa.png");
                if (url != null) {
                    Image img = new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    lblDelete.setIcon(new ImageIcon(img));
                } else {
                    lblDelete.setText("❌"); 
                }
            } catch(Exception e) {}
            add(lblDelete);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    public PNL_BanHang() {
        khachHangDAO = new KhachHangDAO(); 
        sanPhamDAO = new SanPhamDAO();
        setLayout(new BorderLayout(15, 0));
        setBackground(bgMain);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // =========================================================================
        // PHẦN BÊN TRÁI: KHU VỰC TÌM KIẾM, KHÁCH HÀNG VÀ BẢNG SẢN PHẨM
        // =========================================================================
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setOpaque(false);

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
        
        radVangLai = new JRadioButton("Khách hàng vãng lai");
        radVangLai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radVangLai.setOpaque(false);
        radVangLai.setFocusPainted(false);

        JButton btnAddCust = new JButton(" Đăng ký khách hàng mới");
        try {
            java.net.URL addURL = getClass().getResource("/icon/ic_them.png"); 
            if (addURL != null) {
                Image imgAdd = new ImageIcon(addURL).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                btnAddCust.setIcon(new ImageIcon(imgAdd));
            }
        } catch (Exception e) {}
        btnAddCust.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddCust.setBackground(colorBlue);
        btnAddCust.setForeground(Color.WHITE);
        btnAddCust.setFocusPainted(false);
        btnAddCust.setBorderPainted(false);
        
        btnAddCust.addActionListener(e -> addCustomerAction.run());

        pnlCustActions.add(radVangLai);
        pnlCustActions.add(btnAddCust);
        pnlCustTop.add(lblCustTitle, BorderLayout.WEST);
        pnlCustTop.add(pnlCustActions, BorderLayout.EAST);
        pnlCustomer.add(pnlCustTop, BorderLayout.NORTH);

        JPanel pnlCustSearch = new JPanel(new BorderLayout(5, 0));
        pnlCustSearch.setBackground(Color.WHITE);
        pnlCustSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            new EmptyBorder(0, 10, 0, 10)
        ));
        
        JLabel lblSearchCustIcon = new JLabel("🔍");
        txtSearchCust = new JTextField(PLACEHOLDER);
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

        pnlCustSelected = new JPanel(new BorderLayout());
        pnlCustSelected.setBackground(new Color(248, 250, 252));
        pnlCustSelected.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        pnlCustSelected.setVisible(false);

        JPanel pnlCustInfoLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlCustInfoLeft.setOpaque(false);
        lblAvatar = new JLabel("?", SwingConstants.CENTER) {
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
        lblCustName = new JLabel("");
        lblCustName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCustID = new JLabel("");
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
        lblPhone = new JLabel("");
        lblEmail = new JLabel("");
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlContact.add(lblPhone);
        pnlContact.add(lblEmail);
        pnlCustInfoCenter.add(pnlContact);

        JPanel pnlCustInfoRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnlCustInfoRight.setOpaque(false);
        lblBadgeDiamond = new JLabel("");
        
        JPanel pnlPoints = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlPoints.setOpaque(false);
        lblPointsVal = new JLabel("0 đ");
        lblPointsVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPointsVal.setForeground(colorBlue);
        JLabel lblPointsLabel = new JLabel("Chi tiêu tích lũy");
        lblPointsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblPointsLabel.setForeground(textGray);
        pnlPoints.add(lblPointsVal);
        pnlPoints.add(lblPointsLabel);

        btnClearCust = new JButton("✕");
        btnClearCust.setBorderPainted(false);
        btnClearCust.setContentAreaFilled(false);
        btnClearCust.setForeground(textGray);
        btnClearCust.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        // KHUNG SẢN PHẨM TRONG HÓA ĐƠN
        RoundedPanel pnlProductSection = new RoundedPanel(12, bgWhite);
        pnlProductSection.setLayout(new BorderLayout(0, 12));
        pnlProductSection.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblSectionTitle = new JLabel("Sản phẩm trong hóa đơn");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSectionTitle.setForeground(textDark);
        pnlProductSection.add(lblSectionTitle, BorderLayout.NORTH);

        JPanel pnlProductInner = new JPanel(new BorderLayout(0, 12));
        pnlProductInner.setOpaque(false);

        JPanel pnlUnifiedSearch = new JPanel(new BorderLayout(10, 0));
        pnlUnifiedSearch.setBackground(Color.WHITE);
        pnlUnifiedSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            new EmptyBorder(0, 10, 0, 5)
        ));
        
        JLabel lblSearchIcon = new JLabel("🔍"); 
        txtUnifiedSearch = new JTextField(PRODUCT_PLACEHOLDER);
        txtUnifiedSearch.setBorder(null);
        txtUnifiedSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUnifiedSearch.setForeground(Color.GRAY);

        JPanel pnlSearchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlSearchActions.setOpaque(false);
        
        JButton btnBarcode = new JButton("🎫"); 
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

        JPanel pnlTableAndSearchWrap = new JPanel(new BorderLayout(0, 12));
        pnlTableAndSearchWrap.setOpaque(false);
        pnlTableAndSearchWrap.add(pnlUnifiedSearch, BorderLayout.NORTH);

        String[] columns = {"STT", "Sản phẩm", "SKU", "Đơn giá", "Số lượng", "Thành tiền", "Xóa"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(52);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(bgHover);
        table.setSelectionForeground(textDark);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11)); 

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(new Color(248, 250, 252));
                label.setForeground(textGray);
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));
                label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                if (column == 3 || column == 5) label.setHorizontalAlignment(SwingConstants.RIGHT);
                else if(column == 0 || column == 4 || column == 6) label.setHorizontalAlignment(SwingConstants.CENTER);
                else label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(85);
        table.getColumnModel().getColumn(6).setPreferredWidth(40);

        table.getColumnModel().getColumn(4).setCellRenderer(new QuantityRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());

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
        add(scrollLeftArea, BorderLayout.CENTER);

        // =========================================================================
        // PHẦN BÊN PHẢI: THÔNG TIN HÓA ĐƠN VÀ TÓM TẮT
        // =========================================================================
        JPanel pnlRight = new JPanel(new BorderLayout(0, 12));
        pnlRight.setPreferredSize(new Dimension(310, 0));
        pnlRight.setOpaque(false);

        JPanel pnlRightTopContent = new JPanel();
        pnlRightTopContent.setLayout(new BoxLayout(pnlRightTopContent, BoxLayout.Y_AXIS));
        pnlRightTopContent.setOpaque(false);

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

        // TÓM TẮT THANH TOÁN
        RoundedPanel pnlSummary = new RoundedPanel(12, bgWhite);
        pnlSummary.setLayout(new BorderLayout(0, 12));
        pnlSummary.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlSummary.setMaximumSize(new Dimension(310, 180));

        JLabel lblSumTitle = new JLabel("Tóm tắt thanh toán");
        lblSumTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSumTitle.setForeground(textDark);
        pnlSummary.add(lblSumTitle, BorderLayout.NORTH);

        JPanel pnlSummaryDetails = new JPanel();
        pnlSummaryDetails.setLayout(new BoxLayout(pnlSummaryDetails, BoxLayout.Y_AXIS));
        pnlSummaryDetails.setOpaque(false);

        JPanel rowTamTinh = new JPanel(new BorderLayout());
        rowTamTinh.setOpaque(false);
        JLabel lblTamTinhKey = new JLabel("Tạm tính");
        lblTamTinhKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTamTinhKey.setForeground(textGray);
        
        lblTamTinhVal = new JLabel("0 đ");
        lblTamTinhVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTamTinhVal.setForeground(textDark);
        
        rowTamTinh.add(lblTamTinhKey, BorderLayout.WEST);
        rowTamTinh.add(lblTamTinhVal, BorderLayout.EAST);
        rowTamTinh.setBorder(new EmptyBorder(4, 0, 4, 0));
        pnlSummaryDetails.add(rowTamTinh);

        JPanel rowKhuyenMai = new JPanel(new BorderLayout());
        rowKhuyenMai.setOpaque(false);
        lblKhuyenMaiKey = new JLabel("Khuyến mãi");
        lblKhuyenMaiKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKhuyenMaiKey.setForeground(textGray);
        
        lblKhuyenMaiVal = new JLabel("0 đ");
        lblKhuyenMaiVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKhuyenMaiVal.setForeground(textDark);
        
        rowKhuyenMai.add(lblKhuyenMaiKey, BorderLayout.WEST);
        rowKhuyenMai.add(lblKhuyenMaiVal, BorderLayout.EAST);
        rowKhuyenMai.setBorder(new EmptyBorder(4, 0, 4, 0));
        pnlSummaryDetails.add(rowKhuyenMai);

        pnlSummaryDetails.add(Box.createVerticalStrut(10));
        
        JPanel pnlTotalRow = new JPanel(new BorderLayout());
        pnlTotalRow.setBackground(new Color(239, 246, 255));
        pnlTotalRow.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblTotalLabel = new JLabel("Tổng tiền thanh toán");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalLabel.setForeground(colorBlue);
        
        lblTotalVal = new JLabel("0 đ");
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalVal.setForeground(colorBlue);
        
        pnlTotalRow.add(lblTotalLabel, BorderLayout.WEST);
        pnlTotalRow.add(lblTotalVal, BorderLayout.EAST);
        pnlSummaryDetails.add(pnlTotalRow);

        pnlSummary.add(pnlSummaryDetails, BorderLayout.CENTER);
        pnlRightTopContent.add(pnlSummary);
        
        pnlRight.add(pnlRightTopContent, BorderLayout.NORTH);

        // =========================================================================
        // PHẦN CHECKOUT: GIAO HÀNG, THANH TOÁN & NÚT THANH TOÁN
        // =========================================================================
        RoundedPanel pnlCheckoutSection = new RoundedPanel(12, bgWhite);
        pnlCheckoutSection.setLayout(new BorderLayout(0, 10)); 
        pnlCheckoutSection.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel pnlDeliveryAndPayment = new JPanel();
        pnlDeliveryAndPayment.setLayout(new BoxLayout(pnlDeliveryAndPayment, BoxLayout.Y_AXIS));
        pnlDeliveryAndPayment.setOpaque(false);
        pnlDeliveryAndPayment.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- 1. Giao hàng & Lắp đặt ---
        chkDelivery = new JCheckBox(" Yêu cầu Giao hàng & Lắp đặt");
        chkDelivery.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkDelivery.setForeground(textDark);
        chkDelivery.setOpaque(false);
        chkDelivery.setFocusPainted(false);
        chkDelivery.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtAddress = new JTextField(" Nhập địa chỉ / ghi chú giao hàng...");
        txtAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtAddress.setForeground(Color.GRAY);
        txtAddress.setEnabled(false); 
        txtAddress.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        txtAddress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtAddress.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtAddress.getText().equals(" Nhập địa chỉ / ghi chú giao hàng...")) {
                    txtAddress.setText("");
                    txtAddress.setForeground(textDark);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtAddress.getText().isEmpty()) {
                    txtAddress.setForeground(Color.GRAY);
                    txtAddress.setText(" Nhập địa chỉ / ghi chú giao hàng...");
                }
            }
        });

        chkDelivery.addActionListener(e -> {
            boolean isChecked = chkDelivery.isSelected();
            txtAddress.setEnabled(isChecked);
            if (isChecked) {
                txtAddress.requestFocus();
                if (txtAddress.getText().equals(" Nhập địa chỉ / ghi chú giao hàng...")) {
                    txtAddress.setText("");
                    txtAddress.setForeground(textDark);
                }
            } else {
                txtAddress.setForeground(Color.GRAY);
                txtAddress.setText(" Nhập địa chỉ / ghi chú giao hàng...");
            }
        });

        pnlDeliveryAndPayment.add(chkDelivery);
        pnlDeliveryAndPayment.add(Box.createVerticalStrut(6));
        pnlDeliveryAndPayment.add(txtAddress);
        pnlDeliveryAndPayment.add(Box.createVerticalStrut(12));

        // --- 2. Hình thức thanh toán ---
        JLabel lblPayTitle = new JLabel("Hình thức thanh toán:");
        lblPayTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPayTitle.setForeground(textDark);
        lblPayTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlPayOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlPayOptions.setOpaque(false);
        pnlPayOptions.setAlignmentX(Component.LEFT_ALIGNMENT);

        radTienMat = new JRadioButton("Tiền mặt", true);
        radTienMat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radTienMat.setOpaque(false);
        radTienMat.setFocusPainted(false);

        radChuyenKhoan = new JRadioButton("Chuyển khoản");
        radChuyenKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radChuyenKhoan.setOpaque(false);
        radChuyenKhoan.setFocusPainted(false);
        radChuyenKhoan.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        ButtonGroup payGroup = new ButtonGroup();
        payGroup.add(radTienMat);
        payGroup.add(radChuyenKhoan);

        pnlPayOptions.add(radTienMat);
        pnlPayOptions.add(radChuyenKhoan);

        pnlDeliveryAndPayment.add(lblPayTitle);
        pnlDeliveryAndPayment.add(Box.createVerticalStrut(4));
        pnlDeliveryAndPayment.add(pnlPayOptions);

        pnlCheckoutSection.add(pnlDeliveryAndPayment, BorderLayout.CENTER);

        // --- Nút Thanh Toán ---
        JButton btnCheckout = new JButton("Thanh toán →");
        btnCheckout.setBackground(colorBlue);
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCheckout.setFocusPainted(false);
        btnCheckout.setBorderPainted(false);
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckout.setPreferredSize(new Dimension(0, 46));

        // Sự kiện khi bấm Thanh toán -> Hiện Pop-up hóa đơn điện tử
        btnCheckout.addActionListener(e -> hienThiPopUpHoaDon());

        pnlCheckoutSection.add(btnCheckout, BorderLayout.SOUTH);
        pnlRight.add(pnlCheckoutSection, BorderLayout.SOUTH);

        add(pnlRight, BorderLayout.EAST);

        setupCustomerEvents();
        setupProductEvents();
    }

    // =========================================================================
    // HÀM HIỂN THỊ POP-UP HÓA ĐƠN ĐIỆN TỬ (ĐÃ CẬP NHẬT THEO YÊU CẦU)
    // =========================================================================
    private void hienThiPopUpHoaDon() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hóa đơn chưa có sản phẩm nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hóa đơn điện tử", true);
        
        JPanel pnlSheet = new JPanel();
        pnlSheet.setLayout(new BoxLayout(pnlSheet, BoxLayout.Y_AXIS));
        pnlSheet.setBackground(Color.WHITE);
        pnlSheet.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Tiêu đề (ĐÃ BỎ CHỮ "Thanh toán thành công" THEO YÊU CẦU)
        JLabel lblTitle = new JLabel("Hóa đơn điện tử", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(colorBlue);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlSheet.add(lblTitle);
        pnlSheet.add(Box.createVerticalStrut(10));

        // Thông tin meta
        JPanel pnlMeta = new JPanel(new GridLayout(2, 2, 10, 3));
        pnlMeta.setOpaque(false);
        pnlMeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        pnlMeta.add(new JLabel("Mã hóa đơn: HD" + (System.currentTimeMillis() % 100000000)));
        pnlMeta.add(new JLabel("Ngày lập: " + LocalDateTime.now().format(dtf)));
        pnlMeta.add(new JLabel("Nhân viên lập: Nguyễn Văn A"));
        pnlMeta.add(new JLabel("Trạng thái: Đã thanh toán"));

        for (Component c : pnlMeta.getComponents()) {
            c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            ((JLabel) c).setForeground(textGray);
        }
        pnlSheet.add(pnlMeta);
        pnlSheet.add(Box.createVerticalStrut(8));
        pnlSheet.add(new JSeparator());
        pnlSheet.add(Box.createVerticalStrut(8));

        // Thông tin khách hàng
        JPanel pnlCust = new JPanel(new BorderLayout());
        pnlCust.setOpaque(false);
        pnlCust.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        String tenKH = (currentKhachHang != null) ? currentKhachHang.getTenKH() : "Khách hàng vãng lai";
        String sdtKH = (currentKhachHang != null) ? currentKhachHang.getSoDienThoai() : "---";
        
        JLabel lblCustName = new JLabel(tenKH);
        lblCustName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lblCustPhone = new JLabel(sdtKH);
        lblCustPhone.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        pnlCust.add(lblCustName, BorderLayout.WEST);
        pnlCust.add(lblCustPhone, BorderLayout.EAST);
        pnlSheet.add(pnlCust);
        pnlSheet.add(Box.createVerticalStrut(6));

        // Bảng sản phẩm
        String[] cols = {"STT", "Sản phẩm", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel invModel = new DefaultTableModel(cols, 0);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            invModel.addRow(new Object[]{
                tableModel.getValueAt(i, 0),
                tableModel.getValueAt(i, 1),
                tableModel.getValueAt(i, 4),
                tableModel.getValueAt(i, 3),
                tableModel.getValueAt(i, 5)
            });
        }
        JTable invTable = new JTable(invModel);
        int rowHeight = 28;
        invTable.setRowHeight(rowHeight);
        invTable.setEnabled(false);
        invTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        int tablePreferredHeight = Math.min(invModel.getRowCount() * rowHeight + 28, 160);
        
        JScrollPane scrollTable = new JScrollPane(invTable);
        scrollTable.setPreferredSize(new Dimension(450, tablePreferredHeight));
        scrollTable.setMaximumSize(new Dimension(Integer.MAX_VALUE, tablePreferredHeight));
        pnlSheet.add(scrollTable);
        pnlSheet.add(Box.createVerticalStrut(8));

        // Tính tiền
        long tamTinh = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String t = tableModel.getValueAt(i, 5).toString().replaceAll("[^0-9]", "");
            if (!t.isEmpty()) tamTinh += Long.parseLong(t);
        }

        long khuyenMai = 0;
        if (currentKhachHang != null) {
            HangThanhVien h = khachHangDAO.layHang(currentKhachHang.getId());
            if (h != null && h.getPhanTramUuDai() > 0) {
                khuyenMai = (long) (tamTinh * (h.getPhanTramUuDai() / 100.0));
            }
        }
        long tongThanhToan = tamTinh - khuyenMai;
        if (tongThanhToan < 0) tongThanhToan = 0;

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        pnlSheet.add(createSummaryRow("Tạm tính", nf.format(tamTinh) + " đ", false));
        pnlSheet.add(createSummaryRow("Khuyến mãi", "-" + nf.format(khuyenMai) + " đ", false));
        pnlSheet.add(createSummaryRow("Tổng tiền thanh toán", nf.format(tongThanhToan) + " đ", true));
        
        String hinhThuc = radChuyenKhoan.isSelected() ? "Chuyển khoản" : "Tiền mặt";
        pnlSheet.add(createSummaryRow("Phương thức thanh toán", hinhThuc, false));

        pnlSheet.add(Box.createVerticalStrut(6));
        pnlSheet.add(new JSeparator());
        pnlSheet.add(Box.createVerticalStrut(6));

        // 2. Giao hàng (ĐÃ ÉP SÁT LỀ TRÁI DÙNG BINDING XBOX / PANEL LEFT ALIGNMENT)
        JPanel pnlDeliv = new JPanel();
        pnlDeliv.setLayout(new BoxLayout(pnlDeliv, BoxLayout.Y_AXIS));
        pnlDeliv.setOpaque(false);
        pnlDeliv.setAlignmentX(Component.LEFT_ALIGNMENT); // Căn lề trái

        JLabel dTitle = new JLabel("Giao hàng & Lắp đặt");
        dTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDeliv.add(dTitle);
        
        boolean isGiao = chkDelivery.isSelected();
        JLabel dStatus = new JLabel("Yêu cầu giao hàng & lắp đặt: " + (isGiao ? "Có" : "Không"));
        dStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDeliv.add(dStatus);

        if (isGiao) {
            String addr = txtAddress.getText().equals(" Nhập địa chỉ / ghi chú giao hàng...") ? "Không có địa chỉ cụ thể" : txtAddress.getText();
            JLabel dAddr = new JLabel("📍 Địa chỉ: " + addr);
            dAddr.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            dAddr.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlDeliv.add(Box.createVerticalStrut(2));
            pnlDeliv.add(dAddr);
        }
        pnlSheet.add(pnlDeliv);

        // QR Code nếu chuyển khoản
        if ("Chuyển khoản".equals(hinhThuc)) {
            pnlSheet.add(Box.createVerticalStrut(8));
            pnlSheet.add(new JSeparator());
            pnlSheet.add(Box.createVerticalStrut(8));

            JLabel qrTitle = new JLabel("Quét mã QR chuyển khoản");
            qrTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            qrTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlSheet.add(qrTitle);
            pnlSheet.add(Box.createVerticalStrut(4));

            try {
                String qrUrl = "https://img.vietqr.io/image/970422-0361112233-compact2.png?amount=" + tongThanhToan + "&addInfo=Thanh%20toan%20HD";
                ImageIcon qrIcon = new ImageIcon(new URL(qrUrl));
                Image scaledQR = qrIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel lblQR = new JLabel(new ImageIcon(scaledQR));
                lblQR.setAlignmentX(Component.CENTER_ALIGNMENT);
                pnlSheet.add(lblQR);
            } catch (Exception ex) {
                JLabel errQR = new JLabel("(Không thể tải mã QR)");
                errQR.setAlignmentX(Component.CENTER_ALIGNMENT);
                pnlSheet.add(errQR);
            }
        }

        JScrollPane scrollSheet = new JScrollPane(pnlSheet);
        scrollSheet.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scrollSheet, BorderLayout.CENTER);

        // 3. ĐÃ BỎ NÚT Ở PHÍA DƯỚI CÙNG THEO YÊU CẦU (Không add pnlBtn chứa nút nữa)

        // Khi đóng dialog (bấm nút X của cửa sổ), tự động làm mới lại bảng bán hàng
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                tableModel.setRowCount(0);
                capNhatTongTien();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createSummaryRow(String label, String value, boolean isBold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lblKey = new JLabel(label);
        JLabel lblVal = new JLabel(value);

        Font font = new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, isBold ? 12 : 11);
        lblKey.setFont(font);
        lblVal.setFont(font);

        if (isBold) {
            lblKey.setForeground(colorBlue);
            lblVal.setForeground(colorBlue);
        } else {
            lblKey.setForeground(textGray);
            lblVal.setForeground(textDark);
        }

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.EAST);
        return row;
    }

    private void capNhatTongTien() {
        long tongTien = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String tienStr = tableModel.getValueAt(i, 5).toString().replaceAll("[^0-9]", ""); 
            if (!tienStr.isEmpty()) {
                tongTien += Long.parseLong(tienStr);
            }
        }
        
        long tienKhuyenMai = 0;
        String tenKMBadge = "Khuyến mãi";
        
        if (currentKhachHang != null) {
            HangThanhVien hang = khachHangDAO.layHang(currentKhachHang.getId());
            if (hang != null && hang.getPhanTramUuDai() > 0) {
                tienKhuyenMai = (long) (tongTien * (hang.getPhanTramUuDai() / 100.0));
                tenKMBadge = "Khuyến mãi (Khách " + hang.getTen() + " - " + hang.getPhanTramUuDai() + "%)";
            }
        }
        
        long tongThanhToan = tongTien - tienKhuyenMai;
        if (tongThanhToan < 0) tongThanhToan = 0; 
        
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        
        if (lblTamTinhVal != null) lblTamTinhVal.setText(nf.format(tongTien) + " đ");
        
        if (lblKhuyenMaiKey != null) lblKhuyenMaiKey.setText(tenKMBadge);
        if (lblKhuyenMaiVal != null) {
            if (tienKhuyenMai > 0) {
                lblKhuyenMaiVal.setText("-" + nf.format(tienKhuyenMai) + " đ");
                lblKhuyenMaiVal.setForeground(new Color(22, 163, 74)); 
            } else {
                lblKhuyenMaiVal.setText("0 đ");
                lblKhuyenMaiVal.setForeground(textDark);
            }
        }
        
        if (lblTotalVal != null) lblTotalVal.setText(nf.format(tongThanhToan) + " đ");
    }

    // =========================================================================
    // EVENTS KHÁCH HÀNG 
    // =========================================================================
    private void setupCustomerEvents() {
        btnClearCust.addActionListener(e -> {
            pnlCustSelected.setVisible(false);
            txtSearchCust.setText("");
            txtSearchCust.requestFocus();
            currentKhachHang = null;
            capNhatTongTien(); 
        });

        radVangLai.addItemListener(e -> {
            if(radVangLai.isSelected()) {
                pnlCustCenter.setVisible(false);
                if(popupSearch != null) popupSearch.setVisible(false);
                currentKhachHang = null;
                capNhatTongTien(); 
            } else {
                pnlCustCenter.setVisible(true);
            }
            revalidate();
            repaint();
        });

        txtSearchCust.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearchCust.getText().equals(PLACEHOLDER)) {
                    txtSearchCust.setText("");
                    txtSearchCust.setForeground(textDark);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearchCust.getText().isEmpty()) {
                    txtSearchCust.setForeground(Color.GRAY);
                    txtSearchCust.setText(PLACEHOLDER);
                }
            }
        });

        popupSearch = new JPopupMenu();
        popupSearch.setFocusable(false);
        listModel = new DefaultListModel<>();
        listSearch = new JList<>(listModel);
        listSearch.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        listSearch.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    KhachHang kh = (KhachHang) value;
                    setText(kh.getTenKH() + " - " + kh.getSoDienThoai());
                    setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                }
                return c;
            }
        });

        JScrollPane scrollPopup = new JScrollPane(listSearch);
        scrollPopup.setBorder(null);
        scrollPopup.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        popupSearch.add(scrollPopup);

        txtSearchCust.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { timKiemKhachHang(); }
            public void removeUpdate(DocumentEvent e) { timKiemKhachHang(); }
            public void changedUpdate(DocumentEvent e) { timKiemKhachHang(); }
        });

        listSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    KhachHang selectedKh = listSearch.getSelectedValue();
                    if (selectedKh != null) {
                        chonKhachHang(selectedKh);
                    }
                }
            }
        });
    }

    private void timKiemKhachHang() {
        String keyword = txtSearchCust.getText().trim();
        if (keyword.isEmpty() || keyword.equals(PLACEHOLDER.trim())) {
            popupSearch.setVisible(false);
            return;
        }

        ArrayList<KhachHang> ketQua = khachHangDAO.timKiem(keyword, null, true, null, null);
        listModel.clear();
        for (KhachHang kh : ketQua) listModel.addElement(kh);

        if (listModel.isEmpty()) {
            popupSearch.setVisible(false);
        } else {
            popupSearch.setPopupSize(txtSearchCust.getWidth(), Math.min(ketQua.size() * 35, 200));
            popupSearch.show(txtSearchCust, 0, txtSearchCust.getHeight());
            txtSearchCust.requestFocus(); 
        }
    }

    private void chonKhachHang(KhachHang kh) {
        popupSearch.setVisible(false);
        this.currentKhachHang = kh; 
        
        String ten = kh.getTenKH().trim();
        String initChar = ten.isEmpty() ? "?" : ten.substring(0, 1).toUpperCase();
        lblAvatar.setText(initChar);
        lblCustName.setText(kh.getTenKH());
        lblCustID.setText(kh.getId());
        lblPhone.setText("📞 " + kh.getSoDienThoai());
        lblEmail.setText("✉ " + (kh.getEmail() != null && !kh.getEmail().isEmpty() ? kh.getEmail() : "Không có email"));

        java.math.BigDecimal tongTien = khachHangDAO.tongChiTieu(kh.getId());
        HangThanhVien hang = khachHangDAO.layHang(kh.getId());
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        lblPointsVal.setText(nf.format(tongTien) + " đ");

        String colorBadge = switch (hang.getTen()) {
            case "Diamond" -> "background-color:#f3e8ff; color:#9333ea;";
            case "Gold" -> "background-color:#fef08a; color:#854d0e;";
            case "Silver" -> "background-color:#e2e8f0; color:#475569;";
            default -> "background-color:#f1f5f9; color:#64748b;";
        };
        lblBadgeDiamond.setText("<html><span style='" + colorBadge + " padding:3px 10px; border-radius:10px;'><b>" + hang.getTen() + "</b></span></html>");
        
        pnlCustSelected.setVisible(true);
        txtSearchCust.setText("");
        
        capNhatTongTien(); 
    }

    // =========================================================================
    // EVENTS SẢN PHẨM & CLICK BẢNG HÓA ĐƠN
    // =========================================================================
    private void setupProductEvents() {
        txtUnifiedSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtUnifiedSearch.getText().equals(PRODUCT_PLACEHOLDER)) {
                    txtUnifiedSearch.setText("");
                    txtUnifiedSearch.setForeground(textDark);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtUnifiedSearch.getText().isEmpty()) {
                    txtUnifiedSearch.setForeground(Color.GRAY);
                    txtUnifiedSearch.setText(PRODUCT_PLACEHOLDER);
                }
            }
        });

        popupProductSearch = new JPopupMenu();
        popupProductSearch.setFocusable(false);
        listProductModel = new DefaultListModel<>();
        listProductSearch = new JList<>(listProductModel);
        listProductSearch.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listProductSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        listProductSearch.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SanPham) {
                    SanPham sp = (SanPham) value;
                    java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                    setText(sp.getSku() + " | " + sp.getTen() + " - " + nf.format(sp.getGiaBan()) + " đ");
                    setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                }
                return c;
            }
        });

        JScrollPane scrollPopup = new JScrollPane(listProductSearch);
        scrollPopup.setBorder(null);
        scrollPopup.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        popupProductSearch.add(scrollPopup);

        txtUnifiedSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { timKiemSanPham(); }
            public void removeUpdate(DocumentEvent e) { timKiemSanPham(); }
            public void changedUpdate(DocumentEvent e) { timKiemSanPham(); }
        });

        listProductSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    SanPham selectedSp = listProductSearch.getSelectedValue();
                    if (selectedSp != null) {
                        chonSanPham(selectedSp);
                    }
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row >= 0) {
                    if (col == 6) {
                        int confirm = JOptionPane.showConfirmDialog(
                                PNL_BanHang.this,
                                "Bạn có chắc muốn xóa sản phẩm này khỏi hóa đơn?",
                                "Xác nhận xóa",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            tableModel.removeRow(row); 
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                tableModel.setValueAt(String.valueOf(i + 1), i, 0); 
                            }
                            capNhatTongTien();
                        }
                    }
                    else if (col == 4) {
                        Rectangle cellRect = table.getCellRect(row, col, false);
                        int clickX = e.getX() - cellRect.x;
                        
                        int currentQty = Integer.parseInt(tableModel.getValueAt(row, 4).toString());
                        boolean qtyChanged = false;

                        if (clickX < 30) {
                            if (currentQty > 1) { currentQty--; qtyChanged = true; }
                        } else if (clickX > cellRect.width - 30) {
                            currentQty++; qtyChanged = true; 
                        }

                        if (qtyChanged) {
                            tableModel.setValueAt(String.valueOf(currentQty), row, 4);
                            String giaStr = tableModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", "");
                            long donGia = Long.parseLong(giaStr);
                            long thanhTien = donGia * currentQty;
                            
                            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                            tableModel.setValueAt(nf.format(thanhTien), row, 5); 
                            capNhatTongTien(); 
                        }
                    }
                }
            }
        });
    }

    private void timKiemSanPham() {
        String keyword = txtUnifiedSearch.getText().trim();
        if (keyword.isEmpty() || keyword.equals(PRODUCT_PLACEHOLDER.trim())) {
            popupProductSearch.setVisible(false);
            return;
        }

        ArrayList<SanPham> ketQua = sanPhamDAO.timKiem(keyword, null, null, 0, null, true);
        listProductModel.clear();
        for (SanPham sp : ketQua) {
            listProductModel.addElement(sp);
        }

        if (listProductModel.isEmpty()) {
            popupProductSearch.setVisible(false);
        } else {
            popupProductSearch.setPopupSize(txtUnifiedSearch.getWidth(), Math.min(ketQua.size() * 35, 200));
            popupProductSearch.show(txtUnifiedSearch, 0, txtUnifiedSearch.getHeight());
            txtUnifiedSearch.requestFocus(); 
        }
    }

    private void chonSanPham(SanPham sp) {
        popupProductSearch.setVisible(false);
        txtUnifiedSearch.setText("");
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 2).equals(sp.getSku())) {
                int currentQty = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                int newQty = currentQty + 1;
                tableModel.setValueAt(String.valueOf(newQty), i, 4);
                
                long total = sp.getGiaBan().longValue() * newQty;
                tableModel.setValueAt(nf.format(total), i, 5); 
                capNhatTongTien();
                return;
            }
        }

        String nameHtml = "<html><b>" + sp.getTen() + "</b><br><span style='color:#888; font-size:9px;'>" 
                        + (sp.getThuongHieu() != null ? sp.getThuongHieu() : "") + "</span></html>";
        int stt = tableModel.getRowCount() + 1;
        
        tableModel.addRow(new Object[]{
            String.valueOf(stt),
            nameHtml,
            sp.getSku(),
            nf.format(sp.getGiaBan()),
            "1",
            nf.format(sp.getGiaBan()), 
            ""
        });
        
        capNhatTongTien();
    }
}