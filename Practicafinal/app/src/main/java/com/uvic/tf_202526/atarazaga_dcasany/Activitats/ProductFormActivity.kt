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
    // ELIMINAT: private var tempPhotoUri: Uri? = null
    private var currentTempPhotoPath: String? = null // RUTA ABSOLUTA DEL FITXER TEMPORAL DE LA CÀMERA
    private var streamerId: Int = -1
    private var productIdToEdit: Int = -1

    // LAUNCHER CÀMERA (Corregit per utilitzar la ruta File)
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentTempPhotoPath?.let { tempPath ->
                lifecycleScope.launch(Dispatchers.IO) {

                    // 1. Copiem el fitxer des de la ruta absoluta temporal a la ruta persistent
                    val localPathString = copyFileToLocalFile(tempPath)

                    // 2. Si la còpia ha anat bé, carreguem la imatge al Main Thread
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
                            Toast.makeText(this@ProductFormActivity, "Imatge de càmera copiada localment.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ProductFormActivity, "Error al copiar la imatge de la càmera.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        // Netejar la referència a la ruta temporal
        currentTempPhotoPath = null
    }

    // LAUNCHER GALERIA (Manté la lògica original de ContentResolver)
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

                // +++ DEPURACIÓ: LOGS D'INICI +++
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

                        // +++ CARREGAR IMATGE EXISTENT (VERSIÓ CORREGIDA) +++
                        if (!prod.imatgeUri.isNullOrEmpty()) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val savedUri = Uri.parse(prod.imatgeUri)
                                    Log.d("DEBUG_IMATGE", "Intentant obrir URI: $savedUri, Scheme: ${savedUri.scheme}")

                                    var inputStream: java.io.InputStream? = null

                                    // Estratègia 1: ContentResolver per a URIs content://
                                    if (savedUri.scheme == "content") {
                                        try {
                                            inputStream = contentResolver.openInputStream(savedUri)
                                            Log.d("DEBUG_IMATGE", "Obert amb ContentResolver")
                                        } catch (e: SecurityException) {
                                            Log.e("DEBUG_IMATGE", "Permís denegat per contentResolver: ${e.message}")
                                        } catch (e: Exception) {
                                            Log.e("DEBUG_IMATGE", "Error amb contentResolver: ${e.message}")
                                        }
                                    }

                                    // Estratègia 2: FileInputStream per a URIs file://
                                    if (inputStream == null && savedUri.scheme == "file") {
                                        try {
                                            val file = File(savedUri.path ?: "")
                                            if (file.exists()) {
                                                inputStream = FileInputStream(file)
                                                Log.d("DEBUG_IMATGE", "Obert amb FileInputStream")
                                            } else {
                                                Log.e("DEBUG_IMATGE", "Fitxer no existeix: ${file.absolutePath}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("DEBUG_IMATGE", "Error amb FileInputStream: ${e.message}")
                                        }
                                    }

                                    // Processar InputStream si existeix
                                    inputStream?.use { stream ->
                                        val bitmap = BitmapFactory.decodeStream(stream)

                                        withContext(Dispatchers.Main) {
                                            Log.d("DEBUG_MAIN", "Estic al thread principal")
                                            if (bitmap != null) {
                                                Log.d("DEBUG_MAIN", "Bitmap dimensions: ${bitmap.width}x${bitmap.height}")
                                                ivPreview.setImageBitmap(bitmap)
                                                ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                                                ivPreview.requestLayout()
                                                ivPreview.invalidate()
                                                currentPhotoUri = savedUri
                                                Log.d("DEBUG_LAYOUT", "ImageView dimensions: ${ivPreview.width}x${ivPreview.height}")
                                                Log.d("DEBUG_IMATGE", "Imatge carregada correctament des de: $savedUri")
                                            } else {
                                                Log.d("DEBUG_IMATGE", "Bitmap és null després de decodeStream")
                                                ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                            }
                                        }
                                    } ?: run {
                                        // Si no es pot obrir l'InputStream
                                        withContext(Dispatchers.Main) {
                                            ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                            Log.e("DEBUG_IMATGE", "No s'ha pogut obrir InputStream per a: $savedUri")
                                        }
                                    }

                                } catch (e: SecurityException) {
                                    Log.e("DEBUG_IMATGE", "Permís denegat per llegir la URI: ${e.message}")
                                    withContext(Dispatchers.Main) {
                                        ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                    }
                                } catch (e: Exception) {
                                    Log.e("DEBUG_IMATGE", "Error general carregant imatge: ${e.message}")
                                    withContext(Dispatchers.Main) {
                                        ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                                    }
                                }
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

                val uriString = currentPhotoUri?.toString()

                // +++ DEPURACIÓ: LOGS DE GUARDAR +++
                Log.d("DEBUG_IMATGE", "URI a guardar: $uriString")

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
                        Log.d("DEBUG_IMATGE", "Producte NOU guardat amb URI: $uriString")
                    } else {
                        dao.updateProducte(nouProducte)
                        Log.d("DEBUG_IMATGE", "Producte ACTUALITZAT amb URI: $uriString")
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

    // Funció per copiar la imatge des d'una URI content:// (ús Galeria)
    private fun copyUriToLocalFile(originalUri: Uri): String? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "prod_${streamerId}_${timeStamp}.jpg"
        val destinationFile = File(filesDir, fileName)

        try {
            contentResolver.openInputStream(originalUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    Log.d("GalleryCopy", "Fitxer copiat correctament des de Galeria. Mida: ${destinationFile.length()} bytes")
                    // Retornem la ruta absoluta
                    return destinationFile.absolutePath
                }
            }
        } catch (e: Exception) {
            Log.e("GalleryCopy", "Error copiant fitxer des de URI: ${e.message}")
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
            // Guarda la ruta absoluta per a ús posterior (per a la còpia de fitxers)
            currentTempPhotoPath = it.absolutePath

            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            cameraLauncher.launch(photoURI)
        }
    }

    // Funció per COPIAR la imatge des d'una RUTA ABSOLUTA (ús Càmera)
    private fun copyFileToLocalFile(sourcePath: String): String? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sourceFile = File(sourcePath)
        val fileName = "prod_${streamerId}_${timeStamp}.jpg"
        val destinationFile = File(filesDir, fileName)

        Log.d("CameraCopy", "Intentant copiar fitxer des de: $sourcePath")

        if (!sourceFile.exists()) {
            Log.e("CameraCopy", "Fitxer d'origen temporal no trobat a: $sourcePath")
            return null
        }

        try {
            FileInputStream(sourceFile).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)

                    val fileSize = destinationFile.length()
                    // Després de copiar, esborrem el fitxer temporal!
                    val deleted = sourceFile.delete()
                    Log.d("CameraCopy", "Fitxer copiat correctament. Mida: $fileSize bytes. Esborrat temporal: $deleted")

                    // Si el fitxer es va copiar però té mida zero
                    if (fileSize == 0L) {
                        Log.e("CameraCopy", "ADVERTÈNCIA: El fitxer copiat té una mida de 0 bytes.")
                    }

                    return destinationFile.absolutePath
                }
            }
        } catch (e: Exception) {
            Log.e("CameraCopy", "Error copiant fitxer: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    private fun crearFitxerImatge(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // El fitxer es crea a l'emmagatzematge extern específic de l'aplicació, al subdirectori Pictures
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
}