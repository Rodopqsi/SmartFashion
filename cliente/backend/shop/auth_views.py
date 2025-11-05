from django.contrib.auth.models import User
from django.contrib.auth.password_validation import validate_password
from django.db import IntegrityError
from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from rest_framework_simplejwt.tokens import RefreshToken
from google.oauth2 import id_token
from google.auth.transport import requests as google_requests
from django.core.cache import cache
from django.core.signing import TimestampSigner, BadSignature, SignatureExpired
from django.utils.crypto import get_random_string
from django.utils import timezone
from django.core.mail import send_mail
import os
import base64, hmac, hashlib, json, time
from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import redirect

class CustomTokenObtainPairSerializer(TokenObtainPairSerializer):
    @classmethod
    def get_token(cls, user):
        token = super().get_token(user)
        token['username'] = user.username
        token['email'] = user.email
        return token

    def validate(self, attrs):
        """
        Permite iniciar sesión usando email en lugar de username.
        Si 'username' parece un email, intentamos resolver el username real.
        """
        uname = attrs.get('username') or ''
        if '@' in uname:
            # Permitir login con email aunque existan duplicados: usar el primero.
            u = User.objects.filter(email=uname).order_by('id').first()
            if u:
                attrs['username'] = u.username
        return super().validate(attrs)

class CustomTokenObtainPairView(TokenObtainPairView):
    serializer_class = CustomTokenObtainPairSerializer

class CustomTokenRefreshView(TokenRefreshView):
    pass

@api_view(['POST'])
@permission_classes([AllowAny])
def register(request):
    data = request.data
    username = data.get('username') or (data.get('email') or '').split('@')[0]
    email = data.get('email')
    password = data.get('password')
    if not email or not password:
        return Response({'detail': 'Email y contraseña requeridos'}, status=400)
    # Validación básica de username si se proporcionó
    import re
    if username and not re.fullmatch(r'[a-zA-Z0-9_\.\-]{3,20}', username):
        return Response({'detail': 'Username inválido. Use 3-20 caracteres: letras, números, _ . -'}, status=400)
    try:
        validate_password(password)
    except Exception as e:
        return Response({'detail': ' '.join([str(x) for x in e])}, status=400)
    try:
        user = User.objects.create_user(username=username, email=email, password=password)
        # Requerir verificación de email
        user.is_active = False
        user.save()
    except IntegrityError:
        return Response({'detail': 'Usuario ya existe'}, status=400)
    # Generar código de verificación (6 dígitos) y almacenarlo en cache por 10 minutos
    code = get_random_string(6, allowed_chars='0123456789')
    cache.set(f'verify:{email}', code, timeout=600)
    # En entornos reales, enviar email. En DEBUG, podemos incluir el código en la respuesta.
    debug_mode = os.getenv('DEBUG', 'True').lower() in ('1', 'true', 'yes')
    try:
        send_mail(
            subject='Código de verificación - SmartFashion',
            message=f'Tu código de verificación es: {code}. Expira en 10 minutos.',
            from_email=os.getenv('DEFAULT_FROM_EMAIL') or os.getenv('EMAIL_HOST_USER') or 'no-reply@smarthfashion.local',
            recipient_list=[email],
            fail_silently=not debug_mode,
        )
    except Exception as e:
        if debug_mode:
            # En debug, no fallamos duro por correo
            pass
        else:
            return Response({'detail': 'No se pudo enviar el correo de verificación'}, status=500)
    return Response({ 'detail': 'verification_sent', 'email': email, **({'debug_code': code} if debug_mode else {}) }, status=201)

@api_view(['POST'])
@permission_classes([AllowAny])
def verify_email(request):
    email = request.data.get('email')
    code = request.data.get('code')
    if not email or not code:
        return Response({'detail': 'Email y código requeridos'}, status=400)
    expected = cache.get(f'verify:{email}')
    if not expected:
        return Response({'detail': 'Código expirado o no encontrado'}, status=400)
    if str(expected) != str(code):
        return Response({'detail': 'Código inválido'}, status=400)
    user = User.objects.filter(email=email).order_by('id').first()
    if not user:
        return Response({'detail': 'Usuario no encontrado'}, status=404)
    user.is_active = True
    user.save()
    cache.delete(f'verify:{email}')
    refresh = RefreshToken.for_user(user)
    return Response({
        'user': {'id': user.id, 'username': user.username, 'email': user.email},
        'access': str(refresh.access_token),
        'refresh': str(refresh)
    })

