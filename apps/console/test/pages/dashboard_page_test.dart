import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:teneocast_console/features/dashboard/domain/models/tenant.dart';
import 'package:teneocast_console/features/dashboard/domain/models/dashboard_stats.dart';
import 'package:teneocast_console/features/dashboard/presentation/bloc/dashboard_bloc.dart';
import 'package:teneocast_console/features/dashboard/presentation/pages/dashboard_page.dart';

void main() {
  group('DashboardPage', () {
    late DashboardBloc mockBloc;
    late List<Tenant> mockTenants;
    late DashboardStats mockStats;

    setUp(() {
      mockBloc = DashboardBloc();
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

      mockStats = DashboardStats(
        totalTenants: 24,
        activeTenants: 18,
        totalConnectedClients: 156,
        totalConnectedUsers: 89,
        totalFiles: 1247,
        totalStorageUsedGB: 45.2,
        totalStorageLimitGB: 100.0,
        dailyActiveTenants: [
          ChartDataPoint(date: DateTime(2024, 1, 20), value: 15),
          ChartDataPoint(date: DateTime(2024, 1, 21), value: 18),
        ],
        dailyConnectedClients: [
          ChartDataPoint(date: DateTime(2024, 1, 20), value: 120),
          ChartDataPoint(date: DateTime(2024, 1, 21), value: 145),
        ],
        dailyFileUploads: [
          ChartDataPoint(date: DateTime(2024, 1, 20), value: 45),
          ChartDataPoint(date: DateTime(2024, 1, 21), value: 52),
        ],
      );
    });

    tearDown(() {
      mockBloc.close();
    });

    Widget createTestWidget() {
      return MaterialApp(
        home: BlocProvider<DashboardBloc>.value(
          value: mockBloc,
          child: const DashboardPage(),
        ),
      );
    }

    testWidgets('shows loading indicator initially', (WidgetTester tester) async {
      await tester.pumpWidget(createTestWidget());

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('shows error state when loading fails', (WidgetTester tester) async {
      // Emit error state
      mockBloc.emit(const DashboardError('Test error message'));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('Error loading dashboard'), findsOneWidget);
      expect(find.text('Test error message'), findsOneWidget);
      expect(find.text('Retry'), findsOneWidget);
    });

    testWidgets('shows dashboard content when data is loaded', (WidgetTester tester) async {
      // Emit loaded state
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('Platform Overview'), findsOneWidget);
      expect(find.text('Platform Statistics'), findsOneWidget);
      expect(find.text('Analytics'), findsOneWidget);
      expect(find.text('Tenants'), findsOneWidget);
    });

    testWidgets('displays stats cards correctly', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('Total Tenants'), findsOneWidget);
      expect(find.text('Connected Clients'), findsOneWidget);
      expect(find.text('Total Files'), findsOneWidget);
      expect(find.text('Storage Usage'), findsOneWidget);
      expect(find.text('24'), findsOneWidget); // totalTenants
      expect(find.text('156'), findsOneWidget); // totalConnectedClients
      expect(find.text('1247'), findsOneWidget); // totalFiles
    });

    testWidgets('displays tenant cards correctly', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('Test Tenant 1'), findsOneWidget);
      expect(find.text('Test Tenant 2'), findsOneWidget);
      expect(find.text('test-tenant-1'), findsOneWidget);
      expect(find.text('test-tenant-2'), findsOneWidget);
    });

    testWidgets('shows refresh button in app bar', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.byIcon(Icons.refresh), findsOneWidget);
    });

    testWidgets('calls refresh when refresh button is pressed', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      await tester.tap(find.byIcon(Icons.refresh));
      await tester.pumpAndSettle();

      // Should trigger refresh event
      expect(mockBloc.state, isA<DashboardLoaded>());
    });

    testWidgets('displays impersonation dropdown', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('Impersonate Tenant'), findsOneWidget);
    });

    testWidgets('handles tenant selection for impersonation', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // Tap on impersonation dropdown
      await tester.tap(find.byType(DropdownButton<String>));
      await tester.pumpAndSettle();

      // Should show tenant options
      expect(find.text('Test Tenant 1'), findsOneWidget);
      expect(find.text('Test Tenant 2'), findsOneWidget);
    });

    testWidgets('displays correct tenant count', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('2 total'), findsOneWidget); // 2 tenants in mock data
    });

    testWidgets('shows storage usage percentage', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('45.2%'), findsOneWidget); // 45.2/100.0 * 100
    });

    testWidgets('shows active tenants percentage', (WidgetTester tester) async {
      mockBloc.emit(DashboardLoaded(
        stats: mockStats,
        tenants: mockTenants,
      ));
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('75.0%'), findsOneWidget); // 18/24 * 100
    });
  });
} 