package com.teneocast.auth.integration;

import com.teneocast.auth.AuthServiceApplication;
import com.teneocast.auth.dto.LoginRequest;
import com.teneocast.auth.dto.LoginResponse;
import com.teneocast.auth.entity.User;
import com.teneocast.auth.repository.UserRepository;
import com.teneocast.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = AuthServiceApplication.class
)
@Testcontainers
class AuthServiceWithKafkaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("teneocast_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withKraft()
            .withReuse(true);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        // Configure TestRestTemplate to handle authentication responses
        restTemplate.getRestTemplate().setRequestFactory(
            new org.springframework.http.client.SimpleClientHttpRequestFactory()
        );
        // Configure error handler to not throw exceptions for 4xx responses
        restTemplate.getRestTemplate().setErrorHandler(
            new org.springframework.web.client.DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
                    // Don't treat 4xx responses as errors for testing
                    return response.getStatusCode().is5xxServerError();
                }
            }
        );
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void testFullAuthenticationFlow() {
        // Create a test user
        User testUser = User.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("Integration")
                .lastName("User")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build();

        userRepository.save(testUser);

        // Test login with username
        LoginRequest loginRequest = new LoginRequest("integrationuser", "admin123");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());

        // Test login with email
        LoginRequest emailLoginRequest = new LoginRequest("integration@example.com", "admin123");
        ResponseEntity<LoginResponse> emailResponse = restTemplate.postForEntity(
                "/auth/login",
                emailLoginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, emailResponse.getStatusCode());
        assertNotNull(emailResponse.getBody());
    }

    @Test
    void testUserLoginWithDifferentRoles() {
        // Create admin user
        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(User.UserRole.ADMIN)
                .isActive(true)
                .isEmailVerified(true)
                .build();

        userRepository.save(adminUser);

        // Test admin login
        LoginRequest adminLoginRequest = new LoginRequest("admin", "admin123");
        ResponseEntity<LoginResponse> adminResponse = restTemplate.postForEntity(
                "/auth/login",
                adminLoginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        assertNotNull(adminResponse.getBody());
    }

    @Test
    void testInvalidAuthenticationScenarios() {
        // Test with non-existent user
        LoginRequest nonExistentRequest = new LoginRequest("nonexistent", "password");
        ResponseEntity<Object> nonExistentResponse = restTemplate.exchange(
                "/auth/login",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(nonExistentRequest),
                Object.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, nonExistentResponse.getStatusCode());

        // Test with wrong password
        User testUser = User.builder()
                .username("wrongpassuser")
                .email("wrongpass@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("Wrong")
                .lastName("Pass")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build();

        userRepository.save(testUser);

        LoginRequest wrongPasswordRequest = new LoginRequest("wrongpassuser", "wrongpassword");
        ResponseEntity<Object> wrongPasswordResponse = restTemplate.exchange(
                "/auth/login",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(wrongPasswordRequest),
                Object.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, wrongPasswordResponse.getStatusCode());
    }

    @Test
    void testValidationErrors() {
        // Test missing username
        LoginRequest missingUsernameRequest = new LoginRequest(null, "password");
        ResponseEntity<Object> missingUsernameResponse = restTemplate.exchange(
                "/auth/login",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(missingUsernameRequest),
                Object.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, missingUsernameResponse.getStatusCode());

        // Test missing password
        LoginRequest missingPasswordRequest = new LoginRequest("username", null);
        ResponseEntity<Object> missingPasswordResponse = restTemplate.exchange(
                "/auth/login",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(missingPasswordRequest),
                Object.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, missingPasswordResponse.getStatusCode());
    }
} 