from django.urls import path
from .views import (
    home, sizes, colors, product_detail, product_reviews,
    checkout_preview, checkout_confirm,
    address_default, address_set_default,
    addresses, address_detail, address_mark_default,
    order_tracking,
    claims, claim_detail, returns, return_detail,
    collection_detail,
    payments_create_session, payments_webhook,
)
from .views import catalog_snapshot, chat_ai, chat_ai_status

urlpatterns = [
    path('home/', home, name='home'),
    path('sizes/', sizes, name='sizes'),
    path('colors/', colors, name='colors'),
    path('products/<int:pk>/', product_detail, name='product-detail'),
    path('products/<int:pk>/reviews/', product_reviews, name='product-reviews'),
    path('checkout/preview/', checkout_preview, name='checkout-preview'),
    path('checkout/confirm/', checkout_confirm, name='checkout-confirm'),
    # Payments (Stripe Checkout)
    path('payments/create_session/', payments_create_session, name='payments-create-session'),
    path('payments/webhook/', payments_webhook, name='payments-webhook'),
    # Addresses
    path('addresses/', addresses, name='addresses'),
    path('addresses/<int:addr_id>/', address_detail, name='address-detail'),
    path('addresses/<int:addr_id>/default', address_mark_default, name='address-mark-default'),
    # Back-compat simple default endpoints
    path('profile/address/default', address_default, name='address-default'),
    path('profile/address/default/set', address_set_default, name='address-set-default'),
    # Tracking helper
    path('orders/<str:order_number>/tracking', order_tracking, name='order-tracking'),
    # Claims & Returns
    path('claims/', claims, name='claims'),
    path('claims/<int:pk>/', claim_detail, name='claim-detail'),
    path('returns/', returns, name='returns'),
    path('returns/<int:pk>/', return_detail, name='return-detail'),
    # Collections detail
    path('collections/<slug:slug>/', collection_detail, name='collection-detail'),
    path('catalog/snapshot/', catalog_snapshot, name='catalog-snapshot'),
    path('chat/ai/', chat_ai, name='chat-ai'),
    path('chat/ai/status/', chat_ai_status, name='chat-ai-status'),
]
