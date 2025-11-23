from django.contrib.auth.models import User
from django.contrib.auth.password_validation import validate_password
from django.db import IntegrityError
from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny, IsAuthenticated
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
from django.db import connection
from django.utils import timezone
import pyotp
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
            u = User.objects.filter(email=uname).order_by('id').first()
            if u:
                attrs['username'] = u.username
        data = super().validate(attrs)
        return data

class CustomTokenObtainPairView(TokenObtainPairView):
    serializer_class = CustomTokenObtainPairSerializer
    def post(self, request, *args, **kwargs):
        response = super().post(request, *args, **kwargs)
        if response.status_code == 200:
            try:
                payload = response.data or {}
                rf = payload.get('refresh')
                if rf:
                    rt = RefreshToken(rf)
                    jti = str(rt.get('jti'))
                    user_id = rt.get('user_id') or request.user.id if hasattr(request, 'user') else None
                    ua = request.META.get('HTTP_USER_AGENT', '')[:512]
                    ip = request.META.get('REMOTE_ADDR', '')[:64]
                    with connection.cursor() as cursor:
                        cursor.execute("""
                            CREATE TABLE IF NOT EXISTS user_sessions (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                jti VARCHAR(64) NOT NULL,
                                user_agent TEXT,
                                ip VARCHAR(64),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                            ) ENGINE=InnoDB
                        """)
                        if user_id:
                            cursor.execute("INSERT INTO user_sessions (user_id, jti, user_agent, ip) VALUES (%s, %s, %s, %s)", [user_id, jti, ua, ip])
            except Exception:
                pass
        return response

class CustomTokenRefreshView(TokenRefreshView):
    def post(self, request, *args, **kwargs):

        rf = request.data.get('refresh')
        try:
            if rf:
                rt = RefreshToken(rf)
                user_id = rt.get('user_id')
                iat = int(rt.get('iat', 0))
                if user_id:
                    with connection.cursor() as cursor:
                        cursor.execute("""
                            CREATE TABLE IF NOT EXISTS user_security (
                                user_id BIGINT PRIMARY KEY,
                                totp_secret VARCHAR(64) NULL,
                                totp_enabled TINYINT(1) DEFAULT 0,
                                revoked_after TIMESTAMP NULL
                            ) ENGINE=InnoDB
                        """)
                        cursor.execute("SELECT UNIX_TIMESTAMP(COALESCE(revoked_after, '1970-01-01 00:00:00')) FROM user_security WHERE user_id=%s", [user_id])
                        row = cursor.fetchone()
                        if row and iat and int(iat) < int(row[0] or 0):
                            from rest_framework.response import Response
                            from rest_framework import status
                            return Response({'detail': 'Sesiones revocadas. Inicia sesión de nuevo.'}, status=status.HTTP_401_UNAUTHORIZED)
        except Exception:
            pass
        return super().post(request, *args, **kwargs)

