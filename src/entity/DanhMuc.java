package entity;

import java.util.Objects;

/** 
 * Entity DanhMuc 
 * Ánh xạ bảng DanhMuc trong CSDL QuanLyBanHang. 
 */
public class DanhMuc {
    private String maDanhMuc;  
    private String tenDanhMuc;  
    private String moTa;

    public DanhMuc() { }

    public DanhMuc(String maDanhMuc, String tenDanhMuc, String moTa) {
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.moTa = moTa;
    }

    public DanhMuc(DanhMuc other) {
        this(other.maDanhMuc, other.tenDanhMuc, other.moTa);
    }

    public String getMaDanhMuc() {
        return maDanhMuc;
    }

    public void setMaDanhMuc(String maDanhMuc) {
        this.maDanhMuc = maDanhMuc;
    }

    public String getMaDM() {
        return maDanhMuc;
    }

    public void setMaDM(String maDM) {
        this.maDanhMuc = maDM;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public String getTenDM() {
        return tenDanhMuc;
    }

    public void setTenDM(String tenDM) {
        this.tenDanhMuc = tenDM;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DanhMuc danhMuc = (DanhMuc) o;
        return Objects.equals(maDanhMuc, danhMuc.maDanhMuc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDanhMuc);
    }

    @Override
    public String toString() { 
        return tenDanhMuc != null ? tenDanhMuc : ""; 
    }
}