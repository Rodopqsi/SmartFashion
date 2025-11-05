from django.db import migrations, models


class Migration(migrations.Migration):
    initial = False

    dependencies = [
    ]

    operations = [
        migrations.SeparateDatabaseAndState(
            database_operations=[
                migrations.RunSQL(
                    sql=r"""
                    CREATE TABLE IF NOT EXISTS product_reviews (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        product_id BIGINT NOT NULL,
                        user_email VARCHAR(255) NOT NULL,
                        rating TINYINT NOT NULL,
                        text TEXT NOT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """,
                    reverse_sql=r"""
                    DROP TABLE IF EXISTS product_reviews;
                    """
                ),
            ],
            state_operations=[
                migrations.CreateModel(
                    name='ProductReview',
                    fields=[
                        ('id', models.BigAutoField(primary_key=True, serialize=False)),
                        ('product_id', models.BigIntegerField()),
                        ('user_email', models.CharField(max_length=255)),
                        ('rating', models.PositiveSmallIntegerField()),
                        ('text', models.TextField()),
                        ('created_at', models.DateTimeField(auto_now_add=True)),
                    ],
                    options={
                        'db_table': 'product_reviews',
                    },
                ),
            ],
        ),
    ]
