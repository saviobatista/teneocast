# Indoor Radio Software Provider - Project Documentation (Full Stack Plan)

## TeneoCast Overview

**Product Name**: TeneoCast\
**Domain**: [www.TeneoCast.com](https://www.TeneoCast.com)

This project aims to create a cross-platform, multi-tenant indoor radio software platform for local businesses to run customized radio stations with real-time control, offline playback, and flexible infrastructure. The system includes three key interfaces:

- **Player**: Audio playback with smart controls and offline support
- **Studio**: Interface for local business owners to manage station preferences and send remote commands
- **Console**: Admin panel for platform administrators to manage tenants, media libraries, system analytics, and audio content distribution

## Visual Identity & Branding



### Primary Brand: **TeneoCast**

TeneoCast blends the Latin word *"teneo"* (to hold or possess) with *"cast"* (broadcast), capturing the essence of empowering users with control over audio streaming.

### Logo & Theme Suggestions

- **Logotype**: Clean sans-serif (e.g., Manrope, Satoshi, Inter)
- **Symbol**: Abstract radiowave tower or frequency waveform in a circle
- **Style**: Modern minimalism + motion lines
- **Color Palette**:
  - Deep navy + electric indigo (main)
  - Vibrant gradient accents: lime, violet, and coral per interface
- **Icons**:
  - Player: Play icon + equalizer
  - Studio: Sliders + waveform
  - Console: Radar/tower icon

## App Interface Naming

\| Name | Purpose | | --------- | ------- | --------------------------------------------------------------------------- | | Player | Plays content offline and responds to remote commands | Plays content offline and responds to remote commands                       | | Studio | Tenant-facing interface for scheduling, playlists, and sending commands | Tenant-facing interface for scheduling, playlists, and sending commands     | | Console | Admin interface for managing tenants, content, and global system operations | Admin interface for managing tenants, content, and global system operations |

### Logo & Theme Suggestions

- **Typography**: Montserrat, Manrope, Poppins
- **Color Palette**:
  - Indigo / Deep Blue → Console
  - Purple Gradient → Studio
  - Emerald or Lime → Player
- **Icons**:
  - Player: Headphones / Waveform
  - Studio: Slider + Music Note
  - Console: Tower + Dashboard

## System Architecture Overview

### General Architecture Markdown

```md
# TeneoCast System Architecture Overview

## Clients
- Player (Windows, Android, Web): Flutter app for offline audio playback
- Studio: Flutter app for tenant-level configuration, scheduling, commands
- Console: Flutter app for global admin and tenant management

## Core Services (Spring Boot)
- auth-service: Auth, JWT, role-based access, pairing
- tenant-service: Tenants, subdomains, preferences, users
- media-service: Music & ad asset upload, S3 storage
- player-service: WebSocket control, pairing, command dispatch
- analytics-service: Playback logs, Kafka ingestion, reporting
- tts-service: TTS generation and delivery
- admin-service: Global content, impersonation, platform settings
- common: DTOs, error handling, shared utils

## Queues
- Kafka (optional initially):
  - Player → analytics-service (metrics)
  - Studio → tts-service (TTS commands)

## Storage
- PostgreSQL: Relational data (tenants, users, assets)
- S3 / MinIO: Audio files (music, ads, TTS)
- Redis: Caching WebSocket sessions, pairing tokens

## DevOps
- Local Dev: Docker Compose (Postgres, Redis, MinIO, Spring Boot)
- CI/CD: GitHub Actions
- Monitoring: Datadog (logs, metrics)

## Runtime
- Prod Infra: AWS Fargate for all services
- Dev/Test Infra: Docker Compose + Testcontainers
```

### Remote Command Delivery (Studio → Player)

1. Studio UI sends remote command via REST (e.g., POST `/api/player/{id}/command`)
2. Backend (player-service) enqueues command and pushes via WebSocket to the corresponding connected Player
3. Player processes the command (e.g., play ad, TTS, pause) and acknowledges
4. Acknowledgment optionally logged or pushed to analytics

### WebSocket Protocol Structure

#### Connection

- Endpoint: `wss://api.teneocast.com/ws/player`
- Auth: JWT passed via query param or headers
- Protocol: JSON

#### Incoming Message Types (from Server → Player)

```json
{
  "type": "COMMAND",
  "commandType": "PLAY_AD", // or PLAY_TTS, PAUSE, RESUME, SKIP
  "payload": {
    "audioUrl": "https://cdn.teneocast.com/ads/ad-123.mp3"
  }
}
```

```json
{
  "type": "UPDATE_SETTINGS",
  "payload": {
    "musicPreferences": [...],
    "adSchedule": [...]
  }
}
```

#### Outgoing Message Types (from Player → Server)

```json
{
  "type": "STATUS",
  "payload": {
    "nowPlaying": "track-abc",
    "uptime": 4350,
    "lastCommand": "PLAY_AD"
  }
}
```

```json
{
  "type": "ACK",
  "payload": {
    "messageId": "uuid-of-message",
    "receivedAt": "2025-07-25T12:00:00Z"
  }
}
```

### High-Level Architecture Diagram

### Components

#### Player

- Single Flutter codebase supporting Web, Windows, and Android
- Platform-adaptive UI with persistent WebSocket connection
- Offline playback with cached queue and platform-specific features
- Responds to TTS and remote commands with audio ducking

#### Studio

- Flutter-based app
- Manages playback configuration and user permissions
- Sends commands and defines schedules

#### Console

- Admin panel built in Flutter
- Global management of tenants and assets
- Can impersonate Studio users

## Multi-Tenancy Model

Each tenant has:

- Custom music/ad preferences
- Independent analytics
- Scoped user accounts

## Offline Playback Strategy

- Downloads audio assets on sync
- TTS and remote commands cached
- Player runs offline until reconnect

## Tech Stack

### Frontend

- Flutter for all interfaces

### Backend

- Java (Spring Boot + WebFlux)
- PostgreSQL, Redis, Kafka, S3
- Batch jobs for audio and reporting
- Datadog for observability

## Infrastructure Strategy

- AWS Fargate (prod), Docker Compose (dev)
- GitHub Actions CI
- Testcontainers for backend tests

## Core Features

- Persistent socket control
- Studio configures what genres/ads are eligible
- AI TTS dispatch
- Offline music + ad logic with playback rules
- High availability deployment ready

## 🛡 User Roles & Access Control

### Console

- Root: full power
- Operator: restricted but tenant-wide view
- Both can impersonate Studio users

### Studio

- Master: full permissions
- Producer: upload + remote commands
- Manager: user management only

### Player

- Registered by Studio using pairing code
- No roles but must authenticate initially

## 🔄 API Interaction Flows

### Studio ↔ Backend Interactions

#### Remote Command API

- `POST /api/player/{playerId}/command`
  - Body:
    ```json
    {
      "type": "PLAY_AD",
      "payload": {
        "adId": "uuid",
        "audioUrl": "https://cdn.teneocast.com/ads/ad-123.mp3"
      }
    }
    ```
  - Action: Enqueues command for WebSocket delivery to Player

#### TTS Message API

- `POST /api/tts`
  - Body:
    ```json
    {
      "text": "Attention shoppers: enjoy 10% off today!",
      "playerId": "uuid"
    }
    ```
  - Action: Triggers TTS generation → returns audio path → player receives PLAY\_TTS command

#### Player Settings API

- `GET /api/player/{id}/settings`
  - Returns playback preferences, genres, ad schedules

#### Upload Media

- `POST /api/media/music`
- `POST /api/media/ad`
  - Action: Uploads asset to S3 with metadata

#### Pair Player

- `POST /api/player/pair`
  - Body: `{ "code": "XYZ123" }`
  - Action: Links a newly installed player to tenant

### Player Setup Flow

- Installed via .exe (Windows) or Play Store (Android)
- First launch: shows pairing code
- Linked by Studio user
- Web player only accessible from Studio (temporary)

### Player Usage Flow

- Connects to backend and retrieves playback settings
- Downloads tracks/ads
- Begins playback when minimum cache is met
- Responds to remote commands with audio ducking

### Player TTS Flow

- Studio user writes message
- Backend generates audio
- Player plays TTS with volume transition

## 🛡 Security Model

### Authentication & Authorization

- **JWT Access Tokens**: Issued by `auth-service` upon login
  - Short-lived (e.g. 15 mins)
  - Contains user ID, tenant ID, and role
- **Refresh Tokens**:
  - Stored securely and rotated upon use
  - Used to re-issue access tokens
- **Role-Based Access Control**:
  - Defined at route-level per module
  - Roles: ROOT, OPERATOR, MASTER, PRODUCER, MANAGER, PLAYER

### CORS & HTTPS

- CORS allowed origins:
  - `https://studio.teneocast.com`
  - `https://console.teneocast.com`
  - `https://*.teneocast.com` (for paired players)
- HTTPS enforced on all endpoints via load balancer (CloudFront or ALB)

### Player Pairing Security

- On first run, Player requests pairing code:
  - 6-character alphanumeric
  - Stored in Redis with TTL (e.g. 10 minutes)
  - One-time use: invalidated after first match
- Studio pairs using `/api/player/pair { code }`
- Once paired, JWT issued and future connections require token

### WebSocket Auth

- WebSocket connection must include:
  - JWT in query string or `Authorization: Bearer` header
  - If invalid, connection is rejected

---

## Backend Module Structure (Spring Boot Monolith)

## 🧪 Integration Testing Strategy

TeneoCast's backend uses **Testcontainers** to support robust integration tests with real dependencies. These tests are run locally and in CI (GitHub Actions) using Docker.

### Tools:

- **JUnit 5** with Spring Boot Test
- **Testcontainers Java** for PostgreSQL, Redis, MinIO, Kafka
- **MockMvc or WebTestClient** for API contract testing

### Sample Stack in Testcontainers:

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

@Container
static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

@Container
static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));
```

### Best Practices:

- Test each module (auth, media, player, etc.) with real DB and mocks
- Use lifecycle hooks to pre-seed test data (Flyway or @Sql)
- Test WebSocket interaction using Spring’s STOMP/WebSocketTest support
- Run tests on every push via GitHub Actions (CI)

### GitHub Actions Example Snippet:

```yaml
services:
  postgres:
    image: postgres:15
    ports: [5432:5432]
  redis:
    image: redis:7
    ports: [6379:6379]
  kafka:
    image: confluentinc/cp-kafka:7.3.0
    ports: [9092:9092]

