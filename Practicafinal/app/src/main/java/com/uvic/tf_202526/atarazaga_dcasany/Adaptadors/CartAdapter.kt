package com.uvic.tf_202526.atarazaga_dcasany.Adaptadors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.CartItemDisplay
import com.uvic.tf_202526.atarazaga_dcasany.R

class CartAdapter(
    private val llista: List<CartItemDisplay>,
    private val listener: OnCartActionListener // Nova interfície
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    interface OnCartActionListener {
        fun onMinusClick(item: CartItemDisplay)
        fun onDeleteClick(item: CartItemDisplay)
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNom: TextView = view.findViewById(R.id.tv_cart_nom)
        val tvInfo: TextView = view.findViewById(R.id.tv_cart_info)
        val btnMinus: ImageButton = view.findViewById(R.id.btn_cart_minus)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_cart_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = llista[position]

        holder.tvNom.text = item.nomProducte


        val textPreu = "${item.quantitat} x ${item.preuUnitariFinal}€ = ${item.preuTotal}€"


        if (item.esOferta && item.preuOferta > 0) {
            holder.tvInfo.text = "$textPreu (OFERTA!)"
            holder.tvInfo.setTextColor(android.graphics.Color.RED)
        } else {
            holder.tvInfo.text = textPreu
            holder.tvInfo.setTextColor(android.graphics.Color.GRAY)
        }

        holder.btnMinus.setOnClickListener { listener.onMinusClick(item) }
        holder.btnDelete.setOnClickListener { listener.onDeleteClick(item) }
    }

    override fun getItemCount(): Int = llista.size
}