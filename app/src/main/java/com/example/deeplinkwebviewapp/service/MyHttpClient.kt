package com.example.deeplinkwebviewapp.service

import android.content.Context
import android.util.Log
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.data.DeviceData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class SimpleCookieJar : CookieJar {
    private val cookieStore: MutableMap<String, List<Cookie>> = mutableMapOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val existingCookies = cookieStore[url.host]?.toMutableList() ?: mutableListOf()
        existingCookies.addAll(cookies)
        cookieStore[url.host] = existingCookies.distinctBy { it.name }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host] ?: emptyList()
        for ( cookie in cookies) {
            Log.d("Cookiemanager", "loadForRequest ${url.toString()} -> $cookie.name")
        }
        return cookies
    }
}

class MyHttpClient private constructor(private val userAgent: String) {

    private val client: OkHttpClient
    private val gson = Gson()

    init {
        // Logging-Interceptor erstellen
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Logging-Level anpassen
        }

        // OkHttpClient mit dem Interceptor erstellen
        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    companion object {
        private const val TAG = "MyHttpClient"
        @Volatile
        private var INSTANCE: MyHttpClient? = null

        fun getInstance(): MyHttpClient {
            return INSTANCE ?: throw IllegalStateException("MyHttpClient is not initialized, call initialize() first.")
        }
        fun initialize(userAgent: String) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = MyHttpClient(userAgent)
                    }
                }
            }
        }
    }

    fun postDeviceData(deviceData: DeviceData, callback: (String?) -> Unit) {
        val json = gson.toJson(deviceData) // JSON-String erstellen
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

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

    // Neue Methode für SFC-Daten
    fun postSfcData(url: String, blz: String, productId: Int, userName: String, callback: (String?) -> Unit) {
        val body = """
        {
            "bankUsers": [
                {
                    "blz": "$blz",
                    "securityMedium": "HBCI",
                    "userName": "$userName"
                }
            ],
            "blz": "$blz",
            "clientId": "00000000-0000-0000-0000-000000000000",
            "manufacturer": "showcaseapps",
            "model": "iPhone10,6",
            "osVersion": "15.3.1",
            "platformId": 1,
            "productId": $productId,
            "productVersion": "7.0.0",
            "promotionsWanted": true,
            "screenHeight": 2436,
            "screenWidth": 1125,
            "services": [
                {
                    "service": "IF"
                }
            ]
        }
        """.trimIndent()

        val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    callback(responseBody) // Rückgabe der Antwort
                } else {
                    val errorBody = response.body?.string() ?: "Keine Antwort vom Server"
                    Log.e("MyHttpClient", "Fehler: ${response.code}, Fehlerinhalt: $errorBody")
                    callback(null) // Im Fehlerfall null zurückgeben
                }
            } catch (e: Exception) {
                Log.e("MyHttpClient", "${e.message}")
                callback(null) // Im Fehlerfall null zurückgeben
            }
        }
    }
    fun postSfmMobi(url: String, blz: String, productId: Int, callback: (String?) -> Unit) {
        val body = """
            {
              "productId": $productId,
              "build": "1.0.1",
              "platformId": 1,
              "blz": "$blz",
              "device": "IOS_PHONE_HIGH",
              "bankCodes": [
                "$blz"
              ]
            }
        """.trimIndent()

        val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    callback(responseBody) // Rückgabe der Antwort
                } else {
                    val errorBody = response.body?.string() ?: "Keine Antwort vom Server"
                    Log.e("MyHttpClient", "Fehler: ${response.code}, Fehlerinhalt: $errorBody")
                    callback(null) // Im Fehlerfall null zurückgeben
                }
            } catch (e: Exception) {
                Log.e("MyHttpClient", "${e.message}")
                callback(null) // Im Fehlerfall null zurückgeben
            }
        }
    }

    suspend fun postSilentLogin(servletUrl: String, payload: String): List<String>? {
        val requestBody = payload.toRequestBody("application/json".toMediaType())

        // Einen separaten OkHttpClient erstellen, der Redirects nicht folgt
        val clientNoRedirects = client.newBuilder()
            .followRedirects(false)
            .build()
        val userAgentString = "" // context.getString(R.string.user_agent_string)
        // POST-Anfrage erstellen
        val request = Request.Builder()
            .url(servletUrl)
            .header("User-Agent", "okhttp3")
            .header("Accept", "*.*")
            .header("Connection", "keep-alive")
            .header("content-type", "application/json")
            .header("User-Agent", userAgentString)
            .header("Accept-Charset", "UTF-8")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = clientNoRedirects.newCall(request).execute()
                if (response.isSuccessful || response.code == 302) { // Erfolg oder Redirect
                    // Header mit Cookies extrahieren
                    val cookies = response.headers("Set-Cookie")
                    cookies
/*
                    // JSESSIONID-Cookie suchen
                    val jsessionId = cookies.find { it.startsWith("JSESSIONID=") }
                        ?.split(";") // Cookie-Daten werden oft durch Semikolon getrennt
                        ?.firstOrNull() // Den ersten Teil (JSESSIONID=xxxx) extrahieren
                        ?.substringAfter("JSESSIONID=") // Den Wert der JSESSIONID extrahieren

                    jsessionId
*/
                } else {
                    Log.e("MyHttpClient", "Fehler: ${response.code}")
                    null
                }
            } catch (e: Exception) {
                Log.e("MyHttpClient", "Exception: ${e.message}")
                null
            }
        }
    }

    fun getRedirectLocation(url: String, callback: (String?) -> Unit) {
        // OkHttpClient erstellen, der Redirects nicht automatisch folgt
        val clientNoRedirects = client.newBuilder()
            .followRedirects(false) // Keine automatischen Umleitungen
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = clientNoRedirects.newCall(request).execute()
                if (response.code == 303) { // HTTP 303 See Other
                    val location = response.header("Location") // Neue Location-URL erhalten
                    callback(location)
                } else {
                    Log.e(TAG, "Kein Redirect: ${response.code}")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
                callback(null)
            }
        }
    }

    fun getPage(url: String, callback: (String?) -> Unit) {
        val client = client.newBuilder()
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.code == 200) {
                    val responseBodyString: String? = response.body?.string()
                    callback(responseBodyString)
                } else {
                    Log.e(TAG, "Response Code: ${response.code}")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
                callback(null)
            }
        }
    }
}

