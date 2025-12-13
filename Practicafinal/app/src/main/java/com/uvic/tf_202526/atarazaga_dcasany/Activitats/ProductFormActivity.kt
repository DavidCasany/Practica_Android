package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
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

    // LAUNCHER CÀMERA
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Si la foto s'ha fet bé, la ruta temporal ara és la bona
            currentPhotoUri = tempPhotoUri
            ivPreview.setImageURI(currentPhotoUri)
        }
    }

    // LAUNCHER GALERIA
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // En galeria rebem directament la URI, però per persistència a vegades cal copiar-la
            // Per simplificar, la farem servir directament (amb permisos de lectura persistents si calgués,
            // però per la pràctica n'hi ha prou així)
            currentPhotoUri = uri
            ivPreview.setImageURI(uri)

            // Permis de lectura persistent (per si reiniciem)
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Algunes versions d'Android no ho necessiten o no ho permeten igual
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        if (streamerId == -1) finish()

        ivPreview = findViewById(R.id.iv_preview)
        val etNom = findViewById<EditText>(R.id.et_nom_prod)
        val etDesc = findViewById<EditText>(R.id.et_desc_prod)
        val etPreu = findViewById<EditText>(R.id.et_preu_prod)
        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnGaleria = findViewById<Button>(R.id.btn_galeria)
        val btnGuardar = findViewById<Button>(R.id.btn_guardar_prod)

        // --- BOTÓ CÀMERA ---
        btnCamera.setOnClickListener {
            if (checkCameraPermission()) {
                obrirCamera()
            } else {
                requestCameraPermission()
            }
        }

        // --- BOTÓ GALERIA ---
        btnGaleria.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // --- GUARDAR A LA BD ---
        btnGuardar.setOnClickListener {
            val nom = etNom.text.toString()
            val preuStr = etPreu.text.toString()

            if (nom.isNotEmpty() && preuStr.isNotEmpty()) {
                val preu = preuStr.toDoubleOrNull() ?: 0.0
                val uriString = currentPhotoUri?.toString() // Convertim URI a String per guardar a Room

                lifecycleScope.launch(Dispatchers.IO) {
                    val nouProducte = Producte(
                        nom = nom,
                        descripcio = etDesc.text.toString(),
                        preu = preu,
                        imatgeUri = uriString,
                        idCreador = streamerId
                    )

                    AppSingleton.getInstance().db.producteDao().addProducte(nouProducte)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProductFormActivity, "Producte Guardat!", Toast.LENGTH_SHORT).show()
                        finish() // Tornem al dashboard
                    }
                }
            } else {
                Toast.makeText(this, "Posa almenys nom i preu", Toast.LENGTH_SHORT).show()
            }
        }

        var productIdToEdit = intent.getIntExtra("PRODUCT_ID", -1)

        if (productIdToEdit != -1) {
            // Si tenim ID de Producte, estem EDITANT
            title = "Editar Producte"
            btnGuardar.text = "ACTUALITZAR PRODUCTE"

            // NOU: Carregar dades a l'AsyncTask
            lifecycleScope.launch(Dispatchers.IO) {
                val producte = AppSingleton.getInstance().db.producteDao().getProducteById(productIdToEdit)
                withContext(Dispatchers.Main) {
                    if (producte != null) {
                        // Omplir els EditTexts i carregar la imatge
                        etNom.setText(producte.nom)
                        etDesc.setText(producte.descripcio)
                        etPreu.setText(producte.preu.toString())
                        // ... (carregar imatge)
                    }
                }
            }
        } else {
            // Si NO tenim ID de Producte, estem CREANT
            title = "Nou Producte"
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
        // Creem un fitxer temporal buit per dir-li a la càmera on guardar la foto
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
            tempPhotoUri = photoURI // Guardem la referència
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