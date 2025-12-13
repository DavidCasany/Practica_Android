package com.uvic.tf_202526.atarazaga_dcasany.Adaptadors

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.BotigaDisplay
import com.uvic.tf_202526.atarazaga_dcasany.R

class BotigaAdapter(
    private val llista: List<BotigaDisplay>, // <--- FIXA'T: Ara fem servir la nova classe
    private val onClick: (BotigaDisplay) -> Unit
) : RecyclerView.Adapter<BotigaAdapter.BotigaViewHolder>() {

    class BotigaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBanner: ImageView = view.findViewById(R.id.iv_streamer_banner_thumb)
        val tvNom: TextView = view.findViewById(R.id.tv_streamer_name)
        // Si el teu layout de fila (item_botiga.xml) té una imatge, posa-la aquí.
        // Si fas servir 'simple_list_item_1', no tindràs imatge a la llista, només text.
        // Per fer-ho bé, assumirem que fas servir un layout simple per ara i mostrem el NOM REAL.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotigaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_streamer_row, parent, false)
        return BotigaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotigaViewHolder, position: Int) {
        val botiga = llista[position]

        holder.tvNom.text = "Botiga de ${botiga.nomStreamer}"

        // Lògica per carregar el mini-banner
        if (!botiga.bannerUri.isNullOrEmpty()) {
            holder.ivBanner.setImageURI(Uri.parse(botiga.bannerUri))
        } else {
            holder.ivBanner.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { onClick(botiga) }
    }

    override fun getItemCount(): Int = llista.size
}