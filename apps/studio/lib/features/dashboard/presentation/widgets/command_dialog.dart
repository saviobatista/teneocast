import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/models/player.dart';
import '../bloc/dashboard_bloc.dart';

class CommandDialog extends StatefulWidget {
  final Player? selectedPlayer;

  const CommandDialog({super.key, this.selectedPlayer});

  @override
  State<CommandDialog> createState() => _CommandDialogState();
}

class _CommandDialogState extends State<CommandDialog> {
  Player? selectedPlayer;
  PlayerCommandType selectedCommand = PlayerCommandType.playAd;
  final Map<String, dynamic> payload = {};

  @override
  void initState() {
    super.initState();
    selectedPlayer = widget.selectedPlayer;
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<DashboardBloc, DashboardState>(
      builder: (context, state) {
        return AlertDialog(
          title: const Text('Send Command'),
          content: SizedBox(
            width: 400,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Player Selection
                DropdownButtonFormField<Player>(
                  value: selectedPlayer,
                  decoration: const InputDecoration(
                    labelText: 'Select Player',
                    border: OutlineInputBorder(),
                  ),
                  items: state.players
                      .where((player) => player.status == PlayerStatus.online)
                      .map((player) => DropdownMenuItem(
                            value: player,
                            child: Text(player.name),
                          ))
                      .toList(),
                  onChanged: (player) {
                    setState(() {
                      selectedPlayer = player;
                    });
                  },
                ),
                const SizedBox(height: 16),

                // Command Type Selection
                DropdownButtonFormField<PlayerCommandType>(
                  value: selectedCommand,
                  decoration: const InputDecoration(
                    labelText: 'Command Type',
                    border: OutlineInputBorder(),
                  ),
                  items: PlayerCommandType.values.map((command) => DropdownMenuItem(
                    value: command,
                    child: Text(_getCommandTypeText(command)),
                  )).toList(),
                  onChanged: (command) {
                    setState(() {
                      selectedCommand = command ?? PlayerCommandType.playAd;
                    });
                  },
                ),
                const SizedBox(height: 16),

                // Command-specific payload
                if (selectedCommand == PlayerCommandType.playAd) ...[
                  TextField(
                    decoration: const InputDecoration(
                      labelText: 'Ad URL',
                      border: OutlineInputBorder(),
                      hintText: 'https://example.com/ad.mp3',
                    ),
                    onChanged: (value) {
                      payload['audioUrl'] = value;
                    },
                  ),
                ] else if (selectedCommand == PlayerCommandType.playTts) ...[
                  TextField(
                    decoration: const InputDecoration(
                      labelText: 'TTS Text',
                      border: OutlineInputBorder(),
                      hintText: 'Enter text to convert to speech',
                    ),
                    maxLines: 3,
                    onChanged: (value) {
                      payload['text'] = value;
                    },
                  ),
                ],
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: selectedPlayer != null ? _sendCommand : null,
              child: const Text('Send'),
            ),
          ],
        );
      },
    );
  }

  void _sendCommand() {
    if (selectedPlayer != null) {
      context.read<DashboardBloc>().add(SendCommand(
        playerId: selectedPlayer!.id,
        playerName: selectedPlayer!.name,
        commandType: selectedCommand,
        payload: payload.isNotEmpty ? payload : null,
      ));
      Navigator.of(context).pop();
    }
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
} 