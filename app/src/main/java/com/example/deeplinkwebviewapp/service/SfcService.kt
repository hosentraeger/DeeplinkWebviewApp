package com.example.deeplinkwebviewapp.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

enum class STAGE { STAGE_RHEIN, STAGE_BETA, STAGE_PROD }

data class SfcResponse(
    var disrupterImageData: String = "",
    val disrupterTitle: String = ""
)

class SfcService(
    private val stage: STAGE,
    private val blz: String,
    private val interfaceVersion: String,
    private val productId: Int
) {
    companion object {
        const val INTERFACE_VERSION = "v2020.02"
        private const val TAG = "SfcService"
    }
    private val client = MyHttpClient.getInstance() // MyHttpClient verwenden
    private val _sfcResponse = MutableLiveData<SfcResponse>()
    val sfcResponse: LiveData<SfcResponse> get() = _sfcResponse // expose as LiveData

    fun fetchVkaData(userName: String) {
        val url = buildUrl(stage, INTERFACE_VERSION)
        client.postSfcData(url, blz, productId, userName) { response ->
            response?.let {
                _sfcResponse.postValue(it) // LiveData aktualisieren
            } ?: run {
                Log.e("SfcService", "Fehler bei der Anfrage")
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
}

class SfcServiceFactory {
    companion object {
        fun create(blz: String, strStage: String, productId: Int): SfcService {
            var stage: STAGE = STAGE.STAGE_PROD
            if (strStage == "Rhein") stage = STAGE.STAGE_RHEIN
            if (strStage == "Beta") stage = STAGE.STAGE_BETA
            return SfcService(stage, blz, "v2020.02", productId)
        }
    }
}
