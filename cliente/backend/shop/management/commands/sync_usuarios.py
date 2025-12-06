from django.core.management.base import BaseCommand
from django.contrib.auth import get_user_model
import time
from shop.models import Usuario


class Command(BaseCommand):
    help = "Sincroniza todos los usuarios de auth_user hacia la tabla 'usuario' (crea/actualiza por email)."

    def handle(self, *args, **options):
        User = get_user_model()
        count_created = 0
        count_updated = 0
        for user in User.objects.all():
            email = getattr(user, 'email', None)
            if not email:
                continue
            usuario = Usuario.objects.filter(email=email).first()
            if usuario is None:
                usuario = Usuario(
                    nombre=getattr(user, 'first_name', '') or '',
                    apellido=getattr(user, 'last_name', '') or '',
                    email=email,
                    telefono='',
                    fecha_registro=int(time.time()),
                    bloqueado=False,
                )
                usuario.save()
                count_created += 1
            else:
                changed = False
                if getattr(user, 'first_name', '') and usuario.nombre != user.first_name:
                    usuario.nombre = user.first_name
                    changed = True
                if getattr(user, 'last_name', '') and usuario.apellido != user.last_name:
                    usuario.apellido = user.last_name
                    changed = True
                if not usuario.fecha_registro:
                    usuario.fecha_registro = int(time.time())
                    changed = True
                if changed:
                    usuario.save()
                    count_updated += 1
        self.stdout.write(self.style.SUCCESS(f"Sync completada. Creados: {count_created}, Actualizados: {count_updated}"))
