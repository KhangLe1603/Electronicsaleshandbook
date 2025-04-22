# Electronic Sales Handbook

**Electronic Sales Handbook** là ứng dụng Android quản lý thông tin sản phẩm, khách hàng và liên kết khách hàng-sản phẩm thông qua **Google Sheets**. Ứng dụng tích hợp **Firebase Authentication** để hỗ trợ đăng nhập bằng email và **Google Sign-In**.

---

## Tổng quan

Ứng dụng cung cấp các chức năng chính:

- **Quản lý sản phẩm**: Thêm, sửa, xóa, tìm kiếm sản phẩm (lưu trên sheet *Sheet1*).
- **Quản lý khách hàng**: Thêm, sửa, xóa, tìm kiếm khách hàng (lưu trên sheet *KhachHang*).
- **Quản lý liên kết khách hàng-sản phẩm**: Tạo và xem liên kết (lưu trên sheet *CustomerProductLink*).
- **Xác thực người dùng**: Đăng nhập/đăng ký bằng email và **Google Sign-In** qua **Firebase Authentication**.

Dự án sử dụng kiến trúc **MVVM** với các thành phần:

- **Repository**: `SheetRepository`, `CustomerRepository`, `CustomerProductLinkRepository`, `AuthRepository`.
- **ViewModel**: `ProductViewModel`, `CustomerViewModel`, `CustomerProductLinkViewModel`, `LinkViewModel`, `AuthViewModel`.

---

## Cấu hình Google Sheets

Ứng dụng sử dụng **Google Sheets API** để lưu trữ và quản lý dữ liệu.

### Thông tin Google Sheet

- **ID Bảng tính (Spreadsheet ID)**:  
  `1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A`

  - Mã định danh duy nhất của Google Sheet.
  - Khai báo trong các class:  
    - `SheetRepository`  
    - `CustomerRepository`  
    - `CustomerProductLinkRepository`  
    (hằng số `SPREADSHEET_ID`).
  - Để thay đổi, chỉnh sửa `SPREADSHEET_ID` trong các class trên.

- **Tên các Sheet và cấu trúc cột**:

  #### Sheet Sản phẩm: `Sheet1`
  | Cột | Tên            | Mô tả                          |
  |-----|----------------|--------------------------------|
  | A   | STT            | Số thứ tự (không sử dụng)      |
  | B   | MÃ SP          | Mã sản phẩm (VD: SP001)        |
  | C   | Tên SPDV       | Tên sản phẩm/dịch vụ           |
  | D   | Mô tả          | Mô tả sản phẩm                 |
  | E   | Đơn giá        | Giá gốc                        |
  | F   | Giá bán        | Giá bán ra                     |
  | G   | Đơn vị tính    | Đơn vị (VD: cái, bộ)           |

  **Sử dụng**:  
  - `SheetRepository` (`fetchProductsWithBackoff`).  
  - `ProductViewModel` (`addProduct`, `updateProduct`, `deleteProduct`).

  #### Sheet Khách hàng: `KhachHang`
  | Cột | Tên            | Mô tả                          |
  |-----|----------------|--------------------------------|
  | A   | STT            | Số thứ tự (không sử dụng)      |
  | B   | MÃ KH          | Mã khách hàng (VD: KH001)      |
  | C   | Họ và đệm      | Họ và đệm khách hàng           |
  | D   | Tên            | Tên khách hàng                 |
  | E   | Địa chỉ        | Địa chỉ khách hàng             |
  | F   | Số điện thoại  | Số điện thoại                  |
  | G   | Email          | Email khách hàng               |
  | H   | Ngày sinh      | Ngày sinh                      |
  | I   | Giới tính      | Giới tính (Nam/Nữ)             |

  **Sử dụng**:  
  - `CustomerRepository` (`fetchCustomersWithBackoff`).  
  - `CustomerViewModel` (`addCustomer`, `updateCustomer`, `deleteCustomer`).

  #### Sheet Liên kết Khách hàng-Sản phẩm: `CustomerProductLink`
  | Cột | Tên            | Mô tả                          |
  |-----|----------------|--------------------------------|
  | A   | STT            | Số thứ tự (không sử dụng)      |
  | B   | MÃ KH          | Mã khách hàng                  |
  | C   | MÃ SP          | Mã sản phẩm                    |
  | D   | Tên đầy đủ     | Tên khách hàng                 |
  | E   | Tên sản phẩm   | Tên sản phẩm                   |

  **Sử dụng**:  
  - `SheetRepository` (`fetchLinksWithBackoff`, `createLink`).  
  - `CustomerProductLinkRepository` (`fetchLinksWithBackoff`).

