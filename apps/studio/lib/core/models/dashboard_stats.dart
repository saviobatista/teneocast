import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dashboard_stats.g.dart';

@JsonSerializable()
class DashboardStats extends Equatable {
  final int connectedPlayers;
  final int totalAudios;
  final int playlistItemsReproduced;
  final int ttsGenerated;
  final int maxSimultaneousPlayers;
  final DateTime lastUpdated;

  const DashboardStats({
    required this.connectedPlayers,
    required this.totalAudios,
    required this.playlistItemsReproduced,
    required this.ttsGenerated,
    required this.maxSimultaneousPlayers,
    required this.lastUpdated,
  });

  factory DashboardStats.fromJson(Map<String, dynamic> json) => _$DashboardStatsFromJson(json);
  Map<String, dynamic> toJson() => _$DashboardStatsToJson(this);

  @override
  List<Object?> get props => [
        connectedPlayers,
        totalAudios,
        playlistItemsReproduced,
        ttsGenerated,
        maxSimultaneousPlayers,
        lastUpdated,
      ];
} 