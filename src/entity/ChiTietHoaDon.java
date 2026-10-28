package entity;

public class ChiTietHoaDon {
    private String maSKU, tenSanPham, maDanhMuc;
    private int soLuong;
    private double donGia;

    public ChiTietHoaDon(SanPham sp, int soLuong, double donGia) {
        this.maSKU = sp.getMaSKU();
        this.tenSanPham = sp.getTenSanPham();
        this.maDanhMuc = sp.getDanhMuc().getMaDanhMuc();
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaSKU() { return maSKU; }
    public String getTenSanPham() { return tenSanPham; }
    public String getMaDanhMuc() { return maDanhMuc; }
    public int getSoLuong() { return soLuong; }
    public double getDonGia() { return donGia; }
    public double getThanhTien() { return soLuong * donGia; }
}
