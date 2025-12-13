package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager // Importa l'estil de graella
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

    // FIX 1: Declaració correcta dels membres de classe
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
            Toast.makeText(this, "Error: Dades de sessió invàlides", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. CONFIGURAR VISTA
        // FIX 2: Inicialització correcta amb els nous IDs del layout
        ivBanner = findViewById(R.id.iv_store_banner)
        rvProductes = findViewById(R.id.rv_store_products)

        // Utilitzem GridLayoutManager per a l'estil de targeta professional
        rvProductes.layoutManager = GridLayoutManager(this, 2)

        // FIX 3: Utilitzem l'únic FAB que existeix al nou layout (fab_go_to_cart)
        val fabCart = findViewById<FloatingActionButton>(R.id.fab_go_to_cart)
        fabCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // 3. CARREGAR DADES DEL STREAMER (BANNER I NOM)
        carregarInfoStreamer()

        // 4. CARREGAR PRODUCTES
        carregarProductes()
    }

    // --- FUNCIÓ: PINTAR LA CAPÇALERA ---
    private fun carregarInfoStreamer() {
        // ivBanner ja és membre de classe i s'ha inicialitzat a onCreate

        lifecycleScope.launch(Dispatchers.IO) {
            // Busquem l'usuari streamer per ID
            val streamer = AppSingleton.getInstance().db.usuariDao().getUsuariById(streamerId)

            withContext(Dispatchers.Main) {
                if (streamer != null) {
                    // FIX 4: Posem el nom a la barra d'acció (més net i professional)
                    title = "Botiga de ${streamer.nom}"

                    // Posem el banner si en té
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
                // Configurem l'adaptador amb els dos listeners
                val adapter = ProducteAdapter(llista, object : ProducteAdapter.OnProducteClickListener {

                    // A) Clic a la targeta -> Anar al Detall
                    override fun onProducteClick(producte: Producte) {
                        val intent = Intent(this@StoreActivity, ProductDetailActivity::class.java)
                        intent.putExtra("PRODUCT_ID", producte.pid)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }

                    // B) Clic al botó "Afegir" (Aquesta funció ha de ser cridada des de dins de l'Adapter)
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
                // Actualitzem la quantitat
                dao.updateQuantitat(itemExistent.id, itemExistent.quantitat + 1)
            } else {
                val nouItem = ItemCarro(
                    idUsuari = userId,
                    idProducte = producte.pid,
                    quantitat = 1
                )
                // Inserim el nou ítem
                dao.insertItem(nouItem)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@StoreActivity, "${producte.nom} afegit al carro!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}