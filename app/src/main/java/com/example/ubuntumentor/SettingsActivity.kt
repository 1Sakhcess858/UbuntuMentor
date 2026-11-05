
package com.example.ubuntumentor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class SettingsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            openLoginScreen()
            return
        }

        setContentView(R.layout.activity_settings)

        val switchNotifications =
            findViewById<SwitchCompat>(R.id.switchNotifications)

        val btnLogout =
            findViewById<Button>(R.id.btnLogout)

        val languageSpinner =
            findViewById<Spinner>(R.id.languageSpinner)

        // LANGUAGE OPTIONS
        val languageNames = listOf(
            getString(R.string.language_english),
            getString(R.string.language_afrikaans),
            getString(R.string.language_zulu)
        )

        val languageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languageNames
        )

        languageAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = languageAdapter

        // FIND CURRENT LANGUAGE
        val savedLanguage =
            AppCompatDelegate.getApplicationLocales().toLanguageTags()

        val currentLanguage = if (savedLanguage.isNotEmpty()) {
            savedLanguage.substringBefore("-").substringBefore(",")
        } else {
            resources.configuration.locales[0].language
        }

        val languagePosition = when (currentLanguage) {
            "af" -> 1
            "zu" -> 2
            else -> 0
        }

        languageSpinner.setSelection(languagePosition, false)

        // CHANGE LANGUAGE
        languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val languageCode = when (position) {
                        1 -> "af"
                        2 -> "zu"
                        else -> "en"
                    }

                    val currentCode =
                        AppCompatDelegate.getApplicationLocales()
                            .toLanguageTags()
                            .substringBefore("-")
                            .substringBefore(",")

                    if (currentCode != languageCode) {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(languageCode)
                        )
                    }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action required
                }
            }

        // NOTIFICATION SETTINGS
        switchNotifications.isChecked =
            sessionManager.areNotificationsEnabled()

        switchNotifications.setOnCheckedChangeListener { _, enabled ->
            sessionManager.setNotificationsEnabled(enabled)
        }

        // LOG OUT
        btnLogout.setOnClickListener {

            val authType = sessionManager.getAuthType()

            sessionManager.logout()
            FirebaseAuth.getInstance().signOut()

            if (authType == "GOOGLE") {

                val gso = GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                    .requestIdToken(
                        "316104145736-cn78r0r03rgnrhvigv4o5qa2cuq5tg65.apps.googleusercontent.com"
                    )
                    .requestEmail()
                    .build()

                val googleSignInClient =
                    GoogleSignIn.getClient(this, gso)

                googleSignInClient.signOut().addOnCompleteListener {
                    openLoginScreen()
                }

            } else {
                openLoginScreen()
            }
        }
    }

    private fun openLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
