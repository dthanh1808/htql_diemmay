<<<<<<< HEAD
# HE-THONG-QUAN-LY-THONG-TIN-VA-HOAT-DONG-KINH-DOANH-TAI-TRUNG-TAM-MUA-SAM-DIEN-MAY-GIA-THANH

## Hóa đơn và báo cáo

Yêu cầu JDK 21 trở lên. Trong Eclipse, chạy `ui.UI_TrangChu` (màn hình đăng nhập hiện chưa có xử lý xác thực).

### Chuẩn bị SQL Server lần đầu

Ứng dụng mặc định dùng database **QuanLyBanHang** theo `sql/QuanLyBanHang.sql`, không còn mặc định ghi XML.

1. Mở **PowerShell bằng Run as administrator** và chạy `Start-Service -Name 'MSSQL$SQLEXPRESS'`. Nếu dùng SSMS, Server name là `.\SQLEXPRESS`, Authentication chọn Windows Authentication. Máy đang dùng có instance SQLEXPRESS, TCP được cấu hình cổng 1433.
2. Ở thư mục project, chạy `powershell -ExecutionPolicy Bypass -File .\setup-database.ps1 -WithDemoData`. Script kết nối bằng tài khoản Windows hiện tại, tạo database nếu chưa có, rồi chạy script bổ sung cột lưu lịch sử. Nếu database đã tồn tại, không chạy lại CREATE TABLE. Bỏ `-WithDemoData` nếu không muốn thêm dữ liệu demo. Có thể truyền `-Server 'TEN_MAY\INSTANCE'` khi dùng máy khác.
3. Sao chép `config/database.example.properties` thành `config/database.properties`, điền SQL login và mật khẩu thực tế. Tài khoản Windows trong SSMS không đồng nghĩa với tài khoản SQL `sa`. Nếu chưa có SQL login, trong SSMS bật **Server Properties → Security → SQL Server and Windows Authentication mode**, khởi động lại dịch vụ, tạo login ở **Security → Logins**, chọn **User Mapping → QuanLyBanHang** và cấp quyền đọc/ghi (`db_datareader`, `db_datawriter`). Đặt thông tin login đó trong file cấu hình; không cần dùng `sa`.
4. Chạy `ui.UI_TrangChu`. Dữ liệu demo gồm nhân viên `NV001`, thành viên SĐT `0901234567`, hai sản phẩm và ca đang mở của ngày chạy script. Không có sẵn hóa đơn: lập giao dịch để có dữ liệu báo cáo. Chạy lại script với `-WithDemoData` vào ngày khác để tạo ca demo của ngày đó; script không đặt lại tồn kho hoặc doanh thu cũ.

Nếu tài khoản Windows hiện tại có quyền quản trị SQL Server, có thể chạy `powershell -ExecutionPolicy Bypass -File .\configure-database.ps1` thay cho tạo login bằng tay ở bước 3. Script tạo login riêng với mật khẩu ngẫu nhiên và lưu file cấu hình được gitignore; không hiển thị mật khẩu, không thay đổi tài khoản `sa`. Login chỉ được đọc các bảng phục vụ bán hàng/báo cáo, thêm hóa đơn/chi tiết/trả góp và cập nhật tồn kho/chi tiêu/doanh thu ca. Script không ghi đè cấu hình đã có.

Sau khi biên dịch, kiểm tra kết nối chỉ đọc bằng `java -cp "bin;src;lib/*" tools.KiemTraKetNoi`. Chạy ứng dụng trong thư mục project để tìm đúng `config/database.properties`; nếu đặt working directory khác, truyền JVM option `-Dgiathanh.db.config=D:/duong-dan/config/database.properties`.

Nếu bước 1 báo Access denied, kiểm tra PowerShell đã chạy bằng quyền Administrator. Không cần cài lại SQL Server. Nếu dùng instance/cổng khác, cập nhật `db.url` trong file cấu hình.

Thứ tự SQL khi chạy thủ công trong SSMS: `QuanLyBanHang.sql` (chỉ lần đầu) → `02_BoSungHoaDon.sql` → `03_DuLieuMau.sql` (tùy chọn). Script 02 có thể chạy lại và không xóa dữ liệu. Script 03 chỉ dành cho thử giao diện, không tự chạy khi mở ứng dụng.

### Sử dụng

