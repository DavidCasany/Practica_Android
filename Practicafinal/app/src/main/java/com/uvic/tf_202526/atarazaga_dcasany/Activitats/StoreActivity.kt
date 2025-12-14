package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uvic.tf_202526.atarazaga_dcasany.Adaptadors.ProducteAdapter
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreActivity : AppCompatActivity() {

    private lateinit var rvProductes: RecyclerView
    private lateinit var ivBanner: ImageView

    private var streamerId: Int = -1
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        // 1. RECOLLIR IDENTIFICADORS
        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", -1)

        if (streamerId == -1 || userId == -1) {
            // TEXT TRADUÏT
            Toast.makeText(this, getString(R.string.error_invalid_session_data), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. CONFIGURAR VISTA
        ivBanner = findViewById(R.id.iv_store_banner)
        rvProductes = findViewById(R.id.rv_store_products)
        rvProductes.layoutManager = GridLayoutManager(this, 2)

        val fabCart = findViewById<FloatingActionButton>(R.id.fab_go_to_cart)
        fabCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // 3. CARREGAR DADES
        carregarInfoStreamer()
        carregarProductes()
    }

    private fun carregarInfoStreamer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val streamer = AppSingleton.getInstance().db.usuariDao().getUsuariById(streamerId)

            withContext(Dispatchers.Main) {
                if (streamer != null) {
                    // TEXT TRADUÏT amb format
                    title = getString(R.string.shop_title_format, streamer.nom)

                    val bannerUri = streamer.bannerUri
                    if (!bannerUri.isNullOrEmpty()) {
                        try {
                            ivBanner.setImageURI(Uri.parse(bannerUri))
                        } catch (e: Exception) {
                            ivBanner.setImageResource(android.R.color.darker_gray)
                        }
                    } else {
                        ivBanner.setImageResource(android.R.color.darker_gray)
                    }
                }
            }
        }
    }

    private fun carregarProductes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val llista = AppSingleton.getInstance().db.producteDao().getProductesByStreamer(streamerId)

            withContext(Dispatchers.Main) {
                val adapter = ProducteAdapter(llista, object : ProducteAdapter.OnProducteClickListener {
                    override fun onProducteClick(producte: Producte) {
                        val intent = Intent(this@StoreActivity, ProductDetailActivity::class.java)
                        intent.putExtra("PRODUCT_ID", producte.pid)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }

                    override fun onAfegirCarroClick(producte: Producte) {
                        afegirAlCarro(producte)
                    }
                })
                rvProductes.adapter = adapter
            }
        }
    }

    private fun afegirAlCarro(producte: Producte) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppSingleton.getInstance().db.carroDao()
            val itemExistent = dao.getItemSpecific(userId, producte.pid)

            if (itemExistent != null) {
                dao.updateQuantitat(itemExistent.id, itemExistent.quantitat + 1)
            } else {
                val nouItem = ItemCarro(
                    idUsuari = userId,
                    idProducte = producte.pid,
                    quantitat = 1
                )
                dao.insertItem(nouItem)
            }

            withContext(Dispatchers.Main) {
                // TEXT TRADUÏT amb variable
                Toast.makeText(this@StoreActivity, getString(R.string.msg_added_to_cart_format, producte.nom), Toast.LENGTH_SHORT).show()
            }
        }
    }
}