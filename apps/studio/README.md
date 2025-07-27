# TeneoCast Studio

A comprehensive Flutter web application for managing indoor radio stations, built with modern architecture and best practices.

## 🎯 Overview

TeneoCast Studio is the management interface for local business owners to control their indoor radio stations. It provides real-time monitoring, remote command execution, and comprehensive analytics for multiple players.

## ✨ Features

### 📊 Dashboard Overview
- **Real-time Statistics**: Connected players, total audios, playlist items reproduced, TTS generated
- **Quick Actions**: Generate TTS and send commands directly from the overview
- **Recent Activity**: Live feed of recent commands and TTS generations
- **Visual Analytics**: Beautiful charts and metrics with gradient cards

### 🎵 Player Management
- **Player Status Monitoring**: Real-time status tracking (Online, Offline, Pairing, Error)
- **Search & Filter**: Find players by name or filter by status
- **Player Registration**: Register new players with automatic pairing code generation
- **Remote Control**: Send commands, generate TTS, shutdown players
- **Player Login**: Direct access to player interfaces

### 📈 Activity Tracking
- **Command History**: Complete log of all remote commands sent to players
- **TTS History**: Track all text-to-speech generations and their success status
- **Real-time Updates**: Live updates as commands are executed

### 🎛️ Remote Commands
- **Play Ad**: Send audio advertisements to specific players
- **Play TTS**: Generate and play text-to-speech messages
- **Playback Control**: Pause, resume, skip tracks
- **System Control**: Shutdown players remotely

## 🏗️ Architecture

### State Management
- **BLoC Pattern**: Clean separation of business logic and UI
- **Event-Driven**: Reactive architecture with event-based communication
- **Immutable State**: Predictable state management with Equatable

### Code Organization
```
lib/
├── core/
│   ├── models/          # Data models with JSON serialization
│   ├── services/        # Business logic and API calls
│   └── utils/          # Shared utilities
├── features/
│   └── dashboard/      # Dashboard feature module
│       ├── data/       # Data layer
│       ├── domain/     # Business logic
│       └── presentation/
│           ├── bloc/    # State management
│           ├── pages/   # Main UI pages
│           └── widgets/ # Reusable UI components
└── shared/
    ├── constants/      # App constants
    └── widgets/       # Shared UI components
```

### Key Components

#### Models
- **Player**: Represents a radio player with status, pairing, and settings
- **DashboardStats**: System statistics and metrics
- **CommandLog**: History of remote commands sent to players
- **TTSLog**: Text-to-speech generation history

#### BLoC Events
- `LoadDashboard`: Initialize dashboard with data
- `RefreshDashboard`: Reload all dashboard data
- `FilterPlayers`: Search and filter player list
- `SendCommand`: Execute remote commands
- `GenerateTTS`: Create text-to-speech
- `RegisterPlayer`: Add new player
- `ShutdownPlayer`: Remote shutdown

#### UI Widgets
- **StatsCard**: Beautiful gradient cards for metrics
- **PlayerCard**: Player information with action buttons
- **ActivityLogCard**: Recent activity display
- **CommandDialog**: Remote command interface
- **TTSDialog**: Text-to-speech generation
- **RegisterPlayerDialog**: New player registration

## 🎨 Design System

### Color Palette
- **Primary**: Purple gradient (#7C3AED)
- **Success**: Green (#10B981)
- **Warning**: Orange (#F59E0B)
- **Error**: Red (#EF4444)
- **Info**: Blue (#3B82F6)

### Typography
- **Font Family**: Manrope (clean, modern sans-serif)
- **Weights**: Regular, Medium, SemiBold, Bold

### Components
- **Material 3**: Modern Material Design components
- **Responsive**: Adaptive layout for different screen sizes
- **Accessible**: WCAG compliant with proper contrast ratios

## 🧪 Testing

### Widget Tests
- **StatsCard**: Rendering and styling verification
- **PlayerCard**: Player information display and interactions
- **ActivityLogCard**: Activity log rendering

### BLoC Tests
- **DashboardBloc**: State management and event handling
- **Async Operations**: Proper handling of simulated API calls
- **State Transitions**: Loading, success, and error states

### Test Coverage
- **Unit Tests**: Business logic and utilities
- **Widget Tests**: UI component behavior
- **Integration Tests**: Feature workflows

## 🚀 Getting Started

### Prerequisites
- Flutter 3.16.0 or higher
- Dart 3.1.0 or higher

### Installation
```bash
# Navigate to the studio directory
cd apps/studio

# Install dependencies
flutter pub get

# Generate code (for JSON serialization)
flutter packages pub run build_runner build --delete-conflicting-outputs

# Run the app
flutter run -d chrome
```

### Development
```bash
# Run tests
flutter test

# Analyze code
flutter analyze

# Format code
flutter format .

# Generate code
flutter packages pub run build_runner build --delete-conflicting-outputs
```

## 📱 Features in Detail

### Dashboard Overview
The main dashboard provides a comprehensive view of the radio station system:

1. **Statistics Cards**: Real-time metrics with beautiful gradients
2. **Quick Actions**: Common tasks accessible from the overview
3. **Recent Activity**: Live feed of system activity
4. **Tab Navigation**: Overview, Players, and Activity tabs

### Player Management
Complete player lifecycle management:

1. **Player Registration**: Create new players with pairing codes
2. **Status Monitoring**: Real-time connection status
3. **Remote Control**: Send commands and generate TTS
4. **Search & Filter**: Find specific players quickly

### Command System
Remote command execution with full history:

1. **Command Types**: Play ad, TTS, pause, resume, skip, shutdown
2. **Target Selection**: Choose specific players for commands
3. **Payload Support**: Custom data for complex commands
4. **Success Tracking**: Monitor command execution status

### TTS Integration
Text-to-speech generation and playback:

1. **Text Input**: Multi-line text input with character limits
2. **Player Targeting**: Send TTS to specific players
3. **Generation Tracking**: Monitor TTS creation and playback
4. **History Logging**: Complete TTS activity history

## 🔧 Configuration

### Environment Variables
```bash
API_BASE_URL=https://api.teneocast.com
WS_BASE_URL=wss://api.teneocast.com/ws
```

### Dependencies
- **flutter_bloc**: State management
- **equatable**: Value equality
- **intl**: Internationalization
- **json_annotation**: JSON serialization
- **flutter_svg**: SVG support
- **dio**: HTTP client
- **web_socket_channel**: Real-time communication

## 🚢 Deployment

### Web Build
```bash
flutter build web --release
```

### Docker
```bash
docker build -t teneocast-studio .
docker run -p 80:80 teneocast-studio
```

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch**
3. **Write tests** for new functionality
4. **Follow the code style** and architecture patterns
5. **Submit a pull request**

### Code Style
- **Dart**: Follow official Dart style guide
- **Flutter**: Use Flutter best practices
- **BLoC**: Follow BLoC pattern conventions
- **Testing**: Maintain high test coverage

## 📄 License

This project is part of the TeneoCast platform. See the main LICENSE file for details.

---

**Built with ❤️ using Flutter and modern web technologies** 