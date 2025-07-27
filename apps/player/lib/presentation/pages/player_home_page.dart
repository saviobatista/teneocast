import 'package:flutter/material.dart';
import 'package:teneocast_player/presentation/widgets/status_bar.dart';
import 'package:teneocast_player/presentation/widgets/now_playing_card.dart';
import 'package:teneocast_player/presentation/widgets/player_controls.dart';
import 'package:teneocast_player/presentation/widgets/visual_equalizer.dart';
import 'package:teneocast_player/presentation/widgets/play_history.dart';
import 'package:teneocast_player/presentation/widgets/help_dialog.dart';
import 'package:teneocast_player/presentation/widgets/bug_report_dialog.dart';

class PlayerHomePage extends StatefulWidget {
  const PlayerHomePage({super.key});

  @override
  State<PlayerHomePage> createState() => _PlayerHomePageState();
}

class _PlayerHomePageState extends State<PlayerHomePage>
    with TickerProviderStateMixin {
  bool _isPlaying = false;
  bool _isOnline = true;
  bool _isConnected = true;
  bool _isUpToDate = true;
  bool _isSynced = true;
  
  late AnimationController _equalizerController;
  
  @override
  void initState() {
    super.initState();
    _equalizerController = AnimationController(
      duration: const Duration(milliseconds: 500),
      vsync: this,
    );
  }
  
  @override
  void dispose() {
    _equalizerController.dispose();
    super.dispose();
  }
  
  void _togglePlayPause() {
    setState(() {
      _isPlaying = !_isPlaying;
      if (_isPlaying) {
        _equalizerController.repeat();
      } else {
        _equalizerController.stop();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.background,
      appBar: AppBar(
        title: Row(
          children: [
            Image.asset(
              'assets/images/logo.png',
              height: 32,
              errorBuilder: (context, error, stackTrace) =>
                  const Icon(Icons.radio, color: Color(0xFFEF4444)),
            ),
            const SizedBox(width: 12),
            const Text(
              'TeneoCast Player',
              style: TextStyle(fontWeight: FontWeight.w600),
            ),
          ],
        ),
        backgroundColor: Theme.of(context).colorScheme.surface,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.help_outline),
            onPressed: () => _showHelpDialog(context),
            tooltip: 'Help',
          ),
          IconButton(
            icon: const Icon(Icons.bug_report_outlined, size: 20),
            onPressed: () => _showBugReportDialog(context),
            tooltip: 'Report Issue',
          ),
        ],
      ),
      body: Column(
        children: [
          // Status Bar
          StatusBar(
            isOnline: _isOnline,
            isConnected: _isConnected,
            isSynced: _isSynced,
            isUpToDate: _isUpToDate,
          ),
          
          // Main Player Area
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  // Now Playing Card
                  NowPlayingCard(isPlaying: _isPlaying),
                  
                  const SizedBox(height: 20),
                  
                  // Controls
                  PlayerControls(
                    isPlaying: _isPlaying,
                    onPlayPause: _togglePlayPause,
                  ),
                  
                  const SizedBox(height: 24),
                  
                  // Visual Equalizer
                  VisualEqualizer(
                    isPlaying: _isPlaying,
                    equalizerController: _equalizerController,
                  ),
                  
                  const SizedBox(height: 24),
                  
                  // Play History
                  const PlayHistory(),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _showHelpDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => const HelpDialog(),
    );
  }

  void _showBugReportDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => const BugReportDialog(),
    );
  }
} 