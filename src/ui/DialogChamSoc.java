package ui;

import dao.*;
import entity.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DialogChamSoc extends JDialog {
    private final ChamSocKhachHangDAO dao = new ChamSocKhachHangDAO();
    private final JComboBox<KhuyenMai> cbKM = new JComboBox<>();
    private final JComboBox<String> cbKenh = new JComboBox<>(new String[] {"SMS", "Email", "Zalo OA"});
    private final JComboBox<String> cbHang = new JComboBox<>(new String[] {"Tất cả", "Member", "Silver", "Gold", "Diamond"});
    private final JComboBox<DanhMuc> cbDanhMuc = new JComboBox<>();
    private final JTextField txtTim = new JTextField();
    private final JTextArea txtNoiDung = new JTextArea(4, 45);
    private final DefaultTableModel model = UIHelper.model("Mã KH", "Họ tên", "SĐT", "Email", "Hạng");
    private final JTable table = new JTable(model);
    private final JLabel lblSoLuong = new JLabel();
    private ArrayList<KhachHang> ketQua;

    public DialogChamSoc(Window owner) {
        super(owner, "Chăm sóc khách hàng và thông báo khuyến mãi", ModalityType.APPLICATION_MODAL);
        JPanel panel = UIHelper.theTrang();
        setContentPane(panel);
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(new JLabel("Chế độ dữ liệu mẫu: chỉ lưu lịch sử mô phỏng, chưa gửi SMS/Email/Zalo thực tế."), BorderLayout.NORTH);
        JPanel filter = new JPanel(new GridLayout(1, 4, 10, 0));
        filter.setOpaque(false);
        filter.add(UIHelper.oLoc("Tên / mã KH / SĐT", txtTim));
        filter.add(UIHelper.oLoc("Hạng thành viên", cbHang));
        cbDanhMuc.addItem(new DanhMuc("", "Tất cả danh mục"));
        for (DanhMuc dm : new DanhMucDAO().layTatCa()) cbDanhMuc.addItem(dm);
        filter.add(UIHelper.oLoc("Danh mục đã mua", cbDanhMuc));
        JButton loc = UIHelper.nut("Lọc người nhận");
        filter.add(loc);
        top.add(filter, BorderLayout.CENTER);
        top.add(lblSoLuong, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        UIHelper.dinhDangBang(table);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        JButton chonTatCa = UIHelper.nut("Chọn tất cả kết quả"), boChon = UIHelper.nut("Bỏ chọn"), lichSu = UIHelper.nut("Lịch sử khách đã chọn");
        bottom.add(UIHelper.hangNut(chonTatCa, boChon, lichSu), BorderLayout.NORTH);
        JPanel soan = new JPanel(new BorderLayout(0, 8));
        soan.setOpaque(false);
        JPanel luaChon = new JPanel(new GridLayout(1, 2, 10, 0));
        luaChon.setOpaque(false);
        for (KhuyenMai km : dao.layKhuyenMai()) cbKM.addItem(km);
        luaChon.add(UIHelper.oLoc("Chương trình khuyến mãi mẫu", cbKM));
        luaChon.add(UIHelper.oLoc("Kênh thông báo", cbKenh));
        soan.add(luaChon, BorderLayout.NORTH);
        txtNoiDung.setLineWrap(true); txtNoiDung.setWrapStyleWord(true);
        txtNoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        soan.add(new JScrollPane(txtNoiDung), BorderLayout.CENTER);
        soan.add(new JLabel("Có thể dùng {ten} để chèn tên khách. Giữ Ctrl khi chọn nhiều khách trong bảng."), BorderLayout.SOUTH);
        bottom.add(soan, BorderLayout.CENTER);
        JButton gui = UIHelper.nutChinh("Gửi mô phỏng"), dong = UIHelper.nut("Đóng");
        bottom.add(UIHelper.hangNut(gui, dong), BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        loc.addActionListener(e -> loc());
        txtTim.addActionListener(e -> loc());
        cbHang.addActionListener(e -> loc());
        cbDanhMuc.addActionListener(e -> loc());
        chonTatCa.addActionListener(e -> { if (table.getRowCount() > 0) table.selectAll(); });
        boChon.addActionListener(e -> table.clearSelection());
        cbKM.addActionListener(e -> dienMau());
        gui.addActionListener(e -> gui());
        dong.addActionListener(e -> dispose());
        lichSu.addActionListener(e -> xemLichSu());
        table.getSelectionModel().addListSelectionListener(e -> capNhatSoLuong());
        dienMau(); loc();
        setSize(1000, 740);
        setLocationRelativeTo(owner);
    }

    private void dienMau() {
        KhuyenMai km = (KhuyenMai) cbKM.getSelectedItem();
        txtNoiDung.setText(km == null ? "" : km.getNoiDungMau());
    }

    private void loc() {
        DanhMuc dm = (DanhMuc) cbDanhMuc.getSelectedItem();
        ketQua = dao.chonDoiTuong(txtTim.getText(), (String) cbHang.getSelectedItem(), dm == null ? "" : dm.getMaDanhMuc());
        model.setRowCount(0);
        for (KhachHang kh : ketQua) model.addRow(new Object[] {kh.getMaKhachHang(), kh.getHoTen(), kh.getSoDienThoai(), kh.getEmail(), kh.getHangThanhVien()});
        capNhatSoLuong();
    }

    private void capNhatSoLuong() {
        lblSoLuong.setText("Khách đang hoạt động: " + table.getRowCount() + " | Đã chọn: " + table.getSelectedRowCount());
    }

    private void gui() {
        ArrayList<String> maKH = new ArrayList<>();
        for (int row : table.getSelectedRows()) maKH.add(ketQua.get(row).getMaKhachHang());
        try {
            if (maKH.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất một khách hàng.");
            KhuyenMai km = (KhuyenMai) cbKM.getSelectedItem();
            if (km == null) throw new IllegalArgumentException("Vui lòng chọn khuyến mãi.");
            if (!UIHelper.xacNhan(this, "Lưu thông báo mô phỏng cho " + maKH.size() + " khách hàng?")) return;
            int soLuong = dao.guiMoPhong(km.getMaKhuyenMai(), maKH, (String) cbKenh.getSelectedItem(), txtNoiDung.getText());
            JOptionPane.showMessageDialog(this, "Đã lưu " + soLuong + " lịch sử thông báo mô phỏng. Chưa gửi thực tế.");
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }

    private void xemLichSu() {
        if (table.getSelectedRowCount() != 1) { JOptionPane.showMessageDialog(this, "Chọn một khách hàng để xem lịch sử."); return; }
        KhachHang kh = ketQua.get(table.getSelectedRow());
        new DialogHoSoKhachHang(this, kh.getMaKhachHang()).setVisible(true);
        loc();
    }
}
