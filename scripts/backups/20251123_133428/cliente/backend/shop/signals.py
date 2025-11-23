import os
import threading
from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
from .models import Producto, VariacionProducto, ImagenProducto, ProductReview, ColeccionProducto
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
        # Silent fail; snapshot will regenerate next change or via command
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
