package com.example.ubuntumentor

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)

        val languages = arrayOf("English", "isiZulu", "Afrikaans")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val prefs = getSharedPreferences("UbuntuMentorPrefs", MODE_PRIVATE)
        val savedLanguage = prefs.getString("language", "English")
        spinnerLanguage.setSelection(languages.indexOf(savedLanguage))

        btnSave.setOnClickListener {
            val selectedLanguage = spinnerLanguage.selectedItem.toString()
            prefs.edit().apply {
                putString("language", selectedLanguage)
                apply()
            }
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
        }
    }
}