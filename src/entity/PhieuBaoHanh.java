package entity;

import java.util.Date;

public class PhieuBaoHanh {
    private String maPhieu;
    private String soSerial;
    private String loaiBaoHanh;
    private Date ngayTiepNhan;																																																																																									
    private Date ngayHenTra;
    private String ghiChu;
    private double chiPhiSuaChua;
    private String trangThai;
    private SanPham sanPham;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private HoaDon hoaDon;
    
    public PhieuBaoHanh() {
    }

    public PhieuBaoHanh(String maPhieu, String soSerial, String loaiBaoHanh, Date ngayTiepNhan, Date ngayHenTra,
			String ghiChu, double chiPhiSuaChua, String trangThai, SanPham sanPham, KhachHang khachHang,
			NhanVien nhanVien, HoaDon hoaDon) {
		super();
		this.maPhieu = maPhieu;
		this.soSerial = soSerial;
		this.loaiBaoHanh = loaiBaoHanh;
		this.ngayTiepNhan = ngayTiepNhan;
		this.ngayHenTra = ngayHenTra;
		this.ghiChu = ghiChu;
		this.chiPhiSuaChua = chiPhiSuaChua;
		this.trangThai = trangThai;
		this.sanPham = sanPham;
		this.khachHang = khachHang;
		this.nhanVien = nhanVien;
		this.hoaDon = hoaDon;
	}

	public String getMaPhieu() {
		return maPhieu;
	}

	public void setMaPhieu(String maPhieu) {
		this.maPhieu = maPhieu;
	}

	public String getSoSerial() {
		return soSerial;
	}

	public void setSoSerial(String soSerial) {
		this.soSerial = soSerial;
	}

	public String getLoaiBaoHanh() {
		return loaiBaoHanh;
	}

	public void setLoaiBaoHanh(String loaiBaoHanh) {
		this.loaiBaoHanh = loaiBaoHanh;
	}

	public Date getNgayTiepNhan() {
		return ngayTiepNhan;
	}

	public void setNgayTiepNhan(Date ngayTiepNhan) {
		this.ngayTiepNhan = ngayTiepNhan;
	}

	public Date getNgayHenTra() {
		return ngayHenTra;
	}

	public void setNgayHenTra(Date ngayHenTra) {
		this.ngayHenTra = ngayHenTra;
	}

	public String getGhiChu() {
		return ghiChu;
	}

	public void setGhiChu(String ghiChu) {
		this.ghiChu = ghiChu;
	}

	public double getChiPhiSuaChua() {
		return chiPhiSuaChua;
	}

	public void setChiPhiSuaChua(double chiPhiSuaChua) {
		this.chiPhiSuaChua = chiPhiSuaChua;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public SanPham getSanPham() {
		return sanPham;
	}

	public void setSanPham(SanPham sanPham) {
		this.sanPham = sanPham;
	}

	public KhachHang getKhachHang() {
		return khachHang;
	}

	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}

	public NhanVien getNhanVien() {
		return nhanVien;
	}

	public void setNhanVien(NhanVien nhanVien) {
		this.nhanVien = nhanVien;
	}

	public HoaDon getHoaDon() {
		return hoaDon;
	}

	public void setHoaDon(HoaDon hoaDon) {
		this.hoaDon = hoaDon;
	}

	@Override
    public String toString() {
        return "PhieuBaoHanh{" +
                "maPhieu='" + maPhieu + '\'' +
                ", soSerial='" + soSerial + '\'' +
                ", sp=" + sanPham +
                ", kh=" + khachHang +
                ", nv=" + nhanVien +
                ", hd=" + hoaDon +
                ", ngayTiepNhan=" + ngayTiepNhan +
                ", ngayHenTra=" + ngayHenTra +
                ", moTa='" + ghiChu + '\'' +
                ", chiPhiSuaChua=" + chiPhiSuaChua +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}