@api_view(['POST'])
@permission_classes([AllowAny])
def register(request):
    data = request.data
    username = data.get('username') or (data.get('email') or '').split('@')[0]
    email = data.get('email')
    password = data.get('password')
    if not email or not password:
        return Response({'detail': 'Email y contraseña requeridos'}, status=400)
    import re
    if username and not re.fullmatch(r'[a-zA-Z0-9_\.\-]{3,20}', username):
        return Response({'detail': 'Username inválido. Use 3-20 caracteres: letras, números, _ . -'}, status=400)
    try:
        validate_password(password)
    except Exception as e:
        return Response({'detail': ' '.join([str(x) for x in e])}, status=400)
    try:
        user = User.objects.create_user(username=username, email=email, password=password)
        user.is_active = False
        user.save()
    except IntegrityError:
        return Response({'detail': 'Usuario ya existe'}, status=400)
    code = get_random_string(6, allowed_chars='0123456789')
    cache.set(f'verify:{email}', code, timeout=600)
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
    debug_mode = os.getenv('DEBUG', 'True').lower() in ('1', 'true', 'yes')
    if token == 'FAKE_GOOGLE_ID_TOKEN' and debug_mode:
        idinfo = { 'email': 'demo_google@example.com' }
    else:
        if not client_id:
            msg = 'GOOGLE_CLIENT_ID no configurado en backend'
            return Response({'detail': msg}, status=400 if debug_mode else 500)
        try:
            idinfo = id_token.verify_oauth2_token(token, google_requests.Request(), audience=client_id)
            iss = idinfo.get('iss')
            if iss not in ('accounts.google.com', 'https://accounts.google.com'):
                return Response({'detail': 'Issuer inválido'}, status=400)
            aud = idinfo.get('aud')
            if aud != client_id:
                return Response({'detail': 'Audience mismatch'}, status=400)
        except Exception as e:
            if debug_mode:
                return Response({'detail': f'Token Google inválido: {str(e)}'}, status=400)
            return Response({'detail': 'Token Google inválido'}, status=400)
    email = idinfo.get('email')
    if not email:
        return Response({'detail': 'Email Google no disponible'}, status=400)
    user = User.objects.filter(email=email).order_by('id').first()
    if not user:
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
    password = request.data.get('password')
    import re
    if not re.fullmatch(r'[a-zA-Z0-9_\.\-]{3,20}', username):
        return Response({'detail': 'Username inválido. Use 3-20 caracteres: letras, números, _ . -'}, status=400)
    if not password:
        return Response({'detail': 'password requerido'}, status=400)
    pending = request.data.get('pending')
    if not username or not pending:
        return Response({'detail': 'username y pending requeridos'}, status=400)
    signer = TimestampSigner()
    debug_mode = os.getenv('DEBUG', 'True').lower() in ('1', 'true', 'yes')
    try:
        email = signer.unsign(pending, max_age=600)
    except (BadSignature, SignatureExpired) as e:
        return Response({'detail': 'Token pendiente inválido o expirado'}, status=400)
    if User.objects.filter(username=username).exists():
        return Response({'detail': 'Nombre de usuario no disponible'}, status=400)
    try:
        validate_password(password)
    except Exception as e:
        return Response({'detail': ' '.join([str(x) for x in e])}, status=400)
    user = User.objects.create_user(username=username, email=email, password=password)
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
        return Response({'detail': 'reset_sent'}, status=200)
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
    try:
        validate_password(new_password, user=user)
    except Exception as e:
        return Response({'detail': ' '.join([str(x) for x in e])}, status=400)
    user.set_password(new_password)
    user.save()
    cache.delete(f'password_reset:{email}')
    return Response({'detail': 'password_reset_success'}, status=200)


@login_required
def admin_sso_redirect(request):
    """If the logged-in user is staff/superuser, mint a short-lived HMAC token
    and redirect to Spring Boot /sso/login?token=...
    Otherwise, redirect to home.
    """
    user = request.user
    if not (user.is_staff or user.is_superuser):
        return redirect('/')

    shared = getattr(settings, 'SSO_SHARED_SECRET', '')
    spring_endpoint = getattr(settings, 'SPRING_ADMIN_SSO_ENDPOINT', 'http://localhost:8081/sso/login')
    if not shared:
        return redirect('/')

    now = int(time.time())
    exp = now + 60
    payload = {
        'sub': str(user.id),
        'email': user.email or user.username,
        'role': 'ADMIN',
        'iat': now,
        'exp': exp,
    }
    payload_bytes = json.dumps(payload, separators=(',', ':')).encode('utf-8')
    sig = hmac.new(shared.encode('utf-8'), payload_bytes, hashlib.sha256).hexdigest()
    payload_b64 = base64.urlsafe_b64encode(payload_bytes).rstrip(b'=')
    token = payload_b64.decode('utf-8') + '.' + sig
    return redirect(f"{spring_endpoint}?token={token}")



def _send_email(subject, message, to_email, debug_fallback=True):
    debug_mode = os.getenv('DEBUG', 'True').lower() in ('1', 'true', 'yes')
    try:
        send_mail(
            subject=subject,
            message=message,
            from_email=os.getenv('DEFAULT_FROM_EMAIL') or os.getenv('EMAIL_HOST_USER') or 'no-reply@smarthfashion.local',
            recipient_list=[to_email],
            fail_silently=not debug_mode,
        )
    except Exception as e:
        if not debug_mode and not debug_fallback:
            raise


