package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Adaptadors.CartAdapter
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.CartItemDisplay
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat // Necessari per al format de moneda
import java.lang.Thread // Necessari per al sleep simulat

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvSubtotal: TextView // NOU: Declaració afegida/corregida
    private var userId: Int = -1

    // Variables de classe accessibles a tot arreu
    private var llistaCompra: List<CartItemDisplay> = emptyList()
    private val df = DecimalFormat("#,##0.00€") // Format de moneda

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // --- CORRECCIONS DE REFERÈNCIA D'ID ---
        rvCart = findViewById(R.id.rv_cart_items) // FIX 1: ID canviada
        rvCart.layoutManager = LinearLayoutManager(this)
        tvTotal = findViewById(R.id.tv_total_value) // FIX 2: ID canviada
        tvSubtotal = findViewById(R.id.tv_subtotal_value) // FIX 3: Inicialització de la nova vista
        val btnCheckout = findViewById<Button>(R.id.btn_checkout)

        val prefs = getSharedPreferences("MerchStreamPrefs", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", -1)

        carregarCarro()

        btnCheckout.setOnClickListener {
            finalitzarCompra()
        }
    }

    private fun carregarCarro() {
        lifecycleScope.launch(Dispatchers.IO) {
            llistaCompra = AppSingleton.getInstance().db.carroDao().getCartItemsComplets(userId)

            // Calcul del total (només productes)
            val subtotal = llistaCompra.sumOf { it.preuTotal }
            val totalFinal = subtotal * 1.05 // 5% de taxa simulada

            withContext(Dispatchers.Main) {
                // Instanciem l'adaptador amb la interfície pels botons
                rvCart.adapter = CartAdapter(llistaCompra, object : CartAdapter.OnCartActionListener {

                    override fun onMinusClick(item: CartItemDisplay) {
                        gestionarResta(item)
                    }

                    override fun onDeleteClick(item: CartItemDisplay) {
                        gestionarEliminar(item)
                    }
                })

                // Actualitzem les TextViews amb el format elegant
                tvSubtotal.text = df.format(subtotal)
                tvTotal.text = df.format(totalFinal)


                if (llistaCompra.isEmpty()) {
                    Toast.makeText(this@CartActivity, "El carretó és buit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- FUNCIONS AUXILIARS DE GESTIÓ ---

    private fun gestionarResta(item: CartItemDisplay) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.quantitat > 1) {
                // Si en tenim més d'1, restem
                AppSingleton.getInstance().db.carroDao().decrementQuantitat(item.idItemCarro)
            } else {
                // Si en queda 1 i restem, l'eliminem del tot
                AppSingleton.getInstance().db.carroDao().deleteItemById(item.idItemCarro)
            }
            // IMPORTANT: Tornem a carregar la llista per veure els canvis
            carregarCarro()
        }
    }

    private fun gestionarEliminar(item: CartItemDisplay) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Eliminem directament
            AppSingleton.getInstance().db.carroDao().deleteItemById(item.idItemCarro)
            carregarCarro()
        }
    }

    // --- FUNCIONS DE PAGAMENT ---

    private fun finalitzarCompra() {
        val total = llistaCompra.sumOf { it.preuTotal } * 1.05 // Total amb taxa simulada

        if (total <= 0.0) {
            Toast.makeText(this, "No pots pagar un carretó buit", Toast.LENGTH_SHORT).show()
            return
        }

        // Diàleg de confirmació
        AlertDialog.Builder(this)
            .setTitle("Passarel·la de Pagament")
            // Usem el format elegant aquí també
            .setMessage("L'import total és de ${df.format(total)}.\n\nVols procedir al pagament?")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("PAGAR ARA") { _, _ ->
                processarPagament(total)
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun processarPagament(total: Double) {
        // Feedback immediat
        Toast.makeText(this, "Connectant amb el banc...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {

            // Buidem el carro de la BD
            AppSingleton.getInstance().db.carroDao().buidarCarro(userId)

            // Simulació d'espera (opcional, per realisme)
            Thread.sleep(1500)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CartActivity, "✅ Pagament de ${df.format(total)} rebut. Gràcies!", Toast.LENGTH_LONG).show()

                // Actualitzem la pantalla (es quedarà buida)
                llistaCompra = emptyList()
                tvSubtotal.text = df.format(0.0) // Resetejem subtotal
                tvTotal.text = df.format(0.0) // Resetejem total

                // Passem un listener buit per a l'adaptador buit
                rvCart.adapter = CartAdapter(llistaCompra, object : CartAdapter.OnCartActionListener {
                    override fun onMinusClick(item: CartItemDisplay) {}
                    override fun onDeleteClick(item: CartItemDisplay) {}
                })
            }
        }
    }
}