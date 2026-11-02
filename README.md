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
