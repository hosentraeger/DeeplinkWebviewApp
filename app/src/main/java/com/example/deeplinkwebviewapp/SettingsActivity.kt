package com.example.deeplinkwebviewapp

import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import android.widget.ArrayAdapter
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {

    private lateinit var blzEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var pinEditText: EditText
    private lateinit var mkaEditText: EditText
    private lateinit var deeplinkURLEditText: EditText
    private lateinit var stageSpinner: Spinner
    private lateinit var logTextView: TextView

    // Statisches Log, das außerhalb der SettingsActivity aktualisiert werden kann
    companion object {
        private var logContent: StringBuilder = StringBuilder()

        // Methode, um dem Log etwas hinzuzufügen
        fun appendLog(logEntry: String) {
            logContent.append(logEntry).append("\n")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Zugriff auf die Views
        blzEditText = findViewById(R.id.editTextBLZ)
        usernameEditText = findViewById(R.id.editTextUsername)
        pinEditText = findViewById(R.id.editTextPIN)
        mkaEditText = findViewById(R.id.editTextMKALine)
        deeplinkURLEditText = findViewById(R.id.editTextDeeplinkURL)
        logTextView = findViewById(R.id.textViewLog)

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

        // Gespeicherte Daten laden
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        blzEditText.setText(sharedPreferences.getString("BLZ", getString(R.string.default_blz)))
        usernameEditText.setText(sharedPreferences.getString("Username", getString(R.string.default_username)))
        pinEditText.setText(sharedPreferences.getString("PIN", getString(R.string.default_pin)))
        mkaEditText.setText(sharedPreferences.getString("MKALine", getString(R.string.default_mka)))
        deeplinkURLEditText.setText(sharedPreferences.getString("DeeplinkURL", getString(R.string.default_deeplink_url)))

        // SF Stage Auswahl
        val stage = sharedPreferences.getString("SFStage", getString(R.string.default_stage))
        val stagePosition = adapter.getPosition(stage)
        stageSpinner.setSelection(stagePosition)

        // Zugriff auf das FCM-Token Textfeld
        val fcmTokenTextView: TextView = findViewById(R.id.textViewFCMToken)

// FCM-Token laden und anzeigen
        val fcmToken = sharedPreferences.getString("FCMToken", "Token nicht verfügbar")
        fcmTokenTextView.text = fcmToken

// Optional: Wenn du möchtest, dass das Token kopiert werden kann
        fcmTokenTextView.setOnLongClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("FCM Token", fcmToken)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "FCM-Token kopiert", Toast.LENGTH_SHORT).show()
            true
        }

        // Log anzeigen
        logTextView.text = logContent.toString()
    }

    // Log aktualisieren, wenn die Aktivität wieder sichtbar wird
    override fun onResume() {
        super.onResume()
        logTextView.text = logContent.toString()
    }
}
