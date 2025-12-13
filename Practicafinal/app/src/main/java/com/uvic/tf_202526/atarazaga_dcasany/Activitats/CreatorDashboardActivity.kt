package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uvic.tf_202526.atarazaga_dcasany.Adaptadors.CreatorProductAdapter
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatorDashboardActivity : AppCompatActivity() {

    private var streamerId: Int = -1
    private lateinit var rvProductes: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_dashboard)

        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        if (streamerId == -1) {
            finish()
            return
        }
        Toast.makeText(this, "Aquest Streamer té la ID: $streamerId", Toast.LENGTH_LONG).show()

        title = "Gestió de Productes"

        // Usem GridLayout (2 columnes) perquè quedi més "botiga"
        rvProductes = findViewById(R.id.rv_els_meus_productes)
        rvProductes.layoutManager = GridLayoutManager(this, 2)

        val btnAdd = findViewById<FloatingActionButton>(R.id.fab_add_product)

        btnAdd.setOnClickListener {
            val intent = Intent(this, ProductFormActivity::class.java)
            intent.putExtra("STREAMER_ID", streamerId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarElsMeusProductes()
    }

    private fun carregarElsMeusProductes() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Consultem a la BD els productes d'aquest Creador
            val llista = AppSingleton.getInstance().db.producteDao().getProductesByStreamer(streamerId)

            withContext(Dispatchers.Main) {
                // 2. Muntem l'adaptador
                val adapter = CreatorProductAdapter(llista, object : CreatorProductAdapter.OnCreatorClickListener {

                    override fun onEditClick(producte: Producte) {
                        // Opcional: Aquí podries obrir el ProductFormActivity passant-li el producte per editar
                        val intent = Intent(this@CreatorDashboardActivity, ProductFormActivity::class.java)
                        intent.putExtra("STREAMER_ID", streamerId) // Li diem qui és l'amo
                        intent.putExtra("PRODUCT_ID", producte.pid) // NOU: Li diem QUIN producte és
                        startActivity(intent)
                        Toast.makeText(this@CreatorDashboardActivity, "Editar: ${producte.nom}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDeleteClick(producte: Producte) {
                        confirmarEsborrar(producte)
                    }
                })
                rvProductes.adapter = adapter
            }
        }
    }

    private fun confirmarEsborrar(producte: Producte) {
        AlertDialog.Builder(this)
            .setTitle("Esborrar Producte")
            .setMessage("Estàs segur que vols eliminar '${producte.nom}'?")
            .setPositiveButton("Sí") { _, _ ->
                esborrarProducte(producte)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun esborrarProducte(producte: Producte) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Eliminem de la BD
            AppSingleton.getInstance().db.producteDao().deleteProducte(producte)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CreatorDashboardActivity, "Eliminat!", Toast.LENGTH_SHORT).show()
                // Refresquem la llista
                carregarElsMeusProductes()
            }
        }
    }
}