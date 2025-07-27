import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../../core/models/dashboard_stats.dart';
import '../../../../core/models/player.dart';
import '../../../../core/models/command_log.dart';
import '../../../../core/models/tts_log.dart';

part 'dashboard_event.dart';
part 'dashboard_state.dart';

class DashboardBloc extends Bloc<DashboardEvent, DashboardState> {
  DashboardBloc() : super(const DashboardState()) {
    on<LoadDashboard>(_onLoadDashboard);
    on<RefreshDashboard>(_onRefreshDashboard);
    on<FilterPlayers>(_onFilterPlayers);
    on<SendCommand>(_onSendCommand);
    on<GenerateTTS>(_onGenerateTTS);
    on<RegisterPlayer>(_onRegisterPlayer);
    on<ShutdownPlayer>(_onShutdownPlayer);
  }

  Future<void> _onLoadDashboard(
    LoadDashboard event,
    Emitter<DashboardState> emit,
  ) async {
    emit(state.copyWith(status: DashboardStatus.loading));
    
    try {
      // Simulate API calls - replace with actual API calls
      await Future.delayed(const Duration(milliseconds: 800));
      
      final stats = DashboardStats(
        connectedPlayers: 12,
        totalAudios: 245,
        playlistItemsReproduced: 1847,
        ttsGenerated: 89,
        maxSimultaneousPlayers: 15,
        lastUpdated: DateTime.now(),
      );

      // ignore: prefer_const_constructors
      final players = [
        Player(
          id: '1',
          name: 'Store Front Player',
          tenantId: 'tenant1',
          status: PlayerStatus.online,
          isActive: true,
          lastSeenAt: DateTime.now(),
        ),
        Player(
          id: '2',
          name: 'Back Office Player',
          tenantId: 'tenant1',
          status: PlayerStatus.online,
          isActive: true,
          lastSeenAt: DateTime.now().subtract(const Duration(minutes: 5)),
        ),
        Player(
          id: '3',
          name: 'Warehouse Player',
          tenantId: 'tenant1',
          status: PlayerStatus.offline,
          isActive: true,
          lastSeenAt: DateTime.now().subtract(const Duration(hours: 2)),
        ),
        // ignore: prefer_const_constructors
        Player(
          id: '4',
          name: 'New Player',
          tenantId: 'tenant1',
          status: PlayerStatus.pairing,
          pairingCode: 'ABC123',
          isActive: false,
        ),
      ];

      // ignore: prefer_const_constructors
      final commandLogs = [
        CommandLog(
          id: '1',
          playerId: '1',
          playerName: 'Store Front Player',
          commandType: PlayerCommandType.playAd,
          executedAt: DateTime.now().subtract(const Duration(minutes: 2)),
          success: true,
        ),
        CommandLog(
          id: '2',
          playerId: '2',
          playerName: 'Back Office Player',
          commandType: PlayerCommandType.playTts,
          executedAt: DateTime.now().subtract(const Duration(minutes: 5)),
          success: true,
        ),
        CommandLog(
          id: '3',
          playerId: '1',
          playerName: 'Store Front Player',
          commandType: PlayerCommandType.pause,
          executedAt: DateTime.now().subtract(const Duration(minutes: 10)),
          success: true,
        ),
      ];

      // ignore: prefer_const_constructors
      final ttsLogs = [
        TTSLog(
          id: '1',
          playerId: '2',
          playerName: 'Back Office Player',
          text: 'Attention shoppers: enjoy 10% off today!',
          audioUrl: 'https://cdn.teneocast.com/tts/tts-1.mp3',
          generatedAt: DateTime.now().subtract(const Duration(minutes: 5)),
          success: true,
        ),
        TTSLog(
          id: '2',
          playerId: '1',
          playerName: 'Store Front Player',
          text: 'Welcome to our store!',
          audioUrl: 'https://cdn.teneocast.com/tts/tts-2.mp3',
          generatedAt: DateTime.now().subtract(const Duration(minutes: 15)),
          success: true,
        ),
        TTSLog(
          id: '3',
          playerId: '1',
          playerName: 'Store Front Player',
          text: 'Closing in 30 minutes',
          audioUrl: 'https://cdn.teneocast.com/tts/tts-3.mp3',
          generatedAt: DateTime.now().subtract(const Duration(hours: 1)),
          success: true,
        ),
      ];

      emit(state.copyWith(
        status: DashboardStatus.success,
        stats: stats,
        players: players,
        filteredPlayers: players,
        commandLogs: commandLogs,
        ttsLogs: ttsLogs,
      ));
    } catch (error) {
      emit(state.copyWith(
        status: DashboardStatus.failure,
        errorMessage: error.toString(),
      ));
    }
  }

