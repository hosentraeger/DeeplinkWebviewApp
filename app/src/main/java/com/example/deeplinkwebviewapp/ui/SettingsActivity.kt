package com.example.deeplinkwebviewapp.ui

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.viewmodel.SettingsViewModel
import com.example.deeplinkwebviewapp.viewmodel.SettingsViewModelFactory


class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        settingsViewModel = ViewModelProvider(this, SettingsViewModelFactory(application, sharedPreferences)).get(SettingsViewModel::class.java)

        // Zugriff auf die Views
        val blzEditText: EditText = findViewById(R.id.editTextBLZ)
        val usernameEditText: EditText = findViewById(R.id.editTextUsername)
        val personenNummerEditText: EditText = findViewById(R.id.editTextPersonennummer)
        val pinEditText: EditText = findViewById(R.id.editTextPIN)

        // Gespeicherte Daten anzeigen
        blzEditText.setText(settingsViewModel.getBLZ())
        usernameEditText.setText(settingsViewModel.getUsername())
        personenNummerEditText.setText(settingsViewModel.getPersonennummer())
        pinEditText.setText(settingsViewModel.getPIN())

        // Button zum Speichern der Einstellungen
        val saveButton: Button = findViewById(R.id.buttonSave)
        saveButton.setOnClickListener {
            settingsViewModel.saveSettings(
                blzEditText.text.toString(),
                usernameEditText.text.toString(),
                personenNummerEditText.text.toString(),
                pinEditText.text.toString()
            )
            Toast.makeText(this, "Einstellungen gespeichert", Toast.LENGTH_SHORT).show()
        }
    }
}
