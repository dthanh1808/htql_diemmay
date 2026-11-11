package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import connect.ConnectDB;
import entity.NhanVien;

public class NhanVien_DAO {

	private static final String SELECT_COLUMNS = "maNV, hoTen, gioiTinh, sdt, email, ngaySinh, "
			+ "diaChi, chucVu, trangThai, ngayVaoLam";

	// Chuyển dữ liệu từ ResultSet sang entity
	private NhanVien mapNhanVien(ResultSet rs) throws SQLException {
		NhanVien nv = new NhanVien();

		nv.setMaNV(rs.getString("maNV"));
		nv.setHoTen(rs.getString("hoTen"));
		nv.setGioiTinh(rs.getBoolean("gioiTinh"));
		nv.setSdt(rs.getString("sdt"));
		nv.setEmail(rs.getString("email"));

		Date ngaySinh = rs.getDate("ngaySinh");
		nv.setNgaySinh(ngaySinh != null ? ngaySinh.toLocalDate() : null);

		nv.setDiachi(rs.getString("diaChi"));
		nv.setChucVu(rs.getString("chucVu"));
		nv.setTrangThai(rs.getBoolean("trangThai"));
		nv.setNgayVaoLam(rs.getDate("ngayVaoLam"));

		// Bảng hiện tại chưa có cột anhDaiDien
		nv.setAnhDaiDien(null);

		return nv;
	}

	private Date toSqlDate(java.util.Date date) {
		return date == null ? null : new Date(date.getTime());
	}

	private void setDateOrNull(PreparedStatement ps, int index, Date date) throws SQLException {

		if (date == null) {
			ps.setNull(index, java.sql.Types.DATE);
		} else {
			ps.setDate(index, date);
		}
	}

	// Lấy toàn bộ nhân viên
	public ArrayList<NhanVien> getalltblNhanVien() {
		ArrayList<NhanVien> dsNV = new ArrayList<>();

		String sql = "SELECT " + SELECT_COLUMNS + " FROM NhanVien";

		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				dsNV.add(mapNhanVien(rs));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNV;
	}

	// Tìm nhân viên theo mã
	public ArrayList<NhanVien> getNhanVienTheoMa(String maNV) {
		ArrayList<NhanVien> dsNV = new ArrayList<>();

		String sql = "SELECT " + SELECT_COLUMNS + " FROM NhanVien WHERE maNV = ?";

		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maNV);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					dsNV.add(mapNhanVien(rs));
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNV;
	}

	// Kiểm tra mã nhân viên đã tồn tại
	public boolean kiemTraMaNV(String maNV) {
		String sql = "SELECT COUNT(*) FROM NhanVien WHERE maNV = ?";

		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maNV);

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

	// Thêm nhân viên
	public boolean create(NhanVien nv) {
		String sql = "INSERT INTO NhanVien " + "(maNV, hoTen, gioiTinh, sdt, email, ngaySinh, "
				+ "diaChi, chucVu, trangThai, ngayVaoLam) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, nv.getMaNV());
			ps.setString(2, nv.getHoTen());
			ps.setBoolean(3, nv.isGioiTinh());
			ps.setString(4, nv.getSdt());
			ps.setString(5, nv.getEmail());

			if (nv.getNgaySinh() != null) {
				ps.setDate(6, Date.valueOf(nv.getNgaySinh()));
			} else {
				ps.setNull(6, java.sql.Types.DATE);
			}

			ps.setString(7, nv.getDiachi());
			ps.setString(8, nv.getChucVu());
			ps.setBoolean(9, nv.isTrangThai());
			ps.setDate(10, toSqlDate(nv.getNgayVaoLam()));

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Cập nhật nhân viên, cho phép sửa cả mã nhân viên
	public boolean update(NhanVien nv, String maNVCu) {
		String sql = "UPDATE NhanVien SET " + "maNV = ?, hoTen = ?, gioiTinh = ?, sdt = ?, "
				+ "email = ?, ngaySinh = ?, diaChi = ?, chucVu = ?, " + "trangThai = ?, ngayVaoLam = ? "
				+ "WHERE maNV = ?";

		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, nv.getMaNV());
			ps.setString(2, nv.getHoTen());
			ps.setBoolean(3, nv.isGioiTinh());
			ps.setString(4, nv.getSdt());
			ps.setString(5, nv.getEmail());

			if (nv.getNgaySinh() != null) {
				ps.setDate(6, Date.valueOf(nv.getNgaySinh()));
			} else {
				ps.setNull(6, java.sql.Types.DATE);
			}

			ps.setString(7, nv.getDiachi());
			ps.setString(8, nv.getChucVu());
			ps.setBoolean(9, nv.isTrangThai());
			ps.setDate(10, toSqlDate(nv.getNgayVaoLam()));
			ps.setString(11, maNVCu);

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Chuyển trạng thái nhân viên sang nghỉ việc
	// Không xóa vĩnh viễn dữ liệu
	public boolean capNhatTrangThaiNghiViec(String maNV) {
		String sql = "UPDATE NhanVien SET trangThai = 0 WHERE maNV = ?";

		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, maNV);
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}