package com.example.deeplinkwebviewapp

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

enum class STAGE { STAGE_RHEIN, STAGE_BETA, STAGE_PROD }

@Serializable
data class VkaResponse(
    val services: List<Service>
)

@Serializable
data class Service(
    val IF: IfService
)

@Serializable
data class IfService(
    val status: String,
    val id: Int,
    val version: Int,
    val overview: List<OverviewItem>,
    val disrupter: Disrupter,
    val logoutPageURL: String,
    val eventId: String,
    val persNr: String,
    val mkaId: String
)

@Serializable
data class OverviewItem(
    val banner: String,
    val imgAlt: String,
    val url: String,
    val openOutsideApp: Boolean,
    val aemAsset: Boolean
)

@Serializable
data class Disrupter(
    val image: String,
    val imgAlt: String,
    val headline: String,
    val richHeadline: Boolean,
    val text: String,
    val richText: Boolean,
    val aemAsset: Boolean,
    val firstLink: Link,
    val forwardLink: Link,
    val noInterestLink: Link
)

@Serializable
data class Link(
    val title: String,
    val url: String,
    val openOutsideApp: Boolean,
    val altText: String? = null,
    val highlighted: Boolean
)

class SfcService(
    private val stage: STAGE,
    private val blz: String,
    private val productId: String
) {
    companion object {
        const val INTERFACE_VERSION = "v2020.02"
    }

    private val client = OkHttpClient()

    suspend fun getVkaData(userName: String): VkaResponse? {
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
                Json.decodeFromString(response.body!!.string())
            } else {
                null
            }
        } catch (e: Exception) {
            null
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
            if ( strStage == "RHEIN")  stage = STAGE.STAGE_RHEIN
            if ( strStage == "BETA")  stage = STAGE.STAGE_BETA
            return SfcService(stage, blz, productId) // Replace PRODUCT_ID with your actual value
        }
    }
}
