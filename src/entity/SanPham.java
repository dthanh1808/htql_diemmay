package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Đối tượng dữ liệu, sử dụng constructor, getter và setter thông thường. */
public class SanPham {
    private String maSKU;
    private String maVach;
    private String tenSP;
    private String thuongHieu;
    private BigDecimal donGia;
    private int thoiGianBaoHanh;
    private String model;
    private String moTa;
    private String nhomSP;    
    private boolean trangThai;
    private DanhMuc danhMuc;
    private KhuyenMai khuyenMai;

    public SanPham() { }
    
	public SanPham(String maSKU, String maVach, String tenSP, String thuongHieu, BigDecimal donGia, int thoiGianBaoHanh,
			String model, String moTa, String nhomSP, boolean trangThai, DanhMuc danhMuc, KhuyenMai khuyenMai) {
		super();
		this.maSKU = maSKU;
		this.maVach = maVach;
		this.tenSP = tenSP;
		this.thuongHieu = thuongHieu;
		this.donGia = donGia;
		this.thoiGianBaoHanh = thoiGianBaoHanh;
		this.model = model;
		this.moTa = moTa;
		this.nhomSP = nhomSP;
		this.trangThai = trangThai;
		this.danhMuc = danhMuc;
		this.khuyenMai = khuyenMai;
	}
	public SanPham(SanPham other) {
        this(other.maSKU, other.maVach, other.tenSP, other.thuongHieu, other.donGia,
                other.thoiGianBaoHanh, other.model, other.moTa,other.nhomSP, other.trangThai,
                other.danhMuc, other.khuyenMai);
    }

	public String getMaSKU() {
		return maSKU;
	}

	public void setMaSKU(String maSKU) {
		this.maSKU = maSKU;
	}

	public String getMaVach() {
		return maVach;
	}

	public void setMaVach(String maVach) {
		this.maVach = maVach;
	}

	public String getTenSP() {
		return tenSP;
	}

	public void setTenSP(String tenSP) {
		this.tenSP = tenSP;
	}

	public String getThuongHieu() {
		return thuongHieu;
	}

	public void setThuongHieu(String thuongHieu) {
		this.thuongHieu = thuongHieu;
	}

	public BigDecimal getDonGia() {
		return donGia;
	}

	public void setDonGia(BigDecimal donGia) {
		this.donGia = donGia;
	}

	public int getThoiGianBaoHanh() {
		return thoiGianBaoHanh;
	}

	public void setThoiGianBaoHanh(int thoiGianBaoHanh) {
		this.thoiGianBaoHanh = thoiGianBaoHanh;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	public String getNhomSP() {
		return nhomSP;
	}

	public void setNhomSP(String nhomSP) {
		this.nhomSP = nhomSP;
	}
	
	public boolean isTrangThai() {
		return trangThai;
	}

	public void setTrangThai(boolean trangThai) {
		this.trangThai = trangThai;
	}

	public DanhMuc getDanhMuc() {
		return danhMuc;
	}

	public void setDanhMuc(DanhMuc danhMuc) {
		this.danhMuc = danhMuc;
	}

	public KhuyenMai getKhuyenMai() {
		return khuyenMai;
	}

	public void setKhuyenMai(KhuyenMai khuyenMai) {
		this.khuyenMai = khuyenMai;
	}
}
