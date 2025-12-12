package com.uvic.tf_202526.atarazaga_dcasany.Adaptadors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.CartItemDisplay
import com.uvic.tf_202526.atarazaga_dcasany.R

class CartAdapter(private val llista: List<CartItemDisplay>) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNom: TextView = view.findViewById(android.R.id.text1) // Usem layout per defecte d'Android per anar ràpid
        val tvInfo: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        // Fem servir un layout simple d'Android que té Títol i Subtítol
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = llista[position]
        holder.tvNom.text = item.nomProducte
        holder.tvInfo.text = "${item.quantitat} x ${item.preuUnitari}€ = ${item.preuTotal}€"
    }

    override fun getItemCount(): Int = llista.size
}