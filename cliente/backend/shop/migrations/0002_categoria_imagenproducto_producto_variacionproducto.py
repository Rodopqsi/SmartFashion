
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('shop', '0001_product_reviews'),
    ]

    operations = [
        migrations.CreateModel(
            name='Categoria',
            fields=[
                ('id', models.BigAutoField(primary_key=True, serialize=False)),
                ('nombre', models.CharField(max_length=255)),
            ],
            options={
                'db_table': 'Categorias',
                'managed': False,
            },
        ),
        migrations.CreateModel(
            name='ImagenProducto',
            fields=[
                ('id', models.BigAutoField(primary_key=True, serialize=False)),
                ('id_color', models.BigIntegerField(null=True)),
                ('url', models.TextField()),
                ('id_producto', models.BigIntegerField()),
            ],
            options={
                'db_table': 'imagenes_producto',
                'managed': False,
            },
        ),
        migrations.CreateModel(
            name='Producto',
            fields=[
                ('id', models.BigAutoField(primary_key=True, serialize=False)),
                ('nombre', models.CharField(max_length=255)),
                ('descripcion', models.TextField()),
                ('precio', models.DecimalField(decimal_places=2, max_digits=8)),
                ('id_categoria', models.BigIntegerField()),
                ('image_preview', models.CharField(max_length=255)),
            ],
            options={
                'db_table': 'Producto',
                'managed': False,
            },
        ),
        migrations.CreateModel(
            name='VariacionProducto',
            fields=[
                ('id', models.BigAutoField(primary_key=True, serialize=False)),
                ('id_producto', models.BigIntegerField()),
                ('id_talla', models.BigIntegerField()),
                ('id_color', models.BigIntegerField()),
                ('stock', models.IntegerField()),
            ],
            options={
                'db_table': 'variaciones_producto',
                'managed': False,
            },
        ),
    ]
