package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.os.Bundle
import android.view.View // Import necessari
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
import java.text.DecimalFormat
import java.lang.Thread

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvSubtotalLabel: TextView // FIX 1: DECLARACIÓ DE LA NOVA VISTA
    private var userId: Int = -1

    private var llistaCompra: List<CartItemDisplay> = emptyList()
    private val df = DecimalFormat("#,##0.00€")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // 1. INICIALITZACIÓ DE VISTES
        rvCart = findViewById(R.id.rv_cart_items)
        rvCart.layoutManager = LinearLayoutManager(this)
        tvTotal = findViewById(R.id.tv_total_value)
        tvSubtotal = findViewById(R.id.tv_subtotal_value)
        tvSubtotalLabel = findViewById(R.id.tv_subtotal_label) // FIX 2: INICIALITZACIÓ A ONCREATE
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

            // 1. Càlcul del Total ORIGINAL (Sense descomptes aplicats) per al Subtotal
            val totalOriginal = llistaCompra.sumOf { it.preuOriginal * it.quantitat }

            // 2. Càlcul del Total FINAL (Amb descomptes aplicats) per al Total
            val totalAmbOfertes = llistaCompra.sumOf { it.preuTotal }

            // 3. Comprovem si hi ha ofertes actives
            val hiHaOfertes = llistaCompra.any { it.esOferta }

            withContext(Dispatchers.Main) {
                // Instanciem l'adaptador
                rvCart.adapter = CartAdapter(llistaCompra, object : CartAdapter.OnCartActionListener {
                    override fun onMinusClick(item: CartItemDisplay) {
                        gestionarResta(item)
                    }
                    override fun onDeleteClick(item: CartItemDisplay) {
                        gestionarEliminar(item)
                    }
                })

                // --- GESTIÓ DE VISIBILITAT I VALORS ---
                if (hiHaOfertes) {
                    // Mostrar Subtotal com a preu original
                    tvSubtotalLabel.visibility = View.VISIBLE
                    tvSubtotal.visibility = View.VISIBLE
                    tvSubtotal.text = df.format(totalOriginal)

                    // Total és el preu final amb oferta
                    tvTotal.text = df.format(totalAmbOfertes)
                } else {
                    // Amagar Subtotal si no hi ha ofertes
                    tvSubtotalLabel.visibility = View.GONE
                    tvSubtotal.visibility = View.GONE

                    // El Total mostra el preu sense descompte (que és igual al preu final)
                    tvTotal.text = df.format(totalAmbOfertes)
                }

                if (llistaCompra.isEmpty()) {
                    // Si és buit, resetejar tot
                    tvTotal.text = df.format(0.0)
                    tvSubtotal.text = df.format(0.0)
                    tvSubtotalLabel.visibility = View.GONE
                    Toast.makeText(this@CartActivity, "El carretó és buit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- FUNCIONS AUXILIARS DE GESTIÓ (ARA FUNCIONEN CORRECTAMENT PERQUÈ CARREGARCARRRO NO PETA) ---

    private fun gestionarResta(item: CartItemDisplay) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.quantitat > 1) {
                AppSingleton.getInstance().db.carroDao().decrementQuantitat(item.idItemCarro)
            } else {
                AppSingleton.getInstance().db.carroDao().deleteItemById(item.idItemCarro)
            }
            carregarCarro() // Aquesta crida era la que petava
        }
    }

    private fun gestionarEliminar(item: CartItemDisplay) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppSingleton.getInstance().db.carroDao().deleteItemById(item.idItemCarro)
            carregarCarro() // Aquesta crida era la que petava
        }
    }

    // --- FUNCIONS DE PAGAMENT ---

    private fun finalitzarCompra() {
        // FIX: Eliminem el * 1.05
        val total = llistaCompra.sumOf { it.preuTotal }

        if (total <= 0.0) {
            Toast.makeText(this, "No pots pagar un carretó buit", Toast.LENGTH_SHORT).show()
            return
        }

        // Diàleg de confirmació
        AlertDialog.Builder(this)
            .setTitle("Passarel·la de Pagament")
            .setMessage("L'import total és de ${df.format(total)}.\n\nVols procedir al pagament?")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("PAGAR ARA") { _, _ ->
                processarPagament(total)
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun processarPagament(total: Double) {
        Toast.makeText(this, "Connectant amb el banc...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            AppSingleton.getInstance().db.carroDao().buidarCarro(userId)

            Thread.sleep(1500)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CartActivity, "✅ Pagament de ${df.format(total)} rebut. Gràcies!", Toast.LENGTH_LONG).show()

                // Actualitzem la pantalla
                llistaCompra = emptyList()
                tvSubtotal.text = df.format(0.0)
                tvTotal.text = df.format(0.0)
                tvSubtotalLabel.visibility = View.GONE

                rvCart.adapter = CartAdapter(llistaCompra, object : CartAdapter.OnCartActionListener {
                    override fun onMinusClick(item: CartItemDisplay) {}
                    override fun onDeleteClick(item: CartItemDisplay) {}
                })
            }
        }
    }
}