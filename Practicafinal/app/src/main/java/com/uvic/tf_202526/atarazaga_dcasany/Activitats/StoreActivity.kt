package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uvic.tf_202526.atarazaga_dcasany.Adaptadors.ProducteAdapter
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreActivity : AppCompatActivity() {

    private lateinit var rvProductes: RecyclerView
    private var streamerId: Int = -1
    private var userId: Int = -1 // <--- NOU

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        // 1. RECOLLIR IDENTIFICADORS

        // ID del Streamer (necessari per carregar els productes)
        streamerId = intent.getIntExtra("STREAMER_ID", -1)

        // ID de l'Espectador (necessari per afegir al carro)
        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", -1)

        if (streamerId == -1 || userId == -1) {
            Toast.makeText(this, "Error: Identificador de botiga o sessió perdut", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. CONFIGURAR LA VISTA
        title = "Botiga del Streamer #$streamerId"

        // RecyclerView en format de graella (2 columnes)
        rvProductes = findViewById(R.id.rv_productes_botiga)
        rvProductes.layoutManager = GridLayoutManager(this, 2)

        // Botó de Carretó
        val btnCart = findViewById<FloatingActionButton>(R.id.fab_view_cart)

        // 3. LÒGICA DEL CARRETÓ
        btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // 4. CARREGAR PRODUCTES (aquesta funció ja la tenies)
        carregarProductes()
    }

    private fun carregarProductes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val llista = AppSingleton.getInstance().db.producteDao().getProductesByStreamer(streamerId)

            withContext(Dispatchers.Main) {
                val adapter = ProducteAdapter(llista, object : ProducteAdapter.OnProducteClickListener {
                    override fun onProducteClick(producte: Producte) {
                        Toast.makeText(this@StoreActivity, "${producte.descripcio}", Toast.LENGTH_SHORT).show()
                    }

                    // --- NOU: LOGICA D'AFEGIR AL CARRO ---
                    override fun onAfegirCarroClick(producte: Producte) {
                        afegirAlCarro(producte)
                    }
                })
                rvProductes.adapter = adapter
            }
        }
    }

    // --- NOVA FUNCIÓ ---
    private fun afegirAlCarro(producte: Producte) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppSingleton.getInstance().db.carroDao()

            // 1. Mirem si ja el tenim
            val itemExistent = dao.getItemSpecific(userId, producte.pid)

            if (itemExistent != null) {
                // Si existeix, sumem +1
                itemExistent.quantitat += 1
                dao.updateItem(itemExistent)
            } else {
                // Si no, el creem
                val nouItem = ItemCarro(
                    idUsuari = userId,
                    idProducte = producte.pid,
                    quantitat = 1
                )
                dao.insertItem(nouItem)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@StoreActivity, "Afegit al carro: ${producte.nom}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}