package com.uvic.tf_202526.atarazaga_dcasany.Adaptadors

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import java.io.File

class ProducteAdapter(
    private val llista: List<Producte>,
    private val listener: OnProducteClickListener
) : RecyclerView.Adapter<ProducteAdapter.ProducteViewHolder>() {

    interface OnProducteClickListener {
        fun onProducteClick(producte: Producte)
        fun onAfegirCarroClick(producte: Producte)
    }

    class ProducteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImatge: ImageView = itemView.findViewById(R.id.iv_producte)
        val tvNom: TextView = itemView.findViewById(R.id.tv_nom_prod)
        val tvPreu: TextView = itemView.findViewById(R.id.tv_preu_prod)
        val btnAfegir: Button = itemView.findViewById(R.id.btn_afegir_carro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProducteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producte, parent, false)
        return ProducteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProducteViewHolder, position: Int) {
        val prod = llista[position]

        holder.tvNom.text = prod.nom
        holder.tvPreu.text = "${prod.preu} €"

        // Carregar imatge si existeix
        if (!prod.imatgeUri.isNullOrEmpty()) {
            try {
                holder.ivImatge.setImageURI(Uri.parse(prod.imatgeUri))
            } catch (e: Exception) {
                holder.ivImatge.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        holder.itemView.setOnClickListener { listener.onProducteClick(prod) }
        holder.btnAfegir.setOnClickListener { listener.onAfegirCarroClick(prod) }
    }

    override fun getItemCount(): Int = llista.size
}