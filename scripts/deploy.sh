#!/usr/bin/env bash

################################################################################
# AGROTECH - Deployment Script
# Plataforma IoT para Monitoreo Agrícola
#
# Uso:
#   bash scripts/deploy.sh dev                  # Modo desarrollo
#   bash scripts/deploy.sh prod                 # Modo producción
#   bash scripts/deploy.sh dev --clean          # Limpiar volúmenes (dev)
#   bash scripts/deploy.sh dev --help           # Mostrar ayuda
#
# Requisitos:
#   - Docker >= 20.10
#   - Docker Compose >= 2.0
#   - curl (para health checks)
################################################################################

set -euo pipefail

# Configuración
ENVIRONMENT=${1:-dev}
CLEAN_VOLUMES=${2:-}
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Contadores
ERRORS=0
WARNINGS=0

# Funciones de log
log_header() {
    echo -e "\n${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}\n"
}

log_info() {
    echo -e "${BLUE}ℹ ${NC}$1"
}

log_success() {
    echo -e "${GREEN}✓ ${NC}$1"
}

log_warning() {
    echo -e "${YELLOW}⚠ ${NC}$1"
    WARNINGS=$((WARNINGS + 1))
}

log_error() {
    echo -e "${RED}✗ ${NC}$1"
    ERRORS=$((ERRORS + 1))
}

log_cmd() {
    echo -e "${MAGENTA}→ ${NC}$1"
}

# Funciones de validación
check_prerequisites() {
    log_header "VALIDANDO REQUISITOS DEL SISTEMA"
    
    local missing_tools=0
    
    # Verificar Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker no encontrado. Instálalo desde https://docs.docker.com/get-docker/"
        missing_tools=1
    else
        local docker_version=$(docker --version | grep -oP '\d+\.\d+' | head -1)
        log_success "Docker $docker_version instalado"
    fi
    
    # Verificar Docker Compose
    if ! command -v docker compose &> /dev/null; then
        log_error "Docker Compose no encontrado. Instálalo desde https://docs.docker.com/compose/install/"
        missing_tools=1
    else
        local compose_version=$(docker compose version | grep -oP '\d+\.\d+' | head -1)
        log_success "Docker Compose $compose_version instalado"
    fi
    
    # Verificar curl
    if ! command -v curl &> /dev/null; then
        log_warning "curl no encontrado. Se usará para health checks pero el despliegue continuará"
    else
        log_success "curl disponible"
    fi
    
    if [ $missing_tools -ne 0 ]; then
        log_error "Faltan herramientas requeridas. Abortando."
        exit 1
    fi
}

check_disk_space() {
    log_info "Verificando espacio en disco..."
    local available=$(df "$PROJECT_DIR" | awk 'NR==2 {print $4}')
    local required=$((2000000))  # 2GB en KB
    
    if [ "$available" -lt "$required" ]; then
        log_error "Espacio insuficiente. Se requieren 2GB libres, disponibles: $((available / 1024))MB"
        exit 1
    else
        log_success "Espacio suficiente disponible: $((available / 1024 / 1024))GB"
    fi
}

check_ports() {
    log_info "Verificando disponibilidad de puertos..."
    
    local ports=(8080 3000 5432 27017)
    local busy=0
    
    for port in "${ports[@]}"; do
        if nc -z localhost "$port" 2>/dev/null; then
            log_warning "Puerto $port está en uso"
            busy=$((busy + 1))
        fi
    done
    
    if [ $busy -gt 0 ]; then
        log_warning "$busy puertos están en uso. Se intentará detener servicios existentes."
    fi
}

setup_env_file() {
    log_header "CONFIGURANDO VARIABLES DE ENTORNO"
    
    if [ ! -f "$PROJECT_DIR/.env" ]; then
        log_warning ".env no encontrado, creando desde .env.example"
        
        if [ ! -f "$PROJECT_DIR/.env.example" ]; then
            log_error ".env.example no encontrado"
            exit 1
        fi
        
        cp "$PROJECT_DIR/.env.example" "$PROJECT_DIR/.env"
        log_success ".env creado"
        log_warning "⚠️  Por favor, revisa y edita .env si necesitas cambiar credenciales o puertos"
    else
        log_success ".env encontrado"
    fi
    
    # Validar que .env tiene las variables requeridas
    local required_vars=("POSTGRES_PASSWORD" "MONGO_INITDB_ROOT_PASSWORD" "JWT_SECRET")
    for var in "${required_vars[@]}"; do
        if grep -q "^$var=" "$PROJECT_DIR/.env"; then
            log_success "$var está configurado"
        else
            log_error "$var no encontrado en .env"
            exit 1
        fi
    done
}

