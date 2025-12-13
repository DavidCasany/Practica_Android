package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductFormActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private var currentPhotoUri: Uri? = null // Aquí guardarem la ruta de la foto final
    private var tempPhotoUri: Uri? = null    // Ruta temporal per a la càmera
    private var streamerId: Int = -1
    private var productIdToEdit: Int = -1    // ID del producte si estem editant (-1 si és nou)

    // LAUNCHER CÀMERA
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri = tempPhotoUri
            ivPreview.setImageURI(currentPhotoUri)
        }
    }

    // LAUNCHER GALERIA
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            currentPhotoUri = uri
            ivPreview.setImageURI(uri)
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Ignorar error en versions antigues
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        // 1. RECUPEREM IDs DE L'INTENT
        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        productIdToEdit = intent.getIntExtra("PRODUCT_ID", -1)

        // Si no tenim streamerId, alguna cosa ha anat malament
        if (streamerId == -1) {
            finish()
            return
        }

        // 2. VINCULEM ELS ELEMENTS DE LA UI
        ivPreview = findViewById(R.id.iv_preview)
        val etNom = findViewById<EditText>(R.id.et_nom_prod)
        val etDesc = findViewById<EditText>(R.id.et_desc_prod)
        val etPreu = findViewById<EditText>(R.id.et_preu_prod)
        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnGaleria = findViewById<Button>(R.id.btn_galeria)
        val btnGuardar = findViewById<Button>(R.id.btn_guardar_prod)

        // --- NOUS CAMPS D'OFERTA (Dev B) ---
        val cbOferta = findViewById<CheckBox>(R.id.cb_es_oferta)
        val etPreuOferta = findViewById<EditText>(R.id.et_preu_oferta)

        // Lògica CheckBox: Si es marca, s'activa el camp de preu oferta
        cbOferta.setOnCheckedChangeListener { _, isChecked ->
            etPreuOferta.isEnabled = isChecked
            if (!isChecked) etPreuOferta.text.clear()
        }

        // 3. SI ESTEM EN MODE EDICIÓ, CARREGUEM DADES
        if (productIdToEdit != -1) {
            title = "Editar Producte"
            btnGuardar.text = "ACTUALITZAR PRODUCTE"

            lifecycleScope.launch(Dispatchers.IO) {
                val prod = AppSingleton.getInstance().db.producteDao().getProducteById(productIdToEdit)

                withContext(Dispatchers.Main) {
                    if (prod != null) {
                        etNom.setText(prod.nom)
                        etDesc.setText(prod.descripcio)
                        etPreu.setText(prod.preu.toString())

                        // Carregar Ofertes
                        cbOferta.isChecked = prod.esOferta
                        if (prod.preuOferta > 0) {
                            etPreuOferta.setText(prod.preuOferta.toString())
                        }
                        etPreuOferta.isEnabled = prod.esOferta

                        // Carregar Imatge existent
                        if (!prod.imatgeUri.isNullOrEmpty()) {
                            currentPhotoUri = Uri.parse(prod.imatgeUri)
                            ivPreview.setImageURI(currentPhotoUri)
                        }
                    }
                }
            }
        } else {
            title = "Nou Producte"
        }

        // --- BOTONS CÀMERA / GALERIA ---
        btnCamera.setOnClickListener {
            if (checkCameraPermission()) obrirCamera() else requestCameraPermission()
        }

        btnGaleria.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // --- GUARDAR (INSERT o UPDATE) ---
        btnGuardar.setOnClickListener {
            val nom = etNom.text.toString()
            val preuStr = etPreu.text.toString()
            val desc = etDesc.text.toString()

            // Recollim dades d'oferta
            val esOferta = cbOferta.isChecked
            val preuOferta = etPreuOferta.text.toString().toDoubleOrNull() ?: 0.0

            if (nom.isNotEmpty() && preuStr.isNotEmpty()) {
                val preu = preuStr.toDoubleOrNull() ?: 0.0
                val uriString = currentPhotoUri?.toString()

                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppSingleton.getInstance().db.producteDao()

                    if (productIdToEdit == -1) {
                        // A) CREAR NOU (INSERT)
                        val nouProducte = Producte(
                            nom = nom,
                            descripcio = desc,
                            preu = preu,
                            imatgeUri = uriString,
                            esOferta = esOferta,
                            preuOferta = preuOferta,
                            idCreador = streamerId
                        )
                        dao.addProducte(nouProducte)
                    } else {
                        // B) ACTUALITZAR EXISTENT (UPDATE)
                        // Important: Mantenim la mateixa ID (pid)
                        val prodEditat = Producte(
                            pid = productIdToEdit,
                            nom = nom,
                            descripcio = desc,
                            preu = preu,
                            imatgeUri = uriString,
                            esOferta = esOferta,
                            preuOferta = preuOferta,
                            idCreador = streamerId
                        )
                        dao.updateProducte(prodEditat)
                    }

                    withContext(Dispatchers.Main) {
                        val missatge = if (productIdToEdit == -1) "Creat correctament!" else "Actualitzat correctament!"
                        Toast.makeText(this@ProductFormActivity, missatge, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Nom i Preu són obligatoris", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- FUNCIONS AUXILIARS CÀMERA ---

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
    }

    private fun obrirCamera() {
        val photoFile: File? = try {
            crearFitxerImatge()
        } catch (ex: IOException) {
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            tempPhotoUri = photoURI
            cameraLauncher.launch(photoURI)
        }
    }

    @Throws(IOException::class)
    private fun crearFitxerImatge(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
}