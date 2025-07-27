import 'package:equatable/equatable.dart';

class DashboardStats extends Equatable {
  final int totalTenants;
  final int activeTenants;
  final int totalConnectedClients;
  final int totalConnectedUsers;
  final int totalFiles;
  final double totalStorageUsedGB;
  final double totalStorageLimitGB;
  final List<ChartDataPoint> dailyActiveTenants;
  final List<ChartDataPoint> dailyConnectedClients;
  final List<ChartDataPoint> dailyFileUploads;

  const DashboardStats({
    required this.totalTenants,
    required this.activeTenants,
    required this.totalConnectedClients,
    required this.totalConnectedUsers,
    required this.totalFiles,
    required this.totalStorageUsedGB,
    required this.totalStorageLimitGB,
    required this.dailyActiveTenants,
    required this.dailyConnectedClients,
    required this.dailyFileUploads,
  });

  factory DashboardStats.fromJson(Map<String, dynamic> json) {
    return DashboardStats(
      totalTenants: json['totalTenants'] as int? ?? 0,
      activeTenants: json['activeTenants'] as int? ?? 0,
      totalConnectedClients: json['totalConnectedClients'] as int? ?? 0,
      totalConnectedUsers: json['totalConnectedUsers'] as int? ?? 0,
      totalFiles: json['totalFiles'] as int? ?? 0,
      totalStorageUsedGB: (json['totalStorageUsedGB'] as num?)?.toDouble() ?? 0.0,
      totalStorageLimitGB: (json['totalStorageLimitGB'] as num?)?.toDouble() ?? 0.0,
      dailyActiveTenants: (json['dailyActiveTenants'] as List<dynamic>?)
          ?.map((e) => ChartDataPoint.fromJson(e as Map<String, dynamic>))
          .toList() ?? [],
      dailyConnectedClients: (json['dailyConnectedClients'] as List<dynamic>?)
          ?.map((e) => ChartDataPoint.fromJson(e as Map<String, dynamic>))
          .toList() ?? [],
      dailyFileUploads: (json['dailyFileUploads'] as List<dynamic>?)
          ?.map((e) => ChartDataPoint.fromJson(e as Map<String, dynamic>))
          .toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'totalTenants': totalTenants,
      'activeTenants': activeTenants,
      'totalConnectedClients': totalConnectedClients,
      'totalConnectedUsers': totalConnectedUsers,
      'totalFiles': totalFiles,
      'totalStorageUsedGB': totalStorageUsedGB,
      'totalStorageLimitGB': totalStorageLimitGB,
      'dailyActiveTenants': dailyActiveTenants.map((e) => e.toJson()).toList(),
      'dailyConnectedClients': dailyConnectedClients.map((e) => e.toJson()).toList(),
      'dailyFileUploads': dailyFileUploads.map((e) => e.toJson()).toList(),
    };
  }

  double get storageUsagePercentage => 
      totalStorageLimitGB > 0 ? (totalStorageUsedGB / totalStorageLimitGB) * 100 : 0.0;

  double get activeTenantsPercentage => 
      totalTenants > 0 ? (activeTenants / totalTenants) * 100 : 0.0;

  @override
  List<Object?> get props => [
    totalTenants,
    activeTenants,
    totalConnectedClients,
    totalConnectedUsers,
    totalFiles,
    totalStorageUsedGB,
    totalStorageLimitGB,
    dailyActiveTenants,
    dailyConnectedClients,
    dailyFileUploads,
  ];
}

class ChartDataPoint extends Equatable {
  final DateTime date;
  final double value;

  const ChartDataPoint({
    required this.date,
    required this.value,
  });

  factory ChartDataPoint.fromJson(Map<String, dynamic> json) {
    return ChartDataPoint(
      date: DateTime.parse(json['date'] as String),
      value: (json['value'] as num).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'date': date.toIso8601String(),
      'value': value,
    };
  }

  @override
  List<Object?> get props => [date, value];
} 