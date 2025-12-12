package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Adaptadors.ProducteAdapter
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreActivity : AppCompatActivity() {

    private lateinit var rvProductes: RecyclerView
    private var streamerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        // Recuperem l'ID del Streamer passat per Intent
        streamerId = intent.getIntExtra("STREAMER_ID", -1)

        if (streamerId == -1) {
            Toast.makeText(this, "Error: Botiga no trobada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Títol temporal (podries buscar el nom del streamer a la BD també)
        title = "Botiga del Streamer #$streamerId"

        // Configurem RecyclerView en Graella (2 columnes)
        rvProductes = findViewById(R.id.rv_productes_botiga)
        rvProductes.layoutManager = GridLayoutManager(this, 2)

        carregarProductes()
    }

    private fun carregarProductes() {
        lifecycleScope.launch(Dispatchers.IO) {
            // FEM LA CONSULTA AL DAO: Productes d'aquest Creador
            val llista = AppSingleton.getInstance().db.producteDao().getProductesByStreamer(streamerId)

            withContext(Dispatchers.Main) {
                if (llista.isEmpty()) {
                    Toast.makeText(this@StoreActivity, "Aquesta botiga encara està buida!", Toast.LENGTH_LONG).show()
                }

                val adapter = ProducteAdapter(llista, object : ProducteAdapter.OnProducteClickListener {
                    override fun onProducteClick(producte: Producte) {
                        Toast.makeText(this@StoreActivity, "Detalls: ${producte.nom}", Toast.LENGTH_SHORT).show()
                        // Aquí aniria la DetailActivity
                    }

                    override fun onAfegirCarroClick(producte: Producte) {
                        Toast.makeText(this@StoreActivity, "Afegit al carro: ${producte.nom}", Toast.LENGTH_SHORT).show()
                        // Aquí guardaries a la taula ItemCarro
                    }
                })
                rvProductes.adapter = adapter
            }
        }
    }
}