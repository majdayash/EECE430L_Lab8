package com.majdayash.currencyexchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.majdayash.currencyexchange.api.ExchangeService
import com.majdayash.currencyexchange.api.model.ExchangeRates
import com.majdayash.currencyexchange.api.model.Transaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var buyUsdTextView: TextView? = null
    private var sellUsdTextView: TextView? = null
    private var fab: FloatingActionButton? = null
    private var transactionDialog: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buyUsdTextView = findViewById(R.id.txtBuyUsdRate)
        sellUsdTextView = findViewById(R.id.txtSellUsdRate)

        fab = findViewById(R.id.fab)
        fab?.setOnClickListener {
            showDialog()
        }

        fetchRates()
    }

    private fun fetchRates() {
        ExchangeService.exchangeApi().getExchangeRates().enqueue(object : Callback<ExchangeRates> {
            override fun onResponse(call: Call<ExchangeRates>, response: Response<ExchangeRates>) {
                val responseBody: ExchangeRates? = response.body()
                val buyRate = responseBody?.lbpToUsd
                val sellRate = responseBody?.usdToLbp

                if (buyRate != null) {
                    buyUsdTextView?.text = buyRate.toString()
                }

                if (sellRate != null) {
                    sellUsdTextView?.text = sellRate.toString()
                }
            }

            override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                showMessage("Could not fetch exchange rates.")
            }
        })
    }

    private fun showDialog() {
        transactionDialog = LayoutInflater.from(this)
            .inflate(R.layout.dialog_transaction, null, false)

        MaterialAlertDialogBuilder(this).setView(transactionDialog)
            .setTitle("Add Transaction")
            .setMessage("Enter transaction details")
            .setPositiveButton("Add") { dialog, _ ->
                val usdAmountText = transactionDialog?.findViewById<TextInputLayout>(R.id.txtInptUsdAmount)
                    ?.editText?.text?.toString()?.trim()

                val lbpAmountText = transactionDialog?.findViewById<TextInputLayout>(R.id.txtInptLbpAmount)
                    ?.editText?.text?.toString()?.trim()

                val usdAmount = usdAmountText?.toFloatOrNull()
                val lbpAmount = lbpAmountText?.toFloatOrNull()

                if (usdAmount == null || lbpAmount == null) {
                    showMessage("Enter valid USD and LBP amounts.")
                    dialog.dismiss()
                    return@setPositiveButton
                }

                val transactionType = transactionDialog?.findViewById<RadioGroup>(R.id.rdGrpTransactionType)
                    ?.checkedRadioButtonId

                val transaction = Transaction().apply {
                    this.usdAmount = usdAmount
                    this.lbpAmount = lbpAmount
                    this.usdToLbp = transactionType == R.id.rdBtnSellUsd
                }

                addTransaction(transaction)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addTransaction(transaction: Transaction) {
        ExchangeService.exchangeApi().addTransaction(transaction).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                showMessage("Transaction added!")
                fetchRates()
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                showMessage("Could not add transaction.")
            }
        })
    }

    private fun showMessage(message: String) {
        val anchor = fab ?: findViewById(android.R.id.content)
        Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show()
    }
}
