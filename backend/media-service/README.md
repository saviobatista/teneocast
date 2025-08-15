# TeneoCast Media Service

The Media Service is a Spring Boot application that handles audio asset management for the TeneoCast platform, including music files, advertisements, and media metadata.

## 🚀 Features

- **Music Management**: Upload, store, and manage music files with genre classification
- **Advertisement Management**: Upload, store, and manage advertisement files with type classification
- **S3/MinIO Integration**: Secure file storage with tenant isolation
- **Metadata Extraction**: Automatic audio file metadata extraction
- **Multi-tenancy**: Complete tenant isolation for all media assets
- **Search & Filtering**: Advanced search and filtering capabilities
- **File Validation**: Audio file format and size validation

## 🏗️ Architecture

### Core Components

- **Entities**: Music, Advertisement, MusicGenre, AdType
- **Services**: MusicService, AdvertisementService, StorageService, MediaProcessingService
- **Controllers**: MusicController, AdvertisementController, HealthController
- **Repositories**: JPA repositories for data access
- **Storage**: S3/MinIO integration for file storage

### Database Schema

- **media.music_genres**: Music genre definitions
- **media.ad_types**: Advertisement type definitions
- **media.music**: Music file metadata and storage references
- **media.advertisements**: Advertisement file metadata and storage references

## 🔧 Configuration

### Environment Variables

```yaml
# Database
SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/teneocast
SPRING_DATASOURCE_USERNAME: teneocast
SPRING_DATASOURCE_PASSWORD: teneocast_dev

# Redis
SPRING_REDIS_HOST: localhost
SPRING_REDIS_PORT: 6379

# S3/MinIO
AWS_S3_ENDPOINT: http://localhost:9000
AWS_S3_REGION: us-east-1
AWS_S3_BUCKET_PREFIX: teneocast-media
AWS_S3_ACCESS_KEY: teneocast
AWS_S3_SECRET_KEY: teneocast_dev
AWS_S3_FORCE_PATH_STYLE: true

# Media Settings
MEDIA_UPLOAD_MAX_FILE_SIZE: 100MB
MEDIA_UPLOAD_ALLOWED_AUDIO_FORMATS: mp3,wav,ogg,m4a,aac
MEDIA_PROCESSING_EXTRACT_METADATA: true
MEDIA_PROCESSING_VALIDATE_AUDIO: true
```

### Application Properties

```yaml
server:
  port: 8083
  servlet:
    context-path: /media

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

## 📡 API Endpoints

### Health Check
- `GET /health` - Service health status

### Music Management
- `POST /api/media/music` - Upload music file
- `GET /api/media/music/{id}` - Get music by ID
- `GET /api/media/music` - List/search music with pagination
- `GET /api/media/music/all` - Get all music for tenant
- `DELETE /api/media/music/{id}` - Delete music file
- `GET /api/media/music/genres` - Get all music genres

### Advertisement Management
- `POST /api/media/ad` - Upload advertisement file
- `GET /api/media/ad/{id}` - Get advertisement by ID
- `GET /api/media/ad` - List/search advertisements with pagination
- `GET /api/media/ad/all` - Get all advertisements for tenant
- `DELETE /api/media/ad/{id}` - Delete advertisement file
- `GET /api/media/ad/types` - Get ad types for tenant

## 🚀 Getting Started

### Prerequisites

- Java 17+
- PostgreSQL 15+
- Redis 7+
- MinIO (S3-compatible storage)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/saviobatista/teneocast.git
   cd teneocast/backend/media-service
   ```

2. **Set up database**
   ```bash
   # Create database and run migrations
   psql -U postgres -c "CREATE DATABASE teneocast;"
   ./gradlew flywayMigrate
   ```

3. **Configure environment**
   ```bash
   # Copy and modify application.yml as needed
   cp src/main/resources/application.yml src/main/resources/application-local.yml
   ```

4. **Run the service**
   ```bash
   ./gradlew bootRun
   ```

### Docker Development

1. **Build the image**
   ```bash
   docker build -t teneocast-media-service .
   ```

2. **Run with Docker Compose**
   ```bash
   # From the root directory
   docker compose up media-service
   ```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew integrationTest
```

### End-to-End Tests
```bash
./gradlew e2eTest
```

### Test Coverage
```bash
./gradlew jacocoTestReport
```

## 📊 Monitoring

### Health Endpoints
- `/health` - Basic health check
- `/actuator/health` - Detailed health information
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Logging
The service uses structured logging with the following levels:
- `DEBUG`: Detailed debugging information
- `INFO`: General information about service operations
- `WARN`: Warning messages for potential issues
- `ERROR`: Error messages for failed operations

## 🔒 Security

### Tenant Isolation
- All media assets are isolated by tenant ID
- File storage uses tenant-specific buckets
- API endpoints require `X-Tenant-ID` header

### File Validation
- File type validation (audio files only)
- File size limits (configurable)
- Malicious file detection

### Access Control
- Tenant-scoped access to all resources
- File path validation to prevent directory traversal

## 🚢 Deployment

### Production Considerations

1. **Database**: Use production PostgreSQL with proper backup strategy
2. **Storage**: Use production S3 or MinIO with replication
3. **Security**: Use proper IAM roles and access keys
4. **Monitoring**: Enable comprehensive logging and metrics
5. **Scaling**: Configure horizontal scaling for high load

### Environment-Specific Configurations

- **Development**: Local database, file storage, debug logging
- **Staging**: Staging database, S3 storage, info logging
- **Production**: Production database, S3 storage, warn/error logging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE.md) file for details.

## 🆘 Support

- **Issues**: [GitHub Issues](https://github.com/saviobatista/teneocast/issues)
- **Documentation**: [Project Wiki](https://github.com/saviobatista/teneocast/wiki)
- **Community**: [Discord Server](https://discord.gg/teneocast)
