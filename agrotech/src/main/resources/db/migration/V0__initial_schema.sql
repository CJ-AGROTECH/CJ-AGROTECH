-- Flyway V0: Initial Database Schema
-- Creates all tables and constraints for AGROTECH system

-- ========================================
-- TABLE: usuarios
-- ========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id uuid NOT NULL PRIMARY KEY,
    email character varying(255) NOT NULL UNIQUE,
    nombre character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    rol character varying(255) NOT NULL,
    CONSTRAINT usuarios_rol_check CHECK ((rol::text = ANY (ARRAY['ADMIN'::character varying, 'INGENIERO'::character varying, 'AGRICULTOR'::character varying]::text[])))
);

-- ========================================
-- TABLE: fincas
-- ========================================
CREATE TABLE IF NOT EXISTS fincas (
    id uuid NOT NULL PRIMARY KEY,
    latitud double precision,
    longitud double precision,
    municipio character varying(255) NOT NULL,
    nombre character varying(255) NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT fk_fincas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ========================================
-- TABLE: catalogo_cultivos
-- ========================================
CREATE TABLE IF NOT EXISTS catalogo_cultivos (
    id uuid NOT NULL PRIMARY KEY,
    descripcion character varying(255),
    dias_crecimiento integer,
    humedad_optima_max double precision,
    humedad_optima_min double precision,
    nombre character varying(255) NOT NULL UNIQUE,
    temp_optima_max double precision,
    temp_optima_min double precision,
    variedad character varying(255),
    usuario_id uuid,
    CONSTRAINT fk_catalogo_cultivos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ========================================
-- TABLE: lotes
-- ========================================
CREATE TABLE IF NOT EXISTS lotes (
    id uuid NOT NULL PRIMARY KEY,
    area_hectareas double precision NOT NULL,
    nombre character varying(255) NOT NULL,
    cultivo_id uuid NOT NULL,
    finca_id uuid NOT NULL,
    CONSTRAINT fk_lotes_cultivo FOREIGN KEY (cultivo_id) REFERENCES catalogo_cultivos(id),
    CONSTRAINT fk_lotes_finca FOREIGN KEY (finca_id) REFERENCES fincas(id)
);

-- ========================================
-- TABLE: dispositivos
-- ========================================
CREATE TABLE IF NOT EXISTS dispositivos (
    id uuid NOT NULL PRIMARY KEY,
    estado character varying(255) NOT NULL,
    mac_address character varying(255) NOT NULL UNIQUE,
    nombre character varying(255) NOT NULL,
    ultima_sincronizacion timestamp(6) without time zone,
    lote_id uuid NOT NULL,
    CONSTRAINT fk_dispositivos_lote FOREIGN KEY (lote_id) REFERENCES lotes(id),
    CONSTRAINT dispositivos_estado_check CHECK ((estado::text = ANY (ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying, 'MANTENIMIENTO'::character varying, 'OFFLINE'::character varying]::text[])))
);

-- ========================================
-- TABLE: configuracion_alertas
-- ========================================
CREATE TABLE IF NOT EXISTS configuracion_alertas (
    id uuid NOT NULL PRIMARY KEY,
    condicion character varying(255),
    mensaje character varying(500),
    prioridad character varying(255) NOT NULL,
    umbral double precision,
    umbral_max double precision,
    umbral_min double precision,
    variable character varying(255) NOT NULL,
    dispositivo_id uuid,
    lote_id uuid,
    CONSTRAINT fk_configuracion_alertas_dispositivo FOREIGN KEY (dispositivo_id) REFERENCES dispositivos(id) ON DELETE CASCADE,
    CONSTRAINT fk_configuracion_alertas_lote FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    CONSTRAINT configuracion_alertas_condicion_check CHECK ((condicion::text = ANY (ARRAY['MAYOR_QUE'::character varying, 'MENOR_QUE'::character varying, 'IGUAL_A'::character varying]::text[]))),
    CONSTRAINT configuracion_alertas_prioridad_check CHECK ((prioridad::text = ANY (ARRAY['BAJA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'CRITICA'::character varying]::text[]))),
    CONSTRAINT configuracion_alertas_variable_check CHECK ((variable::text = ANY (ARRAY['TEMP_AIRE'::character varying, 'HUM_AIRE'::character varying, 'PRESION'::character varying, 'LUX'::character varying, 'HUM_SUELO'::character varying, 'TEMP_SUELO'::character varying, 'PRECIPITACION'::character varying, 'VIENTO'::character varying]::text[])))
);

-- ========================================
-- TABLE: historial_alertas
-- ========================================
CREATE TABLE IF NOT EXISTS historial_alertas (
    id uuid NOT NULL PRIMARY KEY,
    fecha timestamp(6) without time zone NOT NULL,
    leida boolean NOT NULL DEFAULT false,
    mensaje character varying(255) NOT NULL,
    prioridad character varying(255) NOT NULL,
    dispositivo_id uuid,
    lote_id uuid,
    CONSTRAINT fk_historial_alertas_dispositivo FOREIGN KEY (dispositivo_id) REFERENCES dispositivos(id),
    CONSTRAINT fk_historial_alertas_lote FOREIGN KEY (lote_id) REFERENCES lotes(id),
    CONSTRAINT historial_alertas_prioridad_check CHECK ((prioridad::text = ANY (ARRAY['BAJA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'CRITICA'::character varying]::text[])))
);

-- ========================================
-- TABLE: mantenimientos
-- ========================================
CREATE TABLE IF NOT EXISTS mantenimientos (
    id uuid NOT NULL PRIMARY KEY,
    descripcion character varying(500) NOT NULL,
    fecha timestamp(6) without time zone NOT NULL,
    dispositivo_id uuid NOT NULL,
    CONSTRAINT fk_mantenimientos_dispositivo FOREIGN KEY (dispositivo_id) REFERENCES dispositivos(id)
);

-- ========================================
-- TABLE: logs_sistema
-- ========================================
CREATE TABLE IF NOT EXISTS logs_sistema (
    id uuid NOT NULL PRIMARY KEY,
    accion character varying(255) NOT NULL,
    ip_address character varying(255),
    "timestamp" timestamp(6) without time zone DEFAULT NOW(),
    usuario_id uuid NOT NULL,
    CONSTRAINT fk_logs_sistema_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ========================================
-- INDEXES
-- ========================================
CREATE INDEX IF NOT EXISTS idx_fincas_usuario_id ON fincas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_lotes_finca_id ON lotes(finca_id);
CREATE INDEX IF NOT EXISTS idx_lotes_cultivo_id ON lotes(cultivo_id);
CREATE INDEX IF NOT EXISTS idx_dispositivos_lote_id ON dispositivos(lote_id);
CREATE INDEX IF NOT EXISTS idx_configuracion_alertas_dispositivo_id ON configuracion_alertas(dispositivo_id);
CREATE INDEX IF NOT EXISTS idx_configuracion_alertas_lote_id ON configuracion_alertas(lote_id);
CREATE INDEX IF NOT EXISTS idx_historial_alertas_fecha ON historial_alertas(fecha DESC);
CREATE INDEX IF NOT EXISTS idx_historial_alertas_dispositivo_id ON historial_alertas(dispositivo_id);
CREATE INDEX IF NOT EXISTS idx_historial_alertas_lote_id ON historial_alertas(lote_id);
CREATE INDEX IF NOT EXISTS idx_logs_sistema_usuario_id ON logs_sistema(usuario_id);
CREATE INDEX IF NOT EXISTS idx_logs_sistema_timestamp ON logs_sistema("timestamp" DESC);
