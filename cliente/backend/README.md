# SmartFashion Backend (Django + DRF)

## Requisitos
- Python 3.11+
- MySQL server

## Configuración
1. Crear entorno:
```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```
2. Configurar variables:
```powershell
copy .env.example .env
# Edita .env con tus credenciales
```
3. Migraciones y arranque:
```powershell
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

## Envío de correo
Configura SMTP en `.env` (App Password si usas Gmail). Usa:
```powershell
python manage.py shell -c "from django.core.mail import send_mail; print(send_mail('Test','Body','From <you@mail>', ['to@mail']))"
```

## Endpoints clave
- POST /api/auth/register/
- POST /api/auth/register/verify/
- POST /api/auth/token/
- POST /api/auth/token/refresh/
- POST /api/auth/google/
- POST /api/auth/google/complete/
- POST /api/auth/password_reset/
- POST /api/auth/password_reset/verify/