wait_for_port() {
    local port="$1"
    local service="$2"
    local retries=30
    local count=0
    
    while [ $count -lt $retries ]; do
        if nc -z localhost "$port" 2>/dev/null; then
            log_success "$service está escuchando en puerto $port"
            return 0
        fi
        count=$((count + 1))
        if [ $((count % 5)) -eq 0 ]; then
            log_info "Esperando $service en puerto $port... ($count/$retries)"
        fi
        sleep 1
    done
    
    log_error "$service no respondió en puerto $port después de $(($retries / 5)) minutos"
    return 1
}

wait_for_http() {
    local url="$1"
    local name="$2"
    local retries=30
    local count=0
    
    while [ $count -lt $retries ]; do
        if curl -s -f "$url" >/dev/null 2>&1; then
            log_success "$name está disponible en $url"
            return 0
        fi
        count=$((count + 1))
        if [ $((count % 5)) -eq 0 ]; then
            log_info "Esperando $name ($count/$retries)..."
        fi
        sleep 1
    done
    
    log_error "$name no respondió en $url"
    return 1
}


show_help() {
    cat << EOF
${CYAN}AGROTECH - Deployment Script${NC}

${GREEN}USAR:${NC}
  bash scripts/deploy.sh dev               Modo desarrollo (hot-reload, logs completos)
  bash scripts/deploy.sh prod              Modo producción (optimizado, no-cache)
  bash scripts/deploy.sh dev --clean       Limpiar volúmenes, base de datos y logs
  bash scripts/deploy.sh --help            Mostrar este mensaje

${GREEN}EJEMPLOS:${NC}
  ${MAGENTA}$ bash scripts/deploy.sh dev${NC}
    → Inicia AGROTECH en desarrollo

  ${MAGENTA}$ bash scripts/deploy.sh prod${NC}
    → Inicia AGROTECH en producción

  ${MAGENTA}$ bash scripts/deploy.sh dev --clean${NC}
    → Limpia todo y despliega fresh

${GREEN}SERVICIOS DISPONIBLES DESPUÉS DEL DESPLIEGUE:${NC}
  Backend API:  http://localhost:8080
  Frontend:     http://localhost:3000
  PostgreSQL:   localhost:5432
  MongoDB:      localhost:27017

${GREEN}COMANDOS ÚTILES DURANTE EJECUCIÓN:${NC}
  Ver logs:                 docker compose logs -f backend frontend
  Acceso al backend:        docker compose exec backend bash
  Acceso al frontend:       docker compose exec frontend sh
  Detener servicios:        docker compose down
  Detener y limpiar todo:   docker compose down -v

${GREEN}REQUISITOS:${NC}
  - Docker >= 20.10
  - Docker Compose >= 2.0
  - curl (para verificaciones)
  - 2GB de espacio libre mínimo

${GREEN}VARIABLES DE ENTORNO:${NC}
  Se cargan automáticamente desde .env
  Ver .env.example para valores por defecto
EOF
}

cleanup_all() {
    log_header "LIMPIANDO RECURSOS"
    
    log_cmd "Deteniendo contenedores..."
    docker compose down -v --remove-orphans 2>/dev/null || true
    
    log_cmd "Eliminando imágenes..."
    docker compose down --rmi all 2>/dev/null || true
    
    log_success "Limpieza completada"
}

# Procesar argumentos
if [ "$ENVIRONMENT" = "--help" ] || [ "$ENVIRONMENT" = "-h" ]; then
    show_help
    exit 0
fi

