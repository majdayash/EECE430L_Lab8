package com.majdayash.currencyexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.majdayash.currencyexchange.api.Authentication
import com.majdayash.currencyexchange.api.ExchangeService
import com.majdayash.currencyexchange.api.model.Transaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionsFragment : Fragment() {

    class TransactionAdapter(
        private val inflater: LayoutInflater,
        private val dataSource: List<Transaction>
    ) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = inflater.inflate(R.layout.item_transaction, parent, false)
            val transaction = dataSource[position]

            val type = if (transaction.usdToLbp == true) "Sell USD" else "Buy USD"
            view.findViewById<TextView>(R.id.txtTransactionType).text = type
            view.findViewById<TextView>(R.id.txtTransactionAmounts).text =
                "USD ${transaction.usdAmount ?: 0f} | LBP ${transaction.lbpAmount ?: 0f}"
            view.findViewById<TextView>(R.id.txtTransactionMeta).text =
                "ID: ${transaction.id ?: "-"}   Date: ${transaction.addedDate ?: "-"}"

            return view
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return dataSource[position].id?.toLong() ?: 0L
        }

        override fun getCount(): Int {
            return dataSource.size
        }
    }

    private var listview: ListView? = null
    private var transactions: ArrayList<Transaction> = ArrayList()
    private var adapter: TransactionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchTransactions()
    }

    override fun onResume() {
        super.onResume()
        fetchTransactions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)
        listview = view.findViewById(R.id.listview)
        adapter = TransactionAdapter(inflater, transactions)
        listview?.adapter = adapter
        return view
    }

    private fun fetchTransactions() {
        val token = Authentication.getToken()
        if (token == null) {
            transactions.clear()
            adapter?.notifyDataSetChanged()
            return
        }

        ExchangeService.exchangeApi().getTransactions("Bearer $token")
            .enqueue(object : Callback<List<Transaction>> {
                override fun onFailure(call: Call<List<Transaction>>, t: Throwable) {
                    return
                }

                override fun onResponse(
                    call: Call<List<Transaction>>,
                    response: Response<List<Transaction>>
                ) {
                    val body = response.body() ?: return
                    transactions.clear()
                    transactions.addAll(body)
                    adapter?.notifyDataSetChanged()
                }
            })
    }
}
