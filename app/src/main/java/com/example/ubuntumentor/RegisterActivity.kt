
package com.example.ubuntumentor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize session manager
        sessionManager = SessionManager(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val request = AuthRequest(
                name = name,
                email = email,
                password = password
            )

            ApiClient.apiService.registerUser(request)
                .enqueue(object : Callback<AuthResponse> {

                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>
                    ) {

                        if (response.isSuccessful) {

                            val user = response.body()

                            // SAVE THE NEW USER SESSION
                            if (user?.token.isNullOrBlank()) {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Registration succeeded, but no authentication token was received",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            sessionManager.saveSession(
                                userId = user?.userId?.toString() ?: "",
                                name = name,
                                authType = "API",
                                token = user?.token
                            )

                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration Successful!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Open Dashboard
                            val intent = Intent(
                                this@RegisterActivity,
                                DashboardActivity::class.java
                            )

                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()

                        } else {

                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration Failed. Email might already exist.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<AuthResponse>,
                        t: Throwable
                    ) {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}
