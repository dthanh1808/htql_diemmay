package dao;

import entity.DuLieuSanPham;
import java.io.IOException;

public interface KhoSanPham {
    /** null khi chưa có file dữ liệu; file lỗi phải báo lỗi, không tự đặt lại dữ liệu. */
    DuLieuSanPham doc() throws IOException;
    void luu(DuLieuSanPham duLieu, long phienBanHienTai) throws IOException;
}
