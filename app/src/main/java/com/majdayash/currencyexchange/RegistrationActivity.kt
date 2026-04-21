package com.majdayash.currencyexchange

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.majdayash.currencyexchange.api.Authentication
import com.majdayash.currencyexchange.api.ExchangeService
import com.majdayash.currencyexchange.api.model.Token
import com.majdayash.currencyexchange.api.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationActivity : AppCompatActivity() {

    private var usernameEditText: TextInputLayout? = null
    private var passwordEditText: TextInputLayout? = null
    private var submitButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        usernameEditText = findViewById(R.id.txtInptUsername)
        passwordEditText = findViewById(R.id.txtInptPassword)
        submitButton = findViewById(R.id.btnSubmit)

        submitButton?.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val user = User().apply {
            username = usernameEditText?.editText?.text.toString().trim()
            password = passwordEditText?.editText?.text.toString().trim()
        }

        if (user.username.isNullOrEmpty() || user.password.isNullOrEmpty()) {
            Snackbar.make(submitButton as View, "Enter username and password.", Snackbar.LENGTH_LONG).show()
            return
        }

        ExchangeService.exchangeApi().addUser(user).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                Snackbar.make(submitButton as View, "Could not create account.", Snackbar.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                ExchangeService.exchangeApi().authenticate(user).enqueue(object : Callback<Token> {
                    override fun onFailure(call: Call<Token>, t: Throwable) {
                        Snackbar.make(submitButton as View, "Account created, login failed.", Snackbar.LENGTH_LONG).show()
                    }

                    override fun onResponse(call: Call<Token>, response: Response<Token>) {
                        val token = response.body()?.token
                        if (token != null) {
                            Authentication.saveToken(token)
                            Snackbar.make(submitButton as View, "Account created.", Snackbar.LENGTH_LONG).show()
                            onCompleted()
                        } else {
                            Snackbar.make(submitButton as View, "Invalid auth response.", Snackbar.LENGTH_LONG).show()
                        }
                    }
                })
            }
        })
    }

    private fun onCompleted() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
