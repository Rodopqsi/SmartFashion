package com.ropa.smartfashionecommerce.miperfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color

@Composable
fun DireccionEnvioDialog(
    onDismissRequest: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nombreDireccion by remember { mutableStateOf(TextFieldValue("")) }
    var detalleDireccion by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agregar Nueva Dirección",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Campo: Nombre de la dirección
                OutlinedTextField(
                    value = nombreDireccion,
                    onValueChange = { nombreDireccion = it },
                    label = { Text("Nombre (ej. Casa, Trabajo)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Campo: Detalle de dirección
                OutlinedTextField(
                    value = detalleDireccion,
                    onValueChange = { detalleDireccion = it },
                    label = { Text("Detalle (ej. Av. Javier Prado 123, Lima)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (nombreDireccion.text.isNotEmpty() && detalleDireccion.text.isNotEmpty()) {
                                onSave(nombreDireccion.text, detalleDireccion.text)
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A237E)
                        )
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}
