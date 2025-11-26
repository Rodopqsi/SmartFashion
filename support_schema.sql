-- Support schema: Reclamacion, Devolucion, DevolucionItem, Coleccion, ColeccionProducto

CREATE TABLE IF NOT EXISTS `Reclamacion` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_number` VARCHAR(64) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `telefono` VARCHAR(64) NULL,
    `tipo` VARCHAR(16) NOT NULL,
    `detalle` TEXT NOT NULL,
    `estado` VARCHAR(32) NOT NULL DEFAULT 'registrado',
    `respuesta` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `Devolucion` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_number` VARCHAR(64) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `telefono` VARCHAR(64) NULL,
    `motivo` VARCHAR(64) NOT NULL,
    `descripcion` TEXT NULL,
    `metodo` VARCHAR(16) NOT NULL,
    `estado` VARCHAR(32) NOT NULL DEFAULT 'solicitado',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `DevolucionItem` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `devolucion_id` BIGINT NOT NULL,
    `product_sku` VARCHAR(64) NULL,
    `product_name` VARCHAR(255) NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `condicion` VARCHAR(32) NULL,
    CONSTRAINT `devolucion_item_fk` FOREIGN KEY (`devolucion_id`) REFERENCES `Devolucion`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `Coleccion` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `nombre` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `descripcion` TEXT NULL,
    `image_url` VARCHAR(1024) NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `orden` INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ColeccionProducto` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `id_coleccion` BIGINT UNSIGNED NOT NULL,
    `id_producto` BIGINT UNSIGNED NOT NULL,
    UNIQUE KEY `ux_coleccion_producto` (`id_coleccion`, `id_producto`),
    CONSTRAINT `coleccion_producto_coleccion_fk` FOREIGN KEY(`id_coleccion`) REFERENCES `Coleccion`(`id`) ON DELETE CASCADE,
    CONSTRAINT `coleccion_producto_producto_fk` FOREIGN KEY(`id_producto`) REFERENCES `Producto`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
