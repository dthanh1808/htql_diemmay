package ui;

import dao.HoaDonRepository;
import entity.HoaDon;
import service.BaoCaoService;
import service.BaoCaoService.Row;
import service.ExportService;
import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PNL_BaoCao extends JPanel {
    private final JComboBox<String> type=new JComboBox<>(BaoCaoService.TYPES);
    private final JTextField from=new JTextField(9),to=new JTextField(9),employee=new JTextField(12),category=new JTextField(12);
    private final JComboBox<String> tier=new JComboBox<>(new String[]{"Tất cả hạng","Khách lẻ","Member","Silver","Gold","Diamond"});
    private final JComboBox<String> method=new JComboBox<>(new String[]{"Tất cả phương thức","Tiền mặt","Chuyển khoản","Trả góp"});
    private final DefaultTableModel model=BusinessUI.model("Nhóm thống kê","Số HĐ","Số SP","Tiền hàng (đ)","Giảm giá (đ)","Doanh thu (đ)");
    private final JTable table=BusinessUI.table(model);
    private final JLabel summary=new JLabel(" ");
    private final Chart chart=new Chart();
    private List<Row> rows=List.of();
    private String snapshotTitle="",snapshotFilters="";
    private final BaoCaoService.Access access;
    public PNL_BaoCao() { this(new BaoCaoService.Access("", true)); }
    public PNL_BaoCao(BaoCaoService.Access access) {
        this.access=access;
        if(!access.manager()) { type.removeAllItems(); type.addItem(BaoCaoService.TYPES[4]); type.addItem(BaoCaoService.TYPES[7]); employee.setEnabled(false); }
        setLayout(new BorderLayout(10,12)); setBackground(BusinessUI.BG); setBorder(BorderFactory.createEmptyBorder(15,18,15,18));
        LocalDate today=BusinessUI.now().toLocalDate(); from.setText(today.withDayOfMonth(1).format(BusinessUI.DATE)); to.setText(today.format(BusinessUI.DATE));
        JPanel top=new JPanel(new GridLayout(0,1,0,6));
        JPanel title=new JPanel(new FlowLayout(FlowLayout.LEFT)); JLabel heading=new JLabel("THỐNG KÊ & BÁO CÁO"); heading.setFont(new Font("Segoe UI",Font.BOLD,18)); heading.setForeground(BusinessUI.BLUE); title.add(heading); title.add(type); top.add(title);
        JPanel dates=new JPanel(new FlowLayout(FlowLayout.LEFT)); dates.add(new JLabel("Từ ngày (dd/MM/yyyy)")); dates.add(from); dates.add(new JLabel("Đến ngày")); dates.add(to);
        dates.add(BusinessUI.button("Hôm nay",()-> { LocalDate d=BusinessUI.now().toLocalDate(); setDates(d,d); })); dates.add(BusinessUI.button("Tháng này",()-> { LocalDate d=BusinessUI.now().toLocalDate(); setDates(d.withDayOfMonth(1),d); }));
        dates.add(BusinessUI.button("Năm nay",()-> { LocalDate d=BusinessUI.now().toLocalDate(); setDates(d.withDayOfYear(1),d); })); top.add(dates);
        JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT)); filters.add(new JLabel("Nhân viên")); filters.add(employee); filters.add(new JLabel("Danh mục")); filters.add(category); filters.add(tier); filters.add(method); filters.add(BusinessUI.button("Xem báo cáo",this::refresh)); top.add(filters);
        top.add(summary); add(top,BorderLayout.NORTH);
        JPanel content=new JPanel(new GridLayout(2,1,0,10));content.setOpaque(false);content.add(new JScrollPane(table));content.add(chart);add(content);
        JPanel bottom=new JPanel(new BorderLayout());
        bottom.add(new JLabel("Doanh thu = tiền hàng − giảm giá; trả góp ghi nhận toàn bộ giá trị bán. Biểu đồ: 10 nhóm đầu (VNĐ)."));
        JPanel exports=new JPanel(); exports.add(BusinessUI.button("Xuất Excel",()->export(false))); exports.add(BusinessUI.button("Xuất PDF",()->export(true))); bottom.add(exports,BorderLayout.EAST); add(bottom,BorderLayout.SOUTH);
        type.addActionListener(e->refresh()); addHierarchyListener(e->{if((e.getChangeFlags()&java.awt.event.HierarchyEvent.SHOWING_CHANGED)!=0&&isShowing())refresh();});
        if(!HoaDonRepository.getInstance().isSql())refresh();else summary.setText("Nguồn dữ liệu: SQL Server • Chọn Xem báo cáo để tải.");
    }
    private void setDates(LocalDate a,LocalDate b){from.setText(a.format(BusinessUI.DATE));to.setText(b.format(BusinessUI.DATE));refresh();}
    private void refresh() {
        var repository=HoaDonRepository.getInstance();
        if(repository.isSql())BusinessUI.background(this,repository::findAll,this::applyReport);
        else try{applyReport(repository.findAll());}catch(Exception e){BusinessUI.error(this,e);}
    }
    private void applyReport(List<HoaDon> all) {
        try {
            var filter=new BaoCaoService.Filter(LocalDate.parse(from.getText().trim(),BusinessUI.DATE),LocalDate.parse(to.getText().trim(),BusinessUI.DATE),employee.getText(),category.getText(),tier.getSelectedIndex()==0?"":tier.getSelectedItem().toString(),method.getSelectedIndex()==0?"":method.getSelectedItem().toString());
            int reportType=java.util.Arrays.asList(BaoCaoService.TYPES).indexOf(type.getSelectedItem());
            List<Row> computed=BaoCaoService.summarize(all,filter,reportType,access);
            rows=computed; snapshotTitle=type.getSelectedItem().toString();
            snapshotFilters=from.getText()+" - "+to.getText()+" | NV: "+(access.manager()?employee.getText():access.employee())+" | Danh mục: "+category.getText()+" | "+tier.getSelectedItem()+" | "+method.getSelectedItem();
            var sortKeys=table.getRowSorter().getSortKeys();table.setRowSorter(null);model.setRowCount(0); BigDecimal revenue=BigDecimal.ZERO,discount=BigDecimal.ZERO; long quantity=0;
            for(Row r:rows){model.addRow(new Object[]{r.label(),r.invoices(),r.quantity(),r.gross(),r.discount(),r.revenue()});revenue=revenue.add(r.revenue());discount=discount.add(r.discount());quantity+=r.quantity();}
            table.setRowSorter(new javax.swing.table.TableRowSorter<>(model));table.getRowSorter().setSortKeys(sortKeys);
            summary.setText(rows.size()+" nhóm • Sản phẩm: "+quantity+" • Giảm giá: "+BusinessUI.money(discount)+" • Doanh thu: "+BusinessUI.money(revenue)+(rows.isEmpty()?" • Không có dữ liệu trong kỳ":""));
            chart.repaint();
        }catch(Exception e){BusinessUI.error(this,e);}
    }
    private void export(boolean pdf) {
        if(snapshotTitle.isBlank()){JOptionPane.showMessageDialog(this,"Chọn Xem báo cáo và tải dữ liệu thành công trước khi xuất.");return;}
        // Xuất đúng snapshot đang hiển thị; không âm thầm thay đổi dữ liệu theo ô lọc chưa áp dụng.
        Path p=BusinessUI.choose(this,"Bao-cao-Gia-Thanh",pdf?"pdf":"xlsx"); if(p==null)return;
        try {
            if(pdf){List<String> lines=new ArrayList<>(List.of("ĐIỆN MÁY GIA THÀNH",snapshotTitle,snapshotFilters,summary.getText(),""));
                for(Row r:rows){lines.add(r.label()+" | "+r.invoices()+" HĐ | "+r.quantity()+" SP");lines.add("Tiền hàng: "+BusinessUI.money(r.gross())+" | Giảm: "+BusinessUI.money(r.discount()));lines.add("Doanh thu: "+BusinessUI.money(r.revenue()));lines.add("");} ExportService.pdf(p,lines);
            }else{List<List<Object>> cells=new ArrayList<>();cells.add(List.of(snapshotTitle));cells.add(List.of(snapshotFilters));cells.add(List.of(summary.getText()));cells.add(List.of("Nhóm thống kê","Số hóa đơn","Số sản phẩm","Tiền hàng (VNĐ)","Giảm giá (VNĐ)","Doanh thu (VNĐ)"));
                for(Row r:rows)cells.add(List.of(r.label(),r.invoices(),r.quantity(),r.gross(),r.discount(),r.revenue()));ExportService.excel(p,cells);}
            JOptionPane.showMessageDialog(this,"Đã xuất: "+p);
        }catch(Exception e){BusinessUI.error(this,e);}
    }
    private final class Chart extends JPanel {
        Chart(){setPreferredSize(new Dimension(850,230));setBackground(Color.WHITE);}
        protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(rows.isEmpty()){g.setColor(Color.GRAY);g.drawString("Không có dữ liệu để vẽ biểu đồ.",30,50);g.dispose();return;}
            int count=Math.min(10,rows.size()), width=Math.max(1,(getWidth()-80)/count),height=Math.max(1,getHeight()-85);double max=rows.stream().limit(10).map(Row::revenue).mapToDouble(BigDecimal::doubleValue).max().orElse(1);if(max<=0)max=1;
            g.setFont(new Font("Segoe UI",Font.PLAIN,10));
            for(int i=0;i<count;i++){Row r=rows.get(i);int bar=(int)(r.revenue().doubleValue()/max*height);int x=45+i*width,y=height+30-bar;g.setColor(BusinessUI.BLUE);g.fillRoundRect(x,y,Math.max(2,width-16),bar,5,5);g.setColor(Color.DARK_GRAY);g.drawString(BusinessUI.money(r.revenue()),x,Math.max(14,y-5));String label=r.label();g.drawString(label.length()>15?label.substring(0,14)+"…":label,x,height+50);}
            g.dispose();
        }
    }
}
