package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.os.Bundle
import android.view.View
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
    private lateinit var tvSubtotalLabel: TextView
    private var userId: Int = -1

    private var llistaCompra: List<CartItemDisplay> = emptyList()

    private val df by lazy { DecimalFormat("#,##0.00${getString(R.string.euro_suffix)}") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        rvCart = findViewById(R.id.rv_cart_items)
        rvCart.layoutManager = LinearLayoutManager(this)
        tvTotal = findViewById(R.id.tv_total_value)
        tvSubtotal = findViewById(R.id.tv_subtotal_value)
        tvSubtotalLabel = findViewById(R.id.tv_subtotal_label)
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

            val totalOriginal = llistaCompra.sumOf { it.preuOriginal * it.quantitat }
            val totalAmbOfertes = llistaCompra.sumOf { it.preuTotal }
            val hiHaOfertes = llistaCompra.any { it.esOferta }

            withContext(Dispatchers.Main) {
                rvCart.adapter = CartAdapter(llistaCompra, object : CartAdapter.OnCartActionListener {
                    override fun onMinusClick(item: CartItemDisplay) {
                        gestionarResta(item)
                    }
                    override fun onDeleteClick(item: CartItemDisplay) {
                        gestionarEliminar(item)
                    }
                })

                if (hiHaOfertes) {
                    tvSubtotalLabel.visibility = View.VISIBLE
                    tvSubtotal.visibility = View.VISIBLE
                    tvSubtotal.text = df.format(totalOriginal)
                    tvTotal.text = df.format(totalAmbOfertes)
                } else {
                    tvSubtotalLabel.visibility = View.GONE
                    tvSubtotal.visibility = View.GONE
                    tvTotal.text = df.format(totalAmbOfertes)
                }

                if (llistaCompra.isEmpty()) {
                    tvTotal.text = df.format(0.0)
                    tvSubtotal.text = df.format(0.0)
                    tvSubtotalLabel.visibility = View.GONE
                    Toast.makeText(this@CartActivity, getString(R.string.msg_cart_empty), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun gestionarResta(item: CartItemDisplay) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.quantitat > 1) {
                AppSingleton.getInstance().db.carroDao().decrementQuantitat(item.idItemCarro)
            } else {
                AppSingleton.getInstance().db.carroDao().deleteItemById(item.idItemCarro)
            }
            carregarCarro()
        }
    }

    private fun gestionarEliminar(item: CartItemDisplay) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppSingleton.getInstance().db.carroDao().deleteItemById(item.idItemCarro)
            carregarCarro()
        }
    }

    private fun finalitzarCompra() {
        val total = llistaCompra.sumOf { it.preuTotal }

        if (total <= 0.0) {
            Toast.makeText(this, getString(R.string.error_pay_empty), Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)

            .setTitle(getString(R.string.payment_title))
            .setMessage(getString(R.string.payment_msg_format, df.format(total)))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(getString(R.string.btn_pay_now)) { _, _ ->
                processarPagament(total)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun processarPagament(total: Double) {

        Toast.makeText(this, getString(R.string.msg_connecting_bank), Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            AppSingleton.getInstance().db.carroDao().buidarCarro(userId)

            Thread.sleep(1500)

            withContext(Dispatchers.Main) {

                Toast.makeText(this@CartActivity, getString(R.string.msg_payment_success_format, df.format(total)), Toast.LENGTH_LONG).show()

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