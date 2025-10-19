package com.ropa.smartfashionecommerce.detalles


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ropa.smartfashionecommerce.R
import android.content.Intent
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity





class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        Toast.makeText(this, "Se cargó activity_details.xml", Toast.LENGTH_LONG).show()

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_details_toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_fav -> {
                    Toast.makeText(this, "Favorito", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        // Imagen (puedes recibir url en intent y cargar con Glide/Picasso)
        val productImage = findViewById<ImageView>(R.id.product_image)
        productImage.setImageResource(R.drawable.modelo_ropa)

        // Sizes
        val rgSizes = findViewById<RadioGroup>(R.id.rg_sizes)
        rgSizes.setOnCheckedChangeListener { _, checkedId ->
            val chosen = when (checkedId) {
                R.id.size_s -> "S"
                R.id.size_m -> "M"
                R.id.size_l -> "L"
                R.id.size_xl -> "XL"
                else -> ""
            }
            Toast.makeText(this, "Tamaño: $chosen", Toast.LENGTH_SHORT).show()
        }

        // Buttons
        val btnBuy = findViewById<Button>(R.id.btn_buy)
        val btnAdd = findViewById<Button>(R.id.btn_add_cart)
        btnBuy.setOnClickListener {
            Toast.makeText(this, "Comprar ahora", Toast.LENGTH_SHORT).show()
            // navegar / checkout
        }
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Añadido al carrito", Toast.LENGTH_SHORT).show()
            // añadir al carrito
        }

        // RecyclerView "More from" (opcional)
        val rvMore = findViewById<RecyclerView>(R.id.rv_more)
        rvMore.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val dummy = listOf(
            Pair("Prenda 1", R.drawable.modelo_ropa),
            Pair("Prenda 2", R.drawable.modelo_ropa),
            Pair("Prenda 3", R.drawable.modelo_ropa)
        )
        rvMore.adapter = MoreAdapter(dummy)

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Abrir HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_cart -> {
                    Toast.makeText(this, "Carrito", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, MiPerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }

    // Adapter simple para la lista horizontal "More from..."
    class MoreAdapter(private val items: List<Pair<String, Int>>) :
        RecyclerView.Adapter<MoreAdapter.VH>() {

        inner class VH(val view: android.view.View) : RecyclerView.ViewHolder(view) {
            val img: ImageView = view.findViewById(R.id.more_img)
            val name: TextView = view.findViewById(R.id.more_name)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_more_product, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val (name, res) = items[position]
            holder.img.setImageResource(res)
            holder.name.text = name
        }

        override fun getItemCount(): Int = items.size
    }
}
