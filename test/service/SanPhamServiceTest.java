package service;

import dao.*;
import entity.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Chạy bằng Java 17, không cần JUnit hay cơ sở dữ liệu. */
public final class SanPhamServiceTest {
    private static int checks;
    @FunctionalInterface private interface Checked { void run() throws Exception; }
    private record Fixture(Path file, SanPhamService service) { }
    private static Path root;

    private static Fixture fresh(String name) throws Exception {
        Path file = root.resolve(name).resolve("san-pham.xml");
        return new Fixture(file, new SanPhamService(new XmlKhoSanPham(file)));
    }
    private static void check(boolean result, String name) {
        checks++; if (!result) throw new AssertionError(name);
    }
    private static void fails(Class<? extends Exception> type, Checked action, String name) throws Exception {
        try { action.run(); } catch (Exception e) { check(type.isInstance(e), name + ": " + e); return; }
        throw new AssertionError("Expected failure: " + name);
    }
    private static int count(SanPhamService service) { return service.traCuu(BoLocSanPham.tatCa(), SanPhamService.SapXep.TEN_A_Z, 1, 100).tongSanPham(); }
    private static SanPhamService.NhapSanPham input(SanPhamService service, String sku, String price, boolean active) {
        return new SanPhamService.NhapSanPham(sku, "Sản phẩm kiểm thử " + sku, service.danhMuc().get(0).id(),
                "Test", new BigDecimal(price), 12, active, "Thông số <b> & mô tả\nDòng thứ hai", "");
    }
    private static SanPhamService.NhapSanPham from(SanPham product, boolean active) {
        return new SanPhamService.NhapSanPham(product.sku(), product.ten(), product.danhMucId(), product.thuongHieu(),
                product.giaBan(), product.baoHanhThang(), active, product.moTa(), product.anhBase64());
    }

    private static void crud() throws Exception {
        Fixture f = fresh("crud"); SanPhamService s = f.service();
        check(count(s) == 8 && s.danhMuc().size() == 8, "Seed once");
        SanPham added = s.them(input(s, "  test-01  ", "5000000", true));
        check(added.sku().equals("TEST-01") && count(s) == 9, "Add and normalize SKU");
        SanPhamService reopened = new SanPhamService(new XmlKhoSanPham(f.file()));
        check(reopened.tim(added.id()).orElseThrow().equals(added), "Persist all fields across reopen");
        fails(IllegalArgumentException.class, () -> s.them(input(s, "test-01", "100", true)), "Duplicate SKU");
        fails(IllegalArgumentException.class, () -> s.them(input(s, "gd001", "100", true)), "Duplicate discontinued SKU");
        fails(IllegalArgumentException.class, () -> s.xoa(added.id()), "Cannot delete active product");
        SanPham stopped = s.capNhat(added.id(), from(added, false));
        check(!stopped.dangKinhDoanh() && stopped.taoLuc().equals(added.taoLuc()), "Stop and preserve created time");
        SanPham active = s.capNhat(stopped.id(), from(stopped, true));
        check(active.dangKinhDoanh(), "Reactivate");
        s.capNhat(active.id(), from(active, false)); s.xoa(active.id());
        check(count(s) == 8 && new SanPhamService(new XmlKhoSanPham(f.file())).tim(active.id()).isEmpty(), "Delete persists");
        fails(IllegalArgumentException.class, () -> s.capNhat(active.id(), from(active, true)), "Update deleted ID rejected");
    }

