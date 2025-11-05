# SmartFashion Admin (Spring Boot)

Admin web para gestionar productos, categorías, usuarios y pedidos. Comparte la misma base de datos MySQL que el backend actual.

## Stack
- Java 17
- Spring Boot 3 (Web, Thymeleaf, Data JPA, Security, Validation)
- MySQL 8

## Arranque rápido
1. Configura variables de entorno (o crea un `.env` y expórtalo en tu shell):
   - `DB_URL=jdbc:mysql://localhost:3306/smarthfashion?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `DB_USER=tu_usuario`
   - `DB_PASS=tu_password`
   - `ADMIN_USER=admin` (opcional)
   - `ADMIN_PASS=admin123` (opcional)
   - `ADMIN_PORT=8081` (opcional)
2. En la carpeta `admin/` ejecuta:
   - Compilar y levantar: `mvn spring-boot:run`

Accede a `http://localhost:8081/login` y entra con `ADMIN_USER`/`ADMIN_PASS`.

## Qué incluye ahora
- Autenticación con Spring Security (in-memory, configurable por env).
- Dashboard básico (`/admin`).
- CRUD inicial de Productos (listar y crear) con relación a Categorías.
- Mapeo JPA a tablas existentes (`Producto`, `Categorias`).

## Próximos pasos sugeridos
- Usuarios y roles desde BD (tabla `usuario` + tabla de roles/estado ban).  
  - Endpoint/admin para banear/desbanear usuario (campo `estado` o tabla de sanciones).
- Gestión de variaciones (tallas/colores/stock) usando `variaciones_producto`.
- Gestión de imágenes por color (`imagenes_producto`).
- Promociones y reglas (`Promocion`, `Aplicacion_promocion`).
- Órdenes, pagos y reportes (`pedido`, `detalle_pedido`, `Transaccion_Pago`).
- Auditoría (tabla de logs), y soft-delete donde aplique.
- Subida de imágenes (S3/Azure Blob/local) con previsualización.
- Paginación/búsqueda en listados.
- Sustituir auth in-memory por usuarios admins en BD, con BCrypt.

## Notas de compatibilidad
- `spring.jpa.hibernate.naming.physical-strategy=PhysicalNamingStrategyStandardImpl` para respetar nombres de tabla/columna del SQL exportado.
- `spring.jpa.hibernate.ddl-auto=none` para no tocar el esquema existente.
