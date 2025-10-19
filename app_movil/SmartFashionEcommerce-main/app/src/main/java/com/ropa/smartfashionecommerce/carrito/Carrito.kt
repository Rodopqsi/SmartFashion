package com.ropa.smartfashionecommerce.carrito

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

// ----------------------- Carrito Activity -----------------------
class Carrito : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CartManager.initialize(this)

        setContent {
            SmartFashionEcommerceTheme {
                ShoppingCartScreen(this)
            }
        }
    }
}

// ----------------------- Shopping Cart Screen -----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartScreen(activity: ComponentActivity? = null) {
    val context = LocalContext.current
    val cartItems by remember { derivedStateOf { CartManager.cartItems } }

    val subtotal = remember { derivedStateOf { CartManager.getTotal() } }
    val igv = subtotal.value * 0.18
    val total = subtotal.value + igv

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SmartFashion",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDFDFD))
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFECECEC))
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    "🛍️ Carrito de compras",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color.Black
                )

                if (cartItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tu carrito está vacío", fontSize = 18.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(cartItems, key = { it.name + it.size + it.color }) { item ->
                            CartItemCard(
                                item = item,
                                onIncrease = { CartManager.updateQuantity(item, item.quantity + 1) },
                                onDecrease = {
                                    if (item.quantity > 1) CartManager.updateQuantity(item, item.quantity - 1)
                                },
                                onDelete = { CartManager.removeItem(item) }
                            )
                        }
                    }

                    OrderSummary(
                        productCount = cartItems.size,
                        subtotal = subtotal.value,
                        igv = igv,
                        total = total,
                        onFinish = {
                            if (activity != null) {
                                AlertDialog.Builder(activity)
                                    .setTitle("✅ Compra realizada")
                                    .setMessage("Tu pedido ha sido procesado exitosamente 🎉")
                                    .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }
                            CartManager.clear()
                        }
                    )
                }
            }
        }
    )
}

// ----------------------- Cart Item Card -----------------------
@Composable
fun CartItemCard(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text("Talla: ${item.size} | Color: ${item.color}", fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ControlButton("-", onClick = onDecrease)
                        Text(
                            "${item.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        ControlButton("+", onClick = onIncrease)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "S/ ${"%.2f".format(item.quantity * item.price)}",
                                color = Color(0xFF007ACC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("S/ ${"%.2f".format(item.price)}/u", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF007ACC))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

// ----------------------- Order Summary -----------------------
@Composable
fun OrderSummary(productCount: Int, subtotal: Double, igv: Double, total: Double, onFinish: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Resumen del pedido", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal ($productCount productos)", color = Color.Black)
                Text("S/ ${"%.2f".format(subtotal)}", color = Color.Black)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("IGV (18%)", color = Color.Black)
                Text("S/ ${"%.2f".format(igv)}", color = Color.Black)
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Gray)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(
                    "S/ ${"%.2f".format(total)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF007ACC),
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Botón de Finalizar compra
            Button(
                onClick = {
                    val intent = Intent(context, FinalizarCompra::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Finalizar compra", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }


            Spacer(modifier = Modifier.height(10.dp))

            // ✅ Nuevo botón: Continuar comprando
            OutlinedButton(
                onClick = {
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                Text("Continuar comprando", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Compra 100% segura. Tus datos están protegidos.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
