class AppConstants {
  // App Information
  static const String appName = 'TeneoCast Studio';
  static const String appVersion = '1.0.0';
  static const String appDescription = 'Manage your indoor radio station';
  
  // API Configuration
  static const String apiBaseUrl = 'https://api.teneocast.com';
  static const String wsBaseUrl = 'wss://api.teneocast.com/ws';
  static const Duration apiTimeout = Duration(seconds: 30);
  
  // UI Configuration
  static const double defaultPadding = 16.0;
  static const double defaultRadius = 12.0;
  static const Duration animationDuration = Duration(milliseconds: 300);
  
  // Colors
  static const int primaryColor = 0xFF7C3AED;
  static const int successColor = 0xFF10B981;
  static const int warningColor = 0xFFF59E0B;
  static const int errorColor = 0xFFEF4444;
  static const int infoColor = 0xFF3B82F6;
  
  // Player Status
  static const String onlineStatus = 'Online';
  static const String offlineStatus = 'Offline';
  static const String pairingStatus = 'Pairing';
  static const String errorStatus = 'Error';
  
  // Command Types
  static const String playAdCommand = 'Play Ad';
  static const String playTtsCommand = 'Play TTS';
  static const String pauseCommand = 'Pause';
  static const String resumeCommand = 'Resume';
  static const String skipCommand = 'Skip';
  static const String shutdownCommand = 'Shutdown';
  
  // Error Messages
  static const String networkErrorMessage = 'Network error occurred. Please check your connection.';
  static const String serverErrorMessage = 'Server error occurred. Please try again later.';
  static const String unknownErrorMessage = 'An unknown error occurred.';
  static const String timeoutErrorMessage = 'Request timed out. Please try again.';
  
  // Success Messages
  static const String commandSentMessage = 'Command sent successfully';
  static const String ttsGeneratedMessage = 'TTS generated successfully';
  static const String playerRegisteredMessage = 'Player registered successfully';
  static const String playerShutdownMessage = 'Player shutdown successfully';
} 