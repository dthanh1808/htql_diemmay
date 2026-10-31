//package entity;
//
//import java.util.Date;
//
//public class PhieuBaoHanh {
//    private String maPhieu;
//    private String soSerial;
//    private String loaiBaoHanh;
//    private SanPham sp;
//    private KhachHang kh;
//    private NhanVien nv;
//    private Date ngayTiepNhan;
//    private Date ngayHenTra;
//    private String ghiChu;
//    private double chiPhiSuaChua;
//    private String trangThai;
//
//    public PhieuBaoHanh() {
//    }
//
//    public PhieuBaoHanh(String maPhieu, String soSerial, SanPham sp, KhachHang kh, NhanVien nv, Date ngayTiepNhan, Date ngayHenTra, String ghiChuS, double chiPhiSuaChua, String trangThai) {
//        this.maPhieu = maPhieu;
//        this.soSerial = soSerial;
//        this.sp = sp;
//        this.kh = kh;
//        this.nv = nv;
//        this.ngayTiepNhan = ngayTiepNhan;
//        this.ngayHenTra = ngayHenTra;
//        this.ghiChu = ghiChu;
//        this.chiPhiSuaChua = chiPhiSuaChua;
//        this.trangThai = trangThai;
//    }
//
//    public String getMaPhieu() {
//		return maPhieu;
//	}
//
//	public void setMaPhieu(String maPhieu) {
//		this.maPhieu = maPhieu;
//	}
//
//	public String getSoSerial() {
//		return soSerial;
//	}
//
//	public void setSoSerial(String soSerial) {
//		this.soSerial = soSerial;
//	}
//
//	public String getLoaiBaoHanh() {
//		return loaiBaoHanh;
//	}
//
//	public void setLoaiBaoHanh(String loaiBaoHanh) {
//		this.loaiBaoHanh = loaiBaoHanh;
//	}
//
//	public SanPham getSp() {
//		return sp;
//	}
//
//	public void setSp(SanPham sp) {
//		this.sp = sp;
//	}
//
//	public KhachHang getKh() {
//		return kh;
//	}
//
//	public void setKh(KhachHang kh) {
//		this.kh = kh;
//	}
//
//	public NhanVien getNv() {
//		return nv;
//	}
//
//	public void setNv(NhanVien nv) {
//		this.nv = nv;
//	}
//
//	public Date getNgayTiepNhan() {
//		return ngayTiepNhan;
//	}
//
//	public void setNgayTiepNhan(Date ngayTiepNhan) {
//		this.ngayTiepNhan = ngayTiepNhan;
//	}
//
//	public Date getNgayHenTra() {
//		return ngayHenTra;
//	}
//
//	public void setNgayHenTra(Date ngayHenTra) {
//		this.ngayHenTra = ngayHenTra;
//	}
//
//	public String getGhiChu() {
//		return ghiChu;
//	}
//
//	public void setGhiChu(String ghiChu) {
//		this.ghiChu = ghiChu;
//	}
//
//	public double getChiPhiSuaChua() {
//		return chiPhiSuaChua;
//	}
//
//	public void setChiPhiSuaChua(double chiPhiSuaChua) {
//		this.chiPhiSuaChua = chiPhiSuaChua;
//	}
//
//	public String getTrangThai() {
//		return trangThai;
//	}
//
//	public void setTrangThai(String trangThai) {
//		this.trangThai = trangThai;
//	}
//
//	@Override
//    public String toString() {
//        return "PhieuBaoHanh{" +
//                "maPhieu='" + maPhieu + '\'' +
//                ", soSerial='" + soSerial + '\'' +
//                ", sp=" + sp +
//                ", kh=" + kh +
//                ", nv=" + nv +
//                ", ngayTiepNhan=" + ngayTiepNhan +
//                ", ngayHenTra=" + ngayHenTra +
//                ", moTa='" + ghiChu + '\'' +
//                ", chiPhiSuaChua=" + chiPhiSuaChua +
//                ", trangThai='" + trangThai + '\'' +
//                '}';
//    }
//}