Electronic Sales Handbook
Ứng dụng Electronic Sales Handbook là một ứng dụng Android giúp quản lý thông tin sản phẩm, khách hàng và liên kết giữa khách hàng và sản phẩm thông qua Google Sheets. Ứng dụng sử dụng Firebase Authentication để hỗ trợ đăng nhập bằng email và Google Sign-In.
Tổng quan
Ứng dụng bao gồm các chức năng chính:

Quản lý sản phẩm: Thêm, sửa, xóa và tìm kiếm sản phẩm (lưu trữ trên Google Sheet Sheet1).
Quản lý khách hàng: Thêm, sửa, xóa và tìm kiếm khách hàng (lưu trữ trên Google Sheet KhachHang).
Quản lý liên kết khách hàng-sản phẩm: Tạo và xem liên kết giữa khách hàng và sản phẩm (lưu trữ trên Google Sheet CustomerProductLink).
Xác thực người dùng: Hỗ trợ đăng nhập/đăng ký bằng email và Google Sign-In thông qua Firebase Authentication.

Dự án sử dụng kiến trúc MVVM, với các repository (SheetRepository, CustomerRepository, CustomerProductLinkRepository, AuthRepository) và ViewModel (ProductViewModel, CustomerViewModel, CustomerProductLinkViewModel, LinkViewModel, AuthViewModel) để quản lý dữ liệu và logic.
Cấu hình Google Sheets
Ứng dụng tích hợp với Google Sheets API để lưu trữ và quản lý dữ liệu. Dưới đây là thông tin cấu hình:
Thông tin Google Sheet

ID Bảng tính (Spreadsheet ID): 1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A

Đây là mã định danh duy nhất của Google Sheet được sử dụng trong ứng dụng.
Được khai báo trong các class SheetRepository, CustomerRepository, CustomerProductLinkRepository dưới dạng hằng số SPREADSHEET_ID.
Để thay đổi ID Bảng tính, chỉnh sửa hằng số SPREADSHEET_ID trong các class trên.


Tên các Sheet và cấu trúc cột:

Sheet Sản phẩm: Sheet1
Cấu trúc cột:
A: STT (Số thứ tự, không sử dụng trong ứng dụng).
B: MÃ SP (Mã sản phẩm, ví dụ: SP001).
C: Tên SPDV (Tên sản phẩm/dịch vụ).
D: Mô tả.
E: Đơn giá.
F: Giá bán.
G: Đơn vị tính.


Sử dụng: Trong SheetRepository (fetchProductsWithBackoff) và ProductViewModel (addProduct, updateProduct, deleteProduct).


Sheet Khách hàng: KhachHang
Cấu trúc cột:
A: STT (Số thứ tự, không sử dụng trong ứng dụng).
B: MÃ KH (Mã khách hàng, ví dụ: KH001).
C: Họ và đệm.
D: Tên.
E: Địa chỉ.
F: Số điện thoại.
G: Email.
H: Ngày sinh.
I: Giới tính.


Sử dụng: Trong CustomerRepository (fetchCustomersWithBackoff) và CustomerViewModel (addCustomer, updateCustomer, deleteCustomer).


Sheet Liên kết Khách hàng-Sản phẩm: CustomerProductLink
Cấu trúc cột:
A: STT (Số thứ tự, không sử dụng trong ứng dụng).
B: MÃ KH (Mã khách hàng).
C: MÃ SP (Mã sản phẩm).
D: Tên đầy đủ (Tên khách hàng).
E: Tên sản phẩm.


Sử dụng: Trong SheetRepository (fetchLinksWithBackoff, createLink) và CustomerProductLinkRepository (fetchLinksWithBackoff).





Cập nhật Cấu hình Google Sheet
Để thay đổi ID Bảng tính hoặc tên sheet:

Mở các class SheetRepository, CustomerRepository, và CustomerProductLinkRepository trong package com.example.electronicsaleshandbook.repository.
Cập nhật hằng số SPREADSHEET_ID với ID Bảng tính mới.
Cập nhật tên sheet trong các phương thức liên quan:
Sheet1 trong fetchProductsWithBackoff (SheetRepository).
KhachHang trong fetchCustomersWithBackoff (CustomerRepository).
CustomerProductLink trong fetchLinksWithBackoff (SheetRepository, CustomerProductLinkRepository) và createLink (SheetRepository).


