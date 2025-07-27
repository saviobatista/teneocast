import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/stats_card.dart';

void main() {
  group('StatsCard', () {
    testWidgets('displays title, value, and subtitle correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Test Title',
              value: '42',
              subtitle: 'Test Subtitle',
              icon: Icons.star,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.text('Test Title'), findsOneWidget);
      expect(find.text('42'), findsOneWidget);
      expect(find.text('Test Subtitle'), findsOneWidget);
      expect(find.byIcon(Icons.star), findsOneWidget);
    });

    testWidgets('displays percentage indicator when percentage is provided', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Test Title',
              value: '75%',
              subtitle: 'Test Subtitle',
              icon: Icons.star,
              color: Colors.blue,
              percentage: 75.0,
            ),
          ),
        ),
      );

      expect(find.text('75.0%'), findsOneWidget);
      expect(find.byType(LinearProgressIndicator), findsOneWidget);
    });

    testWidgets('does not display percentage indicator when percentage is null', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Test Title',
              value: '42',
              subtitle: 'Test Subtitle',
              icon: Icons.star,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.byType(LinearProgressIndicator), findsNothing);
    });

    testWidgets('applies correct color to icon container', (WidgetTester tester) async {
      const testColor = Colors.red;
      
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Test Title',
              value: '42',
              subtitle: 'Test Subtitle',
              icon: Icons.star,
              color: testColor,
            ),
          ),
        ),
      );

      final iconContainers = tester.widgetList<Container>(
        find.descendant(
          of: find.byType(StatsCard),
          matching: find.byType(Container),
        ),
      );
      
      final iconContainer = iconContainers.firstWhere(
        (container) => container.decoration is BoxDecoration,
      );

      expect(iconContainer.decoration, isA<BoxDecoration>());
      final decoration = iconContainer.decoration as BoxDecoration;
      expect(decoration.color, testColor.withOpacity(0.1));
    });

    testWidgets('displays percentage color based on value', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Test Title',
              value: '90%',
              subtitle: 'Test Subtitle',
              icon: Icons.star,
              color: Colors.blue,
              percentage: 90.0,
            ),
          ),
        ),
      );

      // High percentage should show red color
      expect(find.text('90.0%'), findsOneWidget);
    });
  });
} 