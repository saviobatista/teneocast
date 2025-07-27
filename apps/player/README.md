# TeneoCast Player - Cross-Platform Flutter App

[![Build Status](https://github.com/saviobatista/teneocast/workflows/Player%20CI/badge.svg)](https://github.com/saviobatista/teneocast/actions)
[![Test Coverage](https://codecov.io/gh/saviobatista/teneocast/branch/main/graph/badge.svg?flag=player)](https://codecov.io/gh/saviobatista/teneocast)
[![Flutter Version](https://img.shields.io/badge/Flutter-3.10+-blue.svg)](https://flutter.dev)

This is the unified TeneoCast Player application built with Flutter, supporting multiple platforms from a single codebase.

## 🎯 Features

- **Cross-Platform**: Single codebase for Web, Windows, and Android
- **Offline-First**: Cached audio playback with smart sync
- **Real-Time Control**: WebSocket-based remote commands
- **Modern UI**: Material Design 3 with TeneoCast branding
- **Comprehensive Testing**: 46+ widget tests with 100% coverage

## Supported Platforms

- **Web** - Progressive Web App with offline support
- **Windows** - Desktop application with system tray integration  
- **Android** - Mobile app with background playback
- **macOS** - Desktop application (development only)

## Platform-Specific Features

### Web
- Progressive Web App (PWA) capabilities
- Installable from browser
- Offline music caching
- WebSocket real-time control

### Windows
- MSIX installer package
- System tray integration
- Auto-start on Windows boot
- Background audio service

### Android
- Background playback with notifications
- Wake lock for continuous operation
- Permission management
- Workmanager for background tasks

## Development

### Prerequisites

- **Flutter 3.10+** - [Install Flutter](https://flutter.dev/docs/get-started/install)
- Platform-specific requirements:
  - **Web**: Chrome browser
  - **Windows**: Visual Studio 2022 with C++ development tools (Windows only)
  - **Android**: Android Studio with SDK
  - **macOS**: Xcode (macOS only)

### Project Structure

```
apps/player/
├── lib/
│   ├── app.dart                    # Main app widget
│   ├── main.dart                   # App entry point
│   └── presentation/
│       ├── pages/
│       │   └── player_home_page.dart
│       └── widgets/
│           ├── status_bar.dart
│           ├── now_playing_card.dart
│           ├── player_controls.dart
│           ├── visual_equalizer.dart
│           ├── play_history.dart
│           ├── help_dialog.dart
│           └── bug_report_dialog.dart
├── test/
│   ├── widget_test.dart            # Main app tests
│   └── widgets/                    # Individual widget tests
└── web/                           # Web-specific assets
```

### Running for Development

```bash
# Navigate to player directory
cd apps/player

# Check Flutter setup
flutter doctor

# Web (development)
flutter run -d chrome

# Windows (development)
flutter run -d windows

# Android (with device/emulator)
flutter run -d android

# macOS (development)
flutter run -d macos
```

### Testing

```bash
# Run all tests
flutter test

# Run tests with coverage
flutter test --coverage

# Run specific test file
flutter test test/widgets/status_bar_test.dart
```

### Building for Production

```bash
# Web build
flutter build web --release

# Windows build
flutter build windows --release
flutter pub run msix:create  # Creates MSIX installer

# Android build
flutter build apk --release        # APK
flutter build appbundle --release  # App Bundle for Play Store

# macOS build
flutter build macos --release
```

### Multi-Platform Build Script

For convenience, use the provided build script to build for all platforms:

```bash
# Build for Windows and Android (debug)
./build_platforms.sh

# Or build individual platforms
flutter build windows --debug
flutter build apk --debug
```

## Platform Detection

The app automatically detects the current platform and adapts:

```dart
import 'package:flutter/foundation.dart';
import 'dart:io' show Platform;

if (kIsWeb) {
  // Web-specific code
} else if (Platform.isWindows) {
  // Windows-specific code
} else if (Platform.isAndroid) {
  // Android-specific code
}
```

## Configuration

Platform-specific configurations are handled in:

- `pubspec.yaml` - Dependencies and build settings
- `web/` - Web app manifest and service worker
- `windows/` - Windows app configuration
- `android/` - Android app configuration and permissions

## Architecture

The app follows a clean architecture pattern with:

- **BLoC** for state management
- **Hive** for local storage
- **WebSocket** for real-time communication
- **Just Audio** for audio playback
- Platform-specific services when needed

## Audio Playback

- **Web**: Uses Web Audio API through just_audio
- **Windows**: Native Windows audio APIs
- **Android**: MediaPlayer with AudioService for background playback

## Offline Support

- Music files cached locally using Hive
- Queue management for offline playback
- Smart sync when connection restored
- Platform-specific storage locations

## Remote Control

All platforms support WebSocket-based remote control from the Studio interface:

- Play/pause commands
- Skip track
- Volume control
- TTS message injection
- Playlist updates

## Building & Deployment

See the root project's CI/CD workflows in `.github/workflows/frontend-ci.yml` for automated building and deployment across all platforms. 