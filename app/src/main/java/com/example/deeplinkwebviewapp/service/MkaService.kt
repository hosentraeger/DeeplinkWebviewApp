package com.example.deeplinkwebviewapp.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.util.UUID

class MkaRoutingService(private val prebuiltClient: OkHttpClient) {

    suspend fun getRoute(blz: String): String {
        val request = Request.Builder()
            .url("https://global.sfg-mkg-etaps.de/route/blz/mkg?blz=$blz")
            .addHeader("X-Auth-ETAPS-PRODUCT-NAME", "ENDUSER_SAPP")
            .addHeader("x-uui-request-nonce", UUID.randomUUID().toString())
            .addHeader("x-uui-request-time", System.currentTimeMillis().toString().substring(0, 13))
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "Sparkasse Business/6.8.0.59318 (Android 14; API 34)/VERBOSE")
            .build()

        return withContext(Dispatchers.IO) {
            val response = prebuiltClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            response.close()
            return@withContext body
        }
    }
}