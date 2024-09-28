package `mipmap-xxxhdpi`

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

enum class STAGE { STAGE_RHEIN, STAGE_BETA, STAGE_PROD }

class VkaData(private val _rawData: String) {
    val rawJson = Json.decodeFromString<MyDataClass>(jsonString)
    val rawData: String = _rawData
    fun getValue(): String {
        return rawData
    }
    fun getDefaultDisrupter(): ActionPage? {
        return null
    }

    fun getDefaultLogoutPage(): ActionPage? {
        return null
    }

    fun getDefaultOverviewBanner(): ActionBanner? {
        return null
    }

    fun getDefaultConfirmationBanner(): ActionBanner? {
        return null
    }

}

// Hilfsklasse für ActionBanner
data class ActionBanner(
    val banner: String,
    val imgAlt: String,
    val url: String,
    val openOutsideApp: Boolean,
    val aemAsset: Boolean,
    val decodedImage: Bitmap? // Hier wird das dekodierte Bild gespeichert
)

// Hilfsklasse für ActionPage
data class ActionPage(
    val image: String,
    val imgAlt: String,
    val headline: String,
    val richHeadline: Boolean,
    val text: String,
    val richText: Boolean,
    val aemAsset: Boolean,
    val firstLink: ActionLink,
    val forwardLink: ActionLink,
    val noInterestLink: ActionLink,
    val decodedImage: Bitmap? // Hier wird das dekodierte Bild gespeichert
)

// Hilfsklasse für ActionLink
data class ActionLink(
    val title: String,
    val url: String,
    val openOutsideApp: Boolean
)
fun decodeBase64Image(base64Image: String): Bitmap? {
    return try {
        val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}

class SfcService(
    private val stage: STAGE,
    private val blz: String,
    private val productId: String
) {
    companion object {
        const val INTERFACE_VERSION = "v2020.02"
    }

    private val client = OkHttpClient()

    suspend fun updateVkaData(userName: String) {
        val url = buildUrl(stage, INTERFACE_VERSION, blz, productId)

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
            "productId": "$productId",
            "productVersion": "5.14.1",
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

        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: null
            } else {
            }
        } catch (e: Exception) {
        }
    }

    private fun buildUrl(stage: STAGE, version: String, blz: String, productId: String): String {
        val baseUrl = when (stage) {
            STAGE.STAGE_RHEIN -> "https://rhein.starfinanz.de/sfc_rest"
            STAGE.STAGE_BETA -> "https://beta-services.starfinanz.de/sfc_rest_test"
            STAGE.STAGE_PROD -> "https://services.starfinanz.de/sfc_rest"
        }
        return "$baseUrl/$version/service/multi"
    }
}

class SfcServiceFactory {
    companion object {
        fun create(blz: String, strStage: String, productId: String): SfcService {
            var stage: STAGE = STAGE.STAGE_PROD
            if (strStage == "RHEIN") stage = STAGE.STAGE_RHEIN
            if (strStage == "BETA") stage = STAGE.STAGE_BETA
            return SfcService(stage, blz, productId)
        }
    }
}
