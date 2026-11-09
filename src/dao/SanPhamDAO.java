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
 * Sử dụng trực tiếp các bảng SanPham, DanhMuc và KhuyenMai trong CSDL.
 */
public class SanPhamDAO {

    private static final String SELECT_BASE =
            "SELECT sp.maSKU, sp.maVach, sp.tenSP, sp.donGia, sp.thoiGianBaoHanh, sp.slTon, sp.thuongHieu, " +
            "       sp.model, sp.moTa, sp.nhomSanPham, sp.trangThai, sp.maDanhMuc, sp.maKM, " +
            "       dm.tenDanhMuc, dm.moTa AS moTaDM, " +
            "       km.tenKM, km.giaTriGiam, km.loaiKM " +
            "FROM SanPham sp " +
            "LEFT JOIN DanhMuc dm ON sp.maDanhMuc = dm.maDanhMuc " +
            "LEFT JOIN KhuyenMai km ON sp.maKM = km.maKM ";

    public SanPhamDAO() { }

    public SanPhamDAO(DuLieuTam duLieu) {
        this();
    }

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
        String nhom = rs.getString("nhomSanPham");
        sp.setNhomSP(nhom == null ? "Gọn nhẹ" : nhom);
        sp.setTrangThai(rs.getBoolean("trangThai"));

