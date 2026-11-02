package entity;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class NhanVien {
	private String maNV;
	private String hoTen;
	private boolean gioiTinh; // true: Nam, false: Nữ
	private String sdt;
	private String email;
	private LocalDate ngaySinh;
	private String diachi;
	private String chucVu;
	private String trangThai; // true: Đang làm việc, false: Nghỉ việc/Tạm nghỉ
	private Date ngayVaoLam;
	private TaiKhoan taiKhoan;
	private String anhDaiDien;
	private String ghiChu;

	public NhanVien() {
	}

	public NhanVien(String maNV, String hoTen, boolean gioiTinh, String sdt, String email, LocalDate ngaySinh,
			String diachi, String chucVu, String trangThai, Date ngayVaoLam, TaiKhoan taiKhoan, String anhDaiDien,
			String ghiChu) {
		super();
		setMaNV(maNV);
		setHoTen(hoTen);
		this.gioiTinh = gioiTinh;
		setSdt(sdt);
		setEmail(email);
		this.ngaySinh = ngaySinh;
		this.diachi = diachi;
		setChucVu(chucVu);
		this.trangThai = trangThai;
		this.ngayVaoLam = ngayVaoLam;
		this.taiKhoan = taiKhoan;
		this.anhDaiDien = anhDaiDien;
		this.ghiChu = ghiChu;
	}

	public String getMaNV() {
		return maNV;
	}

	public void setMaNV(String maNV) {
		if (maNV == null || maNV.trim().isEmpty()) {
			throw new IllegalArgumentException("Mã nhân viên không được để trống!");
		}
		this.maNV = maNV;
	}

	public String getHoTen() {
		return hoTen;
	}

	public void setHoTen(String hoTen) {
		if (hoTen == null || hoTen.trim().isEmpty()) {
			throw new IllegalArgumentException("Họ tên nhân viên không được để trống!");
		}
		this.hoTen = hoTen;
	}

	public boolean isGioiTinh() {
		return gioiTinh;
	}

	public void setGioiTinh(boolean gioiTinh) {
		this.gioiTinh = gioiTinh;
	}

	public String getSdt() {
		return sdt;
	}

	public void setSdt(String sdt) {
		if (sdt == null || !sdt.matches("\\d{10}")) {
			throw new IllegalArgumentException("Số điện thoại phải bao gồm đúng 10 chữ số!");
		}
		this.sdt = sdt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email != null && !email.isEmpty() && !email.matches("[A-Za-z0-9+_.-]+@(.+)")) {
			throw new IllegalArgumentException("Địa chỉ email không hợp lệ!");
		}
		this.email = email;
	}

	public LocalDate getNgaySinh() {
		return ngaySinh;
	}

	public void setNgaySinh(LocalDate ngaySinh) {
		this.ngaySinh = ngaySinh;
	}

	public String getDiachi() {
		return diachi;
	}

	public void setDiachi(String diachi) {
		this.diachi = diachi;
	}

	public String getChucVu() {
		return chucVu;
	}

	public void setChucVu(String chucVu) {
		if (chucVu == null
				|| (!chucVu.equals("Quản lý") && !chucVu.equals("Bán hàng") && !chucVu.equals("Chăm sóc khách hàng"))) {
			throw new IllegalArgumentException("Chức vụ không hợp lệ theo quy định hệ thống!");
		}
		this.chucVu = chucVu;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public Date getNgayVaoLam() {
		return ngayVaoLam;
	}

	public void setNgayVaoLam(Date ngayVaoLam) {
		this.ngayVaoLam = ngayVaoLam;
	}

	public TaiKhoan getTaiKhoan() {
		return taiKhoan;
	}

	public void setTaiKhoan(TaiKhoan taiKhoan) {
		this.taiKhoan = taiKhoan;
	}

	public String getAnhDaiDien() {
		return anhDaiDien;
	}

	public void setAnhDaiDien(String anhDaiDien) {
		this.anhDaiDien = anhDaiDien;
	}

	public String getGhiChu() {
		return ghiChu;
	}

	public void setGhiChu(String ghiChu) {
		this.ghiChu = ghiChu;
	}

	@Override
	public String toString() {
		return "NhanVien [maNV=" + maNV + ", hoTen=" + hoTen + ", gioiTinh=" + gioiTinh + ", sdt=" + sdt + ", email="
				+ email + ", ngaySinh=" + ngaySinh + ", diachi=" + diachi + ", chucVu=" + chucVu + ", trangThai="
				+ trangThai + ", ngayVaoLam=" + ngayVaoLam + ", taiKhoan=" + taiKhoan + ", anhDaiDien=" + anhDaiDien
				+ ", ghiChu=" + ghiChu + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(maNV);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NhanVien other = (NhanVien) obj;
		return Objects.equals(maNV, other.maNV);
	}
}
