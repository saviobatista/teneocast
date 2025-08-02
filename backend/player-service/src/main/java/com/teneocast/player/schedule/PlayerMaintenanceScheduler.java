package com.teneocast.player.schedule;

import com.teneocast.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerMaintenanceScheduler {
    
    private final PlayerService playerService;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupExpiredPairingCodes() {
        try {
            playerService.cleanupExpiredPairingCodes();
        } catch (Exception e) {
            log.error("Error during pairing code cleanup", e);
        }
    }
}