@api_view(['GET', 'PATCH'])
@permission_classes([IsAuthenticated])
def profile(request):
    """Get or update basic profile fields (first_name, last_name).
    Username update is optional and validated for uniqueness.
    """
    user = request.user
    if request.method == 'GET':
        with connection.cursor() as cursor:
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS user_emails (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    is_verified TINYINT(1) DEFAULT 0,
                    is_primary TINYINT(1) DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uniq_user_email (user_id, email),
                    INDEX idx_user (user_id)
                ) ENGINE=InnoDB
            """)
            cursor.execute("SELECT email, is_verified, is_primary FROM user_emails WHERE user_id=%s ORDER BY is_primary DESC, id DESC", [user.id])
            rows = cursor.fetchall() or []
        emails = [{'email': r[0], 'is_verified': int(r[1])==1, 'is_primary': int(r[2])==1} for r in rows]
        if user.email and not any(e['email'] == user.email for e in emails):
            emails.insert(0, {'email': user.email, 'is_verified': True, 'is_primary': True})
        return Response({
            'id': user.id,
            'username': user.username,
            'email': user.email,
            'first_name': user.first_name,
            'last_name': user.last_name,
            'emails': emails,
        })
    data = request.data
    first_name = (data.get('first_name') or '').strip()
    last_name = (data.get('last_name') or '').strip()
    username = (data.get('username') or '').strip() or None
    if username:
        import re
        if not re.fullmatch(r'[a-zA-Z0-9_\.\-]{3,20}', username):
            return Response({'detail': 'Username inválido'}, status=400)
        if User.objects.exclude(id=user.id).filter(username=username).exists():
            return Response({'detail': 'Username no disponible'}, status=400)
        user.username = username
    user.first_name = first_name
    user.last_name = last_name
    user.save()
    return Response({'detail': 'updated'})


@api_view(['GET', 'POST', 'DELETE'])
@permission_classes([IsAuthenticated])
def emails(request):
    """List, add or delete secondary emails for the authenticated user.
    POST { email } -> creates (verified=0), sends verification code to new email
    DELETE with ?email=... -> removes if not primary
    """
    user = request.user
    with connection.cursor() as cursor:
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS user_emails (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id BIGINT NOT NULL,
                email VARCHAR(255) NOT NULL,
                is_verified TINYINT(1) DEFAULT 0,
                is_primary TINYINT(1) DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uniq_user_email (user_id, email),
                INDEX idx_user (user_id)
            ) ENGINE=InnoDB
        """)
    if request.method == 'GET':
        with connection.cursor() as cursor:
            cursor.execute("SELECT email, is_verified, is_primary FROM user_emails WHERE user_id=%s ORDER BY is_primary DESC, id DESC", [user.id])
            rows = cursor.fetchall() or []
        out = [{'email': r[0], 'is_verified': int(r[1])==1, 'is_primary': int(r[2])==1} for r in rows]
        if user.email and not any(e['email'] == user.email for e in out):
            out.insert(0, {'email': user.email, 'is_verified': True, 'is_primary': True})
        return Response({'data': out})
    if request.method == 'DELETE':
        email = (request.GET.get('email') or '').strip()
        if not email:
            return Response({'detail': 'email requerido'}, status=400)
        if email == user.email:
            return Response({'detail': 'No puedes eliminar el email principal'}, status=400)
        with connection.cursor() as cursor:
            cursor.execute("DELETE FROM user_emails WHERE user_id=%s AND email=%s", [user.id, email])
        try:
            _send_email('Se eliminó un correo vinculado', f'Se eliminó {email} de tu cuenta. Si no fuiste tú, contacta soporte.', user.email)
        except Exception:
            pass
        return Response({'detail': 'deleted'})
    email = (request.data.get('email') or '').strip().lower()
    if not email:
        return Response({'detail': 'email requerido'}, status=400)
    with connection.cursor() as cursor:
        try:
            cursor.execute("INSERT INTO user_emails (user_id, email, is_verified, is_primary) VALUES (%s, %s, 0, 0)", [user.id, email])
        except Exception:
            pass
    code = get_random_string(6, allowed_chars='0123456789')
    cache.set(f'email_verify:{user.id}:{email}', code, timeout=900)
    signer = TimestampSigner()
    token = signer.sign(json.dumps({'uid': user.id, 'email': email}))
    link = f"{request.build_absolute_uri('/api/auth/emails/verify_link/')}?token={token}"
    _send_email('Verifica tu correo - SmartFashion', f'Tu código de verificación es: {code}. Expira en 15 minutos.\nO haz clic: {link}', email)
    return Response({'detail': 'verification_sent'})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def emails_verify(request):
    user = request.user
    email = (request.data.get('email') or '').strip().lower()
    code = (request.data.get('code') or '').strip()
    if not email or not code:
        return Response({'detail': 'email y code requeridos'}, status=400)
    expected = cache.get(f'email_verify:{user.id}:{email}')
    if not expected:
        return Response({'detail': 'Código expirado o no encontrado'}, status=400)
    if str(expected) != str(code):
        return Response({'detail': 'Código inválido'}, status=400)
    with connection.cursor() as cursor:
        cursor.execute("UPDATE user_emails SET is_verified=1 WHERE user_id=%s AND email=%s", [user.id, email])
    cache.delete(f'email_verify:{user.id}:{email}')
    return Response({'detail': 'verified'})


