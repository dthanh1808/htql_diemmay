# HE-THONG-QUAN-LY-THONG-TIN-VA-HOAT-DONG-KINH-DOANH-TAI-TRUNG-TAM-MUA-SAM-DIEN-MAY-GIA-THANH

## Hóa đơn và báo cáo

Yêu cầu JDK 21 trở lên. Trong Eclipse, chạy `ui.UI_TrangChu` (màn hình đăng nhập hiện chưa có xử lý xác thực).

### Chuẩn bị SQL Server lần đầu

Ứng dụng mặc định dùng database **QuanLyBanHang** theo `sql/QuanLyBanHang.sql`, không còn mặc định ghi XML.

1. Mở **PowerShell bằng Run as administrator** và chạy `Start-Service -Name 'MSSQL$SQLEXPRESS'`. Nếu dùng SSMS, Server name là `.\SQLEXPRESS`, Authentication chọn Windows Authentication. Máy đang dùng có instance SQLEXPRESS, TCP được cấu hình cổng 1433.
2. Ở thư mục project, chạy `powershell -ExecutionPolicy Bypass -File .\setup-database.ps1 -WithDemoData`. Script kết nối bằng tài khoản Windows hiện tại, tạo database nếu chưa có, rồi chạy script bổ sung cột lưu lịch sử. Nếu database đã tồn tại, không chạy lại CREATE TABLE. Bỏ `-WithDemoData` nếu không muốn thêm dữ liệu demo. Có thể truyền `-Server 'TEN_MAY\INSTANCE'` khi dùng máy khác.
3. Sao chép `config/database.example.properties` thành `config/database.properties`, điền SQL login và mật khẩu thực tế. Tài khoản Windows trong SSMS không đồng nghĩa với tài khoản SQL `sa`. Nếu chưa có SQL login, trong SSMS bật **Server Properties → Security → SQL Server and Windows Authentication mode**, khởi động lại dịch vụ, tạo login ở **Security → Logins**, chọn **User Mapping → QuanLyBanHang** và cấp quyền đọc/ghi (`db_datareader`, `db_datawriter`). Đặt thông tin login đó trong file cấu hình; không cần dùng `sa`.
4. Chạy `ui.UI_TrangChu`. Dữ liệu demo gồm nhân viên `NV001`, thành viên SĐT `0901234567`, hai sản phẩm và ca đang mở của ngày chạy script. Không có sẵn hóa đơn: lập giao dịch để có dữ liệu báo cáo. Chạy lại script với `-WithDemoData` vào ngày khác để tạo ca demo của ngày đó; script không đặt lại tồn kho hoặc doanh thu cũ.

Nếu tài khoản Windows hiện tại có quyền quản trị SQL Server, có thể chạy `powershell -ExecutionPolicy Bypass -File .\configure-database.ps1` thay cho tạo login bằng tay ở bước 3. Script tạo login riêng với mật khẩu ngẫu nhiên và lưu file cấu hình được gitignore; không hiển thị mật khẩu, không thay đổi tài khoản `sa`. Login được cấp quyền cho hóa đơn, báo cáo và ba DAO Sản phẩm/Danh mục/Khách hàng; xem mục JDBC bên dưới. Script không ghi đè cấu hình đã có.

Sau khi biên dịch, kiểm tra kết nối chỉ đọc bằng `java -cp "bin;src;lib/*" tools.KiemTraKetNoi`. Chạy ứng dụng trong thư mục project để tìm đúng `config/database.properties`; nếu đặt working directory khác, truyền JVM option `-Dgiathanh.db.config=D:/duong-dan/config/database.properties`.

Nếu bước 1 báo Access denied, kiểm tra PowerShell đã chạy bằng quyền Administrator. Không cần cài lại SQL Server. Nếu dùng instance/cổng khác, cập nhật `db.url` trong file cấu hình.

