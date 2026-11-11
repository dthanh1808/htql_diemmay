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

	private static final String SELECT_COLUMNS = "maKM, tenKM, loaiKM, giaTriGiam, thoiGianBatDau, "
			+ "thoiGianKetThuc, trangThai, phamViApDung, doiTuongApDung";

	// Chuyển 1 dòng ResultSet sang entity
	private KhuyenMai mapKhuyenMai(ResultSet rs) throws SQLException {
		return new KhuyenMai(rs.getString("maKM"), rs.getString("tenKM"), rs.getString("loaiKM"),
				rs.getDouble("giaTriGiam"), rs.getTimestamp("thoiGianBatDau"), rs.getTimestamp("thoiGianKetThuc"),
				rs.getString("trangThai"), rs.getString("phamViApDung"), rs.getString("doiTuongApDung"));
	}

	// Lấy toàn bộ khuyến mãi
	public ArrayList<KhuyenMai> getalltblKhuyenMai() {
		ArrayList<KhuyenMai> ds = new ArrayList<>();

		String sql = "SELECT " + SELECT_COLUMNS + " FROM KhuyenMai ORDER BY maKM";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			System.err.println("Chưa kết nối database!");
			return ds;
		}

		try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				try {
					ds.add(mapKhuyenMai(rs));
				} catch (IllegalArgumentException ex) {
					// Dòng dữ liệu sai quy tắc của entity: bỏ qua, không làm hỏng cả danh sách
					System.err.println(
							"Bỏ qua khuyến mãi lỗi dữ liệu [" + rs.getString("maKM") + "]: " + ex.getMessage());
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ds;
	}

	// Tìm khuyến mãi theo mã, không thấy trả về null
	public KhuyenMai timTheoMa(String maKM) {
		String sql = "SELECT " + SELECT_COLUMNS + " FROM KhuyenMai WHERE maKM = ?";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			return null;
		}

		try (PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maKM);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapKhuyenMai(rs);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	// Kiểm tra mã khuyến mãi đã tồn tại
	public boolean kiemTraMaKM(String maKM) {
		String sql = "SELECT COUNT(*) FROM KhuyenMai WHERE maKM = ?";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			return false;
		}

		try (PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maKM);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	// Thêm khuyến mãi
	public boolean create(KhuyenMai km) {
		if (km.getThoigianBatDau() == null || km.getThoigianKetThuc() == null) {
			System.err.println("Thời gian khuyến mãi không được để trống!");
			return false;
		}

		String sql = "INSERT INTO KhuyenMai (" + SELECT_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			return false;
		}

		try (PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, km.getMaKM());
			ps.setNString(2, km.getTenKM());
			ps.setNString(3, km.getLoaiKM());
			ps.setDouble(4, km.getGiaTriGiam());
			ps.setTimestamp(5, new Timestamp(km.getThoigianBatDau().getTime()));
			ps.setTimestamp(6, new Timestamp(km.getThoigianKetThuc().getTime()));
			ps.setNString(7, km.getTrangThai());
			ps.setNString(8, km.getPhamViApDung());
			ps.setNString(9, km.getDoiTuongApDung());

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Cập nhật khuyến mãi (không đổi mã)
	public boolean update(KhuyenMai km) {
		if (km.getThoigianBatDau() == null || km.getThoigianKetThuc() == null) {
			System.err.println("Thời gian khuyến mãi không được để trống!");
			return false;
		}

		String sql = "UPDATE KhuyenMai SET tenKM = ?, loaiKM = ?, giaTriGiam = ?, thoiGianBatDau = ?, "
				+ "thoiGianKetThuc = ?, trangThai = ?, phamViApDung = ?, doiTuongApDung = ? WHERE maKM = ?";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			return false;
		}

		try (PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setNString(1, km.getTenKM());
			ps.setNString(2, km.getLoaiKM());
			ps.setDouble(3, km.getGiaTriGiam());
			ps.setTimestamp(4, new Timestamp(km.getThoigianBatDau().getTime()));
			ps.setTimestamp(5, new Timestamp(km.getThoigianKetThuc().getTime()));
			ps.setNString(6, km.getTrangThai());
			ps.setNString(7, km.getPhamViApDung());
			ps.setNString(8, km.getDoiTuongApDung());
			ps.setString(9, km.getMaKM());

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// "Xóa" mềm: chuyển trạng thái sang "Đã hủy", không xóa dòng (tránh lỗi khóa
	// ngoại với hóa đơn)
	public boolean delete(String maKM) {
		String sql = "UPDATE KhuyenMai SET trangThai = N'Đã hủy' WHERE maKM = ?";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			return false;
		}

		try (PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maKM);
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// "Đang kích hoạt" -> "Đã kết thúc" cho khuyến mãi quá hạn. Trả về số dòng cập
	// nhật.
	public int capNhatKhuyenMaiHetHan() {
		String sql = "UPDATE KhuyenMai SET trangThai = N'Đã kết thúc' "
				+ "WHERE trangThai = N'Đang kích hoạt' AND thoiGianKetThuc < GETDATE()";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			return 0;
		}

		try (Statement st = con.createStatement()) {
			return st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	// Sinh mã tiếp theo: KM001 -> KM002 ... Lỗi thì trả về null
	public String taoMaMoi() {
		String sql = "SELECT MAX(CAST(SUBSTRING(maKM, 3, 10) AS INT)) FROM KhuyenMai WHERE maKM LIKE 'KM[0-9]%'";

		Connection con = ConnectDB.getConnection();
		if (con == null) {
			return null;
		}

		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
			int max = rs.next() ? rs.getInt(1) : 0;
			return String.format("KM%03d", max + 1);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}