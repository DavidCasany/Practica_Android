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
    private var producteActual: Producte? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        productId = intent.getIntExtra("PRODUCT_ID", -1)
        userId = intent.getIntExtra("USER_ID", -1)

        if (productId == -1 || userId == -1) {
            // TEXT TRADUÏT
            Toast.makeText(this, getString(R.string.error_product_not_found_session), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        carregarDetallProducte()

        val btnAddToCart = findViewById<Button>(R.id.btn_add_to_cart)
        btnAddToCart.setOnClickListener {
            producteActual?.let { afegirAlCarro(it) }
        }
    }

    private fun carregarDetallProducte() {
        lifecycleScope.launch(Dispatchers.IO) {
            producteActual = AppSingleton.getInstance().db.producteDao().getProducteById(productId)

            withContext(Dispatchers.Main) {
                producteActual?.let { producte ->
                    title = producte.nom
                    mostrarDadesProducte(producte)
                } ?: run {
                    // TEXT TRADUÏT
                    Toast.makeText(this@ProductDetailActivity, getString(R.string.msg_product_not_found), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun mostrarDadesProducte(producte: Producte) {
        val ivImage = findViewById<ImageView>(R.id.iv_product_detail_image)
        val tvName = findViewById<TextView>(R.id.tv_product_detail_name)
        val tvPrice = findViewById<TextView>(R.id.tv_product_detail_price)
        val tvOriginalPrice = findViewById<TextView>(R.id.tv_product_detail_price_original)
        val tvDescription = findViewById<TextView>(R.id.tv_product_detail_description)

        val df = DecimalFormat("#,##0.00${getString(R.string.euro_suffix)}")

        if (!producte.imatgeUri.isNullOrEmpty()) {
            try {
                ivImage.setImageURI(Uri.parse(producte.imatgeUri))
            } catch (e: Exception) {
                ivImage.setImageResource(android.R.color.darker_gray)
            }
        } else {
            ivImage.setImageResource(android.R.color.darker_gray)
        }

        tvName.text = producte.nom
        tvDescription.text = producte.descripcio

        if (producte.esOferta && producte.preuOferta > 0.0) {
            tvPrice.text = df.format(producte.preuOferta)
            tvOriginalPrice.visibility = View.VISIBLE
            tvOriginalPrice.text = df.format(producte.preu)
            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvPrice.text = df.format(producte.preu)
            tvOriginalPrice.visibility = View.GONE
        }
    }

    private fun afegirAlCarro(producte: Producte) {
        lifecycleScope.launch(Dispatchers.IO) {
            val itemCarro = ItemCarro(
                idUsuari = userId,
                idProducte = producte.pid,
                quantitat = 1
            )

            val itemExistent = AppSingleton.getInstance().db.carroDao().getItemSpecific(userId, producte.pid)
            val missatgeId: Int

            if (itemExistent != null) {
                AppSingleton.getInstance().db.carroDao().updateQuantitat(itemExistent.id, itemExistent.quantitat + 1)
                missatgeId = R.string.msg_unit_added // TEXT TRADUÏT
            } else {
                AppSingleton.getInstance().db.carroDao().insertItem(itemCarro)
                missatgeId = R.string.msg_product_added // TEXT TRADUÏT
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProductDetailActivity, getString(missatgeId), Toast.LENGTH_SHORT).show()
            }
        }
    }
}