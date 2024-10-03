package com.example.deeplinkwebviewapp.data
import kotlinx.serialization.Serializable

@Serializable
data class SfcIfResponse(
    val services: List<Service>
)

@Serializable
data class Service(
    val IF: IFData? = null
)

@Serializable
data class IFData(
    val status: String? = null,
    val id: Int? = null,
    val version: Int? = null,
    val overview: List<Overview>? = null,
    val disrupter: Disrupter? = null,
    val logoutPageURL: String? = null,
    val confirmationBannerURL: String? = null,
    val eventId: String? = null,
    val persNr: String? = null,
    val mkaId: String? = null
)

@Serializable
data class Overview(
    val banner: String? = null,
    val imgAlt: String? = null,
    val url: String? = null,
    val openOutsideApp: Boolean? = null,
    val aemAsset: Boolean? = null
)

@Serializable
data class Disrupter(
    val image: String? = null,
    val imgAlt: String? = null,
    val headline: String? = null,
    val richHeadline: Boolean? = null,
    val text: String? = null,
    val richText: Boolean? = null,
    val aemAsset: Boolean? = null,
    val firstLink: Link? = null,
    val secondLink: Link? = null,
    val thirdLink: Link? = null,
    val forwardLink: Link? = null,
    val noInterestLink: Link? = null
)

@Serializable
data class Link(
    val title: String? = null,
    val url: String? = null,
    val openOutsideApp: Boolean? = null,
    val highlighted: Boolean? = null,
    val altText: String? = null
)
