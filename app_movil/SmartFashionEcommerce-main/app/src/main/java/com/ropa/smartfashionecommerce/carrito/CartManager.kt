package com.ropa.smartfashionecommerce.carrito

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private val gson = Gson()
    private val _cartItems: SnapshotStateList<CartItem> = mutableStateListOf()
    val cartItems: List<CartItem> get() = _cartItems

    private const val PREFS_NAME = "cart_prefs"
    private const val CART_KEY = "cart_items"

    // Inicializa el carrito cargando los datos guardados
    fun initialize(context: Context) {
        loadCart(context)
    }

    // Agregar un producto
    fun addItem(item: CartItem) {
        val existingItem = _cartItems.find {
            it.name == item.name && it.size == item.size && it.color == item.color
        }

        if (existingItem != null) {
            existingItem.quantity += item.quantity
        } else {
            _cartItems.add(item)
        }
    }

    // Quitar producto
    fun removeItem(item: CartItem) {
        _cartItems.remove(item)
    }

    // Actualizar cantidad
    fun updateQuantity(item: CartItem, newQuantity: Int) {
        val index = _cartItems.indexOf(item)
        if (index != -1) {
            _cartItems[index] = _cartItems[index].copy(quantity = newQuantity)
        }
    }

    // Vaciar carrito
    fun clear() {
        _cartItems.clear()
    }

    // Total del carrito
    fun getTotal(): Double {
        return _cartItems.sumOf { it.price * it.quantity }
    }

    // Guardar carrito
    fun saveCart(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(_cartItems)
        prefs.edit().putString(CART_KEY, json).apply()
    }

    // Cargar carrito
    private fun loadCart(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(CART_KEY, null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            val items: List<CartItem> = gson.fromJson(json, type)
            _cartItems.clear()
            _cartItems.addAll(items)
        }
    }
}
