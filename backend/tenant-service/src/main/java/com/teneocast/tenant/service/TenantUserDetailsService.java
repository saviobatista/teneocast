package com.teneocast.tenant.service;

import com.teneocast.tenant.entity.TenantUser;
import com.teneocast.tenant.repository.TenantUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantUserDetailsService implements UserDetailsService {

    private final TenantUserRepository tenantUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        // Extract tenant ID and email from username (format: tenantId:email)
        String[] parts = username.split(":", 2);
        if (parts.length != 2) {
            log.warn("Invalid username format: {}", username);
            throw new UsernameNotFoundException("Invalid username format: " + username);
        }
        
        String tenantId = parts[0];
        String email = parts[1];
        
        TenantUser tenantUser = tenantUserRepository.findByTenantIdAndEmail(tenantId, email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {} in tenant: {}", email, tenantId);
                    return new UsernameNotFoundException("User not found with email: " + email + " in tenant: " + tenantId);
                });
        
        if (!tenantUser.getIsActive()) {
            log.warn("Inactive user attempted login: {} in tenant: {}", email, tenantId);
            throw new UsernameNotFoundException("User account is inactive");
        }
        
        return User.builder()
                .username(username)
                .password(tenantUser.getPasswordHash() != null ? tenantUser.getPasswordHash() : "")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + tenantUser.getRole().name())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!tenantUser.getIsActive())
                .build();
    }
} 