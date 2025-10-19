from django.core.management.base import BaseCommand
from django.db import connection, transaction


SQL_INSERTS = {
    'categorias': [
        ("Hombres",),
        ("Mujeres",),
        ("Niños",),
        ("Accesorios",),
    ],
    'productos': [
        # nombre, descripcion, precio, id_categoria, image_preview
        ("Camiseta Básica Blanca", "Camiseta de algodón suave, corte regular.", 29.90, None, "https://picsum.photos/seed/camiblanca/400/300"),
        ("Jeans Slim Azul", "Jeans denim lavado medio, corte slim.", 119.00, None, "https://picsum.photos/seed/jeansazul/400/300"),
        ("Gorra Negra", "Gorra ajustable color negro con logo minimal.", 45.50, None, "https://picsum.photos/seed/gorranegra/400/300"),
        ("Sneakers Urban", "Zapatillas urbanas confort espuma memory.", 199.90, None, "https://picsum.photos/seed/sneakersurban/400/300"),
    ],
}


class Command(BaseCommand):
    help = "Inserta datos de demostración en las tablas basadas en el esquema existente."

    def handle(self, *args, **options):
        with transaction.atomic():
            self._seed_categories()
            self._seed_products()
            self._seed_variations_and_images()
        self.stdout.write(self.style.SUCCESS("Datos demo insertados / actualizados"))

    def _fetch_ids(self, table):
        with connection.cursor() as cursor:
            cursor.execute(f"SELECT id FROM {table}")
            return {row[0] for row in cursor.fetchall()}

    def _seed_categories(self):
        existing = self._fetch_ids('Categorias')
        next_needed = len(SQL_INSERTS['categorias']) - len(existing)
        if next_needed <= 0:
            return
        self.stdout.write("Insertando categorías demo...")
        with connection.cursor() as cursor:
            for name in SQL_INSERTS['categorias']:
                cursor.execute("INSERT INTO Categorias (nombre) VALUES (%s)", name)

    def _seed_products(self):
        # Mapear categorías por nombre
        with connection.cursor() as cursor:
            cursor.execute("SELECT id, nombre FROM Categorias")
            cats = cursor.fetchall()
        name_to_id = {c[1]: c[0] for c in cats}

        productos = []
        # Asignar categorías disponibles en orden
        cat_ids = list(name_to_id.values()) or [None]
        for idx, p in enumerate(SQL_INSERTS['productos']):
            nombre, desc, precio, _cat_placeholder, img = p
            cat_id = cat_ids[idx % len(cat_ids)]
            productos.append((nombre, desc, precio, cat_id, img))

        with connection.cursor() as cursor:
            # Evitar duplicados por nombre
            cursor.execute("SELECT nombre FROM Producto")
            existing_names = {r[0] for r in cursor.fetchall()}
            inserted = 0
            for prod in productos:
                if prod[0] in existing_names:
                    continue
                cursor.execute(
                    "INSERT INTO Producto (nombre, descripcion, precio, id_categoria, image_preview) VALUES (%s, %s, %s, %s, %s)",
                    prod,
                )
                inserted += 1
        if inserted:
            self.stdout.write(f"{inserted} productos demo insertados")

    def _seed_variations_and_images(self):
        # Insertar tallas y colores básicos si vacíos
        with connection.cursor() as cursor:
            cursor.execute("SELECT COUNT(*) FROM tallas")
            if cursor.fetchone()[0] == 0:
                for nombre, tipo in [("S", "ROPA"), ("M", "ROPA"), ("L", "ROPA")]:
                    cursor.execute("INSERT INTO tallas (nombre, tipo) VALUES (%s, %s)", (nombre, tipo))
            cursor.execute("SELECT COUNT(*) FROM colores")
            if cursor.fetchone()[0] == 0:
                for nombre, hexcode in [("Blanco", "#FFFFFF"), ("Negro", "#000000"), ("Azul", "#0000FF")]:
                    cursor.execute("INSERT INTO colores (nombre, codigo_hex) VALUES (%s, %s)", (nombre, hexcode))

            # Obtener ids
            cursor.execute("SELECT id FROM tallas")
            tallas = [r[0] for r in cursor.fetchall()]
            cursor.execute("SELECT id FROM colores")
            colores = [r[0] for r in cursor.fetchall()]
            cursor.execute("SELECT id, image_preview FROM Producto")
            productos = cursor.fetchall()

            # Variaciones (una por talla-color para primeros productos limitados)
            for prod_id, _img in productos:
                for t in tallas:
                    for c in colores[:2]:  # limitar colores
                        # comprobar existencia
                        cursor.execute(
                            "SELECT 1 FROM variaciones_producto WHERE id_producto=%s AND id_talla=%s AND id_color=%s",
                            (prod_id, t, c)
                        )
                        if cursor.fetchone():
                            continue
                        cursor.execute(
                            "INSERT INTO variaciones_producto (id_producto, id_talla, id_color, stock) VALUES (%s, %s, %s, %s)",
                            (prod_id, t, c, 10)
                        )
                # Imagen extra demo
                cursor.execute("SELECT 1 FROM imagenes_producto WHERE id_producto=%s", (prod_id,))
                if not cursor.fetchone():
                    cursor.execute(
                        "INSERT INTO imagenes_producto (id_color, url, id_producto) VALUES (%s, %s, %s)",
                        (None, f"https://picsum.photos/seed/prod{prod_id}/600/400", prod_id)
                    )
