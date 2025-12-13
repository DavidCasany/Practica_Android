package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
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
            Toast.makeText(this, "Error: Dades de sessió invàlides", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. CONFIGURAR VISTA
        rvProductes = findViewById(R.id.rv_productes_botiga)
        rvProductes.layoutManager = GridLayoutManager(this, 2)

        val btnCart = findViewById<FloatingActionButton>(R.id.fab_view_cart)
        btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // 3. CARREGAR DADES DEL STREAMER (BANNER I NOM)
        carregarInfoStreamer()

        // 4. CARREGAR PRODUCTES
        carregarProductes()
    }

    // --- NOVA FUNCIÓ: PINTAR LA CAPÇALERA ---
    private fun carregarInfoStreamer() {
        val ivBanner = findViewById<ImageView>(R.id.iv_store_banner)
        val tvNomBotiga = findViewById<TextView>(R.id.tv_store_name)

        lifecycleScope.launch(Dispatchers.IO) {
            // Busquem l'usuari streamer per ID
            val streamer = AppSingleton.getInstance().db.usuariDao().getUsuariById(streamerId)

            withContext(Dispatchers.Main) {
                if (streamer != null) {
                    // Posem el nom real
                    tvNomBotiga.text = "${streamer.nom}"

                    // Posem el banner si en té
                    val bannerUri = streamer.bannerUri
                    if (!bannerUri.isNullOrEmpty()) {
                        try {
                            // Intentem carregar la URI
                            ivBanner.setImageURI(Uri.parse(bannerUri))
                        } catch (e: Exception) {
                            // Si falla, posem el color gris estàndard (el que estaves veient)
                            ivBanner.setImageResource(android.R.color.darker_gray)
                        }
                    } else {
                        // Si el camp és null (no n'ha pujat cap), posem el gris de nou
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
                // Configurem l'adaptador amb els dos listeners (click producte i click afegir)
                val adapter = ProducteAdapter(llista, object : ProducteAdapter.OnProducteClickListener {

                    // A) Clic a la foto -> Anar al Detall
                    override fun onProducteClick(producte: Producte) {
                        val intent = Intent(this@StoreActivity, ProductDetailActivity::class.java)
                        intent.putExtra("PRODUCT_ID", producte.pid)
                        startActivity(intent)
                    }

                    // B) Clic al botó "Afegir" -> Directe al carro
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

            // Comprovem si ja el té per sumar +1 o crear-lo nou
            val itemExistent = dao.getItemSpecific(userId, producte.pid)

            if (itemExistent != null) {
                itemExistent.quantitat += 1
                dao.updateItem(itemExistent)
            } else {
                val nouItem = ItemCarro(
                    idUsuari = userId,
                    idProducte = producte.pid,
                    quantitat = 1
                )
                dao.insertItem(nouItem)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@StoreActivity, "${producte.nom} afegit al carro!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}