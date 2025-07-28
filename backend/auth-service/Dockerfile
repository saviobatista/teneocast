FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the build file
COPY build.gradle ./

# Copy the source code
COPY src ./src

# Copy the gradle wrapper
COPY gradlew ./
COPY gradle ./gradle

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew build -x test

# Create a non-root user
RUN addgroup --system app && adduser --system --ingroup app app

# Change ownership of the app directory
RUN chown -R app:app /app

# Switch to the app user
USER app

# Expose the port
EXPOSE 8080

# Run the application
CMD ["./gradlew", "bootRun"] 