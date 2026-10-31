package entity;

import java.util.List;

public record DuLieuSanPham(long phienBan, List<DanhMuc> danhMuc, List<SanPham> sanPham) {
    public DuLieuSanPham {
        danhMuc = List.copyOf(danhMuc);
        sanPham = List.copyOf(sanPham);
    }
}
