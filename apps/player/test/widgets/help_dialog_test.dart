import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/presentation/widgets/help_dialog.dart';

void main() {
  group('HelpDialog Widget Tests', () {
    testWidgets('should display dialog title', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const HelpDialog(),
                ),
                child: const Text('Show Help'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Show Help'));
      await tester.pumpAndSettle();

      expect(find.text('Help & Support'), findsOneWidget);
    });

    testWidgets('should display help content', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const HelpDialog(),
                ),
                child: const Text('Show Help'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Show Help'));
      await tester.pumpAndSettle();

      expect(find.text('TeneoCast Player Help'), findsOneWidget);
      expect(find.text('• Use play/pause controls to manage audio'), findsOneWidget);
      expect(find.text('• Monitor connection status in the top bar'), findsOneWidget);
      expect(find.text('• View recently played content in history'), findsOneWidget);
      expect(find.text('• Visual equalizer shows current audio levels'), findsOneWidget);
    });

    testWidgets('should display close button', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const HelpDialog(),
                ),
                child: const Text('Show Help'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Show Help'));
      await tester.pumpAndSettle();

      expect(find.text('Close'), findsOneWidget);
    });

    testWidgets('should close dialog when close button is tapped', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const HelpDialog(),
                ),
                child: const Text('Show Help'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Show Help'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Close'));
      await tester.pumpAndSettle();

      expect(find.text('Help & Support'), findsNothing);
    });
  });
} 