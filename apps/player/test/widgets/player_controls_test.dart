import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/presentation/widgets/player_controls.dart';

void main() {
  group('PlayerControls Widget Tests', () {
    testWidgets('should display play/pause button', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayerControls(
              isPlaying: false,
              onPlayPause: () {},
            ),
          ),
        ),
      );

      expect(find.byIcon(Icons.play_arrow), findsOneWidget);
      expect(find.byIcon(Icons.pause), findsNothing);
    });

    testWidgets('should display pause icon when playing', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayerControls(
              isPlaying: true,
              onPlayPause: () {},
            ),
          ),
        ),
      );

      expect(find.byIcon(Icons.pause), findsOneWidget);
      expect(find.byIcon(Icons.play_arrow), findsNothing);
    });

    testWidgets('should display previous and next buttons', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayerControls(
              isPlaying: false,
              onPlayPause: () {},
            ),
          ),
        ),
      );

      expect(find.byIcon(Icons.skip_previous), findsOneWidget);
      expect(find.byIcon(Icons.skip_next), findsOneWidget);
    });

    testWidgets('should call onPlayPause when play button is tapped', (WidgetTester tester) async {
      bool callbackCalled = false;
      
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayerControls(
              isPlaying: false,
              onPlayPause: () => callbackCalled = true,
            ),
          ),
        ),
      );

      await tester.tap(find.byIcon(Icons.play_arrow));
      await tester.pump();

      expect(callbackCalled, isTrue);
    });

    testWidgets('should have proper button styling', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayerControls(
              isPlaying: false,
              onPlayPause: () {},
            ),
          ),
        ),
      );

      // Should have a container with orange background for the play button
      final containers = tester.widgetList<Container>(find.byType(Container));
      final orangeContainer = containers.where((container) {
        final decoration = container.decoration as BoxDecoration?;
        return decoration?.color == const Color(0xFFEF4444);
      });
      expect(orangeContainer.length, equals(1));
    });

    testWidgets('should have proper spacing between buttons', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayerControls(
              isPlaying: false,
              onPlayPause: () {},
            ),
          ),
        ),
      );

      // Should have SizedBox widgets for spacing
      expect(find.byType(SizedBox), findsWidgets);
    });
  });
} 