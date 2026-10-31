package service;

import dao.KhoSanPham;
import entity.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

public final class SanPhamService {
    public record NhapSanPham(String sku, String ten, UUID danhMucId, String thuongHieu,
            BigDecimal giaBan, int baoHanhThang, boolean dangKinhDoanh, String moTa, String anhBase64) { }
    public enum SapXep { TEN_A_Z, TEN_Z_A, GIA_TANG, GIA_GIAM, MOI_NHAT }
    public record Trang(List<SanPham> sanPham, int tongKetQua, int tongSanPham, int trang, int soTrang) {
        public Trang { sanPham = List.copyOf(sanPham); }
    }
    private final KhoSanPham kho;
    private DuLieuSanPham data;

    public SanPhamService(KhoSanPham kho) throws IOException {
        this.kho = Objects.requireNonNull(kho);
        taiLai();
    }

    public synchronized void taiLai() throws IOException {
        DuLieuSanPham candidate = kho.doc();
        if (candidate == null) {
            candidate = duLieuMau();
            try { kho.luu(candidate, 0); }
            catch (IOException e) {
                // Một cửa sổ khác có thể vừa khởi tạo file lần đầu.
                DuLieuSanPham other = kho.doc();
                if (other == null) throw e;
                candidate = other;
            }
        }
        try { kiemTraDuLieu(candidate); }
        catch (IllegalArgumentException e) { throw new IOException("Dữ liệu sản phẩm không hợp lệ: " + e.getMessage() + " File được giữ nguyên.", e); }
        data = candidate;
    }

