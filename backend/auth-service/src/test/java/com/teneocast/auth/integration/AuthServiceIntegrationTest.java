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
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("teneocast_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

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
        // Configure TestRestTemplate to handle authentication responses properly
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
    }

    @Test
    void testUserRegistrationAndLogin() {
        // Test user registration
        User testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build();

        userRepository.save(testUser);

        // Test login
        LoginRequest loginRequest = new LoginRequest("testuser", "admin123");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                LoginResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "wrongpassword");
        ResponseEntity<Object> response = restTemplate.exchange(
                "/auth/login",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(loginRequest),
                Object.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoginWithMissingFields() {
        LoginRequest loginRequest = new LoginRequest(null, "password");
        ResponseEntity<Object> response = restTemplate.exchange(
                "/auth/login",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(loginRequest),
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
} 