package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductFormActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private var currentPhotoUri: Uri? = null
    private var currentTempPhotoPath: String? = null
    private var streamerId: Int = -1
    private var productIdToEdit: Int = -1

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentTempPhotoPath?.let { tempPath ->
                lifecycleScope.launch(Dispatchers.IO) {


                    val localPathString = copyFileToLocalFile(tempPath)


                    withContext(Dispatchers.Main) {
                        if (localPathString != null) {
                            currentPhotoUri = Uri.fromFile(File(localPathString))

                            try {
                                val bitmap = BitmapFactory.decodeFile(localPathString)
                                if (bitmap != null) {
                                    ivPreview.setImageBitmap(bitmap)
                                    ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                                    Log.d("ProductForm", "Imatge carregada a l'ImageView.")
                                } else {
                                    Log.e("ProductForm", "ERROR: BitmapFactory.decodeFile ha retornat NULL per a la ruta: $localPathString")
                                    ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                }
                            } catch (e: Exception) {
                                Log.e("ProductForm", "Error al carregar la imatge de la càmera amb decodeFile: ${e.message}")
                                ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                            }
                            // TEXT TRADUÏT
                            Toast.makeText(this@ProductFormActivity, getString(R.string.msg_camera_saved), Toast.LENGTH_SHORT).show()
                        } else {
                            // TEXT TRADUÏT
                            Toast.makeText(this@ProductFormActivity, getString(R.string.msg_camera_error), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        currentTempPhotoPath = null
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val localPathString = copyUriToLocalFile(uri)

                withContext(Dispatchers.Main) {
                    if (localPathString != null) {
                        currentPhotoUri = Uri.fromFile(File(localPathString))

                        try {
                            val imageFile = File(localPathString)
                            imageFile.inputStream().use { inputStream ->
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                if (bitmap != null) {
                                    ivPreview.setImageBitmap(bitmap)
                                    ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                                    ivPreview.requestLayout()
                                    ivPreview.invalidate()
                                } else {
                                    ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ProductForm", "Error al carregar la imatge al galleryLauncher: ${e.message}")
                            ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                        }


                        Toast.makeText(this@ProductFormActivity, getString(R.string.msg_gallery_saved), Toast.LENGTH_SHORT).show()
                    } else {

                        Toast.makeText(this@ProductFormActivity, getString(R.string.msg_gallery_error), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        // 1. RECUPEREM IDs DE L'INTENT
        streamerId = intent.getIntExtra("STREAMER_ID", -1)
        productIdToEdit = intent.getIntExtra("PRODUCT_ID", -1)

        if (streamerId == -1) {
            finish()
            return
        }

        // 2. VINCULEM ELS ELEMENTS DE LA UI
        ivPreview = findViewById(R.id.iv_preview)
        val tvTitleForm = findViewById<TextView>(R.id.tv_title_form)
        val etNom = findViewById<EditText>(R.id.et_nom_prod)
        val etDesc = findViewById<EditText>(R.id.et_desc_prod)
        val etPreu = findViewById<EditText>(R.id.et_preu_prod)
        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnGaleria = findViewById<Button>(R.id.btn_galeria)
        val btnGuardar = findViewById<Button>(R.id.btn_guardar_prod)
        val cbOferta = findViewById<CheckBox>(R.id.cb_es_oferta)
        val etPreuOferta = findViewById<EditText>(R.id.et_preu_oferta)


        cbOferta.setOnCheckedChangeListener { _, isChecked ->
            etPreuOferta.isEnabled = isChecked
        }

        // 3. SI ESTEM EN MODE EDICIÓ, CARREGUEM DADES
        if (productIdToEdit != -1) {
            // TEXT TRADUÏT
            tvTitleForm.text = getString(R.string.title_edit_product)
            btnGuardar.text = getString(R.string.btn_update_product)

            lifecycleScope.launch(Dispatchers.IO) {
                val prod = AppSingleton.getInstance().db.producteDao().getProducteById(productIdToEdit)

                Log.d("DEBUG_IMATGE", "Producte carregat per edició:")
                Log.d("DEBUG_IMATGE", "  - Nom: ${prod?.nom}")
                Log.d("DEBUG_IMATGE", "  - imatgeUri: ${prod?.imatgeUri}")
                Log.d("DEBUG_IMATGE", "  - L'URI és null o buida?: ${prod?.imatgeUri.isNullOrEmpty()}")

                withContext(Dispatchers.Main) {
                    if (prod != null) {
                        // Carregar dades dels camps
                        etNom.setText(prod.nom)
                        etDesc.setText(prod.descripcio)
                        etPreu.setText(prod.preu.toString())

                        cbOferta.isChecked = prod.esOferta
                        etPreuOferta.isEnabled = prod.esOferta
                        if (prod.preuOferta > 0.0) {
                            etPreuOferta.setText(prod.preuOferta.toString())
                        } else {
                            etPreuOferta.setText("")
                        }


                        if (!prod.imatgeUri.isNullOrEmpty()) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val savedUri = Uri.parse(prod.imatgeUri)
                                    Log.d("DEBUG_IMATGE", "Intentant obrir URI: $savedUri, Scheme: ${savedUri.scheme}")

                                    var inputStream: java.io.InputStream? = null
                                    if (savedUri.scheme == "content") {
                                        try { inputStream = contentResolver.openInputStream(savedUri) } catch (_: Exception) {}
                                    }
                                    if (inputStream == null && savedUri.scheme == "file") {
                                        try {
                                            val file = File(savedUri.path ?: "")
                                            if (file.exists()) { inputStream = FileInputStream(file) }
                                        } catch (_: Exception) {}
                                    }

                                    inputStream?.use { stream ->
                                        val bitmap = BitmapFactory.decodeStream(stream)

                                        withContext(Dispatchers.Main) {
                                            if (bitmap != null) {
                                                ivPreview.setImageBitmap(bitmap)
                                                ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                                                currentPhotoUri = savedUri
                                            } else {
                                                ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                            }
                                        }
                                    } ?: run {
                                        withContext(Dispatchers.Main) { ivPreview.setImageResource(android.R.drawable.ic_menu_camera) }
                                    }

                                } catch (e: Exception) {
                                    Log.e("DEBUG_IMATGE", "Error general carregant imatge: ${e.message}")
                                    withContext(Dispatchers.Main) { ivPreview.setImageResource(android.R.drawable.ic_menu_camera) }
                                }
                            }
                        } else {
                            ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                        }
                    } else {

                        productIdToEdit = -1
                        tvTitleForm.text = getString(R.string.title_new_product)
                        btnGuardar.text = getString(R.string.btn_save_product)
                    }
                }
            }
        } else {

            tvTitleForm.text = getString(R.string.title_new_product)
            etPreuOferta.isEnabled = false
        }


        btnCamera.setOnClickListener {
            if (checkCameraPermission()) obrirCamera() else requestCameraPermission()
        }

        btnGaleria.setOnClickListener {
            galleryLauncher.launch("image/*")
        }


        btnGuardar.setOnClickListener {
            val nom = etNom.text.toString()
            val preuStr = etPreu.text.toString()
            val desc = etDesc.text.toString()

            val esOferta = cbOferta.isChecked
            val preuOferta = etPreuOferta.text.toString().toDoubleOrNull() ?: 0.0

            if (nom.isNotEmpty() && preuStr.isNotEmpty()) {
                val preu = preuStr.toDoubleOrNull() ?: 0.0
                if (preu <= 0.0) {

                    Toast.makeText(this, getString(R.string.error_price_zero), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (esOferta && preuOferta >= preu) {

                    Toast.makeText(this, getString(R.string.error_offer_price_invalid), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val uriString = currentPhotoUri?.toString()

                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppSingleton.getInstance().db.producteDao()
                    val nouProducte = Producte(
                        pid = if (productIdToEdit != -1) productIdToEdit else 0,
                        nom = nom,
                        descripcio = desc,
                        preu = preu,
                        imatgeUri = uriString,
                        esOferta = esOferta,
                        preuOferta = preuOferta,
                        idCreador = streamerId
                    )

                    val missatgeId = if (productIdToEdit == -1) {
                        dao.addProducte(nouProducte)
                        R.string.msg_product_created
                    } else {
                        dao.updateProducte(nouProducte)
                        R.string.msg_product_updated
                    }

                    withContext(Dispatchers.Main) {

                        Toast.makeText(this@ProductFormActivity, getString(missatgeId), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_required_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyUriToLocalFile(originalUri: Uri): String? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "prod_${streamerId}_${timeStamp}.jpg"
        val destinationFile = File(filesDir, fileName)

        try {
            contentResolver.openInputStream(originalUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    return destinationFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }



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
            currentTempPhotoPath = it.absolutePath

            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            cameraLauncher.launch(photoURI)
        }
    }

    private fun copyFileToLocalFile(sourcePath: String): String? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sourceFile = File(sourcePath)
        val fileName = "prod_${streamerId}_${timeStamp}.jpg"
        val destinationFile = File(filesDir, fileName)

        if (!sourceFile.exists()) {
            return null
        }

        try {
            FileInputStream(sourceFile).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)

                    // Després de copiar, esborrem el fitxer temporal!
                    sourceFile.delete()

                    return destinationFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    private fun crearFitxerImatge(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
}