    public synchronized List<DanhMuc> danhMuc() {
        return data.danhMuc().stream().sorted(Comparator.comparing(c -> chuanTimKiem(c.ten()))).toList();
    }
    public synchronized Optional<SanPham> tim(UUID id) { return data.sanPham().stream().filter(p -> p.id().equals(id)).findFirst(); }
    public synchronized String tenDanhMuc(UUID id) { return data.danhMuc().stream().filter(c -> c.id().equals(id)).map(DanhMuc::ten).findFirst().orElse(""); }
    public synchronized long soSanPham(UUID category) { return data.sanPham().stream().filter(p -> p.danhMucId().equals(category)).count(); }
    public synchronized List<String> thuongHieu() {
        return data.sanPham().stream().map(SanPham::thuongHieu).distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
    public synchronized List<Integer> baoHanh() { return data.sanPham().stream().map(SanPham::baoHanhThang).distinct().sorted().toList(); }

    public synchronized Trang traCuu(BoLocSanPham filter, SapXep order, int page, int pageSize) {
        if (pageSize < 1 || pageSize > 100) throw new IllegalArgumentException("Số dòng mỗi trang phải từ 1 đến 100.");
        Objects.requireNonNull(filter); Objects.requireNonNull(order);
        List<SanPham> matches = data.sanPham().stream().filter(p -> phuHop(p, filter)).sorted(comparator(order)).toList();
        int pages = Math.max(1, (matches.size() + pageSize - 1) / pageSize);
        int current = Math.max(1, Math.min(page, pages));
        int from = (current - 1) * pageSize;
        return new Trang(matches.subList(from, Math.min(from + pageSize, matches.size())), matches.size(), data.sanPham().size(), current, pages);
    }

    private boolean phuHop(SanPham p, BoLocSanPham f) {
        if (f.danhMucId() != null && !f.danhMucId().equals(p.danhMucId())) return false;
        if (f.thuongHieu() != null && !chuanTimKiem(f.thuongHieu()).equals(chuanTimKiem(p.thuongHieu()))) return false;
        if (f.baoHanhThang() != null && f.baoHanhThang() != p.baoHanhThang()) return false;
        if (f.dangKinhDoanh() != null && f.dangKinhDoanh() != p.dangKinhDoanh()) return false;
        int five = p.giaBan().compareTo(new BigDecimal("5000000"));
        int fifteen = p.giaBan().compareTo(new BigDecimal("15000000"));
        if (f.khoangGia() == BoLocSanPham.KhoangGia.DUOI_5_TRIEU && five >= 0) return false;
        if (f.khoangGia() == BoLocSanPham.KhoangGia.TU_5_DEN_15_TRIEU && (five < 0 || fifteen > 0)) return false;
        if (f.khoangGia() == BoLocSanPham.KhoangGia.TREN_15_TRIEU && fifteen <= 0) return false;
        String haystack = chuanTimKiem(p.ten() + " " + p.sku() + " " + p.thuongHieu() + " " + tenDanhMuc(p.danhMucId()));
        String query = chuanTimKiem(f.tuKhoa());
        return query.isEmpty() || Arrays.stream(query.split(" ")).allMatch(haystack::contains);
    }

    private Comparator<SanPham> comparator(SapXep order) {
        Comparator<SanPham> byName = Comparator.comparing(p -> chuanTimKiem(p.ten()));
        Comparator<SanPham> chosen = switch (order) {
            case TEN_A_Z -> byName;
            case TEN_Z_A -> byName.reversed();
            case GIA_TANG -> Comparator.comparing(SanPham::giaBan);
            case GIA_GIAM -> Comparator.comparing(SanPham::giaBan).reversed();
            case MOI_NHAT -> Comparator.comparing(SanPham::taoLuc).reversed();
        };
        return chosen.thenComparing(SanPham::sku);
    }

    public synchronized SanPham them(NhapSanPham input) throws IOException {
        NhapSanPham value = kiemTra(input, null, data);
        Instant now = Instant.now();
        SanPham product = sanPham(UUID.randomUUID(), value, now, now);
        List<SanPham> products = new ArrayList<>(data.sanPham()); products.add(product);
        ghi(data.danhMuc(), products);
        return product;
    }

    public synchronized SanPham capNhat(UUID id, NhapSanPham input) throws IOException {
        SanPham old = batBuoc(id);
        NhapSanPham value = kiemTra(input, id, data);
        SanPham updated = sanPham(id, value, old.taoLuc(), Instant.now());
        List<SanPham> products = data.sanPham().stream().map(p -> p.id().equals(id) ? updated : p).toList();
        ghi(data.danhMuc(), products);
        return updated;
    }

    public synchronized void xoa(UUID id) throws IOException {
        SanPham product = batBuoc(id);
        if (product.dangKinhDoanh()) throw new IllegalArgumentException("Hãy ngừng kinh doanh và lưu sản phẩm trước khi xóa.");
        ghi(data.danhMuc(), data.sanPham().stream().filter(p -> !p.id().equals(id)).toList());
    }

    public synchronized DanhMuc luuDanhMuc(UUID id, String ten, String moTa) throws IOException {
        String name = text(ten, "Tên danh mục", 2, 80);
        String description = optional(moTa, "Mô tả danh mục", 1000);
        if (id != null && data.danhMuc().stream().noneMatch(c -> c.id().equals(id))) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
        if (data.danhMuc().stream().anyMatch(c -> !c.id().equals(id) && chuanTimKiem(c.ten()).equals(chuanTimKiem(name)))) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
        }
        DanhMuc category = new DanhMuc(id == null ? UUID.randomUUID() : id, name, description);
        List<DanhMuc> categories = new ArrayList<>(data.danhMuc());
        categories.removeIf(c -> c.id().equals(category.id())); categories.add(category);
        ghi(categories, data.sanPham()); return category;
    }

    public synchronized void xoaDanhMuc(UUID id) throws IOException {
        if (data.danhMuc().stream().noneMatch(c -> c.id().equals(id))) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
        if (soSanPham(id) > 0) throw new IllegalArgumentException("Không thể xóa danh mục đang chứa sản phẩm, kể cả sản phẩm đã ngừng kinh doanh.");
        ghi(data.danhMuc().stream().filter(c -> !c.id().equals(id)).toList(), data.sanPham());
    }

