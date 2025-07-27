package com.teneocast.common.dto;

/**
 * User roles for TeneoCast platform
 * As defined in the system documentation
 */
public enum UserRole {
    /**
     * Console Roles
     */
    ROOT,      // Full power
    OPERATOR,  // Restricted but tenant-wide view
    
    /**
     * Studio Roles
     */
    MASTER,    // Full permissions
    PRODUCER,  // Upload + remote commands
    MANAGER,   // User management only
    
    /**
     * Player Role
     */
    PLAYER     // Registered by Studio using pairing code
} 