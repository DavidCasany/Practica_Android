package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
    private lateinit var ivBanner: ImageView

    // Launcher de Galeria AMB PERSISTÈNCIA DE PERMISOS
    private val bannerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ivBanner.setImageURI(uri)

            // CORRECCIÓ CLAU: Fem persistent l'URI
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, flags)
            } catch (e: Exception) {
                Toast.makeText(this, "Permisos d'imatge no guardats permanentment.", Toast.LENGTH_LONG).show()
            }

            guardarBannerBD(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_dashboard)

        // --- 1. RECERCA DE LA ID (Millorada per evitar crashes) ---
        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        if (streamerId == -1) {
            val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
            // Utilitzem l'ID genèrica guardada al login
            streamerId = prefs.getInt("USER_ID", -1)
        }

        if (streamerId == -1) {
            Toast.makeText(this, "Error: No s'ha pogut carregar l'usuari Streamer. Sessió invàlida.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Toast.makeText(this, "Dashboard carregat. ID: $streamerId", Toast.LENGTH_SHORT).show()

        // --- 2. INICIALITZACIÓ DE VISTES ---
        rvProductes = findViewById(R.id.rv_els_meus_productes)
        rvProductes.layoutManager = GridLayoutManager(this, 2)
        ivBanner = findViewById(R.id.iv_dashboard_banner)

        val btnAdd = findViewById<FloatingActionButton>(R.id.fab_add_product)

        // --- 3. LISTENERS ---
        ivBanner.setOnClickListener {
            bannerLauncher.launch("image/*")
        }

        btnAdd.setOnClickListener {
            val intent = Intent(this, ProductFormActivity::class.java)
            intent.putExtra("STREAMER_ID", streamerId)
            startActivity(intent)
        }

        // --- 4. CÀRREGA INICIAL DE DADES ---
        carregarDadesUsuari() // Càrrega el nom i el banner
        carregarElsMeusProductes()
    }

    private fun carregarDadesUsuari() {
        lifecycleScope.launch(Dispatchers.IO) {
            val usuari = AppSingleton.getInstance().db.usuariDao().getUsuariById(streamerId)
            withContext(Dispatchers.Main) {
                if (usuari != null) {
                    // Posem el nom del streamer com a títol
                    title = "Botiga de ${usuari.nom}"

                    val bannerUriString = usuari.bannerUri
                    if (!bannerUriString.isNullOrEmpty()) {
                        // CORRECCIÓ: Protegim la càrrega de la imatge
                        try {
                            ivBanner.setImageURI(Uri.parse(bannerUriString))
                        } catch (e: Exception) {
                            // Si peta la URI (permissos trencats), mostrem el placeholder.
                            ivBanner.setImageResource(android.R.color.darker_gray)
                        }
                    } else {
                        ivBanner.setImageResource(android.R.color.darker_gray)
                    }
                }
            }
        }
    }

    private fun guardarBannerBD(uri: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppSingleton.getInstance().db.usuariDao().updateBanner(streamerId, uri)
        }
    }

    override fun onResume() {
        super.onResume()
        if (streamerId != -1) {
            carregarElsMeusProductes()
        }
    }

    private fun carregarElsMeusProductes() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Consultem a la BD els productes d'aquest Creador
            val llista = AppSingleton.getInstance().db.producteDao().getProductesByStreamer(streamerId)

            withContext(Dispatchers.Main) {
                // 2. Muntem l'adaptador
                val adapter = CreatorProductAdapter(llista, object : CreatorProductAdapter.OnCreatorClickListener {

                    override fun onEditClick(producte: Producte) {
                        val intent = Intent(this@CreatorDashboardActivity, ProductFormActivity::class.java)
                        intent.putExtra("STREAMER_ID", streamerId)
                        intent.putExtra("PRODUCT_ID", producte.pid)
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