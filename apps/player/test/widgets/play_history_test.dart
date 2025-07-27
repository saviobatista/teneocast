import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/presentation/widgets/play_history.dart';

void main() {
  group('PlayHistory Widget Tests', () {
    testWidgets('should display "Recently Played" title', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayHistory(),
          ),
        ),
      );

      expect(find.text('Recently Played'), findsOneWidget);
    });

    testWidgets('should display history items', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayHistory(),
          ),
        ),
      );

      // Should display all history items
      expect(find.text('Morning Jazz Mix'), findsOneWidget);
      expect(find.text('Classical Focus'), findsOneWidget);
      expect(find.text('Ambient Workspace'), findsOneWidget);
      expect(find.text('Electronic Chill'), findsOneWidget);
    });

    testWidgets('should display time information', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayHistory(),
          ),
        ),
      );

      // Should display time information
      expect(find.text('2 hours ago'), findsOneWidget);
      expect(find.text('4 hours ago'), findsOneWidget);
      expect(find.text('6 hours ago'), findsOneWidget);
      expect(find.text('Yesterday'), findsOneWidget);
    });

    testWidgets('should display play icons', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayHistory(),
          ),
        ),
      );

      // Should have 4 play icons (one for each history item)
      expect(find.byIcon(Icons.play_circle_outline), findsNWidgets(4));
    });

    testWidgets('should have proper card styling', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayHistory(),
          ),
        ),
      );

      final card = tester.widget<Card>(find.byType(Card));
      expect(card, isNotNull);
    });

    testWidgets('should have proper padding', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: PlayHistory(),
          ),
        ),
      );

      final padding = tester.widget<Padding>(find.byType(Padding).first);
      expect(padding.padding, isA<EdgeInsets>());
    });
  });
} 