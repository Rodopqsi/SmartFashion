from django.db import connection
import os
import requests
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from .serializers import CategoriaSerializer, ProductoCardSerializer, ColeccionSerializer
from .catalog_snapshot import load_snapshot, export_snapshot
from .models import Complaint, ReturnRequest, Coleccion
import os
from django.core.mail import EmailMultiAlternatives
from django.conf import settings
import json
from pathlib import Path
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
    with connection.cursor() as cursor:
        cursor.execute("SELECT id, nombre FROM categorias ORDER BY nombre ASC")
        rows = cursor.fetchall()
        categories = [{'id': r[0], 'nombre': r[1]} for r in rows]

    category_id = request.GET.get('category_id')
    q = request.GET.get('q')
    size_id = request.GET.get('size')
    color_id = request.GET.get('color')

    filters = []
    params = []
    if category_id and category_id.isdigit():
        filters.append('p.id_categoria = %s')
        params.append(int(category_id))
    if q:
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

    limit = request.GET.get('limit')
    page = request.GET.get('page')
    try:
        limit_v = min(max(int(limit), 1), 100) if limit else 12
    except ValueError:
        limit_v = 12
    try:
        page_v = max(int(page), 1) if page else 1
    except ValueError:
        page_v = 1
    offset_v = (page_v - 1) * limit_v

    count_query = f"""
        SELECT COUNT(DISTINCT p.id)
        FROM producto p
        LEFT JOIN variaciones_producto v ON v.id_producto = p.id
        {where_clause}
    """
    with connection.cursor() as cursor:
        cursor.execute(count_query, params)
        total_count_row = cursor.fetchone()
    total_count = int(total_count_row[0] if total_count_row else 0)

    data_query = f"""
        SELECT p.id, p.nombre, p.descripcion, p.precio, c.id AS categoria_id, c.nombre AS categoria_nombre, p.image_preview,
               COALESCE(SUM(v.stock), 0) AS stock_total
        FROM producto p
        LEFT JOIN categorias c ON c.id = p.id_categoria
        LEFT JOIN variaciones_producto v ON v.id_producto = p.id
        {where_clause}
        GROUP BY p.id, p.nombre, p.descripcion, p.precio, c.id, c.nombre, p.image_preview
        ORDER BY p.id DESC
        LIMIT %s OFFSET %s
    """
    with connection.cursor() as cursor:
        cursor.execute(data_query, [*params, limit_v, offset_v])
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

    collections = []
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT id, nombre, slug, descripcion, image_url, orden
            FROM coleccion
            WHERE activo = 1
            ORDER BY orden ASC, id DESC
            LIMIT 8
            """
        )
        col_rows = cursor.fetchall()
    for col in col_rows:
        col_id, col_nombre, col_slug, col_desc, col_image, col_orden = col
        with connection.cursor() as cursor:
            cursor.execute(
                """
                  SELECT p.id, p.nombre, p.descripcion, p.precio,
                      c.id AS categoria_id, c.nombre AS categoria_nombre,
                      p.image_preview,
                      COALESCE(SUM(v.stock), 0) AS stock_total
                  FROM coleccionproducto cp
                  JOIN producto p ON p.id = cp.id_producto
                  LEFT JOIN categorias c ON c.id = p.id_categoria
                  LEFT JOIN variaciones_producto v ON v.id_producto = p.id
                  WHERE cp.id_coleccion = %s
                  GROUP BY p.id, p.nombre, p.descripcion, p.precio, c.id, c.nombre, p.image_preview
                  ORDER BY p.id DESC
                  LIMIT 8
                  """,
                  [col_id]
                 )
            prows = cursor.fetchall()
        prods = []
        for r in prows:
            prods.append({
                'id': r[0],
                'nombre': r[1],
                'descripcion': r[2],
                'precio': r[3],
                'precio_descuento': None,
                'categoria': {'id': r[4], 'nombre': r[5]} if r[4] is not None else None,
                'image_preview': r[6],
                'stock_total': int(r[7] or 0),
            })
        collections.append({
            'id': col_id,
            'nombre': col_nombre,
            'slug': col_slug,
            'descripcion': col_desc,
            'image_url': col_image,
            'orden': int(col_orden or 0),
            'products': prods,
        })

    data = {
        'categories': CategoriaSerializer(categories, many=True).data,
        'featured_products': ProductoCardSerializer(featured, many=True).data,
        'collections': ColeccionSerializer(collections, many=True).data,
        'banners': [],
        'pagination': {'page': page_v, 'limit': limit_v, 'total': total_count},
        'applied_filters': {
            'category_id': int(category_id) if category_id and category_id.isdigit() else None,
            'q': q or None,
            'size': int(size_id) if size_id and size_id.isdigit() else None,
            'color': int(color_id) if color_id and color_id.isdigit() else None,
        }
    }
    return Response({'status': 'ok', 'data': data})


@api_view(['GET'])
def product_list(request):
    """Paginated product list endpoint for frontend consumers.
    Mirrors the product-listing logic from `home` but returns only products and pagination.
    """
    category_id = request.GET.get('category_id')
    q = request.GET.get('q')
    size_id = request.GET.get('size')
    color_id = request.GET.get('color')

    filters = []
    params = []
    if category_id and category_id.isdigit():
        filters.append('p.id_categoria = %s')
        params.append(int(category_id))
    if q:
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

    limit = request.GET.get('limit')
    page = request.GET.get('page')
    try:
        limit_v = min(max(int(limit), 1), 100) if limit else 12
    except ValueError:
        limit_v = 12
    try:
        page_v = max(int(page), 1) if page else 1
    except ValueError:
        page_v = 1
    offset_v = (page_v - 1) * limit_v

    count_query = f"""
        SELECT COUNT(DISTINCT p.id)
        FROM producto p
        LEFT JOIN variaciones_producto v ON v.id_producto = p.id
        {where_clause}
    """
    with connection.cursor() as cursor:
        cursor.execute(count_query, params)
        total_count_row = cursor.fetchone()
    total_count = int(total_count_row[0] if total_count_row else 0)

    data_query = f"""
        SELECT p.id, p.nombre, p.descripcion, p.precio, c.id AS categoria_id, c.nombre AS categoria_nombre, p.image_preview,
               COALESCE(SUM(v.stock), 0) AS stock_total
        FROM producto p
        LEFT JOIN categorias c ON c.id = p.id_categoria
        LEFT JOIN variaciones_producto v ON v.id_producto = p.id
        {where_clause}
        GROUP BY p.id, p.nombre, p.descripcion, p.precio, c.id, c.nombre, p.image_preview
        ORDER BY p.id DESC
        LIMIT %s OFFSET %s
    """
    with connection.cursor() as cursor:
        cursor.execute(data_query, [*params, limit_v, offset_v])
        prod_rows = cursor.fetchall()

    products = []
    for r in prod_rows:
        products.append({
            'id': r[0],
            'nombre': r[1],
            'descripcion': r[2],
            'precio': r[3],
            'precio_descuento': None,
            'categoria': {'id': r[4], 'nombre': r[5]} if r[4] is not None else None,
            'image_preview': r[6],
            'stock_total': int(r[7] or 0),
        })

    return Response({'status': 'ok', 'data': {'products': ProductoCardSerializer(products, many=True).data, 'pagination': {'page': page_v, 'limit': limit_v, 'total': total_count}}})


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
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT p.id, p.nombre, p.descripcion, p.precio, c.id AS categoria_id, c.nombre AS categoria_nombre, p.image_preview,
                   COALESCE(SUM(v.stock), 0) AS stock_total
            FROM producto p
            LEFT JOIN categorias c ON c.id = p.id_categoria
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

    images = []
    images_by_color = {}
    images_by_variant = {}
    try:
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

    related = []
    if product.get('categoria') and product['categoria'].get('id') is not None:
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT p.id, p.nombre, p.descripcion, p.precio, p.image_preview,
                       COALESCE(SUM(v.stock), 0) AS stock_total
                FROM producto p
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
            return Response({'status': 'not_supported', 'message': 'Tabla product_reviews no existe', 'detail': str(e)}, status=status.HTTP_501_NOT_IMPLEMENTED)

        return Response({'status': 'ok'})

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
            cursor.execute("SELECT nombre, precio, image_preview FROM producto WHERE id=%s", [pid])
            prow = cursor.fetchone()
        if not prow:
            return Response({'status': 'invalid', 'message': f'producto {pid} no existe'}, status=status.HTTP_400_BAD_REQUEST)
        name, price, image = prow[0], float(prow[1] or 0), prow[2]

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

    items = request.data.get('items') or []
    if not isinstance(items, list) or not items:
        return Response({'status': 'invalid', 'message': 'items requerido'}, status=status.HTTP_400_BAD_REQUEST)

    user_email = _get_user_email(request)

    try:
        with transaction.atomic():
            subtotal = 0.0
            price_cache = {}
            for it in items:
                pid = int(it.get('product_id'))
                qty = max(1, int(it.get('qty', 1)))
                size_id = it.get('size_id')
                color_id = it.get('color_id')

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

                if pid not in price_cache:
                    with connection.cursor() as cursor:
                        cursor.execute("SELECT precio, nombre, image_preview FROM producto WHERE id=%s", [pid])
                        prow = cursor.fetchone()
                    price_cache[pid] = {
                        'price': float(prow[0] or 0) if prow else 0.0,
                        'name': prow[1] if prow else f'Producto {pid}',
                        'image': prow[2] if prow else None,
                    }
                line_amount = price_cache[pid]['price'] * qty
                subtotal += line_amount

            igv = subtotal * 0.18
            total = subtotal + igv
            order_number = request.data.get('order_number') or None
            try:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        INSERT INTO orders (order_number, email, subtotal, igv, total, created_at)
                        VALUES (%s, %s, %s, %s, %s, NOW())
                        """,
                        [order_number, user_email, subtotal, igv, total]
                    )
                    cursor.execute("SELECT LAST_INSERT_ID()")
                    order_id = cursor.fetchone()[0]
                    if order_number is None:
                        cursor.execute("SELECT order_number FROM orders WHERE id=%s", [order_id])
                        order_number = cursor.fetchone()[0]
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
                import time
                order_number = order_number or f"SF{int(time.time())}"
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
            else:
                try:
                    from django.db import connection as _conn
                    with _conn.cursor() as _c:
                        _c.execute("SELECT id FROM CentroDistribucion LIMIT 1")
                        _r = _c.fetchone()
                        if _r:
                            center_id = _r[0]
                            _c.execute(
                                """
                                INSERT INTO Envio (order_id, id_centro_distribucion, destinatario, direccion, region_destino, email_destino, telefono_destino, status, creado_en)
                                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW())
                                """,
                                [order_number, center_id, destinatario, shipping_address, shipping_region, user_email, telefono_envio, 'CREADO']
                            )
                except Exception:
                    pass
        except Exception as e:
            webhook_status = f"error: {e}"

        return Response({'status': 'ok', 'order_number': order_number, 'tracking_url': tracking_url, 'webhook_status': webhook_status})
    except Exception as e:
        return Response({'status': 'error', 'message': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['POST'])
def payments_create_session(request):
    """Create a Stripe Checkout Session and return the hosted page URL.
    Body: { items:[{product_id, size_id?, color_id?, qty}], address_id?, userEmail? }
    """
    try:
        import stripe
    except Exception:
        return Response({'status': 'error', 'message': 'stripe no instalado en el backend'}, status=status.HTTP_501_NOT_IMPLEMENTED)

    items = request.data.get('items') or []
    if not isinstance(items, list) or not items:
        return Response({'status': 'invalid', 'message': 'items requerido'}, status=status.HTTP_400_BAD_REQUEST)

    email = _get_user_email(request)
    address_id = request.data.get('address_id')

    from rest_framework.test import APIRequestFactory
    factory = APIRequestFactory()
    preview_req = factory.post('/api/checkout/preview/', {
        'items': items,
        'address_id': address_id,
        'userEmail': email,
    }, format='json')
    try:
        preview_req.user = getattr(request, 'user', None)
    except Exception:
        pass
    preview_resp = checkout_preview(preview_req)
    if getattr(preview_resp, 'status_code', 200) != 200:
        return preview_resp
    preview_data = preview_resp.data.get('data', {})
    line_items_preview = preview_data.get('items', [])

    line_items = []
    currency = os.getenv('STRIPE_CURRENCY', 'pen')
    for it in line_items_preview:
        name = it.get('name') or f"Producto {it.get('product_id')}"
        amount_cents = int(round(float(it.get('price', 0.0)) * 100))
        qty = int(it.get('qty', 1))
        line_items.append({
            'price_data': {
                'currency': currency,
                'product_data': { 'name': name },
                'unit_amount': amount_cents,
            },
            'quantity': qty,
        })

    success_base = os.getenv('FRONTEND_URL', 'http://localhost:5173')
    cancel_base = success_base
    import time, json
    pre_order = request.data.get('pre_order') or f"SF{int(time.time())}"

    stripe.api_key = os.getenv('STRIPE_SECRET_KEY', '')
    if not stripe.api_key:
        return Response({'status': 'error', 'message': 'STRIPE_SECRET_KEY no configurado'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    try:
        session = stripe.checkout.Session.create(
            payment_method_types=['card'],
            mode='payment',
            line_items=line_items,
            allow_promotion_codes=True,
            success_url=f"{success_base}/checkout/success?order={pre_order}&session_id={{CHECKOUT_SESSION_ID}}",
            cancel_url=f"{cancel_base}/checkout/cancel",
            metadata={
                'order_number': pre_order,
                'email': email or '',
                'address_id': str(address_id or ''),
                'items': json.dumps(items, ensure_ascii=False),
            }
        )
    except Exception as e:
        return Response({'status': 'error', 'message': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    return Response({'status': 'ok', 'url': session.url})


@api_view(['POST'])
def payments_webhook(request):
    """Stripe webhook: on checkout.session.completed, confirm order and notify Admin.
    Requires STRIPE_WEBHOOK_SECRET env var.
    """
    try:
        import stripe
    except Exception:
        return Response(status=200)

    payload = request.body
    sig_header = request.META.get('HTTP_STRIPE_SIGNATURE')
    endpoint_secret = os.getenv('STRIPE_WEBHOOK_SECRET', '')
    if not endpoint_secret:
        return Response(status=200)
    try:
        event = stripe.Webhook.construct_event(payload, sig_header, endpoint_secret)
    except Exception:
        return Response(status=400)


    if event['type'] == 'checkout.session.completed':
        session = event['data']['object']
        metadata = session.get('metadata') or {}
        import json
        try:
            items = json.loads(metadata.get('items') or '[]')
        except Exception:
            items = []
        address_id = metadata.get('address_id') or None
        email = metadata.get('email') or None
        order_number = metadata.get('order_number') or None

        from rest_framework.test import APIRequestFactory
        factory = APIRequestFactory()
        req = factory.post('/api/checkout/confirm/', {
            'items': items,
            'address_id': address_id,
            'userEmail': email,
            'order_number': order_number,
        }, format='json')
        try:
            resp = checkout_confirm(req)
        except Exception:
            resp = None

        try:
            user_email = email
            destinatario = None
            telefono_envio = None
            shipping_address = None
            shipping_region = None
            try:
                from django.db import connection
                if address_id:
                    try:
                        addr_id_int = int(address_id)
                        with connection.cursor() as cursor:
                            cursor.execute(
                                """
                                SELECT nombre, direccion, region, telefono
                                FROM user_address
                                WHERE id=%s AND user_email=%s
                                """,
                                [addr_id_int, user_email]
                            )
                            row = cursor.fetchone()
                        if row:
                            destinatario = row[0]
                            shipping_address = row[1]
                            shipping_region = row[2]
                            telefono_envio = row[3]
                    except Exception:
                        row = None
                if not (shipping_address and shipping_region) and user_email:
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
                    if row:
                        destinatario = destinatario or row[0]
                        shipping_address = shipping_address or row[1]
                        shipping_region = shipping_region or row[2]
                        telefono_envio = telefono_envio or row[3]

            except Exception:
                pass

            admin_url = os.getenv('ADMIN_URL', 'http://localhost:8081')
            webhook_secret = os.getenv('WEBHOOK_SECRET', '')
            origin_region = 'Lima'
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

            try:
                log_path = os.path.join(os.path.dirname(__file__), 'webhook_debug.log')
                with open(log_path, 'a', encoding='utf-8') as lf:
                    import datetime
                    lf.write(f"\n--- {datetime.datetime.utcnow().isoformat()}Z Stripe webhook ---\n")
                    lf.write(f"admin_url={admin_url}\n")
                    lf.write(f"headers={headers}\n")
                    lf.write(f"payload={payload}\n")
            except Exception:
                pass

            try:
                r = requests.post(f"{admin_url}/api/internal/orders", json=payload, headers=headers, timeout=6)
                try:
                    with open(log_path, 'a', encoding='utf-8') as lf:
                        lf.write(f"response_status={getattr(r, 'status_code', 'n/a')}\n")
                        try:
                            lf.write(f"response_text={r.text}\n")
                        except Exception:
                            lf.write("response_text=<could not read>\n")
                except Exception:
                    pass
                try:
                    if not getattr(r, 'ok', False):
                        from django.db import connection as _conn2
                        with _conn2.cursor() as _c2:
                            _c2.execute("SELECT id FROM CentroDistribucion LIMIT 1")
                            _rr = _c2.fetchone()
                            if _rr:
                                cid = _rr[0]
                                _c2.execute(
                                    """
                                    INSERT INTO Envio (order_id, id_centro_distribucion, destinatario, direccion, region_destino, email_destino, telefono_destino, status, creado_en)
                                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW())
                                    """,
                                    [order_number, cid, payload.get('destinatario'), payload.get('direccion'), payload.get('regionDestino'), payload.get('email'), payload.get('telefono'), 'CREADO']
                                )
                except Exception:
                    pass
            except Exception as e:
                try:
                    with open(log_path, 'a', encoding='utf-8') as lf:
                        lf.write(f"request_exception={str(e)}\n")
                except Exception:
                    pass
        except Exception:
            pass

    return Response(status=200)


@api_view(['GET'])
def catalog_snapshot(request):
    """Return JSON snapshot (products + collections) for chatbot.
    Use ?force=1 to regenerate.
    """
    force = request.GET.get('force') == '1'
    if force:
        export_snapshot()
    snap = load_snapshot()
    if snap is None:
        export_snapshot()
        snap = load_snapshot() or {}
    return Response({'status': 'ok', 'data': snap})


@api_view(['POST'])
def subscribe(request):
    """Register an email subscription. Body: { email: 'user@example.com', name?: 'Nombre' }
    Returns 201 when created, 200 when already subscribed.
    """
    body = request.data or {}
    email = (body.get('email') or request.GET.get('email') or '').strip()
    name = body.get('name') or None
    if not email or '@' not in email:
        return Response({'status': 'invalid', 'message': 'email inválido'}, status=status.HTTP_400_BAD_REQUEST)
    # Store subscriber in a simple JSON file to avoid DB migrations.
    try:
        subs_file = Path(__file__).resolve().parent / 'subscribers.json'
        if subs_file.exists():
            data = json.loads(subs_file.read_text(encoding='utf-8') or '[]')
        else:
            data = []
        email_l = email.lower()
        exists = any(s.get('email') == email_l for s in data)
        if not exists:
            data.append({'email': email_l, 'name': name or '', 'created_at': __import__('datetime').datetime.utcnow().isoformat()})
            subs_file.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')
        return Response({'status': 'ok', 'created': not exists})
    except Exception as e:
        return Response({'status': 'error', 'message': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['POST'])
def notify_collection(request, col_id: int):
    """Internal endpoint to trigger notification for collection by id.
    Protect by header `X-Internal-Token` matching env `INTERNAL_TOKEN`.
    """
    token = request.META.get('HTTP_X_INTERNAL_TOKEN') or request.META.get('HTTP_X_INTERNAL_TOKEN'.lower())
    if not token or token != os.getenv('INTERNAL_TOKEN', ''):
        return Response({'status': 'forbidden'}, status=status.HTTP_403_FORBIDDEN)
    try:
        col = Coleccion.objects.get(id=col_id)
    except Exception:
        return Response({'status': 'not_found'}, status=status.HTTP_404_NOT_FOUND)

    def _notify():
        try:
            subs_file = Path(__file__).resolve().parent / 'subscribers.json'
            if not subs_file.exists():
                return
            subs = json.loads(subs_file.read_text(encoding='utf-8') or '[]')
            if not subs:
                return
            subject = f"Nueva colección en SmartFashion: {col.nombre}"
            plain = f"Hemos publicado una nueva colección: {col.nombre}\n\n{(col.descripcion or '')}\n\nVisítanos para ver los productos: /collections/{col.slug}/"
            html = f"<p>Hola,</p><p>Hemos publicado una nueva colección: <strong>{col.nombre}</strong></p>"
            if col.descripcion:
                html += f"<p>{col.descripcion}</p>"
            html += f"<p><a href=\"{getattr(settings, 'FRONTEND_URL', '') or '/collections/'+col.slug}\">Ver colección</a></p>"
            from_email = getattr(settings, 'DEFAULT_FROM_EMAIL', 'no-reply@smarthfashion.local')
            for s in subs:
                try:
                    to = s.get('email')
                    name = s.get('name') or ''
                    personalized_html = html.replace('<p>Hola,</p>', f'<p>Hola {name},</p>' if name else '<p>Hola,</p>')
                    msg = EmailMultiAlternatives(subject, plain, from_email, [to])
                    msg.attach_alternative(personalized_html, "text/html")
                    msg.send(fail_silently=True)
                except Exception:
                    continue
        except Exception:
            pass

    threading.Thread(target=_notify, daemon=True).start()
    return Response({'status': 'ok'})


def _build_catalog_context_for_ai(snap: dict) -> str:
    """Create a compact, grounded catalog summary for the LLM.
    Includes: name, category, price, colors (names), sizes (names), stock.
    """
    if not snap:
        return ""
    colors = {str(c.get('id')): (c.get('nombre') or '') for c in (snap.get('colors') or [])}
    sizes = {str(s.get('id')): (s.get('nombre') or '') for s in (snap.get('sizes') or [])}
    lines = []
    for p in (snap.get('products') or [])[:200]:
        cname = (p.get('categoria') or {}).get('nombre') or ''
        price = p.get('precio')
        stock = p.get('stock_total')
        vcolors, vsizes = set(), set()
        for v in (p.get('variantes') or []):
            if v.get('color_id') is not None:
                vcolors.add(colors.get(str(v.get('color_id')), ''))
            if v.get('talla_id') is not None:
                vsizes.add(sizes.get(str(v.get('talla_id')), ''))
        color_str = ', '.join(sorted([c for c in vcolors if c])) or '—'
        size_str = ', '.join(sorted([s for s in vsizes if s])) or '—'
        lines.append(f"- {p.get('nombre')} | categoria: {cname} | precio: {price} | colores: {color_str} | tallas: {size_str} | stock: {stock}")
    return "\n".join(lines)


@api_view(['POST'])
def chat_ai(request):
    """LLM-backed answer using Gemini.
    Body: { message: str, open_domain?: bool }
    Env: GEMINI_API_KEY required. GEMINI_MODEL optional (default gemini-1.5-flash)
    If open_domain=true the assistant can responder de forma más general, pero prioriza catálogo cuando aplica.
    """
    user_msg = (request.data.get('message') or '').strip()
    if not user_msg:
        return Response({'status': 'invalid', 'message': 'message requerido'}, status=status.HTTP_400_BAD_REQUEST)

    api_key = os.getenv('GEMINI_API_KEY', '')
    if not api_key:
        return Response({'status': 'not_configured', 'message': 'GEMINI_API_KEY no configurado en backend'}, status=status.HTTP_503_SERVICE_UNAVAILABLE)

    try:
        import google.generativeai as genai
    except Exception:
        return Response({'status': 'error', 'message': 'google-generativeai no instalado'}, status=status.HTTP_501_NOT_IMPLEMENTED)

    snap = load_snapshot() or {}
    catalog_context = _build_catalog_context_for_ai(snap)
    currency = (snap.get('currency') or 'PEN').upper()

    open_domain = bool(request.data.get('open_domain'))
    if open_domain:
        instructions = (
            "Eres un asistente de SmartFashion en español. Puedes conversar de forma general sobre moda, tallas, colores, recomendaciones. "
            "Cuando el usuario pida productos específicos usa el catálogo para datos concretos (precio, stock, colores, tallas). "
            "Si preguntas no pueden contestarse con catálogo (por ejemplo consejos de estilo) responde con sugerencias breves. "
            "Formato para listar productos (máx 5): Nombre — {moneda} precio — colores — tallas — stock. No inventes productos que no estén listados."
        )
    else:
        instructions = (
            "Eres un asistente de compras de SmartFashion en español. Usa SOLO el catálogo provisto para datos concretos. "
            "Si algo no está en el catálogo di que no lo encuentras sin inventar. "
            "Formato para listar productos (máx 5): Nombre — {moneda} precio — colores — tallas — stock. "
            "Respuestas breves y útiles."
        )
    prompt = (
        f"{instructions}\n\n"
        f"MONEDA: {currency}\n\n"
        f"CATALOGO:\n{catalog_context}\n\n"
        f"PREGUNTA DEL USUARIO:\n{user_msg}\n\n"
        f"RESPUESTA:"
    )

    try:
        genai.configure(api_key=api_key)
        model_name = os.getenv('GEMINI_MODEL', 'gemini-1.5-flash')
        model = genai.GenerativeModel(model_name)
        resp = model.generate_content(prompt)
        text = getattr(resp, 'text', None)
        if not text:
            try:
                text = resp.candidates[0].content.parts[0].text
            except Exception:
                text = ''
        if not text:
            return Response({'status': 'empty', 'message': 'Sin respuesta'}, status=status.HTTP_502_BAD_GATEWAY)
        return Response({'status': 'ok', 'data': {'answer': text}})
    except Exception as e:
        return Response({'status': 'error', 'message': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['GET'])
def chat_ai_status(request):
    """Quick health check for Gemini config. Returns model and a short sample response.
    Env: GEMINI_API_KEY (required), GEMINI_MODEL optional.
    """
    api_key = os.getenv('GEMINI_API_KEY', '')
    if not api_key:
        return Response({'status': 'not_configured', 'message': 'GEMINI_API_KEY no configurado'}, status=status.HTTP_503_SERVICE_UNAVAILABLE)
    try:
        import google.generativeai as genai
    except Exception:
        return Response({'status': 'error', 'message': 'google-generativeai no instalado'}, status=status.HTTP_501_NOT_IMPLEMENTED)

    try:
        genai.configure(api_key=api_key)
        model_name = os.getenv('GEMINI_MODEL', 'gemini-1.5-flash')
        model = genai.GenerativeModel(model_name)
        resp = model.generate_content("ping")
        text = getattr(resp, 'text', '') or ''
        return Response({'status': 'ok', 'model': model_name, 'sample': (text or '')[:200]})
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


@api_view(['GET'])
def collection_detail(request, slug: str):
    """Return a collection by slug with products limited to that collection and in-collection filters.
    Query params: size=<id>, color=<id>, min_price, max_price, q, page, limit
    """
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT id, nombre, slug, descripcion, image_url, orden
            FROM coleccion
            WHERE slug = %s AND activo = 1
            LIMIT 1
            """,
            [slug]
        )
        row = cursor.fetchone()
    if not row:
        return Response({'status': 'not_found'}, status=status.HTTP_404_NOT_FOUND)
    col = {
        'id': row[0], 'nombre': row[1], 'slug': row[2], 'descripcion': row[3], 'image_url': row[4], 'orden': int(row[5] or 0)
    }

    size_id = request.GET.get('size')
    color_id = request.GET.get('color')
    q = request.GET.get('q')
    min_price = request.GET.get('min_price')
    max_price = request.GET.get('max_price')

    filters = ["cp.id_coleccion = %s"]
    params = [col['id']]
    if size_id and size_id.isdigit():
        filters.append('EXISTS (SELECT 1 FROM variaciones_producto v2 WHERE v2.id_producto = p.id AND v2.id_talla = %s)')
        params.append(int(size_id))
    if color_id and color_id.isdigit():
        filters.append('EXISTS (SELECT 1 FROM variaciones_producto v3 WHERE v3.id_producto = p.id AND v3.id_color = %s)')
        params.append(int(color_id))
    if min_price:
        try:
            float(min_price)
            filters.append('p.precio >= %s')
            params.append(min_price)
        except Exception:
            pass
    if max_price:
        try:
            float(max_price)
            filters.append('p.precio <= %s')
            params.append(max_price)
        except Exception:
            pass
    if q:
        like = f"%{q}%"
        filters.append('(p.nombre LIKE %s OR p.descripcion LIKE %s)')
        params.extend([like, like])

    where_clause = ' AND '.join(filters)

    limit = request.GET.get('limit')
    page = request.GET.get('page')
    try:
        limit_v = min(max(int(limit), 1), 60) if limit else 12
    except Exception:
        limit_v = 12
    try:
        page_v = max(int(page), 1) if page else 1
    except Exception:
        page_v = 1
    offset_v = (page_v - 1) * limit_v

    with connection.cursor() as cursor:
        cursor.execute(
            f"""
            SELECT COUNT(DISTINCT p.id)
            FROM coleccionproducto cp
            JOIN producto p ON p.id = cp.id_producto
            LEFT JOIN variaciones_producto v ON v.id_producto = p.id
            WHERE {where_clause}
            """,
            params
        )
        total = cursor.fetchone()[0]

    with connection.cursor() as cursor:
        cursor.execute(
            f"""
            SELECT p.id, p.nombre, p.descripcion, p.precio,
                   c.id AS categoria_id, c.nombre AS categoria_nombre,
                   p.image_preview,
                   COALESCE(SUM(v.stock), 0) AS stock_total
            FROM coleccionproducto cp
            JOIN producto p ON p.id = cp.id_producto
            LEFT JOIN categorias c ON c.id = p.id_categoria
            LEFT JOIN variaciones_producto v ON v.id_producto = p.id
            WHERE {where_clause}
            GROUP BY p.id, p.nombre, p.descripcion, p.precio, c.id, c.nombre, p.image_preview
            ORDER BY p.id DESC
            LIMIT %s OFFSET %s
            """,
            [*params, limit_v, offset_v]
        )
        rows = cursor.fetchall()
    products = [
        {
            'id': r[0], 'nombre': r[1], 'descripcion': r[2], 'precio': r[3],
            'precio_descuento': None,
            'categoria': {'id': r[4], 'nombre': r[5]} if r[4] is not None else None,
            'image_preview': r[6], 'stock_total': int(r[7] or 0)
        }
        for r in rows
    ]

    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT DISTINCT t.id, t.nombre, t.tipo
            FROM coleccionproducto cp
            JOIN producto p ON p.id = cp.id_producto
            JOIN variaciones_producto v ON v.id_producto = p.id
            JOIN tallas t ON t.id = v.id_talla
            WHERE cp.id_coleccion = %s
            ORDER BY t.id ASC
            """,
            [col['id']]
        )
        size_rows = cursor.fetchall()
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT DISTINCT co.id, co.nombre, co.codigo_hex
            FROM coleccionproducto cp
            JOIN producto p ON p.id = cp.id_producto
            JOIN variaciones_producto v ON v.id_producto = p.id
            JOIN colores co ON co.id = v.id_color
            WHERE cp.id_coleccion = %s
            ORDER BY co.id ASC
            """,
            [col['id']]
        )
        color_rows = cursor.fetchall()

    sizes = [{'id': r[0], 'nombre': r[1], 'tipo': r[2]} for r in size_rows]
    colors = [{'id': r[0], 'nombre': r[1], 'hex': r[2]} for r in color_rows]

    return Response({
        'status': 'ok',
        'data': {
            'collection': col,
            'products': ProductoCardSerializer(products, many=True).data,
            'pagination': {'page': page_v, 'limit': limit_v, 'total': total},
            'filters': {'sizes': sizes, 'colors': colors},
            'applied_filters': {
                'size': int(size_id) if size_id and size_id.isdigit() else None,
                'color': int(color_id) if color_id and color_id.isdigit() else None,
                'min_price': float(min_price) if min_price else None,
                'max_price': float(max_price) if max_price else None,
                'q': q or None,
            }
        }
    })


@api_view(['GET', 'POST'])
def claims(request):
    """Create and list complaints for the authenticated user."""
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)

    if request.method == 'POST':
        body = request.data
        order_number = body.get('order_number')
        tipo = (body.get('tipo') or '').lower()
        detalle = body.get('detalle')
        telefono = body.get('telefono')
        if not order_number or tipo not in ['queja', 'reclamo'] or not detalle:
            return Response({'status': 'invalid', 'message': 'order_number, tipo(queja|reclamo) y detalle son requeridos'}, status=status.HTTP_400_BAD_REQUEST)
        comp = Complaint.objects.create(
            user_email=email,
            order_number=order_number,
            tipo=tipo,
            detalle=detalle,
        )
        try:
            admin_url = os.getenv('ADMIN_URL', 'http://localhost:8081')
            webhook_secret = os.getenv('WEBHOOK_SECRET', '')
            payload = {
                'id': comp.id,
                'orderNumber': order_number,
                'email': email,
                'telefono': telefono,
                'tipo': tipo,
                'detalle': detalle,
            }
            headers = {'Content-Type': 'application/json'}
            if webhook_secret:
                headers['X-Webhook-Token'] = webhook_secret
            requests.post(f"{admin_url}/api/internal/claims", json=payload, headers=headers, timeout=6)
        except Exception:
            pass
        return Response({'status': 'ok', 'id': comp.id}, status=status.HTTP_201_CREATED)

    qs = Complaint.objects.filter(user_email=email).order_by('-id')[:100]
    data = [
        {
            'id': c.id,
            'order_number': c.order_number,
            'tipo': c.tipo,
            'detalle': c.detalle,
            'estado': c.estado,
            'respuesta': c.respuesta,
            'created_at': c.created_at.isoformat(),
        }
        for c in qs
    ]
    return Response({'status': 'ok', 'data': data})


@api_view(['GET'])
def claim_detail(request, pk: int):
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)
    try:
        c = Complaint.objects.get(id=pk, user_email=email)
    except Complaint.DoesNotExist:
        return Response({'status': 'not_found'}, status=status.HTTP_404_NOT_FOUND)
    data = {
        'id': c.id,
        'order_number': c.order_number,
        'tipo': c.tipo,
        'detalle': c.detalle,
        'estado': c.estado,
        'respuesta': c.respuesta,
        'created_at': c.created_at.isoformat(),
        'updated_at': c.updated_at.isoformat(),
    }
    return Response({'status': 'ok', 'data': data})


@api_view(['GET', 'POST'])
def returns(request):
    """Create and list return requests for the authenticated user."""
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)

    if request.method == 'POST':
        body = request.data
        order_number = body.get('order_number')
        motivo = (body.get('motivo') or '').lower()
        descripcion = body.get('descripcion')
        metodo = (body.get('metodo') or '').lower()
        telefono = body.get('telefono')
        valid_motivos = ['talla_incorrecta', 'defectuoso', 'no_satisfecho', 'otro']
        valid_metodos = ['cambio', 'reembolso']
        if not order_number or motivo not in valid_motivos or metodo not in valid_metodos:
            return Response({'status': 'invalid', 'message': 'order_number, motivo y metodo son requeridos'}, status=status.HTTP_400_BAD_REQUEST)
        rr = ReturnRequest.objects.create(
            user_email=email,
            order_number=order_number,
            motivo=motivo,
            descripcion=descripcion,
            metodo=metodo,
        )
        try:
            admin_url = os.getenv('ADMIN_URL', 'http://localhost:8081')
            webhook_secret = os.getenv('WEBHOOK_SECRET', '')
            payload = {
                'id': rr.id,
                'orderNumber': order_number,
                'email': email,
                'telefono': telefono,
                'motivo': motivo,
                'descripcion': descripcion,
                'metodo': metodo,
            }
            headers = {'Content-Type': 'application/json'}
            if webhook_secret:
                headers['X-Webhook-Token'] = webhook_secret
            requests.post(f"{admin_url}/api/internal/returns", json=payload, headers=headers, timeout=6)
        except Exception:
            pass
        return Response({'status': 'ok', 'id': rr.id}, status=status.HTTP_201_CREATED)

    qs = ReturnRequest.objects.filter(user_email=email).order_by('-id')[:100]
    data = [
        {
            'id': r.id,
            'order_number': r.order_number,
            'motivo': r.motivo,
            'descripcion': r.descripcion,
            'metodo': r.metodo,
            'estado': r.estado,
            'created_at': r.created_at.isoformat(),
        }
        for r in qs
    ]
    return Response({'status': 'ok', 'data': data})


@api_view(['GET'])
def return_detail(request, pk: int):
    email = _get_user_email(request)
    if not email:
        return Response({'status': 'invalid', 'message': 'email requerido'}, status=status.HTTP_400_BAD_REQUEST)
    try:
        r = ReturnRequest.objects.get(id=pk, user_email=email)
    except ReturnRequest.DoesNotExist:
        return Response({'status': 'not_found'}, status=status.HTTP_404_NOT_FOUND)
    data = {
        'id': r.id,
        'order_number': r.order_number,
        'motivo': r.motivo,
        'descripcion': r.descripcion,
        'metodo': r.metodo,
        'estado': r.estado,
        'created_at': r.created_at.isoformat(),
        'updated_at': r.updated_at.isoformat(),
    }
    return Response({'status': 'ok', 'data': data})