    private SanPham batBuoc(UUID id) { return tim(id).orElseThrow(() -> new IllegalArgumentException("Sản phẩm không còn tồn tại. Hãy tải lại danh sách.")); }
    private void ghi(List<DanhMuc> categories, List<SanPham> products) throws IOException {
        DuLieuSanPham next = new DuLieuSanPham(data.phienBan() + 1, categories, products);
        kho.luu(next, data.phienBan());
        data = next; // Chỉ đổi bộ nhớ sau khi file đã lưu thành công.
    }
    private SanPham sanPham(UUID id, NhapSanPham v, Instant created, Instant updated) {
        return new SanPham(id, v.sku(), v.ten(), v.danhMucId(), v.thuongHieu(), v.giaBan(), v.baoHanhThang(),
                v.dangKinhDoanh(), v.moTa(), v.anhBase64(), created, updated);
    }

    private NhapSanPham kiemTra(NhapSanPham input, UUID except, DuLieuSanPham snapshot) {
        Objects.requireNonNull(input);
        String sku = text(input.sku(), "SKU", 2, 40).toUpperCase(Locale.ROOT);
        if (!sku.matches("[A-Z0-9][A-Z0-9._-]{1,39}")) throw new IllegalArgumentException("SKU gồm 2–40 chữ cái không dấu, chữ số, dấu chấm, gạch ngang hoặc gạch dưới.");
        if (snapshot.sanPham().stream().anyMatch(p -> !p.id().equals(except) && p.sku().equalsIgnoreCase(sku))) throw new IllegalArgumentException("SKU đã tồn tại, kể cả ở sản phẩm ngừng kinh doanh.");
        String name = text(input.ten(), "Tên sản phẩm", 3, 150);
        String brand = text(input.thuongHieu(), "Thương hiệu", 1, 80);
        String canonical = snapshot.sanPham().stream().map(SanPham::thuongHieu).filter(b -> chuanTimKiem(b).equals(chuanTimKiem(brand))).findFirst().orElse(brand);
        if (input.danhMucId() == null || snapshot.danhMuc().stream().noneMatch(c -> c.id().equals(input.danhMucId()))) throw new IllegalArgumentException("Hãy chọn một danh mục hiện có.");
        BigDecimal price = input.giaBan();
        if (price == null || price.signum() <= 0 || price.compareTo(new BigDecimal("999999999999")) > 0 || price.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Giá bán phải là số nguyên VND từ 1 đến 999.999.999.999.");
        }
        if (input.baoHanhThang() < 0 || input.baoHanhThang() > 120) throw new IllegalArgumentException("Bảo hành phải từ 0 đến 120 tháng.");
        String description = optional(input.moTa(), "Mô tả", 3000);
        String image = input.anhBase64() == null ? "" : input.anhBase64();
        kiemTraAnh(image);
        return new NhapSanPham(sku, name, input.danhMucId(), canonical, price.setScale(0), input.baoHanhThang(), input.dangKinhDoanh(), description, image);
    }

    private void kiemTraDuLieu(DuLieuSanPham snapshot) {
        Set<UUID> categoryIds = new HashSet<>(), ids = new HashSet<>(); Set<String> names = new HashSet<>();
        for (DanhMuc c : snapshot.danhMuc()) {
            if (c.id() == null || !categoryIds.add(c.id()) || !names.add(chuanTimKiem(c.ten()))) throw new IllegalArgumentException("Danh mục bị trùng hoặc thiếu mã.");
            text(c.ten(), "Tên danh mục", 2, 80); optional(c.moTa(), "Mô tả danh mục", 1000);
        }
        for (SanPham p : snapshot.sanPham()) {
            if (p.id() == null || !ids.add(p.id()) || p.taoLuc() == null || p.capNhatLuc() == null || p.capNhatLuc().isBefore(p.taoLuc())) throw new IllegalArgumentException("Sản phẩm bị trùng mã hoặc sai thời gian.");
            kiemTra(new NhapSanPham(p.sku(), p.ten(), p.danhMucId(), p.thuongHieu(), p.giaBan(), p.baoHanhThang(), p.dangKinhDoanh(), p.moTa(), p.anhBase64()), p.id(), snapshot);
        }
    }

