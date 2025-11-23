package com.ropa.smartfashionecommerce.carrito

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class FinalizarCompra : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CartManager.initialize(this)

        setContent {
            SmartFashionEcommerceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF9F9F9)
                ) {
                    FinalizarCompraScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizarCompraScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // ✅ Productos actuales del carrito
    val cartItems by remember { derivedStateOf { CartManager.cartItems } }

    // ✅ Cálculos del total
    val subtotal = remember { derivedStateOf { CartManager.getTotal() } }
    val igv = subtotal.value * 0.18
    val total = subtotal.value + igv

    // ✅ Datos del formulario
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }

    // ✅ Método de pago
    var metodoPago by remember { mutableStateOf("Tarjeta") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var nombreTarjeta by remember { mutableStateOf("") }
    var numeroYape by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.Black
                                )
                            }
                            Text("Volver", color = Color.Black, fontSize = 16.sp)
                        }

                        Text(
                            "SMARTFASHION",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Finalizar Compra",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🧍 Datos personales
            SectionCard(title = "Datos personales") {
                CustomTextField("Nombres", nombres) { nombres = it }
                CustomTextField("Apellidos", apellidos) { apellidos = it }
                CustomTextField("Correo electrónico", correo, KeyboardType.Email) { correo = it }
                CustomTextField("Teléfono", telefono, KeyboardType.Phone) { telefono = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 📦 Dirección de envío
            SectionCard(title = "Dirección de envío") {
                CustomTextField("Dirección completa", direccion) { direccion = it }
                CustomTextField("Ciudad", ciudad) { ciudad = it }
                CustomTextField("Departamento", departamento) { departamento = it }
                CustomTextField(
                    label = "Código postal",
                    value = codigoPostal,
                    keyboardType = KeyboardType.Number,
                    placeholder = "Ejemplo: 13001"
                ) { codigoPostal = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 💳 Método de pago
            SectionCard(title = "Método de pago") {
                Text("Selecciona un método:", fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PaymentOptionButton(
                        modifier = Modifier.weight(1f),
                        text = "Tarjeta",
                        selected = metodoPago == "Tarjeta"
                    ) { metodoPago = "Tarjeta" }

                    PaymentOptionButton(
                        modifier = Modifier.weight(1f),
                        text = "Yape",
                        selected = metodoPago == "Yape"
                    ) { metodoPago = "Yape" }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (metodoPago == "Tarjeta") {
                    CustomTextField(
                        label = "Número de tarjeta",
                        value = numeroTarjeta,
                        keyboardType = KeyboardType.Number,
                        placeholder = "**** **** **** 1234"
                    ) { numeroTarjeta = it }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        CustomTextField(
                            label = "Fecha de vencimiento",
                            value = fechaVencimiento,
                            modifier = Modifier.weight(1f),
                            placeholder = "MM/AA"
                        ) { fechaVencimiento = it }

                        CustomTextField(
                            label = "CVV",
                            value = cvv,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            placeholder = "123"
                        ) { cvv = it }
                    }

                    CustomTextField(
                        label = "Nombre en la tarjeta",
                        value = nombreTarjeta
                    ) { nombreTarjeta = it }
                } else {
                    CustomTextField(
                        label = "Número de celular Yape",
                        value = numeroYape,
                        keyboardType = KeyboardType.Phone,
                        placeholder = "Ejemplo: 987654321"
                    ) { numeroYape = it }

                    Text(
                        "Se enviará una solicitud de pago a tu Yape.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🧾 Resumen del pedido
            SectionCard(title = "Resumen del pedido") {
                if (cartItems.isEmpty()) {
                    Text("No hay productos en el carrito", color = Color.Gray)
                } else {
                    cartItems.forEach {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${it.name} x${it.quantity}", color = Color.Black)
                            Text("S/ ${"%.2f".format(it.price * it.quantity)}", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = Color.Black)
                        Text("S/ ${"%.2f".format(subtotal.value)}", color = Color.Black)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("IGV (18%)", color = Color.Black)
                        Text("S/ ${"%.2f".format(igv)}", color = Color.Black)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a pagar", fontWeight = FontWeight.Bold, color = Color(0xFF007ACC))
                        Text("S/ ${"%.2f".format(total)}", fontWeight = FontWeight.Bold, color = Color(0xFF007ACC))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Botón Confirmar pedido
            Button(
                onClick = {
                    val faltantes = mutableListOf<String>()
                    if (nombres.isBlank()) faltantes.add("Nombres")
                    if (apellidos.isBlank()) faltantes.add("Apellidos")
                    if (correo.isBlank()) faltantes.add("Correo electrónico")
                    if (telefono.isBlank()) faltantes.add("Teléfono")
                    if (direccion.isBlank()) faltantes.add("Dirección")
                    if (ciudad.isBlank()) faltantes.add("Ciudad")
                    if (departamento.isBlank()) faltantes.add("Departamento")
                    if (codigoPostal.isBlank()) faltantes.add("Código postal")

                    if (metodoPago == "Tarjeta") {
                        if (numeroTarjeta.isBlank()) faltantes.add("Número de tarjeta")
                        if (fechaVencimiento.isBlank()) faltantes.add("Fecha de vencimiento")
                        if (cvv.isBlank()) faltantes.add("CVV")
                        if (nombreTarjeta.isBlank()) faltantes.add("Nombre en la tarjeta")
                    } else if (numeroYape.isBlank()) faltantes.add("Número de Yape")

                    if (faltantes.isNotEmpty()) {
                        AlertDialog.Builder(context)
                            .setTitle("Campos incompletos ⚠️")
                            .setMessage("Faltan: \n\n${faltantes.joinToString(", ")}")
                            .setPositiveButton("Aceptar", null)
                            .show()
                    } else {
                        AlertDialog.Builder(context)
                            .setTitle("✅ Pedido confirmado")
                            .setMessage("Tu compra se ha realizado exitosamente 🎉")
                            .setPositiveButton("Aceptar") { dialog, _ ->
                                dialog.dismiss()
                                // ✅ Limpiar carrito después del pago
                                CartManager.clear()
                            }
                            .show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar pedido", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Compra 100% segura",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// 🔹 Botón para método de pago
@Composable
fun PaymentOptionButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.Black else Color.LightGray
        ),
        modifier = modifier.height(45.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, color = Color.White)
    }
}

// 🔹 Campo personalizado
@Composable
fun CustomTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = Color.Gray) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black
        )
    )
}

// 🔹 Card para secciones
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
