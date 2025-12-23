#!/bin/bash

# Task Manager - Quick Start Script

echo "=========================================="
echo "Task Manager - Full Stack Application"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    echo "Visit: https://docs.docker.com/compose/install/"
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"
echo ""

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    echo "❌ Docker daemon is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker daemon is running"
echo ""

# Create frontend .env if it doesn't exist
if [ ! -f "frontend/.env" ]; then
    echo "📝 Creating frontend/.env from .env.example..."
    cp frontend/.env.example frontend/.env
fi

# Stop any existing containers
echo "🛑 Stopping any existing containers..."
docker-compose down

echo ""
echo "🚀 Starting all services..."
echo "   - PostgreSQL Database"
echo "   - Spring Boot Backend"
echo "   - React Frontend"
echo ""

# Build and start all services
docker-compose up -d --build

# Wait for services to be healthy
echo ""
echo "⏳ Waiting for services to start..."
sleep 5

# Check service status
echo ""
echo "📊 Service Status:"
docker-compose ps

echo ""
echo "=========================================="
echo "✨ Application is starting!"
echo "=========================================="
echo ""
echo "Please wait 30-60 seconds for all services to be fully ready."
echo ""
echo "Access the application at:"
echo "  🌐 Frontend:  http://localhost:3000"
echo "  🔌 Backend:   http://localhost:8080/api/v1"
echo "  📚 Swagger:   http://localhost:8080/api/v1/swagger-ui.html"
echo "  🗄️  Database:  localhost:5432 (postgres/postgres)"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f"
echo ""
echo "To stop all services:"
echo "  docker-compose down"
echo ""
echo "=========================================="

# Follow logs
read -p "Do you want to view logs? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker-compose logs -f
fi
