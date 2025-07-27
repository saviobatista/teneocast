import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:teneocast_console/features/dashboard/domain/models/tenant.dart';
import 'package:teneocast_console/features/dashboard/domain/models/dashboard_stats.dart';

// Events
abstract class DashboardEvent extends Equatable {
  const DashboardEvent();

  @override
  List<Object?> get props => [];
}

class LoadDashboardData extends DashboardEvent {
  const LoadDashboardData();
}

class RefreshDashboardData extends DashboardEvent {
  const RefreshDashboardData();
}

class SelectTenantForImpersonation extends DashboardEvent {
  final String tenantId;

  const SelectTenantForImpersonation(this.tenantId);

  @override
  List<Object?> get props => [tenantId];
}

// States
abstract class DashboardState extends Equatable {
  const DashboardState();

  @override
  List<Object?> get props => [];
}

class DashboardInitial extends DashboardState {}

class DashboardLoading extends DashboardState {}

class DashboardLoaded extends DashboardState {
  final DashboardStats stats;
  final List<Tenant> tenants;
  final String? selectedTenantId;

  const DashboardLoaded({
    required this.stats,
    required this.tenants,
    this.selectedTenantId,
  });

  @override
  List<Object?> get props => [stats, tenants, selectedTenantId];

  DashboardLoaded copyWith({
    DashboardStats? stats,
    List<Tenant>? tenants,
    String? selectedTenantId,
  }) {
    return DashboardLoaded(
      stats: stats ?? this.stats,
      tenants: tenants ?? this.tenants,
      selectedTenantId: selectedTenantId ?? this.selectedTenantId,
    );
  }
}

class DashboardError extends DashboardState {
  final String message;

  const DashboardError(this.message);

  @override
  List<Object?> get props => [message];
}

// BLoC
class DashboardBloc extends Bloc<DashboardEvent, DashboardState> {
  DashboardBloc() : super(DashboardInitial()) {
    on<LoadDashboardData>(_onLoadDashboardData);
    on<RefreshDashboardData>(_onRefreshDashboardData);
    on<SelectTenantForImpersonation>(_onSelectTenantForImpersonation);
  }

  Future<void> _onLoadDashboardData(
    LoadDashboardData event,
    Emitter<DashboardState> emit,
  ) async {
    emit(DashboardLoading());

    try {
      // Simulate API call delay
      await Future.delayed(const Duration(milliseconds: 800));

      // Mock data for development
      final stats = _generateMockStats();
      final tenants = _generateMockTenants();

      emit(DashboardLoaded(
        stats: stats,
        tenants: tenants,
      ));
    } catch (e) {
      emit(DashboardError('Failed to load dashboard data: $e'));
    }
  }

  Future<void> _onRefreshDashboardData(
    RefreshDashboardData event,
    Emitter<DashboardState> emit,
  ) async {
    if (state is DashboardLoaded) {
      final currentState = state as DashboardLoaded;
      emit(DashboardLoaded(
        stats: currentState.stats,
        tenants: currentState.tenants,
        selectedTenantId: currentState.selectedTenantId,
      ));
    }

    add(const LoadDashboardData());
  }

  void _onSelectTenantForImpersonation(
    SelectTenantForImpersonation event,
    Emitter<DashboardState> emit,
  ) {
    if (state is DashboardLoaded) {
      final currentState = state as DashboardLoaded;
      emit(currentState.copyWith(
        selectedTenantId: event.tenantId,
      ));
    }
  }

