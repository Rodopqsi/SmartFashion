package com.ropa.smartfashionecommerce.catalog

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.home.FavActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.ropa.smartfashionecommerce.BuildConfig
import com.ropa.smartfashionecommerce.ServiceLocator
import com.ropa.smartfashionecommerce.TokenStore

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalog)
    val baseUrl = BuildConfig.BASE_URL
    // Si luego quieres consumir API aquí, usa applicationContext
    val tokenStore = TokenStore(applicationContext)
    val client = ServiceLocator.okHttp(tokenStore, baseUrl)
    val retrofit = ServiceLocator.retrofit(client, baseUrl)
    // val shopApi = ServiceLocator.shopApi(retrofit)
    // Ejemplo: val featured = withContext(Dispatchers.IO) { shopApi.getHome(limit = 12) }.data.featured_products
        val category = intent.getStringExtra("CATEGORY") ?: "Hombres"

        val dummyList = when (category) {
            "Hombres" -> listOf(
                Product("Camisa Hombre", "S/120", R.drawable.hombres),
                Product("Pantalón Hombre", "S/150", R.drawable.hombres),
                Product("Zapatos Hombre", "S/200", R.drawable.hombres),
                Product("Camisa Hombre", "S/120", R.drawable.hombres),
                Product("Camisa Hombre", "S/120", R.drawable.hombres),
                Product("Camisa Hombre", "S/120", R.drawable.hombres)
            )
            "Mujeres" -> listOf(
                Product("Blusa Mujer", "S/130", R.drawable.mujeres),
                Product("Falda Mujer", "S/160", R.drawable.mujeres),
                Product("Tacones Mujer", "S/220", R.drawable.mujeres),
                Product("Camisa Hombre", "S/120", R.drawable.mujeres),
                Product("Camisa Hombre", "S/120", R.drawable.mujeres),
                Product("Camisa Hombre", "S/120", R.drawable.mujeres)
            )
            "Niños" -> listOf(
                Product("Camiseta Niño", "S/80", R.drawable.nino),
                Product("Pantalón Niño", "S/100", R.drawable.nino),
                Product("Zapatillas Niño", "S/120", R.drawable.nino),
                Product("Camisa Hombre", "S/120", R.drawable.nino),
                Product("Camisa Hombre", "S/120", R.drawable.nino),
                Product("Camisa Hombre", "S/120", R.drawable.nino)

            )

            else -> emptyList()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.products_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = ViewHolderAdapter(this, dummyList)

        val filterButton = findViewById<androidx.cardview.widget.CardView>(R.id.filter_card)
        filterButton.setOnClickListener { showFilterBottomSheet() }

        val btnPerfil = findViewById<FloatingActionButton>(R.id.btn_perfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, MiPerfilActivity::class.java))
        }

        val composeView = findViewById<ComposeView>(R.id.bottom_navigation_compose)
        composeView.setContent {
            var selectedTab by remember { mutableStateOf("Catalog") }

            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "Home",
                    onClick = {
                        selectedTab = "Home"
                        startActivity(Intent(this@CatalogActivity, HomeActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Cart",
                    onClick = {
                        selectedTab = "Cart"
                        val intent = Intent(this@CatalogActivity, Carrito::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Favorites",
                    onClick = {
                        selectedTab = "Favorites"
                        startActivity(Intent(this@CatalogActivity, FavActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Profile",
                    onClick = {
                        selectedTab = "Profile"
                        startActivity(Intent(this@CatalogActivity, MiPerfilActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_filter, null)
        dialog.setContentView(view)

        val btnHombres = view.findViewById<MaterialButton>(R.id.btnHombres)
        val btnMujeres = view.findViewById<MaterialButton>(R.id.btnMujeres)
        val btnNinos = view.findViewById<MaterialButton>(R.id.btnNinos)

        btnHombres.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CatalogActivity::class.java)
            intent.putExtra("CATEGORY", "Hombres")
            startActivity(intent)
            finish()
        }
        btnMujeres.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CatalogActivity::class.java)
            intent.putExtra("CATEGORY", "Mujeres")
            startActivity(intent)
            finish()
        }
        btnNinos.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CatalogActivity::class.java)
            intent.putExtra("CATEGORY", "Niños")
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}
