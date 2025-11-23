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
        cursor.execute("INSERT INTO orders (order_number, email, subtotal, igv, total, created_at) VALUES (%s,%s,%s,%s,%s,NOW())", ('TESTORDER123','test@example.com',10.0,1.8,11.8))
        cursor.execute("SELECT LAST_INSERT_ID()")
        oid = cursor.fetchone()[0]
    print(json.dumps({'status':'ok','inserted_id': oid}))
except Exception as e:
    print(json.dumps({'status':'error','error': str(e)}))
    sys.exit(1)
