//package ui;
//
//import entity.HoaDon;
//import service.ExportService;
//import java.awt.*;
//import java.math.BigDecimal;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//import javax.swing.*;
//
//public class PNL_ChiTietHoaDon extends JPanel {
//    private final JTextArea detail = new JTextArea();
//    private HoaDon invoice;
//
//    public PNL_ChiTietHoaDon() { this(() -> {}); }
//    public PNL_ChiTietHoaDon(Runnable back) {
//        setLayout(new BorderLayout(0, 12)); setBackground(BusinessUI.BG);
//        setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
//        JLabel title = new JLabel("CHI TIẾT HÓA ĐƠN"); title.setFont(new Font("Segoe UI", Font.BOLD, 18)); title.setForeground(BusinessUI.BLUE);
//        add(title, BorderLayout.NORTH);
//        detail.setEditable(false); detail.setFont(new Font("Segoe UI", Font.PLAIN, 14)); detail.setLineWrap(true); detail.setWrapStyleWord(true); detail.setMargin(new Insets(16,16,16,16));
//        detail.setText("Chọn hóa đơn từ danh sách để xem chi tiết."); add(new JScrollPane(detail));
//        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        buttons.add(BusinessUI.button("Quay lại danh sách", back)); buttons.add(BusinessUI.button("Xuất PDF", this::exportPdf)); buttons.add(BusinessUI.button("In hóa đơn", this::printInvoice)); add(buttons, BorderLayout.SOUTH);
//    }
//    public void setHoaDon(HoaDon invoice) { this.invoice=invoice; detail.setText(String.join("\n", receipt(invoice))); detail.setCaretPosition(0); }
//    public static List<String> receipt(HoaDon h) {
//        List<String> lines=new ArrayList<>(List.of("ĐIỆN MÁY GIA THÀNH","HÓA ĐƠN BÁN HÀNG",h.ma(),"Ngày lập: "+h.ngay().format(BusinessUI.TIME),
//            "Nhân viên: "+h.nhanVien()+" | "+h.ca(),"Trạng thái: "+h.trangThai(),"Khách hàng: "+h.khachHang(),"SĐT: "+h.sdt()+" | Hạng: "+h.hang(),""));
//        for(HoaDon.Dong d:h.chiTiet()) { lines.add(d.sku()+" - "+d.ten()); lines.add("  "+d.soLuong()+" x "+BusinessUI.money(d.donGia())+" | Giảm: "+BusinessUI.money(d.giamGia())+" | Còn: "+BusinessUI.money(d.thanhTien())); }
//        lines.addAll(List.of("","Tạm tính: "+BusinessUI.money(h.tienHang()),"Tổng giảm giá: "+BusinessUI.money(h.giamGia()),"TỔNG THANH TOÁN: "+BusinessUI.money(h.tongTien()),
//            "Phương thức: "+h.phuongThuc(),"Khách thanh toán / trả trước: "+BusinessUI.money(h.khachDua()),
//            "Tiền thừa: "+BusinessUI.money(h.phuongThuc().equals("Tiền mặt")?h.khachDua().subtract(h.tongTien()):BigDecimal.ZERO),
//            "Chương trình: "+h.khuyenMai(),"Giao hàng / lắp đặt: "+(h.giaoHang().isBlank()?"Không":h.giaoHang())));
//        if(h.phuongThuc().equals("Trả góp")) lines.add("Đối tác: "+h.doiTac()+" | Kỳ hạn: "+h.kyHan()+" tháng | Dư nợ gốc: "+BusinessUI.money(h.tongTien().subtract(h.khachDua())));
//        lines.add("Cảm ơn quý khách!"); return lines;
//    }
//    private void exportPdf() {
//        if(invoice==null)return; Path path=BusinessUI.choose(this,invoice.ma(),"pdf"); if(path==null)return;
//        try { ExportService.pdf(path,receipt(invoice)); JOptionPane.showMessageDialog(this,"Đã xuất: "+path); } catch(Exception e){BusinessUI.error(this,e);}
//    }
//    private void printInvoice() {
//        if(invoice==null)return;
//        try { detail.print(new java.text.MessageFormat("Gia Thành"),new java.text.MessageFormat("Trang {0}")); } catch(Exception e){BusinessUI.error(this,e);}
//    }
//}
