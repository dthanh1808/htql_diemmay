package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Đối tượng dữ liệu, sử dụng constructor, getter và setter thông thường. */
public class SanPham {
	private String id;
	private String sku;
	private String ten;
	private String danhMucId;
	private String thuongHieu;
	private BigDecimal giaBan;
	private int baoHanhThang;
	private boolean dangKinhDoanh;
	private String moTa;
	private String anhBase64;
	private LocalDateTime taoLuc;
	private LocalDateTime capNhatLuc;

	public SanPham() {
	}

	public SanPham(String id, String sku, String ten, String danhMucId, String thuongHieu, BigDecimal giaBan,
			int baoHanhThang, boolean dangKinhDoanh, String moTa, String anhBase64, LocalDateTime taoLuc,
			LocalDateTime capNhatLuc) {
		this.id = id;
		this.sku = sku;
		this.ten = ten;
		this.danhMucId = danhMucId;
		this.thuongHieu = thuongHieu;
		this.giaBan = giaBan;
		this.baoHanhThang = baoHanhThang;
		this.dangKinhDoanh = dangKinhDoanh;
		this.moTa = moTa;
		this.anhBase64 = anhBase64;
		this.taoLuc = taoLuc;
		this.capNhatLuc = capNhatLuc;
	}

	public SanPham(SanPham other) {
		this(other.id, other.sku, other.ten, other.danhMucId, other.thuongHieu, other.giaBan, other.baoHanhThang,
				other.dangKinhDoanh, other.moTa, other.anhBase64, other.taoLuc, other.capNhatLuc);
		maVach = other.maVach;
		model = other.model;
		nhomSP = other.nhomSP;
		maKM = other.maKM;
		slTon = other.slTon;
		danhMuc = other.danhMuc == null ? null : new DanhMuc(other.danhMuc);
		khuyenMai = other.khuyenMai;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getTen() {
		return ten;
	}
	public void setTen(String ten) {
		this.ten = ten;
	}

	public String getDanhMucId() {
		return danhMucId;
	}
	public void setDanhMucId(String danhMucId) {
		this.danhMucId = danhMucId;
	}

	public String getThuongHieu() {
		return thuongHieu;
	}
	public void setThuongHieu(String thuongHieu) {
		this.thuongHieu = thuongHieu;
	}

	public BigDecimal getGiaBan() {
		return giaBan;
	}
	public void setGiaBan(BigDecimal giaBan) {
		this.giaBan = giaBan;
	}

	public int getBaoHanhThang() {
		return baoHanhThang;
	}
	public void setBaoHanhThang(int baoHanhThang) {
		this.baoHanhThang = baoHanhThang;
	}

	public boolean isDangKinhDoanh() {
		return dangKinhDoanh;
	}
	public void setDangKinhDoanh(boolean dangKinhDoanh) {
		this.dangKinhDoanh = dangKinhDoanh;
	}

	public String getMoTa() {
		return moTa;
	}
	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	public String getAnhBase64() {
		return anhBase64;
	}
	public void setAnhBase64(String anhBase64) {
		this.anhBase64 = anhBase64;
	}

	public LocalDateTime getTaoLuc() {
		return taoLuc;
	}
	public void setTaoLuc(LocalDateTime taoLuc) {
		this.taoLuc = taoLuc;
	}

	public LocalDateTime getCapNhatLuc() {
		return capNhatLuc;
	}
	public void setCapNhatLuc(LocalDateTime capNhatLuc) {
		this.capNhatLuc = capNhatLuc;
	}
	private String maVach;
	private String model;
	private String nhomSP;
	private String maKM;
	private int slTon;
	private DanhMuc danhMuc;
	private KhuyenMai khuyenMai;
	public String getMaSKU() {
		return sku;
	}
	public void setMaSKU(String value) {
		sku = value;
	}
	public String getTenSP() {
		return ten;
	}
	public void setTenSP(String value) {
		ten = value;
	}
	public BigDecimal getDonGia() {
		return giaBan;
	}
	public void setDonGia(BigDecimal value) {
		giaBan = value;
	}
	public int getThoiGianBaoHanh() {
		return baoHanhThang;
	}
	public void setThoiGianBaoHanh(int value) {
		baoHanhThang = value;
	}
	public boolean isTrangThai() {
		return dangKinhDoanh;
	}
	public void setTrangThai(boolean value) {
		dangKinhDoanh = value;
	}
	public String getMaVach() {
		return maVach;
	}
	public void setMaVach(String value) {
		maVach = value;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String value) {
		model = value;
	}
	public String getNhomSP() {
		return nhomSP;
	}
	public void setNhomSP(String value) {
		nhomSP = value;
	}
	public String getMaKM() {
		return maKM;
	}
	public void setMaKM(String value) {
		maKM = value;
	}
	public int getSlTon() {
		return slTon;
	}
	public void setSlTon(int value) {
		slTon = value;
	}
	public DanhMuc getDanhMuc() {
		return danhMuc;
	}
	public void setDanhMuc(DanhMuc value) {
		danhMuc = value;
		danhMucId = value == null ? null : value.getId();
	}
	public KhuyenMai getKhuyenMai() {
		return khuyenMai;
	}
	public void setKhuyenMai(KhuyenMai value) {
		khuyenMai = value;
		maKM = value == null ? null : value.getMaKM();
	}
	public SanPham(String maSKU, String maVach, String tenSP, String thuongHieu, BigDecimal donGia, int thoiGianBaoHanh,
			String model, String moTa, String nhomSP, boolean trangThai, DanhMuc danhMuc, KhuyenMai khuyenMai) {
		this.sku = maSKU;
		this.maVach = maVach;
		this.ten = tenSP;
		this.thuongHieu = thuongHieu;
		this.giaBan = donGia;
		this.baoHanhThang = thoiGianBaoHanh;
		this.model = model;
		this.moTa = moTa;
		this.nhomSP = nhomSP;
		this.dangKinhDoanh = trangThai;
		setDanhMuc(danhMuc);
		setKhuyenMai(khuyenMai);
	}
}
