# RadiaNano

Ứng dụng Android đọc dữ liệu USB Serial từ Arduino Nano qua cáp OTG.

## Build APK không cần Android Studio

Dự án đã kèm GitHub Actions tại `.github/workflows/build-apk.yml`.

1. Giải nén toàn bộ ZIP.
2. Tải **các tệp và thư mục bên trong** lên thư mục gốc repository GitHub (không tải nguyên file ZIP lên). Giữ nguyên thư mục `.github/workflows`.
3. Vào tab **Actions** → **Build RadiaNano APK** → **Run workflow**.
4. Chờ job hoàn tất màu xanh. Mở phần **Artifacts** và tải `RadiaNano-APK`.
5. Giải nén artifact và cài `app-debug.apk` trên điện thoại Android.

## Kết nối Arduino

- USB OTG, cáp có truyền dữ liệu.
- Serial: 9600 baud, 8-N-1.
- Mỗi bản tin kết thúc bằng newline (`\n`). Hỗ trợ `CPM:12`, `PULSE:345`, hoặc một số nguyên đơn lẻ (được hiểu là CPM).

## Lưu ý đo lường

Ứng dụng hiển thị số đếm CPM và tổng xung do Arduino gửi. Không tự quy đổi CPM thành µSv/h. Muốn hiển thị suất liều cần hiệu chuẩn đầu dò và hệ số chuyển đổi phù hợp.
