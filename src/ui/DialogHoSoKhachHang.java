package ui;

import dao.*;
import entity.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DialogHoSoKhachHang extends JDialog {
    private final String maKH;
    private final KhachHangDAO khDAO = new KhachHangDAO();
    private final HoaDonDAO hdDAO = new HoaDonDAO();
    private final ChamSocKhachHangDAO csDAO = new ChamSocKhachHangDAO();
    private final JTextArea thongTin = new JTextArea();
    private final DefaultTableModel modelHD = UIHelper.model("Mã hóa đơn", "Ngày mua", "Số sản phẩm", "Tổng tiền", "Đã hoàn", "Chi tiêu ròng");
    private final DefaultTableModel modelCS = UIHelper.model("Thời gian", "Loại", "Kênh", "Nội dung", "Trạng thái");
    private final JTable tableHD = new JTable(modelHD), tableCS = new JTable(modelCS);
    private final JButton btnTrangThai = UIHelper.nut("Đổi trạng thái");
    private ArrayList<HoaDon> hoaDon;
    private ArrayList<LichSuCSKH> lichSu;

    public DialogHoSoKhachHang(Window owner, String maKH) {
        super(owner, "Hồ sơ khách hàng", ModalityType.APPLICATION_MODAL);
        this.maKH = maKH;
        JPanel panel = UIHelper.theTrang();
        setContentPane(panel);
        thongTin.setEditable(false);
        thongTin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        thongTin.setBackground(new Color(239, 246, 255));
        thongTin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        thongTin.setLineWrap(true);
        thongTin.setWrapStyleWord(true);
        thongTin.setRows(7);
        panel.add(new JScrollPane(thongTin), BorderLayout.NORTH);
        UIHelper.dinhDangBang(tableHD); UIHelper.dinhDangBang(tableCS);
        JTabbedPane tabs = new JTabbedPane();
        JPanel muaHang = new JPanel(new BorderLayout(0, 10));
        muaHang.add(new JScrollPane(tableHD), BorderLayout.CENTER);
        JButton xemHD = UIHelper.nut("Xem hóa đơn"), hoan = UIHelper.nut("Hoàn tiền mẫu");
        muaHang.add(UIHelper.hangNut(xemHD, hoan), BorderLayout.SOUTH);
        tabs.addTab("Lịch sử mua hàng", muaHang);
        JPanel chamSoc = new JPanel(new BorderLayout(0, 10));
        chamSoc.add(new JScrollPane(tableCS), BorderLayout.CENTER);
        JButton ghiChu = UIHelper.nut("Thêm ghi chú"), xemGhiChu = UIHelper.nut("Xem nội dung");
        chamSoc.add(UIHelper.hangNut(ghiChu, xemGhiChu), BorderLayout.SOUTH);
        tabs.addTab("Lịch sử CSKH", chamSoc);
        panel.add(tabs, BorderLayout.CENTER);
        JButton sua = UIHelper.nutChinh("Chỉnh sửa"), dong = UIHelper.nut("Đóng");
        panel.add(UIHelper.hangNut(sua, btnTrangThai, dong), BorderLayout.SOUTH);
        sua.addActionListener(e -> {
            new DialogKhachHang(this, khDAO.timTheoMa(maKH)).setVisible(true);
            taiLai();
        });
        btnTrangThai.addActionListener(e -> {
            if (UIHelper.xacNhan(this, "Đổi trạng thái hoạt động của khách hàng?")) { khDAO.doiTrangThai(maKH); taiLai(); }
        });
        dong.addActionListener(e -> dispose());
        xemHD.addActionListener(e -> xemHoaDon());
        hoan.addActionListener(e -> hoanTien());
        ghiChu.addActionListener(e -> themGhiChu());
        xemGhiChu.addActionListener(e -> {
            if (tableCS.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch sử CSKH."); return; }
            JTextArea text = new JTextArea(lichSu.get(tableCS.getSelectedRow()).getNoiDung(), 8, 45);
            text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(text), "Nội dung CSKH", JOptionPane.INFORMATION_MESSAGE);
        });
        taiLai();
        setSize(1000, 670);
        setLocationRelativeTo(owner);
    }

    private void taiLai() {
        KhachHang kh = khDAO.timTheoMa(maKH);
        hoaDon = hdDAO.layTheoKhachHang(maKH);
        ChinhSachThanhVien cs = new ChinhSachThanhVienDAO().xacDinhHang(kh.getTongChiTieu());
        thongTin.setText(kh.getMaKhachHang() + " - " + kh.getHoTen() + " | Thẻ: " + kh.getMaTheThanhVien() + " | " + (kh.isHoatDong() ? "Đang hoạt động" : "Ngưng hoạt động")
            + "\nSĐT: " + kh.getSoDienThoai() + " | Email: " + (kh.getEmail().isEmpty() ? "Chưa cung cấp" : kh.getEmail())
            + "\nNgày sinh: " + (kh.getNgaySinh() == null ? "Chưa cung cấp" : UIHelper.NGAY.format(kh.getNgaySinh())) + " | Giới tính: " + kh.getGioiTinh()
            + "\nĐịa chỉ: " + kh.getDiaChi() + "\nNgày đăng ký: " + UIHelper.NGAY.format(kh.getNgayDangKy())
            + "\nHạng: " + kh.getHangThanhVien() + " | Ưu đãi: " + cs.getPhanTramGiam() + "%"
            + "\nTổng chi tiêu: " + UIHelper.tien(kh.getTongChiTieu()) + " | Số hóa đơn: " + hoaDon.size());
        thongTin.setCaretPosition(0);
        btnTrangThai.setText(kh.isHoatDong() ? "Ngưng hoạt động" : "Kích hoạt lại");
        modelHD.setRowCount(0);
        for (HoaDon hd : hoaDon) {
            int soLuong = 0;
            for (ChiTietHoaDon ct : hd.getChiTiet()) soLuong += ct.getSoLuong();
            modelHD.addRow(new Object[] {hd.getMaHoaDon(), UIHelper.NGAY.format(hd.getNgayLap()), soLuong,
                UIHelper.tien(hd.getTongTien()), UIHelper.tien(hd.getTienHoan()), UIHelper.tien(hd.getChiTieuRong())});
        }
        lichSu = csDAO.layLichSu(maKH);
        modelCS.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (LichSuCSKH item : lichSu) modelCS.addRow(new Object[] {fmt.format(item.getThoiGian()), item.getLoai(), item.getKenh(), item.getNoiDung(), item.getTrangThai()});
    }

    private HoaDon chonHoaDon() {
        if (tableHD.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn."); return null; }
        return hoaDon.get(tableHD.getSelectedRow());
    }

    private void xemHoaDon() {
        HoaDon hd = chonHoaDon();
        if (hd == null) return;
        DefaultTableModel model = UIHelper.model("SKU", "Sản phẩm", "SL", "Đơn giá", "Thành tiền");
        for (ChiTietHoaDon ct : hd.getChiTiet()) model.addRow(new Object[] {ct.getMaSKU(), ct.getTenSanPham(), ct.getSoLuong(),
            UIHelper.tien(ct.getDonGia()), UIHelper.tien(ct.getThanhTien())});
        JTable table = new JTable(model);
        UIHelper.dinhDangBang(table);
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(new JLabel(hd.getMaHoaDon() + " | " + UIHelper.NGAY.format(hd.getNgayLap()) + " | Đã thanh toán (dữ liệu mẫu)"), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table); scroll.setPreferredSize(new Dimension(780, 180));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(new JLabel("Tổng: " + UIHelper.tien(hd.getTongTien()) + " | Đã hoàn: " + UIHelper.tien(hd.getTienHoan()) + " | Chi tiêu ròng: " + UIHelper.tien(hd.getChiTieuRong())), BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, panel, "Chi tiết hóa đơn", JOptionPane.PLAIN_MESSAGE);
    }

    private void hoanTien() {
        HoaDon hd = chonHoaDon();
        if (hd == null) return;
        String text = JOptionPane.showInputDialog(this, "Mô phỏng hoàn tiền để kiểm tra chi tiêu và hạng thành viên.\nSố tiền còn lại: "
            + UIHelper.tien(hd.getChiTieuRong()) + "\nNhập tiền hoàn (VNĐ):");
        if (text == null) return;
        try {
            double tien = UIHelper.soTien(text, "Tiền hoàn");
            if (UIHelper.xacNhan(this, "Ghi nhận hoàn tiền mẫu " + UIHelper.tien(tien) + "?")) {
                hdDAO.hoanTienMau(hd.getMaHoaDon(), tien);
                taiLai();
            }
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }

    private void themGhiChu() {
        JComboBox<String> kenh = new JComboBox<>(new String[] {"Điện thoại", "Tại cửa hàng", "Email", "Zalo"});
        JTextArea noiDung = new JTextArea(5, 35);
        noiDung.setLineWrap(true); noiDung.setWrapStyleWord(true);
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(kenh, BorderLayout.NORTH); panel.add(new JScrollPane(noiDung), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, panel, "Ghi nhận chăm sóc khách hàng", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            csDAO.themGhiChu(maKH, (String) kenh.getSelectedItem(), noiDung.getText());
            taiLai();
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }
}