    private static void validation() throws Exception {
        SanPhamService s = fresh("validation").service();
        for (String price : new String[]{"0", "-1", "1.5", "1000000000000"}) {
            fails(IllegalArgumentException.class, () -> s.them(input(s, "BADPRICE", price, true)), "Price " + price);
        }
        for (String sku : new String[]{"A", "Có dấu", "BAD SKU", "A".repeat(41)}) {
            fails(IllegalArgumentException.class, () -> s.them(input(s, sku, "100", true)), "Invalid SKU");
        }
        UUID category = s.danhMuc().get(0).id();
        for (int warranty : new int[]{-1, 121}) {
            fails(IllegalArgumentException.class, () -> s.them(new SanPhamService.NhapSanPham("WARRANTY", "Kiểm thử", category, "Test", BigDecimal.TEN, warranty, true, "", "")), "Warranty bounds");
        }
        fails(IllegalArgumentException.class, () -> s.them(new SanPhamService.NhapSanPham("MISSING", "Kiểm thử", UUID.randomUUID(), "Test", BigDecimal.TEN, 0, true, "", "")), "Missing category");
        fails(IllegalArgumentException.class, () -> s.them(new SanPhamService.NhapSanPham("CONTROL", "Kiểm thử", category, "Test", BigDecimal.TEN, 0, true, "bad\u0000text", "")), "XML control characters rejected");
        fails(IllegalArgumentException.class, () -> s.them(new SanPhamService.NhapSanPham("IMAGE", "Kiểm thử", category, "Test", BigDecimal.TEN, 0, true, "", "not-base64")), "Invalid image");
        check(count(s) == 8, "Invalid submissions do not mutate store");
        SanPham maximum = s.them(new SanPhamService.NhapSanPham("MAXIMUM", "Kiểm thử", category, "Test", new BigDecimal("999999999999"), 120, true, "", ""));
        check(maximum.baoHanhThang() == 120, "Valid maximums accepted");
    }

    private static void categories() throws Exception {
        Fixture f = fresh("categories"); SanPhamService s = f.service();
        DanhMuc empty = s.luuDanhMuc(null, "  Danh mục mới  ", "Mô tả <test> & XML");
        check(empty.ten().equals("Danh mục mới"), "Trim category name");
        fails(IllegalArgumentException.class, () -> s.luuDanhMuc(null, "DANH MUC MOI", ""), "Accent/case duplicate category");
        SanPham p = s.them(new SanPhamService.NhapSanPham("CATEGORY", "Sản phẩm danh mục", empty.id(), "Test", BigDecimal.TEN, 0, false, "", ""));
        s.luuDanhMuc(empty.id(), "Danh mục đã đổi tên", "");
        check(s.tim(p.id()).orElseThrow().danhMucId().equals(empty.id()) && s.tenDanhMuc(empty.id()).equals("Danh mục đã đổi tên"), "Rename preserves product relationship");
        fails(IllegalArgumentException.class, () -> s.xoaDanhMuc(empty.id()), "Cannot delete category with discontinued product");
        s.xoa(p.id()); s.xoaDanhMuc(empty.id());
        check(s.danhMuc().size() == 8, "Delete empty category");
        check(new SanPhamService(new XmlKhoSanPham(f.file())).danhMuc().size() == 8, "Category persistence");
    }

    private static void filtersAndPaging() throws Exception {
        SanPhamService s = fresh("query").service();
        var text = new BoLocSanPham("MAY GIAT lg", null, null, BoLocSanPham.KhoangGia.TAT_CA, null, true);
        check(s.traCuu(text, SanPhamService.SapXep.TEN_A_Z, 1, 10).tongKetQua() == 1, "Unaccented multi-token search");
        for (String price : new String[]{"4999999", "5000000", "15000000", "15000001"}) s.them(input(s, "PRICE" + price, price, true));
        UUID category = s.danhMuc().get(0).id();
        for (var range : BoLocSanPham.KhoangGia.values()) {
            var filter = new BoLocSanPham("PRICE", category, "test", range, 12, true);
            int expected = switch (range) { case TAT_CA -> 4; case DUOI_5_TRIEU, TREN_15_TRIEU -> 1; case TU_5_DEN_15_TRIEU -> 2; };
            check(s.traCuu(filter, SanPhamService.SapXep.GIA_TANG, 1, 10).tongKetQua() == expected, "Combined filter/range " + range);
        }
        var first = s.traCuu(BoLocSanPham.tatCa(), SanPhamService.SapXep.GIA_TANG, 1, 5);
        check(first.sanPham().size() == 5 && first.soTrang() == 3, "Paging first page");
        var last = s.traCuu(BoLocSanPham.tatCa(), SanPhamService.SapXep.GIA_TANG, 999, 5);
        check(last.trang() == 3 && last.sanPham().size() == 2, "Clamp after page changes");
        check(first.sanPham().get(0).giaBan().compareTo(last.sanPham().get(1).giaBan()) <= 0, "Numeric price order");
        var none = s.traCuu(new BoLocSanPham("khongtontai", null, null, BoLocSanPham.KhoangGia.TAT_CA, null, null), SanPhamService.SapXep.TEN_A_Z, 99, 10);
        check(none.tongKetQua() == 0 && none.trang() == 1 && none.soTrang() == 1 && none.sanPham().isEmpty(), "Empty results paging");
        fails(IllegalArgumentException.class, () -> s.traCuu(BoLocSanPham.tatCa(), SanPhamService.SapXep.TEN_A_Z, 1, 0), "Invalid page size");
    }

