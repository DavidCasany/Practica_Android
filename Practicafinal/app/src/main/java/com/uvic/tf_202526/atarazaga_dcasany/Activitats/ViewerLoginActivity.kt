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


        btnLogin.setOnClickListener {
            val nom = etNom.text.toString()
            val pass = etPass.text.toString()

            if (nom.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val usuari = AppSingleton.getInstance().db.usuariDao().getLogin(nom, pass)

                    withContext(Dispatchers.Main) {
                        if (usuari != null) {
                            if (!usuari.esStreamer) {
                                anarAlDashboard(usuari)
                            } else {
                                Toast.makeText(this@ViewerLoginActivity, getString(R.string.error_role_streamer), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@ViewerLoginActivity, getString(R.string.error_login_incorrect), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            }
        }


        btnRegistre.setOnClickListener {
            val nom = etNom.text.toString()
            val pass = etPass.text.toString()

            if (nom.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppSingleton.getInstance().db.usuariDao()
                    val existeix = dao.getUsuariByNom(nom)

                    if (existeix == null) {
                        val nouUsuari = Usuari(nom = nom, contrasenya = pass, esStreamer = false)
                        dao.addUsuari(nouUsuari)
                        val usuariCreat = dao.getLogin(nom, pass)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ViewerLoginActivity, getString(R.string.msg_account_created), Toast.LENGTH_SHORT).show()
                            if (usuariCreat != null) anarAlDashboard(usuariCreat)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ViewerLoginActivity, getString(R.string.error_user_exists), Toast.LENGTH_SHORT).show()
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
        prefs.edit().putInt("USER_ID", usuari.uid).apply()

        val intent = Intent(this, ViewerDashboardActivity::class.java)
        intent.putExtra("USER_ID", usuari.uid)
        startActivity(intent)
        finish()
    }
}