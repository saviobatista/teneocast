import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_console/features/dashboard/domain/models/tenant.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/impersonation_dropdown.dart';

void main() {
  group('ImpersonationDropdown', () {
    late List<Tenant> mockTenants;

    setUp(() {
      mockTenants = [
        Tenant(
          id: '1',
          name: 'Test Tenant 1',
          subdomain: 'test-tenant-1',
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
        ),
        Tenant(
          id: '2',
          name: 'Test Tenant 2',
          subdomain: 'test-tenant-2',
          status: 'inactive',
          createdAt: DateTime(2024, 1, 2),
          lastActiveAt: DateTime.now().subtract(const Duration(days: 1)),
          stats: const TenantStats(
            connectedClients: 0,
            connectedUsers: 0,
            totalFiles: 50,
            musicFiles: 40,
            adFiles: 8,
            ttsFiles: 2,
            storageUsedGB: 3.0,
            storageLimitGB: 10.0,
          ),
          preferences: const {'theme': 'dark'},
        ),
      ];
    });

    testWidgets('displays title correctly', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: mockTenants,
              onTenantSelected: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('Impersonate Tenant'), findsOneWidget);
    });

    testWidgets('displays dropdown with admin option and tenants', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: mockTenants,
              onTenantSelected: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('Admin Console'), findsOneWidget);
      
      // Open dropdown to see tenant options
      await tester.tap(find.byType(DropdownButton<String>));
      await tester.pumpAndSettle();
      
      expect(find.text('Test Tenant 1'), findsOneWidget);
      expect(find.text('Test Tenant 2'), findsOneWidget);
    });

    testWidgets('shows tenant status indicators', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: mockTenants,
              onTenantSelected: (_) {},
            ),
          ),
        ),
      );

      // Open dropdown to see tenant options
      await tester.tap(find.byType(DropdownButton<String>));
      await tester.pumpAndSettle();

      // Should show client count for each tenant
      expect(find.text('5 clients'), findsOneWidget);
      expect(find.text('0 clients'), findsOneWidget);
    });

    testWidgets('calls onTenantSelected when tenant is selected', (WidgetTester tester) async {
      String? selectedTenantId;
      
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: mockTenants,
              onTenantSelected: (tenantId) {
                selectedTenantId = tenantId;
              },
            ),
          ),
        ),
      );

      // Tap on the dropdown to open it
      await tester.tap(find.byType(DropdownButton<String>));
      await tester.pumpAndSettle();

      // Tap on the first tenant option
      await tester.tap(find.text('Test Tenant 1').last);
      await tester.pumpAndSettle();

      expect(selectedTenantId, equals('1'));
    });

    testWidgets('shows impersonating message when tenant is selected', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: mockTenants,
              selectedTenantId: '1',
              onTenantSelected: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('Impersonating: Test Tenant 1'), findsOneWidget);
      expect(find.text('Exit'), findsOneWidget);
    });

    testWidgets('calls onTenantSelected with empty string when exit is pressed', (WidgetTester tester) async {
      String? selectedTenantId;
      
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: mockTenants,
              selectedTenantId: '1',
              onTenantSelected: (tenantId) {
                selectedTenantId = tenantId;
              },
            ),
          ),
        ),
      );

      await tester.tap(find.text('Exit'));
      await tester.pumpAndSettle();

      expect(selectedTenantId, equals(''));
    });

    testWidgets('handles empty tenants list', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: const [],
              onTenantSelected: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('Impersonate Tenant'), findsOneWidget);
      expect(find.text('Admin Console'), findsOneWidget);
    });

    testWidgets('displays tenant subdomain in dropdown', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ImpersonationDropdown(
              tenants: mockTenants,
              onTenantSelected: (_) {},
            ),
          ),
        ),
      );

      // Open dropdown to see tenant options
      await tester.tap(find.byType(DropdownButton<String>));
      await tester.pumpAndSettle();

      expect(find.text('test-tenant-1'), findsOneWidget);
      expect(find.text('test-tenant-2'), findsOneWidget);
    });
  });
} 