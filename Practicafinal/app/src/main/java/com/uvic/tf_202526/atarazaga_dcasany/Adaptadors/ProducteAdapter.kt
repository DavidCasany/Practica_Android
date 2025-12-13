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
        val tvPreuOferta: TextView = itemView.findViewById(R.id.tv_preu_oferta_item) // NOU
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

        // --- LÒGICA D'OFERTA ---
        if (prod.esOferta && prod.preuOferta > 0) {
            // Està d'oferta: Ratllem el preu vell i mostrem el nou
            holder.tvPreu.paintFlags = holder.tvPreu.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvPreu.setTextColor(android.graphics.Color.GRAY) // Gris per al vell

            holder.tvPreuOferta.text = "${prod.preuOferta} €"
            holder.tvPreuOferta.visibility = View.VISIBLE
        } else {
            // Normal: Restaurem l'estat original (importantíssim pel reciclatge de vistes)
            holder.tvPreu.paintFlags = 0
            holder.tvPreu.setTextColor(android.graphics.Color.parseColor("#388E3C")) // Verd original
            holder.tvPreuOferta.visibility = View.GONE
        }
        // -----------------------

        if (!prod.imatgeUri.isNullOrEmpty()) {
            holder.ivImatge.setImageURI(Uri.parse(prod.imatgeUri))
        } else {
            holder.ivImatge.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { listener.onProducteClick(prod) }
        holder.btnAfegir.setOnClickListener { listener.onAfegirCarroClick(prod) }
    }

    override fun getItemCount(): Int = llista.size
}