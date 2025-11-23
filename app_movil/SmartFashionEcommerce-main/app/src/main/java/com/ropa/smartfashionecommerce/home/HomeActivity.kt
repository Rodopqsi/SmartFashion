package com.ropa.smartfashionecommerce.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.ropa.smartfashionecommerce.BuildConfig
import com.ropa.smartfashionecommerce.CategoryDto
import com.ropa.smartfashionecommerce.ColorDto
import com.ropa.smartfashionecommerce.ProductCardDto
import com.ropa.smartfashionecommerce.ServiceLocator
import com.ropa.smartfashionecommerce.SizeDto
import com.ropa.smartfashionecommerce.TokenStore
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartItem
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.catalog.CatalogActivity
import com.ropa.smartfashionecommerce.detalles.ProductDetailActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// MODELO DE PRODUCTO
data class Product(val name: String, val price: String, val image: Int)

// LISTA DE PRODUCTOS
val productList = listOf(
    Product("Blusa Elegante Negra", "S/ 89.90", R.drawable.blusaelegante),
    Product("Vestido Dorado Noche", "S/ 159.90", R.drawable.vestidodorado),
    Product("Casaca Moderna", "S/ 120.00", R.drawable.casaca),
    Product("Pantalón Beige", "S/ 110.00", R.drawable.pantalonbeige),
    Product("Camisa Blanca", "S/ 95.00", R.drawable.camisablanca),
    Product("Vestido Floral", "S/ 150.00", R.drawable.vestidofloral)
)

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CartManager.initialize(this) // ✅ Inicializamos el carrito aquí
        setContent {
            SmartFashionEcommerceTheme {
                FashionHomeScreen(activity = this)
            }
        }
    }
}

