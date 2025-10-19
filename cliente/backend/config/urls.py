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
    path('api/auth/token/', auth_views.CustomTokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/auth/token/refresh/', auth_views.CustomTokenRefreshView.as_view(), name='token_refresh'),
]
