// SettingsActivity.kt
package com.example.deeplinkwebviewapp.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerSorting: Spinner
    private lateinit var spinnerMaxSessionDuration: Spinner
    private lateinit var switchAutoUpdateBalance: Switch
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        spinnerSorting = findViewById(R.id.spinnerSorting)
        spinnerMaxSessionDuration = findViewById(R.id.spinnerMaxSessionDuration)
        switchAutoUpdateBalance = findViewById(R.id.switchAutoUpdateBalance)
        buttonSave = findViewById(R.id.buttonSave)

        // Set up language spinner
        val languages = arrayOf("de", "en", "tr", "ua", "pl", "cz")
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = languageAdapter

        // Set up sorting spinner
        val sortingOptions = arrayOf("holder", "number")
        val sortingAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortingOptions)
        sortingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSorting.adapter = sortingAdapter

        // Set up max session duration spinner
        val durations = arrayOf("2", "5", "10")
        val durationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMaxSessionDuration.adapter = durationAdapter

        // Save button click listener
        buttonSave.setOnClickListener {
            val selectedLanguage = spinnerLanguage.selectedItem.toString()
            val selectedSorting = spinnerSorting.selectedItem.toString()
            val selectedMaxSessionDuration = spinnerMaxSessionDuration.selectedItem.toString().toInt()
            val autoUpdateBalance = switchAutoUpdateBalance.isChecked

            viewModel.saveSettings(selectedLanguage, selectedSorting, selectedMaxSessionDuration, autoUpdateBalance)
            finish() // Close the activity after saving
        }
    }
}
