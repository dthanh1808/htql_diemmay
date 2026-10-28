package ui;

import dao.*;
import entity.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UI_SP extends JPanel {
    private final SanPhamDAO dao = new SanPhamDAO();
    private final JTextField txtTim = new UIHelper.OGoiy("Tìm kiếm theo tên sản phẩm, mã SKU...");
    private final JTextField txtGiaTu = new JTextField(), txtGiaDen = new JTextField();
    private final JComboBox<DanhMuc> cbDanhMuc = new JComboBox<>();
    private final JComboBox<String> cbHang = new JComboBox<>();
    private final JComboBox<String> cbGia = new JComboBox<>(new String[] {"Tất cả", "Dưới 5 triệu", "5 - 15 triệu", "Trên 15 triệu", "Tùy chọn..."});
    private final JComboBox<String> cbTrangThai = new JComboBox<>(new String[] {"Tất cả", "Đang kinh doanh", "Ngưng kinh doanh"});
    private final JComboBox<String> cbBaoHanh = new JComboBox<>(new String[] {"Tất cả", "12 tháng", "24 tháng", "36 tháng"});
    private final DefaultTableModel model = new DefaultTableModel(
        new String[] {"STT", "Hình ảnh", "Tên sản phẩm", "Danh mục", "Thương hiệu", "SKU", "Giá bán", "Bảo hành", "Trạng thái", "Thao tác"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
        @Override public Class<?> getColumnClass(int col) { return col == 1 ? Icon.class : Object.class; }
    };
    private final JTable table = new JTable(model);
    private final JLabel lblTong = new JLabel(), lblTrang = new JLabel();
    private final JButton btnTruoc = UIHelper.nut("<"), btnSau = UIHelper.nut(">");
    private ArrayList<SanPham> ketQua = new ArrayList<>();
    private int trang = 1;
    private static final int SO_DONG = 8;
    private boolean dangTaiBoLoc;
    private boolean dangTaiGia;
    private int giaDaChon;
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

    public UI_SP() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UIHelper.NEN);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel filter = UIHelper.theTrang();
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setBackground(Color.WHITE);
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)), BorderFactory.createEmptyBorder(0, 10, 0, 4)));
        search.add(new JLabel(UIHelper.icon("ic_timkiem", 16)), BorderLayout.WEST);
        txtTim.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTim.setBorder(BorderFactory.createEmptyBorder());
        search.add(txtTim, BorderLayout.CENTER);
        JButton tim = UIHelper.nut("›");
        tim.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        tim.setToolTipText("Tìm kiếm sản phẩm");
        search.add(tim, BorderLayout.EAST);
        String[] titles = {" ", "Danh mục", "Thương hiệu", "Giá", "Bảo hành", "Trạng thái"};
        JComponent[] controls = {search, cbDanhMuc, cbHang, cbGia, cbBaoHanh, cbTrangThai};
        for (int i = 0; i < controls.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i; gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = i == 0 ? 2.5 : 1;
            gbc.insets = new Insets(0, 0, 0, i == controls.length - 1 ? 0 : 12);
            JPanel item = UIHelper.oLoc(titles[i], controls[i]);
            item.setPreferredSize(new Dimension(i == 0 ? 280 : 110, 56));
            row.add(item, gbc);
        }
        filter.add(row, BorderLayout.CENTER);
        JButton them = UIHelper.nutChinh("Thêm sản phẩm");
        them.setIcon(UIHelper.icon("ic_them", 16)); them.setIconTextGap(8);
        them.setPreferredSize(new Dimension(160, 36));
        JButton danhMuc = UIHelper.nutIcon("Quản lý danh mục", "ic_danhmuc");
        danhMuc.setPreferredSize(new Dimension(185, 36));
        JButton moi = UIHelper.nut("Làm mới");
        JPanel actions = new JPanel(new BorderLayout()); actions.setOpaque(false);
        actions.add(UIHelper.hangNut(them, danhMuc), BorderLayout.WEST);
        actions.add(UIHelper.hangNut(moi), BorderLayout.EAST);
        filter.add(actions, BorderLayout.SOUTH);
        add(filter, BorderLayout.NORTH);

        JPanel content = UIHelper.theTrang();
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTong.setForeground(new Color(30, 58, 138));
        content.add(lblTong, BorderLayout.NORTH);
        UIHelper.dinhDangBang(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        int[] widths = {40, 80, 210, 90, 110, 65, 110, 85, 130, 76};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setResizable(false);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(8).setMinWidth(120);
        table.getColumnModel().getColumn(9).setMinWidth(76);
        table.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.NhanMau());
        table.getColumnModel().getColumn(8).setCellRenderer(new UIHelper.NhanMau());
        table.getColumnModel().getColumn(9).setCellRenderer(new UIHelper.ThaoTacRenderer(true));
        table.setToolTipText("Bấm icon để xem/sửa; nhấp chuột phải để đổi trạng thái hoặc xóa.");
        content.add(UIHelper.cuonBang(table), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        lblTrang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTrang.setForeground(new Color(130, 130, 130));
        footer.add(lblTrang, BorderLayout.WEST);
        pnlPages.setOpaque(false); footer.add(pnlPages, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem xem = new JMenuItem("Xem chi tiết"), sua = new JMenuItem("Chỉnh sửa");
        JMenuItem trangThai = new JMenuItem("Đổi trạng thái"), xoa = new JMenuItem("Xóa sản phẩm");
        JMenuItem gia = new JMenuItem("Lọc khoảng giá tùy chọn...");
        menu.add(xem); menu.add(sua); menu.add(trangThai); menu.add(xoa); menu.addSeparator(); menu.add(gia);
        table.setComponentPopupMenu(menu);
        gia.addActionListener(e -> moKhoangGia());
        tim.addActionListener(e -> timKiem());
        txtTim.addActionListener(e -> timKiem());
        cbDanhMuc.addActionListener(e -> { if (!dangTaiBoLoc) timKiem(); });
        cbHang.addActionListener(e -> { if (!dangTaiBoLoc) timKiem(); });
        cbTrangThai.addActionListener(e -> timKiem());
        cbBaoHanh.addActionListener(e -> timKiem());
        cbGia.addActionListener(e -> chonKhoangGia());
        moi.addActionListener(e -> lamMoi());
        them.addActionListener(e -> moForm(null, false));
        danhMuc.addActionListener(e -> {
            new DialogDanhMuc(SwingUtilities.getWindowAncestor(this)).setVisible(true);
            taiBoLoc(); timKiem();
        });
        xem.addActionListener(e -> { SanPham sp = chonSanPham(); if (sp != null) moForm(sp, true); });
        sua.addActionListener(e -> { SanPham sp = chonSanPham(); if (sp != null) moForm(sp, false); });
        trangThai.addActionListener(e -> doiTrangThai());
        xoa.addActionListener(e -> xoa());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && table.rowAtPoint(e.getPoint()) >= 0) {
                    table.setRowSelectionInterval(table.rowAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || table.rowAtPoint(e.getPoint()) < 0) return;
                int viewCol = table.columnAtPoint(e.getPoint());
                int col = table.convertColumnIndexToModel(viewCol);
                if (col == 9 && e.getClickCount() == 1) {
                    int x = e.getX() - table.getCellRect(table.getSelectedRow(), viewCol, true).x;
                    moForm(chonSanPham(), x < table.getColumnModel().getColumn(viewCol).getWidth() / 2);
                } else if (col != 9 && e.getClickCount() == 2) moForm(chonSanPham(), true);
            }
        });
        btnTruoc.addActionListener(e -> { trang--; hienDanhSach(); });
        btnSau.addActionListener(e -> { trang++; hienDanhSach(); });
        taiBoLoc(); timKiem();
    }

    private void chonKhoangGia() {
        if (dangTaiGia) return;
        int index = cbGia.getSelectedIndex();
        if (index == 4) { moKhoangGia(); return; }
        String[] tu = {"", "", "5000000", "15000001"};
        String[] den = {"", "4999999", "15000000", ""};
        txtGiaTu.setText(tu[index]); txtGiaDen.setText(den[index]);
        giaDaChon = index;
        timKiem();
    }

    private void moKhoangGia() {
        String cuTu = txtGiaTu.getText(), cuDen = txtGiaDen.getText();
        JPanel form = new JPanel(new GridLayout(1, 2, 12, 0));
        form.add(UIHelper.oLoc("Giá từ (VNĐ)", txtGiaTu));
        form.add(UIHelper.oLoc("Giá đến (VNĐ)", txtGiaDen));
        while (true) {
            if (JOptionPane.showConfirmDialog(this, form, "Lọc khoảng giá", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                txtGiaTu.setText(cuTu); txtGiaDen.setText(cuDen);
                dangTaiGia = true; cbGia.setSelectedIndex(giaDaChon); dangTaiGia = false;
                return;
            }
            try {
                double tu = txtGiaTu.getText().trim().isEmpty() ? 0 : UIHelper.soTien(txtGiaTu.getText(), "Giá từ");
                double den = txtGiaDen.getText().trim().isEmpty() ? Double.MAX_VALUE : UIHelper.soTien(txtGiaDen.getText(), "Giá đến");
                if (tu > den) throw new IllegalArgumentException("Giá từ không được lớn hơn giá đến.");
                dangTaiGia = true; cbGia.setSelectedIndex(4); dangTaiGia = false; giaDaChon = 4;
                timKiem(); return;
            } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
        }
    }

    private void taiBoLoc() {
        dangTaiBoLoc = true;
        DanhMuc cu = (DanhMuc) cbDanhMuc.getSelectedItem();
        String hangCu = (String) cbHang.getSelectedItem();
        cbDanhMuc.removeAllItems();
        cbDanhMuc.addItem(new DanhMuc("", "Tất cả"));
        for (DanhMuc dm : new DanhMucDAO().layTatCa()) {
            cbDanhMuc.addItem(dm);
            if (cu != null && cu.getMaDanhMuc().equals(dm.getMaDanhMuc())) cbDanhMuc.setSelectedItem(dm);
        }
        cbHang.removeAllItems(); cbHang.addItem("Tất cả");
        ArrayList<String> hangs = new ArrayList<>();
        for (SanPham sp : dao.layTatCa()) if (!hangs.contains(sp.getThuongHieu())) hangs.add(sp.getThuongHieu());
        for (String hang : hangs) cbHang.addItem(hang);
        if (hangs.contains(hangCu)) cbHang.setSelectedItem(hangCu);
        dangTaiBoLoc = false;
    }

    private void timKiem() {
        try {
            double tu = txtGiaTu.getText().trim().isEmpty() ? 0 : UIHelper.soTien(txtGiaTu.getText(), "Giá từ");
            double den = txtGiaDen.getText().trim().isEmpty() ? Double.MAX_VALUE : UIHelper.soTien(txtGiaDen.getText(), "Giá đến");
            if (tu > den) throw new IllegalArgumentException("Giá từ không được lớn hơn giá đến.");
            DanhMuc dm = (DanhMuc) cbDanhMuc.getSelectedItem();
            String hang = cbHang.getSelectedIndex() <= 0 ? "" : (String) cbHang.getSelectedItem();
            int thang = cbBaoHanh.getSelectedIndex() == 0 ? 0 : Integer.parseInt(((String) cbBaoHanh.getSelectedItem()).split(" ")[0]);
            ketQua = dao.timKiem(txtTim.getText(), dm == null ? "" : dm.getMaDanhMuc(), hang, tu, den,
                (String) cbTrangThai.getSelectedItem(), thang);
            trang = 1;
            hienDanhSach();
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }

    private void hienDanhSach() {
        int soTrang = Math.max(1, (ketQua.size() + SO_DONG - 1) / SO_DONG);
        trang = Math.max(1, Math.min(trang, soTrang));
        model.setRowCount(0);
        int batDau = (trang - 1) * SO_DONG;
        int ketThuc = Math.min(batDau + SO_DONG, ketQua.size());
        for (int i = batDau; i < ketThuc; i++) {
            SanPham sp = ketQua.get(i);
            ImageIcon anh = null;
            if (!sp.getHinhAnh().isEmpty() && new java.io.File(sp.getHinhAnh()).isFile()) {
                anh = new ImageIcon(new ImageIcon(sp.getHinhAnh()).getImage().getScaledInstance(40, 34, Image.SCALE_SMOOTH));
            }
            model.addRow(new Object[] {i + 1, anh, sp.getTenSanPham(), sp.getDanhMuc(), sp.getThuongHieu(), sp.getMaSKU(),
                UIHelper.tien(sp.getGiaBan()), sp.getBaoHanh() + " tháng", sp.isKinhDoanh() ? "Đang kinh doanh" : "Ngưng kinh doanh", ""});
        }
        lblTong.setText("<html>Danh sách sản phẩm <span style='color:#6b7280; font-size:11px;'>(Tổng: " + ketQua.size() + " sản phẩm)</span></html>");
        lblTrang.setText("Hiển thị " + (ketQua.isEmpty() ? 0 : batDau + 1) + " - " + ketThuc + " trong tổng số " + ketQua.size() + " sản phẩm");
        pnlPages.removeAll();
        btnTruoc.setPreferredSize(new Dimension(32, 32)); btnSau.setPreferredSize(new Dimension(32, 32));
        btnTruoc.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        btnSau.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        pnlPages.add(btnTruoc);
        for (int i = 1; i <= soTrang; i++) {
            if (i != 1 && i != soTrang && Math.abs(i - trang) > 1) {
                if (i == 2 || i == soTrang - 1) pnlPages.add(new JLabel("..."));
                continue;
            }
            final int page = i;
            JButton button = i == trang ? UIHelper.nutChinh(String.valueOf(i)) : UIHelper.nut(String.valueOf(i));
            button.setBorder(i == trang ? BorderFactory.createEmptyBorder() : BorderFactory.createLineBorder(new Color(230, 230, 230)));
            button.setPreferredSize(new Dimension(32, 32));
            button.addActionListener(e -> { trang = page; hienDanhSach(); });
            pnlPages.add(button);
        }
        pnlPages.add(btnSau); pnlPages.revalidate(); pnlPages.repaint();
        btnTruoc.setEnabled(trang > 1); btnSau.setEnabled(trang < soTrang);
    }

    private SanPham chonSanPham() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm.");
            return null;
        }
        return ketQua.get((trang - 1) * SO_DONG + table.getSelectedRow());
    }

    private void moForm(SanPham sp, boolean chiXem) {
        new DialogSanPham(SwingUtilities.getWindowAncestor(this), sp, chiXem).setVisible(true);
        if (!chiXem) { taiBoLoc(); timKiem(); }
    }

    private void doiTrangThai() {
        SanPham sp = chonSanPham();
        if (sp == null) return;
        if (UIHelper.xacNhan(this, "Chuyển sản phẩm sang " + (sp.isKinhDoanh() ? "ngưng kinh doanh" : "đang kinh doanh") + "?")) {
            dao.doiTrangThai(sp.getMaSKU()); timKiem();
        }
    }

    private void xoa() {
        SanPham sp = chonSanPham();
        if (sp == null) return;
        String message = dao.coHoaDon(sp.getMaSKU()) ? "Sản phẩm đã có hóa đơn. Chuyển sang ngưng kinh doanh?" : "Xóa sản phẩm " + sp.getTenSanPham() + "?";
        if (UIHelper.xacNhan(this, message)) { dao.xoa(sp.getMaSKU()); taiBoLoc(); timKiem(); }
    }

    public void lamMoi() {
        txtTim.setText(""); txtGiaTu.setText(""); txtGiaDen.setText("");
        dangTaiGia = true; cbGia.setSelectedIndex(0); dangTaiGia = false; giaDaChon = 0;
        taiBoLoc();
        dangTaiBoLoc = true;
        cbDanhMuc.setSelectedIndex(0); cbHang.setSelectedIndex(0);
        dangTaiBoLoc = false;
        cbBaoHanh.setSelectedIndex(0); cbTrangThai.setSelectedIndex(0);
        timKiem();
    }

    public void taiLai() {
        taiBoLoc();
        timKiem();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { System.err.println("Sử dụng giao diện Swing mặc định."); }
        SwingUtilities.invokeLater(() -> UIHelper.moPanel(new UI_SP(), "Quản lý sản phẩm - Gia Thành"));
    }
}
