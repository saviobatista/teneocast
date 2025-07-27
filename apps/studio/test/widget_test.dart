import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:teneocast_studio/main.dart';
import 'package:teneocast_studio/features/dashboard/presentation/bloc/dashboard_bloc.dart';

void main() {
  group('TeneoCast Studio App', () {
    testWidgets('app starts without crashing', (tester) async {
      await tester.pumpWidget(const TeneoCastStudioApp());
      expect(find.byType(MaterialApp), findsOneWidget);
      
      // Wait for any async operations to complete
      await tester.pumpAndSettle();
    });

    testWidgets('dashboard loads with BLoC provider', (tester) async {
      await tester.pumpWidget(
        BlocProvider(
          create: (context) => DashboardBloc(),
          child: const MaterialApp(
            home: Scaffold(
              body: Center(
                child: Text('Dashboard Test'),
              ),
            ),
          ),
        ),
      );
      expect(find.text('Dashboard Test'), findsOneWidget);
      
      // Wait for any async operations to complete
      await tester.pumpAndSettle();
    });
  });
} 