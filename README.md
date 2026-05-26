# 📱 Dmanhz Social App (SoulMate) - Mobile Social Network

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android" alt="Platform Android">
  <img src="https://img.shields.io/badge/Language-Java%20%2F%20Kotlin-orange?style=for-the-badge&logo=kotlin" alt="Languages">
  <img src="https://img.shields.io/badge/Backend-Firebase-ffca28?style=for-the-badge&logo=firebase" alt="Backend Firebase">
</p>

**Dmanhz Social App (SoulMate)** là một ứng dụng mạng xã hội di động hiện đại, kết hợp độc đáo giữa việc kết nối cộng đồng, không gian nghe nhạc thư giãn và góc viết nhật ký chia sẻ cảm xúc cá nhân. Dự án được tích hợp các dịch vụ đám mây tiên tiến, hệ thống quản lý dữ liệu thời gian thực và giải pháp thông báo đẩy thông minh nhằm tối ưu hóa trải nghiệm tương tác của người dùng.

---

### 🚀 Tính Năng Nổi Bật

- 🔐 **Xác Thực An Toàn:** Đăng ký, đăng nhập và bảo mật thông tin tài khoản thông qua **Firebase Authentication**.
- 💬 **Tương Tác Thời Gian Thực:** Cập nhật bảng tin (feed), thả cảm xúc, bình luận và trò chuyện tức thời nhờ sức mạnh của **Firebase Realtime Database / Firestore**.
- 🎵 **Social Music & Diary:** Không gian cá nhân cho phép người dùng vừa nghe nhạc giải trí, vừa viết nhật ký lưu trữ những mảnh ghép cảm xúc hàng ngày.
- 🔔 **Thông Báo Đẩy (Push Notifications):** Tích hợp dịch vụ **OneSignal** giúp gửi thông báo tức thì, duy trì tương tác liên tục giữa người dùng.
- ☁️ **Tối Ưu Hóa Media:** Sử dụng **Cloudinary** để lưu trữ, truyền tải hình ảnh và file âm thanh chất lượng cao với tốc độ tối ưu.

---

### 🛠️ Công Nghệ Sử Dụng (Tech Stack)

- **Frontend:** Java / Kotlin (Android SDK)
- **IDE:** Android Studio
- **Backend & Cloud Services:** - **Firebase:** Authentication, Realtime Database, Cloud Functions.
  - **OneSignal:** Giải pháp gửi thông báo đẩy (Push Notifications).
  - **Cloudinary:** Quản lý và lưu trữ dữ liệu truyền thông (Cloud Storage).
- **Kiến trúc:** MVVM (Model-View-ViewModel) đảm bảo mã nguồn sạch, dễ bảo trì và mở rộng.

---

### 📦 Cài Đặt Dự Án (Installation & Setup)

Để chạy thử nghiệm hoặc phát triển tiếp dự án này trên máy cục bộ của bạn, hãy làm theo các bước sau:

1. **Clone repository này về máy:**
   ```bash
   git clone [https://github.com/dmanhz06/Dmanhz_Social_App.git](https://github.com/dmanhz06/Dmanhz_Social_App.git)
   cd Dmanhz_Social_App
#### 2. Cấu hình dịch vụ Firebase
- Truy cập vào [Firebase Console](https://console.firebase.google.com/) và tạo một dự án mới.
- Thêm một ứng dụng Android vào dự án Firebase với package name trùng với dự án của bạn (`com.dmanhz.socialapp` hoặc package name thực tế của bạn).
- Tải file cấu hình `google-services.json` xuống và đặt nó vào thư mục `/app` trong cấu trúc dự án của bạn.

#### 3. Cấu hình OneSignal & Cloudinary
- Thay thế các API Key, App ID và thông tin cấu hình tương ứng của **OneSignal** và **Cloudinary** vào các file cấu hình hoặc hệ thống `Cloud Functions` của bạn.

#### 4. Build và Chạy ứng dụng
- Mở thư mục dự án bằng **Android Studio**.
- Đợi Gradle đồng bộ cấu hình (Sync).
- Kết nối thiết bị Android thật (bật chế độ USB Debugging) hoặc trình giả lập, sau đó nhấn nút **Run** (hoặc tổ hợp phím `Shift + F10`).

---

### 📸 Hình Ảnh Giao Diện (Screenshots)

#### 🔐 Xác Thực & Khởi Đầu (Authentication) - có thể đăng nhập bằng tài khoản Google
| Màn hình Đăng ký | Màn hình Đăng nhập |
| :---: | :---: |
| <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/signup.jpg" width="250" alt="Login Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/login.jpg" width="250" alt="Sign Up Screen"> |

#### 🌐 Không Gian Bản Tin & Tương Tác (Home, Feeds & Interaction)
| Trang chủ (Home) | Bảng tin (Feeds) | Khung bình luận (Comment) |
| :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/home.jpg" width="200" alt="Home Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/feeds.jpg" width="200" alt="Feeds Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/comment.jpg" width="200" alt="Comment Screen"> |

#### 🎵 Âm Nhạc & Góc Tâm Trạng (Music & Journal)
| Nghe nhạc (Music) | Nhật ký cảm xúc | Nhật ký tổng (Journal) | Thống kê cảm xúc (Stats) |
| :---: | :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/music.jpg" width="180" alt="Music Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/nhatki.jpg" width="180" alt="Nhật ký Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/journal.jpg" width="180" alt="Journal Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/stats.jpg" width="180" alt="Stats Screen"> |

#### 💬 Trò Chuyện Realtime & Cá Nhân Hóa (Chat & Settings)
| Danh sách Chat | Chi tiết nhắn tin (Chat Detail) | Cài đặt (Settings) |
| :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/chat.jpg" width="180" alt="Chat Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/chatdetail.jpg" width="180" alt="Chat Detail Screen"> | <img src="https://raw.githubusercontent.com/dmanhz06/Dmanhz_Social_App/main/screenshots/setting.jpg" width="180" alt="Setting Screen"> |

---


### 📬 Liên Hệ (Contact Me)

Nếu bạn có bất kỳ câu hỏi, góp ý hay ý tưởng hợp tác phát triển ứng dụng, vui lòng liên hệ qua:
- **Email:** [duymanhbui305@gmail.com](mailto:duymanhbui305@gmail.com)
- **GitHub:** [https://github.com/dmanhz06](https://github.com/dmanhz06)
- **Repository Link:** [https://github.com/dmanhz06/Dmanhz_Social_App](https://github.com/dmanhz06/Dmanhz_Social_App)
