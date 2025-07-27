import 'package:flutter/material.dart';
import 'package:teneocast_console/core/theme/app_theme.dart';
import 'package:teneocast_console/features/dashboard/domain/models/tenant.dart';

class ImpersonationDropdown extends StatelessWidget {
  final List<Tenant> tenants;
  final String? selectedTenantId;
  final Function(String) onTenantSelected;

  const ImpersonationDropdown({
    super.key,
    required this.tenants,
    this.selectedTenantId,
    required this.onTenantSelected,
  });

  @override
  Widget build(BuildContext context) {
    final selectedTenant = tenants.isNotEmpty && selectedTenantId != null && selectedTenantId!.isNotEmpty
        ? tenants.firstWhere(
            (tenant) => tenant.id == selectedTenantId,
            orElse: () => tenants.first,
          )
        : null;

    return Container(
      constraints: const BoxConstraints(maxWidth: 300),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Impersonate Tenant',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: AppTheme.neutral600,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Container(
            decoration: BoxDecoration(
              border: Border.all(color: AppTheme.neutral300),
              borderRadius: BorderRadius.circular(8),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                value: selectedTenantId?.isEmpty == true ? null : selectedTenantId,
                isExpanded: true,
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                borderRadius: BorderRadius.circular(8),
                items: [
                  const DropdownMenuItem<String>(
                    value: null,
                    child: Row(
                      children: [
                        Icon(Icons.admin_panel_settings, size: 16),
                        SizedBox(width: 8),
                        Text('Admin Console'),
                      ],
                    ),
                  ),
                  ...tenants.map((tenant) => DropdownMenuItem<String>(
                    value: tenant.id,
                    child: Row(
                      children: [
                        Container(
                          width: 8,
                          height: 8,
                          decoration: const BoxDecoration(
                            color: Colors.grey,
                            shape: BoxShape.circle,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Text(
                                tenant.name,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w600,
                                ),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                                                              Text(
                                  tenant.subdomain,
                                  style: const TextStyle(
                                    fontSize: 12,
                                    color: Colors.grey,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 6,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: AppTheme.primaryNavy.withOpacity(0.1),
                            borderRadius: const BorderRadius.all(Radius.circular(4)),
                          ),
                                                      child: Text(
                              '${tenant.stats.connectedClients} clients',
                              style: const TextStyle(
                                fontSize: 10,
                                color: Colors.blue,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                        ),
                      ],
                    ),
                  )),
                ],
                onChanged: (value) {
                  if (value != null) {
                    onTenantSelected(value);
                  } else {
                    // Handle null value (Admin Console selected)
                    onTenantSelected('');
                  }
                },
                hint: const Text('Select tenant to impersonate'),
              ),
            ),
          ),
          if (selectedTenantId != null && selectedTenantId!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Row(
              children: [
                const Icon(
                  Icons.info_outline,
                  size: 16,
                  color: Colors.grey,
                ),
                const SizedBox(width: 4),
                                 Expanded(
                   child: Text(
                     'Impersonating: ${selectedTenant?.name ?? 'Unknown'}',
                     style: const TextStyle(
                       color: Colors.grey,
                     ),
                   ),
                 ),
                TextButton(
                  onPressed: () {
                    // Set to null to select Admin Console
                    onTenantSelected('');
                  },
                  child: const Text('Exit'),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }
} 