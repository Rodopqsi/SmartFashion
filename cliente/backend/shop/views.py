from django.db import connection
import os
import requests
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from .serializers import CategoriaSerializer, ProductoCardSerializer
from django.db import transaction


def _get_user_email(request):
    u = getattr(request, 'user', None)
    if getattr(u, 'is_authenticated', False):
        email = getattr(u, 'email', None)
        if email:
            return email
    return request.data.get('userEmail') or request.data.get('email') or request.GET.get('email')


@api_view(['GET'])
def address_default(request):
    """Return default shipping address for the user (by auth or email param)."""
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT nombre, telefono, direccion, region, distrito, referencia
            FROM user_address
            WHERE user_email=%s AND is_default=1
            ORDER BY id DESC
            LIMIT 1
            """,
            [email]
        )
        row = cursor.fetchone()
    if not row:
        return Response({'status': 'not_found'}, status=status.HTTP_404_NOT_FOUND)
    data = {
        'nombre': row[0], 'telefono': row[1], 'direccion': row[2], 'region': row[3], 'distrito': row[4], 'referencia': row[5]
    }
    return Response({'status': 'ok', 'data': data})


@api_view(['POST'])
def address_set_default(request):
    """Set or upsert default shipping address for the user."""
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)
    nombre = request.data.get('nombre') or request.data.get('destinatario')
    direccion = request.data.get('direccion') or request.data.get('shipping_address')
    region = request.data.get('region') or request.data.get('shipping_region')
    telefono = request.data.get('telefono')
    distrito = request.data.get('distrito')
    referencia = request.data.get('referencia')
    if not nombre or not direccion or not region:
        return Response({'status': 'invalid', 'message': 'nombre, direccion y region son requeridos'}, status=status.HTTP_400_BAD_REQUEST)
    with connection.cursor() as cursor:
        # Ensure only one default per email
        cursor.execute("UPDATE user_address SET is_default=0 WHERE user_email=%s", [email])
        cursor.execute(
            """
            INSERT INTO user_address (user_email, nombre, telefono, direccion, region, distrito, referencia, is_default)
            VALUES (%s, %s, %s, %s, %s, %s, %s, 1)
            """,
            [email, nombre, telefono, direccion, region, distrito, referencia]
        )
    return Response({'status': 'ok'})


@api_view(['GET', 'POST'])
def addresses(request):
    """List or create addresses for the current user.
    Auth is optional; we use JWT user email when present or a provided email param for development.
    """
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)

    if request.method == 'GET':
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id, label, nombre, telefono, alt_telefono, direccion, direccion_linea2,
                       distrito, ciudad, region, estado, pais, codigo_postal, referencia, is_default
                FROM user_address
                WHERE user_email=%s
                ORDER BY is_default DESC, id DESC
                """,
                [email]
            )
            rows = cursor.fetchall()
        data = [
            {
                'id': r[0], 'label': r[1], 'nombre': r[2], 'telefono': r[3], 'alt_telefono': r[4],
                'direccion': r[5], 'direccion_linea2': r[6], 'distrito': r[7], 'ciudad': r[8],
                'region': r[9], 'estado': r[10], 'pais': r[11], 'codigo_postal': r[12],
                'referencia': r[13], 'is_default': bool(r[14])
            }
            for r in rows
        ]
        return Response({'status': 'ok', 'data': data})

    # POST create
    body = request.data
    required = ['nombre', 'direccion', 'region']
    if any(not body.get(k) for k in required):
        return Response({'status': 'invalid', 'message': 'nombre, direccion y region son requeridos'}, status=status.HTTP_400_BAD_REQUEST)

    fields = [
        'label', 'nombre', 'telefono', 'alt_telefono', 'direccion', 'direccion_linea2',
        'distrito', 'ciudad', 'region', 'estado', 'pais', 'codigo_postal', 'referencia'
    ]
    values = [body.get(f) for f in fields]

    make_default = bool(body.get('is_default'))
    with connection.cursor() as cursor:
        if make_default:
            cursor.execute("UPDATE user_address SET is_default=0 WHERE user_email=%s", [email])
        cursor.execute(
            f"""
            INSERT INTO user_address (user_email, {', '.join(fields)}, is_default)
            VALUES (%s, {', '.join(['%s']*len(fields))}, %s)
            """,
            [email, *values, 1 if make_default else 0]
        )
        cursor.execute("SELECT LAST_INSERT_ID()")
        addr_id = cursor.fetchone()[0]
    return Response({'status': 'ok', 'id': addr_id}, status=status.HTTP_201_CREATED)


