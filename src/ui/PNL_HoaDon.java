//package ui;
//
//import dao.HoaDonRepository;
//import entity.HoaDon;
//import java.util.function.Consumer;
//import java.awt.*;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.*;
//import java.util.List;
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import javax.swing.table.DefaultTableModel;
//
//public class PNL_HoaDon extends JPanel {
//    private final HoaDonRepository repository = HoaDonRepository.getInstance();
//    private final JTextField search = new JTextField(19), from = new JTextField(9), to = new JTextField(9);
//    private final JComboBox<String> status = new JComboBox<>(new String[]{"Tất cả trạng thái",HoaDon.DA_TT,HoaDon.TRA_GOP});
//    private final JComboBox<String> method = new JComboBox<>(new String[]{"Tất cả phương thức","Tiền mặt","Chuyển khoản","Trả góp"});
//    private final DefaultTableModel model = BusinessUI.model("Mã HĐ","Ngày lập","Khách hàng","SĐT","Nhân viên","Tổng tiền (đ)","Phương thức","Trạng thái");
//    private final JTable table = BusinessUI.table(model);
//    private final Consumer<HoaDon> onView;
//    private final JLabel summary = new JLabel(" ");
//    private List<HoaDon> visible = new ArrayList<>();
//
//    public PNL_HoaDon() { this(null, null); }
//    public PNL_HoaDon(Runnable onCreate, Consumer<HoaDon> onView) {
//        this.onView = onView;
//        setLayout(new BorderLayout(10,12)); setBackground(BusinessUI.BG); setBorder(new EmptyBorder(15,18,15,18));
//        JPanel north = new JPanel(new GridLayout(0,1,0,6)); north.setOpaque(false);
//        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JLabel title = new JLabel("QUẢN LÝ HÓA ĐƠN"); title.setFont(new Font("Segoe UI",Font.BOLD,18)); title.setForeground(BusinessUI.BLUE);
//        actions.add(title);
//        if (onCreate != null) actions.add(BusinessUI.button("Lập hóa đơn", onCreate)); actions.add(BusinessUI.button("Làm mới",this::reload)); north.add(actions);
//        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT)); search.setToolTipText("Mã hóa đơn, tên khách hàng, SĐT hoặc SKU");
//        filters.add(new JLabel("Tìm kiếm")); filters.add(search); filters.add(status); filters.add(method); north.add(filters);
//        JPanel dates = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        dates.add(new JLabel("Từ ngày (dd/MM/yyyy)")); dates.add(from); dates.add(new JLabel("Đến ngày")); dates.add(to);
//        dates.add(BusinessUI.button("Lọc",this::reload)); dates.add(BusinessUI.button("Xóa lọc",() -> {
//            search.setText(""); from.setText(""); to.setText(""); status.setSelectedIndex(0); method.setSelectedIndex(0); reload();
//        })); north.add(dates); north.add(summary); add(north,BorderLayout.NORTH);
//        add(new JScrollPane(table), BorderLayout.CENTER);
//        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        if (onView != null) bottom.add(BusinessUI.button("Xem chi tiết", this::showSelected)); add(bottom, BorderLayout.SOUTH);
//        table.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) showSelected(); } });
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        int[] widths={130,145,150,110,100,130,120,120};
//        for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
//        search.addActionListener(e -> reload()); status.addActionListener(e -> reload()); method.addActionListener(e -> reload());
//        addHierarchyListener(e -> { if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) reload(); });
//        if(!repository.isSql())reload();else summary.setText("Nguồn dữ liệu: SQL Server • Chọn Làm mới để tải.");
//    }
//    private void reload() {
//        if(repository.isSql())BusinessUI.background(this,repository::findAll,this::applyFilters);
//        else try{applyFilters(repository.findAll());}catch(Exception e){BusinessUI.error(this,e);}
//    }
//    private void applyFilters(List<HoaDon> invoices) {
//        try {
//            LocalDate a = from.getText().isBlank() ? LocalDate.MIN : LocalDate.parse(from.getText().trim(),BusinessUI.DATE);
//            LocalDate b = to.getText().isBlank() ? LocalDate.MAX : LocalDate.parse(to.getText().trim(),BusinessUI.DATE);
//            if (a.isAfter(b)) throw new IllegalArgumentException("Từ ngày phải trước hoặc bằng đến ngày.");
//            String q = search.getText().trim().toLowerCase(Locale.ROOT);
//            List<HoaDon> loaded = invoices.stream().filter(h -> !h.ngay().toLocalDate().isBefore(a) && !h.ngay().toLocalDate().isAfter(b))
//                .filter(h -> status.getSelectedIndex() == 0 || h.trangThai().equals(status.getSelectedItem()))
//                .filter(h -> method.getSelectedIndex() == 0 || h.phuongThuc().equals(method.getSelectedItem()))
//                .filter(h -> (h.ma()+" "+h.khachHang()+" "+h.sdt()+" "+h.chiTiet().stream().map(HoaDon.Dong::sku).reduce("",(x,y)->x+" "+y)).toLowerCase(Locale.ROOT).contains(q)).toList();
//            String selected = selected() == null ? "" : selected().ma(); table.clearSelection();
//            var sortKeys=table.getRowSorter().getSortKeys(); table.setRowSorter(null); visible = loaded; model.setRowCount(0);
//            BigDecimal total = BigDecimal.ZERO; int select = -1;
//            for (int i=0;i<visible.size();i++) {
//                HoaDon h=visible.get(i); if(h.ma().equals(selected)) select=i;
//                model.addRow(new Object[]{h.ma(),h.ngay(),h.khachHang(),h.sdt(),h.nhanVien(),h.tongTien(),h.phuongThuc(),h.trangThai()});
//                if(h.ghiNhanDoanhThu()) total=total.add(h.tongTien());
//            }
//            table.setRowSorter(new javax.swing.table.TableRowSorter<>(model));table.getRowSorter().setSortKeys(sortKeys);
//            summary.setText(visible.size()+" hóa đơn • Doanh thu theo bộ lọc: "+BusinessUI.money(total));
//            if(!visible.isEmpty()) { int row=table.convertRowIndexToView(select<0?0:select); table.setRowSelectionInterval(row,row); }
//            else summary.setText("Không có hóa đơn phù hợp với bộ lọc.");
//        } catch(Exception e) { BusinessUI.error(this,e); }
//    }
//    private HoaDon selected() { return table.getSelectedRow()<0?null:visible.get(table.convertRowIndexToModel(table.getSelectedRow())); }
//    private void showSelected() { HoaDon h=selected(); if(h!=null && onView!=null) onView.accept(h); }
//}
