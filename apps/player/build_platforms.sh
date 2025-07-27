#!/bin/bash

# TeneoCast Player - Multi-platform Build Script
# This script builds the player app for Windows and Android platforms

set -e

echo "🎵 TeneoCast Player - Building for Windows and Android"
echo "======================================================"

# Check if Flutter is available
if ! command -v flutter &> /dev/null; then
    echo "❌ Flutter is not installed or not in PATH"
    exit 1
fi

# Get Flutter version
echo "📱 Flutter version:"
flutter --version

echo ""
echo "🔍 Checking available platforms..."
flutter devices

echo ""
echo "🏗️  Building for Android (Debug)..."
flutter build apk --debug

echo ""
echo "🏗️  Building for Windows (Debug)..."
flutter build windows --debug

echo ""
echo "✅ Builds completed successfully!"
echo ""
echo "📦 Build outputs:"
echo "   Android APK: build/app/outputs/flutter-apk/app-debug.apk"
echo "   Windows: build/windows/runner/Debug/"
echo ""
echo "🚀 To run the apps:"
echo "   Android: flutter run -d android"
echo "   Windows: flutter run -d windows" 