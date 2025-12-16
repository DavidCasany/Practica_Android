package com.uvic.tf_202526.atarazaga_dcasany.Adaptadors

import android.R.attr.onClick
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
import java.text.DecimalFormat
import java.io.File

class ProducteAdapter(
    private val llista: List<Producte>,
    private val listener: OnProducteClickListener
) : RecyclerView.Adapter<ProducteAdapter.ProducteViewHolder>() {

    interface OnProducteClickListener {
        fun onProducteClick(producte: Producte)
        fun onAfegirCarroClick(producte: Producte)
    }

    class ProducteViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivImage: ImageView = view.findViewById(R.id.iv_producte_imatge)
        val tvNom: TextView = view.findViewById(R.id.tv_producte_nom)
        val tvPreu: TextView = view.findViewById(R.id.tv_producte_preu)
        val tvOferta: TextView = view.findViewById(R.id.tv_producte_es_oferta) // NOU
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProducteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producte, parent, false)
        return ProducteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProducteViewHolder, position: Int) {
        val producte = llista[position]
        val df = DecimalFormat("#,##0.00€")

        holder.tvNom.text = producte.nom


        if (producte.esOferta && producte.preuOferta > 0.0) {
            holder.tvPreu.text = df.format(producte.preuOferta)
            holder.tvOferta.visibility = View.VISIBLE
        } else {
            holder.tvPreu.text = df.format(producte.preu)
            holder.tvOferta.visibility = View.GONE
        }


        if (!producte.imatgeUri.isNullOrEmpty()) {
            try {
                holder.ivImage.setImageURI(Uri.parse(producte.imatgeUri))
            } catch (e: Exception) {
                holder.ivImage.setImageResource(android.R.color.darker_gray)
            }
        } else {
            holder.ivImage.setImageResource(android.R.color.darker_gray)
        }

        holder.itemView.setOnClickListener { listener.onProducteClick(producte) }
    }

    override fun getItemCount(): Int = llista.size
}