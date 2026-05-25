#!/bin/bash

# AGROTECH - Deployment & Optimization Script
# Uso: bash scripts/deploy.sh [dev|prod]

set -e

ENVIRONMENT=${1:-dev}
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "🚀 AGROTECH Deployment Script"
echo "📍 Environment: $ENVIRONMENT"
echo "📂 Project: $PROJECT_DIR"
echo "⏰ Time: $(date)"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}ℹ ${NC}$1"
}

log_success() {
    echo -e "${GREEN}✓ ${NC}$1"
}

log_warning() {
    echo -e "${YELLOW}⚠ ${NC}$1"
}

log_error() {
    echo -e "${RED}✗ ${NC}$1"
}

# Validate environment file
if [ ! -f "$PROJECT_DIR/.env" ]; then
    log_warning ".env not found, creating from .env.example"
    cp "$PROJECT_DIR/.env.example" "$PROJECT_DIR/.env"
    log_warning "Please edit .env with your configuration"
fi

# Load environment variables
set -a
source "$PROJECT_DIR/.env"
set +a

log_info "Stopping any running containers..."
cd "$PROJECT_DIR"
docker compose down -v 2>/dev/null || true

log_info "Cleaning Docker cache..."
docker system prune -f --filter "dangling=true" 2>/dev/null || true

if [ "$ENVIRONMENT" = "prod" ]; then
    log_info "🏭 Building for PRODUCTION..."
    log_info "Building backend with optimizations..."
    docker compose build --no-cache backend
    
    log_info "Building frontend with optimizations..."
    docker compose build --no-cache frontend
    
    log_success "Production build complete"
    
    log_info "Starting services..."
    docker compose up -d
    
    log_info "Waiting for services to be healthy..."
    sleep 30
    
    # Health checks
    if curl -s http://localhost:8080/api/health >/dev/null 2>&1; then
        log_success "✅ Backend is healthy"
    else
        log_error "❌ Backend health check failed"
    fi
    
    if curl -s http://localhost:3000/health >/dev/null 2>&1; then
        log_success "✅ Frontend is healthy"
    else
        log_error "❌ Frontend health check failed"
    fi
    
else
    log_info "🚀 Building for DEVELOPMENT..."
    log_info "Building with cache (faster)..."
    docker compose build backend frontend
    
    log_success "Development build complete"
    
    log_info "Starting services..."
    docker compose up -d
    
    log_info "Waiting for services to start..."
    sleep 20
fi

echo ""
log_success "🎉 Deployment complete!"
echo ""
echo "📊 Services:"
echo "   Backend:  http://localhost:8080"
echo "   Frontend: http://localhost:3000"
echo "   Postgres: localhost:5432"
echo "   MongoDB:  localhost:27017"
echo ""
echo "📋 Useful commands:"
echo "   docker compose logs -f backend"
echo "   docker compose logs -f frontend"
echo "   docker compose exec backend bash"
echo "   docker compose exec frontend sh"
echo ""
echo "🛑 To stop: docker compose down -v"
echo ""
