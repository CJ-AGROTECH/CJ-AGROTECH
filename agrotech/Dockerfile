# ===================================
# Build Stage - Compilación
# ===================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copiar solo archivos de configuración (aprovecha caché)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias (se cachea mientras pom.xml no cambie)
RUN ./mvnw dependency:resolve dependency:resolve-plugins -DskipTests

# Copiar código fuente
COPY src src

# Compilar y empaquetar
RUN ./mvnw clean package -DskipTests

# ===================================
# Runtime Stage - Imagen final
# ===================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Crear usuario no-root por seguridad
RUN useradd -m -s /sbin/nologin appuser

# Copiar JAR desde build stage
COPY --from=builder /build/target/agrotech-0.0.1-SNAPSHOT.jar app.jar

# Cambiar propietario del archivo
RUN chown -R appuser:appuser /app

# Cambiar usuario
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
