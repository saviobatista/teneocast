import 'package:flutter/material.dart';

class PlayHistory extends StatelessWidget {
  const PlayHistory({super.key});

  @override
  Widget build(BuildContext context) {
    final historyItems = [
      {'title': 'Morning Jazz Mix', 'time': '2 hours ago'},
      {'title': 'Classical Focus', 'time': '4 hours ago'},
      {'title': 'Ambient Workspace', 'time': '6 hours ago'},
      {'title': 'Electronic Chill', 'time': 'Yesterday'},
    ];

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Recently Played',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: Theme.of(context).colorScheme.onSurface,
              ),
            ),
            const SizedBox(height: 12),
            ...historyItems.map((item) {
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  children: [
                    Icon(
                      Icons.play_circle_outline,
                      size: 20,
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        item['title']!,
                        style: const TextStyle(fontWeight: FontWeight.w500),
                      ),
                    ),
                    Text(
                      item['time']!,
                      style: TextStyle(
                        fontSize: 12,
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              );
            }),
          ],
        ),
      ),
    );
  }
} 