# TeneoCast Player - Cross-Platform Flutter App

This is the unified TeneoCast Player application built with Flutter, supporting multiple platforms from a single codebase.

## Supported Platforms

- **Web** - Progressive Web App with offline support
- **Windows** - Desktop application with system tray integration
- **Android** - Mobile app with background playback

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

- Flutter 3.10+
- Platform-specific requirements:
  - **Web**: Chrome browser
  - **Windows**: Visual Studio 2022 with C++ development tools
  - **Android**: Android Studio with SDK

### Running for Development

```bash
# Web (development)
flutter run -d chrome

# Windows (development)
flutter run -d windows

# Android (with device/emulator)
flutter run -d android
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