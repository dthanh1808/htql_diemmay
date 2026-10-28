# Hệ thống quản lý Điện máy Gia Thành

Ứng dụng Java Swing, sử dụng JDK 17 trở lên và thư viện có sẵn của Java.
Hai phân hệ Sản phẩm và Khách hàng hiện chạy bằng dữ liệu mẫu trong bộ nhớ.
Thay đổi được dùng chung giữa các màn hình trong cùng lần chạy, và mất khi tắt ứng dụng.

## Cấu trúc

```text
src/
  app/         Main.java: điểm bắt đầu chạy ứng dụng
  entity/      Các lớp danh mục, sản phẩm, khách hàng, hóa đơn, chính sách, CSKH
  dao/         Thêm, sửa, xóa, tra cứu và kiểm tra nghiệp vụ
               DuLieuMau.java: các danh sách dùng chung và dữ liệu khởi tạo
  connectDB/   Khung kết nối JDBC để bổ sung SQL sau này, hiện chưa được gọi
  ui/          Màn hình Swing và các hộp thoại nhập/xem dữ liệu
 tests/        ChucNangTest.java: kiểm tra nghiệp vụ không cần thư viện ngoài
 icon/         Biểu tượng giao diện
 image/        Ảnh giao diện
```

DAO thao tác với `ArrayList`, giao diện chỉ gọi DAO để đọc và lưu dữ liệu.
Form thêm và sửa dùng chung; nút Hủy không lưu các thông tin đang nhập.

## Các nhóm chức năng đã thực hiện

Thứ tự triển khai: **3 → 2 → 1 → 5 → 4 → 6 → 7**.

| Nhóm | Nội dung |
| --- | --- |
| 3 | Thêm, sửa, xóa danh mục; không xóa danh mục đang được sử dụng. |
| 2 | Thêm/sửa sản phẩm, nhập/quét mã bằng máy quét dạng bàn phím, chọn ảnh JPG/PNG tối đa 5 MB, kiểm tra dữ liệu, đổi trạng thái và xóa theo điều kiện hóa đơn. |
| 1 | Danh sách, tìm kiếm, lọc kết hợp, phân trang và xem chi tiết sản phẩm. |
| 5 | Đăng ký/sửa khách hàng, kiểm tra SĐT và email, đổi trạng thái, xóa theo điều kiện lịch sử giao dịch. |
| 4 | Tra cứu/lọc/phân trang khách hàng, mã thẻ, hồ sơ, lịch sử mua hàng, chi tiết hóa đơn, tổng chi tiêu và số hóa đơn. |
| 6 | Cấu hình ngưỡng và ưu đãi của bốn hạng; tự động xét lại hạng từ chi tiêu, kể cả khi mô phỏng hoàn tiền. |
| 7 | Ghi chú CSKH, xem lịch sử, chọn đối tượng theo hạng/danh mục đã mua, soạn thông báo và lưu lịch sử gửi mô phỏng. |

## Chạy trong Eclipse

1. Chọn JDK 17 trở lên trong Java Build Path và cấu hình Run.
2. Refresh project, sau đó chọn **Project → Clean**.
3. Chạy `src/app/Main.java` bằng **Run As → Java Application**.
4. Chọn **Sản phẩm** hoặc **Khách hàng** ở menu bên trái.

Giao diện giữ bố cục khung trắng bo góc, bộ lọc gọn, nhãn màu và icon thao tác trong bảng:

- Sản phẩm: bấm icon mắt/bút để xem/sửa; chuột phải trên dòng để đổi trạng thái/xóa. Chọn **Giá → Tùy chọn...** để nhập khoảng giá.
- Khách hàng: bấm icon mắt để mở hồ sơ; **Tìm kiếm nâng cao** để lọc khoảng chi tiêu. Chuột phải trên dòng để mở chính sách thành viên, CSKH và thao tác khách hàng.
- Nút **Xuất file** lưu toàn bộ kết quả lọc khách hàng ra CSV, bao gồm các trang chưa hiển thị.
- Hai bảng Sản phẩm/Khách hàng tự giãn đầy chiều rộng vùng chứa; không cho kéo đổi kích thước hoặc thứ tự cột.

Cũng có thể chạy riêng `UI_SP.java`, `UI_KH.java` hoặc chạy trang chủ `UI_TrangChu.java`.
Màn hình đăng nhập cũ chưa có xử lý xác thực; bản mẫu hiện mở thẳng trang chủ với vai trò quản lý mẫu.
Các phân hệ còn lại hiển thị thông báo đang phát triển.

## Chạy và kiểm tra bằng PowerShell

Thay đường dẫn `$jdkPath` bằng thư mục JDK 17 trở lên trên máy, rồi chạy từ thư mục project:

```powershell
$jdkPath = 'C:\duong-dan-den-jdk-17'
$sources = @(Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
New-Item -ItemType Directory -Path bin -Force | Out-Null
& "$jdkPath\bin\javac.exe" --release 17 -encoding UTF-8 -d bin $sources tests/ChucNangTest.java
& "$jdkPath\bin\java.exe" -cp 'bin;.' ChucNangTest
& "$jdkPath\bin\java.exe" -cp 'bin;.' app.Main
```

Kết quả kiểm tra nghiệp vụ: `PASS: 54 kiểm tra nghiệp vụ.`
Không dùng Java 8 để chạy các lớp đã biên dịch cho Java 17.

## Dữ liệu và thao tác thử

- Có sẵn 8 danh mục, 8 sản phẩm, 12 khách hàng, 8 hóa đơn và 2 chương trình khuyến mãi mẫu.
- Hạng mặc định: Member dưới 10 triệu; Silver từ 10 triệu; Gold từ 30 triệu; Diamond từ 60 triệu. Ưu đãi tương ứng 0%, 3%, 5%, 8%.
- Tổng chi tiêu bằng tổng tiền các hóa đơn mẫu đã thanh toán trừ số tiền đã hoàn. Không sử dụng điểm tích lũy.
- Có thể mở hồ sơ `KH000123`, chọn hóa đơn `HD2609250001`, thử hoàn 4.000.000 đồng để thấy hạng từ Diamond xuống Gold.
- Tiền nhập dưới dạng số, không dùng dấu phân cách hàng nghìn; ngày theo `dd/MM/yyyy`.
- Mã thẻ trong bản mẫu được sinh từ mã khách hàng, ví dụ `KH000123` có thẻ `TV000123`.
- Ảnh sản phẩm lưu bằng đường dẫn đến tệp đã chọn; giữ tệp ở vị trí đó trong khi chạy.
- Nút **Gửi mô phỏng** chỉ thêm lịch sử có trạng thái **Mô phỏng - chưa gửi thực tế**. Chưa tích hợp dịch vụ SMS, Email hoặc Zalo.
- Nút **Hoàn tiền mẫu** chỉ thay đổi số liệu mẫu để kiểm tra hạng; chưa xử lý hoàn tiền hay thanh toán thực tế.
