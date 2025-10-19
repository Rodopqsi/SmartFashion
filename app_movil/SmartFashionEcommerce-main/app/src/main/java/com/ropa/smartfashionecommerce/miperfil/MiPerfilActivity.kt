package com.ropa.smartfashionecommerce.miperfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class MiPerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // ✅ Solo usamos el callback de "volver atrás"
                    MiPerfilScreen(onBack = { finish() })
                }
            }
        }
    }
}
