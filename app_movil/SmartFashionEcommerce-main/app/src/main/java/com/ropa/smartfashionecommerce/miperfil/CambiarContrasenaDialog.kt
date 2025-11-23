package com.ropa.smartfashionecommerce.miperfil

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CambiarContrasenaDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var contrasenaActual by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    // 👁️ Estados para mostrar/ocultar las contraseñas
    var mostrarActual by remember { mutableStateOf(false) }
    var mostrarNueva by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🏷 Título
                Text(
                    text = "Cambiar Contraseña",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ingresa tu contraseña actual y la nueva contraseña",
                    color = Color(0xFF616161),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 🧾 Contraseña actual
                OutlinedTextField(
                    value = contrasenaActual,
                    onValueChange = { contrasenaActual = it },
                    label = { Text("Contraseña actual", color = Color(0xFF424242)) },
                    textStyle = TextStyle(color = Color.Black),
                    singleLine = true,
                    visualTransformation = if (mostrarActual) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        val image = if (mostrarActual) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { mostrarActual = !mostrarActual }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 🧾 Nueva contraseña
                OutlinedTextField(
                    value = nuevaContrasena,
                    onValueChange = { nuevaContrasena = it },
                    label = { Text("Nueva contraseña", color = Color(0xFF424242)) },
                    textStyle = TextStyle(color = Color.Black),
                    singleLine = true,
                    visualTransformation = if (mostrarNueva) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        val image = if (mostrarNueva) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { mostrarNueva = !mostrarNueva }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 🧾 Confirmar nueva contraseña
                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it },
                    label = { Text("Confirmar nueva contraseña", color = Color(0xFF424242)) },
                    textStyle = TextStyle(color = Color.Black),
                    singleLine = true,
                    visualTransformation = if (mostrarConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        val image = if (mostrarConfirmar) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { mostrarConfirmar = !mostrarConfirmar }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(25.dp))

                // 🔘 Botones
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón Cancelar
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = Color(0xFF212121), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Botón Cambiar Contraseña
                    Button(
                        onClick = {
                            if (nuevaContrasena != confirmarContrasena) {
                                Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (nuevaContrasena.length < 6) {
                                Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            cargando = true

                            user?.email?.let { email ->
                                val credential = EmailAuthProvider.getCredential(email, contrasenaActual)
                                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                                    if (reauthTask.isSuccessful) {
                                        user.updatePassword(nuevaContrasena).addOnCompleteListener { updateTask ->
                                            cargando = false
                                            if (updateTask.isSuccessful) {
                                                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            } else {
                                                Toast.makeText(context, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        cargando = false
                                        Toast.makeText(context, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !cargando
                    ) {
                        Text("Cambiar contraseña", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
