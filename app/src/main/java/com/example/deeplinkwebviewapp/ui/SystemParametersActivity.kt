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
import com.example.deeplinkwebviewapp.viewmodel.SystemParametersViewModelFactory
import com.example.deeplinkwebviewapp.viewmodel.SystemParametersViewModel


class SystemParametersActivity : AppCompatActivity() {

    private lateinit var appSpinner: Spinner
    private lateinit var stageSpinner: Spinner
    private lateinit var fcmTokenTextView: TextView
    private lateinit var deviceIdTextView: TextView
    private lateinit var servletUrlTextView: TextView
    private lateinit var systemParametersViewModel: SystemParametersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_parameters)

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        systemParametersViewModel = ViewModelProvider(this, SystemParametersViewModelFactory(application, sharedPreferences)).get(SystemParametersViewModel::class.java)

        // Zugriff auf die Views
        val mkaEditText: EditText = findViewById(R.id.editTextMKALine)
        val deeplinkUrlEditText: EditText = findViewById(R.id.editTextDeeplinkURL)
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
        mkaEditText.setText(systemParametersViewModel.getMKALine())
        deeplinkUrlEditText.setText(systemParametersViewModel.getDeeplinkURL())

        // Stage Spinner
        val stage = systemParametersViewModel.getSFStage()
        val stagePosition = adapter.getPosition(stage)
        stageSpinner.setSelection(stagePosition)

        // App Spinner
        val appAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.app_array,
            android.R.layout.simple_spinner_item
        )
        val app = systemParametersViewModel.getApp()
        val appPosition = appAdapter.getPosition(app)
        appSpinner.setSelection(appPosition)

        val clearBadgeButton: Button = findViewById(R.id.buttonClearBadge)
        clearBadgeButton.setOnClickListener {
            resetBadgeCounterOfPushMessages()
        }
        // Button zum Speichern der Einstellungen
        val saveButton: Button = findViewById(R.id.buttonSave)
        saveButton.setOnClickListener {
            systemParametersViewModel.saveSettings(
                mkaEditText.text.toString(),
                stageSpinner.selectedItem.toString(),
                appSpinner.selectedItem.toString(),
                deeplinkUrlEditText.text.toString()
            )
            Toast.makeText(this, "Einstellungen gespeichert", Toast.LENGTH_SHORT).show()
        }

        // Button zum Regenerieren der Device-ID
        val regenerateButton: Button = findViewById(R.id.buttonRegenerate)
        regenerateButton.setOnClickListener {
            systemParametersViewModel.regenerateDeviceId()
            deviceIdTextView.text = systemParametersViewModel.deviceData.device_id
        }

        // FCM Token anzeigen
        fcmTokenTextView.text = systemParametersViewModel.deviceData.push_id
        deviceIdTextView.text = systemParametersViewModel.deviceData.device_id
        servletUrlTextView.text = getString(R.string.servlet_url)
    }
    private fun resetBadgeCounterOfPushMessages() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}
