package com.example.deeplinkwebviewapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import android.widget.ArrayAdapter
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appSpinner: Spinner
    private lateinit var stageSpinner: Spinner
    private lateinit var fcmTokenTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // SharedPreferences initialisieren
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Zugriff auf die Views
        val blzEditText: EditText = findViewById(R.id.editTextBLZ)
        val usernameEditText: EditText = findViewById(R.id.editTextUsername)
        val pinEditText: EditText = findViewById(R.id.editTextPIN)
        val mkaEditText: EditText = findViewById(R.id.editTextMKALine)
        val deeplinkURLEditText: EditText = findViewById(R.id.editTextDeeplinkURL)
        fcmTokenTextView = findViewById(R.id.textViewFCMToken)  // FCM Token TextView

        // Spinner für SF Stage
        stageSpinner = findViewById(R.id.spinnerSFStage)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.sf_stage_array,  // String array für "Rhein", "Beta", "Prod"
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            stageSpinner.adapter = arrayAdapter
        }

        // Spinner für App Auswahl
        appSpinner = findViewById(R.id.spinnerApp)
        val appSpinnerAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.app_array,  // String array für die Apps
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            appSpinner.adapter = arrayAdapter
        }

        // Gespeicherte Daten laden
        blzEditText.setText(sharedPreferences.getString("BLZ", getString(R.string.default_blz)))
        usernameEditText.setText(sharedPreferences.getString("Username", ""))
        pinEditText.setText(sharedPreferences.getString("PIN", ""))
        mkaEditText.setText(sharedPreferences.getString("MKALine", getString(R.string.default_mka)))
        deeplinkURLEditText.setText(sharedPreferences.getString("DeeplinkURL", getString(R.string.default_deeplink_url)))

        // SF Stage Auswahl
        val stage = sharedPreferences.getString("SFStage", getString(R.string.default_stage))
        val stagePosition = adapter.getPosition(stage)
        stageSpinner.setSelection(stagePosition)

        // App Auswahl
        val app = sharedPreferences.getString("App", getString(R.string.default_app))
        val appPosition = appSpinnerAdapter.getPosition(app)
        appSpinner.setSelection(appPosition)

        // Speichern Button
        val saveButton: Button = findViewById(R.id.buttonSave)
        saveButton.setOnClickListener {
            // Werte speichern
            sharedPreferences.edit().apply {
                putString("BLZ", blzEditText.text.toString())
                putString("Username", usernameEditText.text.toString())
                putString("PIN", pinEditText.text.toString())
                putString("MKALine", mkaEditText.text.toString())
                putString("SFStage", stageSpinner.selectedItem.toString())
                putString("App", appSpinner.selectedItem.toString())
                putString("DeeplinkURL", deeplinkURLEditText.text.toString())
                apply()
            }
            Logger.log("Einstellungen gespeichert.") // Logger verwenden
        }

        // FCM-Token anzeigen
        val fcmToken = sharedPreferences.getString("FCMToken", "Token nicht verfügbar")
        fcmTokenTextView.text = "FCM Token: $fcmToken"
    }
}
