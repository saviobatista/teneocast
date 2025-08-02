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

- **Docker & Docker Compose** - [Install Docker](https://docs.docker.com/get-docker/)
- **Git** - [Install Git](https://git-scm.com/downloads)

*Note: Java, Flutter, and Node.js are not required locally as all services run in Docker containers.*

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/saviobatista/teneocast.git
   cd teneocast
   ```

2. **Verify your setup**
   ```bash
   # Check Docker installation
   docker --version
   docker compose version
   
   # Ensure Docker daemon is running
   docker info
   ```

3. **Start the complete environment**
   ```bash
   # Start all services (frontend, backend, and infrastructure)
   docker compose up -d
   
   # View logs for all services
   docker compose logs -f
   
   # Stop all services
   docker compose down
   ```

   **What's included:**
   - ✅ PostgreSQL database with automatic migrations
   - ✅ Redis cache
   - ✅ MinIO (S3-compatible storage)
   - ✅ Frontend applications (Player, Studio, Console)
   - ✅ Auth Service (user authentication)
   - 🚧 Additional backend services (in development)

4. **Access the applications**
   - **Frontend Applications**: http://localhost:3000
     - Player, Studio, and Console are served from the same frontend container
   - **Auth Service API**: http://localhost:8081
   - **MinIO Console**: http://localhost:9001 (admin: teneocast/teneocast_dev)
   - **PostgreSQL**: localhost:5432 (user: teneocast/teneocast_dev)
   - **Redis**: localhost:6379

### 🚨 Troubleshooting

**Common Issues:**

1. **Port conflicts**
   ```bash
   # Check what's using ports
   lsof -i :3000  # Frontend
   lsof -i :8081  # Auth service
   lsof -i :5432  # PostgreSQL
   # Kill the process or change ports in docker-compose.yml
   ```

2. **Docker permission issues**
   ```bash
   sudo usermod -aG docker $USER
   # Log out and back in
   ```

3. **Services not starting**
   ```bash
   # Check service health
   docker compose ps
   # Check logs for specific service
   docker compose logs auth-service
   ```

4. **Database connection issues**
   ```bash
   # Restart PostgreSQL and dependent services
   docker compose restart postgres auth-service
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
- **Docker Compose**: Complete containerized development environment with all services
- **Single Command Setup**: Everything runs with `docker compose up -d`

### Production
- **Backend**: AWS Fargate + RDS + ElastiCache
- **Frontend**: S3 + CloudFront CDN
- **CI/CD**: GitHub Actions with multi-platform builds

### Docker Commands

```bash
# Start all services
docker compose up -d

# Start specific services
docker compose up -d postgres redis minio  # Infrastructure only
docker compose up -d frontend auth-service  # App services only

# View logs
docker compose logs -f                    # All services
docker compose logs -f frontend          # Specific service
docker compose logs -f auth-service      # Auth service logs

# Rebuild and restart services
docker compose up -d --build

# Stop services
docker compose down                       # Stop all
docker compose stop frontend             # Stop specific service

# Clean up (remove containers, networks, and volumes)
docker compose down -v --remove-orphans
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