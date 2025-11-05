from django.core.management.base import BaseCommand
from django.test import Client
from django.utils import timezone
import re


class Command(BaseCommand):
    help = 'Prueba de extremo a extremo: registro -> verificación -> login (email/username) y Google dev flow.'

    def handle(self, *args, **options):
        c = Client()
        ts = timezone.now().strftime('%Y%m%d%H%M%S')
        email = f'autotest_{ts}@example.com'
        username = f'autotest_{ts}'[:20]
        password = 'Autotest123!'

        print('\n--- Registro ---')
        r = c.post('/api/auth/register/', {
            'email': email,
            'username': username,
            'password': password,
        })
        print('register', r.status_code, r.json() if hasattr(r, 'json') else r.content)
        if r.status_code not in (201, 200):
            print('Registro falló')
            return
        data = r.json()
        code = data.get('debug_code')
        if not code:
            print('DEBUG no devolvió debug_code; configure DEBUG=True o SMTP real para continuar la prueba')
            return

        print('\n--- Verificación ---')
        v = c.post('/api/auth/register/verify/', {
            'email': email,
            'code': code,
        })
        print('verify', v.status_code, v.json() if hasattr(v, 'json') else v.content)
        if v.status_code != 200:
            print('Verificación falló')
            return

        print('\n--- Login con username ---')
        l1 = c.post('/api/auth/token/', {
            'username': username,
            'password': password,
        })
        print('login-username', l1.status_code)
        if l1.status_code != 200:
            print('Login con username falló')
            return

        print('\n--- Login con email ---')
        l2 = c.post('/api/auth/token/', {
            'username': email,
            'password': password,
        })
        print('login-email', l2.status_code)
        if l2.status_code != 200:
            print('Login con email falló')
            return

        print('\n--- Registro duplicado (espera 400) ---')
        r2 = c.post('/api/auth/register/', {
            'email': email,
            'username': username,
            'password': password,
        })
        try:
            body = r2.json()
        except Exception:
            body = r2.content
        print('register-duplicate', r2.status_code, body)
        if r2.status_code != 400:
            print('Se esperaba 400 al registrar duplicado')
            return

        print('\n--- Google dev flow (FAKE token) ---')
        g1 = c.post('/api/auth/google/', {'credential': 'FAKE_GOOGLE_ID_TOKEN'})
        try:
            g1b = g1.json()
        except Exception:
            g1b = {}
        print('google-1', g1.status_code, g1b)
        if g1.status_code not in (200, 202):
            print('Google dev flow primer paso falló')
            return
        if g1.status_code == 202 and g1b.get('need_username'):
            pend = g1b.get('pending')
            sug = g1b.get('suggested_username') or f'google_{ts}'
            # Asegurar username válido
            sug = re.sub(r'[^a-zA-Z0-9_\.-]', '_', sug)[:20]
            g2 = c.post('/api/auth/google/complete/', {
                'username': sug,
                'pending': pend,
            })
            print('google-complete', g2.status_code)
            if g2.status_code != 200:
                print('Google complete falló')
                return

        print('\nOK: Flujos de registro, verificación, login (email/username) y Google dev verificados.')
