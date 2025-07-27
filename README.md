# TeneoCast - Indoor Radio Software Platform

**TeneoCast** is a cross-platform, multi-tenant indoor radio software platform that empowers local businesses to run customized radio stations with real-time control, offline playback, and flexible infrastructure.

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

- Java 17+
- Flutter 3.10+
- Docker & Docker Compose
- Node.js 18+ (for tooling)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/teneocast.git
   cd teneocast
   ```

2. **Choose your development mode**

   **🐳 Docker Mode (Recommended)**
   ```bash
   # Start everything in containers
   ./scripts/docker-dev.sh start all
   
   # Or start components separately
   ./scripts/docker-dev.sh start infrastructure
   ./scripts/docker-dev.sh start frontend
   ```

   **🖥️ Local Mode**
   ```bash
   # Start infrastructure services
   docker-compose up -d postgres redis minio
   
   # Run backend services locally
   cd backend
   ./gradlew bootRun
   
   # Run frontend applications locally
   ./scripts/dev/start-frontend.sh
   ```

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

Copyright © 2024 TeneoCast. All rights reserved. 