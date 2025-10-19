package com.ropa.smartfashionecommerce.miperfil

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PreferenciasNotificacionesDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("SmartFashionPrefs", Context.MODE_PRIVATE)

    // Recuperar valores guardados o establecer por defecto
    var emailNoti by remember { mutableStateOf(sharedPrefs.getBoolean("notif_email", true)) }
    var smsNoti by remember { mutableStateOf(sharedPrefs.getBoolean("notif_sms", true)) }
    var promoNoti by remember { mutableStateOf(sharedPrefs.getBoolean("notif_promos", true)) }
    var pedidosNoti by remember { mutableStateOf(sharedPrefs.getBoolean("notif_pedidos", true)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            // 💡 SIN padding, como el de CambiarContraseña
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // 🏷 Título
                Text(
                    text = "Preferencias de Notificaciones",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF212121),
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configura cómo quieres recibir notificaciones",
                    color = Color(0xFF616161),
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 🔘 Opciones de notificaciones
                NotificationSwitch(
                    title = "Notificaciones por Email",
                    subtitle = "Recibe actualizaciones por correo",
                    checked = emailNoti,
                    onCheckedChange = { emailNoti = it }
                )
                NotificationSwitch(
                    title = "Notificaciones por SMS",
                    subtitle = "Recibe mensajes de texto",
                    checked = smsNoti,
                    onCheckedChange = { smsNoti = it }
                )
                NotificationSwitch(
                    title = "Promociones y Ofertas",
                    subtitle = "Recibe ofertas especiales",
                    checked = promoNoti,
                    onCheckedChange = { promoNoti = it }
                )
                NotificationSwitch(
                    title = "Actualizaciones de Pedidos",
                    subtitle = "Estado de tus compras",
                    checked = pedidosNoti,
                    onCheckedChange = { pedidosNoti = it }
                )

                Spacer(modifier = Modifier.height(25.dp))

                // 🔘 Botones
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = ButtonDefaults.outlinedButtonBorder,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            "Cancelar",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            with(sharedPrefs.edit()) {
                                putBoolean("notif_email", emailNoti)
                                putBoolean("notif_sms", smsNoti)
                                putBoolean("notif_promos", promoNoti)
                                putBoolean("notif_pedidos", pedidosNoti)
                                apply()
                            }
                            Toast.makeText(context, "Preferencias guardadas", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A2463)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            "Guardar Preferencias",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                fontSize = 16.sp
            )
            Text(
                subtitle,
                fontSize = 13.sp,
                color = Color(0xFF616161)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0A2463)
            )
        )
    }
}
