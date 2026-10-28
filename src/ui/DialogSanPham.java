package ui;

import dao.*;
import entity.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Dùng chung cho thêm, sửa và xem chi tiết, không thay đổi dữ liệu trước khi Lưu. */
public class DialogSanPham extends JDialog {
    private final SanPhamDAO dao = new SanPhamDAO();
    private final SanPham sanPham;
    private JTextField txtSKU = new JTextField(), txtMaVach = new JTextField(), txtTen = new JTextField();
    private JTextField txtHang = new JTextField(), txtModel = new JTextField(), txtGia = new JTextField();
    private JTextField txtBaoHanh = new JTextField("12");
    private JComboBox<DanhMuc> cbDanhMuc = new JComboBox<>();
    private JComboBox<String> cbNhom = new JComboBox<>(new String[] {"Gọn nhẹ", "Cồng kềnh"});
    private JComboBox<String> cbTrangThai = new JComboBox<>(new String[] {"Đang kinh doanh", "Ngưng kinh doanh"});
    private JTextArea txtMoTa = new JTextArea(4, 24);
    private JLabel lblAnh = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
    private String duongDanAnh = "";

    public DialogSanPham(Window owner, SanPham sanPham, boolean chiXem) {
        super(owner, chiXem ? "Chi tiết sản phẩm" : sanPham == null ? "Thêm sản phẩm" : "Chỉnh sửa sản phẩm", ModalityType.APPLICATION_MODAL);
        this.sanPham = sanPham;
        JPanel panel = UIHelper.theTrang();
        setContentPane(panel);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        UIHelper.themDong(form, "Mã SKU *", txtSKU);
        UIHelper.themDong(form, "Mã vạch * (nhập hoặc quét)", txtMaVach);
        txtMaVach.setToolTipText("Máy quét mã vạch dạng bàn phím: đặt con trỏ vào đây rồi quét.");
        txtMaVach.addActionListener(e -> txtTen.requestFocusInWindow());
        UIHelper.themDong(form, "Tên sản phẩm *", txtTen);
        UIHelper.themDong(form, "Danh mục *", cbDanhMuc);
        for (DanhMuc dm : new DanhMucDAO().layTatCa()) cbDanhMuc.addItem(dm);
        UIHelper.themDong(form, "Thương hiệu *", txtHang);
        UIHelper.themDong(form, "Model *", txtModel);
        UIHelper.themDong(form, "Giá bán * (VNĐ, nhập số)", txtGia);
        UIHelper.themDong(form, "Bảo hành (tháng)", txtBaoHanh);
        UIHelper.themDong(form, "Nhóm sản phẩm *", cbNhom);
        UIHelper.themDong(form, "Trạng thái", cbTrangThai);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        UIHelper.themDong(form, "Mô tả / thông số kỹ thuật", new JScrollPane(txtMoTa));
        JButton chonAnh = UIHelper.nut("Chọn ảnh JPG/PNG (tối đa 5 MB)");
        JPanel anh = new JPanel(new BorderLayout());
        anh.setOpaque(false);
        lblAnh.setPreferredSize(new Dimension(170, 110));
        anh.add(lblAnh, BorderLayout.CENTER);
        anh.add(chonAnh, BorderLayout.SOUTH);
        UIHelper.themDong(form, "Hình ảnh sản phẩm", anh);
        panel.add(new JScrollPane(form), BorderLayout.CENTER);
        JButton luu = UIHelper.nutChinh("Lưu sản phẩm");
        JButton huy = UIHelper.nut(chiXem ? "Đóng" : "Hủy");
        panel.add(UIHelper.hangNut(luu, huy), BorderLayout.SOUTH);
        luu.addActionListener(e -> luu());
        huy.addActionListener(e -> dispose());
        chonAnh.addActionListener(e -> chonAnh());
        if (sanPham != null) dienThongTin();
        txtSKU.setEditable(sanPham == null);
        cbTrangThai.setEnabled(sanPham != null);
        if (chiXem) {
            for (Component c : form.getComponents()) c.setEnabled(false);
            JTextField[] fields = {txtSKU, txtMaVach, txtTen, txtHang, txtModel, txtGia, txtBaoHanh};
            for (JTextField field : fields) field.setEditable(false);
            txtMoTa.setEditable(false);
            chonAnh.setEnabled(false);
            luu.setVisible(false);
        }
        setSize(760, 730);
        setLocationRelativeTo(owner);
    }

    private void dienThongTin() {
        txtSKU.setText(sanPham.getMaSKU());
        txtMaVach.setText(sanPham.getMaVach());
        txtTen.setText(sanPham.getTenSanPham());
        cbDanhMuc.setSelectedItem(sanPham.getDanhMuc());
        txtHang.setText(sanPham.getThuongHieu());
        txtModel.setText(sanPham.getModel());
        txtGia.setText(String.format(java.util.Locale.ROOT, "%.2f", sanPham.getGiaBan()));
        txtBaoHanh.setText(String.valueOf(sanPham.getBaoHanh()));
        cbNhom.setSelectedItem(sanPham.getNhomSanPham());
        cbTrangThai.setSelectedIndex(sanPham.isKinhDoanh() ? 0 : 1);
        txtMoTa.setText(sanPham.getMoTa());
        duongDanAnh = sanPham.getHinhAnh();
        hienAnh();
    }

    private void chonAnh() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh JPG, PNG", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try {
            String name = file.getName().toLowerCase(java.util.Locale.ROOT);
            if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))
                    || file.length() > 5 * 1024 * 1024 || ImageIO.read(file) == null) {
                throw new IllegalArgumentException("Chọn ảnh JPG/PNG đọc được, dung lượng tối đa 5 MB.");
            }
            duongDanAnh = file.getAbsolutePath();
            hienAnh();
        } catch (Exception ex) { UIHelper.loi(this, ex); }
    }

    private void hienAnh() {
        if (duongDanAnh.isEmpty() || !new File(duongDanAnh).isFile()) {
            lblAnh.setIcon(null);
            lblAnh.setText(duongDanAnh.isEmpty() ? "Chưa có ảnh" : "Không tìm thấy ảnh");
        } else {
            lblAnh.setIcon(new ImageIcon(new ImageIcon(duongDanAnh).getImage().getScaledInstance(140, 90, Image.SCALE_SMOOTH)));
            lblAnh.setText("");
        }
    }

    private void luu() {
        try {
            int baoHanh;
            try { baoHanh = Integer.parseInt(txtBaoHanh.getText().trim()); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Bảo hành phải là số tháng nguyên không âm."); }
            SanPham sp = new SanPham(txtSKU.getText(), txtMaVach.getText(), txtTen.getText(), (DanhMuc) cbDanhMuc.getSelectedItem(),
                txtHang.getText(), txtModel.getText(), UIHelper.soTien(txtGia.getText(), "Giá bán"), baoHanh,
                (String) cbNhom.getSelectedItem(), txtMoTa.getText(), duongDanAnh, cbTrangThai.getSelectedIndex() == 0);
            if (sanPham == null) dao.them(sp); else dao.sua(sp);
            dispose();
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }
}
