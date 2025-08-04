# Integration Tests with Testcontainers

This directory contains integration tests for the Tenant Service using Testcontainers to provide isolated, containerized test environments.

## Overview

The integration tests use Testcontainers to spin up:
- **PostgreSQL 15** database for data persistence
- **Redis 7** for caching and session management

## Test Structure

### BaseIntegrationTest
The `BaseIntegrationTest` class provides:
- Common container setup for PostgreSQL and Redis
- Dynamic property configuration for database and Redis connections
- Helper methods for creating test data
- Automatic cleanup between tests

### Test Classes

1. **TenantIntegrationTest** - Tests tenant CRUD operations
2. **TenantUserIntegrationTest** - Tests user management and authentication
3. **TenantPreferencesIntegrationTest** - Tests tenant preferences functionality
4. **TenantSubscriptionIntegrationTest** - Tests subscription management

## Running the Tests

### Prerequisites
- Docker installed and running
- Java 17+
- Gradle

### Run All Integration Tests
```bash
./gradlew test --tests "*IntegrationTest"
```

### Run Specific Integration Test
```bash
./gradlew test --tests "TenantIntegrationTest"
```

### Run with Coverage
```bash
./gradlew test --tests "*IntegrationTest" jacocoTestReport
```

## Test Configuration

### Application Properties
Integration tests use the `integration-test` profile with configuration in `application-integration-test.yml`:

- **Database**: PostgreSQL with Flyway migrations
- **Redis**: For caching and session storage
- **JWT**: Test-specific secret keys
- **Logging**: Debug level for troubleshooting

### Container Configuration
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");

@Container
static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
    .withExposedPorts(6379);
```

## Test Data Management

### Helper Methods
The base class provides helper methods for creating test data:

```java
// Create a test tenant
Tenant tenant = createTestTenant("Test Tenant", "test-tenant");

// Create a test user
TenantUser user = createTestUser(tenant, "test@example.com", TenantUser.UserRole.MASTER);
```

### Data Cleanup
Tests automatically clean up data between runs:
```java
@BeforeEach
void setUp() {
    tenantUserRepository.deleteAll();
    tenantRepository.deleteAll();
}
```

## Test Categories

### 1. Tenant Management
- Create, read, update, delete tenants
- Subdomain validation
- Tenant status management
- Search and pagination

### 2. User Management
- User CRUD operations
- Role-based access control
- Email validation
- User authentication

### 3. Authentication & Authorization
- JWT token generation and validation
- Login/logout flows
- Token refresh
- Password encryption

### 4. Preferences Management
- Tenant-specific settings
- Volume controls
- Genre preferences
- Advertisement rules

### 5. Subscription Management
- Plan management
- Billing cycles
- Usage limits
- Expiration tracking

## Best Practices

### Test Isolation
- Each test is independent
- Data is cleaned up between tests
- Containers are shared across test classes for performance

### Assertions
- Use AssertJ for readable assertions
- Test both positive and negative scenarios
- Verify database state after operations

### Error Handling
- Test exception scenarios
- Validate error messages
- Test boundary conditions

## Troubleshooting

### Common Issues

1. **Container Startup Failures**
   - Ensure Docker is running
   - Check available ports
   - Verify container images are accessible

2. **Database Connection Issues**
   - Verify PostgreSQL container is healthy
   - Check Flyway migrations
   - Ensure proper credentials

3. **Redis Connection Issues**
   - Verify Redis container is healthy
   - Check port mapping
   - Ensure Redis is accessible

### Debug Mode
Enable debug logging by adding to `application-integration-test.yml`:
```yaml
logging:
  level:
    com.teneocast.tenant: DEBUG
    org.testcontainers: INFO
```

### Performance Tips
- Use `@Container` with `static` for shared containers
- Minimize data creation in `@BeforeEach`
- Use appropriate page sizes for pagination tests
- Clean up data efficiently

## Coverage

The integration tests cover:
- ✅ Service layer operations
- ✅ Repository layer with real database
- ✅ REST API endpoints
- ✅ Authentication flows
- ✅ Data validation
- ✅ Error handling
- ✅ Transaction management

## Future Enhancements

1. **Performance Tests**
   - Load testing with multiple tenants
   - Concurrent user operations
   - Database performance under load

2. **Security Tests**
   - SQL injection prevention
   - XSS protection
   - Rate limiting

3. **API Contract Tests**
   - OpenAPI specification validation
   - Response format consistency
   - Error response standardization 