//package tools;
//
//import connect.ConnectDB;
//import dao.SqlHoaDonRepository;
//import java.sql.*;
//
///** Chẩn đoán chỉ đọc, không in mật khẩu hoặc thông tin giao dịch. */
//public final class KiemTraKetNoi {
//    public static void main(String[] args) throws Exception {
//        try(Connection c=ConnectDB.openConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(
//            "SELECT DB_NAME(),COL_LENGTH('dbo.HoaDon','tienKhachDua'),(SELECT COUNT(*) FROM dbo.SanPham),(SELECT COUNT(*) FROM dbo.NhanVien)")) {
//            r.next();System.out.println("SQL connection OK. Database: "+r.getString(1));
//            System.out.println("Invoice migration ready: "+(r.getObject(2)!=null));
//            System.out.println("Products: "+r.getInt(3)+"; employees: "+r.getInt(4));
//        }
//        System.out.println("Invoice query OK. Count: "+new SqlHoaDonRepository().findAll().size());
//    }
//}
