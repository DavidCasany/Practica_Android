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
        fun onEditClick(producte: Producte)   // Clic a la foto/nom -> Editar
        fun onDeleteClick(producte: Producte) // Clic al botó -> Esborrar
    }

    class CreatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImatge: ImageView = itemView.findViewById(R.id.iv_producte_imatge) // FIX ID
        val tvNom: TextView = itemView.findViewById(R.id.tv_producte_nom) // FIX ID
        val tvPreuFinal: TextView = itemView.findViewById(R.id.tv_producte_preu) // FIX ID (Preu final)
        val tvOfertaTag: TextView = itemView.findViewById(R.id.tv_producte_es_oferta) // NOU TAG OFERTA

        // Eliminem la referència al botó i al preu d'oferta que ja no existeixen al layout.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producte, parent, false)
        return CreatorViewHolder(view)
    }


    override fun onBindViewHolder(holder: CreatorViewHolder, position: Int) {
        val prod = llista[position]

        // 1. Dades de Text
        holder.tvNom.text = prod.nom

        // LÒGICA DE PREU: Utilitzem el preu final (oferta o normal)
        val preuMostrar = if (prod.esOferta && prod.preuOferta > 0.0) prod.preuOferta else prod.preu
        holder.tvPreuFinal.text = "${preuMostrar} €"

        // Etiqueta OFERTA (visible/invisible)
        holder.tvOfertaTag.visibility = if (prod.esOferta && prod.preuOferta > 0.0) View.VISIBLE else View.GONE

        // 2. Imatge
        if (!prod.imatgeUri.isNullOrEmpty()) {
            try {
                holder.ivImatge.setImageURI(Uri.parse(prod.imatgeUri))
            } catch (e: Exception) {
                // Codi a prova de crash si la URI és invàlida
                holder.ivImatge.setImageResource(android.R.color.darker_gray)
            }
        } else {
            holder.ivImatge.setImageResource(android.R.color.darker_gray)
        }

        // 3. LISTENERS
        // Clic NORMAL: Obre el formulari d'Edició
        holder.itemView.setOnClickListener {
            listener.onEditClick(prod)
        }

        // Clic LLARG: Obre el diàleg per Esborrar
        holder.itemView.setOnLongClickListener {
            listener.onDeleteClick(prod)
            true // Retornem true per consumir l'esdeveniment i evitar que es propagui
        }
    }

    override fun getItemCount(): Int = llista.size
}