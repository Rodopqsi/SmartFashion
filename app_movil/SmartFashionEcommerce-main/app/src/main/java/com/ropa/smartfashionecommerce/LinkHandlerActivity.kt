package com.ropa.smartfashionecommerce

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LinkHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data

        if (data != null && data.isHierarchical) {
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")

            when (mode) {
                "resetPassword" -> {
                    // Restablecer contraseña
                    val intent = Intent(this, RecoverPasswordActivity::class.java)
                    intent.putExtra("oobCode", oobCode)
                    startActivity(intent)
                    finish()
                }

                "verifyEmail" -> {
                    // Verificación de correo
                    if (oobCode != null) {
                        FirebaseAuth.getInstance()
                            .applyActionCode(oobCode)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Email verificado correctamente
                                    val verifiedIntent =
                                        Intent(this, DarkLoginActivity::class.java)
                                    verifiedIntent.putExtra(
                                        "verified_message",
                                        "¡Tu correo fue verificado correctamente!"
                                    )
                                    startActivity(verifiedIntent)
                                } else {
                                    // Error al verificar
                                    val errorIntent =
                                        Intent(this, DarkLoginActivity::class.java)
                                    errorIntent.putExtra(
                                        "verified_message",
                                        "Hubo un problema al verificar tu correo."
                                    )
                                    startActivity(errorIntent)
                                }
                                finish()
                            }
                    } else {
                        startActivity(Intent(this, DarkLoginActivity::class.java))
                        finish()
                    }
                }

                else -> {
                    startActivity(Intent(this, DarkLoginActivity::class.java))
                    finish()
                }
            }
        } else {
            startActivity(Intent(this, DarkLoginActivity::class.java))
            finish()
        }
    }
}
