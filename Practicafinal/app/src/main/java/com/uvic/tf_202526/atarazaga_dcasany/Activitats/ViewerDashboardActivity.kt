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
        val streamerId = qrContent.toIntOrNull()

        if (streamerId != null) {
            // Evitem que un usuari s'afegeixi a si mateix (opcional, però recomanat)
            if (streamerId == userId) {
                Toast.makeText(this, "No pots afegir la teva pròpia botiga!", Toast.LENGTH_SHORT).show()
                return
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppSingleton.getInstance().db

                // 1. COMPROVACIONS DE SEGURETAT
                val usuariStreamer = db.usuariDao().getUsuariById(streamerId)
                val jaLaTinc = db.botigaVisitadaDao().jaLaTinc(userId, streamerId)

                withContext(Dispatchers.Main) {
                    if (usuariStreamer == null) {
                        // El número no existeix a la BD
                        Toast.makeText(this@ViewerDashboardActivity, "Error: Aquest codi no correspon a cap usuari.", Toast.LENGTH_LONG).show()

                    } else if (!usuariStreamer.esStreamer) {
                        // L'usuari existeix, però és un espectador (no té botiga)
                        Toast.makeText(this@ViewerDashboardActivity, "Error: Aquest usuari no és un Streamer!", Toast.LENGTH_LONG).show()

                    } else if (jaLaTinc) {
                        // Ja tenim la botiga a la llista
                        Toast.makeText(this@ViewerDashboardActivity, "Ja tens aquesta llista afegida!", Toast.LENGTH_SHORT).show()

                    } else {
                        // 2. TOT CORRECTE: GUARDEM
                        guardarDefinitivament(streamerId)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Codi QR invàlid", Toast.LENGTH_SHORT).show()
        }
    }

    // Funció auxiliar per no tenir tot el codi 'anidat'
    private fun guardarDefinitivament(streamerId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val novaVisita = BotigaVisitada(idEspectador = userId, idStreamer = streamerId)
            AppSingleton.getInstance().db.botigaVisitadaDao().addVisita(novaVisita)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ViewerDashboardActivity, "Botiga afegida correctament!", Toast.LENGTH_SHORT).show()
                carregarBotigues()
            }
        }
    }

    private fun carregarBotigues() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Cridem a la nova Query que fa el JOIN
            val llistaBotigues = AppSingleton.getInstance().db.botigaVisitadaDao().getBotiguesAmbDetall(userId)

            withContext(Dispatchers.Main) {
                if (llistaBotigues.isEmpty()) {
                    // Opcional: Mostrar text "No tens botigues"
                }

                // 2. Configurem l'adaptador amb la nova llista de 'BotigaDisplay'
                val adapter = BotigaAdapter(llistaBotigues) { botigaClickada ->

                    // Quan cliquem, obrim la botiga passant la ID
                    val intent = Intent(this@ViewerDashboardActivity, StoreActivity::class.java)
                    intent.putExtra("STREAMER_ID", botigaClickada.idStreamer)
                    startActivity(intent)
                }

                // Assegura't que el teu RecyclerView es diu 'rvBotigues' o similar
                val rvBotigues = findViewById<RecyclerView>(R.id.rv_botigues)
                rvBotigues.adapter = adapter
            }
        }
    }
}