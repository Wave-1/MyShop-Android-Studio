# 🛍️ MyShop - Ứng dụng Mua sắm Trực tuyến (Android + Firebase)

MyShop là một ứng dụng thương mại điện tử (E-Commerce App) được phát triển trên nền tảng **Android (Java)**.  
Ứng dụng hỗ trợ **đăng nhập, quản lý sản phẩm, giỏ hàng, đặt hàng và quản lý địa chỉ giao hàng**, sử dụng **Firebase Firestore** để lưu trữ dữ liệu thời gian thực.

---

## 🚀 Tính năng chính

### 👤 Quản lý người dùng
- Đăng ký, đăng nhập, đăng xuất bằng Firebase Authentication.
- Lưu thông tin người dùng trong `Firestore` (email, tên, vai trò, số điện thoại...).
- Hỗ trợ phân quyền người dùng (`user` / `admin`).

### 📦 Quản lý sản phẩm
- Hiển thị danh sách sản phẩm theo danh mục.
- Xem chi tiết sản phẩm (ảnh, giá, mô tả, danh mục...).
- Hiển thị **sản phẩm liên quan** cùng danh mục.
- Tìm kiếm sản phẩm theo tên hoặc danh mục.

### 🛒 Giỏ hàng & Đặt hàng
- Thêm/xóa sản phẩm khỏi giỏ hàng.
- Cập nhật số lượng sản phẩm.
- Tính tổng giá trị đơn hàng.
- Thanh toán và lưu đơn hàng vào `Firestore`.

### 🏠 Quản lý địa chỉ giao hàng
- Lưu nhiều địa chỉ cho mỗi người dùng.
- Đặt **địa chỉ mặc định** (default = true).
- Chọn địa chỉ để thanh toán.
- Chỉnh sửa địa chỉ (đang phát triển).

### ⚙️ Admin (tuỳ chọn)
- Quản lý danh mục, sản phẩm, đơn hàng.
- Xem thống kê đơn hàng và người dùng.

---

## 🧱 Kiến trúc & Công nghệ

| Thành phần | Công nghệ sử dụng |
|-------------|------------------|
| **Ngôn ngữ** | Java |
| **Giao diện người dùng** | XML, Material Design 3 |
| **Cơ sở dữ liệu** | Firebase Firestore |
| **Xác thực người dùng** | Firebase Authentication |
| **Thư viện UI** | RecyclerView, CardView, Glide, Material Components |
| **Xử lý hình ảnh** | Glide |
| **Môi trường phát triển** | Android Studio |

---

## 📁 Cấu trúc thư mục

