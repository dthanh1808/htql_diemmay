package ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

// Cần add thư viện jcalendar-1.4.jar vào project
import com.toedter.calendar.JDateChooser;

public class PNL_BaoHanh extends JPanel {
    private Color bgMain = new Color(245, 247, 250);
    private Color bgWhite = Color.WHITE;
    private Color textDark = new Color(30, 30, 30);
    private Color textGray = new Color(130, 130, 130);
    private Color colorBlue = new Color(37, 99, 235);
    private Color bgHover = new Color(239, 246, 255);

    // CardLayout nội bộ để chuyển đổi giữa Danh sách và Form Lập phiếu
    private CardLayout cardLayout;
    private JPanel pnlCardContainer;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Các thành phần bộ lọc
    private JTextField txtSearch;
    private JComboBox<String> cbStatus;
    private JComboBox<String> cbType;
    private JDateChooser txtDate; 
    
    private final String SEARCH_PLACEHOLDER = " Tìm mã phiếu, mã đơn hàng, tên khách hàng...";

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

    // 1. ActionRenderer
    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JButton btnView = new JButton("Xem");
        private JButton btnEdit = new JButton("Sửa");

        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 6));
            setOpaque(true);
            styleTableButton(btnView, colorBlue, new Color(239, 246, 255)); 
            styleTableButton(btnEdit, new Color(220, 38, 38), new Color(254, 226, 226)); 
            add(btnView);
            add(btnEdit);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    // 2. ActionEditor
    class ActionEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnView;
        private JButton btnEdit;
        private int currentRow;

        public ActionEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            btnView = new JButton("Xem");
            btnEdit = new JButton("Sửa");
            styleTableButton(btnView, colorBlue, new Color(239, 246, 255)); 
            styleTableButton(btnEdit, new Color(220, 38, 38), new Color(254, 226, 226)); 

            btnView.addActionListener(e -> {
                fireEditingStopped();
                hienThiChiTiet(currentRow, false); 
            });
            
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                hienThiChiTiet(currentRow, true); 
            });

            panel.add(btnView);
            panel.add(btnEdit);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return ""; }
    }

    public PNL_BaoHanh() {
        setLayout(new BorderLayout());
        setBackground(bgMain);
        setBorder(new EmptyBorder(12, 15, 12, 15));

        // Khởi tạo CardLayout chứa 2 màn hình bên trong PNL_BaoHanh
        cardLayout = new CardLayout();
        pnlCardContainer = new JPanel(cardLayout);
        pnlCardContainer.setOpaque(false);

        // Thêm Card 1: Giao diện Danh sách
        pnlCardContainer.add(createDanhSachPanel(), "DANH_SACH");

        // Thêm Card 2: Giao diện Lập phiếu bảo hành
        pnlCardContainer.add(createLapPhieuPanel(), "LAP_PHIEU");

        add(pnlCardContainer, BorderLayout.CENTER);
        
        // Mặc định hiển thị danh sách khi vừa vào panel
        cardLayout.show(pnlCardContainer, "DANH_SACH");
    }

    // =========================================================================
    // TẠO GIAO DIỆN CARD 1: DANH SÁCH & BỘ LỌC
    // =========================================================================
    private JPanel createDanhSachPanel() {
        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 12));
        pnlDanhSach.setOpaque(false);

        JPanel pnlMainNorth = new JPanel(new BorderLayout(0, 10));
        pnlMainNorth.setOpaque(false);

        RoundedPanel pnlFilter = new RoundedPanel(12, bgWhite);
        pnlFilter.setLayout(new BorderLayout(8, 0));
        pnlFilter.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel pnlSearchBox = new JPanel(new BorderLayout(6, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230)),
            new EmptyBorder(0, 8, 0, 8)
        ));
        pnlSearchBox.setPreferredSize(new Dimension(300, 32)); 

        JLabel lblSearchIcon = new JLabel();
        setCustomIcon(lblSearchIcon, "/icon/ic_timkiem.png", 14, 14);

        txtSearch = new JTextField(SEARCH_PLACEHOLDER);
        txtSearch.setForeground(textGray);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
        txtSearch.setBorder(null);
        pnlSearchBox.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        JPanel pnlFilterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlFilterControls.setOpaque(false);

        cbStatus = new JComboBox<>(new String[]{
            "Trạng thái: Tất cả", "Đang xử lý", "Đã hoàn thành", "Từ chối"
        });
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbStatus.setBackground(Color.WHITE);
        cbStatus.setPreferredSize(new Dimension(145, 32));

        cbType = new JComboBox<>(new String[]{
            "Loại: Tất cả", "Bảo hành phần cứng", "Bảo hành phần mềm"
        });
        cbType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbType.setBackground(Color.WHITE);
        cbType.setPreferredSize(new Dimension(155, 32));
        
        txtDate = new JDateChooser();
        txtDate.setDateFormatString("dd/MM/yyyy"); 
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDate.setPreferredSize(new Dimension(130, 32));
        
        JTextField dateEditor = (JTextField) txtDate.getDateEditor().getUiComponent();
        dateEditor.setBackground(Color.WHITE);

        JButton btnClearFilter = createFlatButton("Xóa bộ lọc", Color.WHITE, new Color(220, 38, 38), new Color(220, 38, 38));
        btnClearFilter.setPreferredSize(new Dimension(90, 32));

        pnlFilterControls.add(cbStatus);
        pnlFilterControls.add(cbType);
        pnlFilterControls.add(txtDate);
        pnlFilterControls.add(btnClearFilter); 

        pnlFilter.add(pnlSearchBox, BorderLayout.WEST); 
        pnlFilter.add(pnlFilterControls, BorderLayout.EAST); 
        pnlMainNorth.add(pnlFilter, BorderLayout.NORTH);

        // Nút Tạo phiếu bảo hành (Xanh dương, căn trái)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        pnlActions.setOpaque(false);
        
        JButton btnCreate = createFlatButton("+ Tạo phiếu bảo hành", colorBlue, Color.WHITE, colorBlue);
        btnCreate.setPreferredSize(new Dimension(160, 36));
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Sự kiện: Chuyển sang Card Lập phiếu ngay trong panel này
        btnCreate.addActionListener(e -> cardLayout.show(pnlCardContainer, "LAP_PHIEU"));

        pnlActions.add(btnCreate);
        pnlMainNorth.add(pnlActions, BorderLayout.CENTER); 
        pnlDanhSach.add(pnlMainNorth, BorderLayout.NORTH);

        // Sự kiện lọc
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(SEARCH_PLACEHOLDER)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(textDark);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText(SEARCH_PLACEHOLDER);
                    txtSearch.setForeground(textGray);
                }
            }
        });

        cbStatus.addActionListener(e -> applyFilters());
        cbType.addActionListener(e -> applyFilters());

        txtDate.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("date".equals(evt.getPropertyName())) {
                    applyFilters();
                }
            }
        });

        btnClearFilter.addActionListener(e -> {
            txtSearch.setText(SEARCH_PLACEHOLDER);
            txtSearch.setForeground(textGray);
            txtDate.setDate(null); 
            cbStatus.setSelectedIndex(0);
            cbType.setSelectedIndex(0);
            if (rowSorter != null) {
                rowSorter.setRowFilter(null);
            }
        });

        // Bảng danh sách
        RoundedPanel pnlTableContainer = new RoundedPanel(12, bgWhite);
        pnlTableContainer.setLayout(new BorderLayout(0, 10));
        pnlTableContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 0));
        pnlTabs.setOpaque(false);
        pnlTabs.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 232, 235)));

        JLabel lblTab1 = new JLabel("Danh sách phiếu bảo hành");
        lblTab1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTab1.setForeground(colorBlue);
        lblTab1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, colorBlue),
            new EmptyBorder(0, 0, 8, 0)
        ));
        pnlTabs.add(lblTab1);
        pnlTableContainer.add(pnlTabs, BorderLayout.NORTH);

        String[] columns = {"STT", "Mã phiếu", "Ngày tạo", "Khách hàng", "Sản phẩm", "Loại bảo hành", "Trạng thái", "Thao tác"};
        String tagDangXuLy = "<html><span style='background-color:#ffedd5; color:#ea580c; padding:4px 10px; border-radius:10px; font-size:11px;'>● Đang xử lý</span></html>";
        String tagHoanThanh = "<html><span style='background-color:#dcfce7; color:#16a34a; padding:4px 10px; border-radius:10px; font-size:11px;'>● Đã hoàn thành</span></html>";
        String tagTuChoi = "<html><span style='background-color:#fee2e2; color:#dc2626; padding:4px 10px; border-radius:10px; font-size:11px;'>● Từ chối</span></html>";

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

        tableModel = new DefaultTableModel(data, columns) { 
            @Override
            public boolean isCellEditable(int row, int column) { return column == 7; }
        };

        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        table.setRowHeight(40); 
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(bgHover);
        table.setSelectionForeground(textDark);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(textDark);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);   
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  
        table.getColumnModel().getColumn(3).setPreferredWidth(160);  
        table.getColumnModel().getColumn(4).setPreferredWidth(160);  
        table.getColumnModel().getColumn(5).setPreferredWidth(150);  
        table.getColumnModel().getColumn(6).setPreferredWidth(130);  
        table.getColumnModel().getColumn(7).setPreferredWidth(120);  

        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 235)));
        scrollTable.getViewport().setBackground(bgWhite);
        pnlTableContainer.add(scrollTable, BorderLayout.CENTER);

        pnlDanhSach.add(pnlTableContainer, BorderLayout.CENTER);
        return pnlDanhSach;
    }

    // =========================================================================
    // TẠO GIAO DIỆN CARD 2: MÀN HÌNH LẬP PHIẾU (ĐÃ FIX KHÓA THÔNG TIN & BỎ TRẠNG THÁI)
    // =========================================================================
    private JPanel createLapPhieuPanel() {
        RoundedPanel pnlLapPhieu = new RoundedPanel(12, bgWhite);
        pnlLapPhieu.setLayout(new BorderLayout(0, 15));
        pnlLapPhieu.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblFormTitle = new JLabel("Lập Phiếu Bảo Hành Mới");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(textDark);
        pnlLapPhieu.add(lblFormTitle, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(bgWhite);

        // --- BƯỚC 1: XÁC THỰC ---
        JPanel pnlVerifyCard = new JPanel(new BorderLayout(0, 10));
        pnlVerifyCard.setBackground(new Color(248, 250, 252));
        pnlVerifyCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        pnlVerifyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel lblVerifyTitle = new JLabel("1. Xác thực thông tin sản phẩm (Nhập Mã hóa đơn hoặc Số Serial)");
        lblVerifyTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblVerifyTitle.setForeground(colorBlue);

        JPanel pnlVerifyInput = new JPanel(new BorderLayout(10, 0));
        pnlVerifyInput.setOpaque(false);
        JTextField txtInputKey = new JTextField();
        txtInputKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton btnCheck = createFlatButton("Kiểm tra", colorBlue, Color.WHITE, colorBlue);
        btnCheck.setPreferredSize(new Dimension(100, 32));

        pnlVerifyInput.add(txtInputKey, BorderLayout.CENTER);
        pnlVerifyInput.add(btnCheck, BorderLayout.EAST);

        JLabel lblCheckResult = new JLabel(" ");
        lblCheckResult.setFont(new Font("Segoe UI", Font.BOLD, 12));

        pnlVerifyCard.add(lblVerifyTitle, BorderLayout.NORTH);
        pnlVerifyCard.add(pnlVerifyInput, BorderLayout.CENTER);
        pnlVerifyCard.add(lblCheckResult, BorderLayout.SOUTH);

        pnlBody.add(pnlVerifyCard);
        pnlBody.add(Box.createVerticalStrut(15));

        // --- BƯỚC 2: THÔNG TIN CHI TIẾT (KHÓA DỮ LIỆU ĐỌC TỪ HÓA ĐƠN, CHỈ CHO CHỌN LOẠI BH VÀ NHẬP MÔ TẢ) ---
        JPanel pnlFormCard = new JPanel();
        pnlFormCard.setLayout(new BoxLayout(pnlFormCard, BoxLayout.Y_AXIS));
        pnlFormCard.setBackground(bgWhite);
        pnlFormCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230)),
            " 2. Thông tin chi tiết bảo hành ",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            textDark
        ));

        // Các trường thông tin (Sẽ bị khóa không cho sửa trực tiếp, load tự động theo hóa đơn)
        JTextField txtTenKH = createReadOnlyTextField("");
        JTextField txtSDT = createReadOnlyTextField("");
        JTextField txtDiaChi = createReadOnlyTextField("");
        
        JTextField txtSanPham = createReadOnlyTextField("");
        JTextField txtMaSP = createReadOnlyTextField("");
        JTextField txtIMEI = createReadOnlyTextField("");
        
        // Loại bảo hành (Cho phép nhân viên chọn)
        JComboBox<String> cbLoaiBH = new JComboBox<>(new String[]{"Bảo hành phần cứng", "Bảo hành phần mềm"});
        styleComboBox(cbLoaiBH);
        cbLoaiBH.setEnabled(false); // Ban đầu khóa

        // Mô tả / Ghi chú (Cho phép nhân viên nhập)
        JTextArea txtMoTa = new JTextArea();
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMoTa.setBackground(new Color(245, 247, 250));
        txtMoTa.setEditable(false); // Ban đầu khóa
        txtMoTa.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        JScrollPane scrollMoTa = new JScrollPane(txtMoTa);
        scrollMoTa.setPreferredSize(new Dimension(0, 65));

        // Thêm vào Form theo từng nhóm giống giao diện Chi tiết
        pnlFormCard.add(createDetailSectionHeader("Thông tin khách hàng", "/icon/ic_khachhang.png"));
        pnlFormCard.add(createFieldRow("Tên KH:", txtTenKH));
        pnlFormCard.add(createFieldRow("SĐT:", txtSDT));
        pnlFormCard.add(createFieldRow("Địa chỉ:", txtDiaChi));
        pnlFormCard.add(Box.createVerticalStrut(10));

        pnlFormCard.add(createDetailSectionHeader("Thông tin sản phẩm & Bảo hành", "/icon/ic_sanpham.png"));
        pnlFormCard.add(createFieldRow("Sản phẩm:", txtSanPham));
        pnlFormCard.add(createFieldRow("Mã SP:", txtMaSP));
        pnlFormCard.add(createFieldRow("IMEI:", txtIMEI));
        pnlFormCard.add(createFieldRow("Loại BH:", cbLoaiBH));
        // Đã bỏ dòng Trạng thái theo yêu cầu
        pnlFormCard.add(Box.createVerticalStrut(10));

        pnlFormCard.add(createDetailSectionHeader("Mô tả / Ghi chú", "/icon/ic_baohanh.png"));
        JPanel pnlMoTaRow = new JPanel(new BorderLayout(10, 0));
        pnlMoTaRow.setBackground(bgWhite);
        JLabel lblMoTa = new JLabel("Mô tả sự cố:");
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMoTa.setForeground(textGray);
        lblMoTa.setPreferredSize(new Dimension(80, 25));
        pnlMoTaRow.add(lblMoTa, BorderLayout.WEST);
        pnlMoTaRow.add(scrollMoTa, BorderLayout.CENTER);
        pnlFormCard.add(pnlMoTaRow);

        JScrollPane scrollForm = new JScrollPane(pnlFormCard);
        scrollForm.setBorder(null);
        scrollForm.getViewport().setBackground(bgWhite);

        pnlBody.add(scrollForm);
        pnlLapPhieu.add(pnlBody, BorderLayout.CENTER);

        // --- NÚT HÀNH ĐỘNG DƯỚI CÙNG ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setBackground(bgWhite);

        JButton btnBack = createFlatButton("Quay lại", Color.WHITE, textDark, new Color(210, 214, 220));
        JButton btnCreateTicket = createFlatButton("Tạo phiếu", new Color(22, 163, 74), Color.WHITE, new Color(22, 163, 74));
        btnCreateTicket.setEnabled(false);

        // Sự kiện Quay lại danh sách
        btnBack.addActionListener(e -> cardLayout.show(pnlCardContainer, "DANH_SACH"));

        // Sự kiện Kiểm tra (Bước 1) -> Load thông tin lên và mở khóa Loại BH + Mô tả
        btnCheck.addActionListener(e -> {
            String key = txtInputKey.getText().trim();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(pnlLapPhieu, "Vui lòng nhập Mã hóa đơn hoặc Số Serial!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (key.toLowerCase().contains("het")) {
                lblCheckResult.setText("<html><span style='color:#dc2626;'>Sản phẩm đã hết hạn bảo hành! Không thể lập phiếu.</span></html>");
                cbLoaiBH.setEnabled(false);
                txtMoTa.setEditable(false);
                txtMoTa.setBackground(new Color(245, 247, 250));
                btnCreateTicket.setEnabled(false);
            } else {
                lblCheckResult.setText("<html><span style='color:#16a34a;'>Sản phẩm còn hạn bảo hành.</span></html>");
                
                // Load dữ liệu tự động (Không cho sửa)
                txtTenKH.setText("Nguyễn Văn A");
                txtSDT.setText("0912345678");
                txtDiaChi.setText("123 Nguyễn Văn Cừ, Q.5, TP.HCM");
                txtSanPham.setText("Điện thoại Smartphone XYZ");
                txtMaSP.setText("SP0012");
                txtIMEI.setText("356789012345678");

                // Mở khóa phần nhân viên được phép thao tác
                cbLoaiBH.setEnabled(true);
                txtMoTa.setEditable(true);
                txtMoTa.setBackground(Color.WHITE);
                txtMoTa.setText(""); // Để trống cho nhân viên tự nhập mô tả lỗi

                btnCreateTicket.setEnabled(true);
            }
        });

        // Sự kiện Tạo phiếu
        btnCreateTicket.addActionListener(e -> {
            if (txtMoTa.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(pnlLapPhieu, "Vui lòng nhập mô tả sự cố bảo hành!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(pnlLapPhieu, "Lập phiếu bảo hành thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(pnlCardContainer, "DANH_SACH"); // Quay về danh sách
        });

        pnlBottom.add(btnBack);
        pnlBottom.add(btnCreateTicket);
        pnlLapPhieu.add(pnlBottom, BorderLayout.SOUTH);

        return pnlLapPhieu;
    }

    // =========================================================================
    // HÀM XỬ LÝ LỌC
    // =========================================================================
    private void applyFilters() {
        if (rowSorter == null) return;
        
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        
        String searchText = txtSearch.getText();
        if (!searchText.equals(SEARCH_PLACEHOLDER) && !searchText.trim().isEmpty()) {
            String lowerSearchText = searchText.trim().toLowerCase();
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    return entry.getStringValue(1).toLowerCase().contains(lowerSearchText) || 
                           entry.getStringValue(3).toLowerCase().contains(lowerSearchText) || 
                           entry.getStringValue(4).toLowerCase().contains(lowerSearchText);   
                }
            });
        }

        if (cbStatus.getSelectedIndex() > 0) { 
            String status = cbStatus.getSelectedItem().toString().toLowerCase();
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    return entry.getStringValue(6).toLowerCase().contains(status);
                }
            });
        }

        if (cbType.getSelectedIndex() > 0) { 
            String type = cbType.getSelectedItem().toString().toLowerCase();
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    return entry.getStringValue(5).toLowerCase().contains(type);
                }
            });
        }
        
        if (txtDate.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dateStr = sdf.format(txtDate.getDate());
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    return entry.getStringValue(2).contains(dateStr); 
                }
            });
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // =========================================================================
    // HÀM HIỂN THỊ POPUP CHI TIẾT
    // =========================================================================
    private void hienThiChiTiet(int row, boolean startInEditMode) {
        int modelRow = table.convertRowIndexToModel(row);

        String maPhieu = tableModel.getValueAt(modelRow, 1).toString();
        String khachHang = tableModel.getValueAt(modelRow, 3).toString();
        String sanPham = tableModel.getValueAt(modelRow, 4).toString();
        String loaiBH = tableModel.getValueAt(modelRow, 5).toString();
        
        String trangThaiHtml = tableModel.getValueAt(modelRow, 6).toString(); 
        String trangThaiText = "Đang xử lý";
        if(trangThaiHtml.contains("Đã hoàn thành")) trangThaiText = "Đã hoàn thành";
        else if(trangThaiHtml.contains("Từ chối")) trangThaiText = "Từ chối";

        JTextField txtTenKH = createTextField(khachHang);
        JTextField txtSDT = createTextField("0912345678");
        JTextField txtDiaChi = createTextField("123 Nguyễn Văn Cừ, Long Biên, HN");
        JTextField txtSanPham = createTextField(sanPham);
        JTextField txtMaSP = createTextField("SP0012");
        JTextField txtIMEI = createTextField("356789012345678");
        
        JComboBox<String> cbLoaiBH = new JComboBox<>(new String[]{"Bảo hành phần cứng", "Bảo hành phần mềm"});
        cbLoaiBH.setSelectedItem(loaiBH);
        styleComboBox(cbLoaiBH);

        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đang xử lý", "Đã hoàn thành", "Từ chối"});
        cbTrangThai.setSelectedItem(trangThaiText);
        styleComboBox(cbTrangThai);

        JTextArea txtGhiChu = new JTextArea("Máy không lên nguồn, cần gửi hãng kiểm tra.");
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        JComponent[] editables = {txtTenKH, txtSDT, txtDiaChi, txtSanPham, txtMaSP, txtIMEI, cbLoaiBH, cbTrangThai, txtGhiChu};

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Phiếu Bảo Hành: " + maPhieu, true);
        dialog.setSize(450, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setBackground(Color.WHITE);
        pnlWrap.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setBackground(Color.WHITE);

        pnlContent.add(createDetailSectionHeader("Thông tin khách hàng", "/icon/ic_khachhang.png"));
        pnlContent.add(createFieldRow("Tên KH:", txtTenKH));
        pnlContent.add(createFieldRow("SĐT:", txtSDT));
        pnlContent.add(createFieldRow("Địa chỉ:", txtDiaChi));
        pnlContent.add(Box.createVerticalStrut(15));

        pnlContent.add(createDetailSectionHeader("Thông tin sản phẩm & Bảo hành", "/icon/ic_sanpham.png"));
        pnlContent.add(createFieldRow("Sản phẩm:", txtSanPham));
        pnlContent.add(createFieldRow("Mã SP:", txtMaSP));
        pnlContent.add(createFieldRow("IMEI:", txtIMEI));
        pnlContent.add(createFieldRow("Loại BH:", cbLoaiBH));
        pnlContent.add(createFieldRow("Trạng thái:", cbTrangThai));
        pnlContent.add(Box.createVerticalStrut(15));

        pnlContent.add(createDetailSectionHeader("Mô tả / Ghi chú", "/icon/ic_baohanh.png"));
        JPanel pnlNote = new JPanel(new BorderLayout());
        pnlNote.setBackground(Color.WHITE);
        pnlNote.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane scrollNote = new JScrollPane(txtGhiChu);
        scrollNote.setPreferredSize(new Dimension(0, 60));
        pnlNote.add(scrollNote, BorderLayout.CENTER);
        pnlContent.add(pnlNote);

        pnlWrap.add(pnlContent, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnClose = createFlatButton("Đóng", Color.WHITE, textDark, new Color(210, 214, 220));
        JButton btnEdit = createFlatButton("Chỉnh sửa", colorBlue, Color.WHITE, colorBlue);
        JButton btnCancel = createFlatButton("Hủy", Color.WHITE, textDark, new Color(210, 214, 220));
        JButton btnSave = createFlatButton("Lưu cập nhật", new Color(22, 163, 74), Color.WHITE, new Color(22, 163, 74));

        Runnable updateBottomPanel = () -> {
            pnlBottom.removeAll();
            if (txtTenKH.isEditable()) { 
                pnlBottom.add(btnCancel);
                pnlBottom.add(btnSave);
            } else {                     
                pnlBottom.add(btnClose);
                pnlBottom.add(btnEdit);
            }
            pnlBottom.revalidate();
            pnlBottom.repaint();
        };

        toggleEditMode(editables, startInEditMode);
        updateBottomPanel.run();

        btnClose.addActionListener(e -> dialog.dispose());
        
        btnEdit.addActionListener(e -> {
            toggleEditMode(editables, true);
            updateBottomPanel.run();
        });

        btnCancel.addActionListener(e -> {
            toggleEditMode(editables, false);
            updateBottomPanel.run();
        });

        btnSave.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
            toggleEditMode(editables, false);
            updateBottomPanel.run();
        });

        pnlWrap.add(pnlBottom, BorderLayout.SOUTH);
        dialog.add(pnlWrap);
        dialog.setVisible(true); 
    }

    // =========================================================================
    // HÀM TIỆN ÍCH DÙNG CHUNG
    // =========================================================================
    private JButton createFlatButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight()); 
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createLineBorder(border, 1));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }
    
    private void styleTableButton(JButton btn, Color fgColor, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(fgColor);
        btn.setBackground(bgColor); 
        btn.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); 
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); 
        btn.setOpaque(true);             
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(50, 26)); 
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

    private JTextField createTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txt.setForeground(textDark);
        return txt;
    }

    // Tạo TextField ở trạng thái chỉ đọc (Read-only) cho form Lập phiếu
    private JTextField createReadOnlyTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txt.setForeground(textDark);
        txt.setEditable(false);
        txt.setBackground(new Color(245, 247, 250));
        txt.setBorder(BorderFactory.createEmptyBorder());
        return txt;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(Color.WHITE);
    }

    private JPanel createDetailSectionHeader(String title, String iconPath) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pnl.setBackground(Color.WHITE);
        JLabel lblIcon = new JLabel();
        setCustomIcon(lblIcon, iconPath, 14, 14);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(colorBlue);
        pnl.add(lblIcon);
        pnl.add(lblTitle);
        return pnl;
    }

    private JPanel createFieldRow(String label, JComponent field) {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKey.setForeground(textGray);
        lblKey.setPreferredSize(new Dimension(80, 25));
        pnl.add(lblKey, BorderLayout.WEST);
        pnl.add(field, BorderLayout.CENTER);
        return pnl;
    }

    private void toggleEditMode(JComponent[] components, boolean isEditable) {
        for (JComponent c : components) {
            if (c instanceof JTextField) {
                JTextField txt = (JTextField) c;
                txt.setEditable(isEditable);
                txt.setOpaque(isEditable);
                txt.setBorder(isEditable ? BorderFactory.createLineBorder(new Color(210, 214, 220)) : BorderFactory.createEmptyBorder());
            } else if (c instanceof JTextArea) {
                JTextArea txtArea = (JTextArea) c;
                txtArea.setEditable(isEditable);
                txtArea.setBackground(isEditable ? Color.WHITE : new Color(245, 247, 250));
            } else if (c instanceof JComboBox) {
                JComboBox<?> cb = (JComboBox<?>) c;
                cb.setEnabled(isEditable);
            }
        }
    }
}
