import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:teneocast_console/features/dashboard/domain/models/dashboard_stats.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/chart_widget.dart';

void main() {
  group('ChartWidget', () {
    late List<ChartDataPoint> mockData;

    setUp(() {
      mockData = [
        ChartDataPoint(date: DateTime(2024, 1, 20), value: 15),
        ChartDataPoint(date: DateTime(2024, 1, 21), value: 18),
        ChartDataPoint(date: DateTime(2024, 1, 22), value: 16),
        ChartDataPoint(date: DateTime(2024, 1, 23), value: 20),
      ];
    });

    testWidgets('displays title correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: mockData,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.text('Test Chart'), findsOneWidget);
    });

    testWidgets('displays chart when data is provided', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: mockData,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.byType(LineChart), findsOneWidget);
    });

    testWidgets('displays no data message when data is empty', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: [],
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.text('No data available'), findsOneWidget);
      expect(find.byType(LineChart), findsNothing);
    });

    testWidgets('handles single data point', (WidgetTester tester) async {
      final singleDataPoint = [
        ChartDataPoint(date: DateTime(2024, 1, 20), value: 15),
      ];

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: singleDataPoint,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.byType(LineChart), findsOneWidget);
    });

    testWidgets('applies correct color to chart', (WidgetTester tester) async {
      const testColor = Colors.red;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: mockData,
              color: testColor,
            ),
          ),
        ),
      );

      expect(find.byType(LineChart), findsOneWidget);
    });

    testWidgets('displays date labels on x-axis', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: mockData,
              color: Colors.blue,
            ),
          ),
        ),
      );

      // Should display month/day format for dates
      expect(find.text('1/20'), findsOneWidget);
      expect(find.text('1/21'), findsOneWidget);
      expect(find.text('1/22'), findsOneWidget);
      expect(find.text('1/23'), findsOneWidget);
    });

    testWidgets('handles large data sets', (WidgetTester tester) async {
      final largeDataSet = <ChartDataPoint>[];

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: largeDataSet,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.byType(LineChart), findsOneWidget);
    });

    testWidgets('handles zero values in data', (WidgetTester tester) async {
      final dataWithZeros = [
        ChartDataPoint(date: DateTime(2024, 1, 20), value: 0),
        ChartDataPoint(date: DateTime(2024, 1, 21), value: 5),
        ChartDataPoint(date: DateTime(2024, 1, 22), value: 0),
        ChartDataPoint(date: DateTime(2024, 1, 23), value: 10),
      ];

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: dataWithZeros,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.byType(LineChart), findsOneWidget);
    });

    testWidgets('handles negative values in data', (WidgetTester tester) async {
      final dataWithNegatives = [
        ChartDataPoint(date: DateTime(2024, 1, 20), value: -5),
        ChartDataPoint(date: DateTime(2024, 1, 21), value: 0),
        ChartDataPoint(date: DateTime(2024, 1, 22), value: 5),
        ChartDataPoint(date: DateTime(2024, 1, 23), value: 10),
      ];

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ChartWidget(
              title: 'Test Chart',
              data: dataWithNegatives,
              color: Colors.blue,
            ),
          ),
        ),
      );

      expect(find.byType(LineChart), findsOneWidget);
    });
  });
} 