Đảm bảo Google Sheet mới có cấu trúc cột giống như mô tả ở trên để tránh lỗi khi phân tích dữ liệu.

Lưu ý

Ứng dụng sử dụng tài khoản dịch vụ (service_account.json) trong thư mục assets để xác thực với Google Sheets API. Đảm bảo file này tồn tại và được cấu hình đúng.
Nếu tạo Google Sheet mới, chia sẻ quyền chỉnh sửa với email tài khoản dịch vụ (lấy từ file service_account.json).
Các repository sử dụng cơ chế cache và thử lại theo cấp số nhân (exponential backoff) để xử lý giới hạn API. Có thể điều chỉnh MIN_REFRESH_INTERVAL (mặc định 2000ms) nếu cần.

Cấu hình Firebase Authentication
Ứng dụng sử dụng Firebase Authentication để hỗ trợ đăng nhập/đăng ký bằng email và Google Sign-In.
Web Client ID

Web Client ID: 492706936531-qppvdsgr5p6jhhe2439cit6kmb8r1deg.apps.googleusercontent.com

Được khai báo trong AuthRepository để cấu hình Google Sign-In.
Đây là ID của ứng dụng web được tạo trong Firebase Console để hỗ trợ xác thực Google.


Cách lấy Web Client ID:

Truy cập Firebase Console.
Chọn dự án của bạn.
Vào Authentication > Sign-in method > Google.
Trong phần Web SDK configuration, sao chép Web client ID.
Cập nhật Web Client ID trong AuthRepository (phương thức khởi tạo GoogleSignInOptions).



Thiết lập Firebase
Để tích hợp Firebase vào dự án Android:

Tạo dự án Firebase:

Truy cập Firebase Console.
Nhấn Add project, đặt tên dự án và làm theo các bước hướng dẫn.
Tải file cấu hình google-services.json và đặt vào thư mục app của dự án.


Thêm Firebase SDK:

Trong file build.gradle (project-level), thêm:buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.2'
    }
}


Trong file build.gradle (app-level), thêm:apply plugin: 'com.google.gms.google-services'

dependencies {
    implementation 'com.google.firebase:firebase-auth:23.0.0'
    implementation 'com.google.android.gms:play-services-auth:21.2.0'
}




Kích hoạt Google Sign-In:

Trong Firebase Console, vào Authentication > Sign-in method.
Kích hoạt Email/Password và Google làm phương thức đăng nhập.


Cấu hình SHA-1 (xem phần SHA-1 bên dưới).


Tạo và Liên kết Dự án Android
Tạo Dự án Android

Mở Android Studio, chọn File > New > New Project.
Chọn template (ví dụ: Empty Activity) và đặt tên ứng dụng là ElectronicSalesHandbook.
Đặt package name là com.example.electronicsaleshandbook.
Chọn API 21 (hoặc cao hơn) làm Minimum SDK.

Tích hợp Google Sheets API

Tạo tài khoản dịch vụ:

Truy cập Google Cloud Console.
Tạo dự án mới hoặc chọn dự án hiện có.
Vào APIs & Services > Enable APIs and Services, tìm và kích hoạt Google Sheets API.
Vào Credentials > Create Credentials > Service Account.
Tạo tài khoản dịch vụ, chọn vai trò Editor và tải file JSON (đặt tên là service_account.json).
Đặt file service_account.json vào thư mục app/src/main/assets.


Thêm thư viện Google API Client:

Trong file build.gradle (app-level), thêm:implementation 'com.google.api-client:google-api-client:2.2.0'
implementation 'com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0'
implementation 'com.google.http-client:google-http-client-jackson2:1.43.3'




Chia sẻ Google Sheet:

Mở Google Sheet với ID 1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A.
Nhấn Share, nhập email tài khoản dịch vụ (lấy từ service_account.json) và cấp quyền Editor.



Tích hợp Firebase Authentication

