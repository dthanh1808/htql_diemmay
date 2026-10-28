package entity;

public class ChinhSachThanhVien {
    private String tenHang;
    private double chiTieuToiThieu, phanTramGiam;

    public ChinhSachThanhVien(String tenHang, double chiTieuToiThieu, double phanTramGiam) {
        this.tenHang = tenHang;
        this.chiTieuToiThieu = chiTieuToiThieu;
        this.phanTramGiam = phanTramGiam;
    }

    public String getTenHang() { return tenHang; }
    public double getChiTieuToiThieu() { return chiTieuToiThieu; }
    public double getPhanTramGiam() { return phanTramGiam; }
}