@api_view(['GET', 'PUT', 'PATCH', 'DELETE'])
def address_detail(request, addr_id: int):
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)

    if request.method == 'GET':
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id, label, nombre, telefono, alt_telefono, direccion, direccion_linea2,
                       distrito, ciudad, region, estado, pais, codigo_postal, referencia, is_default
                FROM user_address
                WHERE id=%s AND user_email=%s
                """,
                [addr_id, email]
            )
            r = cursor.fetchone()
        if not r:
            return Response({'status': 'not_found'}, status=status.HTTP_404_NOT_FOUND)
        data = {
            'id': r[0], 'label': r[1], 'nombre': r[2], 'telefono': r[3], 'alt_telefono': r[4],
            'direccion': r[5], 'direccion_linea2': r[6], 'distrito': r[7], 'ciudad': r[8],
            'region': r[9], 'estado': r[10], 'pais': r[11], 'codigo_postal': r[12],
            'referencia': r[13], 'is_default': bool(r[14])
        }
        return Response({'status': 'ok', 'data': data})

    if request.method in ['PUT', 'PATCH']:
        body = request.data
        # Only update provided keys
        editable = [
            'label', 'nombre', 'telefono', 'alt_telefono', 'direccion', 'direccion_linea2',
            'distrito', 'ciudad', 'region', 'estado', 'pais', 'codigo_postal', 'referencia'
        ]
        sets = []
        params = []
        for k in editable:
            if k in body:
                sets.append(f"{k}=%s")
                params.append(body.get(k))
        if not sets:
            return Response({'status': 'invalid', 'message': 'nada para actualizar'}, status=status.HTTP_400_BAD_REQUEST)
        with connection.cursor() as cursor:
            cursor.execute(
                f"UPDATE user_address SET {', '.join(sets)} WHERE id=%s AND user_email=%s",
                [*params, addr_id, email]
            )
        return Response({'status': 'ok'})

    # DELETE
    with connection.cursor() as cursor:
        cursor.execute("DELETE FROM user_address WHERE id=%s AND user_email=%s", [addr_id, email])
    return Response({'status': 'ok'})


@api_view(['POST'])
def address_mark_default(request, addr_id: int):
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)
    with connection.cursor() as cursor:
        cursor.execute("UPDATE user_address SET is_default=0 WHERE user_email=%s", [email])
        cursor.execute("UPDATE user_address SET is_default=1 WHERE id=%s AND user_email=%s", [addr_id, email])
    return Response({'status': 'ok'})


@api_view(['GET'])
def home(request):
    # Categorías siempre
    with connection.cursor() as cursor:
        cursor.execute("SELECT id, nombre FROM Categorias ORDER BY nombre ASC")
        rows = cursor.fetchall()
        categories = [{'id': r[0], 'nombre': r[1]} for r in rows]

    # Filtros
    category_id = request.GET.get('category_id')
    q = request.GET.get('q')
    size_id = request.GET.get('size')  # id_talla
    color_id = request.GET.get('color')  # id_color

    filters = []
    params = []
    if category_id and category_id.isdigit():
        filters.append('p.id_categoria = %s')
        params.append(int(category_id))
    if q:
        # Buscar en nombre o descripcion
        filters.append('(p.nombre LIKE %s OR p.descripcion LIKE %s)')
        like = f"%{q}%"
        params.extend([like, like])
    if size_id and size_id.isdigit():
        filters.append('EXISTS (SELECT 1 FROM variaciones_producto v2 WHERE v2.id_producto = p.id AND v2.id_talla = %s)')
        params.append(int(size_id))
    if color_id and color_id.isdigit():
        filters.append('EXISTS (SELECT 1 FROM variaciones_producto v3 WHERE v3.id_producto = p.id AND v3.id_color = %s)')
        params.append(int(color_id))

    where_clause = ''
    if filters:
        where_clause = 'WHERE ' + ' AND '.join(filters)

    # Query productos (limit configurable via query param ?limit=)
    limit = request.GET.get('limit')
    try:
        limit_v = min(max(int(limit), 1), 100) if limit else 12
    except ValueError:
        limit_v = 12

    query = f"""
        SELECT p.id, p.nombre, p.descripcion, p.precio, c.id AS categoria_id, c.nombre AS categoria_nombre, p.image_preview,
               COALESCE(SUM(v.stock), 0) AS stock_total
        FROM Producto p
        LEFT JOIN Categorias c ON c.id = p.id_categoria
        LEFT JOIN variaciones_producto v ON v.id_producto = p.id
        {where_clause}
        GROUP BY p.id, p.nombre, p.descripcion, p.precio, c.id, c.nombre, p.image_preview
        ORDER BY p.id DESC
        LIMIT %s
    """
    params.append(limit_v)

    with connection.cursor() as cursor:
        cursor.execute(query, params)
        prod_rows = cursor.fetchall()

    featured = []
    for r in prod_rows:
        featured.append({
            'id': r[0],
            'nombre': r[1],
            'descripcion': r[2],
            'precio': r[3],
            'precio_descuento': None,  # TODO: calcular con promociones
            'categoria': {'id': r[4], 'nombre': r[5]} if r[4] is not None else None,
            'image_preview': r[6],
            'stock_total': int(r[7] or 0),
        })

    data = {
        'categories': CategoriaSerializer(categories, many=True).data,
        'featured_products': ProductoCardSerializer(featured, many=True).data,
        'banners': [],
        'applied_filters': {
            'category_id': int(category_id) if category_id and category_id.isdigit() else None,
            'q': q or None,
            'size': int(size_id) if size_id and size_id.isdigit() else None,
            'color': int(color_id) if color_id and color_id.isdigit() else None,
        }
    }
    return Response({'status': 'ok', 'data': data})


@api_view(['GET'])
def sizes(request):
    with connection.cursor() as cursor:
        cursor.execute("SELECT id, nombre, tipo FROM tallas ORDER BY id ASC")
        rows = cursor.fetchall()
    data = [{'id': r[0], 'nombre': r[1], 'tipo': r[2]} for r in rows]
    return Response({'status': 'ok', 'data': data})


@api_view(['GET'])
def colors(request):
    with connection.cursor() as cursor:
        cursor.execute("SELECT id, nombre, codigo_hex FROM colores ORDER BY id ASC")
        rows = cursor.fetchall()
    data = [{'id': r[0], 'nombre': r[1], 'codigo_hex': r[2]} for r in rows]
    return Response({'status': 'ok', 'data': data})


@api_view(['GET'])
def product_detail(request, pk: int):
    """Return a single product with images, variants summary and related products (basic)."""
    # Product core info
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT p.id, p.nombre, p.descripcion, p.precio, c.id AS categoria_id, c.nombre AS categoria_nombre, p.image_preview,
                   COALESCE(SUM(v.stock), 0) AS stock_total
            FROM Producto p
            LEFT JOIN Categorias c ON c.id = p.id_categoria
            LEFT JOIN variaciones_producto v ON v.id_producto = p.id
            WHERE p.id = %s
            GROUP BY p.id, p.nombre, p.descripcion, p.precio, c.id, c.nombre, p.image_preview
            """,
            [pk]
        )
        row = cursor.fetchone()

    if not row:
        return Response({'status': 'not_found'}, status=status.HTTP_404_NOT_FOUND)

    product = {
        'id': row[0],
        'nombre': row[1],
        'descripcion': row[2],
        'precio': row[3],
        'precio_descuento': None,
        'categoria': {'id': row[4], 'nombre': row[5]} if row[4] is not None else None,
        'image_preview': row[6],
        'stock_total': int(row[7] or 0),
    }

    # Images (optionally grouped by color and/or size)
    images = []
    images_by_color = {}
    images_by_variant = {}  # key: f"{size_id}-{color_id}"
    try:
        # Try to read optional id_talla (size) if schema supports it
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id_talla, id_color, url
                FROM imagenes_producto
                WHERE id_producto = %s
                ORDER BY id ASC
                """,
                [pk]
            )
            rows = cursor.fetchall()
        for sid, cid, url in rows:
            if sid is None and cid is None:
                images.append(url)
            if cid is not None and sid is None:
                images_by_color.setdefault(str(cid), []).append(url)
            if sid is not None and cid is not None:
                key = f"{sid}-{cid}"
                images_by_variant.setdefault(key, []).append(url)
    except Exception:
        # Fallback for old schema without id_talla
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id_color, url
                FROM imagenes_producto
                WHERE id_producto = %s
                ORDER BY id ASC
                """,
                [pk]
            )
            imgs = cursor.fetchall()
        images = [r[1] for r in imgs if r[0] is None]
        for cid, url in imgs:
            if cid is None:
                continue
            images_by_color.setdefault(str(cid), []).append(url)

    # Variants summary (sizes/colors available + stock)
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT id_talla, id_color, stock
            FROM variaciones_producto
            WHERE id_producto = %s
            """,
            [pk]
        )
        vars_rows = cursor.fetchall()

    variants = [
        {'size_id': r[0], 'color_id': r[1], 'stock': int(r[2] or 0)}
        for r in vars_rows
    ]

    # Related products (same category, simple)
    related = []
    if product.get('categoria') and product['categoria'].get('id') is not None:
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT p.id, p.nombre, p.descripcion, p.precio, p.image_preview,
                       COALESCE(SUM(v.stock), 0) AS stock_total
                FROM Producto p
                LEFT JOIN variaciones_producto v ON v.id_producto = p.id
                WHERE p.id_categoria = %s AND p.id <> %s
                GROUP BY p.id, p.nombre, p.descripcion, p.precio, p.image_preview
                ORDER BY p.id DESC
                LIMIT 8
                """,
                [product['categoria']['id'], pk]
            )
            rel_rows = cursor.fetchall()
        for r in rel_rows:
            related.append({
                'id': r[0], 'nombre': r[1], 'descripcion': r[2], 'precio': r[3],
                'image_preview': r[4], 'stock_total': int(r[5] or 0)
            })

    result = {
        'product': product,
        'images': images,
        'imagesByColor': images_by_color,
        'imagesByVariant': images_by_variant,
        'variants': variants,
        'related': related,
    }
    return Response({'status': 'ok', 'data': result})


