
package com.example.ubuntumentor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvWelcomeName =
            findViewById<TextView>(R.id.tvWelcomeName)

        val userName = sessionManager.getUserName()

        tvWelcomeName.text =
            getString(R.string.dashboard_welcome, userName)

        val mainView = findViewById<android.view.View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnOpportunities).setOnClickListener {
            startActivity(Intent(this, OpportunitiesActivity::class.java))
        }

        findViewById<Button>(R.id.btnSavedOpportunities).setOnClickListener {
            startActivity(
                Intent(this, SavedOpportunitiesActivity::class.java)
            )
        }

        findViewById<Button>(R.id.btnApplications).setOnClickListener {
            startActivity(
                Intent(this, ApplicationsActivity::class.java)
            )
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
