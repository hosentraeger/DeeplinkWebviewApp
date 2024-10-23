package com.example.deeplinkwebviewapp.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.UUID
/*
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
*/
@kotlinx.serialization.Serializable
data class
LoginRequest(
val userId: String,
val password: String,
val productId: String,
val productVersion: String,
val blz: String,
val kundenSystemId: String?,
val sicherungsverfahren: String,
val loginType: String = "ENDUSER_SAPP",
val tanMedium: String?
)

@kotlinx.serialization.Serializable
data class MkaResponseList(
    val responses: List<MkaResponseEntry>
)

@kotlinx.serialization.Serializable
data class MkaResponseEntry(
    val category: String,
    val code: Int,
    val message: String,
    val metadata: AuthenticationResponse
)

@kotlinx.serialization.Serializable
data class AuthenticationResponse(
    val authenticationStatus: String? = null,
    val hbciResponseList: List<String>? = null,
    val tanInfoList: List<TanInfo>? = null,
    val contractType: String? = null
)

@kotlinx.serialization.Serializable
data class TanInfo(
    val tanMethod: TanMethod,
    val tanMediaList: List<TanMedia>
)

@kotlinx.serialization.Serializable
data class TanMethod(
    val text: String,
    val key: String
)

@kotlinx.serialization.Serializable
data class TanMedia(
    val label: String
)

class MkaSession (
    context: Context,
    private val productId: String,
    private val productVersion: String,
    private val blz: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "MkaSession"
    }

    private val client = MyOkHttpClientFactory().createClientWithCertificate(context)
    private var mkaRoute: Deferred<String>? = null

    init {
        mkaRoute = scope.async {
            try {
                getRoute(blz)
            } catch (e: Exception) {
                "Fehler beim Abrufen der Route"
            }
        }
    }

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
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            response.close()
            return@withContext body
        }
    }
    suspend fun login(
        anmeldename: String,
        pin: String,
        sicherungsverfahren: String = "923",
        tanMedium: String = "Alle Geräte"
    ): Boolean {

        val url = mkaRoute!!.await() + "/fisession/authenticate"

        val requestBody = LoginRequest (
            userId= anmeldename,
            password= pin,
            productId= productId,
            productVersion= productVersion,
            blz= blz,
            kundenSystemId= "",
            sicherungsverfahren= sicherungsverfahren,
            loginType = "ENDUSER_SAPP",
            tanMedium = tanMedium
        )

        val jsonString = Json.encodeToString(requestBody)


        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), jsonString))
            .addHeader("Accept", "application/osplus.mkg.v5+json")
            .addHeader("X-Auth-ETAPS-PRODUCT-NAME", "sapp")
            .addHeader("x-uui-request-nonce", UUID.randomUUID().toString())
            .addHeader("x-uui-request-time", System.currentTimeMillis().toString().substring(0, 13))
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "Showcase/1.0.0 (Android 14; API 34)/VERBOSE")
            .build()

        val response = client.newCall(request).execute()

        return try {
            if (response.code == 900 || response.isSuccessful) {
                val body = response.body?.string() ?: ""
                // Deserialisiere die JSON-Antwort in die MkaResponseList-Klasse
                val mkaResponseList = Json.decodeFromString<List<MkaResponseEntry>>(body)
                Log.d(TAG, "Response deserialized successfully: $mkaResponseList")
                if ( mkaResponseList.firstOrNull()?.code == 3991) {
                    val kundenSystemId = mkaResponseList.firstOrNull()?.metadata?.hbciResponseList?.firstOrNull()
                    Log.d(TAG, "Kundensystem-ID: $kundenSystemId")
                }
                true
            } else {
                Log.w(TAG, "Request failed with code: ${response.code}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during request or deserialization", e)
            false
        } finally {
            response.close()
        }
    }
}
