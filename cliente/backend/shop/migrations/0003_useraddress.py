
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('shop', '0002_categoria_imagenproducto_producto_variacionproducto'),
    ]

    operations = [
        migrations.CreateModel(
            name='UserAddress',
            fields=[
                ('id', models.BigAutoField(primary_key=True, serialize=False)),
                ('user_email', models.CharField(max_length=255)),
                ('label', models.CharField(max_length=120, null=True)),
                ('nombre', models.CharField(max_length=255)),
                ('telefono', models.CharField(max_length=50, null=True)),
                ('alt_telefono', models.CharField(max_length=50, null=True)),
                ('direccion', models.CharField(max_length=512)),
                ('direccion_linea2', models.CharField(max_length=512, null=True)),
                ('distrito', models.CharField(max_length=255, null=True)),
                ('ciudad', models.CharField(max_length=255, null=True)),
                ('region', models.CharField(max_length=255)),
                ('estado', models.CharField(max_length=255, null=True)),
                ('pais', models.CharField(max_length=100, null=True)),
                ('codigo_postal', models.CharField(max_length=32, null=True)),
                ('referencia', models.CharField(max_length=512, null=True)),
                ('is_default', models.BooleanField(default=False)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
            ],
            options={
                'db_table': 'user_address',
                'managed': False,
            },
        ),
    ]