@api_view(['GET'])
@permission_classes([AllowAny])
def emails_verify_link(request):
    token = request.GET.get('token')
    if not token:
        return Response({'detail': 'token requerido'}, status=400)
    signer = TimestampSigner()
    try:
        data = signer.unsign(token, max_age=900)
        payload = json.loads(data)
        uid = int(payload.get('uid'))
        email = payload.get('email')
    except Exception:
        return Response({'detail': 'token inválido o expirado'}, status=400)
    with connection.cursor() as cursor:
        cursor.execute("UPDATE user_emails SET is_verified=1 WHERE user_id=%s AND email=%s", [uid, email])
    return Response({'detail': 'verified'})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def emails_set_primary(request):
    user = request.user
    email = (request.data.get('email') or '').strip().lower()
    if not email:
        return Response({'detail': 'email requerido'}, status=400)
    with connection.cursor() as cursor:
        cursor.execute("SELECT is_verified FROM user_emails WHERE user_id=%s AND email=%s", [user.id, email])
        row = cursor.fetchone()
        if not row:
            return Response({'detail': 'Email no encontrado en tu lista'}, status=404)
        if int(row[0]) != 1:
            return Response({'detail': 'Debes verificar este email antes de hacerlo principal'}, status=400)
        cursor.execute("UPDATE user_emails SET is_primary=0 WHERE user_id=%s", [user.id])
        cursor.execute("UPDATE user_emails SET is_primary=1 WHERE user_id=%s AND email=%s", [user.id, email])
    old_primary = user.email
    user.email = email
    user.save()
    try:
        if old_primary:
            _send_email('Tu correo principal fue cambiado', f'Se cambió tu correo principal a {email}. Si no fuiste tú, contacta soporte.', old_primary)
        if email:
            _send_email('Confirmación de correo principal', 'Este correo ahora es tu principal en SmartFashion.', email)
    except Exception:
        pass
    return Response({'detail': 'primary_set', 'email': email})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def password_change_request(request):
    """Starts password change: verify current password, send code to primary email, stash new_password temporarily in cache.
    """
    user = request.user
    current_password = request.data.get('current_password')
    new_password = request.data.get('new_password')
    if not current_password or not new_password:
        return Response({'detail': 'Contraseña actual y nueva requeridas'}, status=400)
    if not user.check_password(current_password):
        return Response({'detail': 'Contraseña actual incorrecta'}, status=400)
    try:
        validate_password(new_password, user=user)
    except Exception as e:
        return Response({'detail': ' '.join([str(x) for x in e])}, status=400)
    code = get_random_string(6, allowed_chars='0123456789')
    cache.set(f'pwd_change:{user.id}:code', code, timeout=900)
    cache.set(f'pwd_change:{user.id}:new', new_password, timeout=900)
    to_email = user.email or ''
    if not to_email:
        return Response({'detail': 'Tu cuenta no tiene email principal configurado'}, status=400)
    _send_email('Confirma cambio de contraseña - SmartFashion', f'Tu código de confirmación es: {code}. Expira en 15 minutos.', to_email)
    return Response({'detail': 'verification_sent'})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def password_change_verify(request):
    user = request.user
    code = (request.data.get('code') or '').strip()
    otp = (request.data.get('otp') or '').strip()
    if not code:
        return Response({'detail': 'Código requerido'}, status=400)
    expected = cache.get(f'pwd_change:{user.id}:code')
    if not expected:
        return Response({'detail': 'Código expirado o no encontrado'}, status=400)
    if str(expected) != str(code):
        return Response({'detail': 'Código inválido'}, status=400)
    new_password = cache.get(f'pwd_change:{user.id}:new')
    if not new_password:
        return Response({'detail': 'Nueva contraseña expirada, solicita de nuevo'}, status=400)
    user.set_password(new_password)
    user.save()
    cache.delete(f'pwd_change:{user.id}:code')
    cache.delete(f'pwd_change:{user.id}:new')
    return Response({'detail': 'password_changed'})



