package com.ropa.smartfashionecommerce.detalles

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartItem
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.home.FavActivity
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                    ProductDetailScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("ZARA", "VOGUE", "CHANEL", "RALPH")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🖋️ SMARTFASHION → Ir al Home
                        Text(
                            text = "SMARTFASHION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF111111),
                            modifier = Modifier.clickable {
                                val intent = Intent(context, HomeActivity::class.java)
                                context.startActivity(intent)
                            }
                        )

                        // 🧭 Menú Categorías
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { expanded = true }) {
                                Text("Categorías ▼", color = Color.Black)
                            }

                            // 🔹 Aquí se agregó el Intent hacia CatalogActivity
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                categorias.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria) },
                                        onClick = {
                                            expanded = false
                                            // ✅ Abre el catálogo con la categoría seleccionada
                                            val intent = Intent(
                                                context,
                                                com.ropa.smartfashionecommerce.catalog.CatalogActivity::class.java
                                            )
                                            intent.putExtra("CATEGORY", categoria)
                                            context.startActivity(intent)
                                        }
                                    )
                                }
                            }
                        }

                        // ❤️ 🛒 👤 Botones de acción
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                context.startActivity(Intent(context, FavActivity::class.java))
                            }) {
                                Icon(
                                    Icons.Default.FavoriteBorder,
                                    contentDescription = "Favoritos",
                                    tint = Color.Black
                                )
                            }

                            IconButton(onClick = {
                                context.startActivity(Intent(context, Carrito::class.java))
                            }) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Carrito",
                                    tint = Color.Black
                                )
                            }

                            IconButton(onClick = {
                                context.startActivity(Intent(context, MiPerfilActivity::class.java))
                            }) {
                                Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.Black)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        ProductDetailContent(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ProductDetailContent(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    var selectedSize by remember { mutableStateOf("M") }
    var selectedColor by remember { mutableStateOf("Negro") }
    var quantity by remember { mutableIntStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = context as? ProductDetailActivity
    val intent = activity?.intent

    // ✅ Obtener datos del Intent (si no hay, usar valores por defecto)
    val productName = intent?.getStringExtra("productName") ?: "Blusa Elegante Negra"
    val productPrice = intent?.getDoubleExtra("productPrice", 89.90) ?: 89.90
    val productDescription = intent?.getStringExtra("productDescription")
        ?: "Blusa elegante de corte moderno, perfecta para ocasiones especiales. Confeccionada en tela de alta calidad con acabados refinados."
    val productImage = intent?.getIntExtra("productImage", R.drawable.modelo_ropa) ?: R.drawable.modelo_ropa
    val productImageUrl = intent?.getStringExtra("productImageUrl")
    val stockTotal = intent?.getIntExtra("stockTotal", -1) ?: -1
    val precioDescuento = intent?.getDoubleExtra("precioDescuento", -1.0) ?: -1.0
    val categoriaNombre = intent?.getStringExtra("categoriaNombre")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(top = 16.dp)
                .padding(paddingValues)
        ) {
            if (!productImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = productImageUrl,
                    contentDescription = productName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = productImage),
                    contentDescription = productName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(productName, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text("(24 reseñas)", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (precioDescuento > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "S/ %.2f".format(precioDescuento),
                        fontSize = 20.sp,
                        color = Color(0xFF0D47A1),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "S/ %.2f".format(productPrice),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            } else {
                Text(
                    text = "S/ %.2f".format(productPrice),
                    fontSize = 20.sp,
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = productDescription,
                fontSize = 15.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Talla", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("S", "M", "L", "XL").forEach { size ->
                    OutlinedButton(
                        onClick = { selectedSize = size },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedSize == size) Color.Black else Color.Transparent,
                            contentColor = if (selectedSize == size) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(70.dp) // 🔹 Botones un poco más anchos
                    ) { Text(size) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Color", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorOption(Color.Black, "Negro", selectedColor) { selectedColor = it }
                ColorOption(Color(0xFF607D8B), "Gris", selectedColor) { selectedColor = it }
                ColorOption(Color(0xFFD1B2FF), "Lila", selectedColor) { selectedColor = it }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Cantidad", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { if (quantity > 1) quantity-- },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = CircleShape
                ) { Text("-", fontSize = 20.sp) }

                Text(text = quantity.toString(), fontSize = 18.sp, fontWeight = FontWeight.Medium)

                Button(
                    onClick = { quantity++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = CircleShape
                ) { Text("+", fontSize = 20.sp) }
            }

            Spacer(modifier = Modifier.height(20.dp))
            if (stockTotal >= 0) {
                Text("$stockTotal en stock", color = Color(0xFF0D47A1), fontSize = 14.sp)
            }

            if (!categoriaNombre.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Categoría: $categoriaNombre", color = Color.DarkGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val item = CartItem(
                        name = productName,
                        price = productPrice,
                        quantity = quantity,
                        size = selectedSize,
                        color = selectedColor,
                        imageRes = productImage
                    )
                    CartManager.addItem(item)
                    CartManager.saveCart(context)

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Producto agregado al carrito 🛒")
                    }

                    val intent = Intent(context, Carrito::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar al carrito", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Productos relacionados", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RelatedProduct(
                    "Vestido Dorado Noche",
                    159.90,
                    "Vestido elegante de noche con detalles dorados brillantes.",
                    R.drawable.vestidodorado
                )
                RelatedProduct(
                    "Casaca Moderna",
                    120.90,
                    "Casaca moderna ideal para el día a día, con estilo urbano y comodidad.",
                    R.drawable.casaca
                )
            }
        }
    }
}

@Composable
fun ColorOption(color: Color, label: String, selected: String, onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .border(
                width = if (selected == label) 3.dp else 1.dp,
                color = if (selected == label) Color.Black else Color.LightGray,
                shape = CircleShape
            )
            .background(color, CircleShape)
            .clickable { onSelect(label) }
    )
}

@Composable
fun RelatedProduct(name: String, price: Double, description: String, imageRes: Int) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F9F9))
            .clickable {
                // 🔹 Al hacer clic, se abre esta misma actividad con nuevos datos
                val intent = Intent(context, ProductDetailActivity::class.java).apply {
                    putExtra("productName", name)
                    putExtra("productPrice", price)
                    putExtra("productDescription", description)
                    putExtra("productImage", imageRes)
                }
                context.startActivity(intent)
            }
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = name,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("S/ %.2f".format(price), color = Color(0xFF0D47A1), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
