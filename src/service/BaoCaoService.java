//package service;
//
//import entity.HoaDon;
//import java.math.*;
//import java.time.*;
//import java.time.temporal.WeekFields;
//import java.util.*;
//
//public final class BaoCaoService {
//    public record Access(String employee, boolean manager) {
//        public Access { if (!manager && (employee == null || employee.isBlank())) throw new IllegalArgumentException("Thiếu nhân viên đăng nhập."); }
//    }
//    public static final String[] TYPES = {"Doanh thu theo ngày", "Doanh thu theo tuần", "Doanh thu theo tháng",
//        "Doanh thu theo năm", "Doanh số nhân viên", "Doanh thu theo ca", "Phương thức thanh toán",
//        "Sản phẩm bán chạy", "Khách hàng thân thiết", "Hiệu quả khuyến mãi", "Giao dịch trả góp"};
//    public record Filter(LocalDate from, LocalDate to, String employee, String category, String tier, String method) {
//        public Filter {
//            if (from.isAfter(to)) throw new IllegalArgumentException("Từ ngày phải trước hoặc bằng đến ngày.");
//        }
//        public boolean matches(HoaDon h) {
//            return !h.ngay().toLocalDate().isBefore(from) && !h.ngay().toLocalDate().isAfter(to)
//                && contains(h.nhanVien(), employee) && contains(h.hang(), tier) && contains(h.phuongThuc(), method);
//        }
//    }
//    public record Row(String label, int invoices, long quantity, BigDecimal gross, BigDecimal discount, BigDecimal revenue) {}
//    private static boolean contains(String value, String query) { return value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT)); }
//    public static List<Row> summarize(List<HoaDon> source, Filter filter, int type) {
//        return summarize(source, filter, type, new Access("", true));
//    }
//    public static List<Row> summarize(List<HoaDon> source, Filter filter, int type, Access access) {
//        if (!access.manager() && type != 4 && type != 7) throw new IllegalArgumentException("Nhân viên bán hàng chỉ xem doanh số cá nhân và sản phẩm bán chạy.");
//        Map<String, Row> groups = new TreeMap<>();
//        for (HoaDon h : source) {
//            if (!access.manager() && type == 4 && !h.nhanVien().equals(access.employee())) continue;
//            if (!h.ghiNhanDoanhThu() || !filter.matches(h) || (type == 10 && !h.phuongThuc().equals("Trả góp"))) continue;
//            Map<String, List<HoaDon.Dong>> items = new LinkedHashMap<>();
//            for (HoaDon.Dong d : h.chiTiet()) {
//                if (!contains(d.danhMuc(), filter.category())) continue;
//                String key = switch (type) {
//                    case 0 -> h.ngay().toLocalDate().toString();
//                    case 1 -> { WeekFields w = WeekFields.ISO; yield h.ngay().get(w.weekBasedYear()) + "-W" + String.format("%02d", h.ngay().get(w.weekOfWeekBasedYear())); }
//                    case 2 -> YearMonth.from(h.ngay()).toString();
//                    case 3 -> "" + h.ngay().getYear();
//                    case 4 -> h.nhanVien(); case 5 -> h.ca(); case 6 -> h.phuongThuc();
//                    case 7 -> d.sku() + " - " + d.ten();
//                    case 8 -> h.sdt().isBlank() ? "Khách lẻ" : h.sdt() + " - " + h.khachHang();
//                    case 9 -> h.khuyenMai().isBlank() ? "Không ghi nhận chương trình" : h.khuyenMai();
//                    case 10 -> h.doiTac() + " / " + h.kyHan() + " tháng";
//                    default -> throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");
//                };
//                items.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
//            }
//            // Phân bổ ưu đãi hóa đơn theo giá trị còn lại của từng dòng, tránh doanh thu âm.
//            Map<HoaDon.Dong, BigDecimal> allocations = allocate(h);
//            items.forEach((key, lines) -> {
//                BigDecimal gross = BigDecimal.ZERO, discount = BigDecimal.ZERO; long qty = 0;
//                for (HoaDon.Dong d : lines) {
//                    gross = gross.add(d.tienHang()); discount = discount.add(d.giamGia()).add(allocations.get(d)); qty += d.soLuong();
//                }
//                Row old = groups.getOrDefault(key, new Row(key, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
//                groups.put(key, new Row(key, old.invoices() + 1, old.quantity() + qty, old.gross().add(gross),
//                        old.discount().add(discount), old.revenue().add(gross.subtract(discount))));
//            });
//        }
//        List<Row> rows = new ArrayList<>(groups.values());
//        if (type >= 4) rows.sort(type == 7 ? Comparator.comparingLong(Row::quantity).reversed()
//                : Comparator.comparing(Row::revenue).reversed());
//        return rows;
//    }
//    private static Map<HoaDon.Dong, BigDecimal> allocate(HoaDon h) {
//        Map<HoaDon.Dong, BigDecimal> result = new HashMap<>(); BigDecimal used = BigDecimal.ZERO;
//        if(h.giamHoaDon().signum()==0){for(HoaDon.Dong d:h.chiTiet())result.put(d,BigDecimal.ZERO);return result;}
//        for (int i = 0; i < h.chiTiet().size(); i++) {
//            HoaDon.Dong d = h.chiTiet().get(i);
//            BigDecimal part = i == h.chiTiet().size() - 1 ? h.giamHoaDon().subtract(used)
//                : h.giamHoaDon().multiply(d.thanhTien()).divide(h.tienHang().subtract(h.giamGia().subtract(h.giamHoaDon())), 0, RoundingMode.DOWN);
//            result.put(d, part); used = used.add(part);
//        }
//        return result;
//    }
//}
