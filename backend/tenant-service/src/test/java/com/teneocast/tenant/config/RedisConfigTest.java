package com.teneocast.tenant.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory mockConnectionFactory;

    @Test
    void testRedisConfigBeanCreation() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RedisConfig.class);
        
        // Register mock RedisConnectionFactory
        context.registerBean("redisConnectionFactory", RedisConnectionFactory.class, () -> mockConnectionFactory);
        
        context.refresh();

        // When
        RedisTemplate<String, Object> redisTemplate = context.getBean(RedisTemplate.class);

        // Then
        assertNotNull(redisTemplate);
        assertNotNull(redisTemplate.getConnectionFactory());
        assertTrue(redisTemplate.getKeySerializer() instanceof StringRedisSerializer);
        assertTrue(redisTemplate.getHashKeySerializer() instanceof StringRedisSerializer);
        assertTrue(redisTemplate.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
        assertTrue(redisTemplate.getHashValueSerializer() instanceof GenericJackson2JsonRedisSerializer);

        context.close();
    }

    @Test
    void testRedisTemplateConfiguration() {
        // Given
        RedisConfig config = new RedisConfig();

        // When
        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // Then
        assertNotNull(redisTemplate);
        assertEquals(mockConnectionFactory, redisTemplate.getConnectionFactory());
    }

    @Test
    void testRedisTemplateSerializers() {
        // Given
        RedisConfig config = new RedisConfig();

        // When
        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // Then
        // Verify key serializers are StringRedisSerializer
        assertTrue(redisTemplate.getKeySerializer() instanceof StringRedisSerializer);
        assertTrue(redisTemplate.getHashKeySerializer() instanceof StringRedisSerializer);

        // Verify value serializers are GenericJackson2JsonRedisSerializer
        assertTrue(redisTemplate.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
        assertTrue(redisTemplate.getHashValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
    }

    @Test
    void testRedisTemplateAfterPropertiesSet() {
        // Given
        RedisConfig config = new RedisConfig();

        // When
        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // Then
        // The afterPropertiesSet() method should be called during bean initialization
        // This ensures the template is properly configured
        assertNotNull(redisTemplate);
    }

    @Test
    void testRedisTemplateStringOperations() {
        // Given
        RedisConfig config = new RedisConfig();

        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // When & Then
        // Test that the template can handle string operations
        assertNotNull(redisTemplate.opsForValue());
        assertNotNull(redisTemplate.opsForHash());
        assertNotNull(redisTemplate.opsForList());
        assertNotNull(redisTemplate.opsForSet());
        assertNotNull(redisTemplate.opsForZSet());
    }

    @Test
    void testRedisTemplateKeySerialization() {
        // Given
        RedisConfig config = new RedisConfig();

        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // When
        StringRedisSerializer keySerializer = (StringRedisSerializer) redisTemplate.getKeySerializer();
        String testKey = "test:key:123";
        byte[] serializedKey = keySerializer.serialize(testKey);

        // Then
        assertNotNull(serializedKey);
        assertEquals(testKey, keySerializer.deserialize(serializedKey));
    }

    @Test
    void testRedisTemplateValueSerialization() {
        // Given
        RedisConfig config = new RedisConfig();

        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // When
        GenericJackson2JsonRedisSerializer valueSerializer = (GenericJackson2JsonRedisSerializer) redisTemplate.getValueSerializer();
        TestObject testObject = new TestObject("test", 42);
        byte[] serializedValue = valueSerializer.serialize(testObject);

        // Then
        assertNotNull(serializedValue);
        TestObject deserializedObject = (TestObject) valueSerializer.deserialize(serializedValue);
        assertEquals(testObject.getName(), deserializedObject.getName());
        assertEquals(testObject.getValue(), deserializedObject.getValue());
    }

    @Test
    void testRedisTemplateHashOperations() {
        // Given
        RedisConfig config = new RedisConfig();

        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // When
        StringRedisSerializer hashKeySerializer = (StringRedisSerializer) redisTemplate.getHashKeySerializer();
        GenericJackson2JsonRedisSerializer hashValueSerializer = (GenericJackson2JsonRedisSerializer) redisTemplate.getHashValueSerializer();

        String hashKey = "hash:key";
        TestObject hashValue = new TestObject("hashValue", 100);

        byte[] serializedHashKey = hashKeySerializer.serialize(hashKey);
        byte[] serializedHashValue = hashValueSerializer.serialize(hashValue);

        // Then
        assertNotNull(serializedHashKey);
        assertNotNull(serializedHashValue);
        assertEquals(hashKey, hashKeySerializer.deserialize(serializedHashKey));
        
        TestObject deserializedHashValue = (TestObject) hashValueSerializer.deserialize(serializedHashValue);
        assertEquals(hashValue.getName(), deserializedHashValue.getName());
        assertEquals(hashValue.getValue(), deserializedHashValue.getValue());
    }

    @Test
    void testRedisTemplateNullHandling() {
        // Given
        RedisConfig config = new RedisConfig();

        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(mockConnectionFactory);

        // When
        StringRedisSerializer keySerializer = (StringRedisSerializer) redisTemplate.getKeySerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = (GenericJackson2JsonRedisSerializer) redisTemplate.getValueSerializer();

        // Then
        // Test null key serialization
        assertNull(keySerializer.serialize(null));
        assertNull(keySerializer.deserialize(null));

        // Test null value serialization
        assertNull(valueSerializer.serialize(null));
        assertNull(valueSerializer.deserialize(null));
    }

    // Helper class for testing
    static class TestObject {
        private String name;
        private int value;

        public TestObject() {}

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return value == that.value && java.util.Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, value);
        }
    }
} 