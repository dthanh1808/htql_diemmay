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
	private boolean trangThai; // true: Đang làm việc, false: Nghỉ việc/Tạm nghỉ
	private Date ngayVaoLam;
	private String anhDaiDien;

	public NhanVien() {
	}

	public NhanVien(String maNV, String hoTen, boolean gioiTinh, String sdt, String email, LocalDate ngaySinh,
			String diachi, String chucVu, boolean trangThai, Date ngayVaoLam, String anhDaiDien) {
		super();
		this.maNV = maNV;
		this.hoTen = hoTen;
		this.gioiTinh = gioiTinh;
		this.sdt = sdt;
		this.email = email;
		this.ngaySinh = ngaySinh;
		this.diachi = diachi;
		this.chucVu = chucVu;
		this.trangThai = trangThai;
		this.ngayVaoLam = ngayVaoLam;
		this.anhDaiDien = anhDaiDien;
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
//		if (chucVu == null
//				|| (!chucVu.equals("Quản lý") && !chucVu.equals("Bán hàng") && !chucVu.equals("Chăm sóc khách hàng"))) {
//			throw new IllegalArgumentException("Chức vụ không hợp lệ theo quy định hệ thống!");
//		}
		this.chucVu = chucVu;
	}	

	public boolean isTrangThai() {
		return trangThai;
	}

	public void setTrangThai(boolean trangThai) {
		this.trangThai = trangThai;
	}

	public Date getNgayVaoLam() {
		return ngayVaoLam;
	}

	public void setNgayVaoLam(Date ngayVaoLam) {
		this.ngayVaoLam = ngayVaoLam;
	}

	public String getAnhDaiDien() {
		return anhDaiDien;
	}

	public void setAnhDaiDien(String anhDaiDien) {
		this.anhDaiDien = anhDaiDien;
	}

	@Override
	public String toString() {
		return "NhanVien [maNV=" + maNV + ", hoTen=" + hoTen + ", gioiTinh=" + gioiTinh + ", sdt=" + sdt + ", email="
				+ email + ", ngaySinh=" + ngaySinh + ", diachi=" + diachi + ", chucVu=" + chucVu + ", trangThai="
				+ trangThai + ", ngayVaoLam=" + ngayVaoLam + ", anhDaiDien=" + anhDaiDien
				+ "]";
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
    // Tương thích UI nhân viên cũ dùng nhãn trạng thái, schema SQL dùng BIT.
    private String ghiChu = "";
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String value) { ghiChu = value; }
    public String getTrangThai() { return trangThai ? "Đang làm việc" : "Đã nghỉ việc"; }
    public void setTrangThai(String value) {
        trangThai = "1".equals(value) || "true".equalsIgnoreCase(value) || "Đang làm việc".equals(value);
    }
}
