import os
import json
import sys

# Añadir la ruta del backend al path para que Django y el paquete 'config' sean importables
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

# Ajustar a la ruta del proyecto Django (modulo settings en 'config')
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
try:
    import django
    django.setup()
except Exception as e:
    print(json.dumps({'error': f'django setup failed: {e}'}))
    sys.exit(1)

from django.db import connection

try:
    with connection.cursor() as cursor:
        cursor.execute("SELECT id, order_number, email, total, created_at FROM orders ORDER BY id DESC LIMIT 5")
        rows = cursor.fetchall()
    out = []
    for r in rows:
        out.append({
            'id': int(r[0]) if r[0] is not None else None,
            'order_number': r[1],
            'email': r[2],
            'total': float(r[3]) if r[3] is not None else None,
            'created_at': r[4].isoformat() if getattr(r[4], 'isoformat', None) else str(r[4])
        })
    print(json.dumps({'status': 'ok', 'data': out}, ensure_ascii=False))
except Exception as e:
    print(json.dumps({'error': str(e)}))
    sys.exit(1)
