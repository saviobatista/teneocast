# Contributing to TeneoCast

Thank you for your interest in contributing to TeneoCast! This document provides guidelines and information for contributors.

## 🤝 How to Contribute

### Types of Contributions

- **🐛 Bug Reports**: Report issues you've found
- **✨ Feature Requests**: Suggest new features
- **📝 Documentation**: Improve or add documentation
- **💻 Code Contributions**: Submit code changes
- **🧪 Testing**: Add or improve tests

## 🚀 Getting Started

### Prerequisites

- **Flutter 3.10+** for frontend development
- **Java 17+** for backend development
- **Docker & Docker Compose** for local development
- **Git** for version control

### Development Setup

1. **Fork the repository**
   ```bash
   git clone https://github.com/saviobatista/teneocast.git
   cd teneocast
   ```

2. **Set up your development environment**
   ```bash
   # Install dependencies
   flutter doctor
   java -version
   
   # Start development environment
   ./scripts/docker-dev.sh start all
   ```

3. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## 📝 Code Style Guidelines

### Flutter/Dart
- Follow the [Dart Style Guide](https://dart.dev/guides/language/effective-dart/style)
- Use `flutter analyze` to check code quality
- Write comprehensive widget tests for new components
- Use meaningful variable and function names

### Java/Spring Boot
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful class and method names
- Write unit tests for new functionality
- Document public APIs with Javadoc

### General
- Write clear, descriptive commit messages
- Keep functions small and focused
- Add comments for complex logic
- Update documentation when changing APIs

## 🧪 Testing

### Frontend Testing
```bash
# Run all Flutter tests
cd apps/player
flutter test

# Run with coverage
flutter test --coverage
```

### Backend Testing
```bash
# Run all backend tests
cd backend
./gradlew test

# Run integration tests
./gradlew integrationTest
```

## 📋 Pull Request Process

1. **Create a feature branch** from `main`
2. **Make your changes** following the style guidelines
3. **Write tests** for new functionality
4. **Update documentation** if needed
5. **Run tests** to ensure everything passes
6. **Submit a pull request** with a clear description

### Pull Request Template

```markdown
## Description
Brief description of the changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Test improvement

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] Commit messages are clear
```

## 🐛 Reporting Issues

### Bug Report Template

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. See error

**Expected behavior**
A clear description of what you expected to happen.

**Environment:**
- OS: [e.g. Windows 11]
- Flutter Version: [e.g. 3.10.0]
- Java Version: [e.g. 17.0.2]

**Additional context**
Add any other context about the problem here.
```

## 🎯 Feature Requests

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
A clear description of what the problem is.

**Describe the solution you'd like**
A clear description of what you want to happen.

**Describe alternatives you've considered**
A clear description of any alternative solutions.

**Additional context**
Add any other context or screenshots about the feature request.
```

## 📞 Getting Help

- **GitHub Issues**: [Create an issue](https://github.com/saviobatista/teneocast/issues)
- **Discord Community**: [Join our community](https://discord.gg/teneocast)
- **Documentation**: Check the [docs](docs/) folder

## 📄 License

By contributing to TeneoCast, you agree that your contributions will be licensed under the same license as the project.

---

Thank you for contributing to TeneoCast! 🎉 