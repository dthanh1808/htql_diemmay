package dao;

import entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/** Nghiệp vụ khách hàng trên ArrayList, chưa sử dụng SQL. */
public class KhachHangDAO {
    private final DuLieuTam duLieu;
    public KhachHangDAO() { this(DuLieuTam.getMacDinh()); }
    public KhachHangDAO(DuLieuTam duLieu) { this.duLieu = duLieu; }

    public ArrayList<KhachHang> layTatCa() {
        ArrayList<KhachHang> result = new ArrayList<>();
        for (KhachHang kh : duLieu.getKhachHang()) result.add(new KhachHang(kh));
        return result;
    }

    public KhachHang timTheoMa(String id) {
        for (KhachHang kh : duLieu.getKhachHang()) if (kh.getId().equals(id)) return new KhachHang(kh);
        return null;
    }

    private KhachHang batBuoc(String id) {
        KhachHang kh = timTheoMa(id);
        if (kh == null) throw new IllegalArgumentException("Khách hàng không còn tồn tại.");
        return kh;
    }

    public KhachHang them(KhachHang input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập khách hàng.");
        KhachHang kh = new KhachHang(input);
        kh.setId(null);
        kiemTra(kh);
        kh.setId(duLieu.taoMaKhachHang());
        kh.setDangHoatDong(true);
        kh.setTaoLuc(LocalDateTime.now()); kh.setCapNhatLuc(kh.getTaoLuc());
        duLieu.getKhachHang().add(kh);
        ghiLichSu(kh.getId(), "Đăng ký", "Đăng ký khách hàng");
        return new KhachHang(kh);
    }

    public KhachHang capNhat(KhachHang input) {
        if (input == null) throw new IllegalArgumentException("Chưa nhập khách hàng.");
        KhachHang cu = batBuoc(input.getId());
        KhachHang kh = new KhachHang(input);
        kiemTra(kh);
        kh.setTaoLuc(cu.getTaoLuc()); kh.setCapNhatLuc(LocalDateTime.now());
        for (int i = 0; i < duLieu.getKhachHang().size(); i++) {
            if (duLieu.getKhachHang().get(i).getId().equals(kh.getId())) {
                duLieu.getKhachHang().set(i, kh); break;
            }
        }
        String thayDoi = "Cập nhật hồ sơ";
        if (!Objects.equals(cu.getHoTen(), kh.getHoTen())) thayDoi += "; họ tên: " + cu.getHoTen() + " → " + kh.getHoTen();
        if (!Objects.equals(cu.getSoDienThoai(), kh.getSoDienThoai())) thayDoi += "; đổi số điện thoại";
        if (!Objects.equals(cu.getEmail(), kh.getEmail())) thayDoi += "; đổi email";
        if (!Objects.equals(cu.getDiaChi(), kh.getDiaChi())) thayDoi += "; đổi địa chỉ";
        if (!Objects.equals(cu.getNgaySinh(), kh.getNgaySinh())) thayDoi += "; đổi ngày sinh";
        if (!Objects.equals(cu.getGhiChu(), kh.getGhiChu())) thayDoi += "; đổi ghi chú";
        if (cu.isDangHoatDong() != kh.isDangHoatDong()) thayDoi += kh.isDangHoatDong() ? "; kích hoạt lại" : "; ngừng hoạt động";
        if (cu.isNhanKhuyenMai() != kh.isNhanKhuyenMai()) thayDoi += kh.isNhanKhuyenMai() ? "; đồng ý nhận khuyến mãi" : "; từ chối nhận khuyến mãi";
        ghiLichSu(kh.getId(), "Cập nhật", thayDoi);
        return new KhachHang(kh);
    }

    public boolean coTheXoa(String id) {
        KhachHang kh = batBuoc(id);
        return !kh.isDangHoatDong() && layGiaoDich(id).isEmpty() && layThongBao(id).isEmpty();
    }

    public void xoa(String id) {
        if (!coTheXoa(id)) throw new IllegalArgumentException("Chỉ xóa khách đã ngừng hoạt động, chưa có giao dịch và thông báo. Hãy giữ hồ sơ có lịch sử nghiệp vụ.");
        duLieu.getKhachHang().removeIf(kh -> kh.getId().equals(id));
        duLieu.getLichSuKhachHang().removeIf(ls -> ls.getKhachHangId().equals(id));
    }

