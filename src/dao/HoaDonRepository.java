//package dao;
//import entity.HoaDon;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.sql.SQLException;
//import java.util.List;
//
///** Ứng dụng dùng SQL Server; XML chỉ được bật tường minh cho kiểm thử/offline. */
//public final class HoaDonRepository {
//    private static final HoaDonRepository INSTANCE = "xml".equalsIgnoreCase(System.getProperty("giathanh.storage"))
//        ? new HoaDonRepository(Path.of(System.getProperty("giathanh.data", "data/hoa-don.xml"))) : new HoaDonRepository();
//    private final FileHoaDonRepository file;
//    private final SqlHoaDonRepository sql;
//    private HoaDonRepository() { file=null; sql=new SqlHoaDonRepository(); }
//    public HoaDonRepository(Path path) { file=new FileHoaDonRepository(path); sql=null; }
//    public static HoaDonRepository getInstance() { return INSTANCE; }
//    public boolean isSql() { return sql!=null; }
//    public Path getPath() { if(file==null)throw new IllegalStateException("Đang dùng SQL Server.");return file.getPath(); }
//    public List<HoaDon> findAll() throws IOException {
//        if(file!=null)return file.findAll();
//        try{return sql.findAll();}catch(SQLException e){throw databaseError(e);}
//    }
//    public HoaDon save(HoaDon h) throws IOException {
//        if(file!=null)return file.save(h);
//        try{return sql.save(h);}catch(SQLException e){throw databaseError(e);}
//    }
//    public static IOException databaseError(SQLException e) {
//        return new IOException("Không thể thao tác SQL Server. Kiểm tra kết nối và chạy các script trong sql/. Chi tiết: "+e.getMessage(),e);
//    }
//}
