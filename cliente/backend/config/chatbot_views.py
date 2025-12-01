from django.http import JsonResponse
from django.views.decorators.http import require_POST
from django.views.decorators.csrf import csrf_exempt
from django.db import connection
import json
import re

@csrf_exempt
@require_POST
def query(request):
    try:
        payload = json.loads(request.body.decode('utf-8')) if request.body else {}
    except Exception:
        payload = {}
    question_raw = (payload.get('q') or '').strip()
    question = question_raw.lower()
    resp = { 'ok': True, 'question': question_raw }

    question_norm = question
    is_today = any(w in question_norm for w in ['hoy','dia','día'])
    try:
        with connection.cursor() as cursor:
            if not question_norm:
                resp['message'] = 'Pregunta vacía'
                return JsonResponse(resp)

            # Ventas / ingresos hoy
            if (('ventas' in question_norm) or ('ingresos' in question_norm)) and is_today:
                cursor.execute("SELECT COALESCE(SUM(total),0) AS total, COUNT(*) AS pedidos FROM orders WHERE DATE(created_at)=CURRENT_DATE")
                row = cursor.fetchone() or (0,0)
                resp['type'] = 'kpi'
                resp['data'] = { 'total': float(row[0] or 0), 'pedidos': int(row[1] or 0) }
                return JsonResponse(resp)

            # Top productos últimos 30 días
            if ('top productos' in question_norm) or ('mas vendidos' in question_norm) or ('más vendidos' in question_norm):
                cursor.execute("""
                    SELECT oi.product_id, MAX(oi.name) AS name, SUM(oi.qty) AS qty, SUM(oi.amount) AS revenue
                    FROM order_items oi JOIN orders o ON oi.order_id=o.id
                    WHERE o.created_at>=DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
                    GROUP BY oi.product_id
                    ORDER BY qty DESC
                    LIMIT 5
                """)
                rows = cursor.fetchall() or []
                resp['type'] = 'table'
                resp['columns'] = ['Producto','Cantidad','Ingresos']
                resp['data'] = [ { 'product_id': r[0], 'name': r[1], 'qty': int(r[2] or 0), 'revenue': float(r[3] or 0) } for r in rows ]
                return JsonResponse(resp)

            # Pedidos hoy
            if ('pedidos' in question_norm or 'ordenes' in question_norm or 'órdenes' in question_norm) and is_today:
                cursor.execute("SELECT id, order_number, total FROM orders WHERE DATE(created_at)=CURRENT_DATE ORDER BY created_at DESC LIMIT 10")
                rows = cursor.fetchall() or []
                resp['type'] = 'list'
                resp['data'] = [ { 'id': r[0], 'order_number': r[1], 'total': float(r[2] or 0) } for r in rows ]
                return JsonResponse(resp)

            # Nuevos productos / novedades (sin campo created_at: usamos id descendente)
            if any(w in question_norm for w in ['nuevos','novedades','recientes','agregados']):
                cursor.execute("""
                    SELECT p.id, p.nombre, p.precio, c.nombre AS categoria, p.image_preview
                    FROM producto p LEFT JOIN categorias c ON p.id_categoria=c.id
                    ORDER BY p.id DESC
                    LIMIT 8
                """)
                rows = cursor.fetchall() or []
                resp['type'] = 'products'
                resp['data'] = [ {
                    'id': r[0], 'nombre': r[1], 'precio': float(r[2] or 0),
                    'categoria': r[3], 'image_preview': r[4]
                } for r in rows ]
                return JsonResponse(resp)

            # Productos por categoría (ej: categoria hombres / productos hombres)
            # Detectar palabra clave de categoría con patrones tolerantes
            cat_match = None
            patterns = [
                r'categoria\s+(\w+)',
                r'categoría\s+(\w+)',
                r'productos?(?:\s+\w+){0,3}\s+de\s+(\w+)',
                r'productos?(?:\s+\w+){0,3}\s+para\s+(\w+)',
                r'productos?\s+(\w+)' 
            ]
            for pat in patterns:
                m = re.search(pat, question_norm)
                if m:
                    cat_match = m.group(1)
                    break
            # Heurística directa por palabras clave si no capturó regex
            if not cat_match:
                if any(w in question_norm for w in ['hombre','hombres','caballero','caballeros']):
                    cat_match = 'hombre'
                elif any(w in question_norm for w in ['mujer','mujeres','dama','damas']):
                    cat_match = 'mujer'
            if cat_match:
                # Normalizar posibles pluralizaciones simples (hombres -> hombre)
                cat_key = cat_match.lower()
                if cat_key.endswith('s'):
                    cat_key_singular = cat_key[:-1]
                else:
                    cat_key_singular = cat_key
                cursor.execute("""
                    SELECT p.id, p.nombre, p.precio, c.nombre AS categoria, p.image_preview
                    FROM producto p LEFT JOIN categorias c ON p.id_categoria=c.id
                    WHERE LOWER(c.nombre) LIKE %s OR LOWER(c.nombre) LIKE %s
                    LIMIT 12
                """, [cat_key+'%', cat_key_singular+'%'])
                rows = cursor.fetchall() or []
                if rows:
                    resp['type'] = 'products'
                    resp['data'] = [ {
                        'id': r[0], 'nombre': r[1], 'precio': float(r[2] or 0),
                        'categoria': r[3], 'image_preview': r[4]
                    } for r in rows ]
                    return JsonResponse(resp)

            # Presupuesto / rango de precio (ej: con 50 soles, menor a 80, hasta 120)
            amt = None
            m_amt = re.search(r'(?:con|hasta|menos\s+de|menor\s+a|<=|=|\bpor\b)\s*(\d+[\.,]?\d*)\s*(?:s\/|s\\/|soles|pen)?', question_norm)
            if not m_amt:
                m_amt = re.search(r'(\d+[\.,]?\d*)\s*(?:s\/|s\\/|soles|pen)\b', question_norm)
            if m_amt:
                try:
                    amt = float(m_amt.group(1).replace(',', '.'))
                except Exception:
                    amt = None
            if amt is not None:
                cursor.execute("""
                    SELECT p.id, p.nombre, p.precio, c.nombre AS categoria, p.image_preview
                    FROM producto p LEFT JOIN categorias c ON p.id_categoria=c.id
                    WHERE p.precio <= %s
                    ORDER BY p.precio ASC, p.id DESC
                    LIMIT 12
                """, [amt])
                rows = cursor.fetchall() or []
                if rows:
                    resp['type'] = 'products'
                    resp['info'] = { 'budget': amt }
                    resp['data'] = [ {
                        'id': r[0], 'nombre': r[1], 'precio': float(r[2] or 0),
                        'categoria': r[3], 'image_preview': r[4]
                    } for r in rows ]
                    return JsonResponse(resp)


            resp['type'] = 'none'
            resp['data'] = []
    except Exception as e:
        resp['ok'] = False
        resp['error'] = str(e)
    return JsonResponse(resp)
