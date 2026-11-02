package dao;

import entity.SanPham;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Thêm, sửa, xóa và tìm kiếm sản phẩm trong ArrayList. */
public class SanPhamDAO {
    private final DuLieuTam duLieu;
    private final DanhMucDAO danhMucDAO;

    public SanPhamDAO() { this(DuLieuTam.getMacDinh()); }
    public SanPhamDAO(DuLieuTam duLieu) {
        this.duLieu = duLieu;
        danhMucDAO = new DanhMucDAO(duLieu);
    }

    public ArrayList<SanPham> layTatCa() {
        ArrayList<SanPham> ketQua = new ArrayList<>();
        for (SanPham sp : duLieu.getSanPham()) ketQua.add(new SanPham(sp));
        return ketQua;
    }

    public SanPham timTheoMa(String id) {
        for (SanPham sp : duLieu.getSanPham()) {
            if (sp.getId().equals(id)) return new SanPham(sp);
        }
        return null;
    }

    public SanPham them(SanPham sanPham) {
        if (sanPham == null) throw new IllegalArgumentException("Chưa nhập sản phẩm.");
        SanPham moi = new SanPham(sanPham);
        moi.setId(null); // Mã nội bộ do DAO cấp, người nhập chỉ cần nhập SKU.
        kiemTra(moi);
        moi.setId(duLieu.taoMaSanPham());
        moi.setTaoLuc(LocalDateTime.now());
        moi.setCapNhatLuc(moi.getTaoLuc());
        duLieu.getSanPham().add(moi);
        return new SanPham(moi);
    }

    public SanPham capNhat(SanPham sanPham) {
        if (sanPham == null) throw new IllegalArgumentException("Chưa nhập sản phẩm.");
        SanPham cu = timTheoMa(sanPham.getId());
        if (cu == null) throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
        SanPham moi = new SanPham(sanPham);
        kiemTra(moi);
        moi.setTaoLuc(cu.getTaoLuc());
        moi.setCapNhatLuc(LocalDateTime.now());
        for (int i = 0; i < duLieu.getSanPham().size(); i++) {
            if (duLieu.getSanPham().get(i).getId().equals(moi.getId())) {
                duLieu.getSanPham().set(i, moi);
                return new SanPham(moi);
            }
        }
        throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
    }

    public void xoa(String id) {
        SanPham sp = timTheoMa(id);
        if (sp == null) throw new IllegalArgumentException("Sản phẩm không còn tồn tại.");
        if (sp.isDangKinhDoanh()) throw new IllegalArgumentException("Hãy ngừng kinh doanh và lưu sản phẩm trước khi xóa.");
        for (int i = 0; i < duLieu.getSanPham().size(); i++) {
            if (duLieu.getSanPham().get(i).getId().equals(id)) { duLieu.getSanPham().remove(i); return; }
        }
    }

    private String donGianHoa(String value) { return value == null ? "" : value.trim().replaceAll("\\s+", " "); }

    private void kiemTra(SanPham sp) {
        sp.setSku(donGianHoa(sp.getSku()).toUpperCase(Locale.ROOT));
        sp.setTen(donGianHoa(sp.getTen()));
        sp.setThuongHieu(donGianHoa(sp.getThuongHieu()));
        sp.setMoTa(sp.getMoTa() == null ? "" : sp.getMoTa().trim());
        sp.setAnhBase64(sp.getAnhBase64() == null ? "" : sp.getAnhBase64());
        if (!sp.getSku().matches("[A-Z0-9][A-Z0-9._-]{1,39}")) throw new IllegalArgumentException("SKU gồm 2–40 chữ cái không dấu, chữ số, dấu chấm, gạch ngang hoặc gạch dưới.");
        if (sp.getTen().length() < 3 || sp.getTen().length() > 150) throw new IllegalArgumentException("Tên sản phẩm phải có từ 3 đến 150 ký tự.");
        if (sp.getThuongHieu().isEmpty() || sp.getThuongHieu().length() > 80) throw new IllegalArgumentException("Thương hiệu phải có từ 1 đến 80 ký tự.");
        if (danhMucDAO.timTheoMa(sp.getDanhMucId()) == null) throw new IllegalArgumentException("Hãy chọn một danh mục hiện có.");
        BigDecimal gia = sp.getGiaBan();
        if (gia == null || gia.signum() <= 0 || gia.compareTo(new BigDecimal("999999999999")) > 0 || gia.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Giá bán phải là số nguyên VND từ 1 đến 999.999.999.999.");
        }
        sp.setGiaBan(gia.setScale(0));
        if (sp.getBaoHanhThang() < 0 || sp.getBaoHanhThang() > 120) throw new IllegalArgumentException("Bảo hành phải từ 0 đến 120 tháng.");
        if (sp.getMoTa().length() > 3000) throw new IllegalArgumentException("Mô tả không được vượt quá 3.000 ký tự.");
        for (SanPham cu : duLieu.getSanPham()) {
            if (!cu.getId().equals(sp.getId()) && cu.getSku().equalsIgnoreCase(sp.getSku())) throw new IllegalArgumentException("SKU đã tồn tại, kể cả ở sản phẩm ngừng kinh doanh.");
            if (chuanHoaTimKiem(cu.getThuongHieu()).equals(chuanHoaTimKiem(sp.getThuongHieu()))) sp.setThuongHieu(cu.getThuongHieu());
        }
        kiemTraAnh(sp.getAnhBase64());
    }

