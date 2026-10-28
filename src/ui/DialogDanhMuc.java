package ui;

import dao.DanhMucDAO;
import entity.DanhMuc;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DialogDanhMuc extends JDialog {
    private final DanhMucDAO dao = new DanhMucDAO();
    private final DefaultTableModel model = UIHelper.model("Mã danh mục", "Tên danh mục");
    private final JTable table = new JTable(model);
    private final JTextField txtTen = new JTextField(22);
    private ArrayList<DanhMuc> danhSach;

    public DialogDanhMuc(Window owner) {
        super(owner, "Quản lý danh mục", ModalityType.APPLICATION_MODAL);
        JPanel panel = UIHelper.theTrang();
        setContentPane(panel);
        UIHelper.dinhDangBang(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);
        JPanel form = new JPanel(new BorderLayout(10, 0));
        form.setOpaque(false);
        form.add(new JLabel("Tên danh mục:"), BorderLayout.WEST);
        form.add(txtTen, BorderLayout.CENTER);
        bottom.add(form, BorderLayout.NORTH);
        JButton them = UIHelper.nutChinh("Thêm");
        JButton sua = UIHelper.nut("Sửa");
        JButton xoa = UIHelper.nut("Xóa");
        JButton moi = UIHelper.nut("Làm mới");
        JButton dong = UIHelper.nut("Đóng");
        bottom.add(UIHelper.hangNut(them, sua, xoa, moi, dong), BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (table.getSelectedRow() >= 0) txtTen.setText(danhSach.get(table.getSelectedRow()).getTenDanhMuc());
        });
        them.addActionListener(e -> luu(false));
        sua.addActionListener(e -> luu(true));
        xoa.addActionListener(e -> xoa());
        moi.addActionListener(e -> { table.clearSelection(); txtTen.setText(""); });
        dong.addActionListener(e -> dispose());
        taiDanhSach();
        setSize(620, 460);
        setLocationRelativeTo(owner);
    }

    private void taiDanhSach() {
        model.setRowCount(0);
        danhSach = dao.layTatCa();
        for (DanhMuc dm : danhSach) model.addRow(new Object[] {dm.getMaDanhMuc(), dm.getTenDanhMuc()});
        txtTen.setText("");
    }

    private void luu(boolean sua) {
        try {
            if (sua) {
                if (table.getSelectedRow() < 0) throw new IllegalArgumentException("Vui lòng chọn danh mục cần sửa.");
                dao.sua(danhSach.get(table.getSelectedRow()).getMaDanhMuc(), txtTen.getText());
            } else dao.them(txtTen.getText());
            taiDanhSach();
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }

    private void xoa() {
        try {
            if (table.getSelectedRow() < 0) throw new IllegalArgumentException("Vui lòng chọn danh mục cần xóa.");
            DanhMuc dm = danhSach.get(table.getSelectedRow());
            if (UIHelper.xacNhan(this, "Xóa danh mục " + dm.getTenDanhMuc() + "?")) {
                dao.xoa(dm.getMaDanhMuc());
                taiDanhSach();
            }
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }
}
