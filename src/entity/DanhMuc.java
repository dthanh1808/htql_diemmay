package entity;


/** Đối tượng dữ liệu, sử dụng constructor, getter và setter thông thường. */
public class DanhMuc {
    private String id;
    private String ten;
    private String moTa;

    public DanhMuc() { }

    public DanhMuc(String id, String ten, String moTa) {
        this.id = id;
        this.ten = ten;
        this.moTa = moTa;
    }

    public DanhMuc(DanhMuc other) {
        this(other.id, other.ten, other.moTa);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    @Override
    public String toString() { return ten; }
}
