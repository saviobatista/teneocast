import 'package:equatable/equatable.dart';

class Tenant extends Equatable {
  final String id;
  final String name;
  final String subdomain;
  final String status;
  final DateTime createdAt;
  final DateTime? lastActiveAt;
  final TenantStats stats;
  final Map<String, dynamic> preferences;

  const Tenant({
    required this.id,
    required this.name,
    required this.subdomain,
    required this.status,
    required this.createdAt,
    this.lastActiveAt,
    required this.stats,
    required this.preferences,
  });

  factory Tenant.fromJson(Map<String, dynamic> json) {
    return Tenant(
      id: json['id'] as String,
      name: json['name'] as String,
      subdomain: json['subdomain'] as String,
      status: json['status'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
      lastActiveAt: json['lastActiveAt'] != null 
          ? DateTime.parse(json['lastActiveAt'] as String)
          : null,
      stats: TenantStats.fromJson(json['stats'] as Map<String, dynamic>),
      preferences: json['preferences'] as Map<String, dynamic>? ?? {},
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'subdomain': subdomain,
      'status': status,
      'createdAt': createdAt.toIso8601String(),
      'lastActiveAt': lastActiveAt?.toIso8601String(),
      'stats': stats.toJson(),
      'preferences': preferences,
    };
  }

  Tenant copyWith({
    String? id,
    String? name,
    String? subdomain,
    String? status,
    DateTime? createdAt,
    DateTime? lastActiveAt,
    TenantStats? stats,
    Map<String, dynamic>? preferences,
  }) {
    return Tenant(
      id: id ?? this.id,
      name: name ?? this.name,
      subdomain: subdomain ?? this.subdomain,
      status: status ?? this.status,
      createdAt: createdAt ?? this.createdAt,
      lastActiveAt: lastActiveAt ?? this.lastActiveAt,
      stats: stats ?? this.stats,
      preferences: preferences ?? this.preferences,
    );
  }

  @override
  List<Object?> get props => [
    id, 
    name, 
    subdomain, 
    status, 
    createdAt, 
    lastActiveAt, 
    stats, 
    preferences,
  ];
}

class TenantStats extends Equatable {
  final int connectedClients;
  final int connectedUsers;
  final int totalFiles;
  final int musicFiles;
  final int adFiles;
  final int ttsFiles;
  final double storageUsedGB;
  final double storageLimitGB;

  const TenantStats({
    required this.connectedClients,
    required this.connectedUsers,
    required this.totalFiles,
    required this.musicFiles,
    required this.adFiles,
    required this.ttsFiles,
    required this.storageUsedGB,
    required this.storageLimitGB,
  });

  factory TenantStats.fromJson(Map<String, dynamic> json) {
    return TenantStats(
      connectedClients: json['connectedClients'] as int? ?? 0,
      connectedUsers: json['connectedUsers'] as int? ?? 0,
      totalFiles: json['totalFiles'] as int? ?? 0,
      musicFiles: json['musicFiles'] as int? ?? 0,
      adFiles: json['adFiles'] as int? ?? 0,
      ttsFiles: json['ttsFiles'] as int? ?? 0,
      storageUsedGB: (json['storageUsedGB'] as num?)?.toDouble() ?? 0.0,
      storageLimitGB: (json['storageLimitGB'] as num?)?.toDouble() ?? 10.0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'connectedClients': connectedClients,
      'connectedUsers': connectedUsers,
      'totalFiles': totalFiles,
      'musicFiles': musicFiles,
      'adFiles': adFiles,
      'ttsFiles': ttsFiles,
      'storageUsedGB': storageUsedGB,
      'storageLimitGB': storageLimitGB,
    };
  }

  double get storageUsagePercentage => 
      storageLimitGB > 0 ? (storageUsedGB / storageLimitGB) * 100 : 0.0;

  @override
  List<Object?> get props => [
    connectedClients,
    connectedUsers,
    totalFiles,
    musicFiles,
    adFiles,
    ttsFiles,
    storageUsedGB,
    storageLimitGB,
  ];
} 