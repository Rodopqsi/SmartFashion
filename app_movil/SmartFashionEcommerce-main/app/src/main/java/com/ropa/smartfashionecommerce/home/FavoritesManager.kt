package com.ropa.smartfashionecommerce.home

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class FavoriteItem(
    val id: Int,
    val name: String,
    val price: String,
    val sizes: List<String> = listOf("S", "M", "L"),
    val imageRes: Int,
    var isFavorite: Boolean = true
)

object FavoritesManager {
    private val _favoriteItems = mutableStateListOf<FavoriteItem>()
    val favoriteItems: SnapshotStateList<FavoriteItem> get() = _favoriteItems

    fun addFavorite(item: FavoriteItem) {
        if (_favoriteItems.none { it.id == item.id }) {
            _favoriteItems.add(item)
        }
    }

    fun removeFavorite(item: FavoriteItem) {
        _favoriteItems.removeIf { it.id == item.id }
    }
}
