package com.example.deeplinkwebviewapp.viewmodel

import com.example.deeplinkwebviewapp.service.MyHttpClient
import com.example.deeplinkwebviewapp.service.Logger

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import androidx.lifecycle.viewModelScope
import com.example.deeplinkwebviewapp.data.DeviceDataSingleton
import kotlinx.coroutines.launch

class SystemParametersViewModelFactory(private val application: Application, private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SystemParametersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SystemParametersViewModel(application, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SystemParametersViewModel(application: Application, private val sharedPreferences: SharedPreferences) : AndroidViewModel(application) {

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

    fun saveSettings(mka: String, stage: String, app: String, deeplinkUrl: String) {
        viewModelScope.launch {
            sharedPreferences.edit().apply {
                putString("MKALine", mka)
                putString("SFStage", stage)
                putString("App", app)
                putString("DeeplinkURL", deeplinkUrl)
                apply()
            }
        }
    }

    fun regenerateDeviceId() {
        val deviceData = DeviceDataSingleton.getDeviceData()
        val newDeviceId = java.util.UUID.randomUUID().toString()
        deviceData.deviceMetaData?.deviceId = newDeviceId
        MyHttpClient.getInstance().postDeviceData(deviceData) { response ->
            if (response != null) {
                Logger.log("Gerätedaten erfolgreich gesendet: $response")
            } else {
                Logger.log("Fehler beim Senden der Gerätedaten.")
            }
        }
    }
}
