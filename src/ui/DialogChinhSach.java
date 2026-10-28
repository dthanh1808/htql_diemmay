package ui;

import dao.ChinhSachThanhVienDAO;
import entity.ChinhSachThanhVien;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DialogChinhSach extends JDialog {
    private final ChinhSachThanhVienDAO dao = new ChinhSachThanhVienDAO();
    private final DefaultTableModel model = new DefaultTableModel(new String[] {"Hạng", "Chi tiêu tối thiểu (VNĐ)", "Ưu đãi (%)"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return row > 0 && col > 0; }
    };
    private final JTable table = new JTable(model);

    public DialogChinhSach(Window owner) {
        super(owner, "Cấu hình chính sách thành viên - Quản lý", ModalityType.APPLICATION_MODAL);
        JPanel panel = UIHelper.theTrang();
        setContentPane(panel);
        panel.add(new JLabel("Xét hạng theo tổng chi tiêu sau hoàn tiền. Không sử dụng điểm tích lũy."), BorderLayout.NORTH);
        UIHelper.dinhDangBang(table);
        for (ChinhSachThanhVien cs : dao.layTatCa()) model.addRow(new Object[] {cs.getTenHang(),
            String.format(java.util.Locale.ROOT, "%.2f", cs.getChiTieuToiThieu()), cs.getPhanTramGiam()});
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton luu = UIHelper.nutChinh("Lưu chính sách"), dong = UIHelper.nut("Hủy");
        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);
        bottom.add(new JLabel("Nhập số, không có dấu phân cách. Ngưỡng tăng dần; ưu đãi từ 0% đến 70%."), BorderLayout.NORTH);
        bottom.add(UIHelper.hangNut(luu, dong), BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        luu.addActionListener(e -> luu());
        dong.addActionListener(e -> dispose());
        setSize(740, 390);
        setLocationRelativeTo(owner);
    }

    private void luu() {
        if (table.isEditing() && !table.getCellEditor().stopCellEditing()) return;
        try {
            ArrayList<ChinhSachThanhVien> danhSach = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                danhSach.add(new ChinhSachThanhVien(String.valueOf(model.getValueAt(i, 0)),
                    UIHelper.soTien(String.valueOf(model.getValueAt(i, 1)), "Ngưỡng chi tiêu"),
                    UIHelper.soTien(String.valueOf(model.getValueAt(i, 2)), "Ưu đãi")));
            }
            if (!UIHelper.xacNhan(this, "Lưu chính sách và tính lại hạng cho tất cả khách hàng?")) return;
            dao.luu(danhSach);
            dispose();
        } catch (IllegalArgumentException ex) { UIHelper.loi(this, ex); }
    }
}