@api_view(['GET', 'POST'])
def product_reviews(request, pk: int):
    """
    Reviews endpoint.
    - GET: returns last reviews for product.
    - POST: creates a new review for authenticated user (requires reviews table to exist).
    If table doesn't exist, returns a clear error and the client can fallback.
    """
    if request.method == 'POST':
        # Require authentication (DRF should attach user if JWT/session configured)
        user = getattr(request, 'user', None)
        if not getattr(user, 'is_authenticated', False):
            return Response({'status': 'unauthorized'}, status=status.HTTP_401_UNAUTHORIZED)

        rating = request.data.get('rating')
        text = request.data.get('text', '')
        try:
            rating_v = max(1, min(5, int(rating)))
        except Exception:
            return Response({'status': 'invalid', 'message': 'rating inválido'}, status=status.HTTP_400_BAD_REQUEST)

        email = getattr(user, 'email', None) or 'anon@local'
        try:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO product_reviews (product_id, user_email, rating, text, created_at)
                    VALUES (%s, %s, %s, %s, NOW())
                    """,
                    [pk, email, rating_v, text]
                )
        except Exception as e:
            # Likely table missing
            return Response({'status': 'not_supported', 'message': 'Tabla product_reviews no existe', 'detail': str(e)}, status=status.HTTP_501_NOT_IMPLEMENTED)

        return Response({'status': 'ok'})

    # GET
    try:
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id, user_email, rating, text, created_at
                FROM product_reviews
                WHERE product_id = %s
                ORDER BY id DESC
                LIMIT 50
                """,
                [pk]
            )
            rows = cursor.fetchall()
        data = [
            {
                'id': r[0],
                'user': r[1],
                'rating': int(r[2] or 0),
                'text': r[3],
                'created_at': r[4].isoformat() if r[4] else None,
            }
            for r in rows
        ]
        return Response({'status': 'ok', 'data': data})
    except Exception as e:
        # Table missing: return a default sample
        sample = [
            {'id': 1, 'user': 'Ana', 'rating': 5, 'text': 'Excelente calidad y queda perfecto.'},
            {'id': 2, 'user': 'María', 'rating': 4, 'text': 'Los acabados están muy bien, llegó rápido.'},
        ]
        return Response({'status': 'ok', 'data': sample, 'fallback': True, 'detail': str(e)})


