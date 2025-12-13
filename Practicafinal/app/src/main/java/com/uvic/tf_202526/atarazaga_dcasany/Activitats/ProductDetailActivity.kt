package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
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
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class ProductDetailActivity : AppCompatActivity() {

    private var productId: Int = -1
    private var userId: Int = -1

    // Variable per guardar el producte que estem veient
    private var producteActual: Producte? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // 1. Recollir IDs
        productId = intent.getIntExtra("PRODUCT_ID", -1)
        userId = intent.getIntExtra("USER_ID", -1) // Assumim que l'ID de l'usuari s'ha passat

        if (productId == -1 || userId == -1) {
            Toast.makeText(this, "Error: Producte o sessió no trobats", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Carregar dades
        carregarDetallProducte()

        // 3. Listener del botó
        val btnAddToCart = findViewById<Button>(R.id.btn_add_to_cart)
        btnAddToCart.setOnClickListener {
            producteActual?.let { afegirAlCarro(it) }
        }
    }

    private fun carregarDetallProducte() {
        lifecycleScope.launch(Dispatchers.IO) {
            producteActual = AppSingleton.Companion.getInstance().db.producteDao().getProducteById(productId)

            withContext(Dispatchers.Main) {
                producteActual?.let { producte ->
                    title = producte.nom // Títol de la barra
                    mostrarDadesProducte(producte)
                } ?: run {
                    Toast.makeText(this@ProductDetailActivity, "Producte no trobat.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun mostrarDadesProducte(producte: Producte) {
        // Vistes
        val ivImage = findViewById<ImageView>(R.id.iv_product_detail_image)
        val tvName = findViewById<TextView>(R.id.tv_product_detail_name)
        val tvPrice = findViewById<TextView>(R.id.tv_product_detail_price)
        val tvOriginalPrice = findViewById<TextView>(R.id.tv_product_detail_price_original)
        val tvDescription = findViewById<TextView>(R.id.tv_product_detail_description)

        val df = DecimalFormat("#,##0.00€")

        // 1. Imatge (Càrrega segura amb try-catch si és URI)
        if (!producte.imatgeUri.isNullOrEmpty()) {
            try {
                ivImage.setImageURI(Uri.parse(producte.imatgeUri))
            } catch (e: Exception) {
                ivImage.setImageResource(android.R.color.darker_gray)
            }
        } else {
            ivImage.setImageResource(android.R.color.darker_gray)
        }

        // 2. Textos
        tvName.text = producte.nom
        tvDescription.text = producte.descripcio

        // 3. Lògica de Preus (Oferta vs. Normal)
        if (producte.esOferta && producte.preuOferta > 0.0) {
            // Hi ha oferta
            tvPrice.text = df.format(producte.preuOferta) // Preu Final (Oferta)

            // Mostrem i barrem el preu original
            tvOriginalPrice.visibility = View.VISIBLE
            tvOriginalPrice.text = df.format(producte.preu)
            // Afegim l'efecte de barrat (strikethrough) al preu original
            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        } else {
            // Preu Normal
            tvPrice.text = df.format(producte.preu)
            tvOriginalPrice.visibility = View.GONE
        }
    }

    private fun afegirAlCarro(producte: Producte) {
        lifecycleScope.launch(Dispatchers.IO) {
            val itemCarro = ItemCarro(
                idUsuari = userId,
                idProducte = producte.pid,
                quantitat = 1 // Per simplicitat, afegim només 1
            )

            // Comprovar si el producte ja hi és per augmentar la quantitat o crear-ne un de nou
            val itemExistent = AppSingleton.Companion.getInstance().db.carroDao().getItemByProducte(userId, producte.pid)

            val missatge: String

            if (itemExistent != null) {
                // Si ja existeix, incrementem
                AppSingleton.Companion.getInstance().db.carroDao().updateQuantitat(itemExistent.id, itemExistent.quantitat + 1)
                missatge = "Unitat afegida al carretó!"
            } else {
                // Si no existeix, el creem
                AppSingleton.Companion.getInstance().db.carroDao().addItem(itemCarro)
                missatge = "Producte afegit al carretó!"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProductDetailActivity, missatge, Toast.LENGTH_SHORT).show()
            }
        }
    }
}