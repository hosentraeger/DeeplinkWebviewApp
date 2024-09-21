package com.example.deeplinkwebviewapp

import android.content.Context
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
        val logTextView: TextView = findViewById(R.id.textViewLog)

        // Spinner für SF Stage
        val stageSpinner: Spinner = findViewById(R.id.spinnerSFStage)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.sf_stage_array,  // String array für "Rhein", "Beta", "Prod"
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            stageSpinner.adapter = arrayAdapter
        }

        // Gespeicherte Daten laden
        blzEditText.setText(sharedPreferences.getString("BLZ", getString(R.string.default_blz)))
        usernameEditText.setText(sharedPreferences.getString("Username", ""))
        pinEditText.setText(sharedPreferences.getString("PIN", ""))
        mkaEditText.setText(sharedPreferences.getString("MKALine", getString(R.string.default_mka)))
        deeplinkURLEditText.setText(sharedPreferences.getString("DeeplinkURL", getString(R.string.default_deeplinks_url)))

        // SF Stage Auswahl
        val stage = sharedPreferences.getString("SFStage", getString(R.string.default_stage))
        val stagePosition = adapter.getPosition(stage)
        stageSpinner.setSelection(stagePosition)

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
                putString("DeeplinkURL", deeplinkURLEditText.text.toString())
                apply()
            }
        }

        // Log Button
        val logButton: Button = findViewById(R.id.buttonLog)
        logButton.setOnClickListener {
            // Log-Werte anzeigen
            val logText = """
                BLZ: ${blzEditText.text}
                Username: ${usernameEditText.text}
                PIN: ${pinEditText.text}
                SF Stage: ${stageSpinner.selectedItem}
                MKA-Linie: ${mkaEditText.text}
                Deeplink-URL: ${deeplinkURLEditText.text}
            """.trimIndent()
            logTextView.text = logText
        }
    }
}