@Composable
fun FashionHomeScreen(activity: ComponentActivity) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Home") }
    val pageScroll = rememberScrollState()

    // API wiring (Retrofit/OkHttp) — uses emulator base URL in Debug
    val baseUrl = remember { BuildConfig.BASE_URL }
    val tokenStore = remember { TokenStore(context.applicationContext) }
    val okHttp = remember { ServiceLocator.okHttp(tokenStore, baseUrl) }
    val retrofit = remember { ServiceLocator.retrofit(okHttp, baseUrl) }
    val shopApi = remember { ServiceLocator.shopApi(retrofit) }

    // Remote state for products
    var featured by remember { mutableStateOf<List<ProductCardDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var sizes by remember { mutableStateOf<List<SizeDto>>(emptyList()) }
    var colors by remember { mutableStateOf<List<ColorDto>>(emptyList()) }

    // Filters/search state
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSizeId by remember { mutableStateOf<Int?>(null) }
    var selectedColorId by remember { mutableStateOf<Int?>(null) }
    var searchText by remember { mutableStateOf("") }
    var appliedQuery by remember { mutableStateOf<String?>(null) }

    // Initial load: products + sizes/colors (categories come in home response)
    LaunchedEffect(Unit) {
        try {
            val home = withContext(Dispatchers.IO) { shopApi.getHome(limit = 12) }
            featured = home.data.featured_products
            categories = home.data.categories
            val sizesResp = withContext(Dispatchers.IO) { shopApi.getSizes() }
            val colorsResp = withContext(Dispatchers.IO) { shopApi.getColors() }
            sizes = sizesResp.data
            colors = colorsResp.data
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    // Reload when filters/search change
    LaunchedEffect(selectedCategoryId, selectedSizeId, selectedColorId, appliedQuery) {
        try {
            // Evita que aparezca la lista estática durante cargas
            error = null
            isLoading = true
            val home = withContext(Dispatchers.IO) {
                shopApi.getHome(
                    categoryId = selectedCategoryId,
                    q = appliedQuery,
                    size = selectedSizeId,
                    color = selectedColorId,
                    limit = 12
                )
            }
            featured = home.data.featured_products
            categories = home.data.categories
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                NavigationBarItem(
                    selected = selectedTab == "Home",
                    onClick = { selectedTab = "Home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF212121)) },
                    label = { Text("Home", color = Color(0xFF212121)) }
                )

                NavigationBarItem(
                    selected = selectedTab == "Cart",
                    onClick = {
                        selectedTab = "Cart"
                        val intent = Intent(activity, Carrito::class.java)
                        activity.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Color(0xFF212121)) },
                    label = { Text("Carrito", color = Color(0xFF212121)) }
                )

                NavigationBarItem(
                    selected = selectedTab == "Favorites",
                    onClick = {
                        selectedTab = "Favorites"
                        val intent = Intent(activity, FavActivity::class.java)
                        activity.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos", tint = Color(0xFF212121)) },
                    label = { Text("Favoritos", color = Color(0xFF212121)) }
                )

                NavigationBarItem(
                    selected = selectedTab == "Profile",
                    onClick = {
                        selectedTab = "Profile"
                        val intent = Intent(activity, MiPerfilActivity::class.java)
                        activity.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color(0xFF212121)) },
                    label = { Text("Perfil", color = Color(0xFF212121)) }
                )
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Search bar (full span)
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = { Text("Buscar productos...") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { appliedQuery = searchText.ifBlank { null } }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                )
            }

            // Dynamic filters: Categories
            if (categories.isNotEmpty()) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Column {
                        Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                        LazyRow(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                            items(categories) { cat ->
                                val selected = selectedCategoryId == cat.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategoryId = if (selected) null else cat.id },
                                    label = { Text(cat.nombre) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic filters: Sizes
            if (sizes.isNotEmpty()) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Column {
                        Text("Tallas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp, bottom = 6.dp))
                        LazyRow(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                            items(sizes) { s ->
                                val selected = selectedSizeId == s.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedSizeId = if (selected) null else s.id },
                                    label = { Text(s.nombre) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic filters: Colors
            if (colors.isNotEmpty()) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Column {
                        Text("Colores", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp, bottom = 6.dp))
                        LazyRow(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                            items(colors) { c ->
                                val selected = selectedColorId == c.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedColorId = if (selected) null else c.id },
                                    label = { Text(c.nombre) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Clear filters button
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                    TextButton(onClick = {
                        selectedCategoryId = null
                        selectedSizeId = null
                        selectedColorId = null
                        appliedQuery = null
                        searchText = ""
                    }) { Text("Limpiar filtros") }
                }
            }

            // Banner (full span)
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.imagendehomeprincipal),
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "SMART FASHION",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Static categories row (preserved)
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                    val categoriesStatic = listOf("ZARA", "VOGUE", "CHANEL", "RALPH")
                    LazyRow(modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)) {
                        items(categoriesStatic) { category ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE0E0E0))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable {
                                        if (category == "ZARA") {
                                            activity.startActivity(Intent(activity, CatalogActivity::class.java))
                                        }
                                    }
                            ) {
                                Text(category, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121))
                            }
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (error == null) {
                // Header for DB catalog
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Text("Catálogo (DB) - nose", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                }
                if (featured.isEmpty() && !isLoading) {
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Text("Sin resultados para los filtros/búsqueda.", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                    }
                }
                items(featured) { product ->
                    ProductCardRemote(product)
                }
            } else {
                // Error fallback: static demo list
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Text("Productos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                }
                items(productList) { product ->
                    ProductCard(product)
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = product.image),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showMenu = true }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121))
            Text(product.price, color = Color(0xFF424242), fontSize = 13.sp)
        }

        if (showMenu) {
            Popup(alignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MenuItem(Icons.Default.Visibility, "Ver detalles") {
                            showMenu = false
                            val description = when (product.name) {
                                "Blusa Elegante Negra" -> "Blusa elegante de corte moderno, perfecta para ocasiones especiales."
                                "Vestido Dorado Noche" -> "Vestido de gala color dorado, ideal para eventos de noche con brillo y glamour."
                                "Casaca Moderna" -> "Casaca moderna de estilo urbano, perfecta para outfits casuales."
                                "Pantalón Beige" -> "Pantalón beige elegante y cómodo, ideal para un look casual o formal."
                                "Camisa Blanca" -> "Camisa blanca clásica, básica y versátil para cualquier ocasión."
                                "Vestido Floral" -> "Vestido floral fresco y colorido, ideal para días soleados."
                                else -> "Producto de moda de alta calidad disponible en nuestra tienda."
                            }

                            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                                putExtra("productName", product.name)
                                putExtra("productPrice", product.price.replace("S/", "").trim().toDouble())
                                putExtra("productDescription", description)
                                putExtra("productImage", product.image)
                            }
                            context.startActivity(intent)
                        }

                        // ✅ Agregar al carrito funcionando completamente
                        MenuItem(Icons.Default.ShoppingCart, "Agregar al carrito") {
                            showMenu = false
                            val item = CartItem(
                                name = product.name,
                                size = "M",
                                color = "Negro",
                                quantity = 1,
                                price = product.price.replace("S/", "").trim().toDoubleOrNull() ?: 0.0,
                                imageRes = product.image
                            )
                            CartManager.addItem(item)
                            CartManager.saveCart(context) // 🔥 Guarda el carrito
                            Toast.makeText(context, "Agregado al carrito 🛍️", Toast.LENGTH_SHORT).show()
                        }

                        MenuItem(Icons.Default.Favorite, "Agregar a favoritos") {
                            showMenu = false
                            val favoriteItem = FavoriteItem(
                                id = product.name.hashCode(),
                                name = product.name,
                                price = product.price,
                                sizes = listOf("S", "M", "L"),
                                imageRes = product.image
                            )
                            FavoritesManager.addFavorite(favoriteItem)
                            Toast.makeText(context, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show()
                        }

                        MenuItem(Icons.Default.Share, "Compartir producto") {
                            showMenu = false
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${product.name} - ${product.price}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir con"))
                        }

                        MenuItem(Icons.AutoMirrored.Filled.Chat, "Consultar por WhatsApp") {
                            showMenu = false
                            val url = "https://wa.me/?text=${product.name} - ${product.price}"
                            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
                            context.startActivity(whatsappIntent)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { showMenu = false }) {
                            Text("Cerrar", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardRemote(product: ProductCardDto) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        Column {
            AsyncImage(
                model = product.image_preview,
                contentDescription = product.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showMenu = true }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(product.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121), maxLines = 1)
            Text("S/ ${"%.2f".format(product.precio)}", color = Color(0xFF424242), fontSize = 13.sp)
        }

        if (showMenu) {
            Popup(alignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MenuItem(Icons.Default.Visibility, "Ver detalles") {
                            showMenu = false
                            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                                putExtra("productName", product.nombre)
                                putExtra("productPrice", product.precio)
                                putExtra("productDescription", product.descripcion ?: "Producto de moda disponible en nuestra tienda.")
                                putExtra("productImage", 0) // No tenemos res local; la pantalla de detalle debería adaptarse para URL
                                putExtra("productImageUrl", product.image_preview ?: "")
                                putExtra("stockTotal", product.stock_total)
                                putExtra("precioDescuento", product.precio_descuento ?: -1.0)
                                putExtra("categoriaNombre", product.categoria?.nombre ?: "")
                            }
                            context.startActivity(intent)
                        }

                        MenuItem(Icons.Default.ShoppingCart, "Agregar al carrito") {
                            showMenu = false
                            val item = CartItem(
                                name = product.nombre,
                                size = "M",
                                color = "Negro",
                                quantity = 1,
                                price = product.precio,
                                imageRes = 0
                            )
                            CartManager.addItem(item)
                            CartManager.saveCart(context)
                            Toast.makeText(context, "Agregado al carrito 🛍️", Toast.LENGTH_SHORT).show()
                        }

                        MenuItem(Icons.Default.Favorite, "Agregar a favoritos") {
                            showMenu = false
                            val favoriteItem = FavoriteItem(
                                id = product.id,
                                name = product.nombre,
                                price = "S/ ${"%.2f".format(product.precio)}",
                                sizes = listOf("S", "M", "L"),
                                imageRes = 0
                            )
                            FavoritesManager.addFavorite(favoriteItem)
                            Toast.makeText(context, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show()
                        }

                        MenuItem(Icons.Default.Share, "Compartir producto") {
                            showMenu = false
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${product.nombre} - S/ ${"%.2f".format(product.precio)}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir con"))
                        }

                        MenuItem(Icons.AutoMirrored.Filled.Chat, "Consultar por WhatsApp") {
                            showMenu = false
                            val url = "https://wa.me/?text=${product.nombre} - S/ ${"%.2f".format(product.precio)}"
                            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
                            context.startActivity(whatsappIntent)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { showMenu = false }) {
                            Text("Cerrar", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = Color(0xFF212121), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color(0xFF212121), fontSize = 15.sp)
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomePreview() {
    SmartFashionEcommerceTheme {
        FashionHomeScreen(activity = ComponentActivity())
    }
}
