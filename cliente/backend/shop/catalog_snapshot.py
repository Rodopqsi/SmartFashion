import os
import json
import hashlib
import tempfile
from datetime import datetime, timezone
from django.db import connection


SNAPSHOT_REL_PATH = os.path.join('shop', 'data', 'products_snapshot.json')


def _base_dir():
    from django.conf import settings
    return getattr(settings, 'BASE_DIR', os.getcwd())


def snapshot_path():
    base = _base_dir()
    path = os.path.join(base, SNAPSHOT_REL_PATH)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    return path


def generate_products_snapshot() -> dict:
    """Build an in-memory snapshot of products, variants, images, ratings & collections.
    Returns the dict; caller is responsible for writing atomically.
    """
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT p.id, p.nombre, p.descripcion, p.precio,
                   c.id AS categoria_id, c.nombre AS categoria_nombre,
                   p.image_preview,
                   COALESCE(SUM(v.stock), 0) AS stock_total
            FROM Producto p
            LEFT JOIN Categorias c ON c.id = p.id_categoria
            LEFT JOIN variaciones_producto v ON v.id_producto = p.id
            GROUP BY p.id, p.nombre, p.descripcion, p.precio, c.id, c.nombre, p.image_preview
            ORDER BY p.id ASC
            """
        )
        product_rows = cursor.fetchall()

    product_ids = [r[0] for r in product_rows]

    variants_by_product = {pid: [] for pid in product_ids}
    if product_ids:
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id, id_producto, id_talla, id_color, stock
                FROM variaciones_producto
                WHERE id_producto IN (%s)
                ORDER BY id ASC
                """ % (','.join(str(pid) for pid in product_ids))
            )
            for vid, pid, talla_id, color_id, stock in cursor.fetchall():
                variants_by_product[pid].append({
                    'id': vid,
                    'talla_id': talla_id,
                    'color_id': color_id,
                    'stock': stock,
                })

    images_general = {pid: [] for pid in product_ids}
    images_by_color = {pid: {} for pid in product_ids}
    if product_ids:
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id_producto, id_color, url
                FROM imagenes_producto
                WHERE id_producto IN (%s)
                ORDER BY id ASC
                """ % (','.join(str(pid) for pid in product_ids))
            )
            for pid, color_id, url in cursor.fetchall():
                if color_id is None:
                    images_general[pid].append(url)
                else:
                    images_by_color[pid].setdefault(str(color_id), []).append(url)

    avg_rating = {pid: 0.0 for pid in product_ids}
    rating_count = {pid: 0 for pid in product_ids}
    if product_ids:
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT product_id, AVG(rating) AS avg_r, COUNT(*) AS cnt
                FROM product_reviews
                WHERE product_id IN (%s)
                GROUP BY product_id
                """ % (','.join(str(pid) for pid in product_ids))
            )
            for pid, a, c in cursor.fetchall():
                avg_rating[pid] = float(a or 0.0)
                rating_count[pid] = int(c or 0)

    collections = []
    with connection.cursor() as cursor:
        cursor.execute(
            """
            SELECT id, nombre, slug, descripcion, orden
            FROM Coleccion
            WHERE activo = 1
            ORDER BY orden ASC, id DESC
            """
        )
        col_rows = cursor.fetchall()
    for col in col_rows:
        col_id, nombre, slug, descripcion, orden = col
        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT id_producto
                FROM ColeccionProducto
                WHERE id_coleccion = %s
                ORDER BY id_producto DESC
                LIMIT 50
                """,
                [col_id]
            )
            prod_ids = [r[0] for r in cursor.fetchall()]
        collections.append({
            'id': col_id,
            'nombre': nombre,
            'slug': slug,
            'descripcion': descripcion,
            'orden': orden,
            'product_ids': prod_ids,
        })

    products = []
    for r in product_rows:
        pid = r[0]
        products.append({
            'id': pid,
            'nombre': r[1],
            'descripcion': r[2],
            'precio': float(r[3]),
            'precio_descuento': None,
            'categoria': {'id': r[4], 'nombre': r[5]} if r[4] is not None else None,
            'image_preview': r[6],
            'stock_total': int(r[7] or 0),
            'avg_rating': avg_rating.get(pid, 0.0),
            'rating_count': rating_count.get(pid, 0),
            'variantes': variants_by_product.get(pid, []),
            'imagenes': {
                'general': images_general.get(pid, []),
                'por_color': images_by_color.get(pid, {}),
            },
        })

    colors = []
    with connection.cursor() as cursor:
        try:
            cursor.execute("SELECT id, nombre, codigo_hex FROM colores ORDER BY id ASC")
            colors = [ {'id': r[0], 'nombre': r[1], 'codigo_hex': r[2]} for r in cursor.fetchall() ]
        except Exception:
            colors = []

    sizes = []
    with connection.cursor() as cursor:
        try:
            cursor.execute("SELECT id, nombre, tipo FROM tallas ORDER BY id ASC")
            sizes = [ {'id': r[0], 'nombre': r[1], 'tipo': r[2]} for r in cursor.fetchall() ]
        except Exception:
            sizes = []

    snapshot = {
        'generated_at': datetime.now(timezone.utc).isoformat(),
        'currency': os.getenv('STRIPE_CURRENCY', 'pen').upper(),
        'stats': {
            'total_products': len(products),
            'total_collections': len(collections),
        },
        'products': products,
        'collections': collections,
        'colors': colors,
        'sizes': sizes,
        'meta': {
            'schema_version': 1,
            'source': 'raw_sql',
        }
    }
    return snapshot


def _compute_hash(snapshot: dict) -> str:
    core = {
        'currency': snapshot.get('currency'),
        'products': snapshot.get('products'),
        'collections': snapshot.get('collections'),
        'colors': snapshot.get('colors'),
        'sizes': snapshot.get('sizes'),
    }
    data = json.dumps(core, ensure_ascii=False, separators=(',', ':'), sort_keys=True)
    return hashlib.sha256(data.encode('utf-8')).hexdigest()


def write_snapshot_file(snapshot: dict) -> str:
    path = snapshot_path()
    try:
        if os.path.exists(path):
            with open(path, 'r', encoding='utf-8') as f:
                existing = json.load(f)
            if _compute_hash({'products':existing.get('products'), 'collections':existing.get('collections'), 'colors':existing.get('colors'), 'sizes':existing.get('sizes'), 'currency':existing.get('currency')}) == _compute_hash(snapshot):
                return path
    except Exception:
        pass

    try:
        snapshot.setdefault('meta', {})['content_hash'] = _compute_hash(snapshot)
    except Exception:
        pass

    data = json.dumps(snapshot, ensure_ascii=False, separators=(',', ':'), indent=2)
    fd, tmp_path = tempfile.mkstemp(prefix='snap_', suffix='.json', dir=os.path.dirname(path))
    try:
        with os.fdopen(fd, 'w', encoding='utf-8') as f:
            f.write(data)
        os.replace(tmp_path, path)
    finally:
        try:
            if os.path.exists(tmp_path):
                os.remove(tmp_path)
        except Exception:
            pass
    return path


def export_snapshot() -> str:
    snapshot = generate_products_snapshot()
    return write_snapshot_file(snapshot)


def load_snapshot() -> dict | None:
    path = snapshot_path()
    if not os.path.exists(path):
        return None
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception:
        return None
