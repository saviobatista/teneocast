import 'package:flutter/material.dart';
import 'package:teneocast_console/core/theme/app_theme.dart';
import 'package:teneocast_console/features/dashboard/presentation/pages/dashboard_page.dart';

class TeneoCastConsoleApp extends StatelessWidget {
  const TeneoCastConsoleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'TeneoCast Console',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.system,
      home: const DashboardPage(),
    );
  }
} 