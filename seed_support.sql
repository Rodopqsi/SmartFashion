-- Seed data to test Admin UI: creates a category, a product, a collection, and links them; also adds a sample Reclamacion and Devolucion.

-- Category + Product (safe inserts)
INSERT INTO Categorias (nombre) SELECT 'Sin Categoria' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM Categorias WHERE nombre='Sin Categoria');
SET @cat_id = (SELECT id FROM Categorias WHERE nombre='Sin Categoria' LIMIT 1);

INSERT INTO Producto (nombre, descripcion, precio, id_categoria, image_preview)
SELECT 'Producto prueba', 'Producto de prueba para UI', 9.99, @cat_id, ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE nombre='Producto prueba' AND id_categoria = @cat_id);

SET @prod_id = (SELECT id FROM Producto WHERE nombre='Producto prueba' LIMIT 1);

-- Coleccion + enlace
INSERT INTO Coleccion (nombre, slug, descripcion, image_url, activo, orden)
SELECT 'Primavera 2025', 'primavera-2025', 'Colección de prueba', NULL, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Coleccion WHERE slug='primavera-2025');

SET @coleccion_id = (SELECT id FROM Coleccion WHERE slug='primavera-2025' LIMIT 1);

INSERT INTO ColeccionProducto (id_coleccion, id_producto)
SELECT @coleccion_id, @prod_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ColeccionProducto WHERE id_coleccion=@coleccion_id AND id_producto=@prod_id);

-- Reclamacion y Devolucion de prueba
INSERT INTO Reclamacion (order_number, email, tipo, detalle)
SELECT 'TEST-ORDER-1', 'usuario@example.com', 'reclamo', 'Detalle de prueba'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Reclamacion WHERE order_number='TEST-ORDER-1' AND email='usuario@example.com');

INSERT INTO Devolucion (order_number, email, motivo, metodo)
SELECT 'TEST-ORDER-1', 'usuario@example.com', 'no_satisfecho', 'reembolso'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Devolucion WHERE order_number='TEST-ORDER-1' AND email='usuario@example.com');
