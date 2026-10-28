package dao;

import entity.DanhMuc;
import java.util.ArrayList;

public class DanhMucDAO {
    public ArrayList<DanhMuc> layTatCa() { return new ArrayList<>(DuLieuMau.danhMuc); }

    public DanhMuc timTheoMa(String ma) {
        for (DanhMuc dm : DuLieuMau.danhMuc) {
            if (dm.getMaDanhMuc().equals(ma)) return dm;
        }
        return null;
    }

    public void them(String ten) {
        kiemTraTen(ten, "");
        DuLieuMau.danhMuc.add(new DanhMuc(String.format("DM%03d", DuLieuMau.soDanhMuc++), ten.trim()));
    }

    public void sua(String ma, String ten) {
        DanhMuc dm = timTheoMa(ma);
        if (dm == null) throw new IllegalArgumentException("Danh mục không tồn tại.");
        kiemTraTen(ten, ma);
        dm.setTenDanhMuc(ten.trim());
    }

    public void xoa(String ma) {
        DanhMuc dm = timTheoMa(ma);
        if (dm == null) throw new IllegalArgumentException("Danh mục không tồn tại.");
        for (entity.SanPham sp : DuLieuMau.sanPham) {
            if (sp.getDanhMuc().getMaDanhMuc().equals(ma)) {
                throw new IllegalArgumentException("Danh mục đang có sản phẩm, không thể xóa.");
            }
        }
        for (entity.HoaDon hd : DuLieuMau.hoaDon) {
            for (entity.ChiTietHoaDon ct : hd.getChiTiet()) {
                if (ct.getMaDanhMuc().equals(ma)) throw new IllegalArgumentException("Danh mục đã có lịch sử mua hàng, không thể xóa.");
            }
        }
        DuLieuMau.danhMuc.remove(dm);
    }

    private void kiemTraTen(String ten, String maDangSua) {
        if (ten == null || ten.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên danh mục.");
        for (DanhMuc dm : DuLieuMau.danhMuc) {
            if (!dm.getMaDanhMuc().equals(maDangSua) && dm.getTenDanhMuc().equalsIgnoreCase(ten.trim())) {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
            }
        }
    }
}