        String maDM = rs.getString("maDanhMuc");
        String tenDM = rs.getString("tenDanhMuc");
        sp.setDanhMuc(new DanhMuc(maDM, tenDM != null ? tenDM : maDM, rs.getString("moTaDM")));

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
        String sku = maSKU.trim();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE sp.maSKU = ?")) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docDong(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm sản phẩm theo SKU " + sku + ".", e);
        }
        return null;
    }

    /** Tìm sản phẩm theo mã vạch */
    public SanPham timTheoMaVach(String maVach) {
        if (maVach == null || maVach.trim().isEmpty()) return null;
        String mv = maVach.trim();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE sp.maVach = ?")) {
            ps.setString(1, mv);
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

    /**
     * Thêm sản phẩm mới.
     * Người dùng quản lý bằng mã SKU tự nhập (không tạo id tự động).
     */
    public SanPham them(SanPham sanPham) {
        if (sanPham == null) throw new IllegalArgumentException("Chưa nhập thông tin sản phẩm.");
        kiemTra(sanPham, true);

        try (Connection conn = ConnectDB.openConnection()) {
            // Kiểm tra mã SKU đã tồn tại chưa
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM SanPham WHERE maSKU = ?")) {
                check.setString(1, sanPham.getMaSKU());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException("Mã SKU '" + sanPham.getMaSKU() + "' đã tồn tại trong hệ thống.");
                    }
                }
            }

            // Kiểm tra mã vạch duy nhất nếu có
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

            String sql = "INSERT INTO SanPham (maSKU, maVach, tenSP, donGia, thoiGianBaoHanh, slTon, thuongHieu, " +
                         "                    model, moTa, nhomSanPham, trangThai, maDanhMuc, maKM) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                ps.setString(12, sanPham.getDanhMuc().getMaDM());
                if (sanPham.getMaKM() == null) ps.setNull(13, Types.VARCHAR);
                else ps.setString(13, sanPham.getMaKM());
                ps.executeUpdate();
            }
            return timTheoMa(sanPham.getMaSKU());
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("SKU, mã vạch, danh mục hoặc khuyến mãi không hợp lệ hoặc đã tồn tại.", e);
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

            String sql = "UPDATE SanPham SET maVach=?, tenSP=?, donGia=?, thoiGianBaoHanh=?, thuongHieu=?, " +
                         "                 model=?, moTa=?, nhomSanPham=?, trangThai=?, maDanhMuc=?, maKM=? " +
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
                ps.setString(10, sanPham.getDanhMuc().getMaDM());
                if (sanPham.getMaKM() == null) ps.setNull(11, Types.VARCHAR);
                else ps.setString(11, sanPham.getMaKM());
                ps.setString(12, sanPham.getMaSKU());
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
        String sku = maSKU.trim();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET trangThai = ? WHERE maSKU = ?")) {
            ps.setBoolean(1, dangKinhDoanh);
            ps.setString(2, sku);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Sản phẩm không còn tồn tại trong hệ thống.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật trạng thái sản phẩm.", e);
        }
    }

    /** Nhập bổ sung tồn kho bằng phép cộng nguyên tử để không ghi đè số lượng đã bán. */
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

    /**
     * Xóa sản phẩm khỏi CSDL.
     * Nếu sản phẩm đã phát sinh chi tiết hóa đơn hoặc phiếu bảo hành, sẽ từ chối xóa để bảo toàn ràng buộc dữ liệu.
     */
    public void xoa(String maSKU) {
        if (maSKU == null || maSKU.trim().isEmpty()) throw new IllegalArgumentException("Chưa chọn sản phẩm.");
        String sku = maSKU.trim();
        SanPham sp = timTheoMa(sku);
        if (sp == null) throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
        if (sp.isTrangThai()) {
            throw new IllegalArgumentException("Hãy chuyển sang trạng thái 'Ngừng kinh doanh' trước khi xóa.");
        }

        try (Connection conn = ConnectDB.openConnection()) {
            // Kiểm tra ràng buộc khóa ngoại ChiTietHoaDon
            try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM ChiTietHoaDon WHERE maSKU = ?")) {
                check.setString(1, sku);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalArgumentException("Sản phẩm đã có trong " + rs.getInt(1) +
                                " hóa đơn bán hàng. Không thể xóa để đảm bảo toàn vẹn dữ liệu, hãy để trạng thái 'Ngừng kinh doanh'.");
                    }
                }
            }

            // Kiểm tra ràng buộc phiếu bảo hành
            try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM PhieuBaoHanh WHERE maSKU = ?")) {
                check.setString(1, sku);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalArgumentException("Sản phẩm đã có " + rs.getInt(1) +
                                " phiếu bảo hành liên kết. Hãy giữ lại hồ sơ bảo hành.");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE maSKU = ?")) {
                ps.setString(1, sku);
                ps.executeUpdate();
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

        if (sp.getMaVach() != null) {
            sp.setMaVach(sp.getMaVach().trim());
        }

        if (sp.getNhomSP() == null || sp.getNhomSP().trim().isEmpty()) {
            sp.setNhomSP("Gọn nhẹ");
        } else if (!"Gọn nhẹ".equals(sp.getNhomSP()) && !"Cồng kềnh".equals(sp.getNhomSP())) {
            throw new IllegalArgumentException("Nhóm sản phẩm phải là 'Gọn nhẹ' hoặc 'Cồng kềnh'.");
        }

        if (!sku.matches("[A-Z0-9][A-Z0-9._-]{1,19}")) {
            throw new IllegalArgumentException("Mã SKU phải có 2–20 ký tự gồm chữ cái in hoa, chữ số, dấu chấm, gạch ngang hoặc gạch dưới.");
        }
        if (sp.getTenSP().length() < 3 || sp.getTenSP().length() > 150) {
            throw new IllegalArgumentException("Tên sản phẩm phải có từ 3 đến 150 ký tự.");
        }
        if (sp.getThuongHieu().isEmpty() || sp.getThuongHieu().length() > 100) {
            throw new IllegalArgumentException("Thương hiệu phải có từ 1 đến 100 ký tự.");
        }
        if (sp.getDanhMuc() == null || sp.getDanhMuc().getMaDM() == null || sp.getDanhMuc().getMaDM().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục cho sản phẩm.");
        }
        if (sp.getMaKM() != null) {
            sp.setMaKM(sp.getMaKM().trim());
            if (sp.getMaKM().isEmpty()) sp.setMaKM(null);
        }
        if (sp.getMaVach() != null && sp.getMaVach().length() > 50) {
            throw new IllegalArgumentException("Mã vạch tối đa 50 ký tự.");
        }
        if (sp.getModel().length() > 100) {
            throw new IllegalArgumentException("Model tối đa 100 ký tự.");
        }

        BigDecimal gia = sp.getDonGia();
        if (gia == null || gia.compareTo(BigDecimal.ZERO) <= 0 || gia.compareTo(new BigDecimal("999999999999999999")) > 0) {
            throw new IllegalArgumentException("Đơn giá bán phải lớn hơn 0 và tối đa 999.999.999.999.999.999 đ.");
        }
        sp.setDonGia(gia.setScale(0, BigDecimal.ROUND_HALF_UP));

        if (sp.getThoiGianBaoHanh() < 0 || sp.getThoiGianBaoHanh() > 120) {
            throw new IllegalArgumentException("Thời gian bảo hành phải từ 0 đến 120 tháng.");
        }
        if (sp.getSlTon() < 0) throw new IllegalArgumentException("Tồn kho không được âm.");
        if (sp.getMoTa().length() > 500) {
            throw new IllegalArgumentException("Mô tả sản phẩm tối đa 500 ký tự.");
        }
    }

    private String donGianHoa(String val) {
        return val == null ? "" : val.trim().replaceAll("\\s+", " ");
    }

    /** Lấy danh sách tất cả các thương hiệu hiện có */
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

    /** Lấy danh sách các mức thời gian bảo hành */
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

    /** Lấy toàn bộ danh mục từ CSDL */
    public ArrayList<DanhMuc> layDanhSachDanhMuc() {
        ArrayList<DanhMuc> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maDanhMuc, tenDanhMuc, moTa FROM DanhMuc ORDER BY maDanhMuc");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new DanhMuc(rs.getString("maDanhMuc"), rs.getString("tenDanhMuc"), rs.getString("moTa")));
            }
            return ds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách danh mục.", e);
        }
    }

    public String layTenDanhMuc(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) return "";
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT tenDanhMuc FROM DanhMuc WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đọc tên danh mục sản phẩm.", e);
        }
    }

    public int demSanPhamTheoDanhMuc(String maDanhMuc) {
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM SanPham WHERE maDanhMuc = ?")) {
            ps.setString(1, maDanhMuc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm sản phẩm trong danh mục.", e);
        }
    }

    public DanhMuc themDanhMuc(DanhMuc input) {
        DanhMuc dm = kiemTraDanhMuc(input);
        try (Connection conn = ConnectDB.openConnection()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM DanhMuc WHERE LOWER(tenDanhMuc) = LOWER(?)")) {
                check.setString(1, dm.getTenDM());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
                }
            }
            dm.setMaDM(taoMaDanhMuc(conn));
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO DanhMuc (maDanhMuc, tenDanhMuc, moTa) VALUES (?, ?, ?)")) {
                ps.setString(1, dm.getMaDM());
                ps.setString(2, dm.getTenDM());
                ps.setString(3, dm.getMoTa().isEmpty() ? null : dm.getMoTa());
                ps.executeUpdate();
            }
            return dm;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm danh mục sản phẩm.", e);
        }
    }

    public DanhMuc capNhatDanhMuc(DanhMuc input) {
        DanhMuc dm = kiemTraDanhMuc(input);
        if (dm.getMaDM() == null || dm.getMaDM().isBlank())
            throw new IllegalArgumentException("Mã danh mục không được để trống.");
        try (Connection conn = ConnectDB.openConnection()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM DanhMuc WHERE LOWER(tenDanhMuc) = LOWER(?) AND maDanhMuc <> ?")) {
                check.setString(1, dm.getTenDM());
                check.setString(2, dm.getMaDM());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE DanhMuc SET tenDanhMuc = ?, moTa = ? WHERE maDanhMuc = ?")) {
                ps.setString(1, dm.getTenDM());
                ps.setString(2, dm.getMoTa().isEmpty() ? null : dm.getMoTa());
                ps.setString(3, dm.getMaDM());
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
            }
            return dm;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật danh mục sản phẩm.", e);
        }
    }

    public void xoaDanhMuc(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank()) throw new IllegalArgumentException("Chưa chọn danh mục.");
        if (demSanPhamTheoDanhMuc(maDanhMuc) > 0)
            throw new IllegalArgumentException("Không thể xóa danh mục đang được sản phẩm sử dụng.");
        try (Connection conn = ConnectDB.openConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM DanhMuc WHERE maDanhMuc = ?")) {
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa danh mục sản phẩm.", e);
        }
    }

    private DanhMuc kiemTraDanhMuc(DanhMuc input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập danh mục.");
        DanhMuc dm = new DanhMuc(input);
        dm.setTenDM(donGianHoa(dm.getTenDM()));
        dm.setMoTa(dm.getMoTa() == null ? "" : dm.getMoTa().trim());
        if (dm.getTenDM().length() < 2 || dm.getTenDM().length() > 100)
            throw new IllegalArgumentException("Tên danh mục phải có từ 2 đến 100 ký tự.");
        if (dm.getMoTa().length() > 255)
            throw new IllegalArgumentException("Mô tả danh mục tối đa 255 ký tự.");
        return dm;
    }

    private String taoMaDanhMuc(Connection conn) throws SQLException {
        int max = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT maDanhMuc FROM DanhMuc WHERE maDanhMuc LIKE 'DM%'" );
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                try { max = Math.max(max, Integer.parseInt(id.substring(2))); }
                catch (NumberFormatException ignored) { }
            }
        }
        String code = String.format(Locale.ROOT, "DM%02d", max + 1);
        if (code.length() > 10) throw new IllegalStateException("Đã hết mã danh mục có thể tạo.");
        return code;
    }

    /** Lấy toàn bộ khuyến mãi từ CSDL */
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

    /**
     * Tìm kiếm và lọc sản phẩm kết hợp nhiều điều kiện
     */
    public ArrayList<SanPham> timKiem(String tuKhoa, String danhMucId, String thuongHieu,
            int khoangGia, Integer baoHanh, Boolean dangKinhDoanh) {
        ArrayList<SanPham> tatCa = layTatCa();
        ArrayList<SanPham> ketQua = new ArrayList<>();
        String query = chuanHoaTimKiem(tuKhoa);

        for (SanPham sp : tatCa) {
            if (danhMucId != null && !danhMucId.trim().isEmpty() && !danhMucId.equals(sp.getDanhMucId())) {
                continue;
            }
            if (thuongHieu != null && !thuongHieu.trim().isEmpty() &&
                    !chuanHoaTimKiem(thuongHieu).equals(chuanHoaTimKiem(sp.getThuongHieu()))) {
                continue;
            }
            if (baoHanh != null && baoHanh != sp.getThoiGianBaoHanh()) {
                continue;
            }
            if (dangKinhDoanh != null && dangKinhDoanh != sp.isTrangThai()) {
                continue;
            }

            BigDecimal gia = sp.getDonGia();
            int soVoi5 = gia.compareTo(new BigDecimal("5000000"));
            int soVoi15 = gia.compareTo(new BigDecimal("15000000"));
            // 0: Tất cả, 1: Dưới 5 triệu, 2: 5 - 15 triệu, 3: Trên 15 triệu
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

    /** Sắp xếp danh sách sản phẩm */
    public void sapXep(ArrayList<SanPham> danhSach, final int kieu) {
        // 0: tên A-Z, 1: tên Z-A, 2: giá tăng, 3: giá giảm, 4: mã SKU A-Z
        Collections.sort(danhSach, new Comparator<SanPham>() {
            @Override
            public int compare(SanPham a, SanPham b) {
                int c;
                if (kieu == 2) {
                    c = a.getDonGia().compareTo(b.getDonGia());
                } else if (kieu == 3) {
                    c = b.getDonGia().compareTo(a.getDonGia());
                } else if (kieu == 4) {
                    c = a.getMaSKU().compareTo(b.getMaSKU());
                } else {
                    c = chuanHoaTimKiem(a.getTenSP()).compareTo(chuanHoaTimKiem(b.getTenSP()));
                    if (kieu == 1) c = -c;
                }
                if (c == 0) c = a.getMaSKU().compareTo(b.getMaSKU());
                return c;
            }
        });
    }

    /** Chuẩn hóa xâu tìm kiếm không dấu */
    public static String chuanHoaTimKiem(String value) {
        if (value == null) return "";
        String k = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return k.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

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
