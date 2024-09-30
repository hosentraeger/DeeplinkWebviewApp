package com.example.deeplinkwebviewapp.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.data.DeviceData
import com.example.deeplinkwebviewapp.data.DeviceDataSingleton
import com.example.deeplinkwebviewapp.service.Logger
import com.example.deeplinkwebviewapp.service.MyHttpClient
import com.example.deeplinkwebviewapp.service.SfcServiceFactory
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID

class MainViewModel(
    application: Application,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val _fcmToken = MutableLiveData<String?>()
    val fcmToken: LiveData<String?> get() = _fcmToken
    private val editor = sharedPreferences.edit()
    val deviceData: DeviceData = DeviceDataSingleton.deviceData
    private lateinit var previousLogin: String

    // SfcService initialisieren
    val blz = sharedPreferences.getString("BLZ", "").toString()
    val stage = sharedPreferences.getString("SFStage", "").toString()
    private val sfcService = SfcServiceFactory.create(
        blz,
        //sharedPreferences.getString("BLZ", "").toString(),
        stage,
        //sharedPreferences.getString("Stage", "").toString(),
        444 // TODO:
    )

    private val _disrupterImageData = MutableLiveData<String>()
    val disrupterImageData: LiveData<String> get() = _disrupterImageData

    init {
        sfcService.sfcResponse.observeForever { response ->
            _disrupterImageData.value = response.disrupterImageData
        }
    }
    fun loadVkaData(userId: String) {
        sfcService.fetchVkaData(userId)
    }

    private fun getCurrentTimestamp(): String {
        val currentDateTime = ZonedDateTime.now(java.time.ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return currentDateTime.format(formatter)
    }

    fun initializePreferences() {
        if (!sharedPreferences.getBoolean("valid", false)) {
            editor.putString("BLZ", context.getString(R.string.default_blz))
            editor.putString("Username", context.getString(R.string.default_username))
            editor.putString("PIN", context.getString(R.string.default_pin))
            editor.putString("MKALine", context.getString(R.string.default_mka))
            editor.putString("SFStage", context.getString(R.string.default_stage))
            editor.putString("App", context.getString(R.string.default_app))
            editor.putString("DeeplinkURL", context.getString(R.string.default_deeplink_url))
            editor.putString("PushId", "")
            editor.putString("LastLogin", context.getString(R.string.default_last_login))
            editor.putBoolean("valid", true)
        }
        if (sharedPreferences.getString("DeviceId", "").isNullOrEmpty()) {
            editor.putString("DeviceId", UUID.randomUUID().toString())
        }
        previousLogin = sharedPreferences.getString("LastLogin", deviceData.last_login).toString()
        editor.putString("LastLogin", getCurrentTimestamp())

        editor.apply() // Änderungen speichern
    }

    fun initializeDeviceData() {
        // Letzten Login und Device-ID abrufen
        deviceData.device_id = sharedPreferences.getString("DeviceId", deviceData.device_id).toString()
        deviceData.push_id = sharedPreferences.getString("PushId", deviceData.push_id).toString()
        deviceData.login_id = sharedPreferences.getString("Username", deviceData.login_id).toString()
        deviceData.last_login = previousLogin
    }

    fun onNavItemSelected(itemId: Int): Boolean {
        return when (itemId) {
            R.id.nav_function_appstart -> {
                sendDeviceData()
                true
            }
            else -> false
        }
    }
    private fun sendDeviceData() {
        val deviceData: DeviceData = DeviceDataSingleton.deviceData
        MyHttpClient.getInstance().postDeviceData(deviceData) { response ->
            if (response != null) {
                Logger.log("Gerätedaten erfolgreich gesendet: $response")
            } else {
                Logger.log("Fehler beim Senden der Gerätedaten.")
            }
        }
    }

    fun fetchFcmToken() {
        val deviceData: DeviceData = DeviceDataSingleton.deviceData
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                _fcmToken.value = token

                // Speichere den Token in SharedPreferences, wenn es sich geändert hat
                if (deviceData.push_id != token) {
                    deviceData.push_id = token
                    sharedPreferences.edit().putString("FCMToken", token).apply()
                    sendDeviceData()
                }
            } else {
                _fcmToken.value = null // Fehler beim Abrufen des Tokens
            }
        }
    }
}