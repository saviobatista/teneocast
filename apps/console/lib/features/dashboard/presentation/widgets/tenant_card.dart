import 'package:flutter/material.dart';
import 'package:teneocast_console/core/theme/app_theme.dart';
import 'package:teneocast_console/features/dashboard/domain/models/tenant.dart';

class TenantCard extends StatelessWidget {
  final Tenant tenant;

  const TenantCard({
    super.key,
    required this.tenant,
  });

  @override
  Widget build(BuildContext context) {
    final timeAgo = _getTimeAgo(tenant.lastActiveAt);
    final storagePercentage = (tenant.stats.storageUsedGB / tenant.stats.storageLimitGB) * 100;
    final storageColor = _getStorageColor(storagePercentage);

    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(8),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        tenant.name,
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        tenant.subdomain,
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: AppTheme.neutral500,
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: tenant.status == 'active' 
                        ? Colors.green.withOpacity(0.1)
                        : AppTheme.neutral300.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    tenant.status.toUpperCase(),
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: tenant.status == 'active' 
                          ? Colors.green
                          : AppTheme.neutral500,
                      fontWeight: FontWeight.w600,
                      fontSize: 10,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: _buildStatItem(
                    context,
                    'Connected Clients',
                    tenant.stats.connectedClients.toString(),
                    Icons.computer,
                    AppTheme.primaryNavy,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _buildStatItem(
                    context,
                    'Connected Users',
                    tenant.stats.connectedUsers.toString(),
                    Icons.people,
                    AppTheme.accentLime,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _buildStatItem(
                    context,
                    'Total Files',
                    tenant.stats.totalFiles.toString(),
                    Icons.folder,
                    AppTheme.accentViolet,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Storage Usage',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: AppTheme.neutral500,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          Expanded(
                            child: LinearProgressIndicator(
                              value: storagePercentage / 100,
                              backgroundColor: AppTheme.neutral200,
                              valueColor: AlwaysStoppedAnimation<Color>(storageColor),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            '${storagePercentage.toStringAsFixed(1)}%',
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: storageColor,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${tenant.stats.storageUsedGB.toStringAsFixed(1)} GB / ${tenant.stats.storageLimitGB.toStringAsFixed(1)} GB',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: AppTheme.neutral500,
                          fontSize: 10,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: _buildFileTypeItem(
                    context,
                    'Music',
                    tenant.stats.musicFiles.toString(),
                    Icons.music_note,
                    AppTheme.primaryNavy,
                  ),
                ),
                Expanded(
                  child: _buildFileTypeItem(
                    context,
                    'Ads',
                    tenant.stats.adFiles.toString(),
                    Icons.campaign,
                    AppTheme.accentCoral,
                  ),
                ),
                Expanded(
                  child: _buildFileTypeItem(
                    context,
                    'TTS',
                    tenant.stats.ttsFiles.toString(),
                    Icons.record_voice_over,
                    AppTheme.accentViolet,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                        const Icon(
          Icons.access_time,
          size: 12,
          color: AppTheme.neutral400,
        ),
                const SizedBox(width: 4),
                Expanded(
                  child: Text(
                    'Last active: $timeAgo',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: AppTheme.neutral400,
                      fontSize: 10,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatItem(
    BuildContext context,
    String label,
    String value,
    IconData icon,
    Color color,
  ) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(4),
          decoration: BoxDecoration(
            color: color.withOpacity(0.1),
            borderRadius: BorderRadius.circular(4),
          ),
          child: Icon(
            icon,
            size: 12,
            color: color,
          ),
        ),
        const SizedBox(height: 1),
        Text(
          value,
          style: Theme.of(context).textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: AppTheme.neutral500,
          ),
        ),
      ],
    );
  }

  Widget _buildFileTypeItem(
    BuildContext context,
    String label,
    String value,
    IconData icon,
    Color color,
  ) {
    return Column(
      children: [
        Icon(
          icon,
          size: 12,
          color: color,
        ),
        const SizedBox(height: 1),
        Text(
          value,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            fontWeight: FontWeight.w600,
          ),
        ),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: AppTheme.neutral500,
            fontSize: 10,
          ),
        ),
      ],
    );
  }

  String _getTimeAgo(DateTime? lastActiveAt) {
    if (lastActiveAt == null) return 'Never';
    
    final now = DateTime.now();
    final difference = now.difference(lastActiveAt);
    
    if (difference.inDays > 0) {
      return '${difference.inDays}d ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m ago';
    } else {
      return 'Just now';
    }
  }

  Color _getStorageColor(double percentage) {
    if (percentage >= 80) return Colors.red;
    if (percentage >= 60) return Colors.orange;
    if (percentage >= 40) return Colors.yellow;
    return Colors.green;
  }
} 