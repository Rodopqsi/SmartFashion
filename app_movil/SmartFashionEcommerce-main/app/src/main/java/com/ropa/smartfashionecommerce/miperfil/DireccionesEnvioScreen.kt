package com.ropa.smartfashionecommerce.miperfil

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionesEnvioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("SmartFashionPrefs", Context.MODE_PRIVATE)

    // ✅ El estado debe ser un nuevo Set mutable cuando cambie
    var direcciones by remember {
        mutableStateOf(sharedPrefs.getStringSet("direcciones_envio", emptySet())!!.toMutableSet())
    }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Direcciones de Envío", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF212121))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF1A237E)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar dirección", tint = Color.White)
            }
        }
    ) { padding ->
        if (direcciones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes direcciones guardadas", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF9F9F9))
                    .padding(16.dp)
            ) {
                items(direcciones.toList()) { direccion ->
                    DireccionCard(
                        direccion = direccion,
                        onDelete = {
                            // ✅ Crear una nueva copia al eliminar para forzar recomposición
                            val updated = direcciones.toMutableSet()
                            updated.remove(direccion)
                            sharedPrefs.edit().putStringSet("direcciones_envio", updated).apply()
                            direcciones = updated
                            Toast.makeText(context, "Dirección eliminada", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ✅ Diálogo para agregar nueva dirección
    if (showDialog) {
        DireccionEnvioDialog(
            onDismissRequest = { showDialog = false },
            onSave = { nombre, detalle ->
                val nuevaDireccion = "$nombre - $detalle"
                // ✅ Crear nueva copia también al agregar
                val updated = direcciones.toMutableSet()
                updated.add(nuevaDireccion)
                sharedPrefs.edit().putStringSet("direcciones_envio", updated).apply()
                direcciones = updated
                Toast.makeText(context, "Dirección guardada", Toast.LENGTH_SHORT).show()
                showDialog = false
            }
        )
    }
}

@Composable
fun DireccionCard(direccion: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = direccion,
                color = Color(0xFF1A1A1A),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red
                )
            }
        }
    }
}
