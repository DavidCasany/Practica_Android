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

    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, getString(R.string.scan_cancelled), Toast.LENGTH_SHORT).show()
        } else {
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

        btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt(getString(R.string.scan_prompt))
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            qrLauncher.launch(options)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarBotigues()
    }

    private fun guardarBotiga(qrContent: String) {
        val streamerId = qrContent.toIntOrNull()

        if (streamerId != null) {
            if (streamerId == userId) {
                Toast.makeText(this, getString(R.string.error_self_shop), Toast.LENGTH_SHORT).show()
                return
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppSingleton.getInstance().db
                val usuariStreamer = db.usuariDao().getUsuariById(streamerId)
                val jaLaTinc = db.botigaVisitadaDao().jaLaTinc(userId, streamerId)

                withContext(Dispatchers.Main) {
                    if (usuariStreamer == null) {
                        Toast.makeText(this@ViewerDashboardActivity, getString(R.string.error_qr_user_not_found), Toast.LENGTH_LONG).show()
                    } else if (!usuariStreamer.esStreamer) {
                        Toast.makeText(this@ViewerDashboardActivity, getString(R.string.error_qr_not_streamer), Toast.LENGTH_LONG).show()
                    } else if (jaLaTinc) {
                        Toast.makeText(this@ViewerDashboardActivity, getString(R.string.error_shop_already_added), Toast.LENGTH_SHORT).show()
                    } else {
                        guardarDefinitivament(streamerId)
                    }
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.error_qr_invalid), Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarDefinitivament(streamerId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val novaVisita = BotigaVisitada(idEspectador = userId, idStreamer = streamerId)
            AppSingleton.getInstance().db.botigaVisitadaDao().addVisita(novaVisita)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ViewerDashboardActivity, getString(R.string.msg_shop_added), Toast.LENGTH_SHORT).show()
                carregarBotigues()
            }
        }
    }

    private fun carregarBotigues() {
        lifecycleScope.launch(Dispatchers.IO) {
            val llistaBotigues = AppSingleton.getInstance().db.botigaVisitadaDao().getBotiguesAmbDetall(userId)

            withContext(Dispatchers.Main) {
                val adapter = BotigaAdapter(llistaBotigues) { botigaClickada ->
                    val intent = Intent(this@ViewerDashboardActivity, StoreActivity::class.java)
                    intent.putExtra("STREAMER_ID", botigaClickada.idStreamer)
                    startActivity(intent)
                }
                rvBotigues.adapter = adapter
            }
        }
    }
}