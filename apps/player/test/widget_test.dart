// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/app.dart';

void main() {
  group('TeneoCast Player App Tests', () {
    testWidgets('should display app title', (WidgetTester tester) async {
      // Build our app and trigger a frame.
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Verify that the app title is displayed
      expect(find.text('TeneoCast Player'), findsOneWidget);
    });

    testWidgets('should display status indicators', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Verify that status indicators are displayed
      expect(find.text('Online'), findsOneWidget);
      expect(find.text('Connected'), findsOneWidget);
      expect(find.text('Synced'), findsOneWidget);
      expect(find.text('Updated'), findsOneWidget);
    });

    testWidgets('should display now playing card', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Verify that the now playing card is displayed
      expect(find.text('Now Playing'), findsOneWidget);
      expect(find.text('Ready to Play'), findsOneWidget);
    });

    testWidgets('should display player controls', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Verify that player controls are displayed
      expect(find.byIcon(Icons.play_arrow), findsOneWidget);
      expect(find.byIcon(Icons.skip_previous), findsOneWidget);
      expect(find.byIcon(Icons.skip_next), findsOneWidget);
    });

    testWidgets('should display visual equalizer', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Verify that the visual equalizer is displayed
      expect(find.text('Audio Levels'), findsOneWidget);
    });

    testWidgets('should display play history', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Verify that play history is displayed
      expect(find.text('Recently Played'), findsOneWidget);
      expect(find.text('Morning Jazz Mix'), findsOneWidget);
      expect(find.text('Classical Focus'), findsOneWidget);
    });

    testWidgets('should toggle play/pause when button is tapped', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Initially should show play button
      expect(find.byIcon(Icons.play_arrow), findsOneWidget);
      expect(find.byIcon(Icons.pause), findsNothing);

      // Tap the play button
      await tester.tap(find.byIcon(Icons.play_arrow));
      await tester.pump();

      // Should now show pause button
      expect(find.byIcon(Icons.pause), findsOneWidget);
      expect(find.byIcon(Icons.play_arrow), findsNothing);
    });

    testWidgets('should display help dialog when help button is tapped', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Tap the help button
      await tester.tap(find.byIcon(Icons.help_outline));
      await tester.pumpAndSettle();

      // Should display help dialog
      expect(find.text('Help & Support'), findsOneWidget);
      expect(find.text('TeneoCast Player Help'), findsOneWidget);
    });

    testWidgets('should display bug report dialog when bug report button is tapped', (WidgetTester tester) async {
      await tester.pumpWidget(const TeneoCastPlayerApp());

      // Tap the bug report button
      await tester.tap(find.byIcon(Icons.bug_report_outlined));
      await tester.pumpAndSettle();

      // Should display bug report dialog
      expect(find.text('Report Issue'), findsOneWidget);
      expect(find.text('Experiencing issues with TeneoCast Player?'), findsOneWidget);
    });
  });
}
