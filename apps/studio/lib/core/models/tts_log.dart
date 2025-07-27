import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tts_log.g.dart';

@JsonSerializable()
class TTSLog extends Equatable {
  final String id;
  final String playerId;
  final String playerName;
  final String text;
  final String? audioUrl;
  final DateTime generatedAt;
  final bool success;
  final String? errorMessage;

  const TTSLog({
    required this.id,
    required this.playerId,
    required this.playerName,
    required this.text,
    this.audioUrl,
    required this.generatedAt,
    required this.success,
    this.errorMessage,
  });

  factory TTSLog.fromJson(Map<String, dynamic> json) => _$TTSLogFromJson(json);
  Map<String, dynamic> toJson() => _$TTSLogToJson(this);

  @override
  List<Object?> get props => [
        id,
        playerId,
        playerName,
        text,
        audioUrl,
        generatedAt,
        success,
        errorMessage,
      ];
} 