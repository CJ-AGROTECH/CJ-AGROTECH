-- Make condicion and umbral columns nullable for supporting both range-based and comparison-based alert rules
-- Range-based rules: use umbral_min and umbral_max, condicion is NULL
-- Comparison-based rules: use condicion and umbral, umbral_min and umbral_max are NULL

ALTER TABLE configuracion_alertas
  ALTER COLUMN condicion DROP NOT NULL;

ALTER TABLE configuracion_alertas
  ALTER COLUMN umbral DROP NOT NULL;
