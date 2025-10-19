from django.db import connection
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .serializers import CategoriaSerializer, ProductoCardSerializer


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
