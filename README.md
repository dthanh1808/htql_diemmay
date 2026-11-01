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

## Quản lý khách hàng

- Đăng ký, cập nhật, xem chi tiết, ngừng hoạt động/kích hoạt lại trong màn chỉnh sửa. Hồ sơ mới bắt đầu với chi tiêu 0 đ và hạng Member; tùy chọn nhận khuyến mãi mặc định chưa chọn.
- Họ tên 2–100 ký tự; số di động Việt Nam 10 chữ số, nhận cả định dạng `+84` và khoảng trắng. Số điện thoại và email (nếu nhập) không trùng, kể cả hồ sơ ngừng hoạt động. Ngày sinh tùy chọn, đúng `dd/MM/yyyy`, không ở tương lai hoặc quá 120 năm.
- Tìm theo mã, tên, số điện thoại, email, không phân biệt dấu và hoa/thường. Kết hợp hạng, trạng thái và khoảng ngày đăng ký (bao gồm cả hai ngày biên). Enter hoặc **Áp dụng bộ lọc** để áp dụng; ngày không hợp lệ giữ nguyên kết quả trước đó.
- Sắp xếp tên, chi tiêu, ngày đăng ký; phân trang 10/25/50 dòng. **Xuất CSV** xuất toàn bộ kết quả đã lọc ở mọi trang, UTF-8, hỏi trước khi ghi đè file.
- Nút **Xem**, nhấp đúp/Enter mở hồ sơ; **Sửa** hoặc F2 mở chỉnh sửa; **Lịch sử** mở hoạt động. Chi tiết gồm hồ sơ, lịch sử mua hàng, lịch sử hoạt động và nội dung thông báo nội bộ.
- Chỉ xóa hồ sơ đã lưu ở trạng thái ngừng hoạt động và chưa có giao dịch/thông báo. Khách đã có nghiệp vụ được giữ lại để tra cứu lịch sử. Xóa và bỏ bản nháp đều có bước xác nhận ngay trong trang.
- Tổng chi tiêu là tổng thực thanh toán từ giao dịch mua hàng; không cho nhập tùy ý trong hồ sơ. `KhachHangDAO.ghiNhanMuaHang` là điểm tích hợp cho module hóa đơn sau này, kiểm tra số tiền VND nguyên dương, mã hóa đơn duy nhất và khách đang hoạt động. Hiện có giao dịch minh họa, chưa nối module bán hàng/hoàn tiền.
- Cấu hình 4 hạng **Member → Silver → Gold → Diamond**: ngưỡng chi tiêu tăng dần, Member bắt đầu từ 0; ưu đãi 0–100% và không giảm ở hạng cao hơn. Lưu chính sách tính lại hạng theo chi tiêu hiện có. Ưu đãi mới chỉ là cấu hình, chưa áp dụng vào hóa đơn.
- **Gửi khuyến mãi** chọn nhóm hạng, xem danh sách người nhận và xác nhận trước khi tạo thông báo. Chỉ chọn khách đang hoạt động, đã đồng ý nhận khuyến mãi. Thông báo có nội dung đầy đủ trong chi tiết khách hàng và được ghi vào lịch sử.

**Khuyến mãi hiện là thông báo nội bộ trong ArrayList, chưa gửi email/SMS ra ngoài.** Toàn bộ hồ sơ, lịch sử, chính sách và thông báo thay đổi chỉ tồn tại trong phiên chạy; khởi động lại trở về dữ liệu mẫu. Chỉ thao tác xuất CSV mới tạo file theo vị trí người dùng chọn.

## Cấu trúc OOP và dữ liệu tạm

Luồng xử lý: **Java Swing → DAO → ArrayList<Entity>**.

```text
src/
  entity/
    SanPham.java      # Thuộc tính, constructor, getter/setter, constructor sao chép
    DanhMuc.java
    KhachHang.java
    HangThanhVien.java
    GiaoDichKhachHang.java
    LichSuKhachHang.java
    ThongBaoKhuyenMai.java
  dao/
    DuLieuTam.java    # ArrayList dùng chung, dữ liệu mẫu sản phẩm và khách hàng
    SanPhamDAO.java   # Thêm, sửa, xóa, tìm kiếm, sắp xếp, kiểm tra đầu vào
    DanhMucDAO.java   # Thêm, sửa, xóa danh mục và đếm sản phẩm
    KhachHangDAO.java # Hồ sơ, giao dịch, lịch sử, hạng và thông báo nội bộ
  ui/
    UI_SanPham.java   # Swing, hiển thị, nhận dữ liệu và gọi DAO
    UI_KhachHang.java # Danh sách, form, chi tiết, chính sách, khuyến mãi
```

Entity là class thông thường có các thuộc tính `private`, constructor, getter/setter. DAO dùng vòng lặp `for`, `ArrayList`, `Comparator` để xử lý dữ liệu. Giá bán dùng `BigDecimal` để tránh sai số.

Chưa kết nối SQL và không tự lưu dữ liệu ra file. Các DAO dùng chung một `DuLieuTam` trong suốt lần chạy ứng dụng. **Đóng và mở lại chương trình sẽ trở về 8 sản phẩm, 8 danh mục và 8 khách hàng mẫu; các thay đổi trước đó không được giữ lại.** Nút **Làm mới danh sách** ở Sản phẩm chỉ cập nhật giao diện từ các ArrayList hiện có.

DAO trả về bản sao Entity; sửa một đối tượng bằng setter chỉ có hiệu lực khi gọi `capNhat`. Mã nội bộ dạng `SP0001`, `DM001`, `KH000001`, `TB000001` được cấp tự động, không tái sử dụng mã đã xóa. Đổi SKU, thông tin khách hàng hoặc tên danh mục vẫn giữ liên kết theo mã nội bộ.

Khi nối SQL sau này, thay phần xử lý ArrayList trong DAO bằng truy vấn cơ sở dữ liệu. UI tiếp tục gọi các phương thức DAO. Module hóa đơn/kho chưa triển khai; khi bổ sung phải chặn xóa sản phẩm đã phát sinh giao dịch hoặc còn tồn kho.

## Kiểm tra

Không cần thư viện ngoài hoặc Maven. Trong PowerShell tại thư mục dự án:

```powershell
# JAVA_HOME trỏ đến JDK 17+; hoặc truyền -JavaHome khi chạy script.
.\scripts\check.ps1
# Ví dụ khi JDK chưa nằm trong PATH:
.\scripts\check.ps1 -JavaHome "$env:USERPROFILE\.jdks\temurin-17.0.18"
```

Script biên dịch toàn bộ mã nguồn và chạy kiểm tra DAO cùng các luồng UI Sản phẩm/Khách hàng trên Swing EDT. Mỗi bài kiểm tra dùng một `DuLieuTam` riêng; ảnh kiểm tra nằm trong `build/test-data`.

Sau khi biên dịch, có thể chạy từ thư mục dự án:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -cp "build/classes;." ui.UI_TrangChu
```
