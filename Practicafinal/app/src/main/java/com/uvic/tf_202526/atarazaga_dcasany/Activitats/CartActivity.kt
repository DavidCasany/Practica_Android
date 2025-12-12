package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uvic.tf_202526.atarazaga_dcasany.Adaptadors.CartAdapter
import com.uvic.tf_202526.atarazaga_dcasany.R
import com.uvic.tf_202526.atarazaga_dcasany.Apps.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private var userId: Int = -1

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
            val items = AppSingleton.getInstance().db.carroDao().getCartItemsComplets(userId)

            // Calculem el total sumant totes les línies
            val sumaTotal = items.sumOf { it.preuTotal }

            withContext(Dispatchers.Main) {
                if (items.isEmpty()) {
                    Toast.makeText(this@CartActivity, "El carretó està buit", Toast.LENGTH_SHORT).show()
                }

                rvCart.adapter = CartAdapter(items)
                tvTotal.text = "Total: $sumaTotal €"
            }
        }
    }

    private fun finalitzarCompra() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Aquí podríem guardar una "Comanda" (Order) en una altra taula si volguéssim historial
            // Per ara, simplement buidem el carro simulant que s'ha comprat
            AppSingleton.getInstance().db.carroDao().buidarCarro(userId)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CartActivity, "Compra realitzada amb èxit!", Toast.LENGTH_LONG).show()
                finish() // Tanquem la pantalla del carro
            }
        }
    }
}