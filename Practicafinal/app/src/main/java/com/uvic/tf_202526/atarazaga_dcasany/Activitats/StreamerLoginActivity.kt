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
                                Toast.makeText(this@StreamerLoginActivity, getString(R.string.error_role_viewer), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@StreamerLoginActivity, getString(R.string.error_login_incorrect), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@StreamerLoginActivity, getString(R.string.msg_shop_created), Toast.LENGTH_SHORT).show()
                            if (usuariCreat != null) anarAlDashboard(usuariCreat)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@StreamerLoginActivity, getString(R.string.error_user_exists), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun anarAlDashboard(usuari: Usuari) {
        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("USER_ID", usuari.uid)
            putInt("STREAMER_ID", usuari.uid)
            putBoolean("IS_STREAMER", true)
            apply()
        }

        val intent = Intent(this, CreatorDashboardActivity::class.java)
        intent.putExtra("STREAMER_ID", usuari.uid)
        startActivity(intent)
        finish()
    }
}