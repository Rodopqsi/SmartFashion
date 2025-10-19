from django.urls import path
from .views import home, sizes, colors

urlpatterns = [
    path('home/', home, name='home'),
    path('sizes/', sizes, name='sizes'),
    path('colors/', colors, name='colors'),
]
