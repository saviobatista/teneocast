package com.teneocast.media.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@Slf4j
public class S3Config {
    
    @Value("${aws.s3.endpoint}")
    private String endpoint;
    
    @Value("${aws.s3.region}")
    private String region;
    
    @Value("${aws.s3.access-key}")
    private String accessKey;
    
    @Value("${aws.s3.secret-key}")
    private String secretKey;
    
    @Value("${aws.s3.force-path-style}")
    private boolean forcePathStyle;
    
    @Value("${aws.s3.enabled:true}")
    private boolean s3Enabled;
    
    @Bean
    @Conditional(S3EnabledCondition.class)
    public S3Client s3Client() {
        if (!s3Enabled) {
            log.info("S3 is disabled, not creating S3 client");
            return null;
        }
        
        log.info("Initializing S3 client with endpoint: {}, region: {}", endpoint, region);
        
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(forcePathStyle)
                .build();
        
        log.info("S3 client initialized successfully");
        
        return s3Client;
    }
}
