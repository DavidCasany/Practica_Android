package com.uvic.tf_202526.atarazaga_dcasany.Adaptadors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.BotigaVisitada
import com.uvic.tf_202526.atarazaga_dcasany.R

class BotigaAdapter(
    private val llista: List<BotigaVisitada>,
    private val listener: OnItemClickListener // Aquí demanem algú que implementi la interfície
) : RecyclerView.Adapter<BotigaAdapter.BotigaViewHolder>() {

    // --- AQUESTA ÉS LA INTERFÍCIE QUE DÓNA ERROR ---
    // Ha d'estar definida DINS de la classe BotigaAdapter
    interface OnItemClickListener {
        fun onItemClick(botiga: BotigaVisitada)
    }
    // ----------------------------------------------

    class BotigaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNom: TextView = itemView.findViewById(R.id.tv_nom_botiga)
        val tvData: TextView = itemView.findViewById(R.id.tv_data_visita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotigaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_botiga, parent, false)
        return BotigaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotigaViewHolder, position: Int) {
        val item = llista[position]

        holder.tvNom.text = "Botiga #${item.idStreamer}"
        holder.tvData.text = "ID Visita: ${item.id}"

        // Aquí cridem al mètode de la interfície quan es fa clic
        holder.itemView.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = llista.size
}