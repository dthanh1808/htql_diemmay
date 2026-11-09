package dao;

import connect.ConnectDB;
import entity.DanhMuc;
import java.sql.*;
import java.util.ArrayList;

/** Danh mục được đọc/ghi trực tiếp trong SQL Server. */
public class DanhMucDAO {
	public DanhMucDAO() {
	}
	private DanhMuc docDong(ResultSet r) throws SQLException {
		return new DanhMuc(r.getString("maDanhMuc"), r.getString("tenDanhMuc"), r.getString("moTa"));
	}
	public ArrayList<DanhMuc> layTatCa() {
		ArrayList<DanhMuc> ds = new ArrayList<>();
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c.prepareStatement("SELECT * FROM dbo.DanhMuc ORDER BY tenDanhMuc");
				ResultSet r = p.executeQuery()) {
			while (r.next())
				ds.add(docDong(r));
			return ds;
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}
	public DanhMuc timTheoMa(String id) {
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c.prepareStatement("SELECT * FROM dbo.DanhMuc WHERE maDanhMuc=?")) {
			p.setString(1, id);
			try (ResultSet r = p.executeQuery()) {
				return r.next() ? docDong(r) : null;
			}
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}
	public String layTen(String id) {
		DanhMuc dm = timTheoMa(id);
		return dm == null ? "" : dm.getTen();
	}
	public int demSanPham(String id) {
		try (Connection c = ConnectDB.openConnection();
				PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM dbo.SanPham WHERE maDanhMuc=?")) {
			p.setString(1, id);
			try (ResultSet r = p.executeQuery()) {
				r.next();
				return r.getInt(1);
			}
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}
	private DanhMuc kiemTra(Connection c, DanhMuc input) throws SQLException {
		if (input == null)
			throw new IllegalArgumentException("Chưa nhập danh mục.");
		DanhMuc dm = new DanhMuc(input);
		dm.setTen(dm.getTen() == null ? "" : dm.getTen().trim().replaceAll("\\s+", " "));
		dm.setMoTa(dm.getMoTa() == null ? "" : dm.getMoTa().trim());
		if (dm.getTen().length() < 2 || dm.getTen().length() > 80)
			throw new IllegalArgumentException("Tên danh mục phải có từ 2 đến 80 ký tự.");
		if (dm.getMoTa().length() > 1000)
			throw new IllegalArgumentException("Mô tả tối đa 1.000 ký tự.");
		String sql = "SELECT 1 FROM dbo.DanhMuc WITH (UPDLOCK,HOLDLOCK) "
				+ "WHERE REPLACE(REPLACE(tenDanhMuc,N'đ',N'd'),N'Đ',N'D') COLLATE Latin1_General_100_CI_AI=? AND maDanhMuc<>?";
		try (PreparedStatement p = c.prepareStatement(sql)) {
			p.setNString(1, SanPhamDAO.chuanHoaTimKiem(dm.getTen()));
			p.setString(2, dm.getId() == null ? "" : dm.getId());
			try (ResultSet r = p.executeQuery()) {
				if (r.next())
					throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
			}
		}
		return dm;
	}
	public DanhMuc them(DanhMuc input) {
		if (input == null)
			throw new IllegalArgumentException("Chưa nhập danh mục.");
		try (Connection c = ConnectDB.openConnection()) {
			c.setAutoCommit(false);
			try {
				DanhMuc dm = new DanhMuc(input);
				dm.setId(null);
				dm = kiemTra(c, dm);
				dm.setId(
						JdbcHelper.taoMa(c,
								"SELECT COALESCE(MAX(TRY_CONVERT(BIGINT,SUBSTRING(maDanhMuc,3,8))),0) "
										+ "FROM dbo.DanhMuc WITH (UPDLOCK,HOLDLOCK) WHERE maDanhMuc LIKE 'DM%'",
								"DM", 6));
				try (PreparedStatement p = c
						.prepareStatement("INSERT INTO dbo.DanhMuc(maDanhMuc,tenDanhMuc,moTa) VALUES (?,?,?)")) {
					p.setString(1, dm.getId());
					p.setNString(2, dm.getTen());
					p.setNString(3, dm.getMoTa());
					p.executeUpdate();
				}
				c.commit();
				return dm;
			} catch (SQLException | RuntimeException e) {
				JdbcHelper.rollback(c, e);
				throw e;
			}
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}
	public DanhMuc capNhat(DanhMuc input) {
		try (Connection c = ConnectDB.openConnection()) {
			c.setAutoCommit(false);
			try {
				DanhMuc dm = kiemTra(c, input);
				try (PreparedStatement p = c
						.prepareStatement("UPDATE dbo.DanhMuc SET tenDanhMuc=?,moTa=? WHERE maDanhMuc=?")) {
					p.setNString(1, dm.getTen());
					p.setNString(2, dm.getMoTa());
					p.setString(3, dm.getId());
					if (p.executeUpdate() != 1)
						throw new IllegalArgumentException("Danh mục không còn tồn tại.");
				}
				c.commit();
				return dm;
			} catch (SQLException | RuntimeException e) {
				JdbcHelper.rollback(c, e);
				throw e;
			}
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}
	public void xoa(String id) {
		String sql = "DELETE FROM dbo.DanhMuc WHERE maDanhMuc=? "
				+ "AND NOT EXISTS(SELECT 1 FROM dbo.SanPham WHERE maDanhMuc=?)";
		try (Connection c = ConnectDB.openConnection(); PreparedStatement p = c.prepareStatement(sql)) {
			p.setString(1, id);
			p.setString(2, id);
			if (p.executeUpdate() != 1)
				throw new IllegalArgumentException(
						"Danh mục không tồn tại hoặc đang có sản phẩm (kể cả đã ngừng kinh doanh).");
		} catch (SQLException e) {
			throw JdbcHelper.loi(e);
		}
	}
}
