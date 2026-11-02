package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import connect.ConnectDB;
import entity.NhanVien;

public class NhanVien_DAO {

	// ========================== ĐỌC DỮ LIỆU NHÂN VIÊN ==========================

	public ArrayList<NhanVien> getalltblNhanVien() {
		ArrayList<NhanVien> dsNhanVien = new ArrayList<>();

		Connection con = ConnectDB.getConnection();

		if (con == null) {
			System.out.println("Không thể lấy dữ liệu: Chưa kết nối database!");
			return dsNhanVien;
		}

		String sql = "SELECT * FROM NhanVien";

		try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {

			while (rs.next()) {
				NhanVien nv = new NhanVien();

				nv.setMaNV(rs.getString("maNV"));
				nv.setHoTen(rs.getString("hoTen"));
				nv.setGioiTinh(rs.getString("gioiTinh").equalsIgnoreCase("Nam"));

				java.sql.Date ngaySinh = rs.getDate("ngaySinh");
				nv.setNgaySinh(ngaySinh != null ? ngaySinh.toLocalDate() : null);

				nv.setSdt(rs.getString("sdt"));
				nv.setEmail(rs.getString("email"));
				nv.setChucVu(rs.getString("chucVu"));
				nv.setTrangThai(rs.getString("trangThai"));
				nv.setDiachi(rs.getString("diaChi"));
				nv.setNgayVaoLam(rs.getDate("ngayVaoLam"));
				nv.setAnhDaiDien(rs.getString("anhDaiDien"));

				dsNhanVien.add(nv);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNhanVien;
	}
	// ========================== TẠO NHÂN VIÊN MỚI ==========================

	public boolean create(NhanVien nv) {
		int n = 0;

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "INSERT INTO NhanVien " + "(maNV, hoTen, gioiTinh, ngaySinh, sdt, email, chucVu, "
					+ "trangThai, diaChi, ghiChu, anhDaiDien, ngayVaoLam) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, nv.getMaNV());
			stmt.setString(2, nv.getHoTen());
			stmt.setString(3, nv.isGioiTinh() ? "Nam" : "Nữ");
			stmt.setDate(4, Date.valueOf(nv.getNgaySinh()));
			stmt.setString(5, nv.getSdt());
			stmt.setString(6, nv.getEmail());
			stmt.setString(7, nv.getChucVu());
			stmt.setString(8, nv.getTrangThai());
			stmt.setString(9, nv.getDiachi());
			stmt.setString(10, nv.getGhiChu());
			stmt.setString(11, nv.getAnhDaiDien());
			stmt.setDate(12, new Date(nv.getNgayVaoLam().getTime()));

			n = stmt.executeUpdate();

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return n > 0;
	}

	// ========================== TÌM NHÂN VIÊN THEO MÃ ==========================

	public ArrayList<NhanVien> getNhanVienTheoMa(String maNV) {
		ArrayList<NhanVien> dsNhanVien = new ArrayList<NhanVien>();

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "SELECT * FROM NhanVien WHERE maNV = ?";
			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, maNV);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				NhanVien nv = new NhanVien();

				nv.setMaNV(rs.getString("maNV"));
				nv.setHoTen(rs.getString("hoTen"));
				nv.setGioiTinh(rs.getString("gioiTinh").equalsIgnoreCase("Nam"));
				nv.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
				nv.setSdt(rs.getString("sdt"));
				nv.setEmail(rs.getString("email"));
				nv.setChucVu(rs.getString("chucVu"));
				nv.setTrangThai(rs.getString("trangThai"));
				nv.setDiachi(rs.getString("diaChi"));
				nv.setNgayVaoLam(rs.getDate("ngayVaoLam"));
				nv.setGhiChu(rs.getString("ghiChu"));
				nv.setAnhDaiDien(rs.getString("anhDaiDien"));

				dsNhanVien.add(nv);
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNhanVien;
	}

	// ========================== XÓA NHÂN VIÊN ==========================

	public boolean xoaNhanVien(String maNV) {
		int n = 0;

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "DELETE FROM NhanVien WHERE maNV = ?";
			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, maNV);

			n = stmt.executeUpdate();

			stmt.close();
//			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return n > 0;
	}

	// ========================== LỌC NHÂN VIÊN THEO CHỨC VỤ
	// ==========================

	public ArrayList<NhanVien> getTblTheoChucVu(String chucVu) {
		ArrayList<NhanVien> dsNV = new ArrayList<NhanVien>();

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "SELECT * FROM NhanVien WHERE chucVu = ?";
			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, chucVu);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				NhanVien nv = new NhanVien();

				nv.setMaNV(rs.getString("maNV"));
				nv.setHoTen(rs.getString("hoTen"));
				nv.setGioiTinh(rs.getString("gioiTinh").equalsIgnoreCase("Nam"));
				nv.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
				nv.setSdt(rs.getString("sdt"));
				nv.setEmail(rs.getString("email"));
				nv.setChucVu(rs.getString("chucVu"));
				nv.setTrangThai(rs.getString("trangThai"));
				nv.setDiachi(rs.getString("diaChi"));
				nv.setNgayVaoLam(rs.getDate("ngayVaoLam"));
				nv.setGhiChu(rs.getString("ghiChu"));
				nv.setAnhDaiDien(rs.getString("anhDaiDien"));

				dsNV.add(nv);
			}

			rs.close();
			stmt.close();
