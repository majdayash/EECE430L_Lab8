package com.majdayash.currencyexchange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import com.majdayash.currencyexchange.api.Authentication
import com.majdayash.currencyexchange.api.ExchangeService
import com.majdayash.currencyexchange.api.model.Transaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var menu: Menu? = null
    private var tabLayout: TabLayout? = null
    private var tabsViewPager: ViewPager2? = null
    private var fab: FloatingActionButton? = null
    private var transactionDialog: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Authentication.initialize(this)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        tabsViewPager = findViewById(R.id.tabsViewPager)

        tabLayout?.tabMode = TabLayout.MODE_FIXED
        tabLayout?.isInlineLabel = true

        tabsViewPager?.isUserInputEnabled = true
        val adapter = TabsPagerAdapter(supportFragmentManager, lifecycle)
        tabsViewPager?.adapter = adapter

        if (tabLayout != null && tabsViewPager != null) {
            TabLayoutMediator(tabLayout!!, tabsViewPager!!) { tab, position ->
                when (position) {
                    0 -> tab.text = "Exchange"
                    1 -> tab.text = "Transactions"
                }
            }.attach()
        }

        fab = findViewById(R.id.fab)
        fab?.setOnClickListener {
            showDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        setMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        setMenu()
        return true
    }

    private fun setMenu() {
        val currentMenu = menu ?: return
        currentMenu.clear()
        menuInflater.inflate(
            if (Authentication.getToken() == null) R.menu.menu_logged_out else R.menu.menu_logged_in,
            currentMenu
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.login) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.register) {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.logout) {
            Authentication.clearToken()
            setMenu()
        }
        return true
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
        val authorization = if (Authentication.getToken() != null) {
            "Bearer ${Authentication.getToken()}"
        } else {
            null
        }

        ExchangeService.exchangeApi().addTransaction(transaction, authorization)
            .enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    showMessage("Transaction added!")
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
