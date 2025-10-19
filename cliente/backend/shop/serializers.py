from rest_framework import serializers


class CategoriaSerializer(serializers.Serializer):
    id = serializers.IntegerField()
    nombre = serializers.CharField()


class ProductoCardSerializer(serializers.Serializer):
    id = serializers.IntegerField()
    nombre = serializers.CharField()
    descripcion = serializers.CharField()
    precio = serializers.DecimalField(max_digits=8, decimal_places=2)
    precio_descuento = serializers.DecimalField(max_digits=8, decimal_places=2, required=False, allow_null=True)
    categoria = CategoriaSerializer()
    image_preview = serializers.CharField()
    stock_total = serializers.IntegerField()