    private static void storageFailures() throws Exception {
        Fixture f = fresh("conflict"); SanPhamService a = f.service(), b = new SanPhamService(new XmlKhoSanPham(f.file()));
        a.them(input(a, "WRITER-A", "100", true));
        fails(IOException.class, () -> b.them(input(b, "WRITER-B", "100", true)), "Stale writer blocked");
        check(count(b) == 8, "Stale writer memory unchanged");
        b.taiLai(); check(count(b) == 9, "Reload conflict");
        XmlKhoSanPham real = new XmlKhoSanPham(f.file());
        KhoSanPham broken = new KhoSanPham() {
            public DuLieuSanPham doc() throws IOException { return real.doc(); }
            public void luu(DuLieuSanPham data, long revision) throws IOException { throw new IOException("Simulated disk failure"); }
        };
        SanPhamService failing = new SanPhamService(broken);
        fails(IOException.class, () -> failing.them(input(failing, "DISK-FAIL", "100", true)), "Write failure propagated");
        check(count(failing) == 9 && count(new SanPhamService(real)) == 9, "Write failure does not mutate memory or disk");
        Path malformed = root.resolve("malformed.xml"); Files.writeString(malformed, "<catalog broken");
        byte[] before = Files.readAllBytes(malformed);
        fails(IOException.class, () -> new SanPhamService(new XmlKhoSanPham(malformed)), "Malformed XML rejected");
        check(Arrays.equals(before, Files.readAllBytes(malformed)), "Malformed file preserved");
        Path xxe = root.resolve("xxe.xml");
        Files.writeString(xxe, "<!DOCTYPE catalog [<!ENTITY x SYSTEM 'file:///not-allowed'>]><catalog version='1' revision='1'>&x;</catalog>");
        fails(IOException.class, () -> new XmlKhoSanPham(xxe).doc(), "External entities blocked");
        var valid = real.doc(); var p = valid.sanPham().get(0);
        var dangling = new SanPham(p.id(), p.sku(), p.ten(), UUID.randomUUID(), p.thuongHieu(), p.giaBan(), p.baoHanhThang(), true, "", "", p.taoLuc(), p.capNhatLuc());
        Path invalid = root.resolve("dangling.xml");
        new XmlKhoSanPham(invalid).luu(new DuLieuSanPham(1, valid.danhMuc(), List.of(dangling)), 0);
        fails(IOException.class, () -> new SanPhamService(new XmlKhoSanPham(invalid)), "Invalid persisted references rejected");
    }

    private static void images() throws Exception {
        Fixture f = fresh("images"); SanPhamService s = f.service();
        Path image = root.resolve("image.png"); ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile());
        String encoded = SanPhamService.docAnh(image);
        SanPham p = s.them(new SanPhamService.NhapSanPham("PHOTO", "Sản phẩm có ảnh", s.danhMuc().get(0).id(), "Test", BigDecimal.TEN, 0, true, "", encoded));
        Files.delete(image);
        check(new SanPhamService(new XmlKhoSanPham(f.file())).tim(p.id()).orElseThrow().anhBase64().equals(encoded), "Photo embedded, not linked to original path");
    }

    public static void main(String[] args) throws Exception {
        root = Files.createTempDirectory(Path.of(args[0]), "product-service-test-");
        crud(); validation(); categories(); filtersAndPaging(); storageFailures(); images();
        System.out.println("SanPhamServiceTest: " + checks + " checks passed");
    }
}
