package entity;

import java.util.Date;
import java.util.Objects;

public class KhuyenMai {

	// Các giá trị dùng chung giữa UI, entity và database
	public static final String LOAI_PHAN_TRAM = "Giảm %";
	public static final String LOAI_TIEN_MAT = "Giảm tiền cố định";
	public static final String LOAI_TANG_SP = "Combo";

	public static final String TT_CHO = "Chờ kích hoạt";
	public static final String TT_DANG = "Đang kích hoạt";
	public static final String TT_KET_THUC = "Đã kết thúc";
	public static final String TT_HUY = "Đã hủy";

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
		setTrangThai(trangThai);
		setPhamViApDung(phamViApDung);
		setDoiTuongApDung(doiTuongApDung);
	}

	// Khuyến mãi đang có hiệu lực: trạng thái "Đang kích hoạt" và đang trong khoảng
	// thời gian
	public boolean kiemTraTrangThaiHieuLuc() {
		if (thoiGianBatDau == null || thoiGianKetThuc == null) {
			return false;
		}
		Date now = new Date();
		return TT_DANG.equals(this.trangThai) && !now.before(thoiGianBatDau) && !now.after(thoiGianKetThuc);
	}

	// Trùng lặp: giao nhau về thời gian và cùng là loại giảm theo %
	public boolean kiemTraTrungLapKM(KhuyenMai kmKhac) {
		if (kmKhac == null) {
			return false;
		}
		if (this.thoiGianBatDau == null || this.thoiGianKetThuc == null || kmKhac.thoiGianBatDau == null
				|| kmKhac.thoiGianKetThuc == null) {
			return false;
		}
		boolean trungThoiGian = !(this.thoiGianKetThuc.before(kmKhac.thoiGianBatDau)
				|| this.thoiGianBatDau.after(kmKhac.thoiGianKetThuc));
		boolean cungLoaiPhanTram = LOAI_PHAN_TRAM.equals(this.loaiKM) && LOAI_PHAN_TRAM.equals(kmKhac.loaiKM);
		return trungThoiGian && cungLoaiPhanTram;
	}

	public String getMaKM() {
		return maKM;
	}

	public void setMaKM(String maKM) {
		if (maKM == null || maKM.trim().isEmpty()) {
			throw new IllegalArgumentException("Mã khuyến mãi không được để trống!");
		}
		this.maKM = maKM.trim();
	}

	public String getTenKM() {
		return tenKM;
	}

	public void setTenKM(String tenKM) {
		if (tenKM == null || tenKM.trim().isEmpty()) {
			throw new IllegalArgumentException("Tên chương trình khuyến mãi không được để trống!");
		}
		this.tenKM = tenKM.trim();
	}

	public String getLoaiKM() {
		return loaiKM;
	}

	public void setLoaiKM(String loaiKM) {
		if (loaiKM == null
				|| (!loaiKM.equals(LOAI_PHAN_TRAM) && !loaiKM.equals(LOAI_TIEN_MAT) && !loaiKM.equals(LOAI_TANG_SP))) {
			throw new IllegalArgumentException("Loại khuyến mãi không hợp lệ!");
		}
		this.loaiKM = loaiKM;
	}

	public double getGiaTriGiam() {
		return giaTriGiam;
	}

	// Cho phép 0 vì loại "Tặng sản phẩm" không có giá trị giảm
	public void setGiaTriGiam(double giaTriGiam) {
		if (giaTriGiam < 0) {
			throw new IllegalArgumentException("Giá trị giảm không được âm!");
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
		if (thoiGianBatDau != null && thoigianKetThuc != null && thoigianKetThuc.before(thoiGianBatDau)) {
			throw new IllegalArgumentException("Thời gian kết thúc phải lớn hơn hoặc bằng thời gian bắt đầu!");
		}
		this.thoiGianKetThuc = thoigianKetThuc;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		if (trangThai == null || (!trangThai.equals(TT_CHO) && !trangThai.equals(TT_DANG)
				&& !trangThai.equals(TT_KET_THUC) && !trangThai.equals(TT_HUY))) {
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