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
        val ivImatge: ImageView = itemView.findViewById(R.id.iv_producte)
        val tvNom: TextView = itemView.findViewById(R.id.tv_nom_prod)
        val tvPreu: TextView = itemView.findViewById(R.id.tv_preu_prod)
        val btnAccio: Button = itemView.findViewById(R.id.btn_afegir_carro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producte, parent, false)
        return CreatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: CreatorViewHolder, position: Int) {
        val prod = llista[position]

        holder.tvNom.text = prod.nom
        holder.tvPreu.text = "${prod.preu} €"

        // Carreguem la imatge
        if (!prod.imatgeUri.isNullOrEmpty()) {
            try {
                holder.ivImatge.setImageURI(Uri.parse(prod.imatgeUri))
            } catch (e: Exception) {
                holder.ivImatge.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        // --- CANVI IMPORTANT ---
        // Com que som creadors, el botó serà VERMELL i dirà ESBORRAR
        holder.btnAccio.text = "ESBORRAR"
        holder.btnAccio.setBackgroundColor(0xFFFF0000.toInt()) // Vermell (o fes servir colors.xml)

        // Clic al botó -> Esborrar
        holder.btnAccio.setOnClickListener {
            listener.onDeleteClick(prod)
        }

        // Clic a la resta -> Editar (obrir formulari)
        holder.itemView.setOnClickListener {
            listener.onEditClick(prod)
        }
    }

    override fun getItemCount(): Int = llista.size
}