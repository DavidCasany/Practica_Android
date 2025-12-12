package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.uvic.tf_202526.atarazaga_dcasany.Adaptadors.BotigaAdapter
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.BotigaVisitada
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewerDashboardActivity : AppCompatActivity() {

    private lateinit var rvBotigues: RecyclerView
    private var userId: Int = -1

    // 1. Definim el Launcher del Lector QR (Sessió 11)
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Escaneig cancel·lat", Toast.LENGTH_SHORT).show()
        } else {
            // El QR ens retorna un String. Assumim que és l'ID del Streamer (ex: "1")
            val rawResult = result.contents
            guardarBotiga(rawResult)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer_dashboard)

        rvBotigues = findViewById(R.id.rv_botigues)
        rvBotigues.layoutManager = LinearLayoutManager(this)

        val btnScan = findViewById<FloatingActionButton>(R.id.fab_scan_qr)

        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", -1)

        if (userId == -1) {
            finish()
            return
        }

        // 2. Al fer clic, llancem l'escàner
        btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt("Escaneja el QR del Streamer")
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            qrLauncher.launch(options)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarBotigues()
    }

    // 3. Funció per guardar la visita a la BD
    private fun guardarBotiga(qrContent: String) {
        // Intentem convertir el text del QR a un número (ID del Streamer)
        val streamerId = qrContent.toIntOrNull()

        if (streamerId != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                // Creem l'objecte de la visita
                val novaVisita = BotigaVisitada(
                    idEspectador = userId,
                    idStreamer = streamerId
                )

                // Guardem a Room (si ja existeix, el DAO ho ignorarà gràcies al OnConflictStrategy.IGNORE)
                AppSingleton.getInstance().db.botigaVisitadaDao().addVisita(novaVisita)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewerDashboardActivity, "Botiga afegida!", Toast.LENGTH_SHORT).show()
                    // Refresquem la llista
                    carregarBotigues()
                }
            }
        } else {
            Toast.makeText(this, "Codi QR invàlid: No és un ID numèric", Toast.LENGTH_LONG).show()
        }
    }

    private fun carregarBotigues() {
        lifecycleScope.launch(Dispatchers.IO) {
            val llistaBotigues = AppSingleton.getInstance().db.botigaVisitadaDao().getVisitesByEspectador(userId)

            withContext(Dispatchers.Main) {
                val adapter = BotigaAdapter(llistaBotigues, object : BotigaAdapter.OnItemClickListener {
                    override fun onItemClick(botiga: BotigaVisitada) {
                        val intent = Intent(this@ViewerDashboardActivity, StoreActivity::class.java)
                        intent.putExtra("STREAMER_ID", botiga.idStreamer)
                        startActivity(intent)
                    }
                })
                rvBotigues.adapter = adapter
            }
        }
    }
}