package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import connect.ConnectDB;
import entity.KhuyenMai;

public class KhuyenMai_DAO {

	public ArrayList<KhuyenMai> getalltblKhuyenMai() throws SQLException {
		ArrayList<KhuyenMai> ds = new ArrayList<>();
		Connection con = ConnectDB.getConnection();
		if (con == null) {
			throw new SQLException("Chưa kết nối database!");
		}
		String sql = "SELECT * FROM KhuyenMai ORDER BY maKM";
		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) {
				ds.add(new KhuyenMai(rs.getString("maKM"), rs.getString("tenKM"), rs.getString("loaiKM"),
						rs.getDouble("giaTriGiam"), rs.getTimestamp("thoiGianBatDau"),
						rs.getTimestamp("thoiGianKetThuc"), rs.getString("trangThai"), rs.getString("phamViApDung"),
						rs.getString("doiTuongApDung")));
			}
		}
		return ds;
	}

	public boolean create(KhuyenMai km) throws SQLException {
		Connection con = ConnectDB.getConnection();

		if (con == null) {
			throw new SQLException("Chưa kết nối database!");
		}

		if (km.getThoigianBatDau() == null || km.getThoigianKetThuc() == null) {
			throw new SQLException("Thời gian khuyến mãi không được để trống!");
		}

		String sql = "INSERT INTO KhuyenMai " + "(maKM, tenKM, loaiKM, giaTriGiam, thoiGianBatDau, "
				+ "thoiGianKetThuc, trangThai, phamViApDung, doiTuongApDung) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, km.getMaKM());
			stmt.setNString(2, km.getTenKM());
			stmt.setNString(3, km.getLoaiKM());
			stmt.setDouble(4, km.getGiaTriGiam());
			stmt.setTimestamp(5, new Timestamp(km.getThoigianBatDau().getTime()));
			stmt.setTimestamp(6, new Timestamp(km.getThoigianKetThuc().getTime()));
			stmt.setNString(7, km.getTrangThai());
			stmt.setNString(8, km.getPhamViApDung());
			stmt.setNString(9, km.getDoiTuongApDung());

			return stmt.executeUpdate() > 0;
		}
	}

	public boolean update(KhuyenMai km) throws SQLException {
		Connection con = ConnectDB.getConnection();
		String sql = "UPDATE KhuyenMai SET tenKM=?, loaiKM=?, giaTriGiam=?, thoiGianBatDau=?, "
				+ "thoiGianKetThuc=?, trangThai=?, phamViApDung=?, doiTuongApDung=? WHERE maKM=?";
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setNString(1, km.getTenKM());
			stmt.setNString(2, km.getLoaiKM());
			stmt.setDouble(3, km.getGiaTriGiam());
			stmt.setTimestamp(4, new Timestamp(km.getThoigianBatDau().getTime()));
			stmt.setTimestamp(5, new Timestamp(km.getThoigianKetThuc().getTime()));
			stmt.setNString(6, km.getTrangThai());
			stmt.setNString(7, km.getPhamViApDung());
			stmt.setNString(8, km.getDoiTuongApDung());
			stmt.setString(9, km.getMaKM());
			return stmt.executeUpdate() > 0;
		}
	}

	// Sinh mã tiếp theo: KM001 -> KM005 ...
	public String taoMaMoi() throws SQLException {
		Connection con = ConnectDB.getConnection();
		String sql = "SELECT MAX(CAST(SUBSTRING(maKM, 3, 10) AS INT)) FROM KhuyenMai WHERE maKM LIKE 'KM[0-9]%'";
		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
			int max = rs.next() ? rs.getInt(1) : 0;
			return String.format("KM%03d", max + 1);
		}
	}
}