import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/models/player.dart';
import '../bloc/dashboard_bloc.dart';

class TTSDialog extends StatefulWidget {
  final Player? selectedPlayer;

  const TTSDialog({super.key, this.selectedPlayer});

  @override
  State<TTSDialog> createState() => _TTSDialogState();
}

class _TTSDialogState extends State<TTSDialog> {
  Player? selectedPlayer;
  final TextEditingController _textController = TextEditingController();

  @override
  void initState() {
    super.initState();
    selectedPlayer = widget.selectedPlayer;
  }

  @override
  void dispose() {
    _textController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<DashboardBloc, DashboardState>(
      builder: (context, state) {
        return AlertDialog(
          title: const Text('Generate TTS'),
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

                // TTS Text Input
                TextField(
                  controller: _textController,
                  decoration: const InputDecoration(
                    labelText: 'Text to Speech',
                    border: OutlineInputBorder(),
                    hintText: 'Enter the text you want to convert to speech...',
                  ),
                  maxLines: 4,
                  maxLength: 500,
                ),
                const SizedBox(height: 8),
                Text(
                  'The text will be converted to speech and played on the selected player.',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            ElevatedButton.icon(
              onPressed: selectedPlayer != null && _textController.text.isNotEmpty
                  ? _generateTTS
                  : null,
              icon: const Icon(Icons.record_voice_over),
              label: const Text('Generate'),
            ),
          ],
        );
      },
    );
  }

  void _generateTTS() {
    if (selectedPlayer != null && _textController.text.isNotEmpty) {
      context.read<DashboardBloc>().add(GenerateTTS(
        playerId: selectedPlayer!.id,
        playerName: selectedPlayer!.name,
        text: _textController.text,
      ));
      Navigator.of(context).pop();
    }
  }
} 