    private void kiemTra(KhachHang kh) {
        kh.setHoTen(chuoi(kh.getHoTen()).replaceAll("\\s+", " "));
        kh.setSoDienThoai(chuoi(kh.getSoDienThoai()).replaceAll("[ .()-]", ""));
        if (kh.getSoDienThoai().startsWith("+84")) kh.setSoDienThoai("0" + kh.getSoDienThoai().substring(3));
        kh.setEmail(chuoi(kh.getEmail()).toLowerCase(Locale.ROOT));
        kh.setDiaChi(chuoi(kh.getDiaChi())); kh.setGhiChu(chuoi(kh.getGhiChu()));
        if (kh.getHoTen().length() < 2 || kh.getHoTen().length() > 100 || !kh.getHoTen().matches("[\\p{L}\\p{M} .'-]+") || !kh.getHoTen().matches(".*\\p{L}.*"))
            throw new IllegalArgumentException("Họ tên từ 2–100 ký tự, không chứa chữ số hoặc ký tự lạ.");
        if (!kh.getSoDienThoai().matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số điện thoại di động gồm 10 chữ số, đầu 03/05/07/08/09 (hoặc +84).");
        if (!kh.getEmail().isEmpty() && (kh.getEmail().length() > 254 || kh.getEmail().startsWith(".") || kh.getEmail().contains(".@") || kh.getEmail().contains("..") || !kh.getEmail().matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+")))
            throw new IllegalArgumentException("Email không hợp lệ.");
        if (kh.getDiaChi().length() > 300 || kh.getGhiChu().length() > 1000)
            throw new IllegalArgumentException("Địa chỉ tối đa 300 ký tự, ghi chú tối đa 1.000 ký tự.");
        if (kh.getNgaySinh() != null && (kh.getNgaySinh().isAfter(LocalDate.now()) || kh.getNgaySinh().isBefore(LocalDate.now().minusYears(120))))
            throw new IllegalArgumentException("Ngày sinh phải trong vòng 120 năm và không ở tương lai.");
        for (KhachHang cu : duLieu.getKhachHang()) {
            if (cu.getId().equals(kh.getId())) continue;
            if (cu.getSoDienThoai().equals(kh.getSoDienThoai())) throw new IllegalArgumentException("Số điện thoại đã được đăng ký, kể cả hồ sơ ngừng hoạt động.");
            if (!kh.getEmail().isEmpty() && cu.getEmail().equalsIgnoreCase(kh.getEmail())) throw new IllegalArgumentException("Email đã được đăng ký.");
        }
    }

    private static String chuoi(String text) { return text == null ? "" : text.trim(); }

    private void ghiLichSu(String id, String loai, String noiDung) {
        duLieu.getLichSuKhachHang().add(new LichSuKhachHang(id, LocalDateTime.now(), loai, noiDung));
    }

    public ArrayList<LichSuKhachHang> layLichSu(String id) {
        batBuoc(id);
        ArrayList<LichSuKhachHang> result = new ArrayList<>();
        for (LichSuKhachHang ls : duLieu.getLichSuKhachHang()) if (ls.getKhachHangId().equals(id)) result.add(new LichSuKhachHang(ls));
        Collections.reverse(result); // Cùng thời điểm: hoạt động thêm sau đứng trước.
        result.sort(Comparator.comparing(LichSuKhachHang::getThoiGian).reversed());
        return result;
    }

    public ArrayList<GiaoDichKhachHang> layGiaoDich(String id) {
        batBuoc(id);
        ArrayList<GiaoDichKhachHang> result = new ArrayList<>();
        for (GiaoDichKhachHang gd : duLieu.getGiaoDichKhachHang()) if (gd.getKhachHangId().equals(id)) result.add(new GiaoDichKhachHang(gd));
        Collections.reverse(result);
        result.sort(Comparator.comparing(GiaoDichKhachHang::getThoiGian).reversed());
        return result;
    }

    /** Điểm tích hợp cho module hóa đơn; UI hồ sơ không cho sửa tổng chi tiêu bằng tay. */
    public void ghiNhanMuaHang(String id, String maHoaDon, BigDecimal soTien, String noiDung) {
        KhachHang kh = batBuoc(id);
        if (!kh.isDangHoatDong()) throw new IllegalArgumentException("Khách hàng đã ngừng hoạt động.");
        String ma = chuoi(maHoaDon).toUpperCase(Locale.ROOT);
        if (!ma.matches("[A-Z0-9_-]{2,40}")) throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        kiemTraTien(soTien, false);
        for (GiaoDichKhachHang gd : duLieu.getGiaoDichKhachHang()) if (gd.getMaHoaDon().equalsIgnoreCase(ma))
            throw new IllegalArgumentException("Hóa đơn này đã được ghi nhận.");
        if (chuoi(noiDung).length() > 500) throw new IllegalArgumentException("Nội dung giao dịch tối đa 500 ký tự.");
        duLieu.getGiaoDichKhachHang().add(new GiaoDichKhachHang(ma, id, LocalDateTime.now(), soTien, chuoi(noiDung)));
        ghiLichSu(id, "Mua hàng", "Hóa đơn " + ma + ": " + soTien.toPlainString() + " VND");
    }

    public BigDecimal tongChiTieu(String id) {
        BigDecimal tong = BigDecimal.ZERO;
        for (GiaoDichKhachHang gd : layGiaoDich(id)) tong = tong.add(gd.getSoTien());
        return tong;
    }

    public ArrayList<HangThanhVien> layChinhSach() {
        ArrayList<HangThanhVien> result = new ArrayList<>();
        for (HangThanhVien hang : duLieu.getHangThanhVien()) result.add(new HangThanhVien(hang));
        return result;
    }

    public HangThanhVien layHang(String id) {
        BigDecimal tong = tongChiTieu(id);
        HangThanhVien result = duLieu.getHangThanhVien().get(0);
        for (HangThanhVien hang : duLieu.getHangThanhVien()) if (tong.compareTo(hang.getChiTieuToiThieu()) >= 0) result = hang;
        return new HangThanhVien(result);
    }

    public void luuChinhSach(ArrayList<HangThanhVien> input) {
        String[] ten = {"Member", "Silver", "Gold", "Diamond"};
        if (input == null || input.size() != ten.length) throw new IllegalArgumentException("Chính sách phải có đủ 4 hạng.");
        ArrayList<HangThanhVien> moi = new ArrayList<>();
        for (int i = 0; i < ten.length; i++) {
            HangThanhVien h = input.get(i);
            if (h == null || !ten[i].equals(h.getTen())) throw new IllegalArgumentException("Thứ tự hạng: Member, Silver, Gold, Diamond.");
            kiemTraTien(h.getChiTieuToiThieu(), true);
            if (i == 0 && h.getChiTieuToiThieu().signum() != 0) throw new IllegalArgumentException("Hạng Member bắt đầu từ 0 đ.");
            if (i > 0 && h.getChiTieuToiThieu().compareTo(input.get(i - 1).getChiTieuToiThieu()) <= 0)
                throw new IllegalArgumentException("Ngưỡng chi tiêu phải tăng dần theo hạng.");
            if (h.getPhanTramUuDai() < 0 || h.getPhanTramUuDai() > 100 || (i > 0 && h.getPhanTramUuDai() < input.get(i - 1).getPhanTramUuDai()))
                throw new IllegalArgumentException("Ưu đãi từ 0–100% và không giảm ở hạng cao hơn.");
            moi.add(new HangThanhVien(h));
        }
        duLieu.getHangThanhVien().clear(); duLieu.getHangThanhVien().addAll(moi);
        for (KhachHang kh : duLieu.getKhachHang()) ghiLichSu(kh.getId(), "Chính sách", "Cập nhật chính sách hạng thành viên; hạng được tính lại theo tổng chi tiêu");
    }

    private static void kiemTraTien(BigDecimal tien, boolean choPhepKhong) {
        if (tien == null || tien.signum() < (choPhepKhong ? 0 : 1) || tien.stripTrailingZeros().scale() > 0 || tien.compareTo(new BigDecimal("999999999999")) > 0)
            throw new IllegalArgumentException("Số tiền phải là VND nguyên " + (choPhepKhong ? "không âm" : "dương") + ", tối đa 999.999.999.999 đ.");
    }

    public ArrayList<KhachHang> timKiem(String tuKhoa, String hang, Boolean hoatDong, LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        String[] tokens = SanPhamDAO.chuanHoaTimKiem(chuoi(tuKhoa)).split("\\s+");
        ArrayList<KhachHang> result = new ArrayList<>();
        for (KhachHang kh : layTatCa()) {
            if (hoatDong != null && hoatDong != kh.isDangHoatDong()) continue;
            if (hang != null && !hang.equals(layHang(kh.getId()).getTen())) continue;
            LocalDate ngay = kh.getTaoLuc().toLocalDate();
            if (tuNgay != null && ngay.isBefore(tuNgay) || denNgay != null && ngay.isAfter(denNgay)) continue;
            String text = SanPhamDAO.chuanHoaTimKiem(kh.getId() + " " + kh.getHoTen() + " " + kh.getSoDienThoai() + " " + kh.getEmail());
            boolean khop = true;
            for (String token : tokens) if (!text.contains(token)) { khop = false; break; }
            if (khop) result.add(kh);
        }
        return result;
    }

    public void sapXep(ArrayList<KhachHang> ds, int kieu) {
        Collections.sort(ds, new Comparator<KhachHang>() {
            @Override public int compare(KhachHang a, KhachHang b) {
                int c;
                if (kieu == 2 || kieu == 3) {
                    c = tongChiTieu(a.getId()).compareTo(tongChiTieu(b.getId()));
                    if (kieu == 3) c = -c;
                } else if (kieu == 4) c = b.getTaoLuc().compareTo(a.getTaoLuc());
                else { c = SanPhamDAO.chuanHoaTimKiem(a.getHoTen()).compareTo(SanPhamDAO.chuanHoaTimKiem(b.getHoTen())); if (kieu == 1) c = -c; }
                return c == 0 ? a.getId().compareTo(b.getId()) : c;
            }
        });
    }

    public ArrayList<KhachHang> nguoiNhanKhuyenMai(String hang) {
        ArrayList<KhachHang> result = new ArrayList<>();
        for (KhachHang kh : timKiem("", hang, true, null, null)) if (kh.isNhanKhuyenMai()) result.add(kh);
        return result;
    }

    /** Chỉ tạo thông báo nội bộ, không giả lập việc gửi email/SMS thành công. */
    public int guiKhuyenMai(String hang, String tieuDe, String noiDung) {
        String td = chuoi(tieuDe), nd = chuoi(noiDung);
        if (td.isEmpty() || td.length() > 120 || nd.isEmpty() || nd.length() > 2000)
            throw new IllegalArgumentException("Nhập tiêu đề 1–120 ký tự và nội dung 1–2.000 ký tự.");
        ArrayList<KhachHang> ds = nguoiNhanKhuyenMai(hang);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không có khách hàng đủ điều kiện nhận thông báo.");
        for (KhachHang kh : ds) {
            duLieu.getThongBaoKhuyenMai().add(new ThongBaoKhuyenMai(duLieu.taoMaThongBao(), kh.getId(), td, nd, LocalDateTime.now()));
            ghiLichSu(kh.getId(), "Khuyến mãi", "Tạo thông báo nội bộ: " + td);
        }
        return ds.size();
    }

    public ArrayList<ThongBaoKhuyenMai> layThongBao(String id) {
        batBuoc(id);
        ArrayList<ThongBaoKhuyenMai> result = new ArrayList<>();
        for (ThongBaoKhuyenMai tb : duLieu.getThongBaoKhuyenMai()) if (tb.getKhachHangId().equals(id)) result.add(new ThongBaoKhuyenMai(tb));
        Collections.reverse(result);
        result.sort(Comparator.comparing(ThongBaoKhuyenMai::getTaoLuc).reversed());
        return result;
    }
}
