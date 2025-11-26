import os
import threading
from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
from .models import Producto, VariacionProducto, ImagenProducto, ProductReview, ColeccionProducto, Coleccion
from django.conf import settings
import json
from pathlib import Path
from .catalog_snapshot import export_snapshot

_debounce_lock = threading.Lock()
_timer = None

def _schedule_regeneration():
    if os.getenv('SNAPSHOT_AUTO', '1') != '1':
        return
    global _timer
    with _debounce_lock:
        if _timer is not None:
            _timer.cancel()
        _timer = threading.Timer(2.0, _regenerate)
        _timer.daemon = True
        _timer.start()

def _regenerate():
    try:
        export_snapshot()
    except Exception:
        pass


@receiver(post_save, sender=Producto)
@receiver(post_delete, sender=Producto)
@receiver(post_save, sender=VariacionProducto)
@receiver(post_delete, sender=VariacionProducto)
@receiver(post_save, sender=ImagenProducto)
@receiver(post_delete, sender=ImagenProducto)
@receiver(post_save, sender=ProductReview)
@receiver(post_delete, sender=ProductReview)
@receiver(post_save, sender=ColeccionProducto)
@receiver(post_delete, sender=ColeccionProducto)
def on_catalog_change(**kwargs):
    _schedule_regeneration()


@receiver(post_save, sender=Coleccion)
def on_coleccion_saved(sender, instance, created, **kwargs):
    # When a new collection is created, notify subscribers asynchronously.
    try:
        if not created:
            return
        if not getattr(instance, 'activo', True):
            return
        def _notify():
            try:
                subs_file = Path(__file__).resolve().parent / 'subscribers.json'
                if not subs_file.exists():
                    return
                subs = json.loads(subs_file.read_text(encoding='utf-8') or '[]')
                if not subs:
                    return
                subject = f"Nueva colección en SmartFashion: {instance.nombre}"
                plain = f"Hemos publicado una nueva colección: {instance.nombre}\n\n{(instance.descripcion or '')}\n\nVisítanos para ver los productos: /collections/{instance.slug}/"
                html = f"<p>Hola,</p><p>Hemos publicado una nueva colección: <strong>{instance.nombre}</strong></p>"
                if instance.descripcion:
                    html += f"<p>{instance.descripcion}</p>"
                html += f"<p><a href=\"{getattr(settings, 'FRONTEND_URL', '') or '/collections/'+instance.slug}\">Ver colección</a></p>"
                from_email = getattr(settings, 'DEFAULT_FROM_EMAIL', 'no-reply@smarthfashion.local')
                for s in subs:
                    try:
                        to = s.get('email')
                        name = s.get('name') or ''
                        personalized_html = html.replace('<p>Hola,</p>', f'<p>Hola {name},</p>' if name else '<p>Hola,</p>')
                        # use Django email sending
                        from django.core.mail import EmailMultiAlternatives
                        msg = EmailMultiAlternatives(subject, plain, from_email, [to])
                        msg.attach_alternative(personalized_html, "text/html")
                        msg.send(fail_silently=True)
                    except Exception:
                        continue
            except Exception:
                pass
        t = threading.Thread(target=_notify)
        t.daemon = True
        t.start()
    except Exception:
        pass
