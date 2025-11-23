import os
import sys
import json

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
        cursor.execute("ALTER TABLE orders ADD COLUMN status VARCHAR(32) NULL AFTER created_at")
    print(json.dumps({'status': 'ok', 'msg': 'column added'}))
except Exception as e:
    print(json.dumps({'status': 'skipped_or_error', 'error': str(e)}))
    sys.exit(0)
