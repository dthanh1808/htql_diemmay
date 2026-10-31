package entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SanPham(UUID id, String sku, String ten, UUID danhMucId, String thuongHieu,
        BigDecimal giaBan, int baoHanhThang, boolean dangKinhDoanh, String moTa,
        String anhBase64, Instant taoLuc, Instant capNhatLuc) { }
