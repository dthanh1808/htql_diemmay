package dao;

import entity.*;
import java.util.ArrayList;
import java.util.Locale;

public class SanPhamDAO {
    public ArrayList<SanPham> layTatCa() { return new ArrayList<>(DuLieuMau.sanPham); }

    public SanPham timTheoMa(String ma) {
        for (SanPham sp : DuLieuMau.sanPham) if (sp.getMaSKU().equalsIgnoreCase(ma)) return sp;
        return null;
    }

    public void them(SanPham sp) {
        kiemTra(sp, false);
        sp.setKinhDoanh(true);
        DuLieuMau.sanPham.add(sp);
    }

    public void sua(SanPham sp) {
        kiemTra(sp, true);
        SanPham cu = timTheoMa(sp.getMaSKU());
        DuLieuMau.sanPham.set(DuLieuMau.sanPham.indexOf(cu), sp);
    }

    public boolean coHoaDon(String ma) {
        for (HoaDon hd : DuLieuMau.hoaDon) {
            for (ChiTietHoaDon ct : hd.getChiTiet()) if (ct.getMaSKU().equalsIgnoreCase(ma)) return true;
        }
        return false;
    }

    /** Trả về true nếu xóa; false nếu chỉ chuyển sang ngưng kinh doanh. */
    public boolean xoa(String ma) {
        SanPham sp = timTheoMa(ma);
        if (sp == null) throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        if (coHoaDon(ma)) { sp.setKinhDoanh(false); return false; }
        DuLieuMau.sanPham.remove(sp);
        return true;
    }

    public void doiTrangThai(String ma) {
        SanPham sp = timTheoMa(ma);
        if (sp == null) throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        sp.setKinhDoanh(!sp.isKinhDoanh());
    }

    public ArrayList<SanPham> timKiem(String tuKhoa, String maDM, String hang, double giaTu, double giaDen,
            String trangThai, int baoHanh) {
        ArrayList<SanPham> ketQua = new ArrayList<>();
        String key = tuKhoa.trim().toLowerCase(Locale.ROOT);
        for (SanPham sp : DuLieuMau.sanPham) {
            String text = sp.getMaSKU() + " " + sp.getMaVach() + " " + sp.getTenSanPham() + " "
                + sp.getDanhMuc().getTenDanhMuc() + " " + sp.getThuongHieu();
            if (!text.toLowerCase(Locale.ROOT).contains(key)) continue;
            if (!maDM.isEmpty() && !sp.getDanhMuc().getMaDanhMuc().equals(maDM)) continue;
            if (!hang.isEmpty() && !sp.getThuongHieu().equals(hang)) continue;
            if (sp.getGiaBan() < giaTu || sp.getGiaBan() > giaDen) continue;
            if (trangThai.equals("Đang kinh doanh") && !sp.isKinhDoanh()) continue;
            if (trangThai.equals("Ngưng kinh doanh") && sp.isKinhDoanh()) continue;
            if (baoHanh > 0 && sp.getBaoHanh() != baoHanh) continue;
            ketQua.add(sp);
        }
        return ketQua;
    }

    private void kiemTra(SanPham sp, boolean sua) {
        if (sp.getMaSKU().isEmpty() || sp.getMaVach().isEmpty() || sp.getTenSanPham().isEmpty()
                || sp.getThuongHieu().isEmpty() || sp.getModel().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập SKU, mã vạch, tên, thương hiệu và model.");
        }
        if (sua && timTheoMa(sp.getMaSKU()) == null) throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        if (!sua && timTheoMa(sp.getMaSKU()) != null) throw new IllegalArgumentException("Mã SKU đã tồn tại.");
        for (SanPham other : DuLieuMau.sanPham) {
            if ((!sua || !other.getMaSKU().equalsIgnoreCase(sp.getMaSKU())) && other.getMaVach().equalsIgnoreCase(sp.getMaVach())) {
                throw new IllegalArgumentException("Mã vạch đã tồn tại.");
            }
        }
        if (sp.getDanhMuc() == null || new DanhMucDAO().timTheoMa(sp.getDanhMuc().getMaDanhMuc()) == null) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục hợp lệ.");
        }
        if (!Double.isFinite(sp.getGiaBan()) || sp.getGiaBan() <= 0) throw new IllegalArgumentException("Giá bán phải lớn hơn 0.");
        if (sp.getBaoHanh() < 0) throw new IllegalArgumentException("Bảo hành phải là số tháng không âm.");
        if (!"Gọn nhẹ".equals(sp.getNhomSanPham()) && !"Cồng kềnh".equals(sp.getNhomSanPham())) {
            throw new IllegalArgumentException("Vui lòng chọn nhóm sản phẩm.");
        }
    }
}
