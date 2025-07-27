import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_console/features/dashboard/domain/models/tenant.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/tenant_card.dart';

void main() {
  group('TenantCard', () {
    late Tenant mockTenant;

    setUp(() {
      mockTenant = Tenant(
        id: '1',
        name: 'Test Tenant',
        subdomain: 'test-tenant',
        status: 'active',
        createdAt: DateTime(2024, 1, 1),
        lastActiveAt: DateTime.now().subtract(const Duration(hours: 2)),
        stats: const TenantStats(
          connectedClients: 5,
          connectedUsers: 3,
          totalFiles: 100,
          musicFiles: 70,
          adFiles: 20,
          ttsFiles: 10,
          storageUsedGB: 5.0,
          storageLimitGB: 10.0,
        ),
        preferences: const {'theme': 'default'},
      );
    });

    testWidgets('displays tenant name and subdomain correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: mockTenant),
          ),
        ),
      );

      expect(find.text('Test Tenant'), findsOneWidget);
      expect(find.text('test-tenant'), findsOneWidget);
    });

    testWidgets('displays active status correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: mockTenant),
          ),
        ),
      );

      expect(find.text('ACTIVE'), findsOneWidget);
    });

    testWidgets('displays inactive status correctly', (WidgetTester tester) async {
      final inactiveTenant = mockTenant.copyWith(status: 'inactive');
      
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: inactiveTenant),
          ),
        ),
      );

      expect(find.text('INACTIVE'), findsOneWidget);
    });

    testWidgets('displays stats correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: mockTenant),
          ),
        ),
      );

      expect(find.text('5'), findsOneWidget); // connectedClients
      expect(find.text('3'), findsOneWidget); // connectedUsers
      expect(find.text('100'), findsOneWidget); // totalFiles
      expect(find.text('Connected Clients'), findsOneWidget);
      expect(find.text('Connected Users'), findsOneWidget);
      expect(find.text('Total Files'), findsOneWidget);
    });

    testWidgets('displays storage information correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: mockTenant),
          ),
        ),
      );

      expect(find.text('Storage Usage'), findsOneWidget);
      expect(find.text('5.0 GB / 10.0 GB'), findsOneWidget);
      expect(find.byType(LinearProgressIndicator), findsOneWidget);
    });

    testWidgets('displays file breakdown correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: mockTenant),
          ),
        ),
      );

      expect(find.text('Music'), findsOneWidget);
      expect(find.text('Ads'), findsOneWidget);
      expect(find.text('TTS'), findsOneWidget);
      expect(find.text('70'), findsOneWidget); // musicFiles
      expect(find.text('20'), findsOneWidget); // adFiles
      expect(find.text('10'), findsOneWidget); // ttsFiles
    });

    testWidgets('displays last activity time', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: mockTenant),
          ),
        ),
      );

      expect(find.textContaining('Last active:'), findsOneWidget);
    });

    testWidgets('handles null lastActiveAt', (WidgetTester tester) async {
      final tenantWithNullLastActive = mockTenant.copyWith(lastActiveAt: null);
      
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: tenantWithNullLastActive),
          ),
        ),
      );

      expect(find.textContaining('Last active:'), findsOneWidget);
    });

    testWidgets('displays correct storage percentage color', (WidgetTester tester) async {
      // Test with high storage usage (should show red)
      final highUsageTenant = mockTenant.copyWith(
        stats: const TenantStats(
          connectedClients: 5,
          connectedUsers: 3,
          totalFiles: 100,
          musicFiles: 70,
          adFiles: 20,
          ttsFiles: 10,
          storageUsedGB: 9.0, // 90% usage
          storageLimitGB: 10.0,
        ),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TenantCard(tenant: highUsageTenant),
          ),
        ),
      );

      expect(find.byType(LinearProgressIndicator), findsOneWidget);
    });
  });
} 