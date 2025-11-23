import os,sys,json
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)
os.environ.setdefault('DJANGO_SETTINGS_MODULE','config.settings')
try:
    import django
    django.setup()
except Exception as e:
    print(json.dumps({'error': str(e)}))
    sys.exit(1)
from django.db import connection
with connection.cursor() as c:
    c.execute('SELECT id, nombre, region FROM CentroDistribucion')
    rows = c.fetchall()
print(json.dumps({'status':'ok','data': rows}, default=str))
