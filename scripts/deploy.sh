#!/usr/bin/env bash

# AGROTECH - Deployment Script
# Uso:
#   bash scripts/deploy.sh dev
#   bash scripts/deploy.sh prod

set -euo pipefail

ENVIRONMENT=${1:-dev}
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

wait_for_url() {
    local url="$1"
    local name="$2"
    local retries=15
    local count=0

    while [ $count -lt $retries ]; do
        if curl -s -o /dev/null -w "%{http_code}" "$url" >/dev/null 2>&1; then
            log_success "✅ $name está disponible en $url"
            return 0
        fi
        count=$((count + 1))
        log_info "Esperando $name... ($count/$retries)"
        sleep 3
    done

    log_error "❌ $name no respondió en $url"
    return 1
}

if [ ! -f "$PROJECT_DIR/.env" ]; then
    log_warning ".env no encontrado, creando desde .env.example"
    cp "$PROJECT_DIR/.env.example" "$PROJECT_DIR/.env"
    log_warning "Edita .env antes de ejecutar el despliegue"
fi

set -a
source "$PROJECT_DIR/.env"
set +a

log_info "Deteniendo servicios existentes..."
cd "$PROJECT_DIR"
docker compose down --remove-orphans 2>/dev/null || true

if [ "$ENVIRONMENT" = "prod" ]; then
    log_info "🏭 Desplegando en modo PRODUCTION"
    docker compose build --no-cache backend frontend
else
    log_info "🚀 Desplegando en modo DEVELOPMENT"
    docker compose build backend frontend
fi

log_success "Build de imágenes completado"

log_info "Iniciando servicios..."
docker compose up -d

log_info "Comprobando disponibilidad de servicios..."
wait_for_url "http://localhost:8080" "Backend" || true
wait_for_url "http://localhost:3000" "Frontend" || true

log_success "🎉 Despliegue completo"

echo ""
echo "📊 Servicios disponibles"
echo "   Backend:  http://localhost:8080"
echo "   Frontend: http://localhost:3000"
echo "   Postgres: localhost:5432"
echo "   MongoDB:  localhost:27017"
echo ""
echo "📋 Comandos útiles"
echo "   docker compose logs -f backend frontend"
echo "   docker compose exec backend bash"
echo "   docker compose exec frontend sh"
echo "   docker compose down --remove-orphans"
echo ""
