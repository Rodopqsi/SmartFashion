import random
import time
from decimal import Decimal

from django.core.management.base import BaseCommand
from django.db import connection, transaction


def rnd_price():
    return Decimal(str(round(random.uniform(19.99, 199.99), 2)))


class Command(BaseCommand):
    help = 'Crear productos de ejemplo usando categorías, tallas y colores existentes'

    def add_arguments(self, parser):
        parser.add_argument('--count', type=int, default=5, help='Número de productos a crear')

    def handle(self, *args, **options):
        count = options.get('count', 5)

        with connection.cursor() as cursor:
            # obtener categorías
            cursor.execute("SELECT id, nombre FROM Categorias ORDER BY id ASC")
            categorias = cursor.fetchall()
            if not categorias:
                self.stderr.write('No hay categorías en la base de datos. Crea categorías antes.')
                return

            # obtener colores
            cursor.execute("SELECT id, nombre FROM colores ORDER BY id ASC")
            colores = [r[0] for r in cursor.fetchall()]
            if not colores:
                self.stderr.write('No hay colores en la base de datos. Crea colores antes.')
                return

            # obtener tallas
            cursor.execute("SELECT id, nombre FROM tallas ORDER BY id ASC")
            tallas = [r[0] for r in cursor.fetchall()]
            if not tallas:
                self.stderr.write('No hay tallas en la base de datos. Crea tallas antes.')
                return

            created = []
            for i in range(count):
                cat = random.choice(categorias)
                nombre = f"Demo Producto {int(time.time()) % 100000}-{i}"
                descripcion = f"Producto de demostración en categoria {cat[1]}"
                precio = rnd_price()
                image_preview = '/assets/default-product.jpg'

                # insertar producto
                cursor.execute(
                    "INSERT INTO Producto (nombre, descripcion, precio, id_categoria, image_preview) VALUES (%s, %s, %s, %s, %s)",
                    (nombre, descripcion, precio, cat[0], image_preview),
                )
                cursor.execute("SELECT LAST_INSERT_ID()")
                prod_id = cursor.fetchone()[0]

                # variaciones: crear combinaciones de 1-3 colores y 1-3 tallas
                sel_colors = random.sample(colores, min(len(colores), random.randint(1, 3)))
                sel_sizes = random.sample(tallas, min(len(tallas), random.randint(1, 3)))
                total_stock = 0
                for c in sel_colors:
                    for t in sel_sizes:
                        stock = random.randint(5, 50)
                        total_stock += stock
                        cursor.execute(
                            "INSERT INTO variaciones_producto (id_producto, id_talla, id_color, stock) VALUES (%s, %s, %s, %s)",
                            (prod_id, t, c, stock),
                        )

                # imagenes: una por color
                for c in sel_colors[:3]:
                    img_url = f"/assets/products/{prod_id}_c{c}.jpg"
                    cursor.execute(
                        "INSERT INTO imagenes_producto (id_color, url, id_producto) VALUES (%s, %s, %s)",
                        (c, img_url, prod_id),
                    )

                created.append({'id': prod_id, 'nombre': nombre, 'categoria': cat[1], 'precio': str(precio), 'stock': total_stock})

            # commit
            transaction.set_autocommit(True)

        self.stdout.write(self.style.SUCCESS(f'Productos creados: {len(created)}'))
        for p in created:
            self.stdout.write(f"- {p['id']}: {p['nombre']} | categoria: {p['categoria']} | precio: {p['precio']} | stock_total: {p['stock']}")
