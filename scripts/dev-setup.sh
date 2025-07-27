#!/bin/bash

# TeneoCast Development Setup Script
# This script sets up the local development environment

set -e

echo "🚀 TeneoCast Development Setup"
echo "==============================="

# Check for required tools
echo "📋 Checking prerequisites..."

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker first."
    exit 1
fi

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose not found. Please install Docker Compose first."
    exit 1
fi

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17+ first."
    exit 1
fi

# Check Flutter
if ! command -v flutter &> /dev/null; then
    echo "❌ Flutter not found. Please install Flutter 3.10+ first."
    exit 1
fi

echo "✅ All prerequisites found!"

# Setup environment file
echo "📝 Setting up environment variables..."
if [ ! -f .env ]; then
    cp .env.example .env
    echo "✅ Created .env file from template. Please review and update values."
else
    echo "⚠️  .env file already exists. Skipping..."
fi

# Start infrastructure services
echo "🐳 Starting infrastructure services..."
docker-compose up -d postgres redis minio

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check PostgreSQL
until docker-compose exec postgres pg_isready -U teneocast -d teneocast &> /dev/null; do
    echo "⏳ Waiting for PostgreSQL..."
    sleep 2
done
echo "✅ PostgreSQL is ready!"

# Check Redis
until docker-compose exec redis redis-cli ping &> /dev/null; do
    echo "⏳ Waiting for Redis..."
    sleep 2
done
echo "✅ Redis is ready!"

# Setup MinIO buckets
echo "🗄️  Setting up MinIO buckets..."
docker run --rm --network teneocast_teneocast_network \
    --entrypoint sh minio/mc:latest -c "
    mc alias set minio http://minio:9000 teneocast teneocast_dev &&
    mc mb minio/teneocast-media || true &&
    mc policy set public minio/teneocast-media
"
echo "✅ MinIO buckets configured!"

# Build backend common module
echo "🔧 Building backend common module..."
cd backend
./gradlew :common:build
cd ..

# Install Flutter dependencies
echo "📱 Installing Flutter dependencies..."
for app in studio console player; do
    if [ -d "apps/$app" ]; then
        echo "Installing dependencies for $app..."
        cd "apps/$app"
        flutter pub get
        cd ../..
    fi
done

# Create development scripts
echo "📜 Creating development scripts..."
mkdir -p scripts/dev

# Backend start script
cat > scripts/dev/start-backend.sh << 'EOF'
#!/bin/bash
cd backend
echo "Starting backend services..."
./gradlew bootRun --parallel
EOF

# Frontend start script
cat > scripts/dev/start-frontend.sh << 'EOF'
#!/bin/bash
echo "Starting frontend applications..."

# Start Studio
cd apps/studio
flutter run -d chrome --web-port 3001 &
STUDIO_PID=$!

# Start Console  
cd ../console
flutter run -d chrome --web-port 3002 &
CONSOLE_PID=$!

# Start Player Web
cd ../player
flutter run -d chrome --web-port 3003 &
PLAYER_PID=$!

cd ../..

echo "Frontend applications started!"
echo "Studio: http://localhost:3001"
echo "Console: http://localhost:3002" 
echo "Player: http://localhost:3003"

# Wait for interrupt
trap "kill $STUDIO_PID $CONSOLE_PID $PLAYER_PID" EXIT
wait
EOF

chmod +x scripts/dev/*.sh

echo ""
echo "🎉 Development environment setup complete!"
echo ""
echo "📖 Next steps:"
echo "  Choose one of the following development modes:"
echo ""
echo "  🐳 Docker Mode (Recommended):"
echo "    ./scripts/docker-dev.sh start frontend        # Frontend in containers"
echo "    ./scripts/docker-dev.sh start all            # Everything in containers"
echo ""
echo "  🖥️  Local Mode:"
echo "    ./scripts/dev/start-backend.sh                # Local backend"
echo "    ./scripts/dev/start-frontend.sh               # Local frontend"
echo ""
echo "🌐 Web interfaces:"
echo "  • Studio:  http://localhost:3001"
echo "  • Console: http://localhost:3002"
echo "  • Player:  http://localhost:3003"
echo ""
echo "🗄️  Infrastructure:"
echo "  • PostgreSQL: localhost:5432"
echo "  • Redis:      localhost:6379"
echo "  • MinIO:      http://localhost:9001 (admin: teneocast/teneocast_dev)"
echo ""
echo "📚 Documentation: docs/"
echo ""
echo "Happy coding! 🚀" 