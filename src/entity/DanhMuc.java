package entity;


/** Đối tượng dữ liệu, sử dụng constructor, getter và setter thông thường. */
public class DanhMuc {
<<<<<<< HEAD
    private String maDM;
    private String tenDM;
=======
    private String id;
    private String ten;
>>>>>>> 957a42b1ddd343adedd740681bfd7a592402b483
    private String moTa;

    public DanhMuc() { }

<<<<<<< HEAD
    public DanhMuc(String maDM, String tenDM, String moTa) {
		super();
		this.maDM = maDM;
		this.tenDM = tenDM;
		this.moTa = moTa;
	}

	public DanhMuc(DanhMuc other) {
        this(other.maDM, other.tenDM, other.moTa);
    }

    public String getMaDM() {
		return maDM;
	}

	public void setMaDM(String maDM) {
		this.maDM = maDM;
	}

	public String getTenDM() {
		return tenDM;
	}

	public void setTenDM(String tenDM) {
		this.tenDM = tenDM;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	@Override
    public String toString() { return tenDM; }
=======
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
>>>>>>> 957a42b1ddd343adedd740681bfd7a592402b483
}
