part of 'dashboard_bloc.dart';

abstract class DashboardEvent extends Equatable {
  const DashboardEvent();

  @override
  List<Object?> get props => [];
}

class LoadDashboard extends DashboardEvent {
  const LoadDashboard();
}

class RefreshDashboard extends DashboardEvent {
  const RefreshDashboard();
}

class FilterPlayers extends DashboardEvent {
  final String searchQuery;
  final PlayerStatus? statusFilter;

  const FilterPlayers({
    this.searchQuery = '',
    this.statusFilter,
  });

  @override
  List<Object?> get props => [searchQuery, statusFilter];
}

class SendCommand extends DashboardEvent {
  final String playerId;
  final String playerName;
  final PlayerCommandType commandType;
  final Map<String, dynamic>? payload;

  const SendCommand({
    required this.playerId,
    required this.playerName,
    required this.commandType,
    this.payload,
  });

  @override
  List<Object?> get props => [playerId, playerName, commandType, payload];
}

class GenerateTTS extends DashboardEvent {
  final String playerId;
  final String playerName;
  final String text;

  const GenerateTTS({
    required this.playerId,
    required this.playerName,
    required this.text,
  });

  @override
  List<Object?> get props => [playerId, playerName, text];
}

class RegisterPlayer extends DashboardEvent {
  final String name;

  const RegisterPlayer({required this.name});

  @override
  List<Object?> get props => [name];
}

class ShutdownPlayer extends DashboardEvent {
  final String playerId;

  const ShutdownPlayer({required this.playerId});

  @override
  List<Object?> get props => [playerId];
} 