1. Chọn **Hóa đơn → Lập hóa đơn** để chuyển sang màn hình riêng; nhập mã nhân viên trong database, bấm **Chọn / Bỏ chọn ca** và chọn ca đang mở được phân công trong ngày. Nút này chọn ca thực tế, không tự tạo hoặc đóng ca của phân hệ ca làm việc.
2. Khách lẻ không cần nhập tên. Với thành viên, nhập SĐT rồi bấm **Tìm thành viên theo SĐT** để tải tên/hạng từ SQL. **Thêm sản phẩm** cho phép chọn từ sản phẩm đang kinh doanh và còn tồn kho; tên, danh mục, đơn giá lấy từ database. Nhập số lượng và giảm tiền dòng. Các ô tiền nhập bằng số nguyên VNĐ, không có dấu phân cách.
3. Hạng Member/Silver/Gold/Diamond được giảm 0%/3%/5%/8% trên tổng tiền hàng. Giảm dòng là số tiền cố định của cả dòng, tối đa 50% tiền hàng dòng; tổng tất cả giảm giá không quá 70% hóa đơn. Khách lẻ không bắt buộc thông tin cá nhân. Thành viên phải có tên và SĐT 10 chữ số.
4. Nhập tiền khách đưa, số chuyển khoản hoặc tiền trả trước. Với trả góp, nhập đối tác, kỳ hạn và xác nhận hồ sơ đã được duyệt, ký. Tích xác nhận thanh toán rồi lưu. Mã tự sinh `HDYYMMDDxxxx`; hóa đơn đã lưu không chỉnh sửa. Ở danh sách, chọn **Xem chi tiết** hoặc bấm đúp dòng để mở màn hình chi tiết riêng, in hoặc xuất PDF.
5. Chọn **Báo cáo**, chọn loại và bộ lọc rồi **Xem báo cáo**. Có 11 loại: ngày, tuần ISO, tháng, năm, nhân viên, ca, phương thức, sản phẩm bán chạy, khách hàng, chương trình khuyến mãi và trả góp. Xuất Excel `.xlsx` hoặc PDF theo dữ liệu đang hiển thị.

Các màn hình là JPanel riêng trong CardLayout của trang chủ: `PNL_HoaDon` (danh sách), `PNL_LapHoaDon` (lập và thanh toán), `PNL_ChiTietHoaDon` (xem, in, xuất PDF), `PNL_BaoCao` (thống kê). Nút Thanh toán trên màn hình Bán hàng dẫn đến panel Lập hóa đơn. Giỏ hàng mẫu của màn hình Bán hàng chưa có dữ liệu nghiệp vụ nên không tự chuyển vào hóa đơn mới. Nút Quay lại danh sách và breadcrumb cho phép trở về danh sách; quay lại từ biểu mẫu giữ nội dung đang nhập, nút Xóa nội dung đặt lại biểu mẫu.

### Dữ liệu và giới hạn tích hợp

