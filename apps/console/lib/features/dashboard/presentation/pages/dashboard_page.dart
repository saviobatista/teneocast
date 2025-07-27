import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:teneocast_console/core/theme/app_theme.dart';
import 'package:teneocast_console/features/dashboard/domain/models/tenant.dart';
import 'package:teneocast_console/features/dashboard/domain/models/dashboard_stats.dart';
import 'package:teneocast_console/features/dashboard/presentation/bloc/dashboard_bloc.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/stats_card.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/tenant_card.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/impersonation_dropdown.dart';
import 'package:teneocast_console/features/dashboard/presentation/widgets/chart_widget.dart';

class DashboardPage extends StatelessWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => DashboardBloc()..add(const LoadDashboardData()),
      child: const DashboardView(),
    );
  }
}

class DashboardView extends StatelessWidget {
  const DashboardView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('TeneoCast Console'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              context.read<DashboardBloc>().add(const RefreshDashboardData());
            },
            tooltip: 'Refresh Data',
          ),
          const SizedBox(width: 16),
        ],
      ),
      body: BlocBuilder<DashboardBloc, DashboardState>(
        builder: (context, state) {
          if (state is DashboardLoading) {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }

          if (state is DashboardError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(
                    Icons.error_outline,
                    size: 64,
                    color: Colors.red,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'Error loading dashboard',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    state.message,
                    style: Theme.of(context).textTheme.bodyMedium,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      context.read<DashboardBloc>().add(const LoadDashboardData());
                    },
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          if (state is DashboardLoaded) {
            return _buildDashboardContent(context, state);
          }

          return const SizedBox.shrink();
        },
      ),
    );
  }

  Widget _buildDashboardContent(BuildContext context, DashboardLoaded state) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header with impersonation dropdown
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Platform Overview',
                      style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: AppTheme.primaryNavy,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Monitor and manage your TeneoCast platform',
                      style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        color: AppTheme.neutral600,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 16),
              ImpersonationDropdown(
                tenants: state.tenants,
                selectedTenantId: state.selectedTenantId,
                onTenantSelected: (tenantId) {
                  context.read<DashboardBloc>().add(
                    SelectTenantForImpersonation(tenantId),
                  );
                },
              ),
            ],
          ),
          const SizedBox(height: 32),

          // Statistics Cards
          _buildStatsSection(context, state.stats),
          const SizedBox(height: 32),

          // Charts Section
          _buildChartsSection(context, state.stats),
          const SizedBox(height: 32),

          // Tenants Section
          _buildTenantsSection(context, state.tenants),
        ],
      ),
    );
  }

  Widget _buildStatsSection(BuildContext context, DashboardStats stats) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Platform Statistics',
          style: Theme.of(context).textTheme.headlineSmall?.copyWith(
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 16),
        GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: _getCrossAxisCount(context),
          crossAxisSpacing: 16,
          mainAxisSpacing: 16,
          childAspectRatio: 1.5,
          children: [
            StatsCard(
              title: 'Total Tenants',
              value: stats.totalTenants.toString(),
              subtitle: '${stats.activeTenants} active',
              icon: Icons.business,
              color: AppTheme.primaryNavy,
              percentage: stats.activeTenantsPercentage,
            ),
            StatsCard(
              title: 'Connected Clients',
              value: stats.totalConnectedClients.toString(),
              subtitle: '${stats.totalConnectedUsers} users',
              icon: Icons.devices,
              color: AppTheme.accentLime,
            ),
            StatsCard(
              title: 'Total Files',
              value: stats.totalFiles.toString(),
              subtitle: '${stats.totalStorageUsedGB.toStringAsFixed(1)} GB used',
              icon: Icons.folder,
              color: AppTheme.accentViolet,
              percentage: stats.storageUsagePercentage,
            ),
            StatsCard(
              title: 'Storage Usage',
              value: '${stats.storageUsagePercentage.toStringAsFixed(1)}%',
              subtitle: '${stats.totalStorageUsedGB.toStringAsFixed(1)} / ${stats.totalStorageLimitGB.toStringAsFixed(1)} GB',
              icon: Icons.storage,
              color: AppTheme.accentCoral,
              percentage: stats.storageUsagePercentage,
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildChartsSection(BuildContext context, DashboardStats stats) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Analytics',
          style: Theme.of(context).textTheme.headlineSmall?.copyWith(
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 16),
        GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: _getCrossAxisCount(context),
          crossAxisSpacing: 16,
          mainAxisSpacing: 16,
          childAspectRatio: 1.8,
          children: [
            ChartWidget(
              title: 'Active Tenants',
              data: stats.dailyActiveTenants,
              color: AppTheme.primaryNavy,
            ),
            ChartWidget(
              title: 'Connected Clients',
              data: stats.dailyConnectedClients,
              color: AppTheme.accentLime,
            ),
            ChartWidget(
              title: 'File Uploads',
              data: stats.dailyFileUploads,
              color: AppTheme.accentViolet,
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildTenantsSection(BuildContext context, List<Tenant> tenants) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(
              child: Text(
                'Tenants',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            Text(
              '${tenants.length} total',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppTheme.neutral500,
              ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: _getCrossAxisCount(context),
            crossAxisSpacing: 16,
            mainAxisSpacing: 16,
            childAspectRatio: 1.2,
          ),
          itemCount: tenants.length,
          itemBuilder: (context, index) {
            return TenantCard(tenant: tenants[index]);
          },
        ),
      ],
    );
  }

  int _getCrossAxisCount(BuildContext context) {
    final width = MediaQuery.of(context).size.width;
    if (width > 1200) return 4;
    if (width > 800) return 3;
    if (width > 600) return 2;
    return 1;
  }
} 