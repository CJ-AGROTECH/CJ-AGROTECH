# ===================================
# Build Stage - Compilación
# ===================================
FROM node:18-alpine AS builder

WORKDIR /build

# Copiar archivos de dependencias
COPY package*.json ./
COPY pnpm-lock.yaml ./

# Usar pnpm para instalar (más rápido y eficiente que npm)
RUN npm install -g pnpm && pnpm install --frozen-lockfile

# Copiar código fuente
COPY . .

# Build optimizado
RUN pnpm run build

# ===================================
# Runtime Stage - Nginx
# ===================================
FROM nginx:alpine

# Copiar configuración de nginx optimizada
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# Copiar archivos build desde builder
COPY --from=builder /build/dist /usr/share/nginx/html


# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/index.html || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
