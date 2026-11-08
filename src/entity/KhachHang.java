package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Dữ liệu trong phiên chạy; DAO trả về bản sao để tránh thay đổi ngoài ý muốn. */
public class KhachHang {
    private String maKH;
    private String hoTen;
    private String sdt;
    private String email;
    private String diaChi;
    private LocalDate ngaySinh;
    private boolean gioiTinh;
    private String hangTV;
    private double tongChiTieuTichLuy;
    private boolean trangThai;

    public KhachHang() { }
    
	public KhachHang(String maKH, String hoTen, String sdt, String email, String diaChi, LocalDate ngaySinh,
			boolean gioiTinh, String hangTV, double tongChiTieuTichLuy, boolean trangThai) {
		super();
		this.maKH = maKH;
		this.hoTen = hoTen;
		this.sdt = sdt;
		this.email = email;
		this.diaChi = diaChi;
		this.ngaySinh = ngaySinh;
		this.gioiTinh = gioiTinh;
		this.hangTV = hangTV;
		this.tongChiTieuTichLuy = tongChiTieuTichLuy;
		this.trangThai = trangThai;
	}

	public KhachHang(KhachHang other) {
        this(other.maKH, other.hoTen, other.sdt, other.email, other.diaChi, other.ngaySinh, other.gioiTinh, other.hangTV, other.tongChiTieuTichLuy, other.trangThai);
    }
	public String getMaKH() {
		return maKH;
	}
	public void setMaKH(String maKH) {
		this.maKH = maKH;
	}
	public String getHoTen() {
		return hoTen;
	}
	public void setHoTen(String hoTen) {
		this.hoTen = hoTen;
	}
	public String getSdt() {
		return sdt;
	}
	public void setSdt(String sdt) {
		this.sdt = sdt;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDiaChi() {
		return diaChi;
	}
	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}
	public LocalDate getNgaySinh() {
		return ngaySinh;
	}
	public void setNgaySinh(LocalDate ngaySinh) {
		this.ngaySinh = ngaySinh;
	}
	public boolean isGioiTinh() {
		return gioiTinh;
	}
	public void setGioiTinh(boolean gioiTinh) {
		this.gioiTinh = gioiTinh;
	}
	public String getHangTV() {
		return hangTV;
	}
	public void setHangTV(String hangTV) {
		this.hangTV = hangTV;
	}
	public double getTongChiTieuTichLuy() {
		return tongChiTieuTichLuy;
	}
	public void setTongChiTieuTichLuy(double tongChiTieuTichLuy) {
		this.tongChiTieuTichLuy = tongChiTieuTichLuy;
	}

	public boolean isTrangThai() {
		return trangThai;
	}

	public void setTrangThai(boolean trangThai) {
		this.trangThai = trangThai;
	}
	
}
