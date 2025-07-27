import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/presentation/widgets/now_playing_card.dart';

void main() {
  group('NowPlayingCard Widget Tests', () {
    testWidgets('should display "Now Playing" title', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: NowPlayingCard(isPlaying: false),
          ),
        ),
      );

      expect(find.text('Now Playing'), findsOneWidget);
    });

    testWidgets('should display "Ready to Play" when not playing', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: NowPlayingCard(isPlaying: false),
          ),
        ),
      );

      expect(find.text('Ready to Play'), findsOneWidget);
      expect(find.text('Ambient Mix - Channel 1'), findsNothing);
    });

    testWidgets('should display track info when playing', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: NowPlayingCard(isPlaying: true),
          ),
        ),
      );

      expect(find.text('Ambient Mix - Channel 1'), findsOneWidget);
      expect(find.text('Relaxing background music for focus'), findsOneWidget);
      expect(find.text('Ready to Play'), findsNothing);
    });

    testWidgets('should have proper card styling', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: NowPlayingCard(isPlaying: false),
          ),
        ),
      );

      final card = tester.widget<Card>(find.byType(Card));
      expect(card.elevation, equals(4));
    });

    testWidgets('should have proper padding', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: NowPlayingCard(isPlaying: false),
          ),
        ),
      );

      final padding = tester.widget<Padding>(find.byType(Padding).first);
      expect(padding.padding, isA<EdgeInsets>());
    });
  });
} 