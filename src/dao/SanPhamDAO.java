package dao;

import connect.ConnectDB;
import entity.DanhMuc;
import entity.KhuyenMai;
import entity.SanPham;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.Normalizer;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Data Access Object cho SanPham dựa trên CSDL QuanLyBanHang.
 * Quản lý bằng mã SKU (maSKU làm khóa chính, không tạo id tự động).
 * Hỗ trợ lưu đường dẫn / Base64 hình ảnh và quản lý tự động ngày tạo / cập nhật.
 */
public class SanPhamDAO {

    private static final String SELECT_BASE =
            "SELECT sp.maSKU, sp.maVach, sp.tenSP, sp.donGia, sp.thoiGianBaoHanh, sp.slTon, sp.thuongHieu, " +
            "       sp.model, sp.moTa, sp.nhomSanPham, sp.trangThai, sp.maDanhMuc, sp.maKM, " +
            "       sp.hinhAnh, sp.ngayTao, sp.ngayCapNhat, " +
            "       dm.tenDanhMuc, dm.moTa AS moTaDM, " +
            "       km.tenKM, km.giaTriGiam, km.loaiKM " +
            "FROM SanPham sp " +
            "LEFT JOIN DanhMuc dm ON sp.maDanhMuc = dm.maDanhMuc " +
            "LEFT JOIN KhuyenMai km ON sp.maKM = km.maKM ";

    public SanPhamDAO() { }

    private SanPham docDong(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();
        sp.setMaSKU(rs.getString("maSKU"));
        sp.setMaVach(rs.getString("maVach"));
        sp.setTenSP(rs.getString("tenSP"));
        sp.setDonGia(rs.getBigDecimal("donGia"));
        sp.setThoiGianBaoHanh(rs.getInt("thoiGianBaoHanh"));
        sp.setSlTon(rs.getInt("slTon"));
        sp.setThuongHieu(rs.getString("thuongHieu"));
        sp.setModel(rs.getString("model"));
        sp.setMoTa(rs.getString("moTa"));
        sp.setHinhAnh(rs.getString("hinhAnh")); // Cột ảnh
        
        String nhom = rs.getString("nhomSanPham");
        sp.setNhomSP(nhom == null ? "Gọn nhẹ" : nhom);
        sp.setTrangThai(rs.getBoolean("trangThai"));
        
        // Thời gian
        Timestamp created = rs.getTimestamp("ngayTao");
        Timestamp updated = rs.getTimestamp("ngayCapNhat");
        sp.setNgayTao(created != null ? created.toLocalDateTime() : null);
        sp.setNgayCapNhat(updated != null ? updated.toLocalDateTime() : null);

        // Danh Mục
        String maDM = rs.getString("maDanhMuc");
        String tenDM = rs.getString("tenDanhMuc");
        sp.setDanhMuc(new DanhMuc(maDM, tenDM != null ? tenDM : maDM, rs.getString("moTaDM")));

        // Khuyến Mãi
        String maKM = rs.getString("maKM");
        if (maKM != null) {
            KhuyenMai km = new KhuyenMai();
            km.setMaKM(maKM);
            km.setTenKM(rs.getString("tenKM"));
            km.setGiaTriGiam(rs.getDouble("giaTriGiam"));
            km.setLoaiKM(rs.getString("loaiKM"));
            sp.setKhuyenMai(km);
        }
        return sp;
    }

