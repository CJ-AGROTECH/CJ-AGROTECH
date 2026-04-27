# AGROTECH - Plataforma IoT para Agricultura

Sistema completo para monitoreo climático en parcelas de papa, flores y café en el Oriente Antioqueño (Colombia).

## Arquitectura
- **Backend**: Java 21, Spring Boot 4.0.5, PostgreSQL, MongoDB
- **Frontend**: React + Vite, TailwindCSS, Recharts
- **Hardware**: ESP32 con sensores IoT
- **Despliegue**: Docker Compose

## Variables Climáticas Monitoreadas
- Temperatura del Aire
- Humedad Relativa del Aire
- Presión Atmosférica
- Luminosidad
- Humedad del Suelo
- Temperatura del Suelo
- Precipitación
- Velocidad del Viento

## Despliegue Local

1. **Clonar el repositorio**:
   ```bash
   git clone <repo-url>
   cd CJ-AGROTECH
   ```

2. **Levantar servicios con Docker Compose**:
   ```bash
   docker-compose up --build
   ```

3. **Acceder a la aplicación**:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - PostgreSQL: localhost:5432
   - MongoDB: localhost:27017

## Hardware (ESP32)
- Cargar el código `agrotech/esp32/telemetria.ino` en el ESP32.
- Configurar WiFi y URL del backend.
- Los sensores envían datos cada 1 minuto vía HTTP POST.

## API Endpoints Principales
- `POST /api/auth/login` - Autenticación
- `GET /api/v1/dashboard/historico/{dispositivoId}` - Datos para gráficos
- `POST /api/v1/telemetria/captura` - Captura de datos IoT

## Seguridad
- JWT para autenticación
- Endpoints protegidos excepto login/registro y captura IoT
- CORS habilitado para frontend

## Funcionalidades Clave
- Motor de alertas con anti-spam (1 hora cooldown)
- Eficiencia hídrica real
- Ingesta automática de Open-Meteo cada 15 minutos
- Dashboard con gráficas Recharts
- Exportación CSV de datos históricos
