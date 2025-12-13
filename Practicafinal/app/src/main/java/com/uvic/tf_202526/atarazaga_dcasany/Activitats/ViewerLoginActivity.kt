package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Usuari
import com.uvic.tf_202526.atarazaga_dcasany.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewerLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer_login)

        val etNom = findViewById<EditText>(R.id.et_nom_usuari)
        val etPass = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegistre = findViewById<Button>(R.id.btn_registre)

        // --- LOGIN ---
        btnLogin.setOnClickListener {
            val nom = etNom.text.toString()
            val pass = etPass.text.toString()

            if (nom.isNotEmpty() && pass.isNotEmpty()) {
                // Usem Coroutines per no bloquejar l'app (IO Thread)
                lifecycleScope.launch(Dispatchers.IO) {
                    // Cridem al DAO a través del Singleton
                    val usuari = AppSingleton.Companion.getInstance().db.usuariDao().getLogin(nom, pass)

                    // Tornem al fil principal per tocar la UI (navegar o mostrar Toast)
                    withContext(Dispatchers.Main) {
                        if (usuari != null) {
                            if (!usuari.esStreamer) {
                                anarAlDashboard(usuari)
                            } else {
                                Toast.makeText(
                                    this@ViewerLoginActivity,
                                    "Aquest usuari és Streamer, entra per l'altra pantalla!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@ViewerLoginActivity,
                                "Usuari o password incorrectes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Omple tots els camps", Toast.LENGTH_SHORT).show()
            }
        }

        // --- REGISTRE ---
        btnRegistre.setOnClickListener {
            val nom = etNom.text.toString()
            val pass = etPass.text.toString()

            if (nom.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppSingleton.Companion.getInstance().db.usuariDao()

                    // Comprovem si ja existeix
                    val existeix = dao.getUsuariByNom(nom)

                    if (existeix == null) {
                        // Creem l'objecte Usuari (esStreamer = false)
                        val nouUsuari = Usuari(nom = nom, contrasenya = pass, esStreamer = false)
                        dao.addUsuari(nouUsuari)

                        // El recuperem per tenir la ID autogenerada
                        val usuariCreat = dao.getLogin(nom, pass)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ViewerLoginActivity,
                                "Compte creat!",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (usuariCreat != null) anarAlDashboard(usuariCreat)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ViewerLoginActivity,
                                "Aquest nom d'usuari ja existeix",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun anarAlDashboard(usuari: Usuari) {
        // Guardem l'ID de l'usuari en SharedPrefs per recordar la sessió (Sessió 12)
        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        prefs.edit().putInt("USER_ID", usuari.uid).apply()

        val intent = Intent(this, ViewerDashboardActivity::class.java)
        // Passem l'objecte usuari sencer per si de cas
        // (Per fer això caldria que Usuari fos Serializable, però amb l'ID ja fem via)
        intent.putExtra("USER_ID", usuari.uid)
        startActivity(intent)
        finish() // Tanquem login perquè no pugui tirar enrere
    }
}