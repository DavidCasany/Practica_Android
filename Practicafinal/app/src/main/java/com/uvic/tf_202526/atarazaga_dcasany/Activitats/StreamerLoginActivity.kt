package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Usuari
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StreamerLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streamer_login)

        val etNom = findViewById<EditText>(R.id.et_nom_usuari)
        val etPass = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegistre = findViewById<Button>(R.id.btn_registre)

        // --- LOGIN STREAMER ---
        btnLogin.setOnClickListener {
            val nom = etNom.text.toString()
            val pass = etPass.text.toString()

            if (nom.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val usuari = AppSingleton.getInstance().db.usuariDao().getLogin(nom, pass)

                    withContext(Dispatchers.Main) {
                        if (usuari != null) {
                            if (usuari.esStreamer) {
                                anarAlDashboard(usuari)
                            } else {
                                Toast.makeText(this@StreamerLoginActivity, "Ets un espectador! Ves a la pantalla de Viewer.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@StreamerLoginActivity, "Dades incorrectes", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // --- REGISTRE STREAMER ---
        btnRegistre.setOnClickListener {
            val nom = etNom.text.toString()
            val pass = etPass.text.toString()

            if (nom.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppSingleton.getInstance().db.usuariDao()

                    if (dao.getUsuariByNom(nom) == null) {
                        // Creem i afegim el nou streamer
                        val nouStreamer = Usuari(nom = nom, contrasenya = pass, esStreamer = true)
                        dao.addUsuari(nouStreamer)

                        val usuariCreat = dao.getLogin(nom, pass)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@StreamerLoginActivity, "Botiga creada!", Toast.LENGTH_SHORT).show()
                            if (usuariCreat != null) anarAlDashboard(usuariCreat)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@StreamerLoginActivity, "Aquest nom ja existeix", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * CORRECCIÓ CLAU: Guardar l'ID com a USER_ID a SharedPreferences
     * i passar la flag IS_STREAMER.
     * Això garanteix que les pantalles com CreatorDashboardActivity i altres
     * puguin carregar dades correctament a l'inici.
     */
    private fun anarAlDashboard(usuari: Usuari) {
        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            // AQUESTA LÍNIA ÉS CLAU: Guarda l'ID sota la clau que fan servir totes les activitats
            putInt("USER_ID", usuari.uid)

            // També guardem STREAMER_ID i la flag (per seguretat i claredat)
            putInt("STREAMER_ID", usuari.uid)
            putBoolean("IS_STREAMER", true)
            apply()
        }

        val intent = Intent(this, CreatorDashboardActivity::class.java)
        // Passem la ID via Intent per si l'activitat la necessita immediatament
        intent.putExtra("STREAMER_ID", usuari.uid)
        startActivity(intent)
        finish()
    }
}