class MyOkHttpClientFactory {
    fun createClientWithCertificate(context: Context): OkHttpClient {
        // Lade das Zertifikat und den privaten Schlüssel aus res/raw
        val certInputStream: InputStream = context.resources.openRawResource(R.raw.mkp_apv_etaps) // Zertifikat
        val rootCertInputStream: InputStream = context.resources.openRawResource(R.raw.quovadis_root_ca2) // Root-Zertifikat
        val intermediateCertInputStream: InputStream = context.resources.openRawResource(R.raw.sfg_mkg_etaps) // Intermediate-Zertifikat
        val keyInputStream: InputStream = context.resources.openRawResource(R.raw.mkp_apv_key) // Privater Schlüssel

        // Erstelle ein CertificateFactory für das Zertifikat
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = mutableListOf<Certificate>()

        // Lade das Serverzertifikat
        certificates.add(certificateFactory.generateCertificate(certInputStream))

        // Lade das Root-Zertifikat
        certificates.add(certificateFactory.generateCertificate(rootCertInputStream))

        // Lade das Intermediate-Zertifikat
        certificates.add(certificateFactory.generateCertificate(intermediateCertInputStream))

        // Erstelle und initialisiere einen KeyStore für den privaten Schlüssel
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)

        // Füge die Zertifikate zum KeyStore hinzu
        for (i in certificates.indices) {
            keyStore.setCertificateEntry("cert$i", certificates[i])
        }

        // Lies den privaten Schlüssel und dekodiere ihn
        val keyString = keyInputStream.bufferedReader().use { it.readText() }
        val privateKeyString = keyString
            .substringAfter("-----BEGIN PRIVATE KEY-----")
            .substringBefore("-----END PRIVATE KEY-----")
            .replace("\\s+".toRegex(), "") // Entferne alle Leerzeichen und Zeilenumbrüche

        val keyBytes = android.util.Base64.decode(privateKeyString, android.util.Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val privateKey: PrivateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec)

        // Füge den privaten Schlüssel zum KeyStore hinzu
        val clientCertificateEntry = KeyStore.PrivateKeyEntry(privateKey, arrayOf(certificates[0])) // Hier nehmen wir an, dass das erste Zertifikat das Client-Zertifikat ist
        keyStore.setEntry("client", clientCertificateEntry, KeyStore.PasswordProtection("your_keystore_password".toCharArray())) // Verwenden Sie ein sicheres Passwort

        // Erstelle einen KeyManagerFactory und initialisiere ihn mit dem KeyStore
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, "your_keystore_password".toCharArray()) // Passwort für den KeyStore

        // Erstelle einen TrustManagerFactory und initiiere ihn mit dem KeyStore
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        // Erstelle einen SSLContext mit den KeyManagers und TrustManagers
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, null)

        // Logging-Interceptor erstellen
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Logging-Level anpassen
        }

        val cookieJar = SimpleCookieJar()

        // Erstelle und konfiguriere den OkHttpClient
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslContext.socketFactory, trustManagerFactory.trustManagers[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Für produktive Umgebungen sicherstellen
            .build()
    }
}
