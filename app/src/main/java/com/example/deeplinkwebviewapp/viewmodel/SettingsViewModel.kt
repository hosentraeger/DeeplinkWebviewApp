package com.example.deeplinkwebviewapp.viewmodel

import com.example.deeplinkwebviewapp.service.MyHttpClient
import com.example.deeplinkwebviewapp.data.DeviceDataSingleton
import com.example.deeplinkwebviewapp.service.Logger
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    val deviceData = DeviceDataSingleton.deviceData

    fun getBLZ(): String {
        return sharedPreferences.getString("BLZ", "") ?: ""
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

    fun getMKALine(): String {
        return sharedPreferences.getString("MKALine", "") ?: ""
    }

    fun getDeeplinkURL(): String {
        return sharedPreferences.getString("DeeplinkURL", "") ?: ""
    }

    fun getSFStage(): String {
        return sharedPreferences.getString("SFStage", "") ?: ""
    }

    fun getApp(): String {
        return sharedPreferences.getString("App", "") ?: ""
    }

    fun saveSettings(blz: String, username: String, personennummer: String, pin: String, mka: String, stage: String, app: String, deeplinkUrl: String) {
        viewModelScope.launch {
            sharedPreferences.edit().apply {
                putString("BLZ", blz)
                putString("Username", username)
                putString("Personennummer", personennummer)
                putString("PIN", pin)
                putString("MKALine", mka)
                putString("SFStage", stage)
                putString("App", app)
                putString("DeeplinkURL", deeplinkUrl)
                apply()
            }
        }
    }

    fun regenerateDeviceId() {
        val newDeviceId = java.util.UUID.randomUUID().toString()
        deviceData.device_id = newDeviceId
        MyHttpClient.getInstance().postDeviceData(deviceData) { response ->
            if (response != null) {
                Logger.log("Gerätedaten erfolgreich gesendet: $response")
            } else {
                Logger.log("Fehler beim Senden der Gerätedaten.")
            }
        }
    }
}
