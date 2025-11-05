-- Shipping addresses per user (email-based key). If you use Django auth users with IDs, you can adapt this later.
CREATE TABLE IF NOT EXISTS user_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_email VARCHAR(255) NOT NULL,
  label VARCHAR(120) NULL,
  nombre VARCHAR(255) NOT NULL,
  telefono VARCHAR(50) NULL,
  alt_telefono VARCHAR(50) NULL,
  direccion VARCHAR(512) NOT NULL,
  direccion_linea2 VARCHAR(512) NULL,
  distrito VARCHAR(255) NULL,
  ciudad VARCHAR(255) NULL,
  region VARCHAR(255) NOT NULL,
  estado VARCHAR(255) NULL,
  pais VARCHAR(100) NULL,
  codigo_postal VARCHAR(32) NULL,
  referencia VARCHAR(512) NULL,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional helpful index to find the default quickly
CREATE INDEX IF NOT EXISTS idx_user_address_default ON user_address (user_email, is_default);
CREATE INDEX IF NOT EXISTS idx_user_address_user ON user_address (user_email);