@api_view(['POST'])
def checkout_preview(request):
    """Compute totals for given cart payload; validate stock availability."""
    items = request.data.get('items') or []
    if not isinstance(items, list) or not items:
        return Response({'status': 'invalid', 'message': 'items requerido'}, status=status.HTTP_400_BAD_REQUEST)

    line_items = []
    subtotal = 0.0
    for it in items:
        try:
            pid = int(it.get('product_id'))
            qty = max(1, int(it.get('qty', 1)))
            size_id = it.get('size_id')
            color_id = it.get('color_id')
        except Exception:
            return Response({'status': 'invalid', 'message': 'item inválido'}, status=status.HTTP_400_BAD_REQUEST)

        with connection.cursor() as cursor:
            cursor.execute("SELECT nombre, precio, image_preview FROM Producto WHERE id=%s", [pid])
            prow = cursor.fetchone()
        if not prow:
            return Response({'status': 'invalid', 'message': f'producto {pid} no existe'}, status=status.HTTP_400_BAD_REQUEST)
        name, price, image = prow[0], float(prow[1] or 0), prow[2]

        # validate stock by variant when provided
        stock_ok = True
        if size_id is not None or color_id is not None:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT COALESCE(stock,0) FROM variaciones_producto
                    WHERE id_producto=%s AND (%s IS NULL OR id_talla=%s) AND (%s IS NULL OR id_color=%s)
                    LIMIT 1
                    """,
                    [pid, size_id, size_id, color_id, color_id]
                )
                row = cursor.fetchone()
            stock = int(row[0] if row else 0)
            stock_ok = stock >= qty

        amount = price * qty
        subtotal += amount
        line_items.append({
            'product_id': pid,
            'name': name,
            'image': image,
            'price': price,
            'qty': qty,
            'size_id': size_id,
            'color_id': color_id,
            'stock_ok': stock_ok,
            'amount': amount,
        })

    igv = subtotal * 0.18
    total = subtotal + igv
    return Response({'status': 'ok', 'data': {'items': line_items, 'subtotal': subtotal, 'igv': igv, 'total': total}})


@api_view(['POST'])
def checkout_confirm(request):
    """Attempt to decrement stock for each item atomically; returns an order number. No order table is required."""
    items = request.data.get('items') or []
    if not isinstance(items, list) or not items:
        return Response({'status': 'invalid', 'message': 'items requerido'}, status=status.HTTP_400_BAD_REQUEST)

    user_email = _get_user_email(request)

    try:
        with transaction.atomic():
            subtotal = 0.0
            # map of product_id -> price to reuse
            price_cache = {}
            for it in items:
                pid = int(it.get('product_id'))
                qty = max(1, int(it.get('qty', 1)))
                size_id = it.get('size_id')
                color_id = it.get('color_id')

                # Check current stock
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        SELECT COALESCE(stock,0) FROM variaciones_producto
                        WHERE id_producto=%s AND (%s IS NULL OR id_talla=%s) AND (%s IS NULL OR id_color=%s)
                        LIMIT 1
                        """,
                        [pid, size_id, size_id, color_id, color_id]
                    )
                    row = cursor.fetchone()
                stock = int(row[0] if row else 0)
                if stock < qty:
                    return Response({'status': 'insufficient_stock', 'product_id': pid, 'available': stock}, status=status.HTTP_409_CONFLICT)

                # Decrement
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        UPDATE variaciones_producto
                        SET stock = stock - %s
                        WHERE id_producto=%s AND (%s IS NULL OR id_talla=%s) AND (%s IS NULL OR id_color=%s)
                        LIMIT 1
                        """,
                        [qty, pid, size_id, size_id, color_id, color_id]
                    )

                # Obtain product price and accumulate subtotal
                if pid not in price_cache:
                    with connection.cursor() as cursor:
                        cursor.execute("SELECT precio, nombre, image_preview FROM Producto WHERE id=%s", [pid])
                        prow = cursor.fetchone()
                    price_cache[pid] = {
                        'price': float(prow[0] or 0) if prow else 0.0,
                        'name': prow[1] if prow else f'Producto {pid}',
                        'image': prow[2] if prow else None,
                    }
                line_amount = price_cache[pid]['price'] * qty
                subtotal += line_amount

            # Try to persist order if tables exist
            igv = subtotal * 0.18
            total = subtotal + igv
            order_number = None
            try:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        INSERT INTO orders (order_number, email, subtotal, igv, total, created_at)
                        VALUES (CONCAT('SF', UNIX_TIMESTAMP()), %s, %s, %s, %s, NOW())
                        """,
                        [user_email, subtotal, igv, total]
                    )
                    cursor.execute("SELECT LAST_INSERT_ID()")
                    order_id = cursor.fetchone()[0]
                    cursor.execute("SELECT order_number FROM orders WHERE id=%s", [order_id])
                    order_number = cursor.fetchone()[0]
                # Insert items
                with connection.cursor() as cursor:
                    for it in items:
                        pid = int(it.get('product_id'))
                        qty = max(1, int(it.get('qty', 1)))
                        size_id = it.get('size_id')
                        color_id = it.get('color_id')
                        meta = price_cache.get(pid) or {'price':0.0,'name':f'Producto {pid}','image':None}
                        cursor.execute(
                            """
                            INSERT INTO order_items (order_id, product_id, size_id, color_id, qty, unit_price, amount, name, image)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                            """,
                            [order_id, pid, size_id, color_id, qty, meta['price'], meta['price']*qty, meta['name'], meta['image']]
                        )
            except Exception:
                # Tables may not exist; continue without persistence
                import time
                order_number = f"SF{int(time.time())}"
        # Determine shipping info: default address if not provided in payload
        # Determine shipping address selection priority: address_id -> payload -> default
        destinatario = request.data.get('destinatario') or request.data.get('nombre')
        telefono_envio = None
        shipping_address = None
        shipping_region = None
        address_id = request.data.get('address_id')
        if address_id:
            try:
                address_id = int(address_id)
            except Exception:
                return Response({'status': 'invalid', 'message': 'address_id inválido'}, status=status.HTTP_400_BAD_REQUEST)
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT nombre, direccion, region, telefono
                    FROM user_address
                    WHERE id=%s AND user_email=%s
                    """,
                    [address_id, user_email]
                )
                row = cursor.fetchone()
            if not row:
                return Response({'status': 'invalid', 'message': 'address_id no existe'}, status=status.HTTP_400_BAD_REQUEST)
            destinatario = destinatario or row[0]
            shipping_address = row[1]
            shipping_region = row[2]
            telefono_envio = row[3]
        else:
            shipping_address = request.data.get('shipping_address') or request.data.get('direccion')
            shipping_region = request.data.get('shipping_region') or request.data.get('region')
        if not (shipping_address and shipping_region):
            if not user_email:
                return Response({'status': 'address_required', 'message': 'Falta email para obtener dirección por defecto'}, status=status.HTTP_400_BAD_REQUEST)
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT nombre, direccion, region, telefono
                    FROM user_address
                    WHERE user_email=%s AND is_default=1
                    ORDER BY id DESC
                    LIMIT 1
                    """,
                    [user_email]
                )
                row = cursor.fetchone()
            if not row:
                return Response({'status': 'address_required', 'message': 'Configura tu dirección predeterminada antes de comprar'}, status=status.HTTP_400_BAD_REQUEST)
            destinatario = destinatario or row[0]
            shipping_address = shipping_address or row[1]
            shipping_region = shipping_region or row[2]
            telefono_envio = telefono_envio or row[3]

        # Fire-and-forget webhook to Admin to create shipment and tracking
        tracking_url = None
        webhook_status = None
        try:
            admin_url = os.getenv('ADMIN_URL', 'http://localhost:8081')
            webhook_secret = os.getenv('WEBHOOK_SECRET', '')
            origin_region = request.data.get('origin_region') or 'Lima'

            payload = {
                'orderNumber': order_number,
                'destinatario': destinatario or user_email or 'Cliente',
                'direccion': shipping_address,
                'regionDestino': shipping_region,
                'centroRegion': origin_region,
                'email': user_email,
                'telefono': telefono_envio,
            }
            headers = {'Content-Type': 'application/json'}
            if webhook_secret:
                headers['X-Webhook-Token'] = webhook_secret
            r = requests.post(f"{admin_url}/api/internal/orders", json=payload, headers=headers, timeout=6)
            webhook_status = r.status_code
            if r.ok:
                j = r.json()
                tracking_url = j.get('trackingUrl')
        except Exception as e:
            # Do not fail checkout on webhook issues
            webhook_status = f"error: {e}"

        return Response({'status': 'ok', 'order_number': order_number, 'tracking_url': tracking_url, 'webhook_status': webhook_status})
    except Exception as e:
        return Response({'status': 'error', 'message': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['GET'])
def order_tracking(request, order_number: str):
    """Return a public tracking URL for the order, so the client can open a timeline view.
    In a later iteration we can fetch events and return JSON here.
    """
    admin_url = os.getenv('ADMIN_URL', 'http://localhost:8081')
    url = f"{admin_url}/tracking/{order_number}"
    return Response({'status': 'ok', 'tracking_url': url})
