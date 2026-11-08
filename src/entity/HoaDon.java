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
    public static final String DA_TT = "Đã thanh toán", TRA_GOP = "Trả góp";
    private String maNhanVien, ca, tenKhachHang, sdt, hang, tenKhuyenMai, doiTac;
    private int kyHan;
    private BigDecimal giamHoaDon = BigDecimal.ZERO, khachDua = BigDecimal.ZERO;
    private java.util.List<Dong> chiTiet = new java.util.ArrayList<>();
    public HoaDon(String ma, LocalDateTime ngay, String nv, String ca, String kh, String sdt,
            String hang, String phuongThuc, String trangThai, BigDecimal giam, BigDecimal dua,
            String giaoHang, String km, String doiTac, int kyHan, java.util.List<Dong> ds) {
        maHD = ma; ngayLap = ngay; maNhanVien = nv; this.ca = ca; tenKhachHang = kh; this.sdt = sdt;
        this.hang = hang; phuongThucThanhToan = phuongThuc; this.trangThai = trangThai;
        giamHoaDon = giam; khachDua = dua; ghiChuGiaoLapDat = giaoHang;
        tenKhuyenMai = km; this.doiTac = doiTac; this.kyHan = kyHan;
        chiTiet = new java.util.ArrayList<>(ds);
    }
    public static class Dong {
        private final String sku, ten, danhMuc;
        private final int soLuong;
        private final BigDecimal donGia, giamGia;
        public Dong(String sku, String ten, String danhMuc, int soLuong, BigDecimal donGia, BigDecimal giamGia) {
            this.sku = sku; this.ten = ten; this.danhMuc = danhMuc; this.soLuong = soLuong;
            this.donGia = donGia; this.giamGia = giamGia;
        }
        public String sku() { return sku; }
        public String ten() { return ten; }
        public String danhMuc() { return danhMuc; }
        public int soLuong() { return soLuong; }
        public BigDecimal donGia() { return donGia; }
        public BigDecimal giamGia() { return giamGia; }
        public BigDecimal tienHang() { return donGia.multiply(BigDecimal.valueOf(soLuong)); }
        public BigDecimal thanhTien() { return tienHang().subtract(giamGia); }
    }
    public String ma() { return maHD; }
    public LocalDateTime ngay() { return ngayLap; }
    public String nhanVien() { return maNhanVien; }
    public String ca() { return ca; }
    public String khachHang() { return tenKhachHang; }
    public String sdt() { return sdt; }
    public String hang() { return hang; }
    public String phuongThuc() { return phuongThucThanhToan; }
    public String trangThai() { return trangThai; }
    public BigDecimal giamHoaDon() { return giamHoaDon; }
    public BigDecimal khachDua() { return khachDua; }
    public String giaoHang() { return ghiChuGiaoLapDat; }
    public String khuyenMai() { return tenKhuyenMai; }
    public String doiTac() { return doiTac; }
    public int kyHan() { return kyHan; }
    public java.util.List<Dong> chiTiet() { return java.util.Collections.unmodifiableList(chiTiet); }
    public BigDecimal tienHang() {
        BigDecimal tong = BigDecimal.ZERO;
        for (Dong d : chiTiet) tong = tong.add(d.tienHang());
        return tong;
    }
    public BigDecimal giamGia() {
        BigDecimal tong = giamHoaDon;
        for (Dong d : chiTiet) tong = tong.add(d.giamGia());
        return tong;
    }
    public BigDecimal tongTien() { return tienHang().subtract(giamGia()); }
    public boolean ghiNhanDoanhThu() { return DA_TT.equals(trangThai) || TRA_GOP.equals(trangThai); }
    public HoaDon voiMa(String id) {
        return new HoaDon(id, ngayLap, maNhanVien, ca, tenKhachHang, sdt, hang, phuongThucThanhToan,
                trangThai, giamHoaDon, khachDua, ghiChuGiaoLapDat, tenKhuyenMai, doiTac, kyHan, chiTiet);
    }
}