Thứ tự SQL khi chạy thủ công trong SSMS: `QuanLyBanHang.sql` (chỉ lần đầu) → `02_BoSungHoaDon.sql` → `04_SanPhamKhachHang.sql` → `03_DuLieuMau.sql` (tùy chọn). Script 02 có thể chạy lại và không xóa dữ liệu. Script 03 chỉ dành cho thử giao diện, không tự chạy khi mở ứng dụng.

### Sử dụng

1. Chọn **Hóa đơn → Lập hóa đơn** để chuyển sang màn hình riêng; nhập mã nhân viên trong database, bấm **Chọn / Bỏ chọn ca** và chọn ca đang mở được phân công trong ngày. Nút này chọn ca thực tế, không tự tạo hoặc đóng ca của phân hệ ca làm việc.
2. Khách lẻ không cần nhập tên. Với thành viên, nhập SĐT rồi bấm **Tìm thành viên theo SĐT** để tải tên/hạng từ SQL. **Thêm sản phẩm** cho phép chọn từ sản phẩm đang kinh doanh và còn tồn kho; tên, danh mục, đơn giá lấy từ database. Nhập số lượng và giảm tiền dòng. Các ô tiền nhập bằng số nguyên VNĐ, không có dấu phân cách.
3. Ưu đãi hạng được lấy từ chính sách SQL hiện tại trên tổng tiền hàng. Giảm dòng là số tiền cố định của cả dòng, tối đa 50% tiền hàng dòng; tổng tất cả giảm giá không quá 70% hóa đơn. Khách lẻ không bắt buộc thông tin cá nhân. Thành viên phải có tên và SĐT 10 chữ số.
4. Nhập tiền khách đưa, số chuyển khoản hoặc tiền trả trước. Với trả góp, nhập đối tác, kỳ hạn và xác nhận hồ sơ đã được duyệt, ký. Tích xác nhận thanh toán rồi lưu. Mã tự sinh `HDYYMMDDxxxx`; hóa đơn đã lưu không chỉnh sửa. Ở danh sách, chọn **Xem chi tiết** hoặc bấm đúp dòng để mở màn hình chi tiết riêng, in hoặc xuất PDF.
5. Chọn **Báo cáo**, chọn loại và bộ lọc rồi **Xem báo cáo**. Có 11 loại: ngày, tuần ISO, tháng, năm, nhân viên, ca, phương thức, sản phẩm bán chạy, khách hàng, chương trình khuyến mãi và trả góp. Xuất Excel `.xlsx` hoặc PDF theo dữ liệu đang hiển thị.

Các màn hình là JPanel riêng trong CardLayout của trang chủ: `PNL_HoaDon` (danh sách), `PNL_LapHoaDon` (lập và thanh toán), `PNL_ChiTietHoaDon` (xem, in, xuất PDF), `PNL_BaoCao` (thống kê). Nút Thanh toán trên màn hình Bán hàng dẫn đến panel Lập hóa đơn. Màn hình Bán hàng tìm sản phẩm/khách từ JDBC; giỏ hàng hiện chưa tự chuyển sang màn lập hóa đơn, cần chọn lại khi thanh toán. Nút Quay lại danh sách và breadcrumb cho phép trở về danh sách; quay lại từ biểu mẫu giữ nội dung đang nhập, nút Xóa nội dung đặt lại biểu mẫu.

### Dữ liệu và giới hạn tích hợp

