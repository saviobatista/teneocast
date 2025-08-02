# TeneoCast Player Service - Bruno API Collection

This Bruno collection provides comprehensive API testing for the TeneoCast Player Service.

## 🚀 Getting Started

### 1. Install Bruno
```bash
# Using npm
npm install -g @usebruno/cli

# Or download the desktop app from https://www.usebruno.com/
```

### 2. Open Collection
```bash
# Open in Bruno desktop app
bruno open .

# Or run from command line
bru run --env Local
```

### 3. Environment Setup
The collection includes two environments:
- **Local**: For testing against locally running service
- **Docker**: For testing against containerized service

Both point to `http://localhost:8082` by default.

## 📁 Collection Structure

### Health Check
- **Health Check**: Verify service is running and healthy

### Player Management
- **Create Player**: Register a new player device
- **Get Player by ID**: Retrieve player details
- **List Players by Tenant**: Get all players for a tenant
- **Get Online Players**: List currently online players
- **Generate Pairing Code**: Create pairing code for device registration
- **Get Player Settings**: Retrieve player configuration

### Remote Commands
- **Send Generic Command**: Send any command type
- **Play Advertisement**: Trigger ad playback
- **Play TTS Message**: Send text-to-speech message
- **Pause Player**: Pause current playback
- **Resume Player**: Resume playback
- **Skip Track**: Skip to next track
- **Stop Player**: Stop all playback
- **Set Volume**: Adjust player volume

## 🔧 Usage Tips

### Environment Variables
The collection uses these variables:
- `baseUrl`: Service endpoint (default: http://localhost:8082)
- `tenantId`: Tenant identifier for testing
- `playerId`: Player ID (auto-populated when creating a player)
- `pairingCode`: Generated pairing code
- `authToken`: JWT token for authentication (when implemented)

### Running Tests

#### Individual Requests
1. Select an environment (Local or Docker)
2. Run individual requests to test specific functionality
3. Check the test results in the response panel

#### Full Collection
```bash
# Run all tests
bru run --env Local

# Run specific folder
bru run --env Local --folder "Player Management"

# Generate report
bru run --env Local --output results.json
```

### Test Flow
1. **Health Check**: Verify service is running
2. **Create Player**: Creates a player and stores the ID
3. **Generate Pairing Code**: Creates pairing code for the player
4. **Test Commands**: Send various remote commands
5. **Verify Settings**: Check player configuration

## 🧪 Test Scenarios

### Basic Functionality
- Service health and availability
- Player CRUD operations
- Pairing code generation and validation

### Remote Control
- Command dispatch to connected players
- Error handling for offline players
- Priority-based command queuing

### WebSocket Integration
- Real-time command delivery
- Session management
- Connection state tracking

## 📊 Expected Responses

### Successful Operations
- **Status 200**: Operation completed successfully
- **Response includes**: Success flag, message IDs, timestamps

### Player Offline
- **Status 400**: Player not connected
- **Response includes**: Error message explaining connectivity issue

### Invalid Requests
- **Status 404**: Player not found
- **Status 400**: Invalid request data

## 🔍 Debugging

### Common Issues
1. **Service not responding**: Check if player-service is running on port 8082
2. **Player not found**: Ensure you've created a player first
3. **Commands failing**: Player must have active WebSocket connection for commands

### Logs
```bash
# Check service logs
docker compose logs player-service

# Check service status
docker compose ps player-service
```

## 🔐 Authentication

Currently, the API endpoints don't require authentication for testing. When authentication is implemented:

1. Update the `authToken` environment variable
2. Add authentication headers to requests that require them
3. Handle token refresh logic

## 🎯 Integration Testing

This collection can be integrated into CI/CD pipelines:

```bash
# In your CI pipeline
npm install -g @usebruno/cli
bru run --env Docker --output test-results.json
```

## 📝 Notes

- Player commands will return 400 status if no WebSocket connection exists
- The `playerId` variable is automatically populated when creating a player
- Test assertions verify both success and error scenarios
- Environment variables can be updated between test runs