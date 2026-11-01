package entity;

import java.util.Date;
import java.util.Objects;

public class KhuyenMai {
	private String maKM;
	private String tenKM;
	private String loaiKM;
	private double giaTriGiam;
	private Date thoiGianBatDau;
	private Date thoiGianKetThuc;
	private String trangThai;
	private String phamViApDung;
	private String doiTuongApDung;

	public KhuyenMai() {
	}

	public KhuyenMai(String maKM, String tenKM, String loaiKM, double giaTriGiam, Date thoigianBatDau,
			Date thoigianKetThuc, String trangThai, String phamViApDung, String doiTuongApDung) {
		super();
		setMaKM(maKM);
		setTenKM(tenKM);
		setLoaiKM(loaiKM);
		setGiaTriGiam(giaTriGiam);
		setThoigianBatDau(thoigianBatDau);
		setThoigianKetThuc(thoigianKetThuc);
		this.trangThai = trangThai;
		this.phamViApDung = phamViApDung;
		this.doiTuongApDung = doiTuongApDung;
	}

	public boolean kiemTraTrangThaiHieuLuc() {
		Date now = new Date();
		return "Đang kích hoạt".equals(this.trangThai) && now.after(thoiGianBatDau) && now.before(thoiGianKetThuc);
	}

	public boolean kiemTraTrungLapKM(KhuyenMai kmKhac) {
		if (kmKhac == null)
			return false;
		boolean trungThoiGian = !(this.thoiGianKetThuc.before(kmKhac.thoiGianBatDau)
				|| this.thoiGianBatDau.after(kmKhac.thoiGianKetThuc));
		boolean cungLoaiPhanTram = "Giảm %".equals(this.loaiKM) && "Giảm %".equals(kmKhac.loaiKM);
		return trungThoiGian && cungLoaiPhanTram;
	}

	public String getMaKM() {
		return maKM;
	}

	public void setMaKM(String maKM) {
		if (maKM == null || maKM.trim().isEmpty()) {
			throw new IllegalArgumentException("Mã khuyến mãi không được để trống!");
		}
		this.maKM = maKM;
	}

	public String getTenKM() {
		return tenKM;
	}

	public void setTenKM(String tenKM) {
		if (tenKM == null || tenKM.trim().isEmpty()) {
			throw new IllegalArgumentException("Tên chương trình khuyến mãi không được để trống!");
		}
		this.tenKM = tenKM;
	}

	public String getLoaiKM() {
		return loaiKM;
	}

	public void setLoaiKM(String loaiKM) {
		this.loaiKM = loaiKM;
	}

	public double getGiaTriGiam() {
		return giaTriGiam;
	}

	public void setGiaTriGiam(double giaTriGiam) {
		if (giaTriGiam < 0) {
			throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0!");
		}
		this.giaTriGiam = giaTriGiam;
	}

	public Date getThoigianBatDau() {
		return thoiGianBatDau;
	}

	public void setThoigianBatDau(Date thoigianBatDau) {
		this.thoiGianBatDau = thoigianBatDau;
	}

	public Date getThoigianKetThuc() {
		return thoiGianKetThuc;
	}

	public void setThoigianKetThuc(Date thoigianKetThuc) {
		if (thoiGianBatDau != null && thoiGianKetThuc != null && thoiGianKetThuc.before(thoiGianBatDau)) {
			throw new IllegalArgumentException("Thời gian kết thúc phải lớn hơn hoặc bằng thời gian bắt đầu!");
		}
		this.thoiGianKetThuc = thoiGianKetThuc;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		if (trangThai == null || (!trangThai.equals("Chờ kích hoạt") && !trangThai.equals("Đang kích hoạt")
				&& !trangThai.equals("Đã kết thúc") && !trangThai.equals("Đã hủy"))) {
			throw new IllegalArgumentException("Trạng thái khuyến mãi không hợp lệ!");
		}
		this.trangThai = trangThai;
	}

	public String getPhamViApDung() {
		return phamViApDung;
	}

	public void setPhamViApDung(String phamViApDung) {
		this.phamViApDung = phamViApDung;
	}

	public String getDoiTuongApDung() {
		return doiTuongApDung;
	}

	public void setDoiTuongApDung(String doiTuongApDung) {
		this.doiTuongApDung = doiTuongApDung;
	}

	@Override
	public String toString() {
		return "KhuyenMai [maKM=" + maKM + ", tenKM=" + tenKM + ", loaiKM=" + loaiKM + ", giaTriGiam=" + giaTriGiam
				+ ", thoigianBatDau=" + thoiGianBatDau + ", thoigianKetThuc=" + thoiGianKetThuc + ", trangThai="
				+ trangThai + ", phamViApDung=" + phamViApDung + ", doiTuongApDung=" + doiTuongApDung + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(maKM);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhuyenMai other = (KhuyenMai) obj;
		return Objects.equals(maKM, other.maKM);
	}
}