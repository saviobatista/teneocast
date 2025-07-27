import 'package:flutter/material.dart';

class NowPlayingCard extends StatelessWidget {
  final bool isPlaying;

  const NowPlayingCard({
    super.key,
    required this.isPlaying,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 4,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Text(
              'Now Playing',
              style: TextStyle(
                fontSize: 14,
                color: Theme.of(context).colorScheme.onSurfaceVariant,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              isPlaying ? 'Ambient Mix - Channel 1' : 'Ready to Play',
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w600,
              ),
            ),
            if (isPlaying) ...[
              const SizedBox(height: 8),
              Text(
                'Relaxing background music for focus',
                style: TextStyle(
                  fontSize: 14,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
} 