# TeneoCast Multi-App - Flutter Web Apps Dockerfile

# Base Flutter stage with common dependencies
FROM ubuntu:24.04 AS flutter-base

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    git \
    unzip \
    xz-utils \
    zip \
    libglu1-mesa \
    && rm -rf /var/lib/apt/lists/*

# Install Flutter
ENV FLUTTER_HOME="/usr/local/flutter"
ENV FLUTTER_VERSION="3.16.0"
ENV PATH="$FLUTTER_HOME/bin:$FLUTTER_HOME/bin/cache/dart-sdk/bin:${PATH}"

RUN git clone https://github.com/flutter/flutter.git $FLUTTER_HOME && cd $FLUTTER_HOME && git fetch && git checkout $FLUTTER_VERSION

RUN flutter doctor
RUN flutter config --no-analytics

# Studio App Build Stage
FROM flutter-base AS studio-build
WORKDIR /app/studio
COPY apps/studio/pubspec.yaml apps/studio/pubspec.lock ./
COPY apps/studio ./
RUN rm -rf /app/studio/web
RUN flutter create --platforms web .
RUN flutter clean
RUN flutter build web --release --pwa-strategy=offline-first --dart-define=FLUTTER_WEB_CANVASKIT_URL=/studio/canvaskit/ --web-renderer canvaskit --base-href /studio/

# Console App Build Stage
FROM flutter-base AS console-build
WORKDIR /app/console
COPY apps/console/pubspec.yaml apps/console/pubspec.lock ./
COPY apps/console ./
RUN rm -rf /app/console/web
RUN flutter create --platforms web .
RUN flutter clean
RUN flutter build web --release --pwa-strategy=offline-first --dart-define=FLUTTER_WEB_CANVASKIT_URL=/console/canvaskit/ --web-renderer canvaskit --base-href /console/

# Player App Build Stage
FROM flutter-base AS player-build
WORKDIR /app/player
COPY apps/player/pubspec.yaml apps/player/pubspec.lock ./
COPY apps/player ./
RUN rm -rf /app/player/web
RUN flutter create --platforms web .
RUN flutter clean
RUN flutter build web --release --pwa-strategy=offline-first --dart-define=FLUTTER_WEB_CANVASKIT_URL=/player/canvaskit/ --web-renderer canvaskit --base-href /player/

# Production stage
FROM nginx:alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create directories for each app
RUN mkdir -p /usr/share/nginx/html/studio
RUN mkdir -p /usr/share/nginx/html/console
RUN mkdir -p /usr/share/nginx/html/player

# Copy built web apps from parallel build stages
COPY --from=studio-build /app/studio/build/web /usr/share/nginx/html/studio
COPY --from=console-build /app/console/build/web /usr/share/nginx/html/console
COPY --from=player-build /app/player/build/web /usr/share/nginx/html/player

# Copy home page
COPY apps/index.html /usr/share/nginx/html/index.html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
