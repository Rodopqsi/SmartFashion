from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ('shop', '0003_useraddress'),
    ]

    operations = [
        migrations.CreateModel(
            name='Complaint',
            fields=[
                ('id', models.BigAutoField(primary_key=True, serialize=False)),
                ('user_email', models.CharField(max_length=255)),
                ('order_number', models.CharField(max_length=64)),
                ('tipo', models.CharField(max_length=16)),
                ('detalle', models.TextField()),
                ('estado', models.CharField(default='registrado', max_length=32)),
                ('respuesta', models.TextField(null=True)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
            ],
            options={'db_table': 'complaints'},
        ),
        migrations.CreateModel(
            name='ReturnRequest',
            fields=[
                ('id', models.BigAutoField(primary_key=True, serialize=False)),
                ('user_email', models.CharField(max_length=255)),
                ('order_number', models.CharField(max_length=64)),
                ('motivo', models.CharField(max_length=64)),
                ('descripcion', models.TextField(null=True)),
                ('metodo', models.CharField(max_length=16)),
                ('estado', models.CharField(default='solicitado', max_length=32)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
            ],
            options={'db_table': 'return_requests'},
        ),
    ]