@api_view(['GET', 'POST'])
@permission_classes([IsAuthenticated])
def security_totp_setup(request):
    user = request.user
    with connection.cursor() as cursor:
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS user_security (
                user_id BIGINT PRIMARY KEY,
                totp_secret VARCHAR(64) NULL,
                totp_enabled TINYINT(1) DEFAULT 0,
                revoked_after TIMESTAMP NULL
            ) ENGINE=InnoDB
        """)
    if request.method == 'GET':
        with connection.cursor() as cursor:
            cursor.execute("SELECT totp_enabled FROM user_security WHERE user_id=%s", [user.id])
            row = cursor.fetchone()
        return Response({'totp_enabled': bool(row and int(row[0])==1)})
    secret = pyotp.random_base32()
    with connection.cursor() as cursor:
        cursor.execute("INSERT INTO user_security (user_id, totp_secret, totp_enabled) VALUES (%s, %s, 0) ON DUPLICATE KEY UPDATE totp_secret=VALUES(totp_secret)", [user.id, secret])
    issuer = 'SmartFashion'
    label = user.email or user.username
    uri = pyotp.totp.TOTP(secret).provisioning_uri(name=label, issuer_name=issuer)
    return Response({'secret': secret, 'otpauth_url': uri})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def security_totp_enable(request):
    user = request.user
    code = (request.data.get('code') or '').strip()
    with connection.cursor() as cursor:
        cursor.execute("SELECT totp_secret FROM user_security WHERE user_id=%s", [user.id])
        row = cursor.fetchone()
    secret = row[0] if row else None
    if not secret or not code or not pyotp.TOTP(secret).verify(str(code), valid_window=1):
        return Response({'detail': 'Código inválido'}, status=400)
    with connection.cursor() as cursor:
        cursor.execute("UPDATE user_security SET totp_enabled=1 WHERE user_id=%s", [user.id])
    return Response({'detail': 'totp_enabled'})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def security_totp_disable(request):
    user = request.user
    with connection.cursor() as cursor:
        cursor.execute("UPDATE user_security SET totp_enabled=0 WHERE user_id=%s", [user.id])
    return Response({'detail': 'totp_disabled'})


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def sessions_list(request):
    user = request.user
    with connection.cursor() as cursor:
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS user_sessions (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id BIGINT NOT NULL,
                jti VARCHAR(64) NOT NULL,
                user_agent TEXT,
                ip VARCHAR(64),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB
        """)
        cursor.execute("SELECT id, jti, ip, LEFT(user_agent, 120), created_at FROM user_sessions WHERE user_id=%s ORDER BY id DESC LIMIT 50", [user.id])
        rows = cursor.fetchall() or []
    return Response({'data': [ {'id': r[0], 'jti': r[1], 'ip': r[2], 'ua': r[3], 'created_at': r[4]} for r in rows ]})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def sessions_logout_all(request):
    user = request.user
    now = timezone.now()
    with connection.cursor() as cursor:
        cursor.execute("INSERT INTO user_security (user_id, revoked_after) VALUES (%s, %s) ON DUPLICATE KEY UPDATE revoked_after=VALUES(revoked_after)", [user.id, now])
    return Response({'detail': 'revoked'})
