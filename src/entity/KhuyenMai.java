package entity;

public class KhuyenMai {
    private String maKhuyenMai, tenKhuyenMai, noiDungMau;

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, String noiDungMau) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.noiDungMau = noiDungMau;
    }

    public String getMaKhuyenMai() { return maKhuyenMai; }
    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public String getNoiDungMau() { return noiDungMau; }
    @Override public String toString() { return tenKhuyenMai; }
}
