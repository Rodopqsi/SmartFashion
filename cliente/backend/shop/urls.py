from django.urls import path
from .views import (
    home, sizes, colors, product_detail, product_reviews,
    checkout_preview, checkout_confirm,
    address_default, address_set_default,
    addresses, address_detail, address_mark_default,
    order_tracking,
)

urlpatterns = [
    path('home/', home, name='home'),
    path('sizes/', sizes, name='sizes'),
    path('colors/', colors, name='colors'),
    path('products/<int:pk>/', product_detail, name='product-detail'),
    path('products/<int:pk>/reviews/', product_reviews, name='product-reviews'),
    path('checkout/preview/', checkout_preview, name='checkout-preview'),
    path('checkout/confirm/', checkout_confirm, name='checkout-confirm'),
    # Addresses
    path('addresses/', addresses, name='addresses'),
    path('addresses/<int:addr_id>/', address_detail, name='address-detail'),
    path('addresses/<int:addr_id>/default', address_mark_default, name='address-mark-default'),
    # Back-compat simple default endpoints
    path('profile/address/default', address_default, name='address-default'),
    path('profile/address/default/set', address_set_default, name='address-set-default'),
    # Tracking helper
    path('orders/<str:order_number>/tracking', order_tracking, name='order-tracking'),
]
