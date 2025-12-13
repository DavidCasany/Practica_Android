package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.net.toUri
import android.os.Bundle
import android.os.Environment
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
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import android.util.Log

class ProductFormActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private var currentPhotoUri: Uri? = null // Contindrà la URI del fitxer local amb esquema 'file://'
    private var tempPhotoUri: Uri? = null    // Ruta temporal per a la càmera
    private var streamerId: Int = -1
    private var productIdToEdit: Int = -1

    // LAUNCHER CÀMERA (CORREGIT)
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            //currentPhotoUri = tempPhotoUri // <-- ELIMINAT. El copiem a continuació

            // ** FIX CÀMERA 1: Copiem la URI de la càmera a l'emmagatzematge intern **
            tempPhotoUri?.let { uri ->
                lifecycleScope.launch(Dispatchers.IO) {
                    // El camí local és la RUTA ABSOLUTA.
                    val localPathString = copyUriToLocalFile(uri)

                    withContext(Dispatchers.Main) {
                        if (localPathString != null) {
                            // currentPhotoUri es crea a partir del camí local (més robust)
                            currentPhotoUri = Uri.fromFile(File(localPathString))

                            // ** FIX CÀMERA 2: Carreguem amb BitmapFactory (igual que la galeria) **
                            try {
                                val bitmap = BitmapFactory.decodeFile(localPathString)
                                if (bitmap != null) {
                                    ivPreview.setImageBitmap(bitmap)
                                } else {
                                    ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                }
                            } catch (e: Exception) {
                                Log.e("ProductForm", "Error al carregar la imatge de la càmera: ${e.message}")
                                ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                            }
                            Toast.makeText(this@ProductFormActivity, "Imatge de càmera copiada localment.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ProductFormActivity, "Error al copiar la imatge de la càmera.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // LAUNCHER GALERIA (CORREGIT)
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                // El localPathString és ara la RUTA ABSOLUTA del fitxer copiat
                val localPathString = copyUriToLocalFile(uri)

                withContext(Dispatchers.Main) {
                    if (localPathString != null) {
                        // Creem la URI a partir de la ruta per guardar-la a la BD
                        currentPhotoUri = Uri.fromFile(File(localPathString))

                        // ** FIX GALERIA: Descodificació de la imatge des de la RUTA ABSOLUTA **
                        try {
                            val bitmap = BitmapFactory.decodeFile(localPathString)
                            if (bitmap != null) {
                                ivPreview.setImageBitmap(bitmap)
                            } else {
                                ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                            }
                        } catch (e: Exception) {
                            Log.e("ProductForm", "Error al carregar la imatge al galleryLauncher: ${e.message}")
                            ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                        }

                        Toast.makeText(this@ProductFormActivity, "Imatge copiada localment.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProductFormActivity, "Error al copiar la imatge. Utilitza una imatge local.", Toast.LENGTH_LONG).show()
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

        // --- CAMPS D'OFERTA ---
        val cbOferta = findViewById<CheckBox>(R.id.cb_es_oferta)
        val etPreuOferta = findViewById<EditText>(R.id.et_preu_oferta)

        // Lògica CheckBox: Si es marca, s'activa el camp de preu oferta
        cbOferta.setOnCheckedChangeListener { _, isChecked ->
            etPreuOferta.isEnabled = isChecked
        }

        // 3. SI ESTEM EN MODE EDICIÓ, CARREGUEM DADES
        if (productIdToEdit != -1) {
            tvTitleForm.text = "Editar Producte"
            btnGuardar.text = "ACTUALITZAR PRODUCTE"

            lifecycleScope.launch(Dispatchers.IO) {
                val prod = AppSingleton.getInstance().db.producteDao().getProducteById(productIdToEdit)

                withContext(Dispatchers.Main) {
                    if (prod != null) {
                        // [INICI: Càrrega de dades per a tots els camps]
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
                        // [FINAL: Càrrega de dades]


                        // Carregar Imatge existent (FIX DEFINITIU)
                        if (!prod.imatgeUri.isNullOrEmpty()) {
                            try {
                                val savedUri = Uri.parse(prod.imatgeUri)

                                // ** CORRECCIÓ: Extraiem el PATH ABSOLUT de la URI. **
                                // El .path de file://ruta/fitxer.jpg és /ruta/fitxer.jpg.
                                val filePath = savedUri.path

                                if (filePath != null && File(filePath).exists()) {
                                    val bitmap = BitmapFactory.decodeFile(filePath)
                                    if (bitmap != null) {
                                        ivPreview.setImageBitmap(bitmap)
                                        currentPhotoUri = savedUri // Mantenim la URI completa per guardar
                                    } else {
                                        // Fallback si la descodificació falla
                                        ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                    }
                                } else {
                                    // Fallback si el fitxer no existeix
                                    ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                }

                            } catch (e: Exception) {
                                Log.e("ProductForm", "Error al carregar la imatge en edició: ${e.message}")
                                ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                            }
                        } else {
                            // Si no hi ha URI
                            ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                        }
                    } else {
                        // Si l'ID d'edició falla...
                        productIdToEdit = -1
                        tvTitleForm.text = "Nou Producte"
                        btnGuardar.text = "GUARDAR PRODUCTE"
                    }
                }
            }
        } else {
            tvTitleForm.text = "Nou Producte"
            etPreuOferta.isEnabled = false
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
                if (preu <= 0.0) {
                    Toast.makeText(this, "El preu ha de ser superior a 0.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (esOferta && preuOferta >= preu) {
                    Toast.makeText(this, "El preu d'oferta no pot ser superior al preu base.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // IMPORTANT: currentPhotoUri ara conté la URI amb l'esquema 'file://'
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

                    if (productIdToEdit == -1) {
                        dao.addProducte(nouProducte)
                    } else {
                        dao.updateProducte(nouProducte)
                    }

                    withContext(Dispatchers.Main) {
                        val missatge = if (productIdToEdit == -1) "Producte creat correctament!" else "Producte actualitzat correctament!"
                        Toast.makeText(this@ProductFormActivity, missatge, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Nom i Preu són obligatoris", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Funció per copiar la imatge a l'emmagatzematge privat de l'app (CORREGIT)
    // Retorna la RUTA ABSOLUTA (String) del fitxer copiat
    private fun copyUriToLocalFile(originalUri: Uri): String? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "prod_${streamerId}_${timeStamp}.jpg"
        val destinationFile = File(filesDir, fileName)

        try {
            contentResolver.openInputStream(originalUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    // ** CANVI CLAU **: Retornem la ruta absoluta, no la URI string.
                    return destinationFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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