package com.example.deeplinkwebviewapp.service

import android.net.Uri
import com.example.deeplinkwebviewapp.data.SfcIfResponse
import android.util.Log
import com.example.deeplinkwebviewapp.data.AemBanner
import com.example.deeplinkwebviewapp.data.AemPage
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

enum class STAGE { STAGE_RHEIN, STAGE_BETA, STAGE_PROD }

class SfcService(
    private val stage: STAGE,
    private val blz: String,
    private val productId: Int
) {
    companion object {
        const val INTERFACE_VERSION = "v2020.02"
        private const val TAG = "SfcService"
    }

    private val client = MyHttpClient.getInstance()

    fun fetchVkaData(userName: String, callback: (String?) -> Unit) {
        val url = buildUrl(stage, INTERFACE_VERSION)
        client.postSfcData(url, blz, productId, userName) { response ->
            response?.let {
                try {
                    val sfcIfResponse = Json.decodeFromString<SfcIfResponse>(it)

                    // Überprüfen, ob die Antwort den Status "NO_CONTENT" enthält
                    if (sfcIfResponse.services.any { service -> service.IF?.status == "NO_CONTENT" }) {
                        Log.e(TAG, "Keine Inhalte verfügbar (NO_CONTENT)")
                        callback(null) // Rückgabe von null oder ein spezifisches Ergebnis im Fehlerfall
                    } else {
//                        callback(sfcIfResponse) // Rückgabe der deserialisierten SfcIfResponse
                        callback(response)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler bei der Deserialisierung: ${e.message}")
                    callback(null) // Rückgabe von null im Fehlerfall
                }
            } ?: run {
                Log.e(TAG, "Fehler bei der Anfrage: Response war null")
                callback(null)
            }
        }
    }

    private fun buildUrl(stage: STAGE, interfaceVersion: String): String {
        val baseUrl = when (stage) {
            STAGE.STAGE_RHEIN -> "https://rhein.starfinanz.de/sfc_rest"
            STAGE.STAGE_BETA -> "https://beta-services.starfinanz.de/sfc_rest_test"
            STAGE.STAGE_PROD -> "https://services.starfinanz.de/sfc_rest"
        }
        return "$baseUrl/$interfaceVersion/service/multi"
    }

    fun fetchAemBanner(url: String, callback: (AemBanner?) -> Unit) {
        client.getPage(url) { response ->
            response?.let {
                try {
                    val aemBanner = Json.decodeFromString<AemBanner>(it)
                    callback(aemBanner)
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler bei der Deserialisierung: ${e.message}")
                    callback(null) // Rückgabe von null im Fehlerfall
                }
            } ?: run {
                Log.e(TAG, "Fehler bei der Anfrage: Response war null")
                callback(null)
            }
        }
    }

    fun fetchAemPage(url: String, callback: (AemPage?) -> Unit) {
        client.getPage(url) { response ->
            response?.let {
                try {
                    val aemPage = Json.decodeFromString<AemPage>(it)
                    callback(aemPage)
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler bei der Deserialisierung: ${e.message}")
                    callback(null) // Rückgabe von null im Fehlerfall
                }
            } ?: run {
                Log.e(TAG, "Fehler bei der Anfrage: Response war null")
                callback(null)
            }
        }
    }
}

class SfcServiceFactory {
    companion object {
        fun create(blz: String, strStage: String, productId: Int): SfcService {
            var stage: STAGE = STAGE.STAGE_PROD
            if (strStage == "Rhein") stage = STAGE.STAGE_RHEIN
            if (strStage == "Beta") stage = STAGE.STAGE_BETA
            return SfcService(stage, blz, productId)
        }
    }
}
