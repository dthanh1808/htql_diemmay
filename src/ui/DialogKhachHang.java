package ui;

import dao.KhachHangDAO;
import entity.KhachHang;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

public class DialogKhachHang extends JDialog {
    private final KhachHang khachHang;
    private JTextField txtTen = new JTextField(), txtSDT = new JTextField(), txtEmail = new JTextField();
    private JTextField txtNgaySinh = new JTextField(), txtDiaChi = new JTextField();
    private JComboBox<String> cbGioiTinh = new JComboBox<>(new String[] {"Chưa cung cấp", "Nam", "Nữ", "Khác"});

    public DialogKhachHang(Window owner, KhachHang khachHang) {
        super(owner, khachHang == null ? "Đăng ký khách hàng mới" : "Chỉnh sửa khách hàng", ModalityType.APPLICATION_MODAL);
        this.khachHang = khachHang;
        JPanel panel = UIHelper.theTrang();
        setContentPane(panel);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UIHelper.themDong(form, "Họ tên *", txtTen);
        UIHelper.themDong(form, "Số điện thoại * (10 chữ số)", txtSDT);
        UIHelper.themDong(form, "Email", txtEmail);
        UIHelper.themDong(form, "Ngày sinh (dd/MM/yyyy)", txtNgaySinh);
        UIHelper.themDong(form, "Giới tính", cbGioiTinh);
        UIHelper.themDong(form, "Địa chỉ", txtDiaChi);
        panel.add(form, BorderLayout.CENTER);
        panel.add(new JLabel(khachHang == null ? "Mặc định: hạng Member, chi tiêu 0 đ." : "Mã khách hàng: " + khachHang.getMaKhachHang()), BorderLayout.NORTH);
        JButton luu = UIHelper.nutChinh("Lưu khách hàng"), huy = UIHelper.nut("Hủy");
        panel.add(UIHelper.hangNut(luu, huy), BorderLayout.SOUTH);
        if (khachHang != null) {
            txtTen.setText(khachHang.getHoTen()); txtSDT.setText(khachHang.getSoDienThoai());
            txtEmail.setText(khachHang.getEmail()); txtDiaChi.setText(khachHang.getDiaChi());
            cbGioiTinh.setSelectedItem(khachHang.getGioiTinh());
            txtNgaySinh.setText(khachHang.getNgaySinh() == null ? "" : UIHelper.NGAY.format(khachHang.getNgaySinh()));
        }
        luu.addActionListener(e -> luu());
        huy.addActionListener(e -> dispose());
        setSize(650, 400);
        setLocationRelativeTo(owner);
    }

    private void luu() {
        try {
            KhachHang kh = new KhachHang(khachHang == null ? "" : khachHang.getMaKhachHang(), txtTen.getText(), txtSDT.getText(),
                txtEmail.getText(), UIHelper.ngay(txtNgaySinh.getText(), "Ngày sinh"), (String) cbGioiTinh.getSelectedItem(),
                txtDiaChi.getText(), LocalDate.now(), khachHang == null || khachHang.isHoatDong());
            KhachHangDAO dao = new KhachHangDAO();
            if (khachHang == null) dao.them(kh); else dao.sua(kh);
            dispose();
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }
}
