package com.ropa.smartfashionecommerce.carrito

data class CartItem(
    val name: String,
    val size: String,
    val color: String,
    var quantity: Int,
    val price: Double,
    val imageRes: Int
)