### Cập nhật Cấu hình Google Sheet

Để thay đổi ID bảng tính hoặc tên sheet:

1. Mở các class trong package `com.example.electronicsaleshandbook.repository`:  
   - `SheetRepository`  
   - `CustomerRepository`  
   - `CustomerProductLinkRepository`
2. Cập nhật hằng số `SPREADSHEET_ID`.
3. Cập nhật tên sheet trong các phương thức:  
   - `Sheet1` trong `fetchProductsWithBackoff` (`SheetRepository`).  
   - `KhachHang` trong `fetchCustomersWithBackoff` (`CustomerRepository`).  
   - `CustomerProductLink` trong `fetchLinksWithBackoff` (`SheetRepository`, `CustomerProductLinkRepository`) và `createLink` (`SheetRepository`).
4. Đảm bảo Google Sheet mới có cấu trúc cột như trên.

### Lưu ý

- File *service_account.json* (thư mục *assets*) dùng để xác thực **Google Sheets API**. Đảm bảo file tồn tại và đúng định dạng.
- Nếu tạo Google Sheet mới, chia sẻ quyền **Editor** với email tài khoản dịch vụ (lấy từ *service_account.json*).
- Các repository sử dụng cache và thử lại theo cấp số nhân (exponential backoff). Điều chỉnh `MIN_REFRESH_INTERVAL` (mặc định 2000ms) nếu cần.

---

## Cấu hình Firebase Authentication

Ứng dụng sử dụng **Firebase Authentication** để hỗ trợ đăng nhập/đăng ký bằng email và **Google Sign-In**.

### Web Client ID

- **Web Client ID**:  
  `492706936531-qppvdsgr5p6jhhe2439cit6kmb8r1deg.apps.googleusercontent.com`

  - Khai báo trong `AuthRepository` để cấu hình **Google Sign-In**.
  - Là ID ứng dụng web trong **Firebase Console** cho xác thực Google.

