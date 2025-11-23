from django.core.management.base import BaseCommand
from django.db import connection


class Command(BaseCommand):
    help = "Copy users from auth_user to the admin 'usuario' table if not already present."

    def handle(self, *args, **options):
        with connection.cursor() as cursor:
            try:
                cursor.execute("""
                INSERT INTO usuario (nombre, apellido, email, telefono, fecha_registro)
                SELECT COALESCE(first_name, ''), COALESCE(last_name, ''), COALESCE(email, ''), '', UNIX_TIMESTAMP(date_joined)
                FROM auth_user
                WHERE COALESCE(email, '') NOT IN (SELECT COALESCE(email, '') FROM usuario)
                """)
            except Exception as e:
                self.stderr.write(f"Error inserting users: {e}")
                return
            try:
                cursor.execute("SELECT COUNT(*) FROM usuario")
                row = cursor.fetchone()
                total = row[0] if row else 0
            except Exception:
                total = 'unknown'

        self.stdout.write(self.style.SUCCESS(f"Users synced to 'usuario' table. Total rows now: {total}"))