  // Mock data generators
  DashboardStats _generateMockStats() {
    return DashboardStats(
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
        ChartDataPoint(date: DateTime(2024, 1, 22), value: 16),
        ChartDataPoint(date: DateTime(2024, 1, 23), value: 20),
        ChartDataPoint(date: DateTime(2024, 1, 24), value: 18),
        ChartDataPoint(date: DateTime(2024, 1, 25), value: 22),
        ChartDataPoint(date: DateTime(2024, 1, 26), value: 19),
      ],
      dailyConnectedClients: [
        ChartDataPoint(date: DateTime(2024, 1, 20), value: 120),
        ChartDataPoint(date: DateTime(2024, 1, 21), value: 145),
        ChartDataPoint(date: DateTime(2024, 1, 22), value: 138),
        ChartDataPoint(date: DateTime(2024, 1, 23), value: 162),
        ChartDataPoint(date: DateTime(2024, 1, 24), value: 156),
        ChartDataPoint(date: DateTime(2024, 1, 25), value: 178),
        ChartDataPoint(date: DateTime(2024, 1, 26), value: 156),
      ],
      dailyFileUploads: [
        ChartDataPoint(date: DateTime(2024, 1, 20), value: 45),
        ChartDataPoint(date: DateTime(2024, 1, 21), value: 52),
        ChartDataPoint(date: DateTime(2024, 1, 22), value: 38),
        ChartDataPoint(date: DateTime(2024, 1, 23), value: 67),
        ChartDataPoint(date: DateTime(2024, 1, 24), value: 58),
        ChartDataPoint(date: DateTime(2024, 1, 25), value: 73),
        ChartDataPoint(date: DateTime(2024, 1, 26), value: 61),
      ],
    );
  }

  List<Tenant> _generateMockTenants() {
    return [
      Tenant(
        id: '1',
        name: 'Café Central',
        subdomain: 'cafe-central',
        status: 'active',
        createdAt: DateTime(2024, 1, 15),
        lastActiveAt: DateTime.now().subtract(const Duration(hours: 2)),
        stats: const TenantStats(
          connectedClients: 12,
          connectedUsers: 8,
          totalFiles: 156,
          musicFiles: 120,
          adFiles: 25,
          ttsFiles: 11,
          storageUsedGB: 8.5,
          storageLimitGB: 10.0,
        ),
        preferences: const {'theme': 'warm', 'volume': 0.8},
      ),
      Tenant(
        id: '2',
        name: 'Gym Fitness Pro',
        subdomain: 'gym-fitness',
        status: 'active',
        createdAt: DateTime(2024, 1, 10),
        lastActiveAt: DateTime.now().subtract(const Duration(minutes: 30)),
        stats: const TenantStats(
          connectedClients: 8,
          connectedUsers: 5,
          totalFiles: 89,
          musicFiles: 65,
          adFiles: 18,
          ttsFiles: 6,
          storageUsedGB: 6.2,
          storageLimitGB: 10.0,
        ),
        preferences: const {'theme': 'energetic', 'volume': 0.9},
      ),
      Tenant(
        id: '3',
        name: 'Retail Store Plus',
        subdomain: 'retail-store',
        status: 'active',
        createdAt: DateTime(2024, 1, 8),
        lastActiveAt: DateTime.now().subtract(const Duration(hours: 1)),
        stats: const TenantStats(
          connectedClients: 15,
          connectedUsers: 12,
          totalFiles: 234,
          musicFiles: 180,
          adFiles: 42,
          ttsFiles: 12,
          storageUsedGB: 12.8,
          storageLimitGB: 15.0,
        ),
        preferences: const {'theme': 'professional', 'volume': 0.7},
      ),
      Tenant(
        id: '4',
        name: 'Restaurant Bella',
        subdomain: 'restaurant-bella',
        status: 'inactive',
        createdAt: DateTime(2024, 1, 5),
        lastActiveAt: DateTime.now().subtract(const Duration(days: 3)),
        stats: const TenantStats(
          connectedClients: 0,
          connectedUsers: 0,
          totalFiles: 67,
          musicFiles: 50,
          adFiles: 12,
          ttsFiles: 5,
          storageUsedGB: 4.1,
          storageLimitGB: 10.0,
        ),
        preferences: const {'theme': 'elegant', 'volume': 0.6},
      ),
      Tenant(
        id: '5',
        name: 'Office Space Co',
        subdomain: 'office-space',
        status: 'active',
        createdAt: DateTime(2024, 1, 12),
        lastActiveAt: DateTime.now().subtract(const Duration(minutes: 15)),
        stats: const TenantStats(
          connectedClients: 6,
          connectedUsers: 4,
          totalFiles: 78,
          musicFiles: 60,
          adFiles: 15,
          ttsFiles: 3,
          storageUsedGB: 5.3,
          storageLimitGB: 10.0,
        ),
        preferences: const {'theme': 'corporate', 'volume': 0.5},
      ),
    ];
  }
} 