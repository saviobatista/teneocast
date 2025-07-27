import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'dart:io' show Platform;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize Hive for local storage
  await Hive.initFlutter();
  
  // Platform-specific initialization
  if (!kIsWeb) {
    if (Platform.isWindows) {
      // Windows-specific initialization
      // Import window_manager only when needed
      // await windowManager.ensureInitialized();
    } else if (Platform.isAndroid) {
      // Android-specific initialization
      // Request permissions, setup background tasks, etc.
    }
  }
  
  runApp(const TeneoCastPlayerApp());
}

class TeneoCastPlayerApp extends StatelessWidget {
  const TeneoCastPlayerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'TeneoCast Player',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF10B981), // Emerald/lime
          brightness: Brightness.light,
        ),
        useMaterial3: true,
        fontFamily: 'Manrope',
      ),
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF10B981),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
        fontFamily: 'Manrope',
      ),
      home: const PlayerHomePage(),
    );
  }
}

class PlayerHomePage extends StatelessWidget {
  const PlayerHomePage({super.key});

  String get _platformName {
    if (kIsWeb) return 'Web';
    if (Platform.isWindows) return 'Windows';
    if (Platform.isAndroid) return 'Android';
    if (Platform.isIOS) return 'iOS';
    return 'Unknown';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('TeneoCast Player - $_platformName'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.play_circle_filled,
              size: 120,
              color: Color(0xFF10B981),
            ),
            const SizedBox(height: 24),
            const Text(
              'TeneoCast Player',
              style: TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Audio playback with smart controls - $_platformName',
              style: const TextStyle(
                fontSize: 18,
                color: Colors.grey,
              ),
            ),
            const SizedBox(height: 48),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  children: [
                    const Text(
                      'Welcome to TeneoCast Player',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Professional audio playback with offline support\nand remote control capabilities.\n\nRunning on: $_platformName',
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 24),
                    _buildPlatformFeatures(),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPlatformFeatures() {
    if (kIsWeb) {
      return const Column(
        children: [
          Icon(Icons.web, color: Color(0xFF10B981)),
          SizedBox(height: 8),
          Text('Progressive Web App\nInstallable & Offline Ready'),
        ],
      );
    } else if (!kIsWeb && Platform.isWindows) {
      return const Column(
        children: [
          Icon(Icons.desktop_windows, color: Color(0xFF10B981)),
          SizedBox(height: 8),
          Text('Windows Desktop\nSystem Tray & Auto-start'),
        ],
      );
    } else if (!kIsWeb && Platform.isAndroid) {
      return const Column(
        children: [
          Icon(Icons.phone_android, color: Color(0xFF10B981)),
          SizedBox(height: 8),
          Text('Android Mobile\nBackground Playback & Notifications'),
        ],
      );
    }
    
    return const SizedBox.shrink();
  }
} 