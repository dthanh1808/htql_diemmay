package dao;

import connect.ConnectDB;
import entity.HoaDon;
import service.HoaDonService;
import java.math.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Ánh xạ đúng HoaDon, ChiTietHoaDon và GiaoDichTraGop trong script của project. */
public final class SqlHoaDonRepository {
    @FunctionalInterface public interface Connections { Connection open() throws SQLException; }
    private final Connections connections;
    public SqlHoaDonRepository() { this(ConnectDB::openConnection); }
    public SqlHoaDonRepository(Connections connections) { this.connections=connections; }

    public List<HoaDon> findAll() throws SQLException {
        try(Connection c=connections.open()) {
            boolean snapshots=hasSnapshots(c);
            String customer=snapshots?"COALESCE(h.tenKhachHangLucMua,k.tenKH,N'Khách lẻ')":"COALESCE(k.tenKH,N'Khách lẻ')";
            String phone=snapshots?"COALESCE(h.sdtKhachHangLucMua,k.sdt,'')":"COALESCE(k.sdt,'')";
            String tier=snapshots?"COALESCE(h.hangThanhVienLucMua,k.hangTV,N'Khách lẻ')":"COALESCE(k.hangTV,N'Khách lẻ')";
            String paid=snapshots?"h.tienKhachDua":"CAST(NULL AS DECIMAL(18,0))";
            String name=snapshots?"COALESCE(d.tenSPLucBan,s.tenSP)":"s.tenSP";
            String category=snapshots?"COALESCE(d.tenDanhMucLucBan,m.tenDanhMuc)":"m.tenDanhMuc";
            String promotion=snapshots?"COALESCE(h.tenKhuyenMaiLucMua,p.tenKM,N'')":"COALESCE(p.tenKM,N'')";
            String query="SELECT h.*, "+customer+" AS customerName,"+phone+" AS customerPhone,"+tier+" AS customerTier,"+paid+" AS tender,"+promotion+" AS promotionName,"
                +"g.doiTacTaiChinh,g.kyHan,g.tienTraTruoc,d.maSKU,d.soLuong,d.donGia,d.thanhTien,"+name+" AS productName,"+category+" AS categoryName "
                +"FROM dbo.HoaDon h LEFT JOIN dbo.KhachHang k ON k.maKH=h.maKH LEFT JOIN dbo.KhuyenMai p ON p.maKM=h.maKM "
                +"LEFT JOIN dbo.GiaoDichTraGop g ON g.maHD=h.maHD LEFT JOIN dbo.ChiTietHoaDon d ON d.maHD=h.maHD "
                +"LEFT JOIN dbo.SanPham s ON s.maSKU=d.maSKU LEFT JOIN dbo.DanhMuc m ON m.maDanhMuc=s.maDanhMuc ORDER BY h.ngayLap DESC,h.maHD DESC,d.maSKU";
            List<HoaDon> invoices=new ArrayList<>();
            try(PreparedStatement ps=c.prepareStatement(query);ResultSet rs=ps.executeQuery()) {
                Header header=null;List<HoaDon.Dong> lines=new ArrayList<>();
                while(rs.next()) {
                    if(header==null||!header.id.equals(rs.getString("maHD"))) {
                        if(header!=null)invoices.add(header.build(lines));
                        header=new Header(rs);lines=new ArrayList<>();
                    }
                    if(rs.getString("maSKU")!=null) {
                        BigDecimal price=rs.getBigDecimal("donGia");int qty=rs.getInt("soLuong");
                        BigDecimal discount=price.multiply(BigDecimal.valueOf(qty)).subtract(rs.getBigDecimal("thanhTien"));
                        if(discount.signum()<0)throw new SQLException("Chi tiết hóa đơn "+header.id+" có thành tiền lớn hơn tiền hàng.");
                        lines.add(new HoaDon.Dong(rs.getString("maSKU"),rs.getString("productName"),rs.getString("categoryName"),qty,price,discount));
                    }
                }
                if(header!=null)invoices.add(header.build(lines));
            }
            return invoices;
        }
    }
    private static final class Header {
        final String id,nv,ca,customer,phone,tier,method,status,delivery,promotion,partner;
        final LocalDateTime date;final BigDecimal discount,paid,total,gross;final int term;
        Header(ResultSet r)throws SQLException {
            id=r.getString("maHD");date=r.getTimestamp("ngayLap").toLocalDateTime();nv=r.getString("maNV");ca=empty(r.getString("maCa"));
            customer=r.getString("customerName");phone=r.getString("customerPhone");tier=r.getString("customerTier");method=empty(r.getString("phuongThucThanhToan"));status=empty(r.getString("trangThai"));
            delivery=empty(r.getString("ghiChuGiaoLapDat"));promotion=r.getString("promotionName");partner=empty(r.getString("doiTacTaiChinh"));term=r.getInt("kyHan");
            discount=r.getBigDecimal("tienGiamGia");total=r.getBigDecimal("tongThanhToan");gross=r.getBigDecimal("tongTienHang");
            BigDecimal tender=r.getBigDecimal("tender");
            // Dữ liệu cũ không có tiền khách đưa: dùng số tiền thanh toán tối thiểu, không suy đoán tiền thừa.
            paid=tender!=null?tender:method.equals("Trả góp")?Objects.requireNonNullElse(r.getBigDecimal("tienTraTruoc"),BigDecimal.ZERO):total;
        }
        HoaDon build(List<HoaDon.Dong> lines)throws SQLException {
            BigDecimal lineDiscount=lines.stream().map(HoaDon.Dong::giamGia).reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal lineGross=lines.stream().map(HoaDon.Dong::tienHang).reduce(BigDecimal.ZERO,BigDecimal::add);
            if(lineGross.compareTo(gross)!=0||discount.compareTo(lineDiscount)<0||gross.subtract(discount).compareTo(total)!=0)
                throw new SQLException("Tổng tiền và chi tiết hóa đơn "+id+" không khớp. Cần kiểm tra dữ liệu, không tự sửa số liệu báo cáo.");
            return new HoaDon(id,date,nv,ca,customer,phone,tier,method,status,discount.subtract(lineDiscount),paid,delivery,promotion,partner,term,lines);
        }
    }
    private static String empty(String s){return s==null?"":s;}
    private static boolean hasSnapshots(Connection c)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT CASE WHEN COL_LENGTH('dbo.HoaDon','tienKhachDua') IS NOT NULL AND COL_LENGTH('dbo.HoaDon','hangThanhVienLucMua') IS NOT NULL AND COL_LENGTH('dbo.HoaDon','tenKhachHangLucMua') IS NOT NULL AND COL_LENGTH('dbo.HoaDon','sdtKhachHangLucMua') IS NOT NULL AND COL_LENGTH('dbo.HoaDon','tenKhuyenMaiLucMua') IS NOT NULL AND COL_LENGTH('dbo.ChiTietHoaDon','tenSPLucBan') IS NOT NULL AND COL_LENGTH('dbo.ChiTietHoaDon','tenDanhMucLucBan') IS NOT NULL THEN 1 ELSE 0 END");ResultSet r=p.executeQuery()){r.next();return r.getInt(1)==1;}
    }
    public HoaDon save(HoaDon draft)throws SQLException {
        HoaDonService.validate(draft);
        try(Connection c=connections.open()) {
            c.setAutoCommit(false);
            try {
                if(!hasSnapshots(c))throw new SQLException("Chạy sql/02_BoSungHoaDon.sql trước khi lưu hóa đơn.");
                // Khóa transaction chung cho cấp mã và cập nhật kho; giải phóng tự động khi commit/rollback.
                try(PreparedStatement p=c.prepareStatement("DECLARE @rc int; EXEC @rc=sys.sp_getapplock @Resource=N'GiaThanh.LapHoaDon',@LockMode='Exclusive',@LockOwner='Transaction',@LockTimeout=10000; SELECT @rc");ResultSet r=p.executeQuery()){
                    if(!r.next()||r.getInt(1)<0)throw new SQLException("Đang có giao dịch khác. Vui lòng thử lại.");
                }
                validateShift(c,draft.nhanVien(),draft.ca());
                String customer=validateCustomer(c,draft);
                String promotion=validatePromotion(c,draft);
                validateProducts(c,draft);
                String prefix="HD"+draft.ngay().format(DateTimeFormatter.ofPattern("yyMMdd"));
                long sequence=next(c,"SELECT COALESCE(MAX(TRY_CONVERT(INT,RIGHT(maHD,4))),0)+1 FROM dbo.HoaDon WHERE maHD LIKE ?",prefix+"%");
                if(sequence>9999)throw new SQLException("Đã đạt 9999 hóa đơn/ngày.");
                HoaDon h=draft.voiMa(prefix+String.format(Locale.ROOT,"%04d",sequence));
                String insert="INSERT INTO dbo.HoaDon (maHD,ngayLap,tongTienHang,tienGiamGia,tongThanhToan,phuongThucThanhToan,yeuCauGiaoLapDat,ghiChuGiaoLapDat,trangThai,maNV,maKH,maKM,maCa,tienKhachDua,hangThanhVienLucMua,tenKhachHangLucMua,sdtKhachHangLucMua,tenKhuyenMaiLucMua) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try(PreparedStatement p=c.prepareStatement(insert)) {
                    p.setString(1,h.ma());p.setTimestamp(2,Timestamp.valueOf(h.ngay()));p.setBigDecimal(3,h.tienHang());p.setBigDecimal(4,h.giamGia());p.setBigDecimal(5,h.tongTien());
                    p.setNString(6,h.phuongThuc());p.setNString(7,h.giaoHang().isBlank()?"Không":"Có");p.setNString(8,h.giaoHang());p.setNString(9,h.trangThai());p.setString(10,h.nhanVien());
                    p.setString(11,customer);p.setString(12,promotion);p.setString(13,h.ca());p.setBigDecimal(14,h.khachDua());p.setNString(15,h.hang());p.setNString(16,h.khachHang());p.setString(17,h.sdt());p.setNString(18,h.khuyenMai());p.executeUpdate();
                }
                try(PreparedStatement detail=c.prepareStatement("INSERT INTO dbo.ChiTietHoaDon (maHD,maSKU,soLuong,donGia,thanhTien,tenSPLucBan,tenDanhMucLucBan) VALUES (?,?,?,?,?,?,?)");
                    PreparedStatement stock=c.prepareStatement("UPDATE dbo.SanPham SET slTon=slTon-? WHERE maSKU=? AND trangThai=1 AND slTon>=?")) {
                    for(HoaDon.Dong d:h.chiTiet()) {
                        detail.setString(1,h.ma());detail.setString(2,d.sku());detail.setInt(3,d.soLuong());detail.setBigDecimal(4,d.donGia());detail.setBigDecimal(5,d.thanhTien());detail.setNString(6,d.ten());detail.setNString(7,d.danhMuc());detail.executeUpdate();
                        stock.setInt(1,d.soLuong());stock.setString(2,d.sku());stock.setInt(3,d.soLuong());if(stock.executeUpdate()!=1)throw new SQLException("Không đủ tồn kho hoặc sản phẩm ngừng kinh doanh: "+d.sku());
                    }
                }
                if(h.phuongThuc().equals("Trả góp"))saveInstallment(c,h);
                if(customer!=null)try(PreparedStatement p=c.prepareStatement("UPDATE dbo.KhachHang SET tongChiTieuTichLuy=tongChiTieuTichLuy+? WHERE maKH=?")){p.setBigDecimal(1,h.tongTien());p.setString(2,customer);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("UPDATE dbo.CaLamViec SET tongDoanhThu=COALESCE(tongDoanhThu,0)+? WHERE maCa=?")){p.setBigDecimal(1,h.tongTien());p.setString(2,h.ca());p.executeUpdate();}
                c.commit();return h;
            }catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
        }
    }
    private static long next(Connection c,String query,String pattern)throws SQLException {
        try(PreparedStatement p=c.prepareStatement(query)){p.setString(1,pattern);try(ResultSet r=p.executeQuery()){r.next();return r.getLong(1);}}
    }
    private static void validateShift(Connection c,String employee,String shift)throws SQLException {
        String sql="SELECT 1 FROM dbo.NhanVien n WITH (UPDLOCK,HOLDLOCK) JOIN dbo.LichLamViec l ON l.maNV=n.maNV JOIN dbo.CaLamViec ca WITH (UPDLOCK,HOLDLOCK) ON ca.maCa=l.maCa WHERE n.maNV=? AND n.trangThai=1 AND n.chucVu IN (N'Quản lý',N'Bán hàng') AND ca.maCa=? AND l.ngayLamViec=CAST(GETDATE() AS DATE) AND ca.trangThai=N'Đang mở' AND ca.thoiGianDongCa IS NULL AND CAST(ca.thoiGianMoCa AS DATE)=CAST(GETDATE() AS DATE)";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,employee);p.setString(2,shift);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Nhân viên không hoạt động, chưa được phân ca hoặc ca hôm nay chưa mở.");}}
    }
    private static String validateCustomer(Connection c,HoaDon h)throws SQLException {
        if(h.hang().equals("Khách lẻ"))return null;
        try(PreparedStatement p=c.prepareStatement("SELECT maKH,tenKH,hangTV FROM dbo.KhachHang WITH (UPDLOCK,HOLDLOCK) WHERE sdt=? AND trangThai=1")) {
            p.setString(1,h.sdt());try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new SQLException("Không tìm thấy thành viên đang hoạt động theo SĐT.");
                if(!h.hang().equals(r.getString("hangTV"))||!h.khachHang().equals(r.getString("tenKH")))throw new SQLException("Thông tin thành viên đã thay đổi. Tìm lại khách hàng trước khi thanh toán.");
                if(h.giamHoaDon().compareTo(HoaDonService.uuDai(h.hang(),h.tienHang()))<0)throw new SQLException("Chưa áp dụng đủ ưu đãi thành viên.");
                return r.getString("maKH");
            }
        }
    }
    private static String validatePromotion(Connection c,HoaDon h)throws SQLException {
        if(h.khuyenMai().isBlank())return null;
        try(PreparedStatement p=c.prepareStatement("SELECT maKM FROM dbo.KhuyenMai WITH (UPDLOCK,HOLDLOCK) WHERE (maKM=? OR tenKM=?) AND trangThai=N'Đang kích hoạt' AND ? BETWEEN thoiGianBatDau AND thoiGianKetThuc")){
            p.setNString(1,h.khuyenMai());p.setNString(2,h.khuyenMai());p.setTimestamp(3,Timestamp.valueOf(h.ngay()));
            try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Mã/tên chương trình không tồn tại hoặc không trong thời gian kích hoạt.");String id=r.getString(1);if(r.next())throw new SQLException("Tên khuyến mãi bị trùng. Vui lòng nhập mã khuyến mãi.");return id;}
        }
    }
    private static void validateProducts(Connection c,HoaDon h)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT s.tenSP,s.donGia,s.slTon,s.trangThai,d.tenDanhMuc FROM dbo.SanPham s WITH (UPDLOCK,HOLDLOCK) JOIN dbo.DanhMuc d ON d.maDanhMuc=s.maDanhMuc WHERE s.maSKU=?")){
            for(HoaDon.Dong d:h.chiTiet()) {p.setString(1,d.sku());try(ResultSet r=p.executeQuery()){
                if(!r.next()||!r.getBoolean("trangThai"))throw new SQLException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh: "+d.sku());
                if(r.getInt("slTon")<d.soLuong())throw new SQLException("Không đủ tồn kho: "+d.sku());
                if(!r.getString("tenSP").equals(d.ten())||!r.getString("tenDanhMuc").equals(d.danhMuc())||r.getBigDecimal("donGia").compareTo(d.donGia())!=0)throw new SQLException("Thông tin / giá sản phẩm đã thay đổi. Chọn lại sản phẩm: "+d.sku());
            }}
        }
    }
    private static void saveInstallment(Connection c,HoaDon h)throws SQLException {
        long id=next(c,"SELECT COALESCE(MAX(TRY_CONVERT(BIGINT,SUBSTRING(maTraGop,3,8))),0)+1 FROM dbo.GiaoDichTraGop WHERE maTraGop LIKE ?","TG%");
        if(id>99999999)throw new SQLException("Đã đạt giới hạn mã trả góp.");
        BigDecimal balance=h.tongTien().subtract(h.khachDua());
        try(PreparedStatement p=c.prepareStatement("INSERT INTO dbo.GiaoDichTraGop (maTraGop,doiTacTaiChinh,kyHan,tienTraTruoc,tienGopMoiThang,soTienConLai,trangThai,maHD) VALUES (?,?,?,?,?,?,?,?)")){
            p.setString(1,"TG"+String.format(Locale.ROOT,"%08d",id));p.setNString(2,h.doiTac());p.setInt(3,h.kyHan());p.setBigDecimal(4,h.khachDua());p.setBigDecimal(5,balance.divide(BigDecimal.valueOf(h.kyHan()),0,RoundingMode.CEILING));p.setBigDecimal(6,balance);p.setNString(7,"Đã duyệt");p.setString(8,h.ma());p.executeUpdate();
        }
    }
}
