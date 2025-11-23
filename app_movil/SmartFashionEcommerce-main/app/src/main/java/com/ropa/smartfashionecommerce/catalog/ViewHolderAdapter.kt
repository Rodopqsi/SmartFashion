package com.ropa.smartfashionecommerce.catalog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.detalles.DetailsActivity

class ViewHolderAdapter(
    private val context: Context,
    private val productList: List<Product>
) : RecyclerView.Adapter<ViewHolderAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_producto)
        val name: TextView = itemView.findViewById(R.id.name_producto)
        val price: TextView = itemView.findViewById(R.id.product_price)
        val btnDetails: Button = itemView.findViewById(R.id.btn_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.image.setImageResource(product.imageRes)
        holder.name.text = product.name
        holder.price.text = product.price

        // Click en botón para abrir DetailsActivity
        holder.btnDetails.setOnClickListener {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra("name", product.name)
            intent.putExtra("price", product.price)
            intent.putExtra("imageRes", product.imageRes)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size
}