- `HoaDonRepository` mặc định dùng JDBC/SQL Server. Không tự chuyển về XML nếu mất kết nối. Các bảng `HoaDon`, `ChiTietHoaDon`, `GiaoDichTraGop`, `SanPham`, `KhachHang`, `CaLamViec` được cập nhật trong **một transaction**; lỗi ở bất kỳ bước nào sẽ rollback. Cấp mã được bảo vệ bằng khóa transaction; trừ tồn kho có điều kiện để chặn bán vượt tồn. Kiểm tra lại nhân viên hoạt động, lịch phân công/ca mở, dữ liệu sản phẩm/giá/tồn kho và thông tin thành viên trước khi ghi.
- Script 02 bổ sung cột lưu tiền khách đưa, tên/hạng/SĐT khách hàng, tên chương trình, tên sản phẩm và danh mục tại thời điểm bán. Hóa đơn cũ chưa có bản chụp dùng thông tin danh mục hiện tại; hóa đơn cũ không có tiền khách đưa dùng số thanh toán tối thiểu, không khôi phục được tiền thừa đã đưa trước đây. Thành tiền dòng lưu sau giảm dòng; tiền giảm giá trên header là tổng tất cả khoản giảm. Nếu tổng header và chi tiết không khớp, báo lỗi thay vì tự sửa báo cáo.
- Truy vấn JDBC chạy ngoài luồng giao diện; khóa thao tác trong khi ghi để tránh bấm lưu lặp. Các panel vẫn riêng biệt. Cấu hình ưu tiên JVM properties `giathanh.db.url/user/password`, tiếp đến biến môi trường `GIATHANH_DB_URL/USER/PASSWORD`, rồi file `config/database.properties`. File thật được gitignore. `ConnectDB` mở kết nối riêng cho từng transaction, không chia sẻ `autoCommit` giữa các DAO.
- Chưa nối xác thực đăng nhập, đồng bộ bảo hành hoặc gửi hồ sơ đến đối tác tài chính. Chi tiêu tích lũy và doanh thu ca được cộng khi lưu thành công; hạng khách hàng được tính theo bảng chính sách ở script 04. Khuyến mãi nếu nhập phải khớp mã/tên chương trình đang kích hoạt trong thời gian áp dụng; điều kiện theo sản phẩm/đối tượng của các dạng chương trình phức tạp chưa được mô hình hóa đủ trong schema, nên các khoản giảm vẫn do nhân viên nhập và bị giới hạn theo quy định 50%/70%. Trả góp lưu dư nợ gốc và số tiền gốc/tháng làm tròn lên; lịch thu nợ, lãi/phí hợp đồng chưa được mô hình hóa.
- Có thể bật rõ chế độ offline bằng `-Dgiathanh.storage=xml`, đổi đường dẫn bằng `-Dgiathanh.data=D:/duong-dan/hoa-don.xml`. Chế độ này để kiểm thử/demo độc lập; không tự nhập dữ liệu XML cũ vào database. Kiểm thử mặc định sử dụng kho riêng trong `build/feature-tests`.
- `PNL_BaoCao()` đang là chế độ quản lý của ứng dụng demo. Khi tích hợp đăng nhập, truyền `new BaoCaoService.Access(maNhanVien, false)` vào constructor để giới hạn nhân viên bán hàng: doanh số của đúng nhân viên và sản phẩm bán chạy. Kiểm tra quyền được thực hiện trong service và trên lựa chọn giao diện. Không dùng constructor mặc định cho người dùng chưa xác thực trong bản triển khai thực tế.
- Doanh thu = tiền hàng − tổng giảm giá; trả góp ghi nhận toàn bộ giá trị bán, không phải tiền đã thu. Ưu đãi hóa đơn được phân bổ theo tiền còn lại trên từng dòng; phần lẻ được dồn vào dòng cuối để tổng doanh thu không lệch. Lọc danh mục chỉ tính các dòng phù hợp. Một hóa đơn có thể xuất hiện ở nhiều nhóm sản phẩm, nên không cộng số hóa đơn các nhóm để lấy số giao dịch duy nhất.
- Báo cáo khuyến mãi tổng hợp giao dịch theo tên chương trình ghi trên hóa đơn; không đo tác động nhân quả hoặc lợi nhuận. PDF là hóa đơn bán hàng nội bộ, không có ký số hoặc tích hợp hóa đơn thuế. PDF xuất nhiều trang dưới dạng hình để giữ dấu tiếng Việt; Excel là OOXML với giá trị tiền dạng số, chuỗi không được thực thi như công thức.

### Biên dịch và kiểm tra

Chạy `powershell -ExecutionPolicy Bypass -File .\build-and-test.ps1`. Script biên dịch toàn bộ source và chạy kiểm thử độc lập, không cần cài thư viện mới hoặc kết nối cơ sở dữ liệu. Dữ liệu kiểm thử và ảnh kiểm tra giao diện ở `build/feature-tests`, tách khỏi kho thật.

