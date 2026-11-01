# Hướng dẫn build RadiaNano bằng điện thoại

1. Tải và giải nén `RadiaNano_Fixed_Project.zip`.
2. Mở repository GitHub của bạn, chọn **Add file → Upload files**.
3. Chọn tất cả nội dung bên trong thư mục đã giải nén, bao gồm `.github` (nếu không thấy thư mục này, bật hiển thị tệp ẩn hoặc dùng GitHub web để tạo thư mục workflow).
4. Tải lên ở thư mục gốc repository, giữ nguyên cấu trúc `app/`, `build.gradle.kts`, `settings.gradle.kts` và `.github/workflows/`.
5. Commit thay đổi vào nhánh `main`.
6. Mở tab **Actions** → **Build RadiaNano APK** → **Run workflow** → **Run workflow**.
7. Khi chạy xong có dấu xanh, mở lần chạy đó, kéo xuống **Artifacts**, tải `RadiaNano-APK`.
8. Giải nén và cài `app-debug.apk`. Nếu Android cảnh báo, chỉ cho phép cài ứng dụng từ nguồn bạn tin cậy.

Nếu repository đã có workflow cũ `build-radianano-apk.yml` hoặc `build-apk.yml`, hãy xóa workflow cũ để tránh chạy hai bản build cùng lúc.
