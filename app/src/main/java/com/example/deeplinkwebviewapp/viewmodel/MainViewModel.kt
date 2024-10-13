package com.example.deeplinkwebviewapp.viewmodel

import com.example.deeplinkwebviewapp.data.SfcIfResponse
import com.example.deeplinkwebviewapp.service.SfcServiceFactory
import com.example.deeplinkwebviewapp.data.SfmMobiResponse
import com.example.deeplinkwebviewapp.service.SfmServiceFactory
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.data.DeviceData
import com.example.deeplinkwebviewapp.data.DeviceDataSingleton
import com.example.deeplinkwebviewapp.data.PushNotificationPayload
import com.example.deeplinkwebviewapp.service.Logger
import com.example.deeplinkwebviewapp.service.MyHttpClient
import com.example.deeplinkwebviewapp.service.SilentLoginAndAdvisorDataService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.UUID


class MainViewModel(
    application: Application,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }
    private val _mobiDataLoaded = MutableLiveData<Boolean>()
    val mobiDataLoaded: LiveData<Boolean> = _mobiDataLoaded

    private val context: Context = application.applicationContext
    private val editor = sharedPreferences.edit()
    val deviceData: DeviceData = DeviceDataSingleton.deviceData
    private lateinit var previousLogin: String
    private var sfmMobiResponse: SfmMobiResponse? = null

    private val _fcmToken = MutableLiveData<String?>()
    val fcmToken: LiveData<String?> get() = _fcmToken
    private val _pushNotificationPayload = MutableLiveData<PushNotificationPayload?>()
    val pushNotificationPayload: LiveData<PushNotificationPayload?> get() = _pushNotificationPayload

    // SfcService initialisieren
    private val sfcService = SfcServiceFactory.create(
        sharedPreferences.getString("BLZ", "").toString(),
        sharedPreferences.getString("SFStage", "").toString(),
        444 // TODO:
    )

    // SfcService initialisieren
    private val sfmService = SfmServiceFactory.create(
        sharedPreferences.getString("BLZ", "").toString(),
        sharedPreferences.getString("SFStage", "").toString(),
        444 // TODO:
    )

    private var silentLoginService: SilentLoginAndAdvisorDataService? = null


    fun loadVkaData(pushNotificationPayload: PushNotificationPayload) {
        pushNotificationPayload.iam?.let {
            sfcService.fetchVkaData(it.contentId) { response: String? ->
                response?.let {
                    val sharedPreferences = context.getSharedPreferences(
                        "MyPreferences",
                        Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("vkaData", response)
                    editor.apply()  // apply() speichert asynchron

                    try {
                        _pushNotificationPayload.postValue(pushNotificationPayload)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing SfcIfResponse: ${e.localizedMessage}")
                        _pushNotificationPayload.postValue(null) // Bei Fehler null setzen
                    }
                } ?: run {
                    Log.e("MainViewModel", "Fehler bei der Anfrage")
                    _pushNotificationPayload.postValue(null) // Bei fehlgeschlagener Anfrage null setzen
                }
            }
        }
    }

    fun loadMobiData() {
        sfmService.fetchMobiData() { response: SfmMobiResponse? ->
            response?.let {
                try {
                    sfmMobiResponse = response
                    _mobiDataLoaded.postValue(true)  // Signalisiere, dass die Daten geladen sind
                    Log.d(TAG, "SfmMobData loaded")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing SfmMobiResponse: ${e.localizedMessage}")
                    _mobiDataLoaded.postValue(false)
                }
            } ?: run {
                Log.e("MainViewModel", "Fehler bei der Anfrage")
                _mobiDataLoaded.postValue(false)
            }
        }
    }

    fun getServletUrl ( ): String {
        val bankServlet = sfmMobiResponse?.bankCodesSettings?.data?.values?.firstOrNull()?.servlet ?: "Kein Servlet gefunden"
        println("Bank Servlet: $bankServlet")
        return bankServlet
    }

    fun getMailboxUrl ( ): String {
        val mailBoxUrl = sfmMobiResponse?.bankCodesSettings?.data?.values?.firstOrNull()?.postbox ?: "Kein Servlet gefunden"
        println("mailBoxUrl: $mailBoxUrl")
        return mailBoxUrl
    }

    fun getHostname() : String? {
        val uri = Uri.parse ( getServletUrl ( ) )
        return uri.host
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
            editor.putString("FCMToken", "")
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
        deviceData.push_id = sharedPreferences.getString("FCMToken", deviceData.push_id).toString()
        deviceData.login_id = sharedPreferences.getString("Username", deviceData.login_id).toString()
        deviceData.last_login = previousLogin
    }

    fun sendDeviceData() {
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
                    sharedPreferences.edit().commit()
                    sendDeviceData()
                }
            } else {
                _fcmToken.value = null // Fehler beim Abrufen des Tokens
            }
        }
    }
    fun getDeeplinksWebviewUrl(): String {
        return sharedPreferences.getString("DeeplinkURL", "").toString()
    }
}
