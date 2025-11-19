## Reclamaciones y Devoluciones

Este módulo habilita la creación y gestión de Reclamaciones (queja/reclamo) y Devoluciones (cambio/reembolso) desde el cliente y su administración en el panel Admin.

### Base de datos

- Fuente de verdad del esquema: usa `drawSQL-mysql-export-2025-10-14.sql` (actualizado) para crear/actualizar las tablas de todo el sistema.
- Incluye: `Reclamacion`, `Devolucion`, `DevolucionItem`, `complaints`, `return_requests` y los cambios de `Envio` con `email_destino` y `telefono_destino`.
- Cliente (Django): se incluye la migración `shop/migrations/0004_claims_returns.py` que crea `complaints` y `return_requests` en la BD del cliente.

### API (Cliente)

- POST/GET `/api/claims/` — crea y lista reclamaciones del usuario.
- GET `/api/claims/{id}/`
- POST/GET `/api/returns/` — crea y lista devoluciones del usuario.
- GET `/api/returns/{id}/`

Cuando se crean, el backend envía un webhook al Admin:

- POST `ADMIN_URL/api/internal/claims` { orderNumber, email, telefono, tipo, detalle }
- POST `ADMIN_URL/api/internal/returns` { orderNumber, email, telefono, motivo, descripcion, metodo }

Puede protegerse con `X-Webhook-Token` configurando `WEBHOOK_SECRET` en el cliente y `webhook.shared-secret` en Admin.

### Admin (Spring Boot)

- Entidades: `Reclamacion`, `Devolucion`, `DevolucionItem`.
- Webhook interno: `/api/internal/claims`, `/api/internal/returns`.
- UI: en el menú “Soporte” -> “Reclamaciones” y “Devoluciones”. Desde el detalle se puede actualizar el estado y (para reclamos) escribir una respuesta.

Notificaciones por email (opcional):

- Se agregó `EmailService` que envía correos al cliente cuando cambia el estado.
- Requiere configurar SMTP en `application.properties`:

```
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-correo@gmail.com
spring.mail.password=tu-password-o-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Opcional
app.mail.from=no-reply@smartfashion.local
app.base-url=https://tu-dominio
```

### Frontend (React)

- Rutas nuevas: `/reclamos` y `/devoluciones` para que el cliente cree y consulte sus solicitudes.
- Acceso rápido desde el menú de usuario (navbar).

### Pasos rápidos

1. Crear tablas de Admin con `admin/sql/claims-returns.sql`.
2. Aplicar migraciones del cliente (Django): `python manage.py migrate` en `cliente/backend`.
3. Levantar Admin y Cliente. Configurar `ADMIN_URL` y `WEBHOOK_SECRET` (desde .env del backend de Django o del frontend) si se desea asegurar los webhooks.
4. (Opcional) Configurar `spring.mail.*` en Admin para activar emails al cliente cuando cambie el estado.

Si provisionaste la BD con el drawSQL y ya existen `complaints` y `return_requests`, en Django marca la migración `0004_claims_returns` como aplicada sin ejecutar (fake), para alinear el estado:

```powershell
cd c:\SmarthFashion\cliente\backend
python manage.py migrate shop 0003_useraddress
python manage.py migrate shop 0004_claims_returns --fake
```

### Estados por defecto

- Reclamación: `registrado` -> `en_proceso` -> (`resuelto` | `rechazado`).
- Devolución: `solicitado` -> (`aprobado` | `rechazado`) -> `recibido` -> (`reembolsado` | `completado`).