if [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    log_error "Ambiente inválido: $ENVIRONMENT"
    log_info "Usa 'dev' o 'prod', o ejecuta con --help para ayuda"
    exit 1
fi

if [ "$CLEAN_VOLUMES" = "--clean" ]; then
    cleanup_all
fi

# ============================================================================
# MAIN DEPLOYMENT
# ============================================================================

log_header "INICIANDO DESPLIEGUE DE AGROTECH - $ENVIRONMENT"

# Validaciones
check_prerequisites
check_disk_space
check_ports

# Configuración
setup_env_file

log_header "CARGANDO VARIABLES DE ENTORNO"
set -a
source "$PROJECT_DIR/.env"
set +a
log_success "Variables de entorno cargadas"

# Detener servicios existentes
log_header "PREPARANDO DESPLIEGUE"
log_cmd "Deteniendo servicios existentes..."
cd "$PROJECT_DIR"
docker compose down --remove-orphans 2>/dev/null || true
sleep 2
log_success "Servicios detenidos"

# Build
log_header "COMPILANDO IMÁGENES DOCKER"

if [ "$ENVIRONMENT" = "prod" ]; then
    log_info "🏭 Modo PRODUCTION - Build sin cache (más lento pero optimizado)"
    log_cmd "docker compose build --no-cache backend frontend"
    docker compose build --no-cache backend frontend
else
    log_info "🚀 Modo DEVELOPMENT - Build con cache (rápido)"
    log_cmd "docker compose build backend frontend"
    docker compose build backend frontend
fi

log_success "Imágenes compiladas exitosamente"

# Start services
log_header "INICIANDO SERVICIOS"
log_cmd "docker compose up -d"
docker compose up -d
log_success "Contenedores iniciados"

# Health checks
log_header "VERIFICANDO ESTADO DE SERVICIOS"

log_info "Esperando base de datos PostgreSQL..."
wait_for_port 5432 "PostgreSQL" || log_warning "PostgreSQL: puede tardar más"

log_info "Esperando base de datos MongoDB..."
wait_for_port 27017 "MongoDB" || log_warning "MongoDB: puede tardar más"

log_info "Esperando Backend API..."
wait_for_http "http://localhost:8080/swagger-ui.html" "Backend Swagger" || log_warning "Backend puede aún estar inicializando"

log_info "Esperando Frontend..."
wait_for_http "http://localhost:3000" "Frontend" || log_warning "Frontend puede aún estar inicializando"

# Final summary
log_header "🎉 DESPLIEGUE COMPLETADO CON ÉXITO"

echo -e "${GREEN}STATUS:${NC}"
docker compose ps

echo ""
echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}  📊 SERVICIOS DISPONIBLES${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  ${GREEN}Backend API:${NC}         http://localhost:8080"
echo -e "  ${GREEN}API Docs (Swagger):${NC} http://localhost:8080/swagger-ui.html"
echo -e "  ${GREEN}Frontend:${NC}           http://localhost:3000"
echo -e "  ${GREEN}PostgreSQL:${NC}         localhost:5432"
echo -e "  ${GREEN}MongoDB:${NC}            localhost:27017"
echo ""
echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}  📋 COMANDOS ÚTILES${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  ${MAGENTA}Ver logs (tiempo real):${NC}"
echo -e "    docker compose logs -f backend frontend"
echo ""
echo -e "  ${MAGENTA}Ejecutar comandos en contenedores:${NC}"
echo -e "    docker compose exec backend bash     # Backend Spring Boot"
echo -e "    docker compose exec frontend sh       # Frontend Node"
echo -e "    docker compose exec postgres psql -U agrotech -d agrotech_db"
echo ""
echo -e "  ${MAGENTA}Detener/Reiniciar:${NC}"
echo -e "    docker compose restart                # Reiniciar todo"
echo -e "    docker compose down                   # Detener todo"
echo -e "    docker compose down -v                # Detener y limpiar volúmenes"
echo ""
echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"

# Print warnings and errors summary
echo ""
if [ $WARNINGS -gt 0 ] || [ $ERRORS -gt 0 ]; then
    echo -e "${YELLOW}RESUMEN:${NC}"
    [ $WARNINGS -gt 0 ] && echo -e "  ${YELLOW}⚠  $WARNINGS advertencia(s)${NC}"
    [ $ERRORS -gt 0 ] && echo -e "  ${RED}✗ $ERRORS error(es)${NC}"
    echo ""
fi

if [ $ERRORS -eq 0 ]; then
    log_success "Despliegue finalizado correctamente"
    exit 0
else
    log_error "El despliegue completó con errores"
    exit 1
fi
