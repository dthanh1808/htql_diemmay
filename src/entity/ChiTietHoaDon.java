package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ChiTietHoaDon {
    private int soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private SanPham sanPham;
    private HoaDon hoaDon;

    public ChiTietHoaDon() {
        this.donGia = BigDecimal.ZERO;
        this.thanhTien = BigDecimal.ZERO;
    }

    public ChiTietHoaDon(int soLuong, BigDecimal donGia, SanPham sanPham, HoaDon hoaDon) {
        setSoLuong(soLuong);
        setDonGia(donGia);
        this.sanPham = sanPham;
        this.hoaDon = hoaDon;
    }

    // Tự động tính thành tiền: soLuong * donGia
    public BigDecimal tinhThanhTien() {
        if (this.donGia == null) return BigDecimal.ZERO;
        return this.donGia.multiply(BigDecimal.valueOf(this.soLuong)).setScale(0, RoundingMode.HALF_UP);
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

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        if (donGia == null || donGia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm!");
        }
        this.donGia = donGia.setScale(0, RoundingMode.HALF_UP);
        this.thanhTien = tinhThanhTien();
    }

    public BigDecimal getThanhTien() {
        if (thanhTien == null) {
            thanhTien = tinhThanhTien();
        }
        return thanhTien;
    }

    // Hỗ trợ set trực tiếp nếu cần thiết
    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien != null ? thanhTien.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon [soLuong=" + soLuong + ", donGia=" + donGia + ", thanhTien=" + thanhTien 
                + ", sanPham=" + (sanPham != null ? sanPham.getTenSP() : null) + "]";
    }
}