    public ArrayList<String> layThuongHieu() {
        ArrayList<String> ketQua = new ArrayList<>();
        for (SanPham sp : duLieu.getSanPham()) {
            if (!ketQua.contains(sp.getThuongHieu())) ketQua.add(sp.getThuongHieu());
        }
        Collections.sort(ketQua, String.CASE_INSENSITIVE_ORDER);
        return ketQua;
    }

    public ArrayList<Integer> layBaoHanh() {
        ArrayList<Integer> ketQua = new ArrayList<>();
        for (SanPham sp : duLieu.getSanPham()) {
            if (!ketQua.contains(sp.getBaoHanhThang())) ketQua.add(sp.getBaoHanhThang());
        }
        Collections.sort(ketQua);
        return ketQua;
    }

    public ArrayList<SanPham> timKiem(String tuKhoa, String danhMucId, String thuongHieu,
            int khoangGia, Integer baoHanh, Boolean dangKinhDoanh) {
        ArrayList<SanPham> ketQua = new ArrayList<>();
        String query = chuanHoaTimKiem(tuKhoa);
        for (SanPham sp : duLieu.getSanPham()) {
            if (danhMucId != null && !danhMucId.equals(sp.getDanhMucId())) continue;
            if (thuongHieu != null && !chuanHoaTimKiem(thuongHieu).equals(chuanHoaTimKiem(sp.getThuongHieu()))) continue;
            if (baoHanh != null && baoHanh != sp.getBaoHanhThang()) continue;
            if (dangKinhDoanh != null && dangKinhDoanh != sp.isDangKinhDoanh()) continue;
            int soVoi5 = sp.getGiaBan().compareTo(new BigDecimal("5000000"));
            int soVoi15 = sp.getGiaBan().compareTo(new BigDecimal("15000000"));
            // 0: tất cả, 1: dưới 5 triệu, 2: từ 5 đến 15 triệu, 3: trên 15 triệu.
            if (khoangGia == 1 && soVoi5 >= 0) continue;
            if (khoangGia == 2 && (soVoi5 < 0 || soVoi15 > 0)) continue;
            if (khoangGia == 3 && soVoi15 <= 0) continue;
            String noiDung = chuanHoaTimKiem(sp.getTen() + " " + sp.getSku() + " " + sp.getThuongHieu() + " " + danhMucDAO.layTen(sp.getDanhMucId()));
            boolean phuHop = true;
            if (!query.isEmpty()) {
                for (String tu : query.split(" ")) {
                    if (!noiDung.contains(tu)) { phuHop = false; break; }
                }
            }
            if (phuHop) ketQua.add(new SanPham(sp));
        }
        return ketQua;
    }

    public void sapXep(ArrayList<SanPham> danhSach, final int kieu) {
        // 0: tên A-Z, 1: tên Z-A, 2: giá tăng, 3: giá giảm, 4: mới nhất.
        Collections.sort(danhSach, new Comparator<SanPham>() {
            @Override public int compare(SanPham a, SanPham b) {
                int ketQua;
                if (kieu == 2) ketQua = a.getGiaBan().compareTo(b.getGiaBan());
                else if (kieu == 3) ketQua = b.getGiaBan().compareTo(a.getGiaBan());
                else if (kieu == 4) ketQua = b.getTaoLuc().compareTo(a.getTaoLuc());
                else {
                    ketQua = chuanHoaTimKiem(a.getTen()).compareTo(chuanHoaTimKiem(b.getTen()));
                    if (kieu == 1) ketQua = -ketQua;
                }
                if (ketQua == 0) ketQua = a.getSku().compareTo(b.getSku());
                return ketQua;
            }
        });
    }

    public static String chuanHoaTimKiem(String value) {
        if (value == null) return "";
        String ketQua = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return ketQua.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    public static String docAnh(Path path) throws IOException {
        if (Files.size(path) > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh không được vượt quá 2 MB.");
        String value = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        kiemTraAnh(value);
        return value;
    }

    private static void kiemTraAnh(String value) {
        if (value.isEmpty()) return;
        try {
            if (value.length() > 2_800_000) throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
            byte[] bytes = Base64.getDecoder().decode(value);
            if (bytes.length > 2 * 1024 * 1024) throw new IllegalArgumentException("Ảnh vượt quá 2 MB.");
            try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new IllegalArgumentException("Hãy chọn ảnh PNG hoặc JPEG hợp lệ.");
                ImageReader reader = readers.next();
                try {
                    reader.setInput(stream);
                    String format = reader.getFormatName();
                    if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg")) throw new IllegalArgumentException("Chỉ hỗ trợ PNG và JPEG.");
                    if ((long) reader.getWidth(0) * reader.getHeight(0) > 16_000_000) throw new IllegalArgumentException("Ảnh vượt quá 16 triệu điểm ảnh.");
                    if (reader.read(0) == null) throw new IllegalArgumentException("Không đọc được ảnh.");
                } finally { reader.dispose(); }
            }
        } catch (IOException | IllegalArgumentException e) { throw new IllegalArgumentException("Ảnh không hợp lệ: " + e.getMessage(), e); }
    }
}
