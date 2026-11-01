package entity;

import java.math.BigDecimal;

/** Dữ liệu trong phiên chạy; DAO trả về bản sao để tránh thay đổi ngoài ý muốn. */
public class HangThanhVien {
    private String ten;
    private BigDecimal chiTieuToiThieu;
    private int phanTramUuDai;

    public HangThanhVien() { }

    public HangThanhVien(String ten, BigDecimal chiTieuToiThieu, int phanTramUuDai) {
        this.ten = ten;
        this.chiTieuToiThieu = chiTieuToiThieu;
        this.phanTramUuDai = phanTramUuDai;
    }

    public HangThanhVien(HangThanhVien other) {
        this(other.ten, other.chiTieuToiThieu, other.phanTramUuDai);
    }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public BigDecimal getChiTieuToiThieu() { return chiTieuToiThieu; }
    public void setChiTieuToiThieu(BigDecimal chiTieuToiThieu) { this.chiTieuToiThieu = chiTieuToiThieu; }

    public int getPhanTramUuDai() { return phanTramUuDai; }
    public void setPhanTramUuDai(int phanTramUuDai) { this.phanTramUuDai = phanTramUuDai; }

    @Override public String toString() { return ten; }
}
