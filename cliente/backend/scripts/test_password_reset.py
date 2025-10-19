from django.test import Client

c = Client()
resp = c.post('/api/auth/password_reset/', {'email': 'rodolfo.tavera@tecsup.edu.pe'})
print('reset request', resp.status_code, resp.json())
code = resp.json().get('debug_code')
if code:
    print('debug_code:', code)
    resp2 = c.post('/api/auth/password_reset/verify/', {'email': 'rodolfo.tavera@tecsup.edu.pe', 'code': code, 'new_password': 'NuevaPass123!'})
    print('verify', resp2.status_code, resp2.json())
else:
    print('No debug code returned; check SMTP or DEBUG setting')
