package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailActivity : AppCompatActivity() {

    private var producteActual: Producte? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val productId = intent.getIntExtra("PRODUCT_ID", -1)

        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", -1)

        if (productId == -1) { finish(); return }

        // Carregar dades
        lifecycleScope.launch(Dispatchers.IO) {
            producteActual = AppSingleton.getInstance().db.producteDao().getProducteById(productId)

            withContext(Dispatchers.Main) {
                if (producteActual != null) {
                    mostrarDades(producteActual!!)
                }
            }
        }

        findViewById<Button>(R.id.btn_detall_afegir).setOnClickListener {
            producteActual?.let { afegirAlCarro(it) }
        }
    }

    private fun mostrarDades(prod: Producte) {
        val iv = findViewById<ImageView>(R.id.iv_detall_imatge)
        val tvNom = findViewById<TextView>(R.id.tv_detall_nom)
        val tvPreuOrig = findViewById<TextView>(R.id.tv_detall_preu_original)
        val tvPreuOferta = findViewById<TextView>(R.id.tv_detall_preu_oferta)
        val tvDesc = findViewById<TextView>(R.id.tv_detall_desc)

        tvNom.text = prod.nom
        tvDesc.text = prod.descripcio

        // Imatge
        if (!prod.imatgeUri.isNullOrEmpty()) {
            iv.setImageURI(Uri.parse(prod.imatgeUri))
        }

        // --- LÒGICA D'OFERTA ---
        if (prod.esOferta && prod.preuOferta > 0) {
            // CAS OFERTA: Mostrem els dos preus
            tvPreuOrig.text = "${prod.preu} €"
            tvPreuOrig.paintFlags = tvPreuOrig.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Ratllar text

            tvPreuOferta.text = "${prod.preuOferta} €"
            tvPreuOferta.visibility = View.VISIBLE
        } else {
            // CAS NORMAL: Només preu original normal
            tvPreuOrig.text = "${prod.preu} €"
            tvPreuOrig.paintFlags = 0 // Treure ratllat per si de cas
            tvPreuOferta.visibility = View.GONE
        }
    }

    private fun afegirAlCarro(producte: Producte) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppSingleton.getInstance().db.carroDao()

            // 1. Mirem si ja existeix aquest producte al carro de l'usuari
            val itemExistent = dao.getItemSpecific(userId, producte.pid)

            if (itemExistent != null) {
                // Si ja hi és, sumem +1
                itemExistent.quantitat += 1
                dao.updateItem(itemExistent)
            } else {
                // Si no hi és, el creem
                val nouItem = ItemCarro(
                    idUsuari = userId,
                    idProducte = producte.pid,
                    quantitat = 1
                )
                dao.insertItem(nouItem)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProductDetailActivity, "✅ Afegit al carro: ${producte.nom}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}