import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_player/presentation/widgets/status_bar.dart';

void main() {
  group('StatusBar Widget Tests', () {
    testWidgets('should display platform name', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBar(
              isOnline: true,
              isConnected: true,
              isSynced: true,
              isUpToDate: true,
            ),
          ),
        ),
      );

      // Should display platform name (Web, Windows, Android, iOS, or Unknown)
      final platformTexts = ['Web', 'Windows', 'Android', 'iOS', 'Unknown'];
      bool foundPlatform = false;
      for (final platform in platformTexts) {
        if (find.text(platform).evaluate().isNotEmpty) {
          foundPlatform = true;
          break;
        }
      }
      expect(foundPlatform, isTrue);
    });

    testWidgets('should display all status indicators', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBar(
              isOnline: true,
              isConnected: true,
              isSynced: true,
              isUpToDate: true,
            ),
          ),
        ),
      );

      // Should display all status labels
      expect(find.text('Online'), findsOneWidget);
      expect(find.text('Connected'), findsOneWidget);
      expect(find.text('Synced'), findsOneWidget);
      expect(find.text('Updated'), findsOneWidget);
    });

    testWidgets('should show green indicators when status is active', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBar(
              isOnline: true,
              isConnected: true,
              isSynced: true,
              isUpToDate: true,
            ),
          ),
        ),
      );

      // Should have 4 green status indicators
      final containers = tester.widgetList<Container>(find.byType(Container));
      final greenContainers = containers.where((container) {
        final decoration = container.decoration as BoxDecoration?;
        return decoration?.color == Colors.green;
      });
      expect(greenContainers.length, equals(4));
    });

    testWidgets('should show grey indicators when status is inactive', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBar(
              isOnline: false,
              isConnected: false,
              isSynced: false,
              isUpToDate: false,
            ),
          ),
        ),
      );

      // Should have 4 grey status indicators
      final containers = tester.widgetList<Container>(find.byType(Container));
      final greyContainers = containers.where((container) {
        final decoration = container.decoration as BoxDecoration?;
        return decoration?.color == Colors.grey;
      });
      expect(greyContainers.length, equals(4));
    });

    testWidgets('should have proper styling', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: StatusBar(
              isOnline: true,
              isConnected: true,
              isSynced: true,
              isUpToDate: true,
            ),
          ),
        ),
      );

      // Should have proper container styling
      final container = tester.widget<Container>(find.byType(Container).first);
      expect(container.padding, equals(const EdgeInsets.symmetric(horizontal: 16, vertical: 8)));
      
      final decoration = container.decoration as BoxDecoration;
      expect(decoration.border, isNotNull);
    });
  });
} 