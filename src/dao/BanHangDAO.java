package dao;

import connect.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Dữ liệu chọn trên màn hình lập hóa đơn, không tạo giả nhân viên/sản phẩm/ca. */
public final class BanHangDAO {
    public record SanPham(String sku,String name,String category,BigDecimal price,int stock) {
        @Override public String toString(){return sku+" • "+name+" • Tồn: "+stock;}
    }
    public record KhachHang(String name,String phone,String tier) {}
    public record Ca(String id,String name) { @Override public String toString(){return id+" • "+name;} }
    public List<SanPham> products()throws SQLException {
        List<SanPham> result=new ArrayList<>();
        try(Connection c=ConnectDB.openConnection();PreparedStatement p=c.prepareStatement("SELECT s.maSKU,s.tenSP,d.tenDanhMuc,s.donGia,s.slTon FROM dbo.SanPham s JOIN dbo.DanhMuc d ON d.maDanhMuc=s.maDanhMuc WHERE s.trangThai=1 AND s.slTon>0 ORDER BY s.tenSP");ResultSet r=p.executeQuery()){
            while(r.next())result.add(new SanPham(r.getString(1),r.getString(2),r.getString(3),r.getBigDecimal(4),r.getInt(5)));
        }return result;
    }
    public KhachHang customer(String phone)throws SQLException {
        try(Connection c=ConnectDB.openConnection();PreparedStatement p=c.prepareStatement("SELECT tenKH,sdt,COALESCE(hangTV,N'Member') FROM dbo.KhachHang WHERE sdt=? AND trangThai=1")){
            p.setString(1,phone);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Không tìm thấy thành viên đang hoạt động theo SĐT.");return new KhachHang(r.getString(1),r.getString(2),r.getString(3));}
        }
    }
    public List<Ca> shifts(String employee)throws SQLException {
        List<Ca> result=new ArrayList<>();
        String sql="SELECT DISTINCT c.maCa,COALESCE(d.tenCa,c.maCa) FROM dbo.CaLamViec c JOIN dbo.LichLamViec l ON l.maCa=c.maCa JOIN dbo.NhanVien n ON n.maNV=l.maNV LEFT JOIN dbo.DanhMucCa d ON d.maLoaiCa=c.maLoaiCa WHERE l.maNV=? AND n.trangThai=1 AND n.chucVu IN (N'Quản lý',N'Bán hàng') AND l.ngayLamViec=CAST(GETDATE() AS DATE) AND c.trangThai=N'Đang mở' AND c.thoiGianDongCa IS NULL AND CAST(c.thoiGianMoCa AS DATE)=CAST(GETDATE() AS DATE)";
        try(Connection c=ConnectDB.openConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,employee);try(ResultSet r=p.executeQuery()){while(r.next())result.add(new Ca(r.getString(1),r.getString(2)));}
        }return result;
    }
}
