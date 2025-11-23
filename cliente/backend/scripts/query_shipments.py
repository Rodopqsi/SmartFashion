import os, sys, json
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

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
        cursor.execute("SELECT id, order_id, direccion, region_destino, email_destino, telefono_destino, status, creado_en FROM Envio ORDER BY id DESC LIMIT 20")
        rows = cursor.fetchall()
    out = []
    for r in rows:
        out.append({
            'id': int(r[0]) if r[0] is not None else None,
            'order_id': r[1],
            'direccion': r[2],
            'region_destino': r[3],
            'email_destino': r[4],
            'telefono_destino': r[5],
            'status': r[6],
            'creado_en': r[7].isoformat() if getattr(r[7], 'isoformat', None) else str(r[7])
        })
    print(json.dumps({'status': 'ok', 'data': out}, ensure_ascii=False))
except Exception as e:
    print(json.dumps({'error': str(e)}))
    sys.exit(1)
