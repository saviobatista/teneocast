package com.teneocast.admin.config;

import com.teneocast.admin.entity.AdminUser;
import com.teneocast.admin.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader;
                
                // Validate JWT token and get user
                var adminUserOpt = jwtService.validateTokenAndGetUser(token);
                
                if (adminUserOpt.isPresent()) {
                    AdminUser adminUser = adminUserOpt.get();
                    
                    // Create authentication token with user details and authorities
                    var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + adminUser.getRole().name())
                    );
                    
                    var authentication = new UsernamePasswordAuthenticationToken(
                        adminUser,
                        null,
                        authorities
                    );
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authentication successful for user: {} with role: {}", 
                            adminUser.getEmail(), adminUser.getRole());
                    
                } else {
                    log.debug("JWT token validation failed");
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing JWT authentication: {}", e.getMessage());
            // Don't set authentication - let the request proceed without authentication
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Don't filter health endpoints and actuator endpoints
        return path.startsWith("/health") || 
               path.startsWith("/actuator") || 
               path.equals("/error");
    }
}
