# Contributing to TeneoCast

Thank you for your interest in contributing to TeneoCast! This document provides guidelines and information for contributors.

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Flutter 3.10+
- Docker & Docker Compose
- Git

### Setting Up Development Environment

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/teneocast.git
   cd teneocast
   ```

2. **Run the setup script**
   ```bash
   ./scripts/dev-setup.sh
   ```

3. **Choose development mode**

   **🐳 Docker Mode (Recommended)**
   ```bash
   # Start everything in containers
   ./scripts/docker-dev.sh start all
   
   # Or start components separately
   ./scripts/docker-dev.sh start infrastructure
   ./scripts/docker-dev.sh start frontend
   ```

   **🖥️ Local Mode**
   ```bash
   # Backend (local)
   ./scripts/dev/start-backend.sh
   
   # Frontend (local, in another terminal)
   ./scripts/dev/start-frontend.sh
   ```

## 📋 Development Guidelines

### Code Style

#### Backend (Java/Spring Boot)
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Write comprehensive Javadoc for public APIs
- Maintain test coverage above 80%

#### Frontend (Flutter/Dart)
- Follow [Dart Style Guide](https://dart.dev/guides/language/effective-dart/style)
- Use `flutter analyze` to check code quality
- Implement responsive design principles
- Write widget tests for UI components

### Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Examples:
- `feat(auth): add JWT token refresh mechanism`
- `fix(player): resolve audio sync issue on Windows`
- `docs(api): update WebSocket protocol documentation`

### Branch Naming

- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `hotfix/description` - Critical fixes
- `docs/description` - Documentation updates

### Testing Requirements

#### Backend
- Unit tests for all service methods
- Integration tests for API endpoints
- Use Testcontainers for database tests
- Mock external dependencies

#### Frontend
- Widget tests for UI components
- Integration tests for user flows
- Unit tests for business logic
- Test on multiple screen sizes

### Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write tests first (TDD approach recommended)
   - Implement the feature
   - Ensure all tests pass

3. **Run quality checks**
   ```bash
   # Backend
   cd backend && ./gradlew check
   
   # Frontend
   cd apps/studio && flutter analyze && flutter test
   cd apps/player && flutter analyze && flutter test
   ```

4. **Submit Pull Request**
   - Use the PR template
   - Link related issues
   - Add screenshots for UI changes
   - Request review from maintainers

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Screenshots (if applicable)

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Tests pass locally
- [ ] Documentation updated
```

## 🏗️ Architecture Guidelines

### Backend Services

- **Single Responsibility**: Each service has one clear purpose
- **Database per Service**: Each service owns its data
- **API First**: Design APIs before implementation
- **Security**: Implement authentication and authorization
- **Observability**: Add logging, metrics, and tracing

### Frontend Applications

- **Component-Based**: Build reusable UI components
- **State Management**: Use BLoC pattern consistently
- **Responsive Design**: Support multiple screen sizes
- **Offline Support**: Implement offline capabilities where needed
- **Accessibility**: Follow accessibility guidelines

### Database Design

- **Migrations**: Use Flyway for schema changes
- **Indexes**: Add appropriate database indexes
- **Constraints**: Enforce data integrity at database level
- **Audit Trails**: Track changes to critical data

## 🐛 Reporting Issues

### Bug Reports

Include the following information:
- TeneoCast version
- Platform (Windows, Android, Web)
- Steps to reproduce
- Expected vs actual behavior
- Screenshots or logs
- Environment details

### Feature Requests

- Clear description of the feature
- Use case and business value
- Proposed implementation approach
- Any breaking changes

## 📚 Documentation

### API Documentation

- Use OpenAPI/Swagger specifications
- Include request/response examples
- Document authentication requirements
- Provide error code explanations

### Code Documentation

- Document complex business logic
- Include architecture decision records (ADRs)
- Maintain up-to-date README files
- Write clear inline comments

## 🔒 Security

### Reporting Security Issues

**Do not open public issues for security vulnerabilities.**

Email security issues to: security@teneocast.com

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if known)

### Security Guidelines

- Never commit secrets or credentials
- Use environment variables for configuration
- Validate all user inputs
- Implement proper authentication/authorization
- Use HTTPS in production
- Regularly update dependencies

## 📄 License

By contributing to TeneoCast, you agree that your contributions will be licensed under the project's license.

## 🤝 Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive experience for everyone.

### Standards

- Use welcoming and inclusive language
- Respect differing viewpoints and experiences
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards other community members

### Enforcement

Instances of unacceptable behavior may be reported to the project maintainers.

## 🎉 Recognition

Contributors will be recognized in:
- Release notes
- Contributors file
- Project documentation

Thank you for contributing to TeneoCast! 🚀 