Khi SQL Server đã sẵn sàng và đã chạy script 02, 04, dùng `powershell -ExecutionPolicy Bypass -File .\build-and-test.ps1 -SqlIntegration` để kiểm tra đọc/ghi JDBC, tồn kho, chi tiêu, doanh thu ca, trả góp, lịch sử và rollback. Bộ kiểm tra SQL tạo fixture trong transaction riêng, chặn commit trong repository rồi rollback toàn bộ; không để lại hóa đơn hoặc nhân viên test. Không coi kiểm thử offline là bằng chứng đã kết nối được SQL Server.

Sau khi biên dịch, chạy `java -cp "bin;src;lib/*" ui.UI_TrangChu`.

## Sản phẩm và Khách hàng dùng JDBC

Hai trang dùng **SQL Server**, không còn kho ArrayList tạm hoặc dữ liệu mẫu tự sinh khi mở trang. `ArrayList` chỉ chứa kết quả SELECT để hiển thị, sắp xếp và phân trang.

- `PNL_SanPham` → `SanPhamDAO`, `DanhMucDAO` → bảng `SanPham`, `DanhMuc`: danh sách, thêm/sửa, ngừng/kích hoạt, xóa có điều kiện, ảnh PNG/JPEG lưu trong SQL, tìm không dấu và lọc kết hợp, quản lý danh mục.
- `PNL_KhachHang` → `KhachHangDAO` → bảng `KhachHang`, `HoaDon`, `HangThanhVien`, `LichSuKhachHang`, `ThongBaoKhuyenMai`: đăng ký/sửa/ngừng/kích hoạt/xóa, tìm/lọc/sắp xếp/phân trang, chi tiết, lịch sử mua hàng/hoạt động, xuất CSV, chi tiêu, chính sách hạng và thông báo nội bộ.
- `UI_SanPham`, `UI_KhachHang` là lớp kế thừa ngắn để giữ tên gọi cũ; toàn bộ giao diện nằm ở hai lớp PNL. Entity giữ dữ liệu và getter/setter. DAO dùng `Connection`, `PreparedStatement`, `ResultSet`, vòng lặp; không dùng ORM hoặc framework.
- Mỗi thao tác mở/đóng kết nối riêng bằng `ConnectDB.openConnection()`. Giá trị nhập được gán bằng tham số `?`. Lỗi SQL được báo trên trang; không tự chuyển sang dữ liệu tạm. Đăng ký/cập nhật kèm nhật ký, sửa chính sách và tạo thông báo dùng transaction; lỗi thì rollback.
- SKU theo schema là **2–20 ký tự**. Mã nội bộ `maNoiBo` giữ nguyên khi đổi SKU. Sản phẩm đã có hóa đơn/bảo hành không được đổi SKU hoặc xóa; phải ngừng trước khi xóa sản phẩm chưa có nghiệp vụ. Không xóa danh mục đang có sản phẩm. Sửa thông tin sản phẩm không ghi đè tồn kho.
- Khách chỉ được xóa khi đã ngừng, chi tiêu bằng 0 và không có hóa đơn, bảo hành, thông báo. SĐT/email không trùng, kể cả khách ngừng. Tên, ngày sinh, email, giá, bảo hành và ảnh vẫn được kiểm tra trước khi lưu.
- Lịch sử mua hàng đọc hóa đơn thật. Tổng chi tiêu đọc `tongChiTieuTichLuy`, giữ cả số dư lịch sử trước nâng cấp; không suy đoán lại số dư từ các hóa đơn hiện còn lưu.
- `SqlHoaDonRepository` gọi `KhachHangDAO.ghiNhanMuaHang(Connection,...)` trong transaction thanh toán: ghi nhật ký, cộng chi tiêu một lần. Trigger tính lại hạng theo chính sách. UI hồ sơ không sửa chi tiêu/hạng bằng tay. Hàm ghi nhận không tạo hóa đơn giả và chặn ghi nhận trùng.
- Chính sách mặc định Member/Silver/Gold/Diamond: ngưỡng 0/1.000.000/3.000.000/10.000.000 đ, ưu đãi 0/3/5/8%. Sau đó đọc/ghi trong database; màn lập hóa đơn SQL dùng cùng chính sách. Ưu đãi tối đa 70% để phù hợp giới hạn giảm giá hóa đơn.
- Khuyến mãi khách hàng chỉ tạo **thông báo nội bộ** cho người đang hoạt động và đã đồng ý nhận; không gửi email/SMS.
- Script 04 bổ sung cột thiếu và ba bảng hỗ trợ, không xóa dữ liệu. Khách/sản phẩm cũ chưa có ngày tạo dùng thời điểm nâng cấp làm ngày bắt đầu theo dõi, không phải ngày đăng ký lịch sử chính xác. Nhập nhật ký hóa đơn cũ không cộng lại chi tiêu.
- Sản phẩm mới mặc định tồn kho 0; nhập kho thuộc nghiệp vụ riêng. Không tạo tồn kho giả để bán hàng. Phần này chưa xử lý hoàn tiền.

