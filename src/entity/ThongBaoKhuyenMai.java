package entity;

import java.time.LocalDateTime;

/** Dữ liệu trong phiên chạy; DAO trả về bản sao để tránh thay đổi ngoài ý muốn. */
public class ThongBaoKhuyenMai {
    private String id;
    private String khachHangId;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime taoLuc;

    public ThongBaoKhuyenMai() { }

    public ThongBaoKhuyenMai(String id, String khachHangId, String tieuDe, String noiDung, LocalDateTime taoLuc) {
        this.id = id;
        this.khachHangId = khachHangId;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.taoLuc = taoLuc;
    }

    public ThongBaoKhuyenMai(ThongBaoKhuyenMai other) {
        this(other.id, other.khachHangId, other.tieuDe, other.noiDung, other.taoLuc);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public LocalDateTime getTaoLuc() { return taoLuc; }
    public void setTaoLuc(LocalDateTime taoLuc) { this.taoLuc = taoLuc; }
}
