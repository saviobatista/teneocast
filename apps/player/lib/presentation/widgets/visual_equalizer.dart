import 'package:flutter/material.dart';
import 'dart:math' as math;

class VisualEqualizer extends StatelessWidget {
  final bool isPlaying;
  final AnimationController equalizerController;

  const VisualEqualizer({
    super.key,
    required this.isPlaying,
    required this.equalizerController,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Audio Levels',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: Theme.of(context).colorScheme.onSurface,
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 100,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: List.generate(12, (index) {
                  return AnimatedBuilder(
                    animation: equalizerController,
                    builder: (context, child) {
                      final height = isPlaying
                          ? 20 + (60 * math.sin(equalizerController.value * 2 * math.pi + index * 0.5))
                          : 20.0;
                      return Container(
                        width: 6,
                        height: height.abs(),
                        decoration: BoxDecoration(
                          color: const Color(0xFFEF4444),
                          borderRadius: BorderRadius.circular(3),
                        ),
                      );
                    },
                  );
                }),
              ),
            ),
          ],
        ),
      ),
    );
  }
} 