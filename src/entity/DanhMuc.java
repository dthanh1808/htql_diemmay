package entity;


/** Đối tượng dữ liệu, sử dụng constructor, getter và setter thông thường. */
public class DanhMuc {
    private String maDM;
    private String tenDM;
    private String moTa;

    public DanhMuc() { }

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
}
