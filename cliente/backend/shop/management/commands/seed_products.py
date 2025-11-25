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
            # obtener categorías (crear básicas si faltan)
            try:
                cursor.execute("SELECT id, nombre FROM categorias ORDER BY id ASC")
                categorias = cursor.fetchall()
            except Exception:
                categorias = []

            if not categorias:
                self.stdout.write('No se encontraron categorías — insertando categorías demo...')
                demo_cats = ['Ropa', 'Calzado', 'Accesorios']
                for c in demo_cats:
                    cursor.execute("INSERT INTO categorias (nombre) VALUES (%s)", (c,))
                cursor.execute("SELECT id, nombre FROM categorias ORDER BY id ASC")
                categorias = cursor.fetchall()

            # obtener colores
            try:
                cursor.execute("SELECT id, nombre FROM colores ORDER BY id ASC")
                colores = [r[0] for r in cursor.fetchall()]
            except Exception:
                colores = []

            if not colores:
                self.stdout.write('No se encontraron colores — insertando colores demo...')
                demo_colors = [('Negro', '#000000'), ('Blanco', '#FFFFFF'), ('Rojo', '#FF0000')]
                for nombre, hexcode in demo_colors:
                    cursor.execute("INSERT INTO colores (nombre, codigo_hex) VALUES (%s, %s)", (nombre, hexcode))
                cursor.execute("SELECT id FROM colores")
                colores = [r[0] for r in cursor.fetchall()]

            # obtener tallas
            try:
                cursor.execute("SELECT id, nombre FROM tallas ORDER BY id ASC")
                tallas = [r[0] for r in cursor.fetchall()]
            except Exception:
                tallas = []

            if not tallas:
                self.stdout.write('No se encontraron tallas — insertando tallas demo...')
                demo_sizes = [('S', 'estandar'), ('M', 'estandar'), ('L', 'estandar')]
                for nombre, tipo in demo_sizes:
                    cursor.execute("INSERT INTO tallas (nombre, tipo) VALUES (%s, %s)", (nombre, tipo))
                cursor.execute("SELECT id FROM tallas")
                tallas = [r[0] for r in cursor.fetchall()]

            created = []
            for i in range(count):
                cat = random.choice(categorias)
                nombre = f"Demo Producto {int(time.time()) % 100000}-{i}"
                descripcion = f"Producto de demostración en categoria {cat[1]}"
                precio = rnd_price()
                image_preview = '/assets/default-product.jpg'

                # insertar producto
                cursor.execute(
                    "INSERT INTO producto (nombre, descripcion, precio, id_categoria, image_preview) VALUES (%s, %s, %s, %s, %s)",
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
