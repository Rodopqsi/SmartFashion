from django.db import models


class Categoria(models.Model):
    id = models.BigAutoField(primary_key=True)
    nombre = models.CharField(max_length=255)

    class Meta:
        managed = False
        db_table = 'categorias'


class Producto(models.Model):
    id = models.BigAutoField(primary_key=True)
    nombre = models.CharField(max_length=255)
    descripcion = models.TextField()
    precio = models.DecimalField(max_digits=8, decimal_places=2)
    id_categoria = models.BigIntegerField()
    image_preview = models.CharField(max_length=255)

    class Meta:
        managed = False
        db_table = 'producto'


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


class ProductReview(models.Model):
    id = models.BigAutoField(primary_key=True)
    product_id = models.BigIntegerField()
    user_email = models.CharField(max_length=255)
    rating = models.PositiveSmallIntegerField()
    text = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'product_reviews'


class UserAddress(models.Model):
    id = models.BigAutoField(primary_key=True)
    user_email = models.CharField(max_length=255)
    label = models.CharField(max_length=120, null=True)
    nombre = models.CharField(max_length=255)
    telefono = models.CharField(max_length=50, null=True)
    alt_telefono = models.CharField(max_length=50, null=True)
    direccion = models.CharField(max_length=512)
    direccion_linea2 = models.CharField(max_length=512, null=True)
    distrito = models.CharField(max_length=255, null=True)
    ciudad = models.CharField(max_length=255, null=True)
    region = models.CharField(max_length=255)
    estado = models.CharField(max_length=255, null=True)
    pais = models.CharField(max_length=100, null=True)
    codigo_postal = models.CharField(max_length=32, null=True)
    referencia = models.CharField(max_length=512, null=True)
    is_default = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        managed = False
        db_table = 'user_address'


class Complaint(models.Model):
    id = models.BigAutoField(primary_key=True)
    user_email = models.CharField(max_length=255)
    order_number = models.CharField(max_length=64)
    tipo = models.CharField(max_length=16)
    detalle = models.TextField()
    estado = models.CharField(max_length=32, default='registrado')
    respuesta = models.TextField(null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'complaints'


class ReturnRequest(models.Model):
    id = models.BigAutoField(primary_key=True)
    user_email = models.CharField(max_length=255)
    order_number = models.CharField(max_length=64)
    motivo = models.CharField(max_length=64)
    descripcion = models.TextField(null=True)
    metodo = models.CharField(max_length=16)
    estado = models.CharField(max_length=32, default='solicitado')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'return_requests'


class Coleccion(models.Model):
    id = models.BigAutoField(primary_key=True)
    nombre = models.CharField(max_length=255)
    slug = models.CharField(max_length=255)
    descripcion = models.TextField(null=True)
    activo = models.BooleanField(default=True)
    orden = models.IntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        managed = False
        db_table = 'coleccion'


class ColeccionProducto(models.Model):
    id = models.BigAutoField(primary_key=True)
    id_coleccion = models.BigIntegerField()
    id_producto = models.BigIntegerField()

    class Meta:
        managed = False
        db_table = 'coleccionproducto'
