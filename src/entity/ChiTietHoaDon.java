package entity;

import java.util.Objects;

public class ChiTietHoaDon {
    private int soLuong;
    private double donGia;
    private double thanhTien;
    private SanPham sp;
    private HoaDon hd;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(int soLuong, double donGia, SanPham sp, HoaDon hd) {
        setSoLuong(soLuong);
        setDonGia(donGia);
        this.sp = sp;
        this.hd = hd;
        this.thanhTien = tinhThanhTien();
    }

    public double tinhThanhTien() {
        return this.soLuong * this.donGia;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0!");
        }
        this.soLuong = soLuong;
        this.thanhTien = tinhThanhTien();
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        if (donGia < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm!");
        }
        this.donGia = donGia;
        this.thanhTien = tinhThanhTien();
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public SanPham getSp() {
        return sp;
    }

    public void setSp(SanPham sp) {
        this.sp = sp;
    }

    public HoaDon getHd() {
        return hd;
    }

    public void setHd(HoaDon hd) {
        this.hd = hd;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon [soLuong=" + soLuong + ", donGia=" + donGia + ", thanhTien=" + thanhTien 
                + ", sp=" + (sp != null ? sp.getTen() : null) + "]";
    }
}