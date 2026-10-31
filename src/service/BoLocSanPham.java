package service;

import java.util.UUID;

public record BoLocSanPham(String tuKhoa, UUID danhMucId, String thuongHieu,
        KhoangGia khoangGia, Integer baoHanhThang, Boolean dangKinhDoanh) {
    public enum KhoangGia { TAT_CA, DUOI_5_TRIEU, TU_5_DEN_15_TRIEU, TREN_15_TRIEU }
    public static BoLocSanPham tatCa() { return new BoLocSanPham("", null, null, KhoangGia.TAT_CA, null, null); }
}
