import connect.ConnectDB;
import dao.SqlHoaDonRepository;
import entity.HoaDon;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Chỉ chạy tường minh khi SQL Server sẵn sàng. Toàn bộ fixture được rollback, không commit dữ liệu test. */
public class SqlIntegrationTest {
    static int checks;
    static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    static void execute(Connection c,String sql,Object... values)throws SQLException{
        try(PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);p.executeUpdate();}
    }
    static BigDecimal number(Connection c,String sql,String id)throws SQLException{
        try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,id);try(ResultSet r=p.executeQuery()){r.next();return r.getBigDecimal(1);}}
    }
    public static void main(String[] args)throws Exception {
        String suffix=UUID.randomUUID().toString().replace("-","").substring(0,8);
        String employee="N"+suffix,shift="C"+suffix,category="D"+suffix,product="P"+suffix,customer="K"+suffix;
        String phone="0"+java.util.concurrent.ThreadLocalRandom.current().nextInt(100000000,1000000000);
        try(Connection real=ConnectDB.openConnection()) {
            real.setAutoCommit(false);
            try {
                LocalDateTime now;try(Statement p=real.createStatement();ResultSet r=p.executeQuery("SELECT GETDATE()")){r.next();now=r.getTimestamp(1).toLocalDateTime();}
                execute(real,"INSERT INTO dbo.DanhMuc(maDanhMuc,tenDanhMuc) VALUES (?,?)",category,"Danh mục kiểm thử");
                execute(real,"INSERT INTO dbo.SanPham(maSKU,maVach,tenSP,donGia,slTon,maDanhMuc,thuongHieu) VALUES (?,?,?,?,?,?,?)",product,"V"+suffix,"Sản phẩm kiểm thử",100000,10,category,"Test");
                execute(real,"INSERT INTO dbo.NhanVien(maNV,hoTen,chucVu) VALUES (?,?,?)",employee,"NV kiểm thử","Bán hàng");
                execute(real,"INSERT INTO dbo.CaLamViec(maCa,thoiGianMoCa,trangThai) VALUES (?,?,?)",shift,Timestamp.valueOf(now),"Đang mở");
                execute(real,"INSERT INTO dbo.LichLamViec(maLich,ngayLamViec,maNV,maCa) VALUES (?,CAST(GETDATE() AS DATE),?,?)","L"+suffix,employee,shift);
                execute(real,"INSERT INTO dbo.KhachHang(maKH,tenKH,sdt,hangTV) VALUES (?,?,?,?)",customer,"Khách kiểm thử",phone,"Member");
                // Các lệnh commit/close của repository được giữ trong transaction bên ngoài để test không để lại dữ liệu.
                Connection borrowed=(Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,params)->{
                    if(method.getName().equals("commit")||method.getName().equals("close"))return null;
                    try{return method.invoke(real,params);}catch(InvocationTargetException e){throw e.getCause();}
                });
                SqlHoaDonRepository repo=new SqlHoaDonRepository(()->borrowed);
                var lines=List.of(new HoaDon.Dong(product,"Sản phẩm kiểm thử","Danh mục kiểm thử",2,new BigDecimal("100000"),new BigDecimal("10000")));
                HoaDon draft=new HoaDon("",now,employee,shift,"Khách kiểm thử",phone,"Member","Tiền mặt",HoaDon.DA_TT,new BigDecimal("5000"),new BigDecimal("200000"),"Giao nhà","","",0,lines);
                HoaDon saved=repo.save(draft);
                check(saved.ma().matches("HD[0-9]{10}"),"Mã hóa đơn SQL");
                check(number(real,"SELECT slTon FROM dbo.SanPham WHERE maSKU=?",product).intValueExact()==8,"Trừ tồn kho");
                check(number(real,"SELECT tongChiTieuTichLuy FROM dbo.KhachHang WHERE maKH=?",customer).compareTo(new BigDecimal("185000"))==0,"Cộng chi tiêu");
                check(number(real,"SELECT tongDoanhThu FROM dbo.CaLamViec WHERE maCa=?",shift).compareTo(new BigDecimal("185000"))==0,"Cộng doanh thu ca");
                execute(real,"UPDATE dbo.SanPham SET tenSP=N'Tên sau khi bán' WHERE maSKU=?",product);
                execute(real,"UPDATE dbo.KhachHang SET tenKH=N'Tên KH mới',hangTV=N'Gold' WHERE maKH=?",customer);
                HoaDon loaded=repo.findAll().stream().filter(h->h.ma().equals(saved.ma())).findFirst().orElseThrow();
                check(loaded.chiTiet().get(0).ten().equals("Sản phẩm kiểm thử")&&loaded.khachHang().equals("Khách kiểm thử")&&loaded.hang().equals("Member"),"Bản chụp không đổi theo danh mục");
                check(loaded.giamHoaDon().compareTo(new BigDecimal("5000"))==0&&loaded.khachDua().compareTo(new BigDecimal("200000"))==0,"Đọc đúng giảm giá, tiền khách đưa");
                check(loaded.tongTien().compareTo(saved.tongTien())==0,"Tổng tiền đọc và ghi khớp");
                // Đổi lại dữ liệu danh mục để lập thêm trả góp.
                execute(real,"UPDATE dbo.SanPham SET tenSP=N'Sản phẩm kiểm thử' WHERE maSKU=?",product);
                execute(real,"UPDATE dbo.KhachHang SET tenKH=N'Khách kiểm thử',hangTV=N'Member' WHERE maKH=?",customer);
                HoaDon installment=new HoaDon("",now,employee,shift,"Khách kiểm thử",phone,"Member","Trả góp",HoaDon.TRA_GOP,new BigDecimal("5000"),new BigDecimal("35000"),"","","Đối tác kiểm thử",6,lines);
                HoaDon second=repo.save(installment);
                check(!second.ma().equals(saved.ma()),"Cấp mã liên tiếp trong DB");
                check(number(real,"SELECT soTienConLai FROM dbo.GiaoDichTraGop WHERE maHD=?",second.ma()).compareTo(new BigDecimal("150000"))==0,"Lưu trả góp");
                // Đối tác vượt độ dài cột: lỗi sau INSERT hóa đơn và trừ kho phải rollback tất cả.
                HoaDon failing=new HoaDon("",now,employee,shift,"Khách kiểm thử",phone,"Member","Trả góp",HoaDon.TRA_GOP,new BigDecimal("5000"),new BigDecimal("35000"),"","","X".repeat(101),6,lines);
                try{repo.save(failing);throw new AssertionError("Phải phát hiện lỗi ghi trả góp");}catch(SQLException expected){checks++;}
            }finally{real.rollback();}
        }
        try(Connection verify=ConnectDB.openConnection()) {
            check(number(verify,"SELECT COUNT(*) FROM dbo.NhanVien WHERE maNV=?",employee).intValueExact()==0,"Không để lại dữ liệu test");
        }
        System.out.println("PASS "+checks+" SQL integration checks (all test data rolled back).");
    }
}