- `HoaDonRepository` mặc định dùng JDBC/SQL Server. Không tự chuyển về XML nếu mất kết nối. Các bảng `HoaDon`, `ChiTietHoaDon`, `GiaoDichTraGop`, `SanPham`, `KhachHang`, `CaLamViec` được cập nhật trong **một transaction**; lỗi ở bất kỳ bước nào sẽ rollback. Cấp mã được bảo vệ bằng khóa transaction; trừ tồn kho có điều kiện để chặn bán vượt tồn. Kiểm tra lại nhân viên hoạt động, lịch phân công/ca mở, dữ liệu sản phẩm/giá/tồn kho và thông tin thành viên trước khi ghi.
- Script 02 bổ sung cột lưu tiền khách đưa, tên/hạng/SĐT khách hàng, tên chương trình, tên sản phẩm và danh mục tại thời điểm bán. Hóa đơn cũ chưa có bản chụp dùng thông tin danh mục hiện tại; hóa đơn cũ không có tiền khách đưa dùng số thanh toán tối thiểu, không khôi phục được tiền thừa đã đưa trước đây. Thành tiền dòng lưu sau giảm dòng; tiền giảm giá trên header là tổng tất cả khoản giảm. Nếu tổng header và chi tiết không khớp, báo lỗi thay vì tự sửa báo cáo.
- Truy vấn JDBC chạy ngoài luồng giao diện; khóa thao tác trong khi ghi để tránh bấm lưu lặp. Các panel vẫn riêng biệt. Cấu hình ưu tiên JVM properties `giathanh.db.url/user/password`, tiếp đến biến môi trường `GIATHANH_DB_URL/USER/PASSWORD`, rồi file `config/database.properties`. File thật được gitignore. `ConnectDB` mở kết nối riêng cho từng transaction, không chia sẻ `autoCommit` giữa các DAO.
- Chưa nối xác thực đăng nhập, cấu hình ngưỡng tự đổi hạng, đồng bộ bảo hành hoặc gửi hồ sơ đến đối tác tài chính. Chi tiêu tích lũy và doanh thu ca được cộng khi lưu thành công; chưa tự suy đoán ngưỡng đổi hạng. Khuyến mãi nếu nhập phải khớp mã/tên chương trình đang kích hoạt trong thời gian áp dụng; điều kiện theo sản phẩm/đối tượng của các dạng chương trình phức tạp chưa được mô hình hóa đủ trong schema, nên các khoản giảm vẫn do nhân viên nhập và bị giới hạn theo quy định 50%/70%. Trả góp lưu dư nợ gốc và số tiền gốc/tháng làm tròn lên; lịch thu nợ, lãi/phí hợp đồng chưa được mô hình hóa.
- Có thể bật rõ chế độ offline bằng `-Dgiathanh.storage=xml`, đổi đường dẫn bằng `-Dgiathanh.data=D:/duong-dan/hoa-don.xml`. Chế độ này để kiểm thử/demo độc lập; không tự nhập dữ liệu XML cũ vào database. Kiểm thử mặc định sử dụng kho riêng trong `build/feature-tests`.
- `PNL_BaoCao()` đang là chế độ quản lý của ứng dụng demo. Khi tích hợp đăng nhập, truyền `new BaoCaoService.Access(maNhanVien, false)` vào constructor để giới hạn nhân viên bán hàng: doanh số của đúng nhân viên và sản phẩm bán chạy. Kiểm tra quyền được thực hiện trong service và trên lựa chọn giao diện. Không dùng constructor mặc định cho người dùng chưa xác thực trong bản triển khai thực tế.
- Doanh thu = tiền hàng − tổng giảm giá; trả góp ghi nhận toàn bộ giá trị bán, không phải tiền đã thu. Ưu đãi hóa đơn được phân bổ theo tiền còn lại trên từng dòng; phần lẻ được dồn vào dòng cuối để tổng doanh thu không lệch. Lọc danh mục chỉ tính các dòng phù hợp. Một hóa đơn có thể xuất hiện ở nhiều nhóm sản phẩm, nên không cộng số hóa đơn các nhóm để lấy số giao dịch duy nhất.
- Báo cáo khuyến mãi tổng hợp giao dịch theo tên chương trình ghi trên hóa đơn; không đo tác động nhân quả hoặc lợi nhuận. PDF là hóa đơn bán hàng nội bộ, không có ký số hoặc tích hợp hóa đơn thuế. PDF xuất nhiều trang dưới dạng hình để giữ dấu tiếng Việt; Excel là OOXML với giá trị tiền dạng số, chuỗi không được thực thi như công thức.

### Biên dịch và kiểm tra

Chạy `powershell -ExecutionPolicy Bypass -File .\build-and-test.ps1`. Script biên dịch toàn bộ source và chạy kiểm thử độc lập, không cần cài thư viện mới hoặc kết nối cơ sở dữ liệu. Dữ liệu kiểm thử và ảnh kiểm tra giao diện ở `build/feature-tests`, tách khỏi kho thật.

Khi SQL Server đã sẵn sàng và đã chạy script 02, dùng `powershell -ExecutionPolicy Bypass -File .\build-and-test.ps1 -SqlIntegration` để kiểm tra đọc/ghi JDBC, tồn kho, chi tiêu, doanh thu ca, trả góp, lịch sử và rollback. Bộ kiểm tra SQL tạo fixture trong transaction riêng, chặn commit trong repository rồi rollback toàn bộ; không để lại hóa đơn hoặc nhân viên test. Không coi kiểm thử offline là bằng chứng đã kết nối được SQL Server.

Sau khi biên dịch, chạy `java -cp "bin;src;lib/*" ui.UI_TrangChu`.
=======
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
>>>>>>> 957a42b1ddd343adedd740681bfd7a592402b483
