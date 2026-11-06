# Block Out! - Color Sort Puzzle (Android & iOS Native)

Dự án đầy đủ gồm cả hai phiên bản native hoàn chỉnh:
- **Android**: Viết bằng **Kotlin** và **Jetpack Compose** (thư mục `app/`)
- **iOS**: Viết bằng **Swift** và **SwiftUI** (thư mục `ios/`)

Tham chiếu App Store: [Block Out! - Color Sort Puzzle](https://apps.apple.com/us/app/block-out-color-sort-puzzle/id6752672568)

---

## 📱 Cấu trúc Dự án

```
c:/Users/ASUS/Downloads/block out/
├── app/                                      # Dự án Android (Kotlin / Jetpack Compose)
│   └── src/main/java/com/blockout/puzzle/
├── ios/                                      # Dự án iOS Native (Swift / SwiftUI)
│   ├── BlockOut.xcodeproj/                   # Xcode Project mở trực tiếp trên Mac
│   ├── Package.swift                         # Hỗ trợ Swift Package Manager
│   ├── README.md                             # Hướng dẫn chi tiết bản iOS
│   └── BlockOut/
│       ├── App/                              # BlockOutApp.swift, ContentView.swift
│       ├── Models/                           # BlockColor, Block, Level, ExitDoor, Obstacle...
│       ├── Engine/                           # GameEngine.swift (Logic trượt, va chạm, thoát cổng)
│       ├── Sound/                            # SoundManager.swift (Arcade Synthesizer & Haptics)
│       ├── Data/                             # LevelRepository.swift, PreferencesManager.swift
│       └── UI/                               # GameBoardCanvas, TopBar, Boosters, Screens
```

## 🎮 Các tính năng & Cơ chế Gameplay đã xây dựng

1. **Cơ chế kéo trượt và thoát màu (Color Escape Slide Physics)**:
   - Các khối gạch đồ chơi Lego 3D với đinh tròn (studs), hiệu ứng bóng đổ, ánh sáng viền nổi (bevel highlights).
   - Biểu tượng nhận diện riêng: Sao (Star), Kim cương (Diamond), Vuông (Square), Tròn (Circle), Thập tự (Cross).
   - Ràng buộc trượt theo trục: Khối đứng chỉ trượt DỌC (Lên / Xuống), Khối ngang chỉ trượt NGANG (Trái / Phải).
   - Cổng thoát màu xung quanh bàn cờ (Top, Bottom, Left, Right) với mũi tên neon chỉ hướng. Khi trượt khối gạch cùng màu vào đúng cổng khớp hướng, khối sẽ **thoát ra ngoài bàn cờ** kèm hiệu ứng pháo hoa tia sáng hạt Lego (Particle Burst) và âm thanh ăn điểm.
   - Khi dọn sạch toàn bộ các khối màu trên sàn đấu -> **LEVEL CLEARED!**

2. **Hệ thống màn chơi phong phú (36+ Levels & Daily Challenge)**:
   - Các màn nhập môn (Level 1 - 5) giới thiệu cơ chế trượt gạch, tránh va chạm và chướng ngại vật đá (Rock Obstacles).
   - Tái hiện chính xác **Level 36: "The Canyon Mill"** (đúng theo hình ảnh bạn chụp: bàn cờ 6x6, hệ thống cổng trắng, xanh dương, đen, tím, xanh lá, hồng, cam, 34 lượt di chuyển giới hạn).
   - Màn thử thách hàng ngày **Daily Puzzle (+100 Coins)**: Tạo ngẫu nhiên nhưng cố định (deterministic) theo ngày lịch thực tế.

3. **Bộ bổ trợ / Boosters đầy đủ**:
   - **Undo (Hoàn tác)**: Quay lại nước đi trước đó.
   - **Hint (Gợi ý)**: Tự động tìm khối có thể giải hoặc mở lối thoát cho các khối khác.
   - **+5 Moves**: Thêm 5 lượt đi khi sắp hết giới hạn.
   - **Hammer (Búa đập chướng ngại vật)**: Phá hủy tảng đá/chướng ngại vật cản đường.

4. **Giao diện & Màn hình chính xác theo ảnh mẫu**:
   - **Home Screen**: Card Hero xem trước gạch 3D vui nhộn, tiến độ World 1 (Neon Outskirts), nút chơi tiếp "CONTINUE LEVEL", "LEVEL SELECT", banner Daily Puzzle.
   - **Themes / Customize**: Bộ sưu tập giao diện Midnight Glow, Sunny Meadow, Candy Cloud, Coral Sunset, Underwater World.
   - **Missions**: Danh sách thử thách cục bộ (Puzzle Apprentice, Path Finder, Swift Thinker, Par Perfection...) kèm tiến độ nhận thưởng xu.
   - **Âm thanh & Rung haptic đa tầng**: Tích hợp bộ tổng hợp âm thanh đa tần số tức thì (Sound Synthesizer qua AudioTrack) không phụ thuộc file mp3 ngoài, hỗ trợ rung phản hồi mượt mà.

---

## 📁 Cấu trúc Mã nguồn (Source Code Tree)

```
c:/Users/ASUS/Downloads/block out/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/blockout/puzzle/
│   │   │   ├── MainActivity.kt               # Điều hướng Jetpack Compose & vòng đời
│   │   │   ├── model/
│   │   │   │   ├── BlockColor.kt             # Bảng màu gạch 3D (Blue, Red, Green, Yellow, White, Black...)
│   │   │   │   ├── SlideDirection.kt         # Hướng trượt HORIZONTAL, VERTICAL, ALL
│   │   │   │   ├── ExitSide.kt               # Cổng thoát TOP, BOTTOM, LEFT, RIGHT
│   │   │   │   ├── ExitDoor.kt               # Vị trí, màu sắc cổng
│   │   │   │   ├── Block.kt                  # Kích thước, tọa độ, biểu tượng đinh gạch
│   │   │   │   ├── Obstacle.kt               # Chướng ngại vật đá / rương
│   │   │   │   ├── Level.kt                  # Cấu trúc dữ liệu màn chơi
│   │   │   │   └── GameProgress.kt           # Tiến độ, xu, sao, bộ sưu tập
│   │   │   ├── engine/
│   │   │   │   └── GameEngine.kt             # Logic va chạm, giới hạn trượt, phán đoán thoát, Undo, Hint
│   │   │   ├── sound/
│   │   │   │   └── SoundManager.kt           # Bộ âm thanh Arcade & phản hồi xúc giác rung (Haptics)
│   │   │   ├── data/
│   │   │   │   ├── LevelRepository.kt        # 36+ Màn chơi & Daily Puzzle Generator
│   │   │   │   └── PreferencesManager.kt     # Lưu dữ liệu offline SharedPreferences Gson
│   │   │   └── ui/
│   │   │       ├── components/
│   │   │       │   ├── GameBoardCanvas.kt    # Canvas vẽ đồ họa khối Lego 3D, cử chỉ kéo mượt 120fps, hiệu ứng hạt
│   │   │       │   ├── TopBar.kt             # Thanh hiển thị Xu, Bộ đếm giờ, Màn chơi, Lượt đi & Sao
│   │   │       │   ├── BoosterBar.kt         # 4 nút bổ trợ (Undo, Hint, +5 Moves, Hammer)
│   │   │       │   └── GameDialogs.kt        # Hộp thoại Chiến thắng, Thua cuộc, Cài đặt
│   │   │       └── screens/
│   │   │           ├── HomeScreen.kt         # Màn hình chính Color Escape
│   │   │           ├── GamePlayScreen.kt     # Màn hình chơi game chính
│   │   │           ├── LevelSelectScreen.kt  # Màn hình chọn cấp độ
│   │   │           ├── ThemesScreen.kt       # Màn hình tùy chỉnh giao diện
│   │   │           └── MissionsScreen.kt     # Màn hình nhiệm vụ nhận xu
│   │   └── res/                              # Icon ứng dụng vector, themes, strings
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```

---

## 🚀 Cách chạy và Build ứng dụng

1. **Chạy trực tiếp bằng Android Studio**:
   - Mở Android Studio -> Chọn `Open` -> Trỏ đến thư mục `C:\Users\ASUS\Downloads\block out`.
   - Chọn thiết bị máy ảo (Emulator) hoặc cắm điện thoại Android bật USB Debugging.
   - Bấm nút **Run (Shift + F10)**.

2. **Cài đặt file APK đã build sẵn**:
   - File APK đã được biên dịch thành công tại:
     `app/build/outputs/apk/debug/app-debug.apk`
   - Cài nhanh vào máy kết nối qua adb:
     ```powershell
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```

3. **Lệnh build APK mới từ dòng lệnh**:
   ```powershell
   .\gradlew assembleDebug
   ```

