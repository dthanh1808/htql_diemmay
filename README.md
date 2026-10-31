# Quản lý điện máy Gia Thành

Ứng dụng Java Swing, yêu cầu JDK 17 trở lên. Chạy lớp `ui.UI_TrangChu` trong Eclipse.

## Quản lý sản phẩm

- Danh sách, tìm kiếm không phân biệt hoa/thường và dấu tiếng Việt; tìm theo tên, SKU, thương hiệu, danh mục.
- Kết hợp bộ lọc danh mục, thương hiệu, giá bán, bảo hành và trạng thái. Bấm **Áp dụng bộ lọc** hoặc Enter trong ô tìm kiếm; **Xóa bộ lọc** trả về tất cả sản phẩm.
- Sắp xếp tên, giá và thời gian thêm; chọn 10/25/50 dòng mỗi trang. Trạng thái trống và số trang cập nhật theo kết quả lọc.
- **Thêm sản phẩm**, **Xem**, **Sửa** hoạt động ngay trong trang. Nhấp đúp/Enter để xem, F2 để sửa dòng được chọn.
- Trong chỉnh sửa, chọn **Ngừng kinh doanh** hoặc **Đang kinh doanh** rồi lưu. Chỉ sản phẩm đã lưu ở trạng thái ngừng kinh doanh mới được xóa vĩnh viễn, sau bước xác nhận riêng.
- Quản lý danh mục: thêm, sửa tên/mô tả, xóa danh mục trống. Đổi tên giữ nguyên liên kết với sản phẩm. Danh mục chứa sản phẩm, kể cả sản phẩm ngừng kinh doanh, không được xóa.
- SKU duy nhất (không phân biệt hoa/thường), 2–40 ký tự ASCII: chữ, số, `.`, `_`, `-`. Giá bán là số nguyên VND dương, tối đa 999.999.999.999. Bảo hành 0–120 tháng.
- Ảnh PNG/JPEG không bắt buộc, tối đa 2 MB và 16 triệu điểm ảnh. Ảnh được lưu cùng dữ liệu, không phụ thuộc đường dẫn ảnh gốc.
- Báo lỗi trong trang, giữ thông tin khi lưu thất bại và hỏi trước khi bỏ bản nháp. Chỉ hộp chọn file ảnh dùng cửa sổ chọn file hệ thống.

## Lưu dữ liệu

Dự án chưa tích hợp SQL/JDBC. Chức năng Sản phẩm lưu cục bộ vào `data/san-pham.xml`, đường dẫn tính từ thư mục chạy chương trình. File được khởi tạo **một lần** với 8 sản phẩm và 8 danh mục minh họa. Sau đó mọi thay đổi được giữ khi khởi động lại.

Có thể đổi thư mục bằng VM argument `-Dgiathanh.dataDir=D:/GiaThanhData`. Dữ liệu và file khóa không được đưa lên Git. Sao lưu cả file XML nếu cần chuyển sang máy khác.

Ghi file sử dụng file tạm và thay thế nguyên tử, có khóa và kiểm tra phiên bản để ngăn hai cửa sổ ghi đè dữ liệu của nhau. Nếu có xung đột, về danh sách và bấm **Tải lại dữ liệu**, sau đó thực hiện lại thay đổi. Nếu file hỏng, ứng dụng báo lỗi và giữ nguyên file; không tự khởi tạo đè lên file hỏng.

Module hóa đơn/kho chưa được triển khai nên hiện chưa có kiểm tra liên kết sản phẩm với giao dịch hoặc tồn kho. Khi tích hợp các module này, phải bổ sung điều kiện chặn xóa sản phẩm đã phát sinh giao dịch; có thể tiếp tục dùng ngừng kinh doanh để bảo toàn lịch sử. Lớp `KhoSanPham` cho phép thay lớp lưu XML bằng cơ sở dữ liệu mà không đặt SQL trong giao diện.

Khách hàng hiện vẫn là giao diện minh họa, chưa triển khai nghiệp vụ.

## Kiểm tra

Không cần thư viện ngoài hoặc Maven. Trong PowerShell tại thư mục dự án:

```powershell
# JAVA_HOME trỏ đến JDK 17+; hoặc truyền -JavaHome khi chạy script.
.\scripts\check.ps1
# Ví dụ khi JDK chưa nằm trong PATH:
.\scripts\check.ps1 -JavaHome "$env:USERPROFILE\.jdks\temurin-17.0.18"
```

Script biên dịch toàn bộ mã nguồn và chạy kiểm tra nghiệp vụ/lưu trữ cùng các luồng UI trên Swing EDT. Dữ liệu kiểm tra nằm trong `build/test-data`, không đụng đến `data/` của ứng dụng.

Sau khi biên dịch, có thể chạy từ thư mục dự án:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -cp "build/classes;." ui.UI_TrangChu
```
