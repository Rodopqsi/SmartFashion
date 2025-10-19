from django.db import models


class Categoria(models.Model):
    id = models.BigAutoField(primary_key=True)
    nombre = models.CharField(max_length=255)

    class Meta:
        managed = False
        db_table = 'Categorias'


class Producto(models.Model):
    id = models.BigAutoField(primary_key=True)
    nombre = models.CharField(max_length=255)
    descripcion = models.TextField()
    precio = models.DecimalField(max_digits=8, decimal_places=2)
    id_categoria = models.BigIntegerField()
    image_preview = models.CharField(max_length=255)

    class Meta:
        managed = False
        db_table = 'Producto'


class VariacionProducto(models.Model):
    id = models.BigAutoField(primary_key=True)
    id_producto = models.BigIntegerField()
    id_talla = models.BigIntegerField()
    id_color = models.BigIntegerField()
    stock = models.IntegerField()

    class Meta:
        managed = False
        db_table = 'variaciones_producto'


class ImagenProducto(models.Model):
    id = models.BigAutoField(primary_key=True)
    id_color = models.BigIntegerField(null=True)
    url = models.TextField()
    id_producto = models.BigIntegerField()

    class Meta:
        managed = False
        db_table = 'imagenes_producto'