@api_view(['POST'])
@permission_classes([AllowAny])
def google_oauth(request):
    """Intercambia un credential (ID token) de Google One Tap / OAuth por un JWT propio."""
    token = request.data.get('credential')
    if not token:
        return Response({'detail': 'credential requerido'}, status=400)
    client_id = (os.getenv('GOOGLE_CLIENT_ID') or '').strip()
    # Fallback para desarrollo: si token == 'FAKE_GOOGLE_ID_TOKEN' y DEBUG, crear usuario dummy
    debug_mode = os.getenv('DEBUG', 'True').lower() in ('1', 'true', 'yes')
    if token == 'FAKE_GOOGLE_ID_TOKEN' and debug_mode:
        idinfo = { 'email': 'demo_google@example.com' }
    else:
        if not client_id:
            msg = 'GOOGLE_CLIENT_ID no configurado en backend'
            # En debug, devolver 400 en lugar de 500 para claridad
            return Response({'detail': msg}, status=400 if debug_mode else 500)
        try:
            idinfo = id_token.verify_oauth2_token(token, google_requests.Request(), audience=client_id)
            # Validaciones adicionales recomendadas por Google
            iss = idinfo.get('iss')
            if iss not in ('accounts.google.com', 'https://accounts.google.com'):
                return Response({'detail': 'Issuer inválido'}, status=400)
            aud = idinfo.get('aud')
            if aud != client_id:
                return Response({'detail': 'Audience mismatch'}, status=400)
        except Exception as e:
            # En DEBUG exponemos el motivo para facilitar diagnóstico
            if debug_mode:
                return Response({'detail': f'Token Google inválido: {str(e)}'}, status=400)
            return Response({'detail': 'Token Google inválido'}, status=400)
    email = idinfo.get('email')
    if not email:
        return Response({'detail': 'Email Google no disponible'}, status=400)
    user = User.objects.filter(email=email).order_by('id').first()
    if not user:
        # Primera vez: requerir selección de username en cliente
        suggested = email.split('@')[0]
        signer = TimestampSigner()
        pending = signer.sign(email)
        return Response({
            'need_username': True,
            'email': email,
            'suggested_username': suggested,
            'pending': pending
        }, status=202)
    created = False
    refresh = RefreshToken.for_user(user)
    return Response({
        'user': {'id': user.id, 'username': user.username, 'email': user.email},
        'access': str(refresh.access_token),
        'refresh': str(refresh),
        'is_new': created
    })

@api_view(['POST'])
@permission_classes([AllowAny])
def complete_username(request):
    """Finaliza alta de usuario Google con username elegido por el cliente."""
    username = (request.data.get('username') or '').strip()
    import re
    if not re.fullmatch(r'[a-zA-Z0-9_\.\-]{3,20}', username):
        return Response({'detail': 'Username inválido. Use 3-20 caracteres: letras, números, _ . -'}, status=400)
    pending = request.data.get('pending')
    if not username or not pending:
        return Response({'detail': 'username y pending requeridos'}, status=400)
    signer = TimestampSigner()
    debug_mode = os.getenv('DEBUG', 'True').lower() in ('1', 'true', 'yes')
    try:
        email = signer.unsign(pending, max_age=600)  # 10 minutos
    except (BadSignature, SignatureExpired) as e:
        return Response({'detail': 'Token pendiente inválido o expirado'}, status=400)
    if User.objects.filter(username=username).exists():
        return Response({'detail': 'Nombre de usuario no disponible'}, status=400)
    user = User.objects.create_user(username=username, email=email)
    user.set_unusable_password()
    user.is_active = True
    user.save()
    refresh = RefreshToken.for_user(user)
    return Response({
        'user': {'id': user.id, 'username': user.username, 'email': user.email},
        'access': str(refresh.access_token),
        'refresh': str(refresh)
    })


