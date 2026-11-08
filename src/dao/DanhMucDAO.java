package dao;

import entity.DanhMuc;
import entity.SanPham;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** Thao tác danh mục trên ArrayList; chưa có câu lệnh SQL hay kết nối JDBC. */
public class DanhMucDAO {
    private final DuLieuTam duLieu;

    public DanhMucDAO() { this(DuLieuTam.getMacDinh()); }
    public DanhMucDAO(DuLieuTam duLieu) { this.duLieu = duLieu; }

    public ArrayList<DanhMuc> layTatCa() {
        ArrayList<DanhMuc> ketQua = new ArrayList<>();
        for (DanhMuc dm : duLieu.getDanhMuc()) ketQua.add(new DanhMuc(dm));
        Collections.sort(ketQua, new Comparator<DanhMuc>() {
            @Override public int compare(DanhMuc a, DanhMuc b) {
                return SanPhamDAO.chuanHoaTimKiem(a.getTen()).compareTo(SanPhamDAO.chuanHoaTimKiem(b.getTen()));
            }
        });
        return ketQua;
    }

    public DanhMuc timTheoMa(String id) {
        for (DanhMuc dm : duLieu.getDanhMuc()) {
            if (dm.getId().equals(id)) return new DanhMuc(dm);
        }
        return null;
    }

    public String layTen(String id) {
        DanhMuc dm = timTheoMa(id);
        return dm == null ? "" : dm.getTen();
    }

    public int demSanPham(String id) {
        int soLuong = 0;
        for (SanPham sp : duLieu.getSanPham()) {
            if (sp.getDanhMucId().equals(id)) soLuong++;
        }
        return soLuong;
    }

    private DanhMuc kiemTra(DanhMuc danhMuc) {
        if (danhMuc == null) throw new IllegalArgumentException("Chưa nhập danh mục.");
        String ten = danhMuc.getTen() == null ? "" : danhMuc.getTen().trim().replaceAll("\\s+", " ");
        String moTa = danhMuc.getMoTa() == null ? "" : danhMuc.getMoTa().trim();
        if (ten.length() < 2 || ten.length() > 80) throw new IllegalArgumentException("Tên danh mục phải có từ 2 đến 80 ký tự.");
        if (moTa.length() > 1000) throw new IllegalArgumentException("Mô tả danh mục không vượt quá 1.000 ký tự.");
        for (DanhMuc dm : duLieu.getDanhMuc()) {
            if (!dm.getId().equals(danhMuc.getId()) && SanPhamDAO.chuanHoaTimKiem(dm.getTen()).equals(SanPhamDAO.chuanHoaTimKiem(ten))) {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
            }
        }
        return new DanhMuc(danhMuc.getId(), ten, moTa);
    }

    public DanhMuc them(DanhMuc danhMuc) {
        if (danhMuc == null) throw new IllegalArgumentException("Chưa nhập danh mục.");
        DanhMuc moi = kiemTra(new DanhMuc(null, danhMuc.getTen(), danhMuc.getMoTa()));
        moi.setId(duLieu.taoMaDanhMuc());
        duLieu.getDanhMuc().add(moi);
        return new DanhMuc(moi);
    }

    public DanhMuc capNhat(DanhMuc danhMuc) {
        if (danhMuc == null || timTheoMa(danhMuc.getId()) == null) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
        DanhMuc moi = kiemTra(danhMuc);
        for (int i = 0; i < duLieu.getDanhMuc().size(); i++) {
            if (duLieu.getDanhMuc().get(i).getId().equals(moi.getId())) {
                duLieu.getDanhMuc().set(i, moi);
                return new DanhMuc(moi);
            }
        }
        throw new IllegalArgumentException("Danh mục không còn tồn tại.");
    }

    public void xoa(String id) {
        if (timTheoMa(id) == null) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
        if (demSanPham(id) > 0) throw new IllegalArgumentException("Không thể xóa danh mục đang có sản phẩm, kể cả sản phẩm ngừng kinh doanh.");
        for (int i = 0; i < duLieu.getDanhMuc().size(); i++) {
            if (duLieu.getDanhMuc().get(i).getId().equals(id)) { duLieu.getDanhMuc().remove(i); return; }
        }
    }
}
