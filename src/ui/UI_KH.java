package ui;

import dao.*;
import entity.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UI_KH extends JPanel {
    private final KhachHangDAO dao = new KhachHangDAO();
    private final JTextField txtTim = new UIHelper.OGoiy("Nhập mã KH, tên khách hàng, SĐT, email...");
    private final JTextField txtChiTu = new JTextField(), txtChiDen = new JTextField();
    private final JTextField txtTuNgay = new UIHelper.OGoiy("Từ ngày"), txtDenNgay = new UIHelper.OGoiy("Đến ngày");
    private final JComboBox<String> cbHang = new JComboBox<>(new String[] {"Tất cả", "Member", "Silver", "Gold", "Diamond"});
    private final JComboBox<String> cbTrangThai = new JComboBox<>(new String[] {"Tất cả", "Đang hoạt động", "Ngưng hoạt động"});
    private final DefaultTableModel model = UIHelper.model("STT", "Mã KH", "Họ và tên", "SĐT", "Email", "Hạng thành viên", "Tổng chi tiêu", "Ngày đăng ký", "Trạng thái", "Thao tác");
    private final JTable table = new JTable(model);
    private final JLabel lblTong = new JLabel(), lblTrang = new JLabel();
    private final JButton btnTruoc = UIHelper.nut("<"), btnSau = UIHelper.nut(">");
    private ArrayList<KhachHang> ketQua = new ArrayList<>();
    private int trang = 1;
    private static final int SO_DONG = 8;
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

    public UI_KH() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UIHelper.NEN);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JPanel filter = UIHelper.theTrang();
        JPanel row = new JPanel(new GridBagLayout()); row.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setBackground(Color.WHITE);
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)), BorderFactory.createEmptyBorder(0, 10, 0, 4)));
        search.add(new JLabel(UIHelper.icon("ic_timkiem", 16)), BorderLayout.WEST);
        txtTim.setFont(new Font("Segoe UI", Font.PLAIN, 13)); txtTim.setBorder(BorderFactory.createEmptyBorder());
        search.add(txtTim, BorderLayout.CENTER);
        JButton tim = UIHelper.nut("›"); tim.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        tim.setToolTipText("Tìm kiếm khách hàng"); search.add(tim, BorderLayout.EAST);
        JPanel dates = new JPanel(new GridLayout(1, 2, 5, 0)); dates.setOpaque(false);
        for (JTextField field : new JTextField[] {txtTuNgay, txtDenNgay}) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            field.setToolTipText("dd/MM/yyyy, nhấn Enter để lọc");
            field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(0, 6, 0, 6)));
            dates.add(field);
        }
        String[] titles = {" ", "Hạng thành viên", "Trạng thái", "Ngày đăng ký"};
        JComponent[] controls = {search, cbHang, cbTrangThai, dates};
        int[] widths = {280, 110, 130, 190};
        for (int i = 0; i < controls.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = i;
            gbc.fill = GridBagConstraints.BOTH; gbc.weightx = i == 0 ? 2.5 : 1;
            gbc.insets = new Insets(0, 0, 0, i == controls.length - 1 ? 0 : 12);
            JPanel item = UIHelper.oLoc(titles[i], controls[i]);
            item.setPreferredSize(new Dimension(widths[i], 56)); row.add(item, gbc);
        }
        filter.add(row, BorderLayout.CENTER);
        JButton nangCao = UIHelper.nutIcon("Tìm kiếm nâng cao", "ic_loc");
        nangCao.setForeground(UIHelper.XANH);
        nangCao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nangCao.setPreferredSize(new Dimension(175, 36));
        JButton moi = UIHelper.nut("Làm mới");
        JButton them = UIHelper.nutChinh("Đăng ký khách hàng");
        them.setIcon(UIHelper.icon("ic_them", 16)); them.setIconTextGap(8);
        JPanel actions = new JPanel(new BorderLayout()); actions.setOpaque(false);
        actions.add(UIHelper.hangNut(nangCao, moi), BorderLayout.WEST);
        actions.add(UIHelper.hangNut(them), BorderLayout.EAST);
        filter.add(actions, BorderLayout.SOUTH);
        add(filter, BorderLayout.NORTH);

        JPanel content = UIHelper.theTrang();
        JPanel title = new JPanel(new BorderLayout()); title.setOpaque(false);
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTong.setForeground(new Color(30, 58, 138));
        lblTong.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        title.add(lblTong, BorderLayout.WEST);
        JButton xuat = UIHelper.nutIcon("Xuất file", "ic_xuatfile");
        xuat.setForeground(UIHelper.XANH); title.add(xuat, BorderLayout.EAST);
        content.add(title, BorderLayout.NORTH);
        UIHelper.dinhDangBang(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        int[] columns = {40, 80, 130, 90, 130, 115, 105, 95, 135, 76};
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columns[i]);
            table.getColumnModel().getColumn(i).setResizable(false);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(8).setMinWidth(120);
        table.getColumnModel().getColumn(9).setMinWidth(76);
        table.getColumnModel().getColumn(5).setCellRenderer(new UIHelper.NhanMau());
        table.getColumnModel().getColumn(8).setCellRenderer(new UIHelper.NhanMau());
        table.getColumnModel().getColumn(9).setCellRenderer(new UIHelper.ThaoTacRenderer(false));
        table.setToolTipText("Bấm icon để xem hồ sơ; nhấp chuột phải để chỉnh sửa, đổi trạng thái hoặc xóa.");
        content.add(UIHelper.cuonBang(table), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        lblTrang.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblTrang.setForeground(new Color(130, 130, 130));
        footer.add(lblTrang, BorderLayout.WEST);
        pnlPages.setOpaque(false); footer.add(pnlPages, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem hang = new JMenuItem("Chính sách thành viên"), cskh = new JMenuItem("Chăm sóc / Thông báo");
        JMenuItem xem = new JMenuItem("Xem hồ sơ"), sua = new JMenuItem("Chỉnh sửa");
        JMenuItem trangThai = new JMenuItem("Đổi trạng thái"), xoa = new JMenuItem("Xóa khách hàng");
        menu.add(hang); menu.add(cskh); menu.addSeparator();
        menu.add(xem); menu.add(sua); menu.add(trangThai); menu.add(xoa);
        table.setComponentPopupMenu(menu);
        tim.addActionListener(e -> taiLai());
        txtTim.addActionListener(e -> taiLai());
        txtTuNgay.addActionListener(e -> taiLai()); txtDenNgay.addActionListener(e -> taiLai());
        cbHang.addActionListener(e -> taiLai()); cbTrangThai.addActionListener(e -> taiLai());
        nangCao.addActionListener(e -> moLocNangCao());
        moi.addActionListener(e -> lamMoi());
        them.addActionListener(e -> moForm(null));
        sua.addActionListener(e -> { KhachHang kh = chonKhachHang(); if (kh != null) moForm(kh); });
        xem.addActionListener(e -> xemHoSo());
        trangThai.addActionListener(e -> doiTrangThai()); xoa.addActionListener(e -> xoa());
        xuat.addActionListener(e -> xuatFile());
        hang.addActionListener(e -> { new DialogChinhSach(SwingUtilities.getWindowAncestor(this)).setVisible(true); taiLai(); });
        cskh.addActionListener(e -> { new DialogChamSoc(SwingUtilities.getWindowAncestor(this)).setVisible(true); taiLai(); });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && table.rowAtPoint(e.getPoint()) >= 0) {
                    table.setRowSelectionInterval(table.rowAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || table.rowAtPoint(e.getPoint()) < 0) return;
                int col = table.convertColumnIndexToModel(table.columnAtPoint(e.getPoint()));
                if ((col == 9 && e.getClickCount() == 1) || (col != 9 && e.getClickCount() == 2)) xemHoSo();
            }
        });
        btnTruoc.addActionListener(e -> { trang--; hienDanhSach(); });
        btnSau.addActionListener(e -> { trang++; hienDanhSach(); });
        taiLai();
    }

    private void moLocNangCao() {
        String cuTu = txtChiTu.getText(), cuDen = txtChiDen.getText();
        JPanel form = new JPanel(new GridLayout(1, 2, 12, 0));
        form.add(UIHelper.oLoc("Tổng chi tiêu từ (VNĐ)", txtChiTu));
        form.add(UIHelper.oLoc("Đến (VNĐ)", txtChiDen));
        while (true) {
            if (JOptionPane.showConfirmDialog(this, form, "Tìm kiếm nâng cao", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                txtChiTu.setText(cuTu); txtChiDen.setText(cuDen); return;
            }
            try {
                double tu = txtChiTu.getText().trim().isEmpty() ? 0 : UIHelper.soTien(txtChiTu.getText(), "Chi tiêu từ");
                double den = txtChiDen.getText().trim().isEmpty() ? Double.MAX_VALUE : UIHelper.soTien(txtChiDen.getText(), "Chi tiêu đến");
                if (tu > den) throw new IllegalArgumentException("Chi tiêu từ không được lớn hơn chi tiêu đến.");
                taiLai(); return;
            } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
        }
    }

    private void xuatFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("khach-hang.csv"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Danh sách khách hàng CSV", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) file = new java.io.File(file.getPath() + ".csv");
        if (file.exists() && !UIHelper.xacNhan(this, "Tệp đã tồn tại. Ghi đè?")) return;
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            writer.print("\uFEFF");
            writer.println("Mã KH,Họ tên,Mã thẻ,SĐT,Email,Hạng,Tổng chi tiêu,Ngày đăng ký,Trạng thái");
            for (KhachHang kh : ketQua) {
                String[] values = {kh.getMaKhachHang(), kh.getHoTen(), kh.getMaTheThanhVien(), kh.getSoDienThoai(), kh.getEmail(),
                    kh.getHangThanhVien(), String.valueOf(kh.getTongChiTieu()), UIHelper.NGAY.format(kh.getNgayDangKy()), kh.isHoatDong() ? "Đang hoạt động" : "Ngưng hoạt động"};
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) writer.print(",");
                    writer.print("\"" + values[i].replace("\"", "\"\"") + "\"");
                }
                writer.println();
            }
            if (writer.checkError()) throw new java.io.IOException("Không thể ghi tệp CSV.");
        } catch (java.io.IOException ex) { UIHelper.loi(this, ex); return; }
        JOptionPane.showMessageDialog(this, "Đã xuất " + ketQua.size() + " khách hàng.");
    }

    public void taiLai() {
        try {
            double tu = txtChiTu.getText().trim().isEmpty() ? 0 : UIHelper.soTien(txtChiTu.getText(), "Chi tiêu từ");
            double den = txtChiDen.getText().trim().isEmpty() ? Double.MAX_VALUE : UIHelper.soTien(txtChiDen.getText(), "Chi tiêu đến");
            LocalDate tuNgay = UIHelper.ngay(txtTuNgay.getText(), "Ngày đăng ký từ");
            LocalDate denNgay = UIHelper.ngay(txtDenNgay.getText(), "Ngày đăng ký đến");
            if (tu > den) throw new IllegalArgumentException("Chi tiêu từ không được lớn hơn chi tiêu đến.");
            if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) throw new IllegalArgumentException("Ngày từ không được sau ngày đến.");
            ketQua = dao.timKiem(txtTim.getText(), (String) cbHang.getSelectedItem(), (String) cbTrangThai.getSelectedItem(), tu, den, tuNgay, denNgay);
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
            KhachHang kh = ketQua.get(i);
            model.addRow(new Object[] {i + 1, kh.getMaKhachHang(), kh.getHoTen(), kh.getSoDienThoai(), kh.getEmail(), kh.getHangThanhVien(),
                UIHelper.tien(kh.getTongChiTieu()), UIHelper.NGAY.format(kh.getNgayDangKy()), kh.isHoatDong() ? "Đang hoạt động" : "Ngưng hoạt động", ""});
        }
        lblTong.setText("<html>Danh sách khách hàng <span style='color:#6b7280; font-size:11px;'>(Tổng: " + ketQua.size() + " khách hàng)</span></html>");
        lblTrang.setText("Hiển thị " + (ketQua.isEmpty() ? 0 : batDau + 1) + " - " + ketThuc + " trong tổng số " + ketQua.size() + " khách hàng");
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

    private KhachHang chonKhachHang() {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng.");
            return null;
        }
        return ketQua.get((trang - 1) * SO_DONG + table.getSelectedRow());
    }

    private void moForm(KhachHang kh) {
        new DialogKhachHang(SwingUtilities.getWindowAncestor(this), kh).setVisible(true);
        taiLai();
    }

    private void xemHoSo() {
        KhachHang kh = chonKhachHang();
        if (kh == null) return;
        new DialogHoSoKhachHang(SwingUtilities.getWindowAncestor(this), kh.getMaKhachHang()).setVisible(true);
        taiLai();
    }

    private void doiTrangThai() {
        KhachHang kh = chonKhachHang();
        if (kh == null) return;
        if (UIHelper.xacNhan(this, "Chuyển khách hàng sang " + (kh.isHoatDong() ? "ngưng hoạt động" : "đang hoạt động") + "?")) {
            dao.doiTrangThai(kh.getMaKhachHang()); taiLai();
        }
    }

    private void xoa() {
        KhachHang kh = chonKhachHang();
        if (kh == null) return;
        String message = dao.coHoaDon(kh.getMaKhachHang()) ? "Khách đã có giao dịch. Chuyển sang ngưng hoạt động?"
            : "Xóa khách hàng " + kh.getHoTen() + " cùng các ghi chú CSKH liên quan?";
        if (UIHelper.xacNhan(this, message)) { dao.xoa(kh.getMaKhachHang()); taiLai(); }
    }

    public void lamMoi() {
        txtTim.setText(""); txtChiTu.setText(""); txtChiDen.setText(""); txtTuNgay.setText(""); txtDenNgay.setText("");
        cbHang.setSelectedIndex(0); cbTrangThai.setSelectedIndex(0);
        taiLai();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { System.err.println("Sử dụng giao diện Swing mặc định."); }
        SwingUtilities.invokeLater(() -> UIHelper.moPanel(new UI_KH(), "Quản lý khách hàng - Gia Thành"));
    }
}