steps:
  - name: Run tests
    run: ./gradlew test
```

### 1. `auth-service`

Handles:

- User login, JWT token issuance
- Role-based access control
- Player pairing flow

### 2. `tenant-service`

Handles:

- Tenant and subdomain management
- Preferences
- Studio user CRUD

### 3. `media-service`

Handles:

- Uploads of music and ads
- S3 storage integration
- Genre/ad type registries

### 4. `player-service`

Handles:

- WebSocket server for real-time control
- Player command queue
- Playback settings delivery

### 5. `analytics-service`

Handles:

- Playback log ingestion and storage
- Kafka consumer setup
- Reporting APIs

### 6. `tts-service`

Handles:

- Generating or requesting TTS audio
- Returning audio path for Player use

### 7. `admin-service`

Handles:

- Console global config and impersonation
- Platform-wide settings

### Shared Module: `common`

- DTOs
- Enums
- Utilities
- Error Handling

## 📦 Data Models

### MusicGender

```ts
id: UUID
name: String
```

### Music

```ts
id: UUID
name: String
gender_id: UUID (FK → MusicGender)
file: String (S3 object key)
metadata: JSON (optional)
```

### AdType

```ts
id: UUID
tenant_id: UUID (nullable — null = default)
name: String
is_selectable: Boolean
can_play_remotely: Boolean
can_play_individually: Boolean
```

### AdRule

```ts
id: UUID
tenant_id: UUID
ad_type_id: UUID (FK → AdType)
music_interval: Integer  // # of musics before ad sequence
ads_per_interval: Integer // # of ads played per sequence
```

### AdMedia

```ts
id: UUID
tenant_id: UUID
ad_type_id: UUID
name: String
file: String (S3 key)
metadata: JSON (optional)
```

### Tenant

```ts
id: UUID
name: String
subdomain: String
preferences: JSON
```

### User

```ts
id: UUID
tenant_id: UUID (nullable)
email: String
password_hash: String
role: Enum("ROOT", "OPERATOR", "MASTER", "PRODUCER", "MANAGER")
is_active: Boolean
created_at: Timestamp
last_login_at: Timestamp
```

### Player

```ts
id: UUID
tenant_id: UUID
name: String
pairing_code: String
is_active: Boolean
settings: JSON
last_seen_at: Timestamp
```

---

Let’s continue iterating from here.

