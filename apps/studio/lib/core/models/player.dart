import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'player.g.dart';

enum PlayerStatus {
  @JsonValue('online')
  online,
  @JsonValue('offline')
  offline,
  @JsonValue('pairing')
  pairing,
  @JsonValue('error')
  error,
}

enum PlayerCommandType {
  @JsonValue('PLAY_AD')
  playAd,
  @JsonValue('PLAY_TTS')
  playTts,
  @JsonValue('PAUSE')
  pause,
  @JsonValue('RESUME')
  resume,
  @JsonValue('SKIP')
  skip,
  @JsonValue('SHUTDOWN')
  shutdown,
}

@JsonSerializable()
class Player extends Equatable {
  final String id;
  final String name;
  final String tenantId;
  final PlayerStatus status;
  final String? pairingCode;
  final DateTime? lastSeenAt;
  final Map<String, dynamic>? settings;
  final bool isActive;

  const Player({
    required this.id,
    required this.name,
    required this.tenantId,
    required this.status,
    this.pairingCode,
    this.lastSeenAt,
    this.settings,
    required this.isActive,
  });

  factory Player.fromJson(Map<String, dynamic> json) => _$PlayerFromJson(json);
  Map<String, dynamic> toJson() => _$PlayerToJson(this);

  Player copyWith({
    String? id,
    String? name,
    String? tenantId,
    PlayerStatus? status,
    String? pairingCode,
    DateTime? lastSeenAt,
    Map<String, dynamic>? settings,
    bool? isActive,
  }) {
    return Player(
      id: id ?? this.id,
      name: name ?? this.name,
      tenantId: tenantId ?? this.tenantId,
      status: status ?? this.status,
      pairingCode: pairingCode ?? this.pairingCode,
      lastSeenAt: lastSeenAt ?? this.lastSeenAt,
      settings: settings ?? this.settings,
      isActive: isActive ?? this.isActive,
    );
  }

  @override
  List<Object?> get props => [
        id,
        name,
        tenantId,
        status,
        pairingCode,
        lastSeenAt,
        settings,
        isActive,
      ];
} 