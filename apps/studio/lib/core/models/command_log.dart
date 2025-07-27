import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';
import 'player.dart';

part 'command_log.g.dart';

@JsonSerializable()
class CommandLog extends Equatable {
  final String id;
  final String playerId;
  final String playerName;
  final PlayerCommandType commandType;
  final Map<String, dynamic>? payload;
  final DateTime executedAt;
  final bool success;
  final String? errorMessage;

  const CommandLog({
    required this.id,
    required this.playerId,
    required this.playerName,
    required this.commandType,
    this.payload,
    required this.executedAt,
    required this.success,
    this.errorMessage,
  });

  factory CommandLog.fromJson(Map<String, dynamic> json) => _$CommandLogFromJson(json);
  Map<String, dynamic> toJson() => _$CommandLogToJson(this);

  @override
  List<Object?> get props => [
        id,
        playerId,
        playerName,
        commandType,
        payload,
        executedAt,
        success,
        errorMessage,
      ];
} 