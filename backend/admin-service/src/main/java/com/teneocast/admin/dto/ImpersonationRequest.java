package com.teneocast.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationRequest {

    @NotNull(message = "Target user ID is required")
    private UUID targetUserId;

    @NotNull(message = "Target tenant ID is required")
    private UUID targetTenantId;

    private Map<String, Object> metadata;
}