    /** Lấy toàn bộ danh sách sản phẩm */
    public ArrayList<SanPham> layTatCa() {
        ArrayList<SanPham> ketQua = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " ORDER BY sp.maSKU");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ketQua.add(docDong(rs));
            }
            return ketQua;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách sản phẩm từ cơ sở dữ liệu.", e);
        }
    }

    /** Tìm sản phẩm theo mã SKU (mã SKU là khóa chính duy nhất) */
    public SanPham timTheoMa(String maSKU) {
        if (maSKU == null || maSKU.trim().isEmpty()) return null;
        try (Connection conn = ConnectDB.openConnection()) {
            return timTheoMa(conn, maSKU.trim());
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm sản phẩm theo SKU " + maSKU + ".", e);
        }
    }

    private SanPham timTheoMa(Connection conn, String maSKU) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE sp.maSKU = ?")) {
            ps.setString(1, maSKU);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docDong(rs) : null;
            }
        }
    }

    /** Tìm sản phẩm theo mã vạch */
    public SanPham timTheoMaVach(String maVach) {
        if (maVach == null || maVach.trim().isEmpty()) return null;
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE sp.maVach = ?")) {
            ps.setString(1, maVach.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docDong(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm sản phẩm theo mã vạch.", e);
        }
        return null;
    }

    /** Thêm sản phẩm mới */
    public SanPham them(SanPham sanPham) {
        if (sanPham == null) throw new IllegalArgumentException("Chưa nhập thông tin sản phẩm.");
        kiemTra(sanPham, true);

        try (Connection conn = ConnectDB.openConnection()) {
            conn.setAutoCommit(false);
            try {
                // Kiểm tra mã SKU đã tồn tại
                try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM SanPham WHERE maSKU = ?")) {
                    check.setString(1, sanPham.getMaSKU());
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException("Mã SKU '" + sanPham.getMaSKU() + "' đã tồn tại trong hệ thống.");
                        }
                    }
                }

                // Kiểm tra mã vạch duy nhất
                if (sanPham.getMaVach() != null && !sanPham.getMaVach().isEmpty()) {
                    try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM SanPham WHERE maVach = ?")) {
                        check.setString(1, sanPham.getMaVach());
                        try (ResultSet rs = check.executeQuery()) {
                            if (rs.next()) {
                                throw new IllegalArgumentException("Mã vạch '" + sanPham.getMaVach() + "' đã tồn tại ở sản phẩm khác.");
                            }
                        }
                    }
                }

                // Insert thông tin (Không cần truyền ngayTao và ngayCapNhat vì SQL Server có DEFAULT)
                String sql = "INSERT INTO SanPham (maSKU, maVach, tenSP, donGia, thoiGianBaoHanh, slTon, thuongHieu, " +
                             "model, moTa, nhomSanPham, trangThai, maDanhMuc, maKM, hinhAnh) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sanPham.getMaSKU());
                    ps.setString(2, (sanPham.getMaVach() == null || sanPham.getMaVach().isEmpty()) ? null : sanPham.getMaVach());
                    ps.setString(3, sanPham.getTenSP());
                    ps.setBigDecimal(4, sanPham.getDonGia());
                    ps.setInt(5, sanPham.getThoiGianBaoHanh());
                    ps.setInt(6, sanPham.getSlTon());
                    ps.setString(7, sanPham.getThuongHieu());
                    ps.setString(8, (sanPham.getModel() == null || sanPham.getModel().isEmpty()) ? null : sanPham.getModel());
                    ps.setString(9, (sanPham.getMoTa() == null || sanPham.getMoTa().isEmpty()) ? null : sanPham.getMoTa());
                    ps.setString(10, sanPham.getNhomSP());
                    ps.setBoolean(11, sanPham.isTrangThai());
                    ps.setString(12, sanPham.getDanhMuc().getMaDanhMuc()); // Chú ý gọi hàm lấy mã Danh Mục chuẩn
                
                    if (sanPham.getMaKM() == null) ps.setNull(13, Types.VARCHAR);
                    else ps.setString(13, sanPham.getMaKM());
                
                    ps.setString(14, (sanPham.getHinhAnh() == null || sanPham.getHinhAnh().isEmpty()) ? null : sanPham.getHinhAnh());
                
                    ps.executeUpdate();
                }
                SanPham daLuu = timTheoMa(conn, sanPham.getMaSKU());
                if (daLuu == null) throw new SQLException("Không đọc được sản phẩm vừa thêm.");
                conn.commit();
                return daLuu;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627) {
                throw new IllegalArgumentException("SKU hoặc mã vạch đã tồn tại.", e);
            }
            if (e.getErrorCode() == 547 || "23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Dữ liệu sản phẩm, danh mục hoặc khuyến mãi không hợp lệ.", e);
            }
            throw new IllegalStateException("Không thể lưu sản phẩm vào cơ sở dữ liệu.", e);
        }
    }

    /** Cập nhật thông tin sản phẩm */
    public SanPham capNhat(SanPham sanPham) {
        if (sanPham == null) throw new IllegalArgumentException("Chưa nhập thông tin sản phẩm.");
        kiemTra(sanPham, false);

        try (Connection conn = ConnectDB.openConnection()) {
            // Kiểm tra mã vạch trùng
            if (sanPham.getMaVach() != null && !sanPham.getMaVach().isEmpty()) {
                try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM SanPham WHERE maVach = ? AND maSKU <> ?")) {
                    check.setString(1, sanPham.getMaVach());
                    check.setString(2, sanPham.getMaSKU());
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException("Mã vạch đã được dùng cho sản phẩm khác.");
                        }
                    }
                }
            }

            // Update thông tin (Trigger SQL Server sẽ tự động lo việc đổi ngayCapNhat, ta không cần gửi xuống)
            String sql = "UPDATE SanPham SET maVach=?, tenSP=?, donGia=?, thoiGianBaoHanh=?, thuongHieu=?, " +
                         "model=?, moTa=?, nhomSanPham=?, trangThai=?, maDanhMuc=?, maKM=?, hinhAnh=? " +
                         "WHERE maSKU=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, (sanPham.getMaVach() == null || sanPham.getMaVach().isEmpty()) ? null : sanPham.getMaVach());
                ps.setString(2, sanPham.getTenSP());
                ps.setBigDecimal(3, sanPham.getDonGia());
                ps.setInt(4, sanPham.getThoiGianBaoHanh());
                ps.setString(5, sanPham.getThuongHieu());
                ps.setString(6, (sanPham.getModel() == null || sanPham.getModel().isEmpty()) ? null : sanPham.getModel());
                ps.setString(7, (sanPham.getMoTa() == null || sanPham.getMoTa().isEmpty()) ? null : sanPham.getMoTa());
                ps.setString(8, sanPham.getNhomSP());
                ps.setBoolean(9, sanPham.isTrangThai());
                ps.setString(10, sanPham.getDanhMuc().getMaDanhMuc());
                
                if (sanPham.getMaKM() == null) ps.setNull(11, Types.VARCHAR);
                else ps.setString(11, sanPham.getMaKM());
                
                ps.setString(12, (sanPham.getHinhAnh() == null || sanPham.getHinhAnh().isEmpty()) ? null : sanPham.getHinhAnh());
                ps.setString(13, sanPham.getMaSKU());
                
                int rows = ps.executeUpdate();
                if (rows == 0) throw new IllegalArgumentException("Sản phẩm không còn tồn tại trong hệ thống.");
            }
            return timTheoMa(sanPham.getMaSKU());
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Mã vạch, danh mục hoặc khuyến mãi không hợp lệ hoặc đã tồn tại.", e);
            }
            throw new IllegalStateException("Không thể cập nhật sản phẩm vào cơ sở dữ liệu.", e);
        }
    }

    /** Đổi trạng thái kinh doanh của sản phẩm */
    public void chuyenTrangThai(String maSKU, boolean dangKinhDoanh) {
        if (maSKU == null || maSKU.trim().isEmpty()) return;
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET trangThai = ? WHERE maSKU = ?")) {
            ps.setBoolean(1, dangKinhDoanh);
            ps.setString(2, maSKU.trim());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Sản phẩm không còn tồn tại trong hệ thống.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật trạng thái sản phẩm.", e);
        }
    }

    /** Nhập kho (cộng dồn) */
    public SanPham nhapKho(String maSKU, int soLuongNhap) {
        if (maSKU == null || maSKU.trim().isEmpty()) throw new IllegalArgumentException("Chưa chọn sản phẩm.");
        if (soLuongNhap <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        String sql = "UPDATE SanPham SET slTon = slTon + ? WHERE maSKU = ? AND slTon <= 2147483647 - ?";
        try (Connection conn = ConnectDB.openConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongNhap);
            ps.setString(2, maSKU.trim());
            ps.setInt(3, soLuongNhap);
            if (ps.executeUpdate() == 0) {
                if (timTheoMa(maSKU) == null) throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
                throw new IllegalArgumentException("Số lượng tồn kho vượt giới hạn lưu trữ.");
            }
            return timTheoMa(maSKU);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể nhập tồn kho cho sản phẩm.", e);
        }
    }

    /** Xóa sản phẩm nếu chưa có giao dịch */
    public void xoa(String maSKU) {
        if (maSKU == null || maSKU.trim().isEmpty()) throw new IllegalArgumentException("Chưa chọn sản phẩm.");
        String sku = maSKU.trim();
        SanPham sp = timTheoMa(sku);
        if (sp == null) throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
        if (sp.isTrangThai()) {
            throw new IllegalArgumentException("Hãy chuyển sang trạng thái 'Ngừng kinh doanh' trước khi xóa.");
        }
        if (sp.getSlTon() != 0) throw new IllegalArgumentException("Chỉ xóa sản phẩm khi tồn kho bằng 0.");

        try (Connection conn = ConnectDB.openConnection()) {
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM ChiTietHoaDon WHERE maSKU = ?")) {
                check.setString(1, sku);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("Sản phẩm đã có hóa đơn bán hàng. Không thể xóa.");
                }
            }

            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM PhieuBaoHanh WHERE maSKU = ?")) {
                check.setString(1, sku);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("Sản phẩm đã có phiếu bảo hành liên kết. Không thể xóa.");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE maSKU = ? AND trangThai = 0 AND slTon = 0")) {
                ps.setString(1, sku);
                if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Sản phẩm đã thay đổi trạng thái hoặc tồn kho. Hãy tải lại.");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    /** Kiểm tra hợp lệ dữ liệu sản phẩm */
    private void kiemTra(SanPham sp, boolean taoMoi) {
        String sku = donGianHoa(sp.getMaSKU()).toUpperCase(Locale.ROOT);
        sp.setMaSKU(sku);
        sp.setTenSP(donGianHoa(sp.getTenSP()));
        sp.setThuongHieu(donGianHoa(sp.getThuongHieu()));
        sp.setModel(donGianHoa(sp.getModel()));
        sp.setMoTa(sp.getMoTa() == null ? "" : sp.getMoTa().trim());

        if (sp.getMaVach() != null) sp.setMaVach(sp.getMaVach().trim());
        if (sp.getNhomSP() == null || sp.getNhomSP().trim().isEmpty()) {
            sp.setNhomSP("Gọn nhẹ");
        } else if (!"Gọn nhẹ".equals(sp.getNhomSP()) && !"Cồng kềnh".equals(sp.getNhomSP())) {
            throw new IllegalArgumentException("Nhóm sản phẩm phải là 'Gọn nhẹ' hoặc 'Cồng kềnh'.");
        }

        if (!sku.matches("[A-Z0-9][A-Z0-9._-]{1,19}")) {
            throw new IllegalArgumentException("Mã SKU phải có 2–20 ký tự gồm chữ cái, chữ số, dấu chấm, gạch ngang, gạch dưới.");
        }
        if (sp.getTenSP().length() < 3 || sp.getTenSP().length() > 150) {
            throw new IllegalArgumentException("Tên sản phẩm phải có từ 3 đến 150 ký tự.");
        }
        if (sp.getThuongHieu().isEmpty() || sp.getThuongHieu().length() > 100) {
            throw new IllegalArgumentException("Thương hiệu phải có từ 1 đến 100 ký tự.");
        }
        if (sp.getDanhMuc() == null || sp.getDanhMuc().getMaDanhMuc() == null || sp.getDanhMuc().getMaDanhMuc().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục cho sản phẩm.");
        }
        
        if (sp.getMaVach() != null && sp.getMaVach().isBlank()) sp.setMaVach(null);
        if (sp.getMaKM() != null) {
            sp.setMaKM(sp.getMaKM().trim());
            if (sp.getMaKM().isEmpty()) sp.setMaKM(null);
        }
        if (sp.getMaVach() != null && sp.getMaVach().length() > 50) throw new IllegalArgumentException("Mã vạch tối đa 50 ký tự.");
        if (sp.getModel().length() > 100) throw new IllegalArgumentException("Model tối đa 100 ký tự.");

        BigDecimal gia = sp.getDonGia();
        if (gia == null || gia.compareTo(BigDecimal.ZERO) <= 0 || gia.compareTo(new BigDecimal("999999999999999999")) > 0) {
            throw new IllegalArgumentException("Đơn giá bán phải lớn hơn 0 đ.");
        }
        if (gia.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Giá bán phải là số nguyên VND.");
        }
        sp.setDonGia(gia.setScale(0));

        if (sp.getThoiGianBaoHanh() < 0 || sp.getThoiGianBaoHanh() > 120) throw new IllegalArgumentException("Thời gian bảo hành từ 0 đến 120 tháng.");
        if (sp.getSlTon() < 0) throw new IllegalArgumentException("Tồn kho không được âm.");
        if (sp.getMoTa().length() > 500) throw new IllegalArgumentException("Mô tả sản phẩm tối đa 500 ký tự.");
    }

    private String donGianHoa(String val) {
        return val == null ? "" : val.trim().replaceAll("\\s+", " ");
    }

    /** Lấy danh sách thương hiệu hiện có */
    public ArrayList<String> layThuongHieu() {
        ArrayList<String> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT thuongHieu FROM SanPham WHERE thuongHieu IS NOT NULL AND thuongHieu <> '' ORDER BY thuongHieu");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getString(1));
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách thương hiệu.", e);
        }
    }

    /** Lấy danh sách các mức bảo hành */
    public ArrayList<Integer> layBaoHanh() {
        ArrayList<Integer> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT thoiGianBaoHanh FROM SanPham WHERE thoiGianBaoHanh IS NOT NULL ORDER BY thoiGianBaoHanh");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getInt(1));
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách thời hạn bảo hành.", e);
        }
    }

    /** Lấy toàn bộ danh sách Khuyến Mãi */
    public ArrayList<KhuyenMai> layDanhSachKhuyenMai() {
        ArrayList<KhuyenMai> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maKM, tenKM, giaTriGiam, loaiKM, trangThai FROM KhuyenMai ORDER BY maKM");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setMaKM(rs.getString("maKM"));
                km.setTenKM(rs.getString("tenKM"));
                km.setGiaTriGiam(rs.getDouble("giaTriGiam"));
                km.setLoaiKM(rs.getString("loaiKM"));
                km.setTrangThai(rs.getString("trangThai"));
                ds.add(km);
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách khuyến mãi.", e);
        }
    }

    /** Tìm kiếm sản phẩm nâng cao trên bộ nhớ */
    public ArrayList<SanPham> timKiem(String tuKhoa, String danhMucId, String thuongHieu,
            int khoangGia, Integer baoHanh, Boolean dangKinhDoanh) {
        return timKiem(layTatCa(), tuKhoa, danhMucId, thuongHieu, khoangGia, baoHanh, dangKinhDoanh);
    }

    public ArrayList<SanPham> timKiem(java.util.List<SanPham> tatCa, String tuKhoa, String danhMucId, String thuongHieu,
            int khoangGia, Integer baoHanh, Boolean dangKinhDoanh) {

        ArrayList<SanPham> ketQua = new ArrayList<>();
        String query = chuanHoaTimKiem(tuKhoa);

        for (SanPham sp : tatCa) {
            if (danhMucId != null && !danhMucId.trim().isEmpty() && !danhMucId.equals(sp.getMaDanhMuc())) {
                continue;
            }
            if (thuongHieu != null && !thuongHieu.trim().isEmpty() &&
                    !chuanHoaTimKiem(thuongHieu).equals(chuanHoaTimKiem(sp.getThuongHieu()))) {
                continue;
            }
            if (baoHanh != null && baoHanh != sp.getThoiGianBaoHanh()) continue;
            if (dangKinhDoanh != null && dangKinhDoanh != sp.isTrangThai()) continue;

            BigDecimal gia = sp.getDonGia();
            int soVoi5 = gia.compareTo(new BigDecimal("5000000"));
            int soVoi15 = gia.compareTo(new BigDecimal("15000000"));
            if (khoangGia == 1 && soVoi5 >= 0) continue;
            if (khoangGia == 2 && (soVoi5 < 0 || soVoi15 > 0)) continue;
            if (khoangGia == 3 && soVoi15 <= 0) continue;

            if (!query.isEmpty()) {
                String noiDung = chuanHoaTimKiem(sp.getTenSP() + " " + sp.getMaSKU() + " " +
                        (sp.getMaVach() != null ? sp.getMaVach() : "") + " " +
                        sp.getThuongHieu() + " " + (sp.getModel() != null ? sp.getModel() : "") + " " +
                        sp.getTenDanhMuc());
                boolean match = true;
                for (String tu : query.split("\\s+")) {
                    if (!noiDung.contains(tu)) {
                        match = false;
                        break;
                    }
                }
                if (!match) continue;
            }
            ketQua.add(sp);
        }
        return ketQua;
    }

    /** Sắp xếp sản phẩm */
    public void sapXep(ArrayList<SanPham> danhSach, final int kieu) {
        Collections.sort(danhSach, new Comparator<SanPham>() {
            @Override
            public int compare(SanPham a, SanPham b) {
                int c;
                if (kieu == 2) c = a.getDonGia().compareTo(b.getDonGia());
                else if (kieu == 3) c = b.getDonGia().compareTo(a.getDonGia());
                else if (kieu == 4) c = a.getMaSKU().compareTo(b.getMaSKU());
                else {
                    c = chuanHoaTimKiem(a.getTenSP()).compareTo(chuanHoaTimKiem(b.getTenSP()));
                    if (kieu == 1) c = -c;
                }
                if (c == 0) c = a.getMaSKU().compareTo(b.getMaSKU());
                return c;
            }
        });
    }

    /** Hàm hỗ trợ loại bỏ dấu tiếng Việt để tìm kiếm */
    public static String chuanHoaTimKiem(String value) {
        if (value == null) return "";
        String k = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return k.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    /** Xử lý đọc ảnh ra Base64 */
    public static String docAnh(Path path) throws IOException {
        if (Files.size(path) > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh không được vượt quá 2 MB.");
        String value = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        kiemTraAnh(value);
        return value;
    }

    private static void kiemTraAnh(String value) {
        if (value == null || value.isEmpty()) return;
        try {
            if (value.length() > 2_800_000) throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
            byte[] bytes = Base64.getDecoder().decode(value);
            if (bytes.length > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
            try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new IllegalArgumentException("Hãy chọn ảnh PNG hoặc JPEG hợp lệ.");
                ImageReader reader = readers.next();
                try {
                    reader.setInput(stream);
                    String format = reader.getFormatName();
                    if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg")) {
                        throw new IllegalArgumentException("Chỉ hỗ trợ PNG và JPEG.");
                    }
                    if ((long) reader.getWidth(0) * reader.getHeight(0) > 16_000_000) {
                        throw new IllegalArgumentException("Ảnh vượt quá 16 triệu điểm ảnh.");
                    }
                    if (reader.read(0) == null) throw new IllegalArgumentException("Không đọc được ảnh.");
                } finally {
                    reader.dispose();
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ảnh không hợp lệ hoặc bị lỗi định dạng.");
        }
    }
}