part of 'dashboard_bloc.dart';

enum DashboardStatus { initial, loading, success, failure }

class DashboardState extends Equatable {
  final DashboardStatus status;
  final DashboardStats? stats;
  final List<Player> players;
  final List<Player> filteredPlayers;
  final List<CommandLog> commandLogs;
  final List<TTSLog> ttsLogs;
  final String? errorMessage;
  final String searchQuery;
  final PlayerStatus? statusFilter;

  const DashboardState({
    this.status = DashboardStatus.initial,
    this.stats,
    this.players = const [],
    this.filteredPlayers = const [],
    this.commandLogs = const [],
    this.ttsLogs = const [],
    this.errorMessage,
    this.searchQuery = '',
    this.statusFilter,
  });

  DashboardState copyWith({
    DashboardStatus? status,
    DashboardStats? stats,
    List<Player>? players,
    List<Player>? filteredPlayers,
    List<CommandLog>? commandLogs,
    List<TTSLog>? ttsLogs,
    String? errorMessage,
    String? searchQuery,
    PlayerStatus? statusFilter,
  }) {
    return DashboardState(
      status: status ?? this.status,
      stats: stats ?? this.stats,
      players: players ?? this.players,
      filteredPlayers: filteredPlayers ?? this.filteredPlayers,
      commandLogs: commandLogs ?? this.commandLogs,
      ttsLogs: ttsLogs ?? this.ttsLogs,
      errorMessage: errorMessage ?? this.errorMessage,
      searchQuery: searchQuery ?? this.searchQuery,
      statusFilter: statusFilter ?? this.statusFilter,
    );
  }

  @override
  List<Object?> get props => [
        status,
        stats,
        players,
        filteredPlayers,
        commandLogs,
        ttsLogs,
        errorMessage,
        searchQuery,
        statusFilter,
      ];
} 