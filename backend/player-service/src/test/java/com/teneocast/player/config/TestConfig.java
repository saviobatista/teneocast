package com.teneocast.player.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        SetOperations<String, Object> setOps = Mockito.mock(SetOperations.class);
        ValueOperations<String, Object> valueOps = Mockito.mock(ValueOperations.class);
        
        Mockito.when(redisTemplate.opsForSet()).thenReturn(setOps);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);
        
        return redisTemplate;
    }

    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        return new ObjectMapper();
    }
}