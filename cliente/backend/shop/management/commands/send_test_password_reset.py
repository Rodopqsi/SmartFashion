from django.core.management.base import BaseCommand
from django.test import Client

class Command(BaseCommand):
    help = 'Prueba el flujo de password reset en DEBUG, imprime respuestas'

    def handle(self, *args, **options):
        c = Client()
        email = 'rodolfo.tavera@tecsup.edu.pe'
        print('Solicitando reset para', email)
        resp = c.post('/api/auth/password_reset/', {'email': email})
        print('reset request', resp.status_code, resp.json())
        code = resp.json().get('debug_code')
        if code:
            print('debug_code:', code)
            resp2 = c.post('/api/auth/password_reset/verify/', {'email': email, 'code': code, 'new_password': 'NuevaPass123!'})
            print('verify', resp2.status_code, resp2.json())
        else:
            print('No debug code devuelto; revisa SMTP o DEBUG')
