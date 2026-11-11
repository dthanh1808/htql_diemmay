//package service;
//
//import entity.HoaDon;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.*;
//
//public final class HoaDonService {
//    private HoaDonService() {}
//    public static BigDecimal uuDai(String hang, BigDecimal base) {
//        int percent = switch (hang) { case "Silver" -> 3; case "Gold" -> 5; case "Diamond" -> 8; default -> 0; };
//        return base.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
//    }
//    public static void validate(HoaDon h) {
//        require(h.ngay() != null && !h.nhanVien().isBlank() && !h.ca().isBlank(), "Phải có nhân viên và ca đang mở.");
//        require(List.of("Khách lẻ", "Member", "Silver", "Gold", "Diamond").contains(h.hang()), "Hạng khách hàng không hợp lệ.");
//        require(h.sdt().isEmpty() || h.sdt().matches("[0-9]{10}"), "SĐT phải gồm 10 chữ số.");
//        require(h.hang().equals("Khách lẻ") || (!h.khachHang().isBlank() && !h.sdt().isEmpty()), "Thành viên cần họ tên và SĐT.");
//        require(!h.chiTiet().isEmpty(), "Hóa đơn phải có sản phẩm.");
//        Set<String> skus = new HashSet<>();
//        for (HoaDon.Dong d : h.chiTiet()) {
//            require(!d.sku().isBlank() && !d.ten().isBlank() && !d.danhMuc().isBlank(), "Nhập đủ SKU, tên và danh mục.");
//            require(skus.add(d.sku().toUpperCase(Locale.ROOT)), "SKU bị trùng trong hóa đơn.");
//            require(d.soLuong() > 0 && d.donGia().signum() > 0, "Số lượng và đơn giá phải lớn hơn 0.");
//            require(d.giamGia().signum() >= 0 && d.giamGia().compareTo(d.tienHang().multiply(new BigDecimal("0.5"))) <= 0,
//                    "Giảm giá sản phẩm phải từ 0 đến 50% tiền hàng của dòng.");
//        }
//        require(h.giamHoaDon().signum() >= 0 && h.giamGia().compareTo(h.tienHang().multiply(new BigDecimal("0.7"))) <= 0,
//                "Tổng giảm giá không được vượt quá 70% tổng tiền hàng.");
//        require(List.of("Tiền mặt", "Chuyển khoản", "Trả góp").contains(h.phuongThuc()), "Phương thức không hợp lệ.");
//        require(h.khachDua().signum() >= 0, "Số tiền thanh toán không được âm.");
//        if (h.phuongThuc().equals("Trả góp")) {
//            require(h.trangThai().equals(HoaDon.TRA_GOP), "Trạng thái trả góp không hợp lệ.");
//            require(!h.doiTac().isBlank() && List.of(6,9,12,18,24).contains(h.kyHan()), "Nhập đối tác và kỳ hạn trả góp hợp lệ.");
//            require(h.khachDua().compareTo(h.tongTien()) <= 0, "Tiền trả trước vượt tổng thanh toán.");
//        } else {
//            require(h.trangThai().equals(HoaDon.DA_TT), "Chỉ lưu khi thanh toán thành công.");
//            require(h.khachDua().compareTo(h.tongTien()) >= 0, "Khách thanh toán chưa đủ tiền.");
//            if (h.phuongThuc().equals("Chuyển khoản")) require(h.khachDua().compareTo(h.tongTien()) == 0, "Số chuyển khoản phải bằng tổng thanh toán.");
//        }
//    }
//    private static void require(boolean valid, String message) { if (!valid) throw new IllegalArgumentException(message); }
//}
