package com.teneocast.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateUserRequest {
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    private TenantUserDto.UserRole role;
    
    private Boolean isActive;
} 