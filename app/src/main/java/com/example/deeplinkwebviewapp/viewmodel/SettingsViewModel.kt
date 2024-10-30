// SettingsViewModel.kt
package com.example.deeplinkwebviewapp.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsViewModel : ViewModel() {

    private var _appLanguage: String = "de"
    private var _sorting: String = "holder"
    private var _maxSessionDuration: Int = 5
    private var _autoUpdateBalance: Boolean = false

    fun saveSettings(language: String, sorting: String, maxSessionDuration: Int, autoUpdate: Boolean) {
        _appLanguage = language
        _sorting = sorting
        _maxSessionDuration = maxSessionDuration
        _autoUpdateBalance = autoUpdate

        // Hier kannst du die Einstellungen in SharedPreferences oder einer Datenbank speichern
    }

    // Hier kannst du Getter-Methoden hinzufügen, um die gespeicherten Einstellungen abzurufen
    fun getAppLanguage() = _appLanguage
    fun getSorting() = _sorting
    fun getMaxSessionDuration() = _maxSessionDuration
    fun isAutoUpdateBalanceEnabled() = _autoUpdateBalance
}
