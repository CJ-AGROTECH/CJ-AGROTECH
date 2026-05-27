# AGROTECH

Plataforma IoT completa para monitoreo agrícola con backend Java, frontend React y despliegue Docker.

## Arquitectura general
- **Backend**: Java 21, Spring Boot 4.0.5
- **Frontend**: React + Vite + TailwindCSS + Recharts
- **Base de datos relacional**: PostgreSQL
- **Base de datos de series temporales**: MongoDB
- **Orquestación**: Docker Compose
- **Sensor IoT**: ESP32

## Qué hace esta aplicación
AGROTECH permite:
- gestionar fincas y lotes agrícolas
- registrar dispositivos IoT por lote
- recibir telemetría de sensores de ambiente y suelo
- calcular diagnósticos y alertas configurables
- ingestión de clima externo vía Open-Meteo
- visualizar el histórico en dashboards interactivos
- exportar datos a CSV

## Estructura principal
- `agrotech/` → backend Spring Boot
- `agrotech-frontend/` → frontend React + Vite
- `docker-compose.yml` → define backend, frontend, PostgreSQL y MongoDB
- `scripts/deploy.sh` → script de despliegue integrado
- `agrotech/esp32/telemetria.ino` → código para el dispositivo ESP32

## Ejecutar la aplicación localmente
1. Clona el repositorio:
   ```bash
   git clone <repo-url>
   cd CJ-AGROTECH
   ```

2. Copia el archivo de entorno:
   ```bash
   cp .env.example .env
   ```

3. Ajusta `.env` si necesitas cambiar credenciales o URL del API.

4. Despliega con Docker Compose:
   ```bash
   bash scripts/deploy.sh dev
   ```

5. Para producción rápida:
   ```bash
   bash scripts/deploy.sh prod
   ```

6. Accede a la aplicación:
   - Frontend: `http://localhost:3000`
   - Backend API: `http://localhost:8080`
   - PostgreSQL: `localhost:5432`
   - MongoDB: `localhost:27017`

## Comandos útiles
- Iniciar en primer plano:
  ```bash
  docker compose up --build
  ```
- Detener:
  ```bash
  docker compose down --remove-orphans
  ```
- Ver logs:
  ```bash
  docker compose logs -f backend frontend
  ```

## Endpoints principales
### Autenticación
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### Gestión de fincas y lotes
- `GET /api/fincas`
- `GET /api/fincas/{id}`
- `POST /api/fincas`
- `PUT /api/fincas/{id}`
- `POST /api/fincas/{id}/cargar-clima`
- `GET /api/lotes`
- `GET /api/lotes/finca/{fincaId}`
- `POST /api/lotes`
- `PUT /api/lotes/{id}`
- `POST /api/lotes/{id}/cargar-clima`

### Telemetría
- `POST /api/telemetria/captura`
- `GET /api/telemetria/dispositivo/{dispositivoId}`

### Dashboard y exportación
- `GET /api/dashboard/historico/{dispositivoId}`
- `GET /api/dashboard/eficiencia-hidrica/{dispositivoId}`
- `GET /api/dashboard/exportar/{dispositivoId}`

## Frontend
- `src/pages/Landing.jsx`
- `src/pages/Login.jsx`
- `src/pages/Register.jsx`
- `src/pages/Dashboard.jsx`
- `src/pages/Fincas.jsx`
- `src/pages/Lotes.jsx`
- `src/pages/Dispositivos.jsx`
- `src/pages/Cultivos.jsx`
- `src/pages/Alertas.jsx`

## Backend
- `src/main/java/com/cj/agrotech/controller/` → API REST
- `src/main/java/com/cj/agrotech/service/` → lógica de negocio
- `src/main/java/com/cj/agrotech/repository/` → acceso a PostgreSQL/MongoDB
- `src/main/java/com/cj/agrotech/dto/` → contratos de datos
- `src/main/java/com/cj/agrotech/domain/` → entidades y documentos

## Notas importantes
- El frontend se comunica con el backend a través de `VITE_API_URL`.
- El backend usa JWT y solo permite acceso público a login, registro y captura de telemetría.
- El script `scripts/deploy.sh` prepara y arranca todos los servicios.

## Eliminado
- Se han eliminado `AUDIT_REPORT.md` y `OPTIMIZATIONS.md` para mantener el repositorio limpio y centrado en el código activo.
