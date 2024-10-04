package com.example.deeplinkwebviewapp.service

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.SecretKey
import android.util.Base64
import android.util.Log
import com.example.deeplinkwebviewapp.data.Params
import com.example.deeplinkwebviewapp.data.SilentLoginRequest
import com.google.gson.Gson
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SilentLoginAndAdvisorDataService (private val encryptionKey: SecretKey? ) {
    companion object {
        private const val TAG = "SilentLoginAndConsulterService"
    }

    private val client = MyHttpClient.getInstance()
    private val gson = Gson()
    fun performSilentLogin(servletUrl: String, blz: String, loginName: String, onlineBankingPin: String, targetUrl: String, callback: (String?) -> Unit) {
        if ( encryptionKey != null ) {
            val timestamp = getCurrentTimestamp()
            val applicationId = "88442A28"

            val requestPayload = SilentLoginRequest(
                apiVersion = "1.0",
                service = "IF_SILENT_LOGIN",
                timestamp = timestamp,
                params = Params(onlineBankingPin, targetUrl, loginName),
                applicationId = applicationId,
                blz = blz
            )

            val plaintext = gson.toJson(requestPayload)
            Log.d ( TAG, "plaintext: $plaintext")
            val b64text = Base64.encodeToString(plaintext.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            Log.d(TAG, "b64text: $b64text")
            val digest = signData(b64text, encryptionKey)
            Log.d(TAG, "digest: $digest")
            val signature = b64ToB64Url(digest)
            Log.d(TAG, "signature: $signature")
            val payload = """{"signature":"$b64text.$signature"}"""
            Log.d(TAG, "payload: $payload")
            // POST-Anfrage mit MyHttpClient
            client.postSilentLogin(servletUrl, payload) { response ->
                callback(response)
            }
        }
    }

    private fun signData(data: String, key: SecretKey): String {
        val hmacSHA256 = Mac.getInstance("HmacSHA256")
        hmacSHA256.init(key)
        val signatureBytes = hmacSHA256.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z", Locale.getDefault())
        return dateFormat.format(Date())
    }
    private fun b64ToB64Url(s: String): String {
        var result = s.replace("=", "")
        result = result.replace("+", "-")
        result = result.replace("/", "_")
        return result
    }

    private fun b64UrlToB64(s: String): String {
        var result = s.replace("-", "+")
        result = result.replace("_", "/")
        val paddingNeeded = 4 - result.length % 4
        if (paddingNeeded == 2) {
            result += "=="
        } else if (paddingNeeded == 1) {
            result += "="
        }
        return result
    }
}

class SilentLoginAndAdvisorDataServiceFactory {
    companion object {
        fun create(): SilentLoginAndAdvisorDataService {
            val secretKeyString = SecretKeyProvider.getSilentLoginSecretKey()
            // val decodedKey = Base64.decode(secretKeyString, Base64.DEFAULT)
            val decodedKey = secretKeyString.toByteArray(Charsets.UTF_8)
            val secretKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
            return SilentLoginAndAdvisorDataService(secretKey)
        }
    }
}
