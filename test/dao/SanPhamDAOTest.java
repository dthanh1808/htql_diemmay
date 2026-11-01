package dao;

import entity.DanhMuc;
import entity.SanPham;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Kiểm tra DAO ArrayList, không cần SQL, file dữ liệu hoặc thư viện kiểm tra ngoài. */
public class SanPhamDAOTest {
    private static int checks;

    private static void check(boolean ketQua, String ten) {
        checks++;
        if (!ketQua) throw new AssertionError(ten);
    }

    private static void sai(Runnable thaoTac, String ten) {
        try { thaoTac.run(); }
        catch (IllegalArgumentException e) { checks++; return; }
        throw new AssertionError("Phai bao loi: " + ten);
    }

    private static SanPham tao(SanPhamDAO dao, DanhMucDAO dm, String sku, String gia) {
        SanPham sp = new SanPham();
        sp.setSku(sku);
        sp.setTen("Sản phẩm kiểm thử " + sku.trim());
        sp.setDanhMucId(dm.layTatCa().get(0).getId());
        sp.setThuongHieu("Test");
        sp.setGiaBan(new BigDecimal(gia));
        sp.setBaoHanhThang(12);
        sp.setDangKinhDoanh(true);
        return sp;
    }

    private static void crud() {
        DuLieuTam duLieu = new DuLieuTam();
        SanPhamDAO dao = new SanPhamDAO(duLieu);
        DanhMucDAO dm = new DanhMucDAO(duLieu);
        check(dao.layTatCa().size() == 8 && dm.layTatCa().size() == 8, "Co du lieu mau");
        SanPham sp = dao.them(tao(dao, dm, "  test-01  ", "5000000"));
        check(sp.getSku().equals("TEST-01") && sp.getId() != null, "Chuan hoa SKU va cap ma noi bo");
        check(dao.layTatCa().size() == 9 && sp.getTaoLuc() != null, "Them va gan thoi gian");
        sai(() -> dao.them(tao(dao, dm, "test-01", "100")), "Trung SKU");
        sai(() -> dao.them(tao(dao, dm, "gd001", "100")), "Trung SKU cua san pham ngung ban");
        sai(() -> dao.xoa(sp.getId()), "Xoa san pham dang ban");
        sp.setDangKinhDoanh(false);
        dao.capNhat(sp);
        check(!dao.timTheoMa(sp.getId()).isDangKinhDoanh(), "Ngung kinh doanh");
        sp.setDangKinhDoanh(true);
        dao.capNhat(sp);
        check(dao.timTheoMa(sp.getId()).isDangKinhDoanh(), "Kich hoat lai");
        check(dao.timTheoMa(sp.getId()).getTaoLuc().equals(sp.getTaoLuc()), "Giu ngay tao khi sua");
        sp.setDangKinhDoanh(false); dao.capNhat(sp); dao.xoa(sp.getId());
        check(dao.timTheoMa(sp.getId()) == null && dao.layTatCa().size() == 8, "Xoa san pham ngung ban");
        sai(() -> dao.capNhat(sp), "Sua san pham da xoa");
        check(new SanPhamDAO(duLieu).layTatCa().size() == 8, "Cac DAO dung chung bo du lieu");
        dao.them(tao(dao, dm, "SESSION", "100"));
        check(new SanPhamDAO(duLieu).layTatCa().size() == 9, "Dao moi trong cung phien thay du lieu");
        check(new SanPhamDAO(new DuLieuTam()).layTatCa().size() == 8, "Phien moi tro ve du lieu mau");
    }

    private static void kiemTraNhap() {
        DuLieuTam duLieu = new DuLieuTam();
        SanPhamDAO dao = new SanPhamDAO(duLieu);
        DanhMucDAO dm = new DanhMucDAO(duLieu);
        for (String gia : new String[]{"0", "-1", "1.5", "1000000000000"}) {
            sai(() -> dao.them(tao(dao, dm, "PRICE", gia)), "Gia khong hop le");
        }
        for (String sku : new String[]{"A", "Có dấu", "BAD SKU", "A".repeat(41)}) {
            sai(() -> dao.them(tao(dao, dm, sku, "100")), "SKU khong hop le");
        }
        SanPham sp = tao(dao, dm, "VALIDATE", "100");
        sp.setBaoHanhThang(-1); sai(() -> dao.them(sp), "Bao hanh am");
        sp.setBaoHanhThang(121); sai(() -> dao.them(sp), "Bao hanh qua 120");
        sp.setBaoHanhThang(12); sp.setDanhMucId("KHONG-CO"); sai(() -> dao.them(sp), "Danh muc khong ton tai");
        sp.setDanhMucId(dm.layTatCa().get(0).getId()); sp.setTen("ab"); sai(() -> dao.them(sp), "Ten qua ngan");
        sp.setTen("Sản phẩm hợp lệ"); sp.setThuongHieu(""); sai(() -> dao.them(sp), "Thieu thuong hieu");
        sp.setThuongHieu("Test"); sp.setAnhBase64("not-base64"); sai(() -> dao.them(sp), "Anh khong hop le");
        check(dao.layTatCa().size() == 8, "Nhap sai khong doi du lieu");
        sp.setAnhBase64(""); sp.setGiaBan(new BigDecimal("999999999999")); sp.setBaoHanhThang(120);
        check(dao.them(sp).getBaoHanhThang() == 120, "Chap nhan gia tri bien hop le");
        sai(() -> dao.them(null), "San pham null"); sai(() -> dm.them(null), "Danh muc null");
    }

