package dao;

import connect.ConnectDB;
import entity.DanhMuc;
import entity.SanPham;
import java.sql.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** JDBC cơ bản: mở kết nối, gán tham số, đọc ResultSet và đóng tài nguyên. */
public class SanPhamDAO {
	private static final String SELECT = "SELECT s.*,d.tenDanhMuc,d.moTa AS moTaDanhMuc "
			+ "FROM dbo.SanPham s JOIN dbo.DanhMuc d ON d.maDanhMuc=s.maDanhMuc ";
	public SanPhamDAO() {
	}

	private SanPham docDong(ResultSet r) throws SQLException {
		String anh = r.getString("anhBase64");
		String moTa = r.getString("moTa");
		SanPham sp = new SanPham(r.getString("maNoiBo"), r.getString("maSKU"), r.getString("tenSP"),
				r.getString("maDanhMuc"), r.getString("thuongHieu"), r.getBigDecimal("donGia"),
				r.getInt("thoiGianBaoHanh"), r.getBoolean("trangThai"), moTa == null ? "" : moTa,
				anh == null ? "" : anh, r.getTimestamp("taoLuc").toLocalDateTime(),
				r.getTimestamp("capNhatLuc").toLocalDateTime());
		sp.setMaVach(r.getString("maVach"));
		sp.setModel(r.getString("model"));
		sp.setNhomSP(r.getString("nhomSanPham"));
		sp.setMaKM(r.getString("maKM"));
		sp.setSlTon(r.getInt("slTon"));
		sp.setDanhMuc(new DanhMuc(sp.getDanhMucId(), r.getString("tenDanhMuc"), r.getString("moTaDanhMuc")));
		return sp;
	}

