package entity;

import java.util.Objects;

public class ChiTietHoaDon {
    private int soLuong;
    private double donGia;
    private double thanhTien;
    private SanPham sanPham;
    private HoaDon hoaDon;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(int soLuong, double donGia, SanPham sp, HoaDon hd) {
        setSoLuong(soLuong);
        setDonGia(donGia);
        this.sanPham = sp;
        this.hoaDon = hd;
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
        return sanPham;
    }

    public void setSp(SanPham sp) {
        this.sanPham = sp;
    }

    public HoaDon getHd() {
        return hoaDon;
    }

    public void setHd(HoaDon hd) {
        this.hoaDon = hd;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon [soLuong=" + soLuong + ", donGia=" + donGia + ", thanhTien=" + thanhTien 
                + ", sp=" + (sanPham != null ? sanPham.getTenSP() : null) + "]";
    }
}