    private static void danhMuc() {
        DuLieuTam duLieu = new DuLieuTam();
        SanPhamDAO dao = new SanPhamDAO(duLieu);
        DanhMucDAO dm = new DanhMucDAO(duLieu);
        DanhMuc moi = dm.them(new DanhMuc(null, "  Danh mục mới  ", "Mô tả"));
        check(moi.getTen().equals("Danh mục mới"), "Chuan hoa ten danh muc");
        sai(() -> dm.them(new DanhMuc(null, "DANH MUC MOI", "")), "Ten trung khong dau");
        SanPham sp = tao(dao, dm, "CATEGORY", "100"); sp.setDanhMucId(moi.getId()); sp.setDangKinhDoanh(false);
        SanPham daThem = dao.them(sp);
        moi.setTen("Danh mục đổi tên"); dm.capNhat(moi);
        check(dao.timTheoMa(daThem.getId()).getDanhMucId().equals(moi.getId()) && dm.layTen(moi.getId()).equals("Danh mục đổi tên"), "Doi ten giu lien ket");
        sai(() -> dm.xoa(moi.getId()), "Chan xoa danh muc dang co san pham");
        dao.xoa(daThem.getId()); dm.xoa(moi.getId());
        check(dm.layTatCa().size() == 8, "Xoa danh muc trong");
    }

    private static void timKiemVaSapXep() {
        DuLieuTam duLieu = new DuLieuTam();
        SanPhamDAO dao = new SanPhamDAO(duLieu);
        DanhMucDAO dm = new DanhMucDAO(duLieu);
        check(dao.timKiem("MAY GIAT lg", null, null, 0, null, true).size() == 1, "Tim khong dau nhieu tu");
        for (String gia : new String[]{"4999999", "5000000", "15000000", "15000001"}) dao.them(tao(dao, dm, "PRICE" + gia, gia));
        int[] mongDoi = {4, 1, 2, 1};
        for (int i = 0; i < mongDoi.length; i++) {
            check(dao.timKiem("PRICE", dm.layTatCa().get(0).getId(), "test", i, 12, true).size() == mongDoi[i], "Loc ket hop va bien gia " + i);
        }
        check(dao.timKiem("KhongCoTuNay", null, null, 0, null, null).isEmpty(), "Ket qua trong");
        ArrayList<SanPham> ketQua = dao.timKiem("PRICE", null, null, 0, null, null);
        dao.sapXep(ketQua, 2);
        check(ketQua.get(0).getGiaBan().equals(new BigDecimal("4999999")), "Sap xep gia tang bang so");
        dao.sapXep(ketQua, 3);
        check(ketQua.get(0).getGiaBan().equals(new BigDecimal("15000001")), "Sap xep gia giam");
        check(dao.layThuongHieu().contains("Test") && dao.layBaoHanh().contains(12), "Lay lua chon bo loc tu DAO");
    }

    private static void banSao() {
        DuLieuTam duLieu = new DuLieuTam();
        SanPhamDAO dao = new SanPhamDAO(duLieu);
        DanhMucDAO dm = new DanhMucDAO(duLieu);
        ArrayList<SanPham> ds = dao.layTatCa(); SanPham sp = ds.get(0);
        String ten = sp.getTen(); sp.setTen("Thay đổi chưa gọi cập nhật"); ds.clear();
        check(dao.layTatCa().size() == 8 && dao.timTheoMa(sp.getId()).getTen().equals(ten), "Getter entity khong tu sua ArrayList DAO");
        DanhMuc c = dm.layTatCa().get(0); String tenCu = c.getTen(); c.setTen("Chưa lưu");
        check(dm.timTheoMa(c.getId()).getTen().equals(tenCu), "Danh muc tra ve ban sao");
    }

    private static void anh(String directory) throws Exception {
        Path file = Files.createTempFile(Path.of(directory), "product-image-", ".png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", file.toFile());
        String value = SanPhamDAO.docAnh(file); Files.delete(file);
        DuLieuTam duLieu = new DuLieuTam(); SanPhamDAO dao = new SanPhamDAO(duLieu); DanhMucDAO dm = new DanhMucDAO(duLieu);
        SanPham sp = tao(dao, dm, "IMAGE", "100"); sp.setAnhBase64(value);
        SanPham saved = dao.them(sp);
        check(dao.timTheoMa(saved.getId()).getAnhBase64().equals(value), "Anh duoc giu trong Entity");
    }

    public static void main(String[] args) throws Exception {
        crud(); kiemTraNhap(); danhMuc(); timKiemVaSapXep(); banSao(); anh(args[0]);
        System.out.println("SanPhamDAOTest: " + checks + " checks passed");
    }
}
