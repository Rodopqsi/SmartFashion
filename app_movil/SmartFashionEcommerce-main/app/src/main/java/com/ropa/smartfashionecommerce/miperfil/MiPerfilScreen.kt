package com.ropa.smartfashionecommerce.miperfil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.DarkLoginActivity
import com.ropa.smartfashionecommerce.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPerfilScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val photoUrl = user?.photoUrl
    val email = user?.email ?: "correo@ejemplo.com"

    val sharedPrefs = context.getSharedPreferences("SmartFashionPrefs", Context.MODE_PRIVATE)
    var nombre by remember { mutableStateOf(sharedPrefs.getString("nombre", user?.displayName ?: "") ?: "") }
    var telefono by remember { mutableStateOf(sharedPrefs.getString("telefono", "") ?: "") }
    var fechaNacimiento by remember { mutableStateOf(sharedPrefs.getString("fechaNacimiento", "") ?: "") }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showNotificacionesDialog by remember { mutableStateOf(false) }
    var showDireccionEnvioDialog by remember { mutableStateOf(false) }

    // Lista de direcciones
    var direcciones by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF212121))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFEAEAEA))))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Color(0xFF000000), Color(0xFF757575))),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl ?: R.drawable.ic_person),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Información personal
            Text("Información personal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo", color = Color(0xFF424242)) },
                textStyle = TextStyle(color = Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Correo electrónico", color = Color(0xFF424242)) },
                textStyle = TextStyle(color = Color.Black),
                singleLine = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono", color = Color(0xFF424242)) },
                textStyle = TextStyle(color = Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha de nacimiento", color = Color(0xFF424242)) },
                textStyle = TextStyle(color = Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    sharedPrefs.edit().apply {
                        putString("nombre", nombre)
                        putString("telefono", telefono)
                        putString("fechaNacimiento", fechaNacimiento)
                        apply()
                    }
                    Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Guardar cambios", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Historial de Pedidos
            Text("Historial de Pedidos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))
            PedidoItem("#ORD-001", "Entregado", Color(0xFF4CAF50), "S/ 249.80")
            PedidoItem("#ORD-002", "En tránsito", Color(0xFF3F51B5), "S/ 159.90")
            PedidoItem("#ORD-003", "Procesando", Color(0xFFFFC107), "S/ 89.90")

            Spacer(modifier = Modifier.height(25.dp))

            // Configuración de Cuenta
            Text("Configuración de Cuenta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))

            ProfileOptionCard(Icons.Default.Lock, "Cambiar Contraseña", "Actualiza tu contraseña") {
                showChangePasswordDialog = true
            }

            ProfileOptionCard(Icons.Default.Notifications, "Preferencias de Notificaciones", "Alertas y promociones") {
                showNotificacionesDialog = true
            }

            ProfileOptionCard(Icons.Default.Home, "Dirección de Envío", "Gestiona tus direcciones") {
                val intent = Intent(context, DireccionesEnvioActivity::class.java)
                context.startActivity(intent)
            }

            ProfileOptionCard(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar Sesión", "Salir de tu cuenta") {
                Firebase.auth.signOut()
                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, DarkLoginActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Versión 1.0.0",
                color = Color(0xFF757575),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }

    // Diálogos
    if (showChangePasswordDialog) {
        CambiarContrasenaDialog(onDismiss = { showChangePasswordDialog = false })
    }
    if (showNotificacionesDialog) {
        PreferenciasNotificacionesDialog(onDismiss = { showNotificacionesDialog = false })
    }
    if (showDireccionEnvioDialog) {
        DireccionEnvioDialog(
            onDismissRequest = { showDireccionEnvioDialog = false },
            onSave = { nombre, detalle ->
                direcciones = direcciones + Pair(nombre, detalle)
                Toast.makeText(context, "Dirección guardada", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun PedidoItem(codigo: String, estado: String, colorEstado: Color, precio: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Pedido $codigo", fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(colorEstado, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(estado, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Text(precio, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        }
    }
}

@Composable
fun ProfileOptionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color.Black, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF212121))
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF616161))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF616161))
        }
    }
}
