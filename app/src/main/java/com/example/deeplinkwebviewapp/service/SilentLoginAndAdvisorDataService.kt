package com.example.deeplinkwebviewapp.service

import androidx.lifecycle.LifecycleCoroutineScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import android.util.Base64
import android.util.Log
import com.example.deeplinkwebviewapp.data.Params
import com.example.deeplinkwebviewapp.data.SilentLoginRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

class SilentLoginAndAdvisorDataService (
    private val servletUrl: String,
    private val blz: String,
    private val loginName: String,
    private val onlineBankingPin: String,
    private val encryptionKey: SecretKey?,
    private val lifecycleScope: LifecycleCoroutineScope // LifecycleScope übergeben
) {

    companion object {
        private const val TAG = "SilentLoginAndConsulterService"
    }

    private val client = MyHttpClient.getInstance()
    private val gson = Gson()

    private var cookies: List<String>? = null
    private var sessionValidUntil: Long = 0 // Zeitstempel, bis wann die Session gültig ist

    // Überprüfen, ob die Session noch gültig ist
    fun isSessionValid(): Boolean {
        return cookies != null && System.currentTimeMillis() < sessionValidUntil
    }

    fun getSessionCookies(onSessionReceived: (List<String>?) -> Unit, onError: (Throwable) -> Unit) {
        if (isSessionValid()) {
            onSessionReceived(cookies)
            return
        }

        // Falls keine gültige SessionID vorhanden ist, SilentLogin durchführen
        lifecycleScope.launch {
            try {
                cookies = performSilentLogin()
                sessionValidUntil = System.currentTimeMillis() + (60 * 60 * 1000) // Gültigkeit z.B. für 1 Stunde
                onSessionReceived(cookies)
            } catch (e: Exception) {
                onError(e) // Fehlerfall
            }
        }
    }

    // Suspended function für SilentLogin
    private suspend fun performSilentLogin(): List<String>? {
        val timestamp = getCurrentTimestamp()
        val applicationId = "88442A28"
        val targetUrl = "https://www.sparkasse.de/"

        val requestPayload = SilentLoginRequest(
            apiVersion = "1.0",
            service = "IF_SILENT_LOGIN",
            timestamp = timestamp,
            params = Params(onlineBankingPin, targetUrl, loginName),
            applicationId = applicationId,
            blz = blz
        )

        val plaintext = gson.toJson(requestPayload)
        val b64text = Base64.encodeToString(plaintext.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val digest = encryptionKey?.let { signData(b64text, it) }
        val signature = digest?.let { b64ToB64Url(it) }
        val payload = """{"signature":"$b64text.$signature"}"""

        // POST-Anfrage mit MyHttpClient
        return client.postSilentLogin(servletUrl, payload) ?: throw Exception("SilentLogin failed")
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

object SilentLoginAndAdvisorDataServiceFactory {
    private var instance: SilentLoginAndAdvisorDataService? = null
    private var servletUrl: String? = null
    private var blz: String? = null
    private var loginName: String? = null
    private var onlineBankingPin: String? = null
    private var lifecycleScope: LifecycleCoroutineScope? = null

    fun initialize(
        servletUrl: String,
        blz: String,
        loginName: String,
        onlineBankingPin: String,
        lifecycleScope: LifecycleCoroutineScope
    ) {
        this.servletUrl = servletUrl
        this.blz = blz
        this.loginName = loginName
        this.onlineBankingPin = onlineBankingPin
        this.lifecycleScope = lifecycleScope
    }
    fun getService(): SilentLoginAndAdvisorDataService {
        if (instance == null) {
            val secretKeyString = SecretKeyProvider.getSilentLoginSecretKey()
            val decodedKey = secretKeyString.toByteArray(Charsets.UTF_8)
            val secretKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")

            // Stelle sicher, dass die Parameter initialisiert sind
            val servletUrl = this.servletUrl ?: throw IllegalStateException("Service not initialized")
            val blz = this.blz ?: throw IllegalStateException("Service not initialized")
            val loginName = this.loginName ?: throw IllegalStateException("Service not initialized")
            val onlineBankingPin = this.onlineBankingPin ?: throw IllegalStateException("Service not initialized")
            val lifecycleScope = this.lifecycleScope ?: throw IllegalStateException("Service not initialized")

            instance = SilentLoginAndAdvisorDataService(servletUrl, blz, loginName, onlineBankingPin, secretKey, lifecycleScope)
        }
        return instance!!
    }
}