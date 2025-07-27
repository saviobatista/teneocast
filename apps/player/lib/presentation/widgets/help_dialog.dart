import 'package:flutter/material.dart';

class HelpDialog extends StatelessWidget {
  const HelpDialog({super.key});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Help & Support'),
      content: const Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('TeneoCast Player Help', style: TextStyle(fontWeight: FontWeight.w600)),
          SizedBox(height: 8),
          Text('• Use play/pause controls to manage audio'),
          Text('• Monitor connection status in the top bar'),
          Text('• View recently played content in history'),
          Text('• Visual equalizer shows current audio levels'),
          SizedBox(height: 12),
          Text('For technical support, contact your system administrator.'),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Close'),
        ),
      ],
    );
  }
} 