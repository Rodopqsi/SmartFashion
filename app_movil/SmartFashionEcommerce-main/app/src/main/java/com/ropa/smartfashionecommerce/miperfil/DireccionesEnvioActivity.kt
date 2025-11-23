package com.ropa.smartfashionecommerce.miperfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class DireccionesEnvioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                // ✅ Llamamos a DireccionesEnvioScreen con el parámetro correcto 'onBack'
                DireccionesEnvioScreen(onBack = { finish() })
            }
        }
    }
}
