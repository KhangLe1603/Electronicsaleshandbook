## Cấu hình Google Sheets

Dự án này sử dụng Google Sheets để quản lý thông tin sản phẩm và liên kết giữa khách hàng và sản phẩm. Dưới đây là chi tiết về cách cấu hình tích hợp với Google Sheets.

### Thông tin Google Sheet
- **ID Bảng tính (Spreadsheet ID)**: `1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A`
  - Đây là mã định danh duy nhất của Google Sheet được sử dụng trong ứng dụng.
  - Được khai báo trong class `SheetRepository` dưới dạng hằng số `SPREADSHEET_ID`.
  - Để thay đổi ID Bảng tính, chỉnh sửa hằng số `SPREADSHEET_ID` trong class `SheetRepository`.

- **Tên các Sheet**:
  - **Sheet Sản phẩm**: `Sheet1`
    - Chứa dữ liệu sản phẩm (các cột từ A đến G: STT, MÃ SP, Tên SPDV, Mô tả, Đơn giá, Giá bán, Đơn vị tính).
    - Được sử dụng trong các phương thức `fetchProductsWithBackoff`, `addProduct`, `updateProduct`, và `deleteProduct`.
  - **Sheet Liên kết Khách hàng-Sản phẩm**: `CustomerProductLink`
    - Chứa dữ liệu liên kết giữa khách hàng và sản phẩm (các cột từ B đến E: MÃ KH, MÃ SP, Tên đầy đủ, Tên sản phẩm).
    - Được sử dụng trong các phương thức `fetchLinksWithBackoff` và `createLink`.

### Cập nhật Cấu hình Sheet
Để thay đổi ID Bảng tính hoặc tên sheet:
1. Mở class `SheetRepository` (`com.example.electronicsaleshandbook.repository.SheetRepository`).
2. Cập nhật hằng số `SPREADSHEET_ID` với ID Bảng tính mới.
3. Cập nhật tên sheet trong các phương thức liên quan (ví dụ: `Sheet1` trong `fetchProductsWithBackoff` hoặc `CustomerProductLink` trong `fetchLinksWithBackoff`).
4. Đảm bảo Google Sheet mới có cấu trúc cột giống như mô tả ở trên để tránh lỗi khi phân tích dữ liệu.

### Lưu ý
- Ứng dụng sử dụng tài khoản dịch vụ (`service_account.json`) được lưu trong thư mục `assets` để xác thực với Google Sheets API. Đảm bảo file này được cấu hình đúng và có thể truy cập.
- Nếu tạo Google Sheet mới, hãy chia sẻ quyền truy cập với email của tài khoản dịch vụ.
- Class `SheetRepository` triển khai cơ chế lưu trữ cache và thử lại theo cấp số nhân (exponential backoff) để xử lý giới hạn API. Có thể điều chỉnh `MIN_REFRESH_INTERVAL` hoặc logic thử lại nếu cần.

Nếu gặp vấn đề với tích hợp Google Sheets, hãy kiểm tra log trong `SheetRepository` (được gắn thẻ `SheetRepository`) để tìm thông tin gỡ lỗi.
