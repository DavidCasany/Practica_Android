package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import androidx.core.net.toUri
import android.net.Uri
import android.os.Bundle
import android.view.View // << IMPORTANT
import android.widget.FrameLayout // << IMPORTANT
import android.widget.ImageView
import android.widget.LinearLayout // << IMPORTANT
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
import java.io.File
import java.io.FileOutputStream

class CreatorDashboardActivity : AppCompatActivity() {

    private var streamerId: Int = -1
    private lateinit var rvProductes: RecyclerView
    private lateinit var ivBanner: ImageView
    private lateinit var placeholderLayout: LinearLayout // << NOU ELEMENT
    private lateinit var bannerContainer: FrameLayout  // << NOU ELEMENT

    // Launcher de Galeria AMB COPIA LOCAL
    private val bannerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                // 1. Copiem la imatge a una ubicació de l'app (Internal Storage)
                val localUriString = copyUriToLocalFile(uri)

                withContext(Dispatchers.Main) {
                    if (localUriString != null) {
                        // 2. Visualitzem la URI LOCAL (file://)
                        ivBanner.setImageURI(Uri.parse(localUriString))

                        // AMAGUEM EL PLACEHOLDER PERQUÈ JA TENIM IMATGE
                        placeholderLayout.visibility = View.GONE
                        ivBanner.scaleType = ImageView.ScaleType.CENTER_CROP

                        // 3. Guardem la URI LOCAL a la Base de Dades
                        guardarBannerBD(localUriString)
                        Toast.makeText(this@CreatorDashboardActivity, "✅ Banner guardat permanentment a l'app!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CreatorDashboardActivity, "❌ Error al processar la imatge. Torna a intentar amb una imatge local.", Toast.LENGTH_LONG).show()
                        // Si falla, mostrem el placeholder de nou
                        ivBanner.setImageDrawable(null)
                        placeholderLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_dashboard)

        // --- 1. RECERCA DE LA ID ---
        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        if (streamerId == -1) {
            val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
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
        placeholderLayout = findViewById(R.id.layout_banner_placeholder) // << VINCULACIÓ
        bannerContainer = findViewById(R.id.banner_container) // << VINCULACIÓ

        val btnAdd = findViewById<FloatingActionButton>(R.id.fab_add_product)

        // --- 3. LISTENERS ---
        // Ara el click el fem al contenidor sencer, així és més fàcil de clicar si està buit
        bannerContainer.setOnClickListener {
            bannerLauncher.launch("image/*")
        }

        btnAdd.setOnClickListener {
            val intent = Intent(this, ProductFormActivity::class.java)
            intent.putExtra("STREAMER_ID", streamerId)
            startActivity(intent)
        }

        // --- 4. CÀRREGA INICIAL DE DADES ---
        carregarDadesUsuari()
        carregarElsMeusProductes()
    }

    // NOU: Funció per copiar la imatge a l'emmagatzematge privat de l'app
    private fun copyUriToLocalFile(originalUri: Uri): String? {
        val fileName = "banner_${streamerId}.jpg"
        val destinationFile = File(filesDir, fileName)

        try {
            contentResolver.openInputStream(originalUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    return destinationFile.toUri().toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    private fun carregarDadesUsuari() {
        lifecycleScope.launch(Dispatchers.IO) {
            val usuari = AppSingleton.getInstance().db.usuariDao().getUsuariById(streamerId)
            withContext(Dispatchers.Main) {
                if (usuari != null) {
                    title = "Botiga de ${usuari.nom}"

                    val bannerUriString = usuari.bannerUri

                    // LÒGICA VISUAL DEL BÀNER
                    if (!bannerUriString.isNullOrEmpty()) {
                        try {
                            ivBanner.setImageURI(Uri.parse(bannerUriString))
                            ivBanner.scaleType = ImageView.ScaleType.CENTER_CROP
                            // Si tenim imatge, amaguem el missatge d'ajuda
                            placeholderLayout.visibility = View.GONE
                        } catch (e: Exception) {
                            // Si falla la càrrega, mostrem el missatge
                            ivBanner.setImageDrawable(null)
                            placeholderLayout.visibility = View.VISIBLE
                        }
                    } else {
                        // Si no hi ha imatge, mostrem el missatge elegant
                        ivBanner.setImageDrawable(null)
                        placeholderLayout.visibility = View.VISIBLE
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
            val llista = AppSingleton.getInstance().db.producteDao().getProductesByStreamer(streamerId)

            withContext(Dispatchers.Main) {
                val adapter = CreatorProductAdapter(llista, object : CreatorProductAdapter.OnCreatorClickListener {
                    override fun onEditClick(producte: Producte) {
                        val intent = Intent(this@CreatorDashboardActivity, ProductFormActivity::class.java)
                        intent.putExtra("STREAMER_ID", streamerId)
                        intent.putExtra("PRODUCT_ID", producte.pid)
                        startActivity(intent)
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
            AppSingleton.getInstance().db.producteDao().deleteProducte(producte)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CreatorDashboardActivity, "Eliminat!", Toast.LENGTH_SHORT).show()
                carregarElsMeusProductes()
            }
        }
    }
}