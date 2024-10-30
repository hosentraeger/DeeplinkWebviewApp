package com.example.deeplinkwebviewapp.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deeplinkwebviewapp.data.BankEntry
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AccountSettingsViewModelFactory(private val application: Application, private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountSettingsViewModel(application, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AccountSettingsViewModel(application: Application, private val sharedPreferences: SharedPreferences) : AndroidViewModel(application) {

    fun getBLZ(): String {
        return sharedPreferences.getString("BLZ", "") ?: ""
    }

    fun getMainObv(): BankEntry {
        return BankEntry(getBLZ(),getUsername())
    }

    fun getObvs(): List<BankEntry> {
        return listOf(
            BankEntry("25050180" , "mickymouse"),
            BankEntry("94059421" , "Lasttest_drno"),
            BankEntry("94059549" , "GandalfTheGrey"),
            BankEntry("94050310" , "DarthVader")
        ).plus(getMainObv())
    }

    fun getBlzs(): List<String> {
        return getObvs().map { it.blz }
    }

    fun getUsername(): String {
        return sharedPreferences.getString("Username", "") ?: ""
    }

    fun getPersonennummer(): String {
        return sharedPreferences.getString("Personennummer", "") ?: ""
    }

    fun getPIN(): String {
        return sharedPreferences.getString("PIN", "") ?: ""
    }

    fun saveSettings(blz: String, username: String, personennummer: String, pin: String) {
        viewModelScope.launch {
            sharedPreferences.edit().apply {
                putString("BLZ", blz)
                putString("Username", username)
                putString("Personennummer", personennummer)
                putString("PIN", pin)
                apply()
            }
        }
    }
}
