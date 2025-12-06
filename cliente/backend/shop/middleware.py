from django.utils.deprecation import MiddlewareMixin
from django.contrib.auth import get_user_model
import time
from .models import Usuario


class UsuarioSyncMiddleware(MiddlewareMixin):
    """
    Middleware que asegura que exista/actualice la fila en `usuario` para el usuario autenticado.
    Cubre flujos de Google Auth y registros normales sin depender de signals.
    """

    def process_request(self, request):
        user = getattr(request, "user", None)
        if not user or not user.is_authenticated:
            return None

        email = getattr(user, "email", None)
        if not email:
            return None

        try:
            usuario = Usuario.objects.filter(email=email).first()
            if usuario is None:
                usuario = Usuario(
                    nombre=getattr(user, "first_name", "") or "",
                    apellido=getattr(user, "last_name", "") or "",
                    email=email,
                    telefono="",
                    fecha_registro=int(time.time()),
                    bloqueado=False,
                )
            else:
                # Actualiza nombre/apellido si vienen vacíos
                if getattr(user, "first_name", ""):
                    usuario.nombre = user.first_name
                if getattr(user, "last_name", ""):
                    usuario.apellido = user.last_name
                if not usuario.fecha_registro:
                    usuario.fecha_registro = int(time.time())

            usuario.save(using=Usuario._state.db or None)
        except Exception:
            # No bloquear la request por errores de sync
            return None

        return None
