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

    public SanPham() { }

    public SanPham(String id, String sku, String ten, String danhMucId, String thuongHieu,
            BigDecimal giaBan, int baoHanhThang, boolean dangKinhDoanh, String moTa,
            String anhBase64, LocalDateTime taoLuc, LocalDateTime capNhatLuc) {
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
        this(other.id, other.sku, other.ten, other.danhMucId, other.thuongHieu, other.giaBan,
                other.baoHanhThang, other.dangKinhDoanh, other.moTa, other.anhBase64,
                other.taoLuc, other.capNhatLuc);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getDanhMucId() { return danhMucId; }
    public void setDanhMucId(String danhMucId) { this.danhMucId = danhMucId; }

    public String getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(String thuongHieu) { this.thuongHieu = thuongHieu; }

    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }

    public int getBaoHanhThang() { return baoHanhThang; }
    public void setBaoHanhThang(int baoHanhThang) { this.baoHanhThang = baoHanhThang; }

    public boolean isDangKinhDoanh() { return dangKinhDoanh; }
    public void setDangKinhDoanh(boolean dangKinhDoanh) { this.dangKinhDoanh = dangKinhDoanh; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getAnhBase64() { return anhBase64; }
    public void setAnhBase64(String anhBase64) { this.anhBase64 = anhBase64; }

    public LocalDateTime getTaoLuc() { return taoLuc; }
    public void setTaoLuc(LocalDateTime taoLuc) { this.taoLuc = taoLuc; }

    public LocalDateTime getCapNhatLuc() { return capNhatLuc; }
    public void setCapNhatLuc(LocalDateTime capNhatLuc) { this.capNhatLuc = capNhatLuc; }
}
