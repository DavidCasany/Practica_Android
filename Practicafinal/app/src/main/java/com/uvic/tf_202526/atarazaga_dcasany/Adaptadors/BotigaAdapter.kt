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
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat



class BotigaAdapter(
    private val llista: List<BotigaDisplay>,
    private val onClick: (BotigaDisplay) -> Unit
) : RecyclerView.Adapter<BotigaAdapter.BotigaViewHolder>() {

    class BotigaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBanner: ImageView = view.findViewById(R.id.iv_card_banner)
        val tvNom: TextView = view.findViewById(R.id.tv_card_streamer_name)
        val tvData: TextView = view.findViewById(R.id.tv_card_last_visit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotigaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_streamer_card, parent, false)
        return BotigaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotigaViewHolder, position: Int) {
        val botiga = llista[position]

        holder.tvNom.text = "Botiga de ${botiga.nomStreamer}"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvData.text = "Última visita: ${sdf.format(Date(botiga.dataVisita))}"

        if (!botiga.bannerUri.isNullOrEmpty()) {
            try {

                holder.ivBanner.setImageURI(Uri.parse(botiga.bannerUri))
            } catch (e: Exception) {

                holder.ivBanner.setImageResource(android.R.color.darker_gray)
            }
        } else {
            holder.ivBanner.setImageResource(android.R.color.darker_gray)
        }

        holder.itemView.setOnClickListener { onClick(botiga) }
    }

    override fun getItemCount(): Int = llista.size
}