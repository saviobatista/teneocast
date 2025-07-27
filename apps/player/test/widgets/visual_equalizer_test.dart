import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/presentation/widgets/visual_equalizer.dart';
import 'package:flutter/scheduler.dart';

void main() {
  group('VisualEqualizer Widget Tests', () {
    testWidgets('should display "Audio Levels" title', (WidgetTester tester) async {
      final animationController = AnimationController(
        duration: const Duration(milliseconds: 500),
        vsync: const TestVSync(),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: VisualEqualizer(
              isPlaying: false,
              equalizerController: animationController,
            ),
          ),
        ),
      );

      expect(find.text('Audio Levels'), findsOneWidget);
      
      animationController.dispose();
    });

    testWidgets('should display equalizer bars', (WidgetTester tester) async {
      final animationController = AnimationController(
        duration: const Duration(milliseconds: 500),
        vsync: const TestVSync(),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: VisualEqualizer(
              isPlaying: false,
              equalizerController: animationController,
            ),
          ),
        ),
      );

      // Should have 12 equalizer bars
      final containers = tester.widgetList<Container>(find.byType(Container));
      final equalizerBars = containers.where((container) {
        final decoration = container.decoration as BoxDecoration?;
        return decoration?.color == const Color(0xFFEF4444) &&
               decoration?.borderRadius == BorderRadius.circular(3);
      });
      expect(equalizerBars.length, equals(12));
      
      animationController.dispose();
    });

    testWidgets('should have proper card styling', (WidgetTester tester) async {
      final animationController = AnimationController(
        duration: const Duration(milliseconds: 500),
        vsync: const TestVSync(),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: VisualEqualizer(
              isPlaying: false,
              equalizerController: animationController,
            ),
          ),
        ),
      );

      final card = tester.widget<Card>(find.byType(Card));
      expect(card, isNotNull);
      
      animationController.dispose();
    });

    testWidgets('should have proper padding', (WidgetTester tester) async {
      final animationController = AnimationController(
        duration: const Duration(milliseconds: 500),
        vsync: const TestVSync(),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: VisualEqualizer(
              isPlaying: false,
              equalizerController: animationController,
            ),
          ),
        ),
      );

      final padding = tester.widget<Padding>(find.byType(Padding).first);
      expect(padding.padding, isA<EdgeInsets>());
      
      animationController.dispose();
    });

    testWidgets('should have proper height for equalizer area', (WidgetTester tester) async {
      final animationController = AnimationController(
        duration: const Duration(milliseconds: 500),
        vsync: const TestVSync(),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: VisualEqualizer(
              isPlaying: false,
              equalizerController: animationController,
            ),
          ),
        ),
      );

      final sizedBox = tester.widget<SizedBox>(find.byType(SizedBox).first);
      expect(sizedBox.height, isA<double>());
      
      animationController.dispose();
    });
  });
}

class TestVSync extends TickerProvider {
  const TestVSync();

  @override
  Ticker createTicker(TickerCallback onTick) {
    return Ticker(onTick);
  }
} 