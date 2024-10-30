package com.example.deeplinkwebviewapp.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.viewmodel.AccountSettingsViewModel
import com.example.deeplinkwebviewapp.viewmodel.AccountSettingsViewModelFactory


class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var accountSettingsViewModel: AccountSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        accountSettingsViewModel = ViewModelProvider(this, AccountSettingsViewModelFactory(application, sharedPreferences)).get(AccountSettingsViewModel::class.java)

        // Zugriff auf die Views
        val blzEditText: EditText = findViewById(R.id.editTextBLZ)
        val usernameEditText: EditText = findViewById(R.id.editTextUsername)
        val personenNummerEditText: EditText = findViewById(R.id.editTextPersonennummer)
        val pinEditText: EditText = findViewById(R.id.editTextPIN)

        // Gespeicherte Daten anzeigen
        blzEditText.setText(accountSettingsViewModel.getBLZ())
        usernameEditText.setText(accountSettingsViewModel.getUsername())
        personenNummerEditText.setText(accountSettingsViewModel.getPersonennummer())
        pinEditText.setText(accountSettingsViewModel.getPIN())

        // Button zum Speichern der Einstellungen
        val saveButton: Button = findViewById(R.id.buttonSave)
        saveButton.setOnClickListener {
            accountSettingsViewModel.saveSettings(
                blzEditText.text.toString(),
                usernameEditText.text.toString(),
                personenNummerEditText.text.toString(),
                pinEditText.text.toString()
            )
            Toast.makeText(this, "Einstellungen gespeichert", Toast.LENGTH_SHORT).show()
        }
    }
}
