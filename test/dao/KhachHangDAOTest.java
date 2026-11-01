package dao;

import entity.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;

/** Kiểm tra ràng buộc nghiệp vụ, tính độc lập dữ liệu và các biên chi tiêu. */
public class KhachHangDAOTest {
    private static int checks;
    private static void check(boolean ok, String label) { checks++; if (!ok) throw new AssertionError(label); }
    private static void reject(Runnable action, String label) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException e) { rejected = true; }
        check(rejected, label);
    }
    private static KhachHang customer(String phone) {
        return new KhachHang(null, "Khách Kiểm Thử", phone, "", "", null, true, false, "", null, null);
    }
    public static void main(String[] args) {
        DuLieuTam data = new DuLieuTam(); KhachHangDAO dao = new KhachHangDAO(data);
        check(dao.layTatCa().size() == 8, "Seed customers");
        KhachHang first = dao.layTatCa().get(0);
        check(dao.tongChiTieu(first.getId()).compareTo(new BigDecimal("12850000")) == 0, "Spending from transactions");
        check(dao.layHang(first.getId()).getTen().equals("Diamond"), "Tier derived from spending");
        check(dao.timKiem("NGUYEN TU", null, null, null, null).size() == 1, "Accent insensitive multi token search");
        check(dao.timKiem("0903458789", "Diamond", true, null, null).size() == 1, "Combined filters");
        LocalDate joined = first.getTaoLuc().toLocalDate();
        check(dao.timKiem(first.getId(), null, null, joined, joined).size() == 1, "Inclusive date boundaries");
        reject(() -> dao.timKiem("", null, null, joined.plusDays(1), joined), "Reject reversed range");
        reject(() -> dao.them(null), "Reject null");
        KhachHang draft = customer("+84 912 345 678"); draft.setEmail("  NEW@EXAMPLE.COM ");
        draft.setId("FAKE"); draft.setTaoLuc(LocalDateTime.of(2000, 1, 1, 0, 0)); draft.setDangHoatDong(false);
        KhachHang added = dao.them(draft); String id = added.getId();
        check(added.getSoDienThoai().equals("0912345678") && added.getEmail().equals("new@example.com"), "Normalize phone and email");
        check(!id.equals("FAKE") && added.isDangHoatDong() && added.getTaoLuc().getYear() == LocalDate.now().getYear(), "Server owned id, registration time and active status");
        check(dao.tongChiTieu(id).signum() == 0 && dao.layHang(id).getTen().equals("Member"), "New member starts at zero");
        added.setHoTen("Sửa bản sao"); check(!dao.timTheoMa(id).getHoTen().equals(added.getHoTen()), "Copy protects stored entity");
        reject(() -> dao.them(customer("0912345678")), "Duplicate canonical phone");
        KhachHang bad = customer("0912345679"); bad.setEmail("NEW@EXAMPLE.COM");
        reject(() -> dao.them(bad), "Duplicate email case insensitive");
        bad.setEmail("bad@@example.com"); reject(() -> dao.them(bad), "Invalid email");
        bad.setEmail("bad..email@example.com"); reject(() -> dao.them(bad), "Email consecutive dots");
        bad.setEmail(""); bad.setSoDienThoai("0123456789"); reject(() -> dao.them(bad), "Invalid mobile prefix");
        bad.setSoDienThoai("0912345679"); bad.setHoTen("Khách 123"); reject(() -> dao.them(bad), "Invalid name");
        bad.setHoTen("--"); reject(() -> dao.them(bad), "Name needs letters");
        bad.setHoTen("Khách Hợp Lệ"); bad.setNgaySinh(LocalDate.now().plusDays(1)); reject(() -> dao.them(bad), "Future birthday");
        bad.setNgaySinh(LocalDate.now().minusYears(121)); reject(() -> dao.them(bad), "Unreasonable birthday");
        check(dao.layTatCa().size() == 9, "Invalid writes do not mutate store");
        reject(() -> dao.xoa(id), "Cannot delete active customer");
        LocalDateTime created = dao.timTheoMa(id).getTaoLuc();
        added = dao.timTheoMa(id); added.setDangHoatDong(false); added.setTaoLuc(LocalDateTime.MIN); dao.capNhat(added);
        check(dao.timTheoMa(id).getTaoLuc().equals(created), "Creation metadata preserved");
        reject(() -> dao.them(customer("0912345678")), "Inactive customer retains phone uniqueness");
        check(dao.coTheXoa(id), "Inactive unused customer deletable");
        dao.xoa(id); check(dao.timTheoMa(id) == null, "Delete unused customer");
        added = dao.them(customer("0912345678")); String purchaseId = added.getId();
        check(!id.equals(purchaseId), "Deleted ids not reused");
        dao.ghiNhanMuaHang(purchaseId, "HD-TEST", new BigDecimal("1000000"), "Giao dịch thử");
        check(dao.layHang(purchaseId).getTen().equals("Silver"), "Exact tier threshold");
        reject(() -> dao.ghiNhanMuaHang(purchaseId, "hd-test", BigDecimal.ONE, ""), "Duplicate invoice rejected case insensitive");
        reject(() -> dao.ghiNhanMuaHang(purchaseId, "HD-2", new BigDecimal("1.5"), ""), "Fractional VND rejected");
        reject(() -> dao.ghiNhanMuaHang(purchaseId, "HD-2", BigDecimal.ZERO, ""), "Zero purchase rejected");
        check(dao.layGiaoDich(purchaseId).size() == 1, "Invalid purchase has no side effect");
        added = dao.timTheoMa(purchaseId); added.setDangHoatDong(false); dao.capNhat(added);
        reject(() -> dao.xoa(purchaseId), "Cannot delete customer with purchases");
        reject(() -> dao.ghiNhanMuaHang(purchaseId, "HD-3", BigDecimal.ONE, ""), "Inactive purchase blocked");
        ArrayList<HangThanhVien> policy = dao.layChinhSach(); policy.get(1).setChiTieuToiThieu(new BigDecimal("2000000"));
        dao.luuChinhSach(policy);
        check(dao.layHang(purchaseId).getTen().equals("Member"), "Policy recomputes existing tiers");
        policy.get(1).setChiTieuToiThieu(new BigDecimal("3000000"));
        final ArrayList<HangThanhVien> equalThresholds = policy;
        reject(() -> dao.luuChinhSach(equalThresholds), "Equal thresholds rejected");
        check(dao.layChinhSach().get(1).getChiTieuToiThieu().equals(new BigDecimal("2000000")), "Invalid policy atomic");
        policy = dao.layChinhSach(); policy.get(2).setPhanTramUuDai(101); final ArrayList<HangThanhVien> invalid = policy;
        reject(() -> dao.luuChinhSach(invalid), "Percent range");
        policy = dao.layChinhSach(); policy.get(0).setChiTieuToiThieu(BigDecimal.ONE); final ArrayList<HangThanhVien> invalidBase = policy;
        reject(() -> dao.luuChinhSach(invalidBase), "Member must begin at zero");
        check(dao.nguoiNhanKhuyenMai(null).size() == 6, "Recipients exclude inactive and nonconsenting customers");
        reject(() -> dao.guiKhuyenMai(null, "", "Nội dung"), "Empty title rejected");
        reject(() -> dao.guiKhuyenMai("Unknown", "Tiêu đề", "Nội dung"), "Empty recipient group rejected");
        check(dao.guiKhuyenMai(null, "Ưu đãi", "Nội dung ưu đãi") == 6, "Create one local notification per eligible customer");
        check(dao.layThongBao(first.getId()).size() == 1 && dao.layLichSu(first.getId()).get(0).getLoai().equals("Khuyến mãi"), "Notification and audit linked");
        String stoppedId = dao.layTatCa().get(4).getId(), noConsentId = dao.layTatCa().get(6).getId();
        check(dao.layThongBao(stoppedId).isEmpty() && dao.layThongBao(noConsentId).isEmpty(), "No notification to excluded customers");
        ThongBaoKhuyenMai copy = dao.layThongBao(first.getId()).get(0); copy.setNoiDung("X");
        check(!dao.layThongBao(first.getId()).get(0).getNoiDung().equals("X"), "Notification copy");
        KhachHang recipient = dao.them(customer("0912345600")); recipient.setNhanKhuyenMai(true); dao.capNhat(recipient);
        dao.guiKhuyenMai("Member", "Tin mới", "Nội dung"); recipient.setDangHoatDong(false); dao.capNhat(recipient);
        final String recipientId = recipient.getId(); reject(() -> dao.xoa(recipientId), "Notification history prevents deletion");
        ArrayList<KhachHang> sorted = dao.layTatCa(); dao.sapXep(sorted, 3);
        check(sorted.get(0).getId().equals(first.getId()), "Spending sort numeric");
        check(new KhachHangDAO(data).layTatCa().size() == dao.layTatCa().size(), "Shared store across DAOs");
        check(new KhachHangDAO(new DuLieuTam()).layTatCa().size() == 8, "Fresh launch resets data");
        LocalDateTime sameTime = LocalDateTime.now().plusSeconds(1);
        data.getLichSuKhachHang().add(new LichSuKhachHang(first.getId(), sameTime, "Trước", ""));
        data.getLichSuKhachHang().add(new LichSuKhachHang(first.getId(), sameTime, "Sau", ""));
        check(dao.layLichSu(first.getId()).get(0).getLoai().equals("Sau"), "Equal timestamps preserve newest insertion first");
        System.out.println("KhachHangDAOTest: " + checks + " checks passed");
    }
}