	public ArrayList<SanPham> layTatCa() {
		ArrayList<SanPham> ds = new ArrayList<>();
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c.prepareStatement(SELECT + "ORDER BY s.tenSP");
				ResultSet r = p.executeQuery()) {
			while (r.next())
				ds.add(docDong(r));
			return ds;
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	public int demTatCa() {
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM dbo.SanPham");
				ResultSet r = p.executeQuery()) {
			r.next();
			return r.getInt(1);
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	private SanPham timTheoMa(Connection c, String id) throws SQLException {
		try (PreparedStatement p = c.prepareStatement(SELECT + "WHERE s.maNoiBo=?")) {
			p.setString(1, id);
			try (ResultSet r = p.executeQuery()) {
				return r.next() ? docDong(r) : null;
			}
		}
	}

	public SanPham timTheoMa(String id) {
		try (Connection c = ConnectDB.openConnection()) {
			return timTheoMa(c, id);
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	private String chuoi(String value) {
		return value == null ? "" : value.trim().replaceAll("\\s+", " ");
	}

	private void kiemTra(Connection c, SanPham sp) throws SQLException {
		sp.setSku(chuoi(sp.getSku()).toUpperCase(Locale.ROOT));
		sp.setTen(chuoi(sp.getTen()));
		sp.setThuongHieu(chuoi(sp.getThuongHieu()));
		sp.setMoTa(sp.getMoTa() == null ? "" : sp.getMoTa().trim());
		sp.setAnhBase64(sp.getAnhBase64() == null ? "" : sp.getAnhBase64());
		if (!sp.getSku().matches("[A-Z0-9][A-Z0-9._-]{1,19}"))
			throw new IllegalArgumentException(
					"SKU gồm 2–20 chữ cái không dấu, chữ số, dấu chấm, gạch ngang hoặc gạch dưới.");
		if (sp.getTen().length() < 3 || sp.getTen().length() > 150)
			throw new IllegalArgumentException("Tên sản phẩm phải có từ 3 đến 150 ký tự.");
		if (sp.getThuongHieu().isEmpty() || sp.getThuongHieu().length() > 80)
			throw new IllegalArgumentException("Thương hiệu phải có từ 1 đến 80 ký tự.");
		BigDecimal gia = sp.getGiaBan();
		if (gia == null || gia.signum() <= 0 || gia.compareTo(new BigDecimal("999999999999")) > 0
				|| gia.stripTrailingZeros().scale() > 0)
			throw new IllegalArgumentException("Giá bán phải là số nguyên VND từ 1 đến 999.999.999.999.");
		sp.setGiaBan(gia.setScale(0));
		if (sp.getBaoHanhThang() < 0 || sp.getBaoHanhThang() > 120)
			throw new IllegalArgumentException("Bảo hành phải từ 0 đến 120 tháng.");
		if (sp.getMoTa().length() > 3000)
			throw new IllegalArgumentException("Mô tả tối đa 3.000 ký tự.");
		sp.setMaVach(chuoi(sp.getMaVach()));
		sp.setModel(chuoi(sp.getModel()));
		sp.setNhomSP(chuoi(sp.getNhomSP()));
		sp.setMaKM(chuoi(sp.getMaKM()));
		if (sp.getMaVach().length() > 50 || sp.getModel().length() > 100)
			throw new IllegalArgumentException("Mã vạch tối đa 50 ký tự, model tối đa 100 ký tự.");
		if (!sp.getNhomSP().isEmpty() && !sp.getNhomSP().equals("Gọn nhẹ") && !sp.getNhomSP().equals("Cồng kềnh"))
			throw new IllegalArgumentException("Nhóm sản phẩm phải là Gọn nhẹ hoặc Cồng kềnh.");
		if (sp.getSlTon() < 0)
			throw new IllegalArgumentException("Tồn kho không được âm.");
		try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM dbo.DanhMuc WHERE maDanhMuc=?")) {
			p.setString(1, sp.getDanhMucId());
			try (ResultSet r = p.executeQuery()) {
				if (!r.next())
					throw new IllegalArgumentException("Hãy chọn một danh mục hiện có.");
			}
		}
		try (PreparedStatement p = c.prepareStatement(
				"SELECT 1 FROM dbo.SanPham WITH (UPDLOCK,HOLDLOCK) " + "WHERE UPPER(maSKU)=? AND maNoiBo<>?")) {
			p.setString(1, sp.getSku());
			p.setString(2, sp.getId() == null ? "" : sp.getId());
			try (ResultSet r = p.executeQuery()) {
				if (r.next())
					throw new IllegalArgumentException("SKU đã tồn tại, kể cả sản phẩm ngừng kinh doanh.");
			}
		}
		kiemTraAnh(sp.getAnhBase64());
	}

	private void ganDuLieu(PreparedStatement p, SanPham sp) throws SQLException {
		p.setString(1, sp.getSku());
		p.setNString(2, sp.getTen());
		p.setString(3, sp.getDanhMucId());
		p.setNString(4, sp.getThuongHieu());
		p.setBigDecimal(5, sp.getGiaBan());
		p.setInt(6, sp.getBaoHanhThang());
		p.setBoolean(7, sp.isDangKinhDoanh());
		p.setNString(8, sp.getMoTa());
		p.setString(9, sp.getAnhBase64());
	}

	private void ganThongTinBoSung(PreparedStatement p, SanPham sp) throws SQLException {
		p.setString(11, sp.getMaVach().isEmpty() ? null : sp.getMaVach());
		p.setNString(12, sp.getModel());
		p.setNString(13, sp.getNhomSP().isEmpty() ? null : sp.getNhomSP());
		p.setString(14, sp.getMaKM().isEmpty() ? null : sp.getMaKM());
	}

	public SanPham them(SanPham input) {
		if (input == null)
			throw new IllegalArgumentException("Chưa nhập sản phẩm.");
		try (Connection c = ConnectDB.openConnection()) {
			c.setAutoCommit(false);
			try {
				SanPham sp = new SanPham(input);
				sp.setId(null);
				kiemTra(c, sp);
				sp.setId(
						JdbcHelper.taoMa(c,
								"SELECT COALESCE(MAX(TRY_CONVERT(BIGINT,SUBSTRING(maNoiBo,3,8))),0) "
										+ "FROM dbo.SanPham WITH (UPDLOCK,HOLDLOCK) WHERE maNoiBo LIKE 'SP%'",
								"SP", 6));
				String sql = "INSERT INTO dbo.SanPham(maSKU,tenSP,maDanhMuc,thuongHieu,donGia,thoiGianBaoHanh,"
						+ "trangThai,moTa,anhBase64,maNoiBo,maVach,model,nhomSanPham,maKM,slTon) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				try (PreparedStatement p = c.prepareStatement(sql)) {
					ganDuLieu(p, sp);
					p.setString(10, sp.getId());
					ganThongTinBoSung(p, sp);
					p.setInt(15, sp.getSlTon());
					p.executeUpdate();
				}
				SanPham daLuu = timTheoMa(c, sp.getId());
				c.commit();
				return daLuu;
			} catch (SQLException | RuntimeException e) {
				JdbcHelper.rollback(c, e);
				throw e;
			}
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	public SanPham capNhat(SanPham input) {
		if (input == null)
			throw new IllegalArgumentException("Chưa nhập sản phẩm.");
		try (Connection c = ConnectDB.openConnection()) {
			c.setAutoCommit(false);
			try {
				SanPham sp = new SanPham(input);
				SanPham cu = timTheoMa(c, sp.getId());
				if (cu == null)
					throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
				kiemTra(c, sp);
				// SKU là khóa ngoại của hóa đơn. Giữ mã sau khi đã phát sinh nghiệp vụ.
				if (!cu.getSku().equals(sp.getSku()) && daDuocSuDung(c, cu.getSku()))
					throw new IllegalArgumentException("Sản phẩm đã có hóa đơn/bảo hành nên không được đổi SKU.");
				String sql = "UPDATE dbo.SanPham SET maSKU=?,tenSP=?,maDanhMuc=?,thuongHieu=?,donGia=?,"
						+ "thoiGianBaoHanh=?,trangThai=?,moTa=?,anhBase64=?,capNhatLuc=SYSDATETIME(),"
						+ "maVach=?,model=?,nhomSanPham=?,maKM=? WHERE maNoiBo=?";
				try (PreparedStatement p = c.prepareStatement(sql)) {
					ganDuLieu(p, sp);
					// Vị trí 10–13 là thông tin bổ sung; vị trí 14 là mã nội bộ trong WHERE.
					p.setString(10, sp.getMaVach().isEmpty() ? null : sp.getMaVach());
					p.setNString(11, sp.getModel());
					p.setNString(12, sp.getNhomSP().isEmpty() ? null : sp.getNhomSP());
					p.setString(13, sp.getMaKM().isEmpty() ? null : sp.getMaKM());
					p.setString(14, sp.getId());
					if (p.executeUpdate() != 1)
						throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
				}
				SanPham daLuu = timTheoMa(c, sp.getId());
				c.commit();
				return daLuu;
			} catch (SQLException | RuntimeException e) {
				JdbcHelper.rollback(c, e);
				throw e;
			}
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	private boolean daDuocSuDung(Connection c, String sku) throws SQLException {
		String sql = "SELECT 1 FROM dbo.ChiTietHoaDon WHERE maSKU=? UNION ALL "
				+ "SELECT 1 FROM dbo.PhieuBaoHanh WHERE maSKU=?";
		try (PreparedStatement p = c.prepareStatement(sql)) {
			p.setString(1, sku);
			p.setString(2, sku);
			try (ResultSet r = p.executeQuery()) {
				return r.next();
			}
		}
	}

	public void xoa(String id) {
		String sql = "DELETE FROM dbo.SanPham WHERE maNoiBo=? AND trangThai=0 "
				+ "AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietHoaDon d WHERE d.maSKU=SanPham.maSKU) "
				+ "AND NOT EXISTS(SELECT 1 FROM dbo.PhieuBaoHanh b WHERE b.maSKU=SanPham.maSKU)";
		try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(sql)) {
			p.setString(1, id);
			if (p.executeUpdate() != 1)
				throw new IllegalArgumentException("Chỉ xóa sản phẩm đã ngừng kinh doanh, chưa có hóa đơn/bảo hành.");
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	public ArrayList<String> layThuongHieu() {
		ArrayList<String> ds = new ArrayList<>();
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c
						.prepareStatement("SELECT DISTINCT thuongHieu FROM dbo.SanPham ORDER BY thuongHieu");
				ResultSet r = p.executeQuery()) {
			while (r.next())
				ds.add(r.getString(1));
			return ds;
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}
	public ArrayList<Integer> layBaoHanh() {
		ArrayList<Integer> ds = new ArrayList<>();
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c
						.prepareStatement("SELECT DISTINCT COALESCE(thoiGianBaoHanh,0) FROM dbo.SanPham ORDER BY 1");
				ResultSet r = p.executeQuery()) {
			while (r.next())
				ds.add(r.getInt(1));
			return ds;
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	public ArrayList<SanPham> timKiem(String tuKhoa, String danhMucId, String thuongHieu, int khoangGia,
			Integer baoHanh, Boolean dangKinhDoanh) {
		String sql = SELECT + "WHERE 1=1 ";
		ArrayList<Object> values = new ArrayList<>();
		if (danhMucId != null) {
			sql += "AND s.maDanhMuc=? ";
			values.add(danhMucId);
		}
		if (thuongHieu != null) {
			sql += "AND s.thuongHieu COLLATE Latin1_General_100_CI_AI=? ";
			values.add(thuongHieu);
		}
		if (baoHanh != null) {
			sql += "AND COALESCE(s.thoiGianBaoHanh,0)=? ";
			values.add(baoHanh);
		}
		if (dangKinhDoanh != null) {
			sql += "AND s.trangThai=? ";
			values.add(dangKinhDoanh);
		}
		if (khoangGia == 1)
			sql += "AND s.donGia<5000000 ";
		if (khoangGia == 2)
			sql += "AND s.donGia BETWEEN 5000000 AND 15000000 ";
		if (khoangGia == 3)
			sql += "AND s.donGia>15000000 ";
		String q = chuanHoaTimKiem(tuKhoa);
		if (!q.isEmpty())
			for (String token : q.split(" ")) {
				sql += "AND REPLACE(REPLACE(CONCAT(s.maSKU,N' ',s.maVach,N' ',s.tenSP,N' ',s.thuongHieu,N' ',d.tenDanhMuc),"
						+ "N'đ',N'd'),N'Đ',N'D') COLLATE Latin1_General_100_CI_AI LIKE ? ESCAPE '!' ";
				values.add("%" + thoatLike(token) + "%");
			}
		ArrayList<SanPham> ds = new ArrayList<>();
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c.prepareStatement(sql + "ORDER BY s.tenSP")) {
			for (int i = 0; i < values.size(); i++)
				p.setObject(i + 1, values.get(i));
			try (ResultSet r = p.executeQuery()) {
				while (r.next())
					ds.add(docDong(r));
			}
			return ds;
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}

	static String thoatLike(String value) {
		return value.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");
	}

	public void sapXep(ArrayList<SanPham> danhSach, final int kieu) {
		// 0: tên A-Z, 1: tên Z-A, 2: giá tăng, 3: giá giảm, 4: mới nhất.
		Collections.sort(danhSach, new Comparator<SanPham>() {
			@Override
			public int compare(SanPham a, SanPham b) {
				int ketQua;
				if (kieu == 2)
					ketQua = a.getGiaBan().compareTo(b.getGiaBan());
				else if (kieu == 3)
					ketQua = b.getGiaBan().compareTo(a.getGiaBan());
				else if (kieu == 4)
					ketQua = b.getTaoLuc().compareTo(a.getTaoLuc());
				else {
					ketQua = chuanHoaTimKiem(a.getTen()).compareTo(chuanHoaTimKiem(b.getTen()));
					if (kieu == 1)
						ketQua = -ketQua;
				}
				if (ketQua == 0)
					ketQua = a.getSku().compareTo(b.getSku());
				return ketQua;
			}
		});
	}

	public static String chuanHoaTimKiem(String value) {
		if (value == null)
			return "";
		String ketQua = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return ketQua.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
	}

	public static String docAnh(Path path) throws IOException {
		if (Files.size(path) > 2 * 1024 * 1024)
			throw new IllegalArgumentException("Ảnh không được vượt quá 2 MB.");
		String value = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
		kiemTraAnh(value);
		return value;
	}

	private static void kiemTraAnh(String value) {
		if (value.isEmpty())
			return;
		try {
			if (value.length() > 2_800_000)
				throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
			byte[] bytes = Base64.getDecoder().decode(value);
			if (bytes.length > 2 * 1024 * 1024)
				throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
			try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
				Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
				if (!readers.hasNext())
					throw new IllegalArgumentException("Hãy chọn ảnh PNG hoặc JPEG hợp lệ.");
				ImageReader reader = readers.next();
				try {
					reader.setInput(stream);
					String format = reader.getFormatName();
					if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg"))
						throw new IllegalArgumentException("Chỉ hỗ trợ PNG và JPEG.");
					if ((long) reader.getWidth(0) * reader.getHeight(0) > 16_000_000)
						throw new IllegalArgumentException("Ảnh vượt quá 16 triệu điểm ảnh.");
					if (reader.read(0) == null)
						throw new IllegalArgumentException("Không đọc được ảnh.");
				} finally {
					reader.dispose();
				}
			}
		} catch (IOException | IllegalArgumentException e) {
			throw new IllegalArgumentException("Ảnh không hợp lệ: " + e.getMessage(), e);
		}
	}
}
