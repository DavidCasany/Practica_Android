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

class CreatorProductAdapter(
    private val llista: List<Producte>,
    private val listener: OnCreatorClickListener
) : RecyclerView.Adapter<CreatorProductAdapter.CreatorViewHolder>() {

    interface OnCreatorClickListener {
        fun onEditClick(producte: Producte)
        fun onDeleteClick(producte: Producte)
    }

    class CreatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImatge: ImageView = itemView.findViewById(R.id.iv_producte_imatge)
        val tvNom: TextView = itemView.findViewById(R.id.tv_producte_nom)
        val tvPreuFinal: TextView = itemView.findViewById(R.id.tv_producte_preu)
        val tvOfertaTag: TextView = itemView.findViewById(R.id.tv_producte_es_oferta)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producte, parent, false)
        return CreatorViewHolder(view)
    }


    override fun onBindViewHolder(holder: CreatorViewHolder, position: Int) {
        val prod = llista[position]


        holder.tvNom.text = prod.nom


        val preuMostrar = if (prod.esOferta && prod.preuOferta > 0.0) prod.preuOferta else prod.preu
        holder.tvPreuFinal.text = "${preuMostrar} €"

        holder.tvOfertaTag.visibility = if (prod.esOferta && prod.preuOferta > 0.0) View.VISIBLE else View.GONE


        if (!prod.imatgeUri.isNullOrEmpty()) {
            try {
                holder.ivImatge.setImageURI(Uri.parse(prod.imatgeUri))
            } catch (e: Exception) {

                holder.ivImatge.setImageResource(android.R.color.darker_gray)
            }
        } else {
            holder.ivImatge.setImageResource(android.R.color.darker_gray)
        }


        holder.itemView.setOnClickListener {
            listener.onEditClick(prod)
        }


        holder.itemView.setOnLongClickListener {
            listener.onDeleteClick(prod)
            true
        }
    }

    override fun getItemCount(): Int = llista.size
}