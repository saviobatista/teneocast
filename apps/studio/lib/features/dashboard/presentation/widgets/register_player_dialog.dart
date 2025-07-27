import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/dashboard_bloc.dart';

class RegisterPlayerDialog extends StatefulWidget {
  const RegisterPlayerDialog({super.key});

  @override
  State<RegisterPlayerDialog> createState() => _RegisterPlayerDialogState();
}

class _RegisterPlayerDialogState extends State<RegisterPlayerDialog> {
  final TextEditingController _nameController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Register New Player'),
      content: Form(
        key: _formKey,
        child: SizedBox(
          width: 400,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(
                  labelText: 'Player Name',
                  border: OutlineInputBorder(),
                  hintText: 'e.g., Store Front Player',
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter a player name';
                  }
                  if (value.length < 3) {
                    return 'Player name must be at least 3 characters';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.blue.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.blue.withOpacity(0.3)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          Icons.info_outline,
                          color: Colors.blue[700],
                          size: 20,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'Registration Process',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.blue[700],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '1. A new player will be created with a pairing code\n'
                      '2. Install the TeneoCast Player app on the device\n'
                      '3. Enter the pairing code in the player app\n'
                      '4. The player will automatically connect',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        ElevatedButton.icon(
          onPressed: _registerPlayer,
          icon: const Icon(Icons.add),
          label: const Text('Register'),
        ),
      ],
    );
  }

  void _registerPlayer() {
    if (_formKey.currentState!.validate()) {
      context.read<DashboardBloc>().add(RegisterPlayer(
        name: _nameController.text.trim(),
      ));
      Navigator.of(context).pop();
    }
  }
} 