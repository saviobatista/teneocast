import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../bloc/dashboard_bloc.dart';
import '../widgets/stats_card.dart';
import '../widgets/player_card.dart';
import '../widgets/activity_log_card.dart';
import '../widgets/command_dialog.dart';
import '../widgets/tts_dialog.dart';
import '../widgets/register_player_dialog.dart';
import '../../../../core/models/player.dart';

class DashboardPage extends StatefulWidget {
  const DashboardPage({super.key});

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends State<DashboardPage>
    with TickerProviderStateMixin {
  late TabController _tabController;
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    context.read<DashboardBloc>().add(const LoadDashboard());
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'TeneoCast Studio',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              context.read<DashboardBloc>().add(const RefreshDashboard());
            },
            tooltip: 'Refresh Dashboard',
          ),
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () => _showRegisterPlayerDialog(context),
            tooltip: 'Register New Player',
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(icon: Icon(Icons.dashboard), text: 'Overview'),
            Tab(icon: Icon(Icons.radio), text: 'Players'),
            Tab(icon: Icon(Icons.history), text: 'Activity'),
          ],
        ),
      ),
      body: BlocConsumer<DashboardBloc, DashboardState>(
        listener: (context, state) {
          if (state.errorMessage != null) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.errorMessage!),
                backgroundColor: Colors.red,
              ),
            );
          }
        },
        builder: (context, state) {
          if (state.status == DashboardStatus.loading) {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }

          if (state.status == DashboardStatus.failure) {
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
                    'Failed to load dashboard',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    state.errorMessage ?? 'Unknown error occurred',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      context.read<DashboardBloc>().add(const LoadDashboard());
                    },
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          return TabBarView(
            controller: _tabController,
            children: [
              _buildOverviewTab(context, state),
              _buildPlayersTab(context, state),
              _buildActivityTab(context, state),
            ],
          );
        },
      ),
    );
  }

  Widget _buildOverviewTab(BuildContext context, DashboardState state) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Statistics Cards
          if (state.stats != null) ...[
            Text(
              'System Statistics',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            GridView.count(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              crossAxisCount: 2,
              crossAxisSpacing: 16,
              mainAxisSpacing: 16,
              childAspectRatio: 1.5,
              children: [
                StatsCard(
                  title: 'Connected Players',
                  value: state.stats!.connectedPlayers.toString(),
                  icon: Icons.radio,
                  color: Colors.green,
                  subtitle: 'Active now',
                ),
                StatsCard(
                  title: 'Total Audios',
                  value: state.stats!.totalAudios.toString(),
                  icon: Icons.music_note,
                  color: Colors.blue,
                  subtitle: 'Available tracks',
                ),
                StatsCard(
                  title: 'Playlist Items',
                  value: state.stats!.playlistItemsReproduced.toString(),
                  icon: Icons.playlist_play,
                  color: Colors.orange,
                  subtitle: 'Reproduced today',
                ),
                StatsCard(
                  title: 'TTS Generated',
                  value: state.stats!.ttsGenerated.toString(),
                  icon: Icons.record_voice_over,
                  color: Colors.purple,
                  subtitle: 'This month',
                ),
              ],
            ),
            const SizedBox(height: 32),
          ],

          // Quick Actions
          Text(
            'Quick Actions',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: () => _showTTSDialog(context),
                  icon: const Icon(Icons.record_voice_over),
                  label: const Text('Generate TTS'),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: () => _showCommandDialog(context),
                  icon: const Icon(Icons.play_arrow),
                  label: const Text('Send Command'),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 32),

          // Recent Activity
          Text(
            'Recent Activity',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: ActivityLogCard(
                  title: 'Recent Commands',
                  items: state.commandLogs.take(3).map((log) => {
                    'title': '${log.playerName} - ${_getCommandTypeText(log.commandType)}',
                    'subtitle': DateFormat('MMM dd, HH:mm').format(log.executedAt),
                    'icon': _getCommandIcon(log.commandType),
                    'color': log.success ? Colors.green : Colors.red,
                  }).toList(),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: ActivityLogCard(
                  title: 'Recent TTS',
                  items: state.ttsLogs.take(3).map((log) => {
                    'title': log.text.length > 30 ? '${log.text.substring(0, 30)}...' : log.text,
                    'subtitle': '${log.playerName} - ${DateFormat('MMM dd, HH:mm').format(log.generatedAt)}',
                    'icon': Icons.record_voice_over,
                    'color': log.success ? Colors.purple : Colors.red,
                  }).toList(),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPlayersTab(BuildContext context, DashboardState state) {
    return Column(
      children: [
        // Search and Filter
        Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _searchController,
                  decoration: const InputDecoration(
                    hintText: 'Search players...',
                    prefixIcon: Icon(Icons.search),
                    border: OutlineInputBorder(),
                  ),
                  onChanged: (value) {
                    context.read<DashboardBloc>().add(FilterPlayers(
                      searchQuery: value,
                      statusFilter: state.statusFilter,
                    ));
                  },
                ),
              ),
              const SizedBox(width: 16),
              DropdownButton<PlayerStatus?>(
                value: state.statusFilter,
                hint: const Text('Status'),
                items: [
                  const DropdownMenuItem(
                    value: null,
                    child: Text('All Status'),
                  ),
                  ...PlayerStatus.values.map((status) => DropdownMenuItem(
                    value: status,
                    child: Text(_getStatusText(status)),
                  )),
                ],
                onChanged: (value) {
                  context.read<DashboardBloc>().add(FilterPlayers(
                    searchQuery: state.searchQuery,
                    statusFilter: value,
                  ));
                },
              ),
            ],
          ),
        ),

        // Players List
        Expanded(
          child: state.filteredPlayers.isEmpty
              ? const Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.radio,
                        size: 64,
                        color: Colors.grey,
                      ),
                      SizedBox(height: 16),
                      Text(
                        'No players found',
                        style: TextStyle(
                          fontSize: 18,
                          color: Colors.grey,
                        ),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: state.filteredPlayers.length,
                  itemBuilder: (context, index) {
                    final player = state.filteredPlayers[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: PlayerCard(
                        player: player,
                        onSendCommand: () => _showCommandDialog(context, player: player),
                        onGenerateTTS: () => _showTTSDialog(context, player: player),
                        onShutdown: () => _shutdownPlayer(context, player),
                        onLogin: () => _loginToPlayer(context, player),
                      ),
                    );
                  },
                ),
        ),
      ],
    );
  }

  Widget _buildActivityTab(BuildContext context, DashboardState state) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Command History',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          ListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: state.commandLogs.length,
            itemBuilder: (context, index) {
              final log = state.commandLogs[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: log.success ? Colors.green : Colors.red,
                    child: Icon(
                      _getCommandIcon(log.commandType),
                      color: Colors.white,
                    ),
                  ),
                  title: Text('${log.playerName} - ${_getCommandTypeText(log.commandType)}'),
                  subtitle: Text(DateFormat('MMM dd, yyyy HH:mm').format(log.executedAt)),
                  trailing: Icon(
                    log.success ? Icons.check_circle : Icons.error,
                    color: log.success ? Colors.green : Colors.red,
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 32),
          Text(
            'TTS History',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          ListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: state.ttsLogs.length,
            itemBuilder: (context, index) {
              final log = state.ttsLogs[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: log.success ? Colors.purple : Colors.red,
                    child: const Icon(
                      Icons.record_voice_over,
                      color: Colors.white,
                    ),
                  ),
                  title: Text(log.text),
                  subtitle: Text('${log.playerName} - ${DateFormat('MMM dd, yyyy HH:mm').format(log.generatedAt)}'),
                  trailing: Icon(
                    log.success ? Icons.check_circle : Icons.error,
                    color: log.success ? Colors.green : Colors.red,
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  void _showRegisterPlayerDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => const RegisterPlayerDialog(),
    );
  }

  void _showCommandDialog(BuildContext context, {Player? player}) {
    showDialog(
      context: context,
      builder: (context) => CommandDialog(selectedPlayer: player),
    );
  }

  void _showTTSDialog(BuildContext context, {Player? player}) {
    showDialog(
      context: context,
      builder: (context) => TTSDialog(selectedPlayer: player),
    );
  }

  void _shutdownPlayer(BuildContext context, Player player) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Shutdown Player'),
        content: Text('Are you sure you want to shutdown ${player.name}?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              context.read<DashboardBloc>().add(ShutdownPlayer(playerId: player.id));
              Navigator.of(context).pop();
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Shutdown'),
          ),
        ],
      ),
    );
  }

  void _loginToPlayer(BuildContext context, Player player) {
    // Navigate to player app - this would be implemented based on your routing
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Logging into ${player.name}...'),
      ),
    );
  }

  String _getCommandTypeText(PlayerCommandType type) {
    switch (type) {
      case PlayerCommandType.playAd:
        return 'Play Ad';
      case PlayerCommandType.playTts:
        return 'Play TTS';
      case PlayerCommandType.pause:
        return 'Pause';
      case PlayerCommandType.resume:
        return 'Resume';
      case PlayerCommandType.skip:
        return 'Skip';
      case PlayerCommandType.shutdown:
        return 'Shutdown';
    }
  }

  IconData _getCommandIcon(PlayerCommandType type) {
    switch (type) {
      case PlayerCommandType.playAd:
        return Icons.play_arrow;
      case PlayerCommandType.playTts:
        return Icons.record_voice_over;
      case PlayerCommandType.pause:
        return Icons.pause;
      case PlayerCommandType.resume:
        return Icons.play_arrow;
      case PlayerCommandType.skip:
        return Icons.skip_next;
      case PlayerCommandType.shutdown:
        return Icons.power_settings_new;
    }
  }

  String _getStatusText(PlayerStatus status) {
    switch (status) {
      case PlayerStatus.online:
        return 'Online';
      case PlayerStatus.offline:
        return 'Offline';
      case PlayerStatus.pairing:
        return 'Pairing';
      case PlayerStatus.error:
        return 'Error';
    }
  }
} 