### Cài đặt và chạy

1. Khởi động SQL Server. Với SQLEXPRESS: mở PowerShell bằng Administrator, chạy `Start-Service -Name 'MSSQL$SQLEXPRESS'`.
2. Database đang có: chạy `sql/02_BoSungHoaDon.sql`, sau đó `sql/04_SanPhamKhachHang.sql` trong SSMS. Không chạy lại script tạo bảng. Database mới: chạy `setup-database.ps1`; thêm `-WithDemoData` nếu cần dữ liệu thử. Script gốc hiện chỉ tạo cấu trúc, không còn khối xóa/nạp lại dữ liệu cũ.
3. Sao chép `config/database.example.properties` thành `config/database.properties`, điền URL, tài khoản SQL và mật khẩu thực tế. File thật được gitignore; ConnectDB không có mật khẩu mặc định trong source.
4. Login cần SELECT/INSERT/UPDATE/DELETE trên `SanPham`, `DanhMuc`, `KhachHang`; SELECT/UPDATE trên `HangThanhVien`; SELECT/INSERT/DELETE trên `LichSuKhachHang`; SELECT/INSERT trên `ThongBaoKhuyenMai`, cùng quyền đọc hóa đơn/bảo hành và quyền hóa đơn đã có. `configure-database.ps1` bổ sung quyền cho login tạo mới. Nếu đã có file cấu hình, quản trị viên cấp thêm quyền cho login hiện tại; script không ghi đè cấu hình.
5. Dùng JDK 21+. Trong Eclipse: Refresh → Project/Clean → chạy `ui.UI_TrangChu`. Hoặc chạy `build-and-test.ps1`, rồi `java -cp "bin;src;lib/*" ui.UI_TrangChu`.

### Kiểm thử JDBC riêng

`build-and-test.ps1` biên dịch và chạy kiểm thử offline. Kết quả offline không chứng minh đã nối SQL Server.

Khi SQL Server đã chạy, Windows login có quyền tạo database/login, SQL Authentication đã bật:

`powershell -ExecutionPolicy Bypass -File .\scripts\test-product-customer-jdbc.ps1 -Server '.\SQLEXPRESS' -JavaHome 'DUONG_DAN_JDK_21'`

Script tạo DB riêng `GiaThanhJdbcTest_<GUID>`, chạy schema và migration hai lần, tạo login chỉ cho DB test; kiểm tra CRUD, tìm/lọc, lưu ảnh, giới hạn xóa/đổi SKU, lịch sử, chi tiêu/hạng, chính sách, thông báo và rollback. Cuối cùng xóa đúng DB/login test đã tạo. Không dùng dữ liệu ứng dụng hoặc ghi đè cấu hình thật. Lớp test từ chối chạy nếu database không có tiền tố kiểm thử.
