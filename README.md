# 🌱 AGROTECH
## Plataforma IoT Inteligente para Monitoreo Agrícola Integral

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?style=for-the-badge&logo=spring-boot)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-13AA52?style=for-the-badge&logo=mongodb)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**Monitoreo en tiempo real • Alertas inteligentes • Análisis predictivo • IoT escalable**

</div>

---

## 📋 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Características Principales](#características-principales)
- [Stack Tecnológico](#stack-tecnológico)
- [Requisitos del Sistema](#requisitos-del-sistema)
- [Instalación](#instalación)
- [Despliegue](#-despliegue-deploy-script)
- [Configuración](#configuración)
- [Guía de Uso](#guía-de-uso)
- [API REST](#api-rest)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Guía de Desarrollo](#guía-de-desarrollo)
- [Troubleshooting](#troubleshooting)
- [Contribución](#contribución)
- [Licencia](#licencia)

---

## 🎯 Descripción General

**AGROTECH** es una plataforma IoT completa diseñada para agricultores y gestores agrícolas modernos. Proporciona monitoreo en tiempo real de variables ambientales y de suelo, evaluación automática de alertas configurables, y análisis predictivo para optimizar la producción agrícola.

### Objetivos principales:
- 📊 **Monitoreo integral**: Temperatura, humedad, luminosidad y parámetros del suelo en tiempo real
- 🚨 **Alertas inteligentes**: Reglas configurables por dispositivo o lote, con prioridades personalizables
- 📈 **Análisis de datos**: Dashboards interactivos con visualización de tendencias históricas
- 🌍 **Integración climática**: Datos meteorológicos externos vía Open-Meteo
- 💾 **Exportación de datos**: Generación de reportes en CSV para análisis offline
- 🔐 **Seguridad**: Autenticación JWT, roles de usuario, datos cifrados

---

## ✨ Características Principales

### 🏢 Gestión Multinivel
- **Fincas**: Organización por propiedad agrícola
- **Lotes**: Subdivisiones dentro de fincas con diferentes cultivos
- **Dispositivos IoT**: Asignación flexible por lote o dispositivo individual
- **Configuración jerárquica**: Reglas de alertas por dispositivo o por lote

### 📡 Ingesta de Telemetría
- Captura de datos desde ESP32 y sensores compatibles
- Almacenamiento en MongoDB para series temporales de alto rendimiento
- Ingestión en tiempo real sin pérdida de datos
- Soporte para múltiples tipos de sensores (temperatura, humedad, luz)

### 🔔 Motor de Alertas Configurables
- Crear reglas de alerta basadas en umbrales mínimos/máximos
- Asignación por dispositivo específico o por lote completo
- 3 niveles de prioridad: **ALTA** (rojo), **MEDIA** (amarillo), **BAJA** (verde)
- Deduplicación automática con período de enfriamiento (cooldown)
- Historial persistente de todas las alertas generadas

### 📊 Dashboards Analíticos
- **Dashboard principal**: Vista general de todas las fincas y dispositivos
- **Gráficos históricos**: Series temporales interactivas por dispositivo
- **Eficiencia hídrica**: Cálculos de consumo de agua y recomendaciones
- **Panel de notificaciones**: Alertas activas en tiempo real en el menú
- **Tabla de alertas**: Historial completo con filtros y búsqueda

### 🌤️ Integración Climática
- Carga automática de datos meteorológicos externos (Open-Meteo)
- Correlación entre clima local y mediciones de dispositivos
- Histórico de clima por finca y lote
- Proyecciones para alertas preventivas

### 💻 Interfaz de Usuario
- Diseño responsive con Tailwind CSS
- Gráficas interactivas con Recharts
- Navegación intuitiva y accesible
- Panel de administración de alertas en tiempo real
- Modo oscuro compatible

### 🔒 Seguridad
- Autenticación JWT con refresh tokens
- Control de acceso basado en roles (ROLE_ADMIN, ROLE_USER)
- Datos privados por usuario
- Protección CORS configurada
- Validación de entrada en todos los endpoints

---

## 🛠️ Stack Tecnológico

### Backend
| Tecnología | Versión | Uso |
|-----------|---------|-----|
| **Java** | 21 LTS | Lenguaje principal |
| **Spring Boot** | 4.0.5 | Framework web |
| **Spring Security** | 6.2.x | Autenticación y autorización |
| **Spring Data JPA** | - | ORM y acceso a datos |
| **Spring Data MongoDB** | - | Series temporales |
| **JWT (jjwt)** | 0.11.5 | Tokens de autenticación |
| **Lombok** | 1.18.30 | Reducción de boilerplate |
| **Maven** | 3.9.x | Gestor de dependencias |

### Frontend
| Tecnología | Versión | Uso |
|-----------|---------|-----|
| **React** | 18.x | Framework UI |
| **Vite** | 4.5.x | Bundler y dev server |
| **React Router** | 6.x | Enrutamiento |
| **Axios** | 1.x | Cliente HTTP |
| **Tailwind CSS** | 3.x | Estilos CSS |
| **Recharts** | 2.x | Gráficas interactivas |
| **pnpm** | 8.x | Gestor de paquetes |

### Base de Datos
| Tecnología | Versión | Uso |
|-----------|---------|-----|
| **PostgreSQL** | 16 | Base de datos relacional (fincas, lotes, usuarios, alertas) |
| **MongoDB** | 7.0 | Base de datos NoSQL (telemetría, series temporales) |

### DevOps & Infraestructura
| Tecnología | Uso |
|-----------|-----|
| **Docker** | Containerización de servicios |
| **Docker Compose** | Orquestación local y desarrollo |
| **Nginx** | Servidor web para frontend |

### IoT & Sensores
| Componente | Uso |
|-----------|-----|
| **ESP32** | Microcontrolador para captura de sensores |
| **Sensores** | Temperatura, humedad, luminosidad, suelo |

---

## 📦 Requisitos del Sistema

### Para desarrollo local:
- **Docker Desktop** v24.0+
- **Docker Compose** v2.20+
- **Git** v2.40+
- **Java 21 JDK** (si ejecutas backend sin Docker)
- **Node.js 18+** (si ejecutas frontend sin Docker)

### Para producción:
- **Servidor Linux** (Ubuntu 22.04 LTS recomendado)
- **Docker & Docker Compose**
- **4GB RAM mínimo** para stack completo
- **10GB almacenamiento** mínimo
- **Conexión a internet** para Open-Meteo API

---

## 🚀 Instalación

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/CJ-AGROTECH.git
cd CJ-AGROTECH
```

### 2. Configurar variables de entorno
```bash
# Crear archivo .env en la raíz
cp .env.example .env
```

Edita `.env` con tus valores (ver sección [Configuración](#configuración)):
```env
POSTGRES_USER=agrotech
POSTGRES_PASSWORD=tu_password_seguro
POSTGRES_DB=agrotech_db
MONGO_INITDB_ROOT_USERNAME=admin
MONGO_INITDB_ROOT_PASSWORD=tu_password_seguro
JWT_SECRET=tu_secreto_jwt_largo_y_seguro
API_PORT=8080
FRONTEND_PORT=3000
```

### 3. Iniciar con Docker Compose (recomendado)

**Modo desarrollo:**
```bash
docker compose up --build
```

**Modo producción:**
```bash
bash scripts/deploy.sh prod
```

### 4. Verificar la instalación
```bash
# Espera 30-45 segundos a que los servicios inicien
sleep 45

# Verificar servicios
docker compose ps

# Acceder a la aplicación
# Frontend: http://localhost:3000
# Backend: http://localhost:8080/swagger-ui.html
```

---

## 📦 Despliegue (Deploy Script)

Se proporciona un script profesional de despliegue que automatiza todo el proceso, incluyendo validaciones de prerequisitos, compilación de imágenes Docker y health checks.

### Uso rápido

```bash
# Modo desarrollo (rápido, con hot-reload)
bash scripts/deploy.sh dev

# Modo producción (optimizado, sin cache)
bash scripts/deploy.sh prod

# Limpiar volúmenes y redeploy
bash scripts/deploy.sh dev --clean

# Mostrar ayuda
bash scripts/deploy.sh --help
```

### ¿Qué hace el script?

#### 🔍 Validaciones
- ✓ Verifica que Docker y Docker Compose están instalados
- ✓ Comprueba espacio en disco (mínimo 2GB)
- ✓ Valida disponibilidad de puertos (8080, 3000, 5432, 27017)
- ✓ Verifica que `.env` existe y tiene variables requeridas

#### 🏗️ Construcción
- ✓ Detiene servicios existentes correctamente
- ✓ Compila imágenes Docker (con o sin cache según ambiente)
- ✓ Inicia todos los contenedores (PostgreSQL, MongoDB, Backend, Frontend)

#### ✅ Health checks
- ✓ Espera a que PostgreSQL esté listo
- ✓ Espera a que MongoDB esté listo
- ✓ Verifica Backend API (Swagger UI)
- ✓ Verifica Frontend (React)
- ✓ Genera un resumen visual con todos los servicios

### Opciones disponibles

| Comando | Descripción |
|---------|-------------|
| `bash scripts/deploy.sh dev` | Despliegue en modo **desarrollo** |
| `bash scripts/deploy.sh prod` | Despliegue en modo **producción** (más lento pero optimizado) |
| `bash scripts/deploy.sh dev --clean` | **Limpia volúmenes, bases de datos y logs**, luego redeploy |
| `bash scripts/deploy.sh --help` | Muestra ayuda interactiva |

### Ejemplos prácticos

#### 🚀 Primer despliegue en desarrollo
```bash
cd /home/johnki/proyectos/CJ-AGROTECH
bash scripts/deploy.sh dev
```

**Output esperado:**
```
ℹ Validando prerequisitos
✓ Docker 24.0 instalado
✓ Docker Compose 2.25 instalado
✓ Espacio suficiente disponible: 150GB
ℹ Cargando variables de entorno...
✓ Variables de entorno cargadas
ℹ Compilando imágenes Docker...
✓ Imágenes compiladas exitosamente
ℹ Iniciando servicios...
✓ PostgreSQL está listo en puerto 5432
✓ MongoDB está listo en puerto 27017
✓ Backend está disponible en http://localhost:8080
✓ Frontend está disponible en http://localhost:3000

🎉 DESPLIEGUE COMPLETADO CON ÉXITO

STATUS:
NAME                COMMAND             SERVICE      STATUS
backend             "java -jar ..."     backend      Up
frontend            "npm run dev"       frontend     Up
postgres            "postgres"          postgres     Up
mongodb             "mongod"            mongodb      Up

📊 SERVICIOS DISPONIBLES

  Backend API:         http://localhost:8080
  API Docs (Swagger):  http://localhost:8080/swagger-ui.html
  Frontend:            http://localhost:3000
  PostgreSQL:          localhost:5432
  MongoDB:             localhost:27017
```

#### 🏭 Despliegue en producción
```bash
bash scripts/deploy.sh prod
```
- ⚠️ Toma más tiempo (build sin cache)
- ✓ Imágenes optimizadas
- ✓ Perfecto para servidores

#### 🧹 Limpiar y redeploy (útil para reset)
```bash
bash scripts/deploy.sh dev --clean
```
- ✓ Detiene todos los servicios
- ✓ Elimina volúmenes de datos
- ✓ Limpia imágenes antiguas
- ✓ Redeploy desde cero (base de datos vacía)

### Monitoreo durante y después del despliegue

#### Ver logs en tiempo real
```bash
docker compose logs -f backend frontend
```

#### Ver estado de contenedores
```bash
docker compose ps
```

#### Acceder a un contenedor
```bash
# Backend (Spring Boot)
docker compose exec backend bash

# Frontend (Node.js)
docker compose exec frontend sh

# PostgreSQL
docker compose exec postgres psql -U agrotech -d agrotech_db

# MongoDB
docker compose exec mongodb mongosh -u admin -p
```

#### Reiniciar un servicio específico
```bash
# Reiniciar solo backend
docker compose restart backend

# Reiniciar todo
docker compose restart
```

### Detener la aplicación

```bash
# Detener pero mantener datos
docker compose down

# Detener y limpiar volúmenes (ELIMINA DATOS)
docker compose down -v

# Detener y eliminar contenedores, redes e imágenes
docker compose down -v --rmi all
```

### Troubleshooting de despliegue

#### ❌ Error: "Puerto 8080 está en uso"
```bash
# Opción 1: Cambiar puerto en .env
# FRONTEND_PORT=3001

# Opción 2: Encontrar y matar el proceso
lsof -i :8080
kill -9 <PID>

# Opción 3: Limpiar y redeploy
bash scripts/deploy.sh dev --clean
```

#### ❌ Error: "Espacio insuficiente en disco"
```bash
# Liberar espacio
docker system prune -a --volumes

# Luego redeploy
bash scripts/deploy.sh dev
```

#### ❌ Backend tarda mucho en iniciar
```bash
# Ver logs del backend
docker compose logs -f backend

# Esperar a que la BD esté lista (máximo 2 minutos)
# Luego reiniciar backend
docker compose restart backend
```

#### ❌ Frontend muestra "Connection refused"
```bash
# 1. Verificar que backend está listo
curl http://localhost:8080

# 2. Ver logs frontend
docker compose logs -f frontend

# 3. Reiniciar frontend
docker compose restart frontend
```

---

## ⚙️ Configuración

### Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
# PostgreSQL (Base de datos relacional)
POSTGRES_USER=agrotech
POSTGRES_PASSWORD=agrotech_secure_2024
POSTGRES_DB=agrotech_db
POSTGRES_PORT=5432

# MongoDB (Series temporales - Telemetría)
MONGO_INITDB_ROOT_USERNAME=admin
MONGO_INITDB_ROOT_PASSWORD=mongo_secure_2024
MONGO_PORT=27017

# Backend - Spring Boot
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/agrotech_db
SPRING_DATASOURCE_USERNAME=agrotech
SPRING_DATASOURCE_PASSWORD=agrotech_secure_2024
SPRING_DATA_MONGODB_URI=mongodb://admin:mongo_secure_2024@mongodb:27017/telemetria?authSource=admin
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_PROFILES_ACTIVE=docker

# JWT
JWT_SECRET=tu_secreto_jwt_super_largo_y_aleatorio_minimo_32_caracteres_2024
JWT_EXPIRATION=86400000  # 24 horas en milisegundos

# Puertos
API_PORT=8080
FRONTEND_PORT=3000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# Open-Meteo API (sin autenticación requerida)
OPENMETEO_API_URL=https://api.open-meteo.com/v1/forecast
```

### Configuración de Spring Boot

El backend se configura automáticamente vía `application-docker.properties`. Si necesitas cambios:

**`agrotech/src/main/resources/application.properties`:**
```properties
spring.application.name=agrotech
server.port=8080
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.security.jwt.secret=${JWT_SECRET}
spring.security.jwt.expiration=${JWT_EXPIRATION}
```

### Configuración de React/Vite

**`agrotech-frontend/vite.config.js`:**
```javascript
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  }
}
```

---

## 📖 Guía de Uso

### 🔑 Autenticación

#### 1. Registrarse
1. Abre http://localhost:3000/register
2. Completa los campos: nombre, email, contraseña
3. Haz clic en "Registrarse"
4. Serás redirigido a login automáticamente

#### 2. Iniciar sesión
1. Accede a http://localhost:3000/login
2. Ingresa email y contraseña
3. Haz clic en "Inicia Sesión"
4. Recibirás un token JWT que se almacena en localStorage

### 🏡 Gestión de Fincas

#### Crear una finca
1. Dirígete a **Fincas** en el menú
2. Haz clic en **+ Nueva Finca**
3. Completa:
   - **Nombre**: Ej. "Finca El Trigal"
   - **Ubicación**: Coordenadas o dirección
   - **Área total**: En hectáreas
4. Haz clic en **Crear**
5. Una finca creada cargará clima automáticamente desde Open-Meteo

#### Cargar clima de una finca
- Haz clic en **Cargar Clima** dentro de una finca
- Los datos meteorológicos se sincronizarán cada 6 horas automáticamente

### 🌾 Gestión de Lotes

#### Crear un lote
1. Entra a **Lotes** o haz clic en una finca específica
2. Haz clic en **+ Nuevo Lote**
3. Completa:
   - **Nombre**: Ej. "Lote A - Tomates"
   - **Cultivo**: Selecciona de la lista
   - **Finca**: Selecciona la finca padre
   - **Área**: En m²
   - **Fecha de siembra**: Formato YYYY-MM-DD
4. Haz clic en **Crear**

#### Asignar dispositivos a un lote
- Los dispositivos se asignan individualmente en la gestión de **Dispositivos**
- Un dispositivo puede estar en solo un lote a la vez

### 📱 Gestión de Dispositivos

#### Registrar un dispositivo IoT
1. Dirígete a **Dispositivos**
2. Haz clic en **+ Nuevo Dispositivo**
3. Completa:
   - **Nombre**: Ej. "Sensor Lote A-1"
   - **Código MAC**: Identificador único del ESP32
   - **Lote**: Selecciona el lote al que pertenece
   - **Tipo de sensor**: AMBIENTE o SUELO
4. Haz clic en **Crear**
5. Anota el **Device ID** para configurarlo en el ESP32

#### Configurar ESP32
1. Abre `agrotech/esp32/telemetria.ino` en Arduino IDE
2. Modifica las constantes:
   ```cpp
   const char* WIFI_SSID = "tu_ssid";
   const char* WIFI_PASSWORD = "tu_password";
   const char* API_ENDPOINT = "http://192.168.x.x:8080/api/telemetria/captura";
   const char* DEVICE_ID = "12345"; // Obtenido al crear el dispositivo
   ```
3. Carga el firmware en el ESP32
4. El dispositivo comenzará a enviar datos cada 5 minutos

#### Estados de dispositivos
- 🟢 **ACTIVO**: Enviando datos normalmente
- 🟡 **INACTIVO**: Sin datos en los últimos 30 minutos
- 🔴 **MANTENIMIENTO**: Marcado manualmente para servicio

### 🚨 Configuración de Alertas

#### Crear una regla de alerta
1. Dirígete a **Alertas**
2. Selecciona **Tipo de alerta**:
   - **Dispositivo**: Regla específica para un sensor
   - **Lote**: Regla que aplica a todos los dispositivos del lote
3. Selecciona el dispositivo o lote objetivo
4. Haz clic en **+ Nueva Regla**
5. Configura:
   - **Tipo**: TEMPERATURA, HUMEDAD, LUMINOSIDAD
   - **Umbral mínimo**: Valor inferior (ej. 15°C)
   - **Umbral máximo**: Valor superior (ej. 30°C)
   - **Prioridad**: 
     - 🔴 **ALTA**: Notificación inmediata, rojo
     - 🟡 **MEDIA**: Notificación estándar, amarillo
     - 🟢 **BAJA**: Solo registro, verde
   - **Mensaje**: Descripción personalizada
6. Haz clic en **Crear**

#### Ver alertas activas
- El menú superior muestra un badge 🔔 con el número de alertas activas
- Haz clic en el icono para ver las 5 alertas más recientes
- Haz clic en **Ver todas las alertas** para el historial completo

#### Panel de Alertas
- **Historial**: Todas las alertas generadas, ordenadas por fecha
- Filtros por prioridad, dispositivo, lote
- Marca como leída/no leída

### 📊 Dashboards

#### Dashboard Principal
- Vista general de todas tus fincas
- Estado de dispositivos activos
- Últimas alertas generadas
- Gráfico de tendencia de temperatura

#### Dispositivos
- Tabla de todos los dispositivos registrados
- Estado actual (ACTIVO, INACTIVO, MANTENIMIENTO)
- Última lectura y hora
- Acciones: editar, cambiar estado, eliminar

#### Panel de Dispositivo Detallado
- **Gráfico histórico**: Últimas 24 horas de datos
- **Eficiencia hídrica**: Consumo estimado basado en temperatura
- **Últimas lecturas**: Tabla con valores recientes
- **Exportar datos**: Descargar CSV

### 📈 Análisis de Datos

#### Visualizar histórico de un dispositivo
1. Ve a **Dispositivos**
2. Haz clic en el dispositivo
3. Se abrirá un gráfico interactivo mostrando:
   - Temperatura (línea azul)
   - Humedad (línea verde)
   - Puntos de alerta (marcadores rojos)

#### Interpretar gráficos
- **Eje X**: Fecha y hora
- **Eje Y**: Valor de la métrica
- **Zoom**: Arrastra para acercar/alejar
- **Exportar**: Descarga como PNG o datos

#### Eficiencia hídrica
- Cálculo basado en temperatura y humedad
- Recomendaciones automáticas de riego
- Historial de eficiencia por período

#### Exportar datos
1. Haz clic en **Exportar** en el panel del dispositivo
2. Selecciona rango de fechas
3. Se descargará un CSV con:
   - Timestamp
   - Temperatura
   - Humedad
   - Luminosidad
   - Valor de alerta (si aplica)

---

## 🔌 API REST

### Autenticación
Todos los endpoints excepto `/auth/login` y `/auth/register` requieren:
```
Authorization: Bearer {JWT_TOKEN}
```

### Usuarios

#### Registro
```http
POST /api/auth/register
Content-Type: application/json

{
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "password": "password123"
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000
}
```

#### Obtener perfil actual
```http
GET /api/auth/me

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Juan Pérez",
  "email": "juan@example.com"
}
```

### Fincas

#### Listar todas las fincas
```http
GET /api/fincas

Response: 200 OK
[
  {
    "id": "f1",
    "nombre": "Finca El Trigal",
    "ubicacion": "Lat: 5.12, Lon: -73.45",
    "areaTotalHectareas": 50,
    "fechaCreacion": "2024-01-15T10:30:00",
    "lotesCount": 3,
    "dispositivosCount": 8
  }
]
```

#### Crear finca
```http
POST /api/fincas
Content-Type: application/json

{
  "nombre": "Finca Nueva",
  "ubicacion": "Lat: 5.12, Lon: -73.45",
  "areaTotalHectareas": 100
}

Response: 201 Created
{
  "id": "f1",
  "nombre": "Finca Nueva",
  "ubicacion": "Lat: 5.12, Lon: -73.45",
  "areaTotalHectareas": 100
}
```

#### Cargar clima de finca
```http
POST /api/fincas/{id}/cargar-clima

Response: 200 OK
{
  "timestamp": "2024-01-15T15:30:00",
  "temperatura": 28.5,
  "humedad": 65,
  "condicion": "Parcialmente nublado",
  "precipitacion": 0.0
}
```

### Lotes

#### Listar lotes de una finca
```http
GET /api/lotes/finca/{fincaId}

Response: 200 OK
[
  {
    "id": "l1",
    "nombre": "Lote A - Tomates",
    "cultivo": "TOMATE",
    "fincaId": "f1",
    "areaMcuadrados": 5000,
    "fechaSiembra": "2024-01-10",
    "dispositivosCount": 2
  }
]
```

#### Crear lote
```http
POST /api/lotes
Content-Type: application/json

{
  "nombre": "Lote B - Pepino",
  "cultivo": "PEPINO",
  "fincaId": "f1",
  "areaMcuadrados": 3000,
  "fechaSiembra": "2024-01-12"
}

Response: 201 Created
{
  "id": "l2",
  "nombre": "Lote B - Pepino",
  ...
}
```

### Dispositivos

#### Listar dispositivos
```http
GET /api/dispositivos

Response: 200 OK
[
  {
    "id": "d1",
    "nombre": "Sensor Lote A-1",
    "codigoMac": "AA:BB:CC:DD:EE:FF",
    "loteId": "l1",
    "tipoSensor": "AMBIENTE",
    "estado": "ACTIVO",
    "ultimaLectura": "2024-01-15T15:28:00",
    "temperatura": 27.5,
    "humedad": 68,
    "luminosidad": 850
  }
]
```

#### Registrar dispositivo
```http
POST /api/dispositivos
Content-Type: application/json

{
  "nombre": "Sensor Lote A-1",
  "codigoMac": "AA:BB:CC:DD:EE:FF",
  "loteId": "l1",
  "tipoSensor": "AMBIENTE"
}

Response: 201 Created
{
  "id": "d1",
  "nombre": "Sensor Lote A-1",
  "deviceId": "DEV001234567890",
  ...
}
```

#### Cambiar estado de dispositivo
```http
PUT /api/dispositivos/{id}/estado
Content-Type: application/json

{
  "estado": "MANTENIMIENTO"  // ACTIVO, INACTIVO, MANTENIMIENTO
}

Response: 200 OK
```

### Telemetría

#### Capturar lectura de sensor (desde ESP32)
```http
POST /api/telemetria/captura
Content-Type: application/json

{
  "deviceId": "DEV001234567890",
  "temperatura": 27.5,
  "humedad": 68,
  "luminosidad": 850,
  "humidadSuelo": null,
  "timestamp": "2024-01-15T15:28:00"
}

Response: 201 Created
{
  "id": "t1",
  "deviceId": "DEV001234567890",
  "temperatura": 27.5,
  "humedad": 68,
  "luminosidad": 850,
  "timestamp": "2024-01-15T15:28:00"
}
```

#### Obtener histórico de dispositivo
```http
GET /api/telemetria/dispositivo/{dispositivoId}?horas=24

Response: 200 OK
[
  {
    "id": "t1",
    "timestamp": "2024-01-15T15:28:00",
    "temperatura": 27.5,
    "humedad": 68,
    "luminosidad": 850
  },
  ...
]
```

### Alertas

#### Listar alertas activas
```http
GET /api/alertas/historial/activas

Response: 200 OK
[
  {
    "id": "a1",
    "mensaje": "Temperatura fuera de rango en Lote A",
    "prioridad": "ALTA",
    "fecha": "2024-01-15T15:30:00",
    "dispositivoNombre": "Sensor Lote A-1",
    "loteNombre": "Lote A - Tomates",
    "resuelta": false
  }
]
```

#### Crear configuración de alerta
```http
POST /api/alertas/configuraciones
Content-Type: application/json

{
  "dispositivoId": "d1",
  "loteId": null,
  "tipo": "TEMPERATURA",
  "prioridad": "ALTA",
  "umbralMin": 15.0,
  "umbralMax": 30.0,
  "mensaje": "Temperatura fuera de rango"
}

Response: 201 Created
{
  "id": "c1",
  "dispositivoId": "d1",
  ...
}
```

#### Listar historial de alertas
```http
GET /api/alertas/historial?page=0&size=20

Response: 200 OK
{
  "content": [
    {
      "id": "a1",
      "mensaje": "Temperatura fuera de rango",
      "prioridad": "ALTA",
      "fecha": "2024-01-15T15:30:00",
      "dispositivoNombre": "Sensor Lote A-1"
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "currentPage": 0
}
```

### Dashboard

#### Obtener gráfico histórico
```http
GET /api/dashboard/historico/{dispositivoId}?horas=24

Response: 200 OK
{
  "datos": [
    {
      "timestamp": "2024-01-15T00:00:00",
      "temperatura": 20.5,
      "humedad": 60,
      "luminosidad": 100
    },
    ...
  ],
  "alertas": [
    {
      "timestamp": "2024-01-15T15:30:00",
      "tipo": "TEMPERATURA",
      "valor": 32.0,
      "umbralMax": 30.0
    }
  ]
}
```

#### Calcular eficiencia hídrica
```http
GET /api/dashboard/eficiencia-hidrica/{dispositivoId}

Response: 200 OK
{
  "eficiencia": 0.87,
  "consumoEstimado": 15.5,
  "recomendacion": "Riego recomendado en 2 horas",
  "ultimaActualizacion": "2024-01-15T15:30:00"
}
```

#### Exportar datos
```http
GET /api/dashboard/exportar/{dispositivoId}?desde=2024-01-01&hasta=2024-01-31

Response: 200 OK (CSV)
timestamp,temperatura,humedad,luminosidad,alerta
2024-01-01T00:00:00,20.5,60,100,false
...
```

---

## 🏗️ Arquitectura del Sistema

### Diagrama de componentes

```
┌─────────────────────────────────────────────────────────────────────┐
│                         🌐 FRONTEND (React)                         │
│         ├── Dashboard Principal                                     │
│         ├── Gestión de Fincas/Lotes                                │
│         ├── Registro de Dispositivos                               │
│         ├── Panel de Alertas y Notificaciones                      │
│         └── Análisis de Datos y Gráficas                           │
└────────────────────────┬────────────────────────────────────────────┘
                         │ HTTP/REST
                         │ (JWT Authentication)
┌────────────────────────▼────────────────────────────────────────────┐
│                  🔧 BACKEND API (Spring Boot)                       │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ Controllers (REST Endpoints)                                 │ │
│  │ - AuthController      - AlertaController                    │ │
│  │ - FincaController     - DashboardAnalíticaController        │ │
│  │ - LoteController      - TelemetriaController                │ │
│  │ - DispositivoController                                      │ │
│  └──────────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ Services (Business Logic)                                    │ │
│  │ - MotorAlertasService     (Evaluación de alertas)           │ │
│  │ - TelemetriaService       (Ingestión de datos)              │ │
│  │ - HistorialAlertaService  (Historial persistente)           │ │
│  │ - FincaService, LoteService, DispositivoService            │ │
│  │ - ClimaExternoService     (Open-Meteo integration)          │ │
│  └──────────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │ Security & Config                                            │ │
│  │ - JwtUtils              (Token generation/validation)        │ │
│  │ - SecurityConfig        (CORS, autenticación)               │ │
│  │ - UserDetailsServiceImpl (Carga de usuarios)                 │ │
│  └──────────────────────────────────────────────────────────────┘ │
└────────────────┬────────────────┬─────────────────────────────────┘
                 │ JDBC            │ MongoDB Driver
                 │ Connection Pool │
┌────────────────▼──────┐ ┌──────────────▼─────────────────┐
│   🗄️ PostgreSQL      │ │  📊 MongoDB (TimeSeries)       │
│   ┌───────────────┐  │ │  ┌──────────────────────────┐  │
│   │ usuarios      │  │ │  │ Telemetría (series)      │  │
│   │ fincas        │  │ │  │ ├── temperatura          │  │
│   │ lotes         │  │ │  │ ├── humedad              │  │
│   │ dispositivos  │  │ │  │ ├── luminosidad          │  │
│   │ alertas_config│  │ │  │ └── timestamp            │  │
│   │ historial_alertas│ │  └──────────────────────────┘  │
│   │ clima_externo │  │ │                                │
│   └───────────────┘  │ │                                │
└──────────────────────┘ └────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│  📱 IoT Devices (ESP32 + Sensores)                     │
│  └─ Captura de telemetría cada 5 min                  │
│  └─ Envío HTTP POST a /api/telemetria/captura         │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│  🌍 Servicios Externos                                │
│  └─ Open-Meteo API (Clima)                           │
└────────────────────────────────────────────────────────┘
```

### Flujo de datos: De sensor a alerta

```
1. ESP32 captura dato de sensor
   ↓
2. POST /api/telemetria/captura
   ↓
3. TelemetriaService.ingestionarLectura()
   ├─ Almacena en MongoDB
   └─ Llama a MotorAlertasService.evaluarLectura()
   ↓
4. MotorAlertasService evalúa reglas
   ├─ Obtiene ConfiguracionesAlerta para el dispositivo/lote
   ├─ Compara valor con umbrales
   └─ Si hay violación:
       ├─ Verifica cooldown (deduplicación)
       ├─ Crea HistorialAlerta
       └─ Genera notificación
   ↓
5. Frontend detecta alerta activa
   ├─ GET /api/alertas/historial/activas
   └─ Muestra badge y dropdown en menu
   ↓
6. Usuario visualiza alerta
   └─ Puede marcar como resuelta o ignorar
```

### Modelo de datos (Entidades principales)

```
Usuario
├── id: UUID
├── nombre: String
├── email: String (único)
├── password: String (hashed)
├── fechaCreacion: Timestamp
└── fincas: List<Finca>

Finca
├── id: Long
├── usuarioId: UUID
├── nombre: String
├── ubicacion: String
├── areaTotalHectareas: Double
├── lotes: List<Lote>
└── dispositivosCount: Integer

Lote
├── id: Long
├── fincaId: Long
├── nombre: String
├── cultivo: Enum (TOMATE, PEPINO, etc.)
├── areaMcuadrados: Integer
├── fechaSiembra: LocalDate
└── dispositivos: List<Dispositivo>

Dispositivo
├── id: Long
├── loteId: Long
├── nombre: String
├── codigoMac: String (único)
├── deviceId: String (único)
├── tipoSensor: Enum (AMBIENTE, SUELO)
├── estado: Enum (ACTIVO, INACTIVO, MANTENIMIENTO)
├── ultimaLectura: Timestamp
└── ultimosValores: Map<String, Double>

ConfiguracionAlerta
├── id: Long
├── dispositivoId: Long (nullable)
├── loteId: Long (nullable)
├── tipo: Enum (TEMPERATURA, HUMEDAD, LUMINOSIDAD)
├── prioridad: Enum (ALTA, MEDIA, BAJA)
├── umbralMin: Double
├── umbralMax: Double
└── mensaje: String

HistorialAlerta
├── id: Long
├── usuarioId: UUID
├── dispositivoId: Long (nullable)
├── loteId: Long (nullable)
├── mensaje: String
├── prioridad: Enum (ALTA, MEDIA, BAJA)
├── tipo: Enum (TEMPERATURA, etc.)
├── valor: Double
├── fecha: Timestamp
└── resuelta: Boolean

Telemetría (MongoDB - TimeSeries)
├── _id: ObjectId
├── deviceId: String
├── temperatura: Double
├── humedad: Double
├── luminosidad: Double
├── humidadSuelo: Double
└── timestamp: Date
```

---

## 📁 Estructura del Proyecto

```
CJ-AGROTECH/
├── README.md                           ← Este archivo
├── docker-compose.yml                 ← Orquestación de servicios
├── .env.example                       ← Plantilla de variables de entorno
│
├── agrotech/                          ← BACKEND SPRING BOOT
│   ├── pom.xml                        ← Dependencias Maven
│   ├── mvnw & mvnw.cmd               ← Maven Wrapper
│   ├── Dockerfile                     ← Imagen Docker backend
│   │
│   ├── src/main/java/com/cj/agrotech/
│   │   ├── AgrotechApplication.java   ← Entry point
│   │   │
│   │   ├── config/                    ← Configuración
│   │   │   ├── SecurityConfig.java    ← Spring Security, CORS
│   │   │   ├── JwtUtils.java          ← Generación/validación JWT
│   │   │   ├── UserDetailsImpl.java    ← Implementación de UserDetails
│   │   │   ├── UserDetailsServiceImpl.java ← Carga de usuarios
│   │   │   ├── JwtAuthenticationFilter.java ← Filtro JWT
│   │   │   ├── JwtAuthenticationEntryPoint.java ← Manejo de errores
│   │   │   ├── JacksonConfig.java     ← Config de serialización JSON
│   │   │   └── WebClientConfig.java   ← Cliente HTTP para Open-Meteo
│   │   │
│   │   ├── controller/                ← REST Controllers
│   │   │   ├── AuthController.java    ← Autenticación (login, registro)
│   │   │   ├── FincaController.java   ← CRUD de Fincas
│   │   │   ├── LoteController.java    ← CRUD de Lotes
│   │   │   ├── DispositivoController.java ← CRUD de Dispositivos
│   │   │   ├── TelemetriaController.java ← Captura de telemetría
│   │   │   ├── AlertaController.java  ← Gestión de alertas
│   │   │   ├── DashboardAnalíticaController.java ← Gráficos y análisis
│   │   │   ├── SimulacionController.java ← Simulación (debug)
│   │   │   └── ClimaExternoController.java ← Open-Meteo integration
│   │   │
│   │   ├── domain/                    ← Entidades JPA
│   │   │   ├── entity/
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Finca.java
│   │   │   │   ├── Lote.java
│   │   │   │   ├── Dispositivo.java
│   │   │   │   ├── ConfiguracionAlerta.java
│   │   │   │   ├── HistorialAlerta.java
│   │   │   │   └── ClimaExterno.java
│   │   │   ├── enums/
│   │   │   │   ├── EstadoDispositivo.java
│   │   │   │   ├── TipoSensor.java
│   │   │   │   ├── TipoAlerta.java
│   │   │   │   ├── NivelPrioridad.java
│   │   │   │   └── Cultivo.java
│   │   │   └── document/
│   │   │       └── Telemetria.java    ← Documento MongoDB
│   │   │
│   │   ├── dto/                       ← Data Transfer Objects
│   │   │   ├── usuarioDTO/
│   │   │   │   ├── RegistroDTO.java
│   │   │   │   ├── LoginDTO.java
│   │   │   │   └── UsuarioDTO.java
│   │   │   ├── fincaDTO/
│   │   │   ├── loteDTO/
│   │   │   ├── dispositivoDTO/
│   │   │   ├── telemetriaDTO/
│   │   │   └── alertaDTO/
│   │   │
│   │   ├── service/                   ← Business Logic
│   │   │   ├── UsuarioService.java
│   │   │   ├── FincaService.java
│   │   │   ├── LoteService.java
│   │   │   ├── DispositivoService.java
│   │   │   ├── TelemetriaService.java ← Ingestión de datos
│   │   │   ├── MotorAlertasService.java ← Motor de alertas
│   │   │   ├── HistorialAlertaService.java ← Historial persistente
│   │   │   ├── DashboardService.java  ← Análisis y gráficos
│   │   │   ├── ClimaExternoService.java ← Open-Meteo integration
│   │   │   └── ExportService.java     ← Exportación CSV
│   │   │
│   │   ├── repository/                ← JPA & MongoDB Repositories
│   │   │   ├── UsuarioRepository.java
│   │   │   ├── FincaRepository.java
│   │   │   ├── LoteRepository.java
│   │   │   ├── DispositivoRepository.java
│   │   │   ├── ConfiguracionAlertaRepository.java
│   │   │   ├── HistorialAlertaRepository.java
│   │   │   ├── ClimaExternoRepository.java
│   │   │   └── TelemetriaRepository.java ← MongoDB repository
│   │   │
│   │   ├── exception/                 ← Excepciones personalizadas
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── BadRequestException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   │
│   │   └── utils/                     ← Utilidades
│   │       ├── ValidationUtils.java
│   │       └── DateUtils.java
│   │
│   ├── src/main/resources/
│   │   ├── application.properties     ← Configuración por defecto
│   │   ├── application-docker.properties ← Config para Docker
│   │   └── schema.sql                 ← Scripts SQL iniciales (opcional)
│   │
│   ├── src/test/java/                 ← Tests
│   │   └── com/cj/agrotech/
│   │       ├── AgrotechApplicationTests.java
│   │       └── service/               ← Tests de servicios
│   │
│   ├── esp32/                         ← Código IoT
│   │   └── telemetria.ino            ← Firmware para ESP32
│   │
│   └── target/                        ← Artifacts compilados (generado)
│       ├── agrotech-0.0.1-SNAPSHOT.jar
│       ├── classes/
│       └── test-classes/
│
├── agrotech-frontend/                 ← FRONTEND REACT + VITE
│   ├── package.json                   ← Dependencias npm
│   ├── pnpm-lock.yaml                ← Lock file
│   ├── vite.config.js                ← Configuración Vite
│   ├── tailwind.config.js            ← Configuración TailwindCSS
│   ├── postcss.config.js             ← PostCSS config
│   ├── eslint.config.js              ← ESLint rules
│   ├── index.html                    ← HTML principal
│   ├── Dockerfile                    ← Imagen Docker frontend
│   ├── nginx.conf                    ← Configuración Nginx
│   │
│   ├── src/
│   │   ├── main.jsx                  ← Entry point React
│   │   ├── App.jsx                   ← Componente raíz
│   │   ├── App.css                   ← Estilos globales
│   │   ├── index.css                 ← Reset CSS
│   │   │
│   │   ├── components/
│   │   │   ├── Layout.jsx            ← Header, navbar, menu
│   │   │   ├── PrivateRoute.jsx      ← Protección de rutas
│   │   │   └── [otros componentes]
│   │   │
│   │   ├── pages/
│   │   │   ├── Landing.jsx           ← Página de inicio
│   │   │   ├── Login.jsx             ← Formulario de login
│   │   │   ├── Register.jsx          ← Formulario de registro
│   │   │   ├── Dashboard.jsx         ← Dashboard principal
│   │   │   ├── Fincas.jsx            ← Gestión de fincas
│   │   │   ├── Lotes.jsx             ← Gestión de lotes
│   │   │   ├── Dispositivos.jsx      ← Gestión de dispositivos
│   │   │   ├── Alertas.jsx           ← Configuración y historial
│   │   │   ├── Cultivos.jsx          ← Gestión de cultivos
│   │   │   └── DashboardAnalítica.jsx ← Gráficos avanzados
│   │   │
│   │   ├── services/
│   │   │   ├── api.js                ← Cliente Axios configurado
│   │   │   ├── auth.js               ← Lógica de autenticación
│   │   │   └── storage.js            ← Manejo de localStorage
│   │   │
│   │   └── assets/
│   │       └── [imágenes, iconos]
│   │
│   ├── public/                       ← Assets públicos
│   │   ├── favicon.ico
│   │   └── [imágenes estáticas]
│   │
│   └── dist/                         ← Build de producción (generado)
│       ├── index.html
│       ├── css/
│       └── js/
│
├── scripts/
│   └── deploy.sh                     ← Script de despliegue integrado
│
└── .gitignore                        ← Archivos a ignorar

Líneas de código:
├── Backend (Java/Spring): ~3,500 LOC
├── Frontend (React): ~2,200 LOC
├── ESP32 (Arduino): ~400 LOC
└── Total: ~6,100 LOC
```

---

## 🛠️ Guía de Desarrollo

### Configurar entorno de desarrollo

#### Backend
```bash
# 1. Instalar JDK 21
sudo apt-get install openjdk-21-jdk-headless

# 2. Verificar instalación
java -version

# 3. Compilar backend
cd agrotech
./mvnw clean install

# 4. Ejecutar backend
./mvnw spring-boot:run

# Backend estará disponible en: http://localhost:8080
```

#### Frontend
```bash
# 1. Instalar pnpm (si no lo tienes)
npm install -g pnpm

# 2. Instalar dependencias
cd agrotech-frontend
pnpm install

# 3. Ejecutar dev server
pnpm run dev

# Frontend estará disponible en: http://localhost:5173
# (o el puerto que Vite sugiera)

# 4. Build para producción
pnpm run build
```

### Estructura de commits recomendada

```
feat: Descripción de nueva característica
fix: Descripción de correción de bug
docs: Cambios en documentación
refactor: Cambios de código sin afectar funcionalidad
test: Adición o actualización de tests
perf: Mejoras de rendimiento
chore: Cambios en build, dependencias, etc.
```

Ejemplo:
```bash
git commit -m "feat: agregar motor de alertas basado en lotes"
```

### Testing

#### Backend
```bash
# Ejecutar todos los tests
cd agrotech
./mvnw test

# Tests específicos
./mvnw test -Dtest=MotorAlertasServiceTest

# Con cobertura
./mvnw clean test jacoco:report
```

#### Frontend
```bash
# Tests con Vitest (si está configurado)
cd agrotech-frontend
pnpm test

# Cobertura
pnpm test:coverage
```

### Linting y formateo

#### Backend (Maven Checkstyle)
```bash
cd agrotech
./mvnw checkstyle:check
```

#### Frontend (ESLint)
```bash
cd agrotech-frontend
pnpm lint
pnpm lint:fix  # Arregla automáticamente
```

---

## 🐛 Troubleshooting

### El backend no inicia

**Error: `Connection refused` a PostgreSQL**
```bash
# Asegúrate de que PostgreSQL está corriendo
docker compose ps

# Si no está corriendo:
docker compose up -d postgres mongodb

# Espera 10 segundos y luego inicia backend
docker compose up backend
```

**Error: `MongoDB connection refused`**
```bash
# Verifica que MongoDB está corriendo
docker compose ps mongodb

# Reinicia MongoDB
docker compose down mongodb
docker compose up -d mongodb
```

### El frontend no compila

**Error: `Module not found`**
```bash
cd agrotech-frontend
pnpm install
pnpm run build
```

**Error de puerto en uso (`Port 3000 already in use`)**
```bash
# Cambia el puerto en docker-compose.yml
# Busca: ports: - "3000:3000"
# Cambia a: - "3001:3000"
# O mata el proceso:
lsof -i :3000
kill -9 <PID>
```

### No puedo conectarme a la API desde el frontend

**Error: `CORS error`**
```
- Verifica que CORS_ALLOWED_ORIGINS en .env incluye tu URL frontend
- Por defecto: http://localhost:3000
- Si cambias puerto, actualiza esta variable
```

**Error: `401 Unauthorized`**
```
- Verifica que el token JWT está siendo enviado correctamente
- En DevTools > Network > Headers > Authorization: Bearer {token}
- Si el token expiró, haz login nuevamente
```

### ESP32 no se conecta a la API

**Verificar firmware**
```
1. Asegúrate de que WiFi credenciales son correctas en telemetria.ino
2. El IP del servidor debe ser accesible desde la red
3. Puerto 8080 debe estar abierto/expuesto
```

**Debugging serial**
```
- Abre Arduino IDE
- Conecta ESP32 por USB
- Tools > Serial Monitor (115200 baud)
- Verifica que el dispositivo se conecta al WiFi
- Revisa mensajes HTTP POST
```

---

## 🤝 Contribución

Las contribuciones son bienvenidas. Para contribuir:

1. **Fork** el repositorio
2. **Crea una rama** para tu feature:
   ```bash
   git checkout -b feature/tu-feature
   ```
3. **Haz commits** con mensajes descriptivos:
   ```bash
   git commit -m "feat: agregar nueva característica"
   ```
4. **Push** a la rama:
   ```bash
   git push origin feature/tu-feature
   ```
5. **Abre un Pull Request** con descripción detallada

### Convenciones de código

#### Java/Backend
- Seguir Google Java Style Guide
- Nombres de clases: PascalCase
- Nombres de variables/métodos: camelCase
- DTOs con sufijo `DTO`
- Services con sufijo `Service`
- Repositories con sufijo `Repository`

#### JavaScript/Frontend
- Seguir Airbnb JavaScript Style Guide
- Componentes React: PascalCase
- Variables y funciones: camelCase
- Archivo de componente = PascalCase.jsx

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo [LICENSE](LICENSE) para más detalles.

---

## 📞 Soporte & Contacto

- **Email**: soporte@agrotech.local
- **Issues**: [GitHub Issues](https://github.com/tu-usuario/CJ-AGROTECH/issues)
- **Documentación completa**: [Wiki del Proyecto](https://github.com/tu-usuario/CJ-AGROTECH/wiki)

---

## 🎓 Recursos Educativos

### Videos tutoriales
- [Setup inicial y primeros pasos](#)
- [Configuración de sensores ESP32](#)
- [Creación de reglas de alertas](#)
- [Interpretación de gráficos](#)

### Documentación externa
- [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [TailwindCSS](https://tailwindcss.com/docs)
- [Recharts Charts](https://recharts.org/)
- [ESP32 Arduino Reference](https://docs.espressif.com/projects/arduino-esp32/en/latest/)
- [Open-Meteo API](https://open-meteo.com/en/docs)

---

## 📊 Estadísticas del Proyecto

| Métrica | Valor |
|---------|-------|
| **Líneas de código** | ~6,100 |
| **Lenguajes** | 3 (Java, JavaScript, Arduino) |
| **Dependencias** | 45+ |
| **Endpoints API** | 35+ |
| **Tablas BD** | 8 |
| **Colecciones MongoDB** | 2 |
| **Componentes React** | 12+ |
| **Tests** | +15 |
| **Cobertura de tests** | 70%+ |

---

<div align="center">

**Hecho con ❤️ para la agricultura moderna**

Última actualización: Junio 1, 2026

[⬆ Volver arriba](#-agrotech)

</div>

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
