package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import androidx.core.net.toUri
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var placeholderLayout: LinearLayout
    private lateinit var bannerContainer: FrameLayout

    private val bannerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            lifecycleScope.launch(Dispatchers.IO) {

                val localUriString = copyUriToLocalFile(uri)

                withContext(Dispatchers.Main) {
                    if (localUriString != null) {

                        actualitzarUIBanner(localUriString)


                        guardarBannerBD(localUriString)

                        Toast.makeText(this@CreatorDashboardActivity, getString(R.string.banner_saved_success), Toast.LENGTH_SHORT).show()
                    } else {

                        Toast.makeText(this@CreatorDashboardActivity, getString(R.string.banner_process_error), Toast.LENGTH_LONG).show()
                        actualitzarUIBanner(null)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_dashboard)


        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        if (streamerId == -1) {
            val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
            streamerId = prefs.getInt("USER_ID", -1)
        }

        if (streamerId == -1) {
            Toast.makeText(this, getString(R.string.error_session_invalid), Toast.LENGTH_LONG).show()
            finish()
            return
        }


        Toast.makeText(this, getString(R.string.dashboard_loaded_msg, streamerId), Toast.LENGTH_SHORT).show()


        rvProductes = findViewById(R.id.rv_els_meus_productes)
        rvProductes.layoutManager = GridLayoutManager(this, 2)


        ivBanner = findViewById(R.id.iv_dashboard_banner)
        placeholderLayout = findViewById(R.id.layout_banner_placeholder)
        bannerContainer = findViewById(R.id.banner_container)

        val btnAdd = findViewById<FloatingActionButton>(R.id.fab_add_product)

        bannerContainer.setOnClickListener {
            bannerLauncher.launch("image/*")
        }

        btnAdd.setOnClickListener {
            val intent = Intent(this, ProductFormActivity::class.java)
            intent.putExtra("STREAMER_ID", streamerId)
            startActivity(intent)
        }


        carregarDadesUsuari()
        carregarElsMeusProductes()
    }


    private fun actualitzarUIBanner(uriString: String?) {
        if (!uriString.isNullOrEmpty()) {
            try {
                ivBanner.setImageURI(Uri.parse(uriString))
                ivBanner.scaleType = ImageView.ScaleType.CENTER_CROP

                placeholderLayout.visibility = View.GONE
            } catch (e: Exception) {

                ivBanner.setImageDrawable(null)
                placeholderLayout.visibility = View.VISIBLE
            }
        } else {

            ivBanner.setImageDrawable(null)
            placeholderLayout.visibility = View.VISIBLE
        }
    }


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

                    title = getString(R.string.shop_title_format, usuari.nom)


                    actualitzarUIBanner(usuari.bannerUri)
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

            carregarDadesUsuari()
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
                        Toast.makeText(this@CreatorDashboardActivity, getString(R.string.edit_product_toast, producte.nom), Toast.LENGTH_SHORT).show()
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
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.confirm_delete_msg, producte.nom))
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                esborrarProducte(producte)
            }
            .setNegativeButton(getString(R.string.dialog_no), null)
            .show()
    }

    private fun esborrarProducte(producte: Producte) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppSingleton.getInstance().db.producteDao().deleteProducte(producte)

            withContext(Dispatchers.Main) {
                // TEXT TRADUÏT
                Toast.makeText(this@CreatorDashboardActivity, getString(R.string.deleted_toast), Toast.LENGTH_SHORT).show()
                carregarElsMeusProductes()
            }
        }
    }
}