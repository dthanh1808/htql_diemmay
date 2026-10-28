package entity;

public class SanPham {
    private String maSKU, maVach, tenSanPham, thuongHieu, model, nhomSanPham, moTa, hinhAnh;
    private DanhMuc danhMuc;
    private double giaBan;
    private int baoHanh;
    private boolean kinhDoanh;

    public SanPham(String maSKU, String maVach, String tenSanPham, DanhMuc danhMuc,
            String thuongHieu, String model, double giaBan, int baoHanh,
            String nhomSanPham, String moTa, String hinhAnh, boolean kinhDoanh) {
        this.maSKU = maSKU.trim();
        this.maVach = maVach.trim();
        this.tenSanPham = tenSanPham.trim();
        this.danhMuc = danhMuc;
        this.thuongHieu = thuongHieu.trim();
        this.model = model.trim();
        this.giaBan = giaBan;
        this.baoHanh = baoHanh;
        this.nhomSanPham = nhomSanPham;
        this.moTa = moTa.trim();
        this.hinhAnh = hinhAnh;
        this.kinhDoanh = kinhDoanh;
    }

    public String getMaSKU() { return maSKU; }
    public String getMaVach() { return maVach; }
    public String getTenSanPham() { return tenSanPham; }
    public DanhMuc getDanhMuc() { return danhMuc; }
    public String getThuongHieu() { return thuongHieu; }
    public String getModel() { return model; }
    public double getGiaBan() { return giaBan; }
    public int getBaoHanh() { return baoHanh; }
    public String getNhomSanPham() { return nhomSanPham; }
    public String getMoTa() { return moTa; }
    public String getHinhAnh() { return hinhAnh; }
    public boolean isKinhDoanh() { return kinhDoanh; }
    public void setKinhDoanh(boolean kinhDoanh) { this.kinhDoanh = kinhDoanh; }
}
