# 🎬 CineMax - Movies, TV Series & Anime App

<p align="center">
  <h1 align="center" style="border-bottom: none;">
    <code style="background-color: #1B5E20; color: white; padding: 10px 20px; border-radius: 15px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; font-size: 48px;">CMAX</code>
  </h1>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Flutter-%2302569B.svg?style=for-the-badge&logo=Flutter&logoColor=white" alt="Flutter">
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">
  <img src="https://img.shields.io/badge/Dart-0175C2?style=for-the-badge&logo=dart&logoColor=white" alt="Dart">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
</p>

---
## � Download CineMax

Get the latest version of CineMax for your Android device:

<p align="center">
  <a href="https://github.com/M0SAD67/CinMax/releases/download/V1.0.0/app-release.apk">
    <img src="https://img.shields.io/badge/Download-Latest%20APK-1B5E20?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
  </a>
  <br><br>
  <a href="https://github.com/M0SAD67/CinMax/releases"><b>Browse all versions and changelogs on the Releases page</b></a>
</p>

---

## 📝 Project Overview
**CineMax** is a high-performance streaming application built with Flutter, designed to offer a seamless experience for watching movies, TV shows, and anime. It integrates multiple sources from around the world, providing a Netflix-like interface with advanced features like Watch Parties and AI-powered recommendations.

---

## ✨ Key Features

- 🎥 **Comprehensive Content**: Support for movies, TV series, and anime from global and regional sources.
- 🌐 **Multi-Source Integration**: Scrapers for popular sources including Wecima, FaselHD, ArabSeed, HiAnime, Anime4Up, and more.
- 👥 **Watch Party**: Create rooms to watch content simultaneously with friends, including real-time chat.
- 🤖 **AI Assistant**: Smart chat assistant powered by Gemini AI to help you find your next favorite show.
- 📥 **Offline Viewing**: Download content in various qualities for watching without an internet connection.
- 📺 **Advanced Video Player**:
  - Custom subtitle support and styling.
  - Server and quality selection.
  - Gesture controls for brightness and volume.
  - Picture-in-Picture (PiP) mode.
- 🌍 **Multi-Language Support**: Localized in 9 languages (Arabic, English, French, Spanish, etc.).
- 🌙 **Modern UI/UX**: Sleek glassmorphism design with support for both Dark and Light modes.
- 🔐 **User Accounts**: Firebase and Google Sign-In for syncing favorites and watch history.
- 🔔 **Smart Notifications**: Stay updated with new episodes from your watchlist.

---

## 📂 Project Structure (lib/)

The `lib/` directory follows a modular architecture for scalability and maintenance:

```bash
lib/
├── ⚙️ config/
│   ├── constants.dart      # Global app constants
│   ├── routes.dart         # App routing logic
│   └── theme.dart          # App themes and styling
├── 🌍 l10n/
│   ├── app_ar.arb          # Localization files (9+ languages)
│   └── app_localizations.dart
├── 📦 models/
│   ├── movie.dart          # Content data models
│   ├── tv_series.dart
│   └── watch_party_room.dart
├── 🏗️ providers/
│   ├── content_provider.dart # State management (Provider)
│   ├── auth_provider.dart
│   └── watch_party_provider.dart
├── 📱 screens/
│   ├── auth/               # Authentication screens
│   ├── player/             # Video player interfaces
│   ├── home_screen.dart    # Dashboard
│   └── details_screen.dart # Content information
├── 🛠️ services/
│   ├── sources/            # Web scrapers for 50+ sources
│   ├── ai_service.dart     # Gemini AI integration
│   ├── auth_service.dart   # Firebase Auth service
│   └── download_service.dart # Background downloading logic
├── 🔧 utils/
│   ├── error_handler.dart  # Global error management
│   └── responsive.dart     # Screen scaling and layout tools
├── 🧩 widgets/
│   ├── player/             # Player-specific components
│   ├── subtitle/           # Subtitle rendering widgets
│   └── movie_card.dart     # Reusable UI components
├── firebase_options.dart   # Firebase configuration
└── main.dart               # App entry point
```

---

## 🚀 Tech Stack & Dependencies

The project leverages a robust set of libraries to provide a high-quality streaming experience.

### **Core Framework & State**
```bash
pubspec.yaml/
├── 💙 flutter (SDK)         # UI Framework
├── 🏗️ provider             # State Management
└── 🌐 dio & http           # Networking & API calls
```

### **Media & Video Playback**
```bash
pubspec.yaml/
├── 🎬 media_kit            # Cross-platform video engine
├── 📹 media_kit_video      # Video rendering & controls
├── 🎼 media_kit_libs_video # Platform-specific codecs
├── 📝 subtitle             # Subtitle parsing & display
└── 🎨 palette_generator    # Dynamic UI colors from posters
```

### **Backend & AI Services**
```bash
pubspec.yaml/
├── 🔥 firebase_core        # Firebase initialization
├── 🔐 firebase_auth        # User authentication
├── ☁️ cloud_firestore      # Real-time database
├── 💬 firebase_messaging   # Push notifications
├── 🤖 google_generative_ai # Gemini AI integration
└── 🔑 google_sign_in       # Social authentication
```

### **UI Components & Animations**
```bash
pubspec.yaml/
├── 🖼️ cached_network_image # Image caching & loading
├── 🧪 shimmer              # Loading skeletons
├── 🎠 carousel_slider      # Featured content sliders
├── 🎭 lottie               # Vector animations
├── ⚡ flutter_animate      # Smooth UI transitions
├── 💎 m3e_design          # Material 3 components
└── 📍 iconsax             # Modern icon set
```

### **Storage & Utilities**
```bash
pubspec.yaml/
├── 🗄️ sqflite             # Local SQLite database
├── 💾 shared_preferences   # Local settings storage
├── 📥 flutter_downloader   # Background downloads
├── 🛠️ path_provider        # File system access
├── 🔒 encrypt & crypto     # Security & hashing
└── 🔗 url_launcher         # Opening external links
```

---

## 🛠️ How to Get Started

1. **Prerequisites**: Ensure you have Flutter SDK installed.
2. **Setup**:
   ```bash
   flutter pub get
   ```
3. **Run**:
   ```bash
   # For Android/iOS
   flutter run
   ```

---

<p align="center">
  Developed with ❤️ by the CineMax Team
</p>
