# Quản lý điện máy Gia Thành

Ứng dụng Java Swing, yêu cầu JDK 17 trở lên. Chạy lớp `ui.UI_TrangChu` trong Eclipse.

## Quản lý sản phẩm

- Danh sách, tìm kiếm không phân biệt hoa/thường và dấu tiếng Việt; tìm theo tên, SKU, thương hiệu, danh mục.
- Kết hợp bộ lọc danh mục, thương hiệu, giá bán, bảo hành và trạng thái. Bấm **Áp dụng bộ lọc** hoặc Enter trong ô tìm kiếm; **Xóa bộ lọc** trả về tất cả sản phẩm.
- Sắp xếp tên, giá và thời gian thêm; chọn 10/25/50 dòng mỗi trang. Trạng thái trống và số trang cập nhật theo kết quả lọc.
- **Thêm sản phẩm**, **Xem**, **Sửa** hoạt động ngay trong trang. Nhấp đúp/Enter để xem, F2 để sửa dòng được chọn.
- Trong chỉnh sửa, chọn **Ngừng kinh doanh** hoặc **Đang kinh doanh** rồi lưu. Chỉ sản phẩm đã lưu ở trạng thái ngừng kinh doanh mới được xóa, sau bước xác nhận riêng.
- Quản lý danh mục: thêm, sửa tên/mô tả, xóa danh mục trống. Đổi tên giữ nguyên liên kết với sản phẩm. Danh mục chứa sản phẩm, kể cả sản phẩm ngừng kinh doanh, không được xóa.
- SKU duy nhất (không phân biệt hoa/thường), 2–40 ký tự ASCII: chữ, số, `.`, `_`, `-`. Giá bán là số nguyên VND dương, tối đa 999.999.999.999. Bảo hành 0–120 tháng.
- Ảnh PNG/JPEG không bắt buộc, tối đa 2 MB và 16 triệu điểm ảnh. Nội dung ảnh giữ trong Entity trong phiên chạy hiện tại.
- Báo lỗi trong trang, giữ thông tin khi lưu thất bại và hỏi trước khi bỏ bản nháp. Chỉ hộp chọn file ảnh dùng cửa sổ chọn file hệ thống.

## Cấu trúc OOP và dữ liệu tạm

Luồng xử lý: **Java Swing → DAO → ArrayList<Entity>**.

```text
src/
  entity/
    SanPham.java      # Thuộc tính, constructor, getter/setter, constructor sao chép
    DanhMuc.java
  dao/
    DuLieuTam.java    # Hai ArrayList và 8 sản phẩm / 8 danh mục mẫu
    SanPhamDAO.java   # Thêm, sửa, xóa, tìm kiếm, sắp xếp, kiểm tra đầu vào
    DanhMucDAO.java   # Thêm, sửa, xóa danh mục và đếm sản phẩm
  ui/
    UI_SanPham.java   # Swing, hiển thị, nhận dữ liệu và gọi DAO
```

Entity là class thông thường có các thuộc tính `private`, constructor, getter/setter. DAO dùng vòng lặp `for`, `ArrayList`, `Comparator` để xử lý dữ liệu. Giá bán dùng `BigDecimal` để tránh sai số.

Chưa kết nối SQL và không lưu dữ liệu ra file. Các DAO dùng chung một `DuLieuTam` trong suốt lần chạy ứng dụng. **Đóng và mở lại chương trình sẽ trở về 8 sản phẩm và 8 danh mục mẫu; các thay đổi trước đó không được giữ lại.** Nút **Làm mới danh sách** chỉ cập nhật giao diện từ các ArrayList hiện có.

DAO trả về bản sao Entity; sửa một đối tượng bằng setter chỉ có hiệu lực khi gọi `capNhat`. Mã nội bộ dạng `SP0001`, `DM001` được cấp tự động. Đổi SKU hoặc tên danh mục vẫn giữ liên kết theo mã nội bộ.

Khi nối SQL sau này, thay phần xử lý ArrayList trong DAO bằng truy vấn cơ sở dữ liệu. UI tiếp tục gọi các phương thức DAO. Module hóa đơn/kho chưa triển khai; khi bổ sung phải chặn xóa sản phẩm đã phát sinh giao dịch hoặc còn tồn kho.

Khách hàng hiện vẫn là giao diện minh họa, chưa triển khai nghiệp vụ.

## Kiểm tra

Không cần thư viện ngoài hoặc Maven. Trong PowerShell tại thư mục dự án:

```powershell
# JAVA_HOME trỏ đến JDK 17+; hoặc truyền -JavaHome khi chạy script.
.\scripts\check.ps1
# Ví dụ khi JDK chưa nằm trong PATH:
.\scripts\check.ps1 -JavaHome "$env:USERPROFILE\.jdks\temurin-17.0.18"
```

Script biên dịch toàn bộ mã nguồn và chạy kiểm tra DAO cùng các luồng UI trên Swing EDT. Mỗi bài kiểm tra dùng một `DuLieuTam` riêng; ảnh kiểm tra nằm trong `build/test-data`.

Sau khi biên dịch, có thể chạy từ thư mục dự án:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -cp "build/classes;." ui.UI_TrangChu
```