    private static String text(String value, String label, int min, int max) {
        String result = value == null ? "" : value.strip().replaceAll("\\s+", " ");
        kiemTraKyTu(result, label);
        if (result.length() < min || result.length() > max) throw new IllegalArgumentException(label + " phải có từ " + min + " đến " + max + " ký tự.");
        return result;
    }
    private static String optional(String value, String label, int max) {
        String result = value == null ? "" : value.strip();
        kiemTraKyTu(result, label);
        if (result.length() > max) throw new IllegalArgumentException(label + " không được vượt quá " + max + " ký tự.");
        return result;
    }
    private static void kiemTraKyTu(String value, String label) {
        if (value.codePoints().anyMatch(c -> !(c == 9 || c == 10 || c == 13 || c >= 32 && c <= 0xD7FF
                || c >= 0xE000 && c <= 0xFFFD || c >= 0x10000 && c <= 0x10FFFF))) {
            throw new IllegalArgumentException(label + " chứa ký tự điều khiển không hợp lệ.");
        }
    }
    public static String chuanTimKiem(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).strip().replaceAll("\\s+", " ");
    }

    public static String docAnh(Path path) throws IOException {
        if (Files.size(path) > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh phải nhỏ hơn hoặc bằng 2 MB.");
        String image = Base64.getEncoder().encodeToString(Files.readAllBytes(path)); kiemTraAnh(image); return image;
    }
    private static void kiemTraAnh(String value) {
        if (value.isEmpty()) return;
        try {
            if (value.length() > 2_800_000) throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
            byte[] bytes = Base64.getDecoder().decode(value);
            if (bytes.length > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
            try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new IllegalArgumentException("Ảnh không hợp lệ. Hãy chọn PNG hoặc JPEG.");
                ImageReader reader = readers.next();
                try {
                    reader.setInput(stream);
                    String format = reader.getFormatName();
                    if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg")) throw new IllegalArgumentException("Chỉ hỗ trợ ảnh PNG và JPEG.");
                    if ((long) reader.getWidth(0) * reader.getHeight(0) > 16_000_000) throw new IllegalArgumentException("Ảnh không được vượt quá 16 triệu điểm ảnh.");
                    if (reader.read(0) == null) throw new IllegalArgumentException("Không đọc được ảnh.");
                } finally { reader.dispose(); }
            }
        } catch (IOException | IllegalArgumentException e) { throw new IllegalArgumentException("Ảnh không hợp lệ: " + e.getMessage(), e); }
    }

    private DuLieuSanPham duLieuMau() {
        String[] categoryNames = {"Tủ lạnh", "Máy giặt", "Tivi", "Máy lạnh", "Laptop", "Điện thoại", "Gia dụng", "Phụ kiện"};
        List<DanhMuc> categories = Arrays.stream(categoryNames).map(n -> new DanhMuc(UUID.randomUUID(), n, "")).toList();
        String[][] examples = {
            {"TL001", "Tủ lạnh Samsung Inverter 236 lít", "Samsung", "12000000", "24"},
            {"MG001", "Máy giặt LG Inverter FV1408", "LG", "8500000", "12"},
            {"TV001", "Smart Tivi Samsung 55 inch", "Samsung", "15990000", "24"},
            {"ML001", "Máy lạnh Daikin Inverter 1 HP", "Daikin", "13500000", "24"},
            {"LT001", "Laptop ASUS VivoBook 15", "ASUS", "14990000", "24"},
            {"DT001", "Điện thoại Samsung Galaxy A35", "Samsung", "9990000", "12"},
            {"GD001", "Nồi chiên không dầu Lock&Lock 5 lít", "Lock&Lock", "2190000", "12"},
            {"PK001", "Tai nghe Sony WH-CH520", "Sony", "1490000", "12"}
        };
        List<SanPham> products = new ArrayList<>(); Instant now = Instant.now();
        for (int i = 0; i < examples.length; i++) {
            String[] p = examples[i]; products.add(new SanPham(UUID.randomUUID(), p[0], p[1], categories.get(i).id(), p[2], new BigDecimal(p[3]), Integer.parseInt(p[4]), i != 6, "", "", now, now));
        }
        return new DuLieuSanPham(1, categories, products);
    }
}
