package com.example.deeplinkwebviewapp.service

import com.example.deeplinkwebviewapp.data.SfmMobiResponse
import android.util.Log
import kotlinx.serialization.json.Json

class SfmService(
    private val stage: STAGE,
    private val blz: String,
    private val productId: Int
) {
    companion object {
        const val INTERFACE_VERSION = "v2024.04"
        private const val TAG = "SfmService"
    }

    private val client = MyHttpClient.getInstance()

    fun fetchMobiData(callback: (SfmMobiResponse?) -> Unit) {
        val url = buildUrl(stage, INTERFACE_VERSION)
        client.postSfmMobi(url, blz, productId) { response ->
            response?.let {
                try {
                    // Überprüfen, ob die Antwort den Status "NO_CONTENT" enthält
                    if (it == "NO_CONTENT") {
                        Log.e(TAG, "Keine Inhalte verfügbar (NO_CONTENT)")
                        callback(null) // Rückgabe von null oder ein spezifisches Ergebnis im Fehlerfall
                    } else {
                        val sfmMobiResponse = Json{ignoreUnknownKeys = true}.decodeFromString<SfmMobiResponse>(it)
                        callback(sfmMobiResponse) // Rückgabe der deserialisierten SfcIfResponse
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
            STAGE.STAGE_RHEIN -> "https://rhein.starfinanz.de/sfm_mobi"
            STAGE.STAGE_BETA -> "https://beta-services.starfinanz.de/sfm_mobi"
            STAGE.STAGE_PROD -> "https://services.starfinanz.de/sfm_mobi"
        }
        return "$baseUrl/$interfaceVersion/sapp"
    }
}

class SfmServiceFactory {
    companion object {
        fun create(blz: String, strStage: String, productId: Int): SfmService {
            var stage: STAGE = STAGE.STAGE_PROD
            if (strStage == "Rhein") stage = STAGE.STAGE_RHEIN
            if (strStage == "Beta") stage = STAGE.STAGE_BETA
            return SfmService(stage, blz, productId)
        }
    }
}
