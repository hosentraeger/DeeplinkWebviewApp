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
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

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
data class
RegisterDeviceRequest(
    val kundenSystemId: String,
    val sicherungsverfahren: String,
    val tanMedium: String
)

@kotlinx.serialization.Serializable
data class
CheckTanStatusRequest(
    val auftragsreferenz: String,
    val sicherungsverfahren: String
)

@kotlinx.serialization.Serializable
sealed class Metadata

@kotlinx.serialization.Serializable
data class MkaResponseEntry<T : Metadata>(
    val category: String,
    val code: Int? = 9999,
    val message: String,
    val metadata: T
)

@kotlinx.serialization.Serializable
data class AuthenticationResponse(
    val authenticationStatus: String? = null,
    val hbciResponseList: List<String>? = null,
    val tanInfoList: List<TanInfo>? = null,
    val contractType: String? = null,
) : Metadata()

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

@kotlinx.serialization.Serializable
data class SignatureChallenge(
    val orderReference: String,
    val tanDate: String,
    val tanIndex: String,
    val tanTime: String
) : Metadata ( )

@kotlinx.serialization.Serializable
data class DeviceRegistrationRequest(
    val authenticationStatus: String? = null,
    val signatureChallenge: SignatureChallenge
) : Metadata()

@kotlinx.serialization.Serializable
data class DeviceRegistrationFinalization(
    val authenticationStatus: String? = null,
    val tanStatus: String?
) : Metadata()

class MkaSession (
    private val context: Context,
    private val productId: String,
    private val productVersion: String,
    private val blz: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
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
        kundenSystemId: String,
        sicherungsverfahren: String,
        tanMedium: String
    ): List<MkaResponseEntry<AuthenticationResponse>>? {

        val url = mkaRoute!!.await() + "/fisession/authenticate"

        val requestBody = LoginRequest (
            userId= anmeldename,
            password= pin,
            productId= productId,
            productVersion= productVersion,
            blz= blz,
            kundenSystemId= kundenSystemId,
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
                val mkaResponseList = Json.decodeFromString<List<MkaResponseEntry<AuthenticationResponse>>>(body)
                Log.d(TAG, "Response deserialized successfully: $mkaResponseList")
                if ( mkaResponseList.firstOrNull()?.code == 3991) {
                    val newKundenSystemId = mkaResponseList.firstOrNull()?.metadata?.hbciResponseList?.firstOrNull()
                    Log.d(TAG, "Kundensystem-ID: $newKundenSystemId")
                }
                mkaResponseList
            } else {
                Log.w(TAG, "Request failed with code: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during request or deserialization", e)
            null
        } finally {
            response.close()
        }
    }

    suspend fun registerDevice(kundenSystemId: String, sicherungsverfahren: String, tanMedium: String): List<MkaResponseEntry<DeviceRegistrationRequest>>? {
        val url = mkaRoute!!.await() + "/fisession/registerDevice"

        val requestBody = RegisterDeviceRequest (
            kundenSystemId= kundenSystemId,
            sicherungsverfahren= sicherungsverfahren,
            tanMedium = tanMedium
        )

        val jsonString = Json.encodeToString(requestBody)

        val request = Request.Builder()
            .url(url)
            .post(jsonString.toRequestBody("application/json".toMediaTypeOrNull()))
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
                val mkaResponseList = Json.decodeFromString<List<MkaResponseEntry<DeviceRegistrationRequest>>>(body)
                Log.d(TAG, "Response deserialized successfully: $mkaResponseList")
                val orderReference = mkaResponseList.firstOrNull()?.metadata?.signatureChallenge?.orderReference
                Log.d(TAG, "orderReference: $orderReference")
                mkaResponseList
            } else {
                Log.w(TAG, "Request failed with code: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during request or deserialization", e)
            null
        } finally {
            response.close()
        }
    }

    suspend fun finishRegistration(auftragsReferenz: String, sicherungsverfahren: String): List<MkaResponseEntry<DeviceRegistrationFinalization>>? {
        val url = mkaRoute!!.await() + "/fisession/authenticate/sca/checkTanStatus"

        val requestBody = CheckTanStatusRequest (
            auftragsReferenz,
            sicherungsverfahren
        )

        val jsonString = Json.encodeToString(requestBody)

        val request = Request.Builder()
            .url(url)
            .post(jsonString.toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Accept", "application/osplus.mkg.v5+json")
            .addHeader("X-Auth-ETAPS-PRODUCT-NAME", "sapp")
            .addHeader("x-uui-request-nonce", UUID.randomUUID().toString())
            .addHeader("x-uui-request-time", System.currentTimeMillis().toString().substring(0, 13))
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "Showcase/1.0.0 (Android 14; API 34)/VERBOSE")
            .build()

        val response = client.newCall(request).execute()

        return try {
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val mkaResponseList = Json.decodeFromString<List<MkaResponseEntry<DeviceRegistrationFinalization>>>(body)
                Log.d(TAG, "Response deserialized successfully: $mkaResponseList")
                mkaResponseList
            } else {
                Log.w(TAG, "Request failed with code: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during request or deserialization", e)
            null
        } finally {
            response.close()
        }
    }

    fun saveKeyValueToSharedPrefs(key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(
            "MyPreferences",
            Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()  // apply() speichert asynchron

    }

}
