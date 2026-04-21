package com.majdayash.currencyexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.majdayash.currencyexchange.api.ExchangeService
import com.majdayash.currencyexchange.api.model.ExchangeRates
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExchangeFragment : Fragment() {

    private var buyUsdTextView: TextView? = null
    private var sellUsdTextView: TextView? = null
    private var calculatorAmountInput: TextInputLayout? = null
    private var calculatorDirectionGroup: RadioGroup? = null
    private var calculatorResultTextView: TextView? = null

    private var usdToLbpRate: Float? = null
    private var lbpToUsdRate: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchRates()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_exchange, container, false)

        buyUsdTextView = view.findViewById(R.id.txtBuyUsdRate)
        sellUsdTextView = view.findViewById(R.id.txtSellUsdRate)
        calculatorAmountInput = view.findViewById(R.id.txtInptCalcAmount)
        calculatorDirectionGroup = view.findViewById(R.id.rdGrpCalcDirection)
        calculatorResultTextView = view.findViewById(R.id.txtCalcResult)

        val calculateButton = view.findViewById<MaterialButton>(R.id.btnCalculate)
        calculateButton.setOnClickListener {
            calculateExchange()
        }

        updateDisplayedRates()

        return view
    }

    private fun fetchRates() {
        ExchangeService.exchangeApi().getExchangeRates().enqueue(object : Callback<ExchangeRates> {
            override fun onResponse(call: Call<ExchangeRates>, response: Response<ExchangeRates>) {
                val responseBody = response.body()
                lbpToUsdRate = responseBody?.lbpToUsd
                usdToLbpRate = responseBody?.usdToLbp
                updateDisplayedRates()
            }

            override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                return
            }
        })
    }

    private fun updateDisplayedRates() {
        buyUsdTextView?.text = lbpToUsdRate?.toString() ?: "--"
        sellUsdTextView?.text = usdToLbpRate?.toString() ?: "--"
    }

    private fun calculateExchange() {
        val amount = calculatorAmountInput?.editText?.text?.toString()?.trim()?.toFloatOrNull()
        if (amount == null) {
            calculatorResultTextView?.text = "Enter a valid amount."
            return
        }

        val selectedDirection = calculatorDirectionGroup?.checkedRadioButtonId
        val resultText = when (selectedDirection) {
            R.id.rdBtnUsdToLbp -> {
                val rate = usdToLbpRate
                if (rate == null) "Rates not available yet." else {
                    val result = amount * rate
                    String.format("%.2f USD = %.2f LBP", amount, result)
                }
            }
            R.id.rdBtnLbpToUsd -> {
                val rate = lbpToUsdRate
                if (rate == null) "Rates not available yet." else {
                    val result = amount * rate
                    String.format("%.2f LBP = %.6f USD", amount, result)
                }
            }
            else -> "Select exchange direction."
        }

        calculatorResultTextView?.text = resultText
    }
}
