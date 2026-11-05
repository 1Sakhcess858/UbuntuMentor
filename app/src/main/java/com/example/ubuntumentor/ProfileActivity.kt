
package com.example.ubuntumentor

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.initialize(this)

        createProfileScreen()
        loadUserProfile()
    }

    private fun createProfileScreen() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "My Profile"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        nameText = TextView(this).apply {
            text = "Name: Loading..."
            textSize = 20f
            setPadding(0, 40, 0, 20)
        }

        emailText = TextView(this).apply {
            text = "Email: Loading..."
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }

        statusText = TextView(this).apply {
            text = "Loading your profile..."
            textSize = 15f
            setPadding(0, 20, 0, 20)
        }

        progressBar = ProgressBar(this)

        val backButton = Button(this).apply {
            text = "Back to Dashboard"
            setOnClickListener {
                finish()
            }
        }

        layout.addView(title)
        layout.addView(nameText)
        layout.addView(emailText)
        layout.addView(progressBar)
        layout.addView(statusText)
        layout.addView(backButton)

        setContentView(layout)
    }

    private fun loadUserProfile() {

        progressBar.visibility = android.view.View.VISIBLE
        statusText.text = "Loading your profile..."

        ApiClient.apiService.getCurrentUser()
            .enqueue(object : Callback<UserProfile> {

                override fun onResponse(
                    call: Call<UserProfile>,
                    response: Response<UserProfile>
                ) {

                    progressBar.visibility = android.view.View.GONE

                    if (response.isSuccessful && response.body() != null) {

                        val profile = response.body()!!

                        nameText.text = "Name: ${profile.name}"
                        emailText.text = "Email: ${profile.email}"

                        statusText.text = "Your profile loaded successfully."

                    } else {

                        statusText.text =
                            "Could not load profile. Server response: ${response.code()}"

                        Toast.makeText(
                            this@ProfileActivity,
                            "Profile request failed: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<UserProfile>,
                    t: Throwable
                ) {

                    progressBar.visibility = android.view.View.GONE

                    statusText.text = "Connection error: ${t.message}"

                    Toast.makeText(
                        this@ProfileActivity,
                        "Could not connect to the server.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
