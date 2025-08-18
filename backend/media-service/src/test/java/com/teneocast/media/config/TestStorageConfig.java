package com.teneocast.media.config;

import com.teneocast.media.service.LocalFileStorageService;
import com.teneocast.media.service.StorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestStorageConfig {
    
    @Bean
    @Primary
    public StorageService storageService() {
        return new LocalFileStorageService();
    }
}
