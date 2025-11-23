from django.contrib import admin
from django.urls import path, include
from shop import auth_views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('shop.urls')),
    path('api/auth/register/', auth_views.register, name='register'),
    path('api/auth/register/verify/', auth_views.verify_email, name='verify_email'),
    path('api/auth/google/', auth_views.google_oauth, name='google_oauth'),
    path('api/auth/google/complete/', auth_views.complete_username, name='complete_username'),
    path('api/auth/password_reset/', auth_views.password_reset_request, name='password_reset_request'),
    path('api/auth/password_reset/verify/', auth_views.password_reset_verify, name='password_reset_verify'),
    path('api/auth/profile/', auth_views.profile, name='profile'),
    path('api/auth/emails/', auth_views.emails, name='emails'),
    path('api/auth/emails/verify/', auth_views.emails_verify, name='emails_verify'),
    path('api/auth/emails/set_primary/', auth_views.emails_set_primary, name='emails_set_primary'),
    path('api/auth/emails/verify_link/', auth_views.emails_verify_link, name='emails_verify_link'),
    path('api/auth/password_change/', auth_views.password_change_request, name='password_change_request'),
    path('api/auth/password_change/verify/', auth_views.password_change_verify, name='password_change_verify'),
    path('api/auth/security/totp/setup/', auth_views.security_totp_setup, name='security_totp_setup'),
    path('api/auth/security/totp/enable/', auth_views.security_totp_enable, name='security_totp_enable'),
    path('api/auth/security/totp/disable/', auth_views.security_totp_disable, name='security_totp_disable'),
    path('api/auth/sessions/', auth_views.sessions_list, name='sessions_list'),
    path('api/auth/sessions/logout_all/', auth_views.sessions_logout_all, name='sessions_logout_all'),
    path('api/auth/token/', auth_views.CustomTokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/auth/token/refresh/', auth_views.CustomTokenRefreshView.as_view(), name='token_refresh'),
    path('sso/admin/', auth_views.admin_sso_redirect, name='admin_sso'),
]
