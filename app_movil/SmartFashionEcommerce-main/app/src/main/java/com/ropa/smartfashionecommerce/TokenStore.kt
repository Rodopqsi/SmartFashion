package com.ropa.smartfashionecommerce

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth")

class TokenStore(private val context: Context) {
    private val KEY_ACCESS = stringPreferencesKey("access")
    private val KEY_REFRESH = stringPreferencesKey("refresh")

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit { it[KEY_ACCESS] = access; it[KEY_REFRESH] = refresh }
    }
    suspend fun getAccess(): String? = context.dataStore.data.map { it[KEY_ACCESS] }.first()
    suspend fun getRefresh(): String? = context.dataStore.data.map { it[KEY_REFRESH] }.first()
    suspend fun clear() { context.dataStore.edit { it.clear() } }
}