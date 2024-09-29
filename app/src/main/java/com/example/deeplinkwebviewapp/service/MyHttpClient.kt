package com.example.deeplinkwebviewapp.service

import android.util.Log
import com.example.deeplinkwebviewapp.data.DeviceData
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyHttpClient private constructor() {

    private val client = OkHttpClient()
    private val gson = Gson()

    companion object {
        @Volatile
        private var INSTANCE: MyHttpClient? = null

        fun getInstance(): MyHttpClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MyHttpClient().also { INSTANCE = it }
            }
        }
    }

    fun postDeviceData(deviceData: DeviceData, callback: (String?) -> Unit) {
        val json = gson.toJson(deviceData) // JSON-String erstellen
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        Log.d("MyHttpClient", requestBody.toString()) // Für Debugging

        // POST-Anfrage erstellen
        val request = Request.Builder()
            .url("https://www.fsiebecke.de/appstart") // Ersetze dies durch deine URL
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).execute().use { response: Response ->
                callback(if (response.isSuccessful) response.body?.string() else null)
            }
        }
    }
}
