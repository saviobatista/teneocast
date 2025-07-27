#!/bin/bash

# TeneoCast Build Script
# Builds all project components

set -e

echo "🏗️  TeneoCast Build Script"
echo "========================="

# Parse command line arguments
BUILD_TARGET=${1:-all}
BUILD_MODE=${2:-debug}

case $BUILD_TARGET in
    "backend")
        echo "🔧 Building backend services..."
        cd backend
        ./gradlew build
        cd ..
        ;;
    
    "frontend")
        echo "📱 Building frontend applications..."
        
        # Build web applications
        for app in studio console; do
            echo "Building $app for web..."
            cd apps/$app
            flutter build web --release
            cd ../..
        done
        
        # Build player for web
        echo "Building player for web..."
        cd apps/player
        flutter build web --release
        cd ../..
        
        # Build Windows player if requested
        if [[ "$BUILD_MODE" == "release" ]] && [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
            echo "Building player for Windows..."
            cd apps/player
            flutter build windows --release
            cd ../..
        fi
        
        # Build Android player if requested
        if [[ "$BUILD_MODE" == "release" ]] && command -v flutter &> /dev/null; then
            echo "Building player for Android..."
            cd apps/player
            flutter build apk --release
            flutter build appbundle --release
            cd ../..
        fi
        ;;
        
    "docker")
        echo "🐳 Building Docker images..."
        
        # Build backend service images
        services=("auth-service" "player-service" "tenant-service" "media-service" "analytics-service" "tts-service" "admin-service")
        
        for service in "${services[@]}"; do
            echo "Building $service Docker image..."
            docker build -t teneocast/$service:latest -f backend/$service/Dockerfile backend/$service
        done
        ;;
        
    "all")
        echo "🚀 Building everything..."
        
        # Build backend
        echo "🔧 Building backend..."
        cd backend
        ./gradlew build
        cd ..
        
        # Build frontend
        echo "📱 Building frontend..."
        for app in studio console player; do
            echo "Building $app for web..."
            cd apps/$app
            flutter build web --release
            cd ../..
        done
        
        echo "✅ Build complete!"
        ;;
        
    *)
        echo "❌ Invalid build target: $BUILD_TARGET"
        echo "Usage: $0 [backend|frontend|docker|all] [debug|release]"
        exit 1
        ;;
esac

echo ""
echo "🎉 Build completed successfully!"

case $BUILD_TARGET in
    "frontend"|"all")
        echo ""
        echo "📦 Frontend build artifacts:"
        echo "  • Studio:  apps/studio/build/web/"
        echo "  • Console: apps/console/build/web/"
        echo "  • Player:  apps/player/build/web/ (+ Windows/Android if built)"
        ;;
esac

case $BUILD_TARGET in
    "backend"|"all")
        echo ""
        echo "📦 Backend build artifacts:"
        echo "  • JAR files:  backend/*/build/libs/"
        ;;
esac 