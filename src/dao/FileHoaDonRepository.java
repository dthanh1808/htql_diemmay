package dao;

import entity.HoaDon;
import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Kho cục bộ cho bản desktop. Không giả lập dữ liệu khi đọc lỗi. */
final class FileHoaDonRepository {
    private final Path path;
    public FileHoaDonRepository(Path path) { this.path = path.toAbsolutePath(); }
    public Path getPath() { return path; }

    public synchronized List<HoaDon> findAll() throws IOException {
        if (!Files.exists(path)) return new ArrayList<>();
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) { p.loadFromXML(in); }
        List<HoaDon> result = new ArrayList<>();
        try {
            int count = Integer.parseInt(p.getProperty("count"));
            for (int i = 0; i < count; i++) {
                String k = i + ".";
                List<HoaDon.Dong> lines = new ArrayList<>();
                for (int j = 0; j < Integer.parseInt(p.getProperty(k + "lines")); j++) {
                    String l = k + j + ".";
                    lines.add(new HoaDon.Dong(p.getProperty(l + "sku"), p.getProperty(l + "name"),
                            p.getProperty(l + "category"), Integer.parseInt(p.getProperty(l + "qty")),
                            new BigDecimal(p.getProperty(l + "price")), new BigDecimal(p.getProperty(l + "discount"))));
                }
                result.add(new HoaDon(p.getProperty(k + "id"), LocalDateTime.parse(p.getProperty(k + "date")),
                        p.getProperty(k + "employee"), p.getProperty(k + "shift"), p.getProperty(k + "customer"),
                        p.getProperty(k + "phone"), p.getProperty(k + "tier"), p.getProperty(k + "method"),
                        p.getProperty(k + "status"), new BigDecimal(p.getProperty(k + "discount")),
                        new BigDecimal(p.getProperty(k + "paid")), p.getProperty(k + "delivery"),
                        p.getProperty(k + "promotion"), p.getProperty(k + "partner"),
                        Integer.parseInt(p.getProperty(k + "term")), lines));
            }
        } catch (RuntimeException e) { throw new IOException("Dữ liệu hóa đơn không hợp lệ: " + path, e); }
        result.sort(Comparator.comparing(HoaDon::ngay).reversed());
        return result;
    }

    public synchronized HoaDon save(HoaDon draft) throws IOException {
        service.HoaDonService.validate(draft);
        Files.createDirectories(path.getParent());
        try (FileChannel channel = FileChannel.open(path.resolveSibling(path.getFileName() + ".lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                var lock = channel.lock()) {
            List<HoaDon> invoices = findAll();
            String prefix = "HD" + draft.ngay().format(DateTimeFormatter.ofPattern("yyMMdd"));
            int next = invoices.stream().map(HoaDon::ma).filter(id -> id.startsWith(prefix))
                    .mapToInt(id -> Integer.parseInt(id.substring(8))).max().orElse(0) + 1;
            if (next > 9999) throw new IOException("Đã đạt giới hạn 9999 hóa đơn/ngày.");
            HoaDon saved = draft.voiMa(prefix + String.format("%04d", next));
            invoices.add(saved);
            write(invoices);
            return saved;
        }
    }

    private void write(List<HoaDon> invoices) throws IOException {
        Properties p = new Properties();
        p.setProperty("count", "" + invoices.size());
        for (int i = 0; i < invoices.size(); i++) {
            HoaDon h = invoices.get(i); String k = i + ".";
            String[][] fields = {{"id", h.ma()}, {"date", h.ngay().toString()}, {"employee", h.nhanVien()},
                {"shift", h.ca()}, {"customer", h.khachHang()}, {"phone", h.sdt()}, {"tier", h.hang()},
                {"method", h.phuongThuc()}, {"status", h.trangThai()}, {"discount", h.giamHoaDon().toPlainString()},
                {"paid", h.khachDua().toPlainString()}, {"delivery", h.giaoHang()}, {"promotion", h.khuyenMai()},
                {"partner", h.doiTac()}, {"term", "" + h.kyHan()}, {"lines", "" + h.chiTiet().size()}};
            for (String[] f : fields) p.setProperty(k + f[0], f[1]);
            for (int j = 0; j < h.chiTiet().size(); j++) {
                HoaDon.Dong d = h.chiTiet().get(j); String l = k + j + ".";
                p.setProperty(l + "sku", d.sku()); p.setProperty(l + "name", d.ten());
                p.setProperty(l + "category", d.danhMuc()); p.setProperty(l + "qty", "" + d.soLuong());
                p.setProperty(l + "price", d.donGia().toPlainString()); p.setProperty(l + "discount", d.giamGia().toPlainString());
            }
        }
        Path temp = Files.createTempFile(path.getParent(), "hoa-don-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) { p.storeToXML(out, "Gia Thanh invoices", "UTF-8"); }
            try { Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
