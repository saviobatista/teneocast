# Player Service

The Player Service is a core component of the TeneoCast system responsible for managing audio player devices and facilitating real-time communication between Studio applications and Player clients.

## Features

### Core Functionality
- **Player Management**: Register, configure, and monitor audio player devices
- **WebSocket Communication**: Real-time bidirectional communication with Player clients
- **Player Pairing**: Secure device pairing using temporary codes
- **Command Dispatch**: Send remote commands (play, pause, skip, volume, etc.) to players
- **Session Management**: Track active player sessions and connection states

### Remote Commands
- `PLAY_AD` - Play advertisement content
- `PLAY_TTS` - Play text-to-speech messages
- `PLAY_TRACK` - Play specific audio tracks
- `PAUSE` / `RESUME` - Control playback state
- `SKIP` / `STOP` - Navigation commands
- `SET_VOLUME` - Volume control
- `UPDATE_SETTINGS` - Push configuration updates

### WebSocket Protocol
- **Connection**: `wss://api.teneocast.com/ws/player?token=JWT&playerId=UUID`
- **Authentication**: JWT-based with player ID validation
- **Heartbeat**: Automatic connection monitoring and cleanup
- **Message Types**: Command dispatch, status updates, acknowledgments, pairing

## API Endpoints

### Player Management
- `POST /api/players` - Create new player
- `GET /api/players/{id}` - Get player details
- `GET /api/players?tenantId=X` - List players by tenant
- `GET /api/players/online?tenantId=X` - List online players
- `POST /api/players/{id}/pairing-code` - Generate pairing code

### Remote Commands
- `POST /api/player/{id}/command` - Send generic command
- `POST /api/player/{id}/play-ad` - Play advertisement
- `POST /api/player/{id}/play-tts` - Play TTS message
- `POST /api/player/{id}/pause` - Pause playback
- `POST /api/player/{id}/resume` - Resume playback
- `POST /api/player/{id}/skip` - Skip current track
- `POST /api/player/{id}/volume` - Set volume level

### Health & Monitoring
- `GET /health` - Service health check with active player counts

## Technology Stack

- **Framework**: Spring Boot 3.2.0 with Java 17
- **Database**: PostgreSQL with Flyway migrations
- **Cache**: Redis for session management and pairing codes
- **WebSockets**: Spring WebSocket with custom message handlers
- **Security**: JWT-based authentication
- **Build**: Gradle with comprehensive testing setup

## Database Schema

### Players Table
- Player registration and configuration
- Platform support (Web, Windows, Android, iOS)
- Capabilities and current status
- Pairing code management

### Player Sessions Table
- Active WebSocket session tracking
- Connection metadata and timestamps
- Session lifecycle management

### Player Capabilities Table
- Feature support matrix per player
- Platform-specific capabilities

## Configuration

### Environment Variables
- `SPRING_DATASOURCE_URL` - PostgreSQL connection string
- `SPRING_REDIS_HOST` - Redis cache host
- `WS_ALLOWED_ORIGINS` - WebSocket CORS origins
- `PLAYER_PAIRING_CODE_EXPIRY` - Pairing code timeout (seconds)

### Application Profiles
- `dev` - Local development with embedded services
- `docker` - Container deployment configuration
- `test` - Test environment with H2 database

## Development

### Running Locally
```bash
# From the backend directory
cd backend
./gradlew :player-service:bootRun
```

### Running Tests
```bash
# From the backend directory
cd backend
./gradlew :player-service:test
```

### Building the Service
```bash
# From the backend directory
cd backend
./gradlew :player-service:build
```

### Building Docker Image
```bash
# From the backend directory
cd backend
docker build -t teneocast/player-service ./player-service
```

## Integration

### With Studio Application
1. Studio creates player via API
2. Studio generates pairing code for player
3. Player app connects to WebSocket with pairing code
4. Studio sends remote commands via REST API
5. Commands are dispatched to player via WebSocket

### With Player Applications
1. Player apps connect to WebSocket endpoint
2. Authentication via JWT token
3. Real-time command reception and acknowledgment
4. Status reporting back to service
5. Automatic reconnection and session management

## Monitoring

- Health endpoint provides service status
- Active player and session counts
- Redis-based cross-instance session awareness
- Comprehensive logging for debugging
- Scheduled cleanup of expired pairing codes

## Security

- JWT-based WebSocket authentication
- CORS configuration for allowed origins
- Input validation on all endpoints
- Secure pairing code generation and expiry
- Session isolation per tenant