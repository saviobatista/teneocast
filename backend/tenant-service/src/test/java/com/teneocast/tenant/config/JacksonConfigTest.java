package com.teneocast.tenant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class JacksonConfigTest {

    @Test
    void testJacksonConfigBeanCreation() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(JacksonConfig.class);
        context.refresh();

        // When
        ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

        // Then
        assertNotNull(objectMapper);
        // Check if JavaTimeModule is registered (any module with "jsr310" in the name)
        assertTrue(objectMapper.getRegisteredModuleIds().stream().anyMatch(id -> id.toString().contains("jsr310")));
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertTrue(objectMapper.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING));

        context.close();
    }

    @Test
    void testObjectMapperPrimaryAnnotation() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(JacksonConfig.class);
        context.refresh();

        // When
        ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

        // Then
        assertNotNull(objectMapper);
        // The @Primary annotation ensures this bean is returned when multiple ObjectMapper beans exist

        context.close();
    }

    @Test
    void testObjectMapperDateTimeSerialization() throws Exception {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();
        LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 12, 30, 45);

        // When
        String json = objectMapper.writeValueAsString(testDateTime);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("2024-01-01T12:30:45"));
        // Should not be serialized as timestamp due to WRITE_DATES_AS_TIMESTAMPS being disabled
        assertFalse(json.matches("\\d+"));
    }

    @Test
    void testObjectMapperDateTimeDeserialization() throws Exception {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();
        String json = "\"2024-01-01T12:30:45\"";

        // When
        LocalDateTime dateTime = objectMapper.readValue(json, LocalDateTime.class);

        // Then
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 30, 45), dateTime);
    }

    @Test
    void testObjectMapperEnumSerialization() throws Exception {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();

        enum TestEnum {
            VALUE_ONE, VALUE_TWO
        }

        // When
        String json = objectMapper.writeValueAsString(TestEnum.VALUE_ONE);

        // Then
        assertEquals("\"VALUE_ONE\"", json);
        // Should use toString() due to WRITE_ENUMS_USING_TO_STRING being enabled
    }

    @Test
    void testObjectMapperEnumDeserialization() throws Exception {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();

        enum TestEnum {
            VALUE_ONE, VALUE_TWO
        }

        // When
        TestEnum value = objectMapper.readValue("\"VALUE_ONE\"", TestEnum.class);

        // Then
        assertEquals(TestEnum.VALUE_ONE, value);
    }

    @Test
    void testObjectMapperComplexObjectSerialization() throws Exception {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();

        TestObject testObject = new TestObject();
        testObject.setName("Test");
        testObject.setValue(42);
        testObject.setDateTime(LocalDateTime.of(2024, 1, 1, 12, 30, 45));
        testObject.setEnumValue(TestEnum.VALUE_ONE);

        // When
        String json = objectMapper.writeValueAsString(testObject);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Test\""));
        assertTrue(json.contains("\"value\":42"));
        assertTrue(json.contains("\"dateTime\":\"2024-01-01T12:30:45\""));
        assertTrue(json.contains("\"enumValue\":\"VALUE_ONE\""));
    }

    @Test
    void testObjectMapperComplexObjectDeserialization() throws Exception {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();

        String json = """
                {
                    "name": "Test",
                    "value": 42,
                    "dateTime": "2024-01-01T12:30:45",
                    "enumValue": "VALUE_ONE"
                }
                """;

        // When
        TestObject testObject = objectMapper.readValue(json, TestObject.class);

        // Then
        assertEquals("Test", testObject.getName());
        assertEquals(42, testObject.getValue());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 30, 45), testObject.getDateTime());
        assertEquals(TestEnum.VALUE_ONE, testObject.getEnumValue());
    }

    @Test
    void testObjectMapperNullHandling() throws Exception {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();

        TestObject testObject = new TestObject();
        testObject.setName(null);
        testObject.setValue(null);
        testObject.setDateTime(null);
        testObject.setEnumValue(null);

        // When
        String json = objectMapper.writeValueAsString(testObject);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"name\":null"));
        assertTrue(json.contains("\"value\":null"));
        assertTrue(json.contains("\"dateTime\":null"));
        assertTrue(json.contains("\"enumValue\":null"));
    }

    @Test
    void testObjectMapperConfigurationFeatures() {
        // Given
        JacksonConfig config = new JacksonConfig();
        ObjectMapper objectMapper = config.objectMapper();

        // Then
        // Verify that JavaTimeModule is registered
        assertTrue(objectMapper.getRegisteredModuleIds().stream().anyMatch(id -> id.toString().contains("jsr310")));

        // Verify serialization features
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertTrue(objectMapper.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING));
    }

    // Helper classes for testing
    enum TestEnum {
        VALUE_ONE, VALUE_TWO
    }

    static class TestObject {
        private String name;
        private Integer value;
        private LocalDateTime dateTime;
        private TestEnum enumValue;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }

        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

        public TestEnum getEnumValue() { return enumValue; }
        public void setEnumValue(TestEnum enumValue) { this.enumValue = enumValue; }
    }
} 