@api_view(['POST'])
@permission_classes([AllowAny])
def password_reset_request(request):
    """Solicita un código de restablecimiento de contraseña enviado al email del usuario."""
    email = (request.data.get('email') or '').strip()
    if not email:
        return Response({'detail': 'Email requerido'}, status=400)
    # Rate limiting simple: máximo 5 intentos por 15 minutos por email y por IP
    client_ip = request.META.get('REMOTE_ADDR', 'unknown')
    k_email = f'rl:pwdreset:email:{email}'
    k_ip = f'rl:pwdreset:ip:{client_ip}'
    email_count = cache.get(k_email, 0)
    ip_count = cache.get(k_ip, 0)
    if email_count >= 5 or ip_count >= 20:
        return Response({'detail': 'Demasiados intentos, intente más tarde'}, status=429)
    cache.set(k_email, email_count + 1, timeout=900)
    cache.set(k_ip, ip_count + 1, timeout=900)
    user = User.objects.filter(email=email).order_by('id').first()
    if not user:
        # No revelar si el email existe por seguridad; responder OK
        return Response({'detail': 'reset_sent'}, status=200)
    # Generar código de 6 dígitos y almacenarlo en cache por 15 minutos
    code = get_random_string(6, allowed_chars='0123456789')
    cache.set(f'password_reset:{email}', code, timeout=900)
    debug_mode = os.getenv('DEBUG', 'True').lower() in ('1', 'true', 'yes')
    try:
        send_mail(
            subject='Código para restablecer contraseña - SmartFashion',
            message=f'Su código para restablecer la contraseña es: {code}. Expira en 15 minutos.',
            from_email=os.getenv('DEFAULT_FROM_EMAIL') or os.getenv('EMAIL_HOST_USER') or 'no-reply@smarthfashion.local',
            recipient_list=[email],
            fail_silently=not debug_mode,
        )
    except Exception as e:
        if not debug_mode:
            return Response({'detail': 'No se pudo enviar el correo de restablecimiento'}, status=500)
    return Response({'detail': 'reset_sent', **({'debug_code': code} if debug_mode else {})}, status=200)


@api_view(['POST'])
@permission_classes([AllowAny])
def password_reset_verify(request):
    """Verifica el código y establece la nueva contraseña."""
    email = (request.data.get('email') or '').strip()
    code = (request.data.get('code') or '').strip()
    new_password = request.data.get('new_password')
    if not email or not code or not new_password:
        return Response({'detail': 'Email, código y nueva contraseña requerados'}, status=400)
    # Rate limiting verificación: 10 intentos por 15 minutos por email
    k_email_verify = f'rl:pwdverify:email:{email}'
    vcount = cache.get(k_email_verify, 0)
    if vcount >= 10:
        return Response({'detail': 'Demasiados intentos, intente más tarde'}, status=429)
    cache.set(k_email_verify, vcount + 1, timeout=900)
    expected = cache.get(f'password_reset:{email}')
    if not expected:
        return Response({'detail': 'Código expirado o no encontrado'}, status=400)
    if str(expected) != str(code):
        return Response({'detail': 'Código inválido'}, status=400)
    user = User.objects.filter(email=email).order_by('id').first()
    if not user:
        return Response({'detail': 'Usuario no encontrado'}, status=404)
    # Validar la nueva contraseña
    try:
        validate_password(new_password, user=user)
    except Exception as e:
        return Response({'detail': ' '.join([str(x) for x in e])}, status=400)
    user.set_password(new_password)
    user.save()
    cache.delete(f'password_reset:{email}')
    return Response({'detail': 'password_reset_success'}, status=200)


# --- SSO Admin redirect (Django -> Spring Boot) ---
@login_required
def admin_sso_redirect(request):
    """If the logged-in user is staff/superuser, mint a short-lived HMAC token
    and redirect to Spring Boot /sso/login?token=...
    Otherwise, redirect to home.
    """
    user = request.user
    # Only allow admin users
    if not (user.is_staff or user.is_superuser):
        return redirect('/')

    shared = getattr(settings, 'SSO_SHARED_SECRET', '')
    spring_endpoint = getattr(settings, 'SPRING_ADMIN_SSO_ENDPOINT', 'http://localhost:8081/sso/login')
    if not shared:
        # If not configured, just return to home for safety
        return redirect('/')

    now = int(time.time())
    exp = now + 60  # must match Spring's sso.max-age
    payload = {
        'sub': str(user.id),
        'email': user.email or user.username,
        'role': 'ADMIN',
        'iat': now,
        'exp': exp,
    }
    payload_bytes = json.dumps(payload, separators=(',', ':')).encode('utf-8')
    # Sign the RAW JSON bytes (Spring verifies HMAC over decoded payload bytes)
    sig = hmac.new(shared.encode('utf-8'), payload_bytes, hashlib.sha256).hexdigest()
    payload_b64 = base64.urlsafe_b64encode(payload_bytes).rstrip(b'=')
    token = payload_b64.decode('utf-8') + '.' + sig
    return redirect(f"{spring_endpoint}?token={token}")
