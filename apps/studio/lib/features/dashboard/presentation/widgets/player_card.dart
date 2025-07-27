import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../../../core/models/player.dart';

class PlayerCard extends StatelessWidget {
  final Player player;
  final VoidCallback onSendCommand;
  final VoidCallback onGenerateTTS;
  final VoidCallback onShutdown;
  final VoidCallback onLogin;

  const PlayerCard({
    super.key,
    required this.player,
    required this.onSendCommand,
    required this.onGenerateTTS,
    required this.onShutdown,
    required this.onLogin,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: _getStatusColor(player.status),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.radio,
                    color: Colors.white,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        player.name,
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        _getStatusText(player.status),
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: _getStatusColor(player.status),
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                ),
                if (player.pairingCode != null)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.orange.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(4),
                      border: Border.all(color: Colors.orange),
                    ),
                    child: Text(
                      'Code: ${player.pairingCode}',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Colors.orange,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
              ],
            ),
            if (player.lastSeenAt != null) ...[
              const SizedBox(height: 8),
              Text(
                'Last seen: ${DateFormat('MMM dd, HH:mm').format(player.lastSeenAt!)}',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: Colors.grey[600],
                ),
              ),
            ],
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: player.status == PlayerStatus.online ? onSendCommand : null,
                    icon: const Icon(Icons.play_arrow, size: 16),
                    label: const Text('Command'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 8),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: player.status == PlayerStatus.online ? onGenerateTTS : null,
                    icon: const Icon(Icons.record_voice_over, size: 16),
                    label: const Text('TTS'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 8),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: player.status == PlayerStatus.online ? onShutdown : null,
                    icon: const Icon(Icons.power_settings_new, size: 16),
                    label: const Text('Shutdown'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 8),
                      foregroundColor: Colors.red,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: onLogin,
                    icon: const Icon(Icons.login, size: 16),
                    label: const Text('Login'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 8),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Color _getStatusColor(PlayerStatus status) {
    switch (status) {
      case PlayerStatus.online:
        return Colors.green;
      case PlayerStatus.offline:
        return Colors.grey;
      case PlayerStatus.pairing:
        return Colors.orange;
      case PlayerStatus.error:
        return Colors.red;
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