  Future<void> _onRefreshDashboard(
    RefreshDashboard event,
    Emitter<DashboardState> emit,
  ) async {
    add(const LoadDashboard());
  }

  void _onFilterPlayers(
    FilterPlayers event,
    Emitter<DashboardState> emit,
  ) {
    final filteredPlayers = state.players.where((player) {
      if (event.statusFilter != null && player.status != event.statusFilter) {
        return false;
      }
      if (event.searchQuery.isNotEmpty) {
        return player.name.toLowerCase().contains(event.searchQuery.toLowerCase());
      }
      return true;
    }).toList();

    emit(state.copyWith(
      filteredPlayers: filteredPlayers,
      statusFilter: event.statusFilter,
      searchQuery: event.searchQuery,
    ));
  }

  Future<void> _onSendCommand(
    SendCommand event,
    Emitter<DashboardState> emit,
  ) async {
    try {
      // Simulate API call
      await Future.delayed(const Duration(milliseconds: 500));
      
      // Add to command logs
      final newCommand = CommandLog(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        playerId: event.playerId,
        playerName: event.playerName,
        commandType: event.commandType,
        payload: event.payload,
        executedAt: DateTime.now(),
        success: true,
      );

      final updatedLogs = [newCommand, ...state.commandLogs];
      
      emit(state.copyWith(commandLogs: updatedLogs));
    } catch (error) {
      emit(state.copyWith(
        errorMessage: 'Failed to send command: ${error.toString()}',
      ));
    }
  }

  Future<void> _onGenerateTTS(
    GenerateTTS event,
    Emitter<DashboardState> emit,
  ) async {
    try {
      // Simulate API call
      await Future.delayed(const Duration(milliseconds: 1000));
      
      // Add to TTS logs
      final newTTS = TTSLog(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        playerId: event.playerId,
        playerName: event.playerName,
        text: event.text,
        audioUrl: 'https://cdn.teneocast.com/tts/tts-${DateTime.now().millisecondsSinceEpoch}.mp3',
        generatedAt: DateTime.now(),
        success: true,
      );

      final updatedLogs = [newTTS, ...state.ttsLogs];
      
      emit(state.copyWith(ttsLogs: updatedLogs));
    } catch (error) {
      emit(state.copyWith(
        errorMessage: 'Failed to generate TTS: ${error.toString()}',
      ));
    }
  }

  Future<void> _onRegisterPlayer(
    RegisterPlayer event,
    Emitter<DashboardState> emit,
  ) async {
    try {
      // Simulate API call
      await Future.delayed(const Duration(milliseconds: 500));
      
      final newPlayer = Player(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        name: event.name,
        tenantId: 'tenant1',
        status: PlayerStatus.pairing,
        pairingCode: _generatePairingCode(),
        isActive: false,
      );

      final updatedPlayers = [newPlayer, ...state.players];
      final updatedFilteredPlayers = [newPlayer, ...state.filteredPlayers];
      
      emit(state.copyWith(
        players: updatedPlayers,
        filteredPlayers: updatedFilteredPlayers,
      ));
    } catch (error) {
      emit(state.copyWith(
        errorMessage: 'Failed to register player: ${error.toString()}',
      ));
    }
  }

  Future<void> _onShutdownPlayer(
    ShutdownPlayer event,
    Emitter<DashboardState> emit,
  ) async {
    try {
      // Simulate API call
      await Future.delayed(const Duration(milliseconds: 500));
      
      final updatedPlayers = state.players.map((player) {
        if (player.id == event.playerId) {
          return player.copyWith(status: PlayerStatus.offline);
        }
        return player;
      }).toList();

      final updatedFilteredPlayers = state.filteredPlayers.map((player) {
        if (player.id == event.playerId) {
          return player.copyWith(status: PlayerStatus.offline);
        }
        return player;
      }).toList();
      
      emit(state.copyWith(
        players: updatedPlayers,
        filteredPlayers: updatedFilteredPlayers,
      ));
    } catch (error) {
      emit(state.copyWith(
        errorMessage: 'Failed to shutdown player: ${error.toString()}',
      ));
    }
  }

  String _generatePairingCode() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    return String.fromCharCodes(
      Iterable.generate(6, (_) => chars.codeUnitAt(DateTime.now().millisecondsSinceEpoch % chars.length)),
    );
  }
} 