- **Cách lấy Web Client ID**:

  1. Truy cập [Firebase Console](https://console.firebase.google.com/).
  2. Chọn dự án.
  3. Vào **Authentication** > **Sign-in method** > **Google**.
  4. Trong **Web SDK configuration**, sao chép **Web client ID**.
  5. Cập nhật trong `AuthRepository` (phương thức khởi tạo `GoogleSignInOptions`).

### Thiết lập Firebase

Để tích hợp **Firebase**:

1. **Tạo dự án Firebase**:

   - Truy cập [Firebase Console](https://console.firebase.google.com/).
   - Nhấn **Add project**, đặt tên và hoàn thành các bước.
   - Tải file *google-services.json* và đặt vào thư mục *app*.

2. **Thêm Firebase SDK**:

   - File *build.gradle* (project-level):

     ```gradle
     buildscript {
         dependencies {
             classpath 'com.google.gms:google-services:4.4.2'
         }
     }
     ```

   - File *build.gradle* (app-level):

     ```gradle
     apply plugin: 'com.google.gms.google-services'

     dependencies {
         implementation 'com.google.firebase:firebase-auth:23.0.0'
         implementation 'com.google.android.gms:play-services-auth:21.2.0'
     }
     ```

3. **Kích hoạt Google Sign-In**:

   - Trong **Firebase Console**, vào **Authentication** > **Sign-in method**.
   - Kích hoạt **Email/Password** và **Google**.

4. **Cấu hình SHA-1** (xem phần SHA-1).

---

## Tạo và Liên kết Dự án Android

### Dự án Android

1. Minimum SDK: **API 21** (hoặc cao hơn).

### Tích hợp Google Sheets API

1. **Tạo tài khoản dịch vụ**:

   - Truy cập [Google Cloud Console](https://console.cloud.google.com/).
   - Tạo hoặc chọn dự án.
   - Vào **APIs & Services** > **Enable APIs and Services**, kích hoạt **Google Sheets API**.
   - Vào **Credentials** > **Create Credentials** > **Service Account**.
   - Tạo tài khoản dịch vụ, chọn vai trò **Editor**, tải file *service_account.json*.
   - Đặt file vào thư mục *app/src/main/assets*.

2. **Thêm thư viện Google API Client**:

   - File *build.gradle* (app-level):

     ```gradle
     implementation 'com.google.api-client:google-api-client:2.2.0'
     implementation 'com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0'
     implementation 'com.google.http-client:google-http-client-jackson2:1.43.3'
     ```

3. **Chia sẻ Google Sheet**:

   - Mở Google Sheet với ID `1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A`.
   - Nhấn **Share**, nhập email tài khoản dịch vụ (từ *service_account.json*), cấp quyền **Editor**.

### Tích hợp Firebase Authentication

- Thực hiện các bước trong **Thiết lập Firebase**.
- Đảm bảo *google-services.json* nằm trong thư mục *app*.
- Cập nhật **Web Client ID** trong `AuthRepository`.

### Đồng bộ dự án

- Nhấn **Sync Project with Gradle Files** trong **Android Studio**.

---

## SHA-1

**SHA-1** là mã băm xác thực ứng dụng Android với **Firebase**, cần cho **Google Sign-In**.

### Vai trò của SHA-1

- Đảm bảo ứng dụng được xác minh an toàn khi dùng **Firebase Authentication**.
- Cần thêm SHA-1 vào **Firebase Console** để **Google Sign-In** hoạt động.

### Cách lấy SHA-1

1. **Từ Android Studio**:

   - Mở **Gradle** panel (bên phải).
   - Chọn `:app` > **Tasks** > **android** > **signingReport**.
   - Chạy `signingReport`, SHA-1 xuất hiện trong **Run** console (variant: debug).

2. **Từ lệnh `keytool`**:

   - Mở terminal/command prompt.
   - Chạy:

     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```

   - Sao chép SHA-1.

3. **Thêm SHA-1 vào Firebase**:

   - Trong **Firebase Console**, vào **Project Settings** > **Your apps**.
   - Chọn ứng dụng Android hoặc thêm mới (package name: `com.example.electronicsaleshandbook`).
   - Dán SHA-1 vào **SHA-1 certificate fingerprint**.
   - Tải lại *google-services.json* nếu có thay đổi, đặt vào thư mục *app*.

---

## Gỡ lỗi và Lưu ý

### Gỡ lỗi Google Sheets

- Kiểm tra log:
  - `SheetRepository` (tag: `SheetRepository`).
  - `CustomerRepository` (tag: `CustomerRepository`).
  - `CustomerProductLinkRepository` (tag: `CustomerProductLinkRepository`).
- Đảm bảo *service_account.json* tồn tại trong *assets*.
- Kiểm tra quyền Google Sheet (email tài khoản dịch vụ cần quyền **Editor**).
- Lỗi 429 (Quota exceeded): Điều chỉnh `MIN_REFRESH_INTERVAL` hoặc số lần thử lại trong `fetch*WithBackoff`.

### Gỡ lỗi Firebase Authentication

- Kiểm tra log trong `AuthRepository` (tag: `AuthRepository`).
- Đảm bảo **Web Client ID** khớp với **Firebase Console**.
- Kiểm tra SHA-1 trong **Firebase Console**.
- Đảm bảo *google-services.json* đúng vị trí.

### Lưu ý khác

- Các **ViewModel** dùng **LiveData** để cập nhật giao diện theo thời gian thực.
- Ứng dụng dùng cache để giảm yêu cầu API. Gọi `invalidateCache` khi cần làm mới.
- Đảm bảo kết nối internet khi chạy ứng dụng.

---

## Cấu trúc Project

### Repository

- `AuthRepository`: Quản lý đăng nhập/đăng ký (email, **Google Sign-In**).
- `SheetRepository`: Quản lý sản phẩm và liên kết khách hàng-sản phẩm.
- `CustomerRepository`: Quản lý khách hàng.
- `CustomerProductLinkRepository`: Quản lý liên kết khách hàng-sản phẩm.

### ViewModel

- `AuthViewModel`: Xử lý xác thực.
- `ProductViewModel`: Tìm kiếm, sắp xếp, thêm/sửa/xóa sản phẩm.
- `CustomerViewModel`: Tìm kiếm, sắp xếp, thêm/sửa/xóa khách hàng.
- `CustomerProductLinkViewModel`: Làm mới liên kết khách hàng-sản phẩm.
- `LinkViewModel`: Quản lý liên kết và lấy danh sách khách hàng/sản phẩm.

---

## Liên hệ

Nếu có câu hỏi hoặc cần hỗ trợ, vui lòng mở **issue** trên GitHub hoặc liên hệ qua email: [leminhkhang48@gmail.com] hoặc số điện thoại Zalo: 0794356155.
