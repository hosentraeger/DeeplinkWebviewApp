package com.example.deeplinkwebviewapp.data
import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Parcelize
@Serializable
data class SfcIfResponse(
    val services: List<Service>
) : Parcelable

@Parcelize
@Serializable
data class Service(
    val IF: IFData? = null
) : Parcelable

@Parcelize
@Serializable
data class IFData(
    val status: String? = null,
    val id: Int? = null,
    val version: Int? = null,
    val overview: List<AemBanner>? = null,
    val disrupter: AemPage? = null,
    val logoutPageURL: String? = null,
    val logoutPage: AemPage?= null,
    val confirmationBannerURL: String? = null,
    val confirmationBanner: AemBanner? = null,
    val eventId: String? = null,
    val persNr: String? = null,
    val mkaId: String? = null
) : Parcelable

@Parcelize
@Serializable
data class AemBanner(
    val banner: String? = null,
    val imgAlt: String? = null,
    val url: String? = null,
    val openOutsideApp: Boolean? = null,
    val aemAsset: Boolean? = null
) : Parcelable

@Parcelize
@Serializable
data class AemPage(
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
) : Parcelable

@Parcelize
@Serializable
data class Link(
    val title: String? = null,
    val url: String? = null,
    val openOutsideApp: Boolean? = null,
    val highlighted: Boolean? = null,
    val altText: String? = null
) : Parcelable
