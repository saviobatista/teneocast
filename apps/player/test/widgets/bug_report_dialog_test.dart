import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/presentation/widgets/bug_report_dialog.dart';

void main() {
  group('BugReportDialog Widget Tests', () {
    testWidgets('should display dialog title', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const BugReportDialog(),
                ),
                child: const Text('Report Bug'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Report Bug'));
      await tester.pumpAndSettle();

      expect(find.text('Report Issue'), findsOneWidget);
    });

    testWidgets('should display description text', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const BugReportDialog(),
                ),
                child: const Text('Report Bug'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Report Bug'));
      await tester.pumpAndSettle();

      expect(find.text('Experiencing issues with TeneoCast Player?'), findsOneWidget);
    });

    testWidgets('should display text field', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const BugReportDialog(),
                ),
                child: const Text('Report Bug'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Report Bug'));
      await tester.pumpAndSettle();

      expect(find.byType(TextField), findsOneWidget);
      expect(find.text('Describe the issue'), findsOneWidget);
    });

    testWidgets('should display action buttons', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const BugReportDialog(),
                ),
                child: const Text('Report Bug'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Report Bug'));
      await tester.pumpAndSettle();

      expect(find.text('Cancel'), findsOneWidget);
      expect(find.text('Submit'), findsOneWidget);
    });

    testWidgets('should close dialog when cancel is tapped', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const BugReportDialog(),
                ),
                child: const Text('Report Bug'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Report Bug'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Cancel'));
      await tester.pumpAndSettle();

      expect(find.text('Report Issue'), findsNothing);
    });

    testWidgets('should show snackbar when submit is tapped', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (context) => const BugReportDialog(),
                ),
                child: const Text('Report Bug'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Report Bug'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Submit'));
      await tester.pumpAndSettle();

      expect(find.text('Bug report submitted. Thank you!'), findsOneWidget);
    });
  });
} 