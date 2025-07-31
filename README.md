# TeneoCast - Indoor Radio Software Platform

**TeneoCast** is a cross-platform, multi-tenant indoor radio software platform that empowers local businesses to run customized radio stations with real-time control, offline playback, and flexible infrastructure.

## 📊 Status

[![Build Status](https://github.com/saviobatista/teneocast/workflows/CI/badge.svg)](https://github.com/saviobatista/teneocast/actions)
[![Test Coverage](https://codecov.io/gh/saviobatista/teneocast/branch/main/graph/badge.svg)](https://codecov.io/gh/saviobatista/teneocast)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Flutter Version](https://img.shields.io/badge/Flutter-3.10+-blue.svg)](https://flutter.dev)
[![Java Version](https://img.shields.io/badge/Java-17+-green.svg)](https://adoptium.net/)

## 🎯 Overview

TeneoCast blends the Latin word *"teneo"* (to hold or possess) with *"cast"* (broadcast), capturing the essence of empowering users with control over audio streaming.

### Core Interfaces

- **Player**: Audio playback with smart controls and offline support (Windows, Android, Web)
- **Studio**: Interface for local business owners to manage station preferences and send remote commands
- **Console**: Admin panel for platform administrators to manage tenants, media libraries, and system analytics

## 🏗️ Architecture

```
TeneoCast System Architecture

Clients:
├── Player (Flutter)
│   ├── Windows (.exe)
│   ├── Android (.apk)
│   └── Web (Progressive Web App)
├── Studio (Flutter Web)
└── Console (Flutter Web)

Backend Services (Spring Boot):
├── auth-service     → Authentication & authorization
├── tenant-service   → Multi-tenancy management
├── media-service    → Audio asset management
├── player-service   → WebSocket control & commands
├── analytics-service → Playback analytics & reporting
├── tts-service      → Text-to-speech generation
└── admin-service    → Platform administration

Infrastructure:
├── PostgreSQL  → Relational data
├── Redis       → Caching & sessions
├── Kafka       → Event streaming (optional)
└── S3/MinIO    → Audio file storage
```

## 🚀 Quick Start

### Prerequisites

- **Java 17+** - [Download from Adoptium](https://adoptium.net/)
- **Flutter 3.10+** - [Install Flutter](https://flutter.dev/docs/get-started/install)
- **Docker & Docker Compose** - [Install Docker](https://docs.docker.com/get-docker/)
- **Node.js 18+** (for tooling) - [Download Node.js](https://nodejs.org/)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/saviobatista/teneocast.git
   cd teneocast
   ```

2. **Verify your setup**
   ```bash
   # Check Flutter installation
   flutter doctor
   
   # Check Java version
   java -version
   
   # Check Docker
   docker --version
   docker-compose --version
   ```

3. **Choose your development mode**

   **🐳 Docker Mode (Infrastructure Only)**
   ```bash
   # Start infrastructure services in containers
   docker compose -f docker-compose.dev.yml up -d
   
   # Run Flutter apps locally
   cd apps/studio && flutter run -d chrome
   cd apps/console && flutter run -d chrome  
   cd apps/player && flutter run -d chrome
   ```

   **🖥️ Quick Setup**
   ```bash
   # Use the development setup script
   ./scripts/dev-setup.sh
   ```

4. **Access the applications**
   - **Player (Web)**: http://localhost:3000
   - **Studio**: http://localhost:3001
   - **Console**: http://localhost:3002
   - **Backend API**: http://localhost:8080

### 🚨 Troubleshooting

**Common Issues:**

1. **Flutter not found**
   ```bash
   export PATH="$PATH:$HOME/flutter/bin"
   ```

2. **Port conflicts**
   ```bash
   # Check what's using port 8080
   lsof -i :8080
   # Kill the process or change ports in docker-compose.yml
   ```

3. **Docker permission issues**
   ```bash
   sudo usermod -aG docker $USER
   # Log out and back in
   ```

4. **Java version mismatch**
   ```bash
   # Set JAVA_HOME
   export JAVA_HOME=/path/to/java17
   ```

**Getting Help:**
- Check the [Issues](https://github.com/saviobatista/teneocast/issues) page
- Join our [Discord](https://discord.gg/teneocast) community
- Review the [Full Stack Plan](docs/indoor_radio_full_stack_plan.md)

## 📁 Project Structure

```
teneocast/
├── apps/                    # Frontend applications
│   ├── player/             # Cross-platform player (Flutter: Web, Windows, Android)
│   ├── studio/             # Studio interface (Flutter Web)
│   └── console/            # Admin console (Flutter Web)
├── backend/                # Spring Boot services
│   ├── auth-service/       # Authentication service
│   ├── tenant-service/     # Tenant management
│   ├── media-service/      # Media asset management
│   ├── player-service/     # Player control & WebSocket
│   ├── analytics-service/  # Analytics & reporting
│   ├── tts-service/        # Text-to-speech service
│   ├── admin-service/      # Admin operations
│   └── common/             # Shared utilities & DTOs
├── infrastructure/         # Docker, K8s, Terraform
├── docs/                   # Documentation
├── scripts/                # Build & deployment scripts
└── .github/workflows/      # CI/CD pipelines
```

## 🛡️ Security & Multi-tenancy

- **JWT-based authentication** with role-based access control
- **Multi-tenant architecture** with tenant isolation
- **Player pairing** using secure one-time codes
- **WebSocket security** with token-based authentication

## 🔧 Development

### Backend Services

Each backend service is a Spring Boot application with:
- RESTful APIs
- WebSocket support (player-service)
- PostgreSQL integration
- Redis caching
- Comprehensive testing with Testcontainers

### Frontend Applications

All frontend applications use Flutter with:
- Responsive design
- Offline-first architecture (Player)
- Real-time updates via WebSocket
- Modern UI/UX with Material Design

## 🚢 Deployment

### Development
- **Docker Compose**: Full containerized development environment
- **Local Development**: Mix of containerized infrastructure and local apps

### Production
- **Backend**: AWS Fargate + RDS + ElastiCache
- **Frontend**: S3 + CloudFront CDN
- **CI/CD**: GitHub Actions with multi-platform builds

### Docker Commands

```bash
# Full environment
./scripts/docker-dev.sh start all

# Frontend only
./scripts/docker-dev.sh start frontend

# Infrastructure only  
./scripts/docker-dev.sh start infrastructure

# View logs
./scripts/docker-dev.sh logs studio

# Clean up
./scripts/docker-dev.sh clean
```

## 📖 Documentation

- [Full Stack Plan](docs/indoor_radio_full_stack_plan.md)
- [CI/CD Strategy](docs/teneo_cast_ci_cd.md)
- [API Documentation](docs/api/)
- [Deployment Guide](docs/deployment/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

TeneoCast uses a **dual-licensing model**:
- **🆓 Free for individuals and small businesses** (≤ 10 employees)
- **💼 Commercial licenses for larger organizations**

See [LICENSE.md](LICENSE.md) for detailed licensing information.

**Copyright © 2024 TeneoCast. All rights reserved.** 