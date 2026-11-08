package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HoaDon {
	private String maHD;
	private LocalDateTime ngayLap;
	private BigDecimal tongTienHang;
	private BigDecimal tienGiamGia;
	private BigDecimal tongThanhToan;
	private String phuongThucThanhToan;
	private boolean yeuCauGiaoLapDat;
	private String ghiChuGiaoLapDat;
	private String trangThai;
	private NhanVien nhanVien;
	private KhachHang khahHang;
	private KhuyenMai khuyenMai;
	
	public HoaDon() {
		super();
	}

	public HoaDon(String maHD, LocalDateTime ngayLap, BigDecimal tongTienHang, BigDecimal tienGiamGia,
			BigDecimal tongThanhToan, String phuongThucThanhToan, boolean yeuCauGiaoLapDat, String ghiChuGiaoLapDat,
			String trangThai) {
		super();
		this.maHD = maHD;
		this.ngayLap = ngayLap;
		this.tongTienHang = tongTienHang;
		this.tienGiamGia = tienGiamGia;
		this.tongThanhToan = tongThanhToan;
		this.phuongThucThanhToan = phuongThucThanhToan;
		this.yeuCauGiaoLapDat = yeuCauGiaoLapDat;
		this.ghiChuGiaoLapDat = ghiChuGiaoLapDat;
		this.trangThai = trangThai;
	}

	public String getMaHD() {
		return maHD;
	}

	public void setMaHD(String maHD) {
		this.maHD = maHD;
	}

	public LocalDateTime getNgayLap() {
		return ngayLap;
	}

	public void setNgayLap(LocalDateTime ngayLap) {
		this.ngayLap = ngayLap;
	}

	public BigDecimal getTongTienHang() {
		return tongTienHang;
	}

	public void setTongTienHang(BigDecimal tongTienHang) {
		this.tongTienHang = tongTienHang;
	}

	public BigDecimal getTienGiamGia() {
		return tienGiamGia;
	}

	public void setTienGiamGia(BigDecimal tienGiamGia) {
		this.tienGiamGia = tienGiamGia;
	}

	public BigDecimal getTongThanhToan() {
		return tongThanhToan;
	}

	public void setTongThanhToan(BigDecimal tongThanhToan) {
		this.tongThanhToan = tongThanhToan;
	}

	public String getPhuongThucThanhToan() {
		return phuongThucThanhToan;
	}

	public void setPhuongThucThanhToan(String phuongThucThanhToan) {
		this.phuongThucThanhToan = phuongThucThanhToan;
	}

	public boolean isYeuCauGiaoLapDat() {
		return yeuCauGiaoLapDat;
	}

	public void setYeuCauGiaoLapDat(boolean yeuCauGiaoLapDat) {
		this.yeuCauGiaoLapDat = yeuCauGiaoLapDat;
	}

	public String getGhiChuGiaoLapDat() {
		return ghiChuGiaoLapDat;
	}

	public void setGhiChuGiaoLapDat(String ghiChuGiaoLapDat) {
		this.ghiChuGiaoLapDat = ghiChuGiaoLapDat;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
}
