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

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private var userId: Int = -1

    // --- CORRECCIÓ: Variable de classe perquè sigui accessible a tot arreu ---
    private var llistaCompra: List<CartItemDisplay> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        rvCart = findViewById(R.id.rv_cart)
        rvCart.layoutManager = LinearLayoutManager(this)
        tvTotal = findViewById(R.id.tv_total_price)
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
            // Guardem el resultat a la variable de classe 'llistaCompra'
            llistaCompra = AppSingleton.getInstance().db.carroDao().getCartItemsComplets(userId)

            val sumaTotal = llistaCompra.sumOf { it.preuTotal }

            withContext(Dispatchers.Main) {
                if (llistaCompra.isEmpty()) {
                    Toast.makeText(this@CartActivity, "El carretó està buit", Toast.LENGTH_SHORT).show()
                }

                rvCart.adapter = CartAdapter(llistaCompra)
                tvTotal.text = "Total: $sumaTotal €"
            }
        }
    }

    private fun finalitzarCompra() {
        // Calculem el total usant la variable de classe 'llistaCompra'
        val total = llistaCompra.sumOf { it.preuTotal }

        if (total <= 0.0) {
            Toast.makeText(this, "No pots pagar un carretó buit", Toast.LENGTH_SHORT).show()
            return
        }

        // Diàleg de confirmació (Simulació de Passarel·la de Pagament)
        AlertDialog.Builder(this)
            .setTitle("Passarel·la de Pagament")
            .setMessage("L'import total és de $total €.\n\nVols procedir al pagament?")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("PAGAR ARA") { _, _ ->
                ferPagamentReal(total)
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun ferPagamentReal(importPagat: Double) {
        // Simulació de "Processant..."
        Toast.makeText(this, "Connectant amb el banc...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            // Aquí podríem guardar la comanda a un historial...

            // 1. Buidem el carro
            AppSingleton.getInstance().db.carroDao().buidarCarro(userId)

            // 2. Esperem 2 segons per donar realisme
            Thread.sleep(2000)

            withContext(Dispatchers.Main) {
                // 3. Feedback a l'usuari
                Toast.makeText(this@CartActivity, "✅ Pagament de $importPagat € acceptat!", Toast.LENGTH_LONG).show()

                // 4. Refresquem la pantalla (ara buida)
                llistaCompra = emptyList()
                rvCart.adapter = CartAdapter(llistaCompra)
                tvTotal.text = "Total: 0.0 €"

                // Opcional: Tancar l'activitat per tornar a la botiga
                // finish()
            }
        }
    }

    private fun processarPagament(total: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Aquí podries guardar la comanda en una taula 'orders_table' si volguessis

            // 2. Buidem el carro perquè ja s'ha pagat
            AppSingleton.getInstance().db.carroDao().buidarCarro(userId)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CartActivity, "✅ Pagament de ${total}€ rebut. Gràcies!", Toast.LENGTH_LONG).show()

                // Actualitzem la pantalla (es quedarà buida)
                llistaCompra = emptyList()
                rvCart.adapter = CartAdapter(llistaCompra)
                tvTotal.text = "Total: 0.0 €"

                // Opcional: Tancar la pantalla i tornar a la botiga
                // finish()
            }
        }
    }
}