-- DEPRECATED: No ejecutar este archivo. La definición completa de user_address ya está
-- incluida en el archivo principal drawSQL-mysql-export-2025-10-14.sql con índices.
-- Dejar este archivo solo como referencia para cambios históricos.

-- Si necesitas alterar una BD existente y tu MySQL NO soporta "IF NOT EXISTS"
-- en ADD COLUMN/CREATE INDEX, usa pasos manuales como:
--   SHOW COLUMNS FROM user_address LIKE 'ciudad';
--   ALTER TABLE user_address ADD COLUMN ciudad VARCHAR(255) NULL AFTER distrito;
--   (repetir para cada columna faltante) y luego:
--   CREATE INDEX idx_user_address_user ON user_address (user_email);
