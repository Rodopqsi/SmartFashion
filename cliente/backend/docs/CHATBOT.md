# Chatbot de Catálogo – SmartFashion

Este chatbot consume un snapshot JSON del catálogo para responder preguntas sobre productos, categorías, precios, stock, colores y más.

## Fuentes de datos
- Archivo: `cliente/backend/shop/data/products_snapshot.json`
- Endpoint: `GET /api/catalog/snapshot` (opcional `?force=1` para regenerar)

El snapshot se re-genera automáticamente (debounce ~2s) ante cambios en:
- `Producto`, `VariacionProducto`, `ImagenProducto`, `ProductReview`, `ColeccionProducto` (señales en `shop/signals.py`).

Además, puedes regenerar manualmente:
```powershell
cd cliente/backend
python manage.py export_products_snapshot
```

## Esquema del snapshot
```jsonc
{
  "generated_at": "ISO-8601",
  "currency": "PEN",
  "stats": { "total_products": 0, "total_collections": 0 },
  "products": [
    {
      "id": 1,
      "nombre": "Polo Classic",
      "descripcion": "...",
      "precio": 59.9,
      "precio_descuento": null,
      "categoria": { "id": 2, "nombre": "Polos" },
      "image_preview": "...",
      "stock_total": 123,
      "avg_rating": 4.2,
      "rating_count": 10,
      "variantes": [ { "id": 10, "talla_id": 3, "color_id": 2, "stock": 5 } ],
      "imagenes": { "general": ["..."], "por_color": { "2": ["..."] } }
    }
  ],
  "collections": [ { "id": 1, "nombre": "Novedades", "slug": "novedades", "product_ids": [1,2,3] } ],
  "colors": [ { "id": 1, "nombre": "Negro", "codigo_hex": "#000000" } ],
  "sizes": [ { "id": 1, "nombre": "S", "tipo": "alpha" } ],
  "meta": { "schema_version": 1, "source": "raw_sql", "content_hash": "..." }
}
```

## Buenas prácticas
- El snapshot se escribe de forma atómica y no se reescribe si el contenido no cambió (hash en `meta.content_hash`).
- Para chatbots/LLMs, preferir el endpoint (con cache HTTP) y refrescar bajo demanda `?force=1` en flujos críticos.

## Consumo desde frontend
- El widget `ChatWidget.jsx` consulta el endpoint y entiende preguntas clave:
  - Descuentos/ofertas/promociones.
  - Precio de un producto por nombre.
  - Stock/Tallas/Colores de un producto.
  - Filtros por categoría, color y precio (e.g. "menos de 100", "entre 50 y 100").

## Variables relevantes
- `SNAPSHOT_AUTO=1` (por defecto): habilita regeneración automática por señales.
- `STRIPE_CURRENCY` controla `currency` (PEN por defecto).

## Endpoints relacionados
- `GET /api/colors/`, `GET /api/sizes/` existen aparte, pero el snapshot ya incluye colores y tallas para búsquedas rápidas.

## Problemas comunes
- Si el snapshot no aparece: revisa permisos de escritura en `shop/data/` y que el backend corre.
- Si el contenido no refleja cambios: espera ~2s tras el cambio o fuerza con `?force=1` o el comando de exportación.
