import dao.HoaDonRepository;
import entity.HoaDon;
import service.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Chạy bằng java, không cần JUnit hoặc tải dependency. */
public class BusinessFeatureTest {
    static int checks;
    static BigDecimal n(String s){return new BigDecimal(s);}
    static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
    static void rejects(Runnable action,String message){try{action.run();throw new AssertionError(message);}catch(IllegalArgumentException expected){checks++;}}
    static HoaDon invoice(String nv,String method,List<HoaDon.Dong> lines,BigDecimal discount,BigDecimal paid){
        return new HoaDon("",LocalDateTime.of(2026,11,2,10,30),nv,"Ca sáng","Nguyễn Văn A","0901234567","Gold",method,
            method.equals("Trả góp")?HoaDon.TRA_GOP:HoaDon.DA_TT,discount,paid,"Giao hàng tại nhà","KM tháng 11",method.equals("Trả góp")?"Đối tác A":"",method.equals("Trả góp")?12:0,lines);
    }
    public static void main(String[] args)throws Exception{
        Path dir=Path.of("build/feature-tests");Files.createDirectories(dir);Path db=dir.resolve("invoices-"+System.nanoTime()+".xml");
        var lines=List.of(new HoaDon.Dong("SP01","Tủ lạnh Samsung","Tủ lạnh",2,n("10000000"),n("1000000")),new HoaDon.Dong("SP02","Máy giặt LG","Máy giặt",1,n("5000000"),n("0")));
        HoaDon h=invoice("NV01","Tiền mặt",lines,n("1250000"),n("23000000"));HoaDonService.validate(h);
        check(h.tongTien().compareTo(n("22750000"))==0,"Tính tổng tiền");
        check(HoaDonService.uuDai("Diamond",n("1000000")).compareTo(n("80000"))==0,"Ưu đãi Diamond");
        rejects(()->HoaDonService.validate(invoice("NV01","Tiền mặt",lines,n("20000000"),n("30000000"))),"Giới hạn 70%");
        rejects(()->HoaDonService.validate(invoice("NV01","Tiền mặt",lines,n("0"),n("100"))),"Tiền mặt thiếu");
        rejects(()->HoaDonService.validate(invoice("NV01","Chuyển khoản",lines,n("0"),n("30000000"))),"Chuyển khoản dư");
        rejects(()->HoaDonService.validate(invoice("NV01","Tiền mặt",List.of(lines.get(0),lines.get(0)),n("0"),n("50000000"))),"SKU trùng");
        rejects(()->HoaDonService.validate(invoice("NV01","Tiền mặt",List.of(new HoaDon.Dong("X","X","X",0,n("100"),n("0"))),n("0"),n("0"))),"Số lượng 0");
        rejects(()->HoaDonService.validate(invoice("NV01","Tiền mặt",List.of(new HoaDon.Dong("X","X","X",1,n("100"),n("51"))),n("0"),n("100"))),"Giảm dòng >50%");
        rejects(()->HoaDonService.validate(invoice("","Tiền mặt",lines,n("0"),n("50000000"))),"Thiếu nhân viên");
        System.setProperty("giathanh.storage","xml");System.setProperty("giathanh.data",db.toString());
        HoaDonRepository repo=new HoaDonRepository(db);check(repo.findAll().isEmpty(),"Kho mới trống");
        HoaDon first=repo.save(h),second=repo.save(invoice("NV02","Trả góp",lines,n("1000000"),n("2000000")));
        check(first.ma().equals("HD2611020001")&&second.ma().equals("HD2611020002"),"Mã 12 ký tự tăng dần");
        check(new HoaDonRepository(db).findAll().size()==2,"Đọc lại sau khởi động");
        var filter=new BaoCaoService.Filter(LocalDate.of(2026,11,2),LocalDate.of(2026,11,2),"","","","");
        List<HoaDon> all=repo.findAll();var daily=BaoCaoService.summarize(all,filter,0);var products=BaoCaoService.summarize(all,filter,7);
        check(daily.size()==1&&daily.get(0).invoices()==2&&daily.get(0).quantity()==6,"Biên ngày và số lượng");
        BigDecimal sum=products.stream().map(BaoCaoService.Row::revenue).reduce(BigDecimal.ZERO,BigDecimal::add);
        check(sum.compareTo(daily.get(0).revenue())==0,"Tổng doanh thu theo sản phẩm khớp theo ngày");
        check(BaoCaoService.summarize(all,filter,10).get(0).invoices()==1,"Báo cáo trả góp");
        for(int i=0;i<10;i++)check(BaoCaoService.summarize(all,filter,i).stream().map(BaoCaoService.Row::revenue).reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(sum)==0,"Tổng khớp cho báo cáo "+i);
        check(BaoCaoService.summarize(all,new BaoCaoService.Filter(LocalDate.of(2026,12,1),LocalDate.of(2026,12,31),"","","",""),0).isEmpty(),"Kỳ không có dữ liệu");
        var sales=new BaoCaoService.Access("NV01",false);
        check(BaoCaoService.summarize(all,filter,4,sales).size()==1&&BaoCaoService.summarize(all,filter,4,sales).get(0).label().equals("NV01"),"Doanh số cá nhân");
        rejects(()->BaoCaoService.summarize(all,filter,0,sales),"Chặn báo cáo tổng của NV bán hàng");
        check(BaoCaoService.summarize(all,new BaoCaoService.Filter(filter.from(),filter.to(),"","Máy giặt","",""),0).get(0).quantity()==2,"Lọc danh mục theo dòng");
        rejects(()->new BaoCaoService.Filter(LocalDate.of(2026,11,3),filter.to(),"","","",""),"Khoảng ngày ngược");
        // Rounding phải không làm lệch tổng tiền khi chia ưu đãi cho nhiều sản phẩm.
        var odd=invoice("NV01","Tiền mặt",List.of(new HoaDon.Dong("A","A","A",1,n("101"),n("50")),new HoaDon.Dong("B","B","B",1,n("102"),n("0"))),n("92"),n("100"));
        var allocated=BaoCaoService.summarize(List.of(odd),filter,7);check(allocated.stream().allMatch(r->r.revenue().signum()>=0)&&allocated.stream().map(BaoCaoService.Row::revenue).reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(odd.tongTien())==0,"Phân bổ lẻ chính xác và không âm");
        Path broken=dir.resolve("broken.xml");Files.writeString(broken,"broken");try{new HoaDonRepository(broken).findAll();throw new AssertionError("Không báo lỗi dữ liệu hỏng");}catch(java.io.IOException expected){checks++;}
        ExportService.excel(dir.resolve("report.xlsx"),List.of(List.of("Tên","Doanh thu"),List.of("=SUM(A1)",n("22750000"))));
        try(ZipFile zip=new ZipFile(dir.resolve("report.xlsx").toFile())){
            var factory=DocumentBuilderFactory.newInstance();factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
            var entries=zip.entries();while(entries.hasMoreElements()){var entry=entries.nextElement();try(var in=zip.getInputStream(entry)){factory.newDocumentBuilder().parse(in);checks++;}}
            String sheet=new String(zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            check(sheet.contains("inlineStr")&&!sheet.contains("<f>"),"Chuỗi không thực thi công thức Excel");
        }
        List<String> receipt=new ArrayList<>(ui.PNL_ChiTietHoaDon.receipt(first));for(int i=0;i<100;i++)receipt.add("Sản phẩm điện máy số "+i+" — Tiếng Việt");ExportService.pdf(dir.resolve("invoice.pdf"),receipt);
        String pdf=Files.readString(dir.resolve("invoice.pdf"),java.nio.charset.StandardCharsets.ISO_8859_1);check(pdf.startsWith("%PDF-1.4")&&pdf.contains("/Count 3"),"PDF phân trang");
        System.setProperty("giathanh.data",db.toString());
        SwingUtilities.invokeAndWait(()->{
            try {
                var createRequested=new java.util.concurrent.atomic.AtomicBoolean();
                var selectedInvoice=new java.util.concurrent.atomic.AtomicReference<HoaDon>();
                ui.PNL_HoaDon invoices=new ui.PNL_HoaDon(()->createRequested.set(true),selectedInvoice::set);capture(invoices,dir.resolve("invoices.png"));
                var tables=new ArrayList<JTable>();findTables(invoices,tables);JTable t=tables.get(0);
                t.getRowSorter().toggleSortOrder(0);t.setRowSelectionInterval(1,1);
                check(t.getValueAt(1,0).toString().equals("HD2611020002"),"Chọn hóa đơn sau khi sắp xếp");
                findButton(invoices,"Lập hóa đơn").doClick();check(createRequested.get(),"Điều hướng sang panel lập hóa đơn");
                findButton(invoices,"Xem chi tiết").doClick();check(selectedInvoice.get().ma().equals("HD2611020002"),"Truyền đúng hóa đơn sang panel chi tiết");
                capture(new ui.PNL_BaoCao(),dir.resolve("reports.png"));
                capture(new ui.PNL_BaoCao(sales),dir.resolve("sales-report.png"));
                capture(new ui.PNL_LapHoaDon(),dir.resolve("create-invoice.png"));
                ui.PNL_ChiTietHoaDon details=new ui.PNL_ChiTietHoaDon();details.setHoaDon(first);capture(details,dir.resolve("invoice-details.png"));
            }
            catch(Exception e){throw new RuntimeException(e);}
        });
        System.out.println("PASS "+checks+" checks. Artifacts: "+dir.toAbsolutePath());
    }
    static void capture(JPanel panel,Path path)throws Exception{
        panel.setSize(1080,550);panel.addNotify();layout(panel);layout(panel);BufferedImage image=new BufferedImage(1080,550,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();panel.printAll(g);g.dispose();ImageIO.write(image,"png",path.toFile());panel.removeNotify();
    }
    static void layout(java.awt.Container c){c.doLayout();for(var child:c.getComponents())if(child instanceof java.awt.Container sub)layout(sub);}
    static void findTables(java.awt.Container c,List<JTable> found){for(var child:c.getComponents()){if(child instanceof JTable t)found.add(t);if(child instanceof java.awt.Container sub)findTables(sub,found);}}
    static JButton findButton(java.awt.Container c,String text){for(var child:c.getComponents()){if(child instanceof JButton b&&text.equals(b.getText()))return b;if(child instanceof java.awt.Container sub){JButton found=findButton(sub,text);if(found!=null)return found;}}return null;}
}