//			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNV;
	}

	// ========================== LỌC NHÂN VIÊN THEO TRẠNG THÁI
	// ==========================

	public ArrayList<NhanVien> getTblTheoTrangThai(String trangThai) {
		ArrayList<NhanVien> dsNV = new ArrayList<NhanVien>();

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "SELECT * FROM NhanVien WHERE trangThai = ?";
			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, trangThai);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				NhanVien nv = new NhanVien();

				nv.setMaNV(rs.getString("maNV"));
				nv.setHoTen(rs.getString("hoTen"));
				nv.setGioiTinh(rs.getString("gioiTinh").equalsIgnoreCase("Nam"));
				nv.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
				nv.setSdt(rs.getString("sdt"));
				nv.setEmail(rs.getString("email"));
				nv.setChucVu(rs.getString("chucVu"));
				nv.setTrangThai(rs.getString("trangThai"));
				nv.setDiachi(rs.getString("diaChi"));
				nv.setNgayVaoLam(rs.getDate("ngayVaoLam"));
				nv.setGhiChu(rs.getString("ghiChu"));
				nv.setAnhDaiDien(rs.getString("anhDaiDien"));

				dsNV.add(nv);
			}

			rs.close();
			stmt.close();
//			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNV;
	}

	// ========================== LỌC NHÂN VIÊN THEO GIỚI TÍNH
	// ==========================

	public ArrayList<NhanVien> getTblTheoGioiTinh(boolean gioiTinh) {
		ArrayList<NhanVien> dsNV = new ArrayList<NhanVien>();

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "SELECT * FROM NhanVien WHERE gioiTinh = ?";
			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, gioiTinh ? "Nam" : "Nữ");

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				NhanVien nv = new NhanVien();

				nv.setMaNV(rs.getString("maNV"));
				nv.setHoTen(rs.getString("hoTen"));
				nv.setGioiTinh(rs.getString("gioiTinh").equalsIgnoreCase("Nam"));
				nv.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
				nv.setSdt(rs.getString("sdt"));
				nv.setEmail(rs.getString("email"));
				nv.setChucVu(rs.getString("chucVu"));
				nv.setTrangThai(rs.getString("trangThai"));
				nv.setDiachi(rs.getString("diaChi"));
				nv.setNgayVaoLam(rs.getDate("ngayVaoLam"));
				nv.setGhiChu(rs.getString("ghiChu"));
				nv.setAnhDaiDien(rs.getString("anhDaiDien"));

				dsNV.add(nv);
			}

			rs.close();
			stmt.close();
//			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNV;
	}

	// ========================== TÌM KIẾM NHÂN VIÊN ==========================

	public ArrayList<NhanVien> timKiemNhanVien(String tuKhoa) {
		ArrayList<NhanVien> dsNV = new ArrayList<NhanVien>();

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "SELECT * FROM NhanVien " + "WHERE maNV LIKE ? OR hoTen LIKE ? "
					+ "OR sdt LIKE ? OR email LIKE ?";

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, "%" + tuKhoa + "%");
			stmt.setString(2, "%" + tuKhoa + "%");
			stmt.setString(3, "%" + tuKhoa + "%");
			stmt.setString(4, "%" + tuKhoa + "%");

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				NhanVien nv = new NhanVien();

				nv.setMaNV(rs.getString("maNV"));
				nv.setHoTen(rs.getString("hoTen"));
				nv.setGioiTinh(rs.getString("gioiTinh").equalsIgnoreCase("Nam"));
				nv.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
				nv.setSdt(rs.getString("sdt"));
				nv.setEmail(rs.getString("email"));
				nv.setChucVu(rs.getString("chucVu"));
				nv.setTrangThai(rs.getString("trangThai"));
				nv.setDiachi(rs.getString("diaChi"));
				nv.setNgayVaoLam(rs.getDate("ngayVaoLam"));
				nv.setGhiChu(rs.getString("ghiChu"));
				nv.setAnhDaiDien(rs.getString("anhDaiDien"));

				dsNV.add(nv);
			}

			rs.close();
			stmt.close();
//			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dsNV;
	}

	// ========================== CẬP NHẬT NHÂN VIÊN ==========================

	public boolean update(NhanVien nv) {
		int n = 0;

		try {
			Connection con = ConnectDB.getConnection();

			String sql = "UPDATE NhanVien SET " + "hoTen = ?, gioiTinh = ?, ngaySinh = ?, sdt = ?, "
					+ "email = ?, chucVu = ?, trangThai = ?, diaChi = ?, "
					+ "ghiChu = ?, anhDaiDien = ?, ngayVaoLam = ? " + "WHERE maNV = ?";

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, nv.getHoTen());
			stmt.setString(2, nv.isGioiTinh() ? "Nam" : "Nữ");
			stmt.setDate(3, Date.valueOf(nv.getNgaySinh()));
			stmt.setString(4, nv.getSdt());
			stmt.setString(5, nv.getEmail());
			stmt.setString(6, nv.getChucVu());
			stmt.setString(7, nv.getTrangThai());
			stmt.setString(8, nv.getDiachi());
			stmt.setString(9, nv.getGhiChu());
			stmt.setString(10, nv.getAnhDaiDien());
			stmt.setDate(11, new Date(nv.getNgayVaoLam().getTime()));
			stmt.setString(12, nv.getMaNV());

			n = stmt.executeUpdate();

			stmt.close();
//			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return n > 0;
	}
}