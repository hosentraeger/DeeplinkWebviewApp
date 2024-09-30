package com.example.deeplinkwebviewapp.ui

import android.content.Context
import com.example.deeplinkwebviewapp.viewmodel.SettingsViewModel
import com.example.deeplinkwebviewapp.viewmodel.SettingsViewModelFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.deeplinkwebviewapp.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var appSpinner: Spinner
    private lateinit var stageSpinner: Spinner
    private lateinit var fcmTokenTextView: TextView
    private lateinit var deviceIdTextView: TextView
    private lateinit var servletUrlTextView: TextView
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
        val mkaEditText: EditText = findViewById(R.id.editTextMKALine)
        val deeplinkURLEditText: EditText = findViewById(R.id.editTextDeeplinkURL)
        fcmTokenTextView = findViewById(R.id.textViewFCMToken)
        deviceIdTextView = findViewById(R.id.textViewDeviceId)
        servletUrlTextView = findViewById(R.id.textViewServletUrl)

        // Spinner initialisieren
        stageSpinner = findViewById(R.id.spinnerSFStage)
        appSpinner = findViewById(R.id.spinnerApp)

        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.sf_stage_array,
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            stageSpinner.adapter = arrayAdapter
        }

        // Gespeicherte Daten anzeigen
        blzEditText.setText(settingsViewModel.getBLZ())
        usernameEditText.setText(settingsViewModel.getUsername())
        personenNummerEditText.setText(settingsViewModel.getPersonennummer())
        pinEditText.setText(settingsViewModel.getPIN())
        mkaEditText.setText(settingsViewModel.getMKALine())
        deeplinkURLEditText.setText(settingsViewModel.getDeeplinkURL())

        // Stage Spinner
        val stage = settingsViewModel.getSFStage()
        val stagePosition = adapter.getPosition(stage)
        stageSpinner.setSelection(stagePosition)

        // App Spinner
        val appAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.app_array,
            android.R.layout.simple_spinner_item
        )
        val app = settingsViewModel.getApp()
        val appPosition = appAdapter.getPosition(app)
        appSpinner.setSelection(appPosition)

        // Button zum Speichern der Einstellungen
        val saveButton: Button = findViewById(R.id.buttonSave)
        saveButton.setOnClickListener {
            settingsViewModel.saveSettings(
                blzEditText.text.toString(),
                usernameEditText.text.toString(),
                personenNummerEditText.text.toString(),
                pinEditText.text.toString(),
                mkaEditText.text.toString(),
                stageSpinner.selectedItem.toString(),
                appSpinner.selectedItem.toString(),
                deeplinkURLEditText.text.toString()
            )
            Toast.makeText(this, "Einstellungen gespeichert", Toast.LENGTH_SHORT).show()
        }

        // Button zum Regenerieren der Device-ID
        val regenerateButton: Button = findViewById(R.id.buttonRegenerate)
        regenerateButton.setOnClickListener {
            settingsViewModel.regenerateDeviceId()
            deviceIdTextView.text = settingsViewModel.deviceData.device_id
        }

        // FCM Token anzeigen
        fcmTokenTextView.text = settingsViewModel.deviceData.push_id
        deviceIdTextView.text = settingsViewModel.deviceData.device_id
        servletUrlTextView.text = "<servlet url>"
    }
}
