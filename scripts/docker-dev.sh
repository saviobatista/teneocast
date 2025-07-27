#!/bin/bash

# TeneoCast Docker Development Script
# Manages Docker containers for development

set -e

echo "🐳 TeneoCast Docker Development"
echo "==============================="

# Parse command line arguments
COMMAND=${1:-help}
TARGET=${2:-all}

case $COMMAND in
    "start")
        echo "🚀 Starting TeneoCast services..."
        
        case $TARGET in
            "infrastructure"|"infra")
                echo "📊 Starting infrastructure services..."
                docker-compose up -d postgres redis minio kafka zookeeper
                ;;
            "frontend"|"fe")
                echo "🎨 Starting frontend applications..."
                docker-compose up -d studio console player
                ;;
            "backend"|"be")
                echo "🔧 Starting backend services..."
                echo "⚠️  Backend services are currently commented out in docker-compose.yml"
                echo "    Uncomment them when Spring Boot services are ready"
                ;;
            "all")
                echo "🌟 Starting all available services..."
                docker-compose up -d postgres redis minio kafka zookeeper studio console player
                ;;
            *)
                echo "❌ Invalid target: $TARGET"
                echo "Available targets: infrastructure, frontend, backend, all"
                exit 1
                ;;
        esac
        ;;
        
    "stop")
        echo "🛑 Stopping TeneoCast services..."
        
        case $TARGET in
            "infrastructure"|"infra")
                docker-compose stop postgres redis minio kafka zookeeper
                ;;
            "frontend"|"fe")
                docker-compose stop studio console player
                ;;
            "backend"|"be")
                echo "🔧 Backend services are not currently running"
                ;;
            "all")
                docker-compose stop
                ;;
            *)
                echo "❌ Invalid target: $TARGET"
                exit 1
                ;;
        esac
        ;;
        
    "restart")
        echo "🔄 Restarting TeneoCast services..."
        $0 stop $TARGET
        sleep 2
        $0 start $TARGET
        ;;
        
    "build")
        echo "🏗️  Building TeneoCast images..."
        
        case $TARGET in
            "frontend"|"fe")
                echo "🎨 Building frontend applications..."
                docker-compose build studio console player
                ;;
            "backend"|"be")
                echo "🔧 Building backend services..."
                echo "⚠️  Backend Dockerfiles need to be created"
                ;;
            "all")
                docker-compose build
                ;;
            *)
                echo "❌ Invalid target: $TARGET"
                exit 1
                ;;
        esac
        ;;
        
    "logs")
        echo "📄 Showing logs for $TARGET..."
        
        case $TARGET in
            "studio")
                docker-compose logs -f studio
                ;;
            "console")
                docker-compose logs -f console
                ;;
            "player")
                docker-compose logs -f player
                ;;
            "postgres")
                docker-compose logs -f postgres
                ;;
            "redis")
                docker-compose logs -f redis
                ;;
            "minio")
                docker-compose logs -f minio
                ;;
            "all")
                docker-compose logs -f
                ;;
            *)
                echo "❌ Invalid service: $TARGET"
                exit 1
                ;;
        esac
        ;;
        
    "status")
        echo "📊 TeneoCast Services Status:"
        echo "============================="
        docker-compose ps
        ;;
        
    "clean")
        echo "🧹 Cleaning up TeneoCast containers and images..."
        docker-compose down -v --rmi local
        docker system prune -f
        ;;
        
    "frontend-only")
        echo "🎨 Starting frontend-only development environment..."
        docker-compose -f docker-compose.frontend.yml up -d
        ;;
        
    "help")
        echo "Usage: $0 <command> [target]"
        echo ""
        echo "Commands:"
        echo "  start <target>       Start services"
        echo "  stop <target>        Stop services"
        echo "  restart <target>     Restart services"
        echo "  build <target>       Build images"
        echo "  logs <service>       Show logs"
        echo "  status               Show service status"
        echo "  clean                Clean up containers and images"
        echo "  frontend-only        Start only frontend apps"
        echo ""
        echo "Targets:"
        echo "  infrastructure       Postgres, Redis, MinIO, Kafka"
        echo "  frontend             Studio, Console, Player"
        echo "  backend              Spring Boot services"
        echo "  all                  All services"
        echo ""
        echo "Services (for logs):"
        echo "  studio, console, player, postgres, redis, minio, all"
        echo ""
        echo "Examples:"
        echo "  $0 start infrastructure    # Start only infrastructure"
        echo "  $0 start frontend          # Start only frontend apps"
        echo "  $0 logs studio             # View studio logs"
        echo "  $0 frontend-only           # Frontend development only"
        ;;
        
    *)
        echo "❌ Unknown command: $COMMAND"
        echo "Run '$0 help' for usage information"
        exit 1
        ;;
esac

echo ""
echo "🌐 Access URLs:"
echo "  • Studio:  http://localhost:3001"
echo "  • Console: http://localhost:3002"
echo "  • Player:  http://localhost:3003"
echo "  • MinIO:   http://localhost:9001 (admin: teneocast/teneocast_dev)"
echo ""
echo "Use '$0 status' to check service health" 