CREATE DATABASE IF NOT EXISTS `smarthfashion` CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE `smarthfashion`;
CREATE TABLE `Producto`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `descripcion` TEXT NOT NULL,
    `precio` DECIMAL(8, 2) NOT NULL,
    `id_categoria` BIGINT UNSIGNED NOT NULL,
    `image_preview` VARCHAR(255) NOT NULL
);
CREATE TABLE `Categorias`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL
);
CREATE TABLE `variaciones_producto`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_producto` BIGINT UNSIGNED NOT NULL,
    `id_talla` BIGINT UNSIGNED NOT NULL,
    `id_color` BIGINT UNSIGNED NOT NULL,
    `stock` INT NOT NULL,
    UNIQUE KEY `unique_variacion` (`id_producto`, `id_talla`, `id_color`)
);
CREATE TABLE `imagenes_producto`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_color` BIGINT UNSIGNED NULL,
    `url` TEXT NOT NULL,
    `id_producto` BIGINT UNSIGNED NOT NULL
);
CREATE TABLE `tallas`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `tipo` VARCHAR(255) NOT NULL
);
CREATE TABLE `colores`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `codigo_hex` CHAR(255) NOT NULL
);
CREATE TABLE `usuario`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `apellido` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `telefono` VARCHAR(255) NOT NULL,
    `fecha_registro` BIGINT NOT NULL
);
CREATE TABLE `Carrito`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_usuario` BIGINT UNSIGNED NOT NULL,
    `estado` VARCHAR(255) NOT NULL
);
CREATE TABLE `Item_carrito`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_variacion_producto` BIGINT UNSIGNED NOT NULL,
    `cantidad` BIGINT NOT NULL,
    `id_carrito` BIGINT UNSIGNED NOT NULL,
    `precio_unitario_guardado` DECIMAL(8, 2) NOT NULL,
    `descuento_aplicado` DECIMAL(8, 2) NOT NULL,
    `id_promocion_aplicada` BIGINT UNSIGNED NOT NULL
);
CREATE TABLE `Promocion`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `codigo` VARCHAR(255) NULL,
    `nombre` VARCHAR(255) NOT NULL,
    `tipo_descuento` VARCHAR(255) NOT NULL COMMENT 'Ej: \'PORCENTAJE\', \'MONTO_FIJO\'',
    `valor` DECIMAL(8, 2) NOT NULL COMMENT 'Ej: 0.15 o 10.00',
    `fecha_inicio` TIMESTAMP NOT NULL,
    `fecha_fin` TIMESTAMP NOT NULL,
    `activo` BIGINT NOT NULL
);
CREATE TABLE `Aplicacion_promocion`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_promocion` BIGINT UNSIGNED NOT NULL,
    `id_producto` BIGINT UNSIGNED NULL,
    `id_categoria` BIGINT UNSIGNED NULL
);
CREATE TABLE `pedido`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_usuario` BIGINT UNSIGNED NOT NULL,
    `fecha_pedido` BIGINT NOT NULL,
    `estado` VARCHAR(255) NOT NULL COMMENT 'El estado de la orden (Ej: \'Pagado\', \'Procesando Envío\', \'Entregado\', \'Cancelado\').',
    `total_neto_productos` DECIMAL(8, 2) NOT NULL COMMENT 'El valor de los productos después de todos los descuentos.',
    `costo_envio` DECIMAL(8, 2) NOT NULL,
    `total_final` DECIMAL(8, 2) NOT NULL,
    `direccion_envio` BIGINT NOT NULL,
    `id_carrito` BIGINT UNSIGNED NOT NULL
);
CREATE TABLE `detalle_pedido`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_pedido` BIGINT UNSIGNED NOT NULL,
    `id_variacion_producto` BIGINT UNSIGNED NOT NULL,
    `cantidad` INT NOT NULL,
    `precio_unitario` DECIMAL(8, 2) NOT NULL,
    `descuento_aplicado` BIGINT NOT NULL,
    `id_promocion_aplicada` BIGINT UNSIGNED NOT NULL,
    `subtotal` DECIMAL(8, 2) NOT NULL COMMENT 'Campo calculado: (cantidad * precio_unitario) - descuento_aplicado.'
);
CREATE TABLE `Transaccion_Pago`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_pedido` BIGINT UNSIGNED NOT NULL,
    `monto` DECIMAL(8, 2) NOT NULL COMMENT 'El monto exacto que se procesó (debe coincidir con Pedido.total_final).',
    `fecha_transaccion` TIMESTAMP NOT NULL,
    `metodo` VARCHAR(255) NOT NULL COMMENT 'El método usado (Ej: \'Tarjeta de Crédito\', \'PayPal\', \'Transferencia\').',
    `pasarela_id` VARCHAR(255) NOT NULL COMMENT 'CRÍTICO: El ID único que la pasarela de pago te devuelve (Ej: ch_1FzS...).',
    `estado_pago` VARCHAR(255) NOT NULL COMMENT 'El estado final (Ej: \'Aprobado\', \'Rechazado\', \'Pendiente\').'
);
CREATE TABLE `favorito`(
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `id_usuario` BIGINT UNSIGNED NOT NULL,
    `id_producto` BIGINT UNSIGNED NOT NULL,
    `fecha_agregado` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE
    `imagenes_producto` ADD CONSTRAINT `imagenes_producto_id_producto_foreign` FOREIGN KEY(`id_producto`) REFERENCES `Producto`(`id`);
ALTER TABLE
    `Item_carrito` ADD CONSTRAINT `item_carrito_id_promocion_aplicada_foreign` FOREIGN KEY(`id_promocion_aplicada`) REFERENCES `Promocion`(`id`);
ALTER TABLE
    `variaciones_producto` ADD CONSTRAINT `variaciones_producto_id_talla_foreign` FOREIGN KEY(`id_talla`) REFERENCES `tallas`(`id`);
ALTER TABLE
    `Item_carrito` ADD CONSTRAINT `item_carrito_id_carrito_foreign` FOREIGN KEY(`id_carrito`) REFERENCES `Carrito`(`id`);
-- Removed incorrect FK that referenced Producto instead of usuario
-- ALTER TABLE `favorito` ADD CONSTRAINT `favorito_id_usuario_foreign` FOREIGN KEY(`id_usuario`) REFERENCES `Producto`(`id`);
ALTER TABLE
    `variaciones_producto` ADD CONSTRAINT `variaciones_producto_id_producto_foreign` FOREIGN KEY(`id_producto`) REFERENCES `Producto`(`id`);
ALTER TABLE
    `imagenes_producto` ADD CONSTRAINT `imagenes_producto_id_color_foreign` FOREIGN KEY(`id_color`) REFERENCES `colores`(`id`);
ALTER TABLE
    `pedido` ADD CONSTRAINT `pedido_id_carrito_foreign` FOREIGN KEY(`id_carrito`) REFERENCES `Carrito`(`id`);
ALTER TABLE
    `Aplicacion_promocion` ADD CONSTRAINT `aplicacion_promocion_id_promocion_foreign` FOREIGN KEY(`id_promocion`) REFERENCES `Promocion`(`id`);
ALTER TABLE
    `Carrito` ADD CONSTRAINT `carrito_id_usuario_foreign` FOREIGN KEY(`id_usuario`) REFERENCES `usuario`(`id`);
ALTER TABLE
    `Transaccion_Pago` ADD CONSTRAINT `transaccion_pago_id_pedido_foreign` FOREIGN KEY(`id_pedido`) REFERENCES `pedido`(`id`);
ALTER TABLE
    `Item_carrito` ADD CONSTRAINT `item_carrito_id_variacion_producto_foreign` FOREIGN KEY(`id_variacion_producto`) REFERENCES `variaciones_producto`(`id`);
ALTER TABLE
    `Aplicacion_promocion` ADD CONSTRAINT `aplicacion_promocion_id_producto_foreign` FOREIGN KEY(`id_producto`) REFERENCES `Producto`(`id`);
ALTER TABLE
    `pedido` ADD CONSTRAINT `pedido_id_usuario_foreign` FOREIGN KEY(`id_usuario`) REFERENCES `usuario`(`id`);
ALTER TABLE
    `Producto` ADD CONSTRAINT `producto_id_categoria_foreign` FOREIGN KEY(`id_categoria`) REFERENCES `Categorias`(`id`);
ALTER TABLE
    `Aplicacion_promocion` ADD CONSTRAINT `aplicacion_promocion_id_categoria_foreign` FOREIGN KEY(`id_categoria`) REFERENCES `Categorias`(`id`);
ALTER TABLE
    `detalle_pedido` ADD CONSTRAINT `detalle_pedido_id_promocion_aplicada_foreign` FOREIGN KEY(`id_promocion_aplicada`) REFERENCES `Promocion`(`id`);
ALTER TABLE
    `detalle_pedido` ADD CONSTRAINT `detalle_pedido_id_pedido_foreign` FOREIGN KEY(`id_pedido`) REFERENCES `pedido`(`id`);
ALTER TABLE
    `detalle_pedido` ADD CONSTRAINT `detalle_pedido_id_variacion_producto_foreign` FOREIGN KEY(`id_variacion_producto`) REFERENCES `variaciones_producto`(`id`);
ALTER TABLE
    `favorito` ADD CONSTRAINT `favorito_id_usuario_foreign` FOREIGN KEY(`id_usuario`) REFERENCES `usuario`(`id`);
ALTER TABLE
    `favorito` ADD CONSTRAINT `favorito_id_producto_foreign` FOREIGN KEY(`id_producto`) REFERENCES `Producto`(`id`);