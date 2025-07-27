import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'dart:io' show Platform;
import 'app.dart';

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