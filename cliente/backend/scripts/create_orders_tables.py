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

create_statements = [
    """
    CREATE TABLE IF NOT EXISTS orders (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      order_number VARCHAR(32) NOT NULL UNIQUE,
      email VARCHAR(255),
      subtotal DECIMAL(10,2) NOT NULL,
      igv DECIMAL(10,2) NOT NULL,
      total DECIMAL(10,2) NOT NULL,
      created_at DATETIME NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """,

    """
    CREATE TABLE IF NOT EXISTS order_items (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      order_id BIGINT NOT NULL,
      product_id BIGINT NOT NULL,
      size_id BIGINT NULL,
      color_id BIGINT NULL,
      qty INT NOT NULL,
      unit_price DECIMAL(10,2) NOT NULL,
      amount DECIMAL(10,2) NOT NULL,
      name VARCHAR(255),
      image VARCHAR(1024),
      CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """,

    """
    ALTER TABLE orders ADD COLUMN IF NOT EXISTS status VARCHAR(32) NULL AFTER created_at;
    """,

    """
    UPDATE orders
    SET status = 'PAGADO'
    WHERE status IS NULL AND id > 0;
    """
]

results = []
with connection.cursor() as cursor:
    for stmt in create_statements:
        s = stmt.strip()
        if not s:
            continue
        try:
            if s.upper().startswith('ALTER TABLE ORDERS ADD COLUMN') and 'STATUS' in s.upper():
                cursor.execute("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'status'")
                exists = cursor.fetchone()[0]
                if not exists:
                    cursor.execute("ALTER TABLE orders ADD COLUMN status VARCHAR(32) NULL AFTER created_at")
                    results.append({'stmt': 'ALTER TABLE orders ADD COLUMN status', 'ok': True})
                else:
                    results.append({'stmt': 'ALTER TABLE orders ADD COLUMN status', 'ok': 'already_exists'})
                continue

            cursor.execute(s)
            results.append({'stmt': s.splitlines()[0], 'ok': True})
        except Exception as e:
            results.append({'stmt': s.splitlines()[0], 'error': str(e)})

print(json.dumps({'status': 'done', 'results': results}, ensure_ascii=False))