Thực hiện các bước trong phần Thiết lập Firebase ở trên.
Đảm bảo google-services.json được đặt đúng trong thư mục app.
Cập nhật Web Client ID trong AuthRepository như mô tả ở phần Web Client ID.

Đồng bộ dự án

Nhấn Sync Project with Gradle Files trong Android Studio để tải các thư viện cần thiết.

SHA-1
SHA-1 là mã băm dùng để xác thực ứng dụng Android với Firebase, cần thiết cho Google Sign-In.
Vai trò của SHA-1

SHA-1 đảm bảo ứng dụng Android được xác minh an toàn khi sử dụng Firebase Authentication.
Cần thêm SHA-1 vào Firebase Console để Google Sign-In hoạt động.

Cách lấy SHA-1

Từ Android Studio:

Mở Gradle panel (bên phải Android Studio).
Chọn :app > Tasks > android > signingReport.
Chạy signingReport, SHA-1 sẽ xuất hiện trong Run console (variant: debug).


Từ lệnh keytool:

Mở terminal/command prompt.
Chạy lệnh:keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android


Sao chép SHA-1 từ đầu ra.


Thêm SHA-1 vào Firebase:

Trong Firebase Console, vào Project Settings > Your apps.
Chọn ứng dụng Android hoặc thêm mới (nhập package name com.example.electronicsaleshandbook).
Dán SHA-1 vào trường SHA-1 certificate fingerprint.
Tải lại file google-services.json nếu có thay đổi và đặt vào thư mục app.



Gỡ lỗi và Lưu ý
Gỡ lỗi Google Sheets

Kiểm tra log trong các repository (SheetRepository, CustomerRepository, CustomerProductLinkRepository) với tag tương ứng (ví dụ: SheetRepository).
Đảm bảo file service_account.json tồn tại trong thư mục assets.
Kiểm tra quyền truy cập Google Sheet (email tài khoản dịch vụ phải có quyền Editor).
Nếu gặp lỗi 429 (Quota exceeded), điều chỉnh MIN_REFRESH_INTERVAL hoặc số lần thử lại trong fetch*WithBackoff.

Gỡ lỗi Firebase Authentication

Kiểm tra log trong AuthRepository (tag: AuthRepository).
Đảm bảo Web Client ID trong AuthRepository khớp với Firebase Console.
Kiểm tra SHA-1 trong Firebase Console có đúng với ứng dụng không.
Đảm bảo google-services.json được đặt đúng trong thư mục app.

Lưu ý khác

Các ViewModel (ProductViewModel, CustomerViewModel, LinkViewModel, CustomerProductLinkViewModel, AuthViewModel) sử dụng LiveData để cập nhật giao diện theo thời gian thực.
Ứng dụng sử dụng cơ chế cache để giảm số lượng yêu cầu API. Gọi invalidateCache trong repository khi cần làm mới dữ liệu.
Đảm bảo kết nối internet khi ứng dụng chạy, vì tất cả dữ liệu được lấy từ Google Sheets.

Cấu trúc Project

Repository:

AuthRepository: Quản lý đăng nhập/đăng ký bằng email và Google Sign-In.
SheetRepository: Quản lý sản phẩm và liên kết khách hàng-sản phẩm.
CustomerRepository: Quản lý khách hàng.
CustomerProductLinkRepository: Quản lý liên kết khách hàng-sản phẩm (bổ sung cho SheetRepository).


ViewModel:

AuthViewModel: Xử lý logic xác thực.
ProductViewModel: Xử lý tìm kiếm, sắp xếp, thêm/sửa/xóa sản phẩm.
CustomerViewModel: Xử lý tìm kiếm, sắp xếp, thêm/sửa/xóa khách hàng.
CustomerProductLinkViewModel: Xử lý làm mới liên kết khách hàng-sản phẩm.
LinkViewModel: Quản lý liên kết khách hàng-sản phẩm và lấy danh sách khách hàng/sản phẩm.



Liên hệ
Nếu bạn có câu hỏi hoặc cần hỗ trợ, vui lòng mở issue trên GitHub hoặc liên hệ qua email: [your-email@example.com].
