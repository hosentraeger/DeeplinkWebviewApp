package com.example.deeplinkwebviewapp.data
import kotlinx.serialization.Serializable

@Serializable
data class SfmMobiResponse(
    val bankCodesSettings: BankCodesSettings? = null,
    val contactOptions: ContactOptions? = null,
    val agency: Agency? = null,
    val phone: Phone? = null,
    val nav: Nav? = null
)

@Serializable
data class BankCodesSettings(
    val status: String? = "",
    val hash: String? = "",
    val data: Map<String, BankData>? = emptyMap()
)

@Serializable
data class BankData(
    val cardControl: String? = "",
    val cookie: String? = "",
    val cookieLabel: String? = "",
    val openCookiesExtBrowser: Boolean? = false,
    val privacy: String? = "",
    val thirdPartyAccess: String? = "",
    val accountAlarm: String? = "",
    val migrateSMS2PushTAN: Boolean? = false,
    val obkw: String? = "",
    val postbox: String? = "",
    val serviceCenter: String? = "",
    val servlet: String? = "",
    val mbaOnboarding: String? = "",
    val applePayFKP: Boolean? = false,
    val limitAdjustment: String? = "",
    val diamondContent: String? = "",
    val solutionFinder: String? = ""
)

@Serializable
data class ContactOptions(
    val status: String? = "",
    val hash: String? = "",
    val data: ContactData? = null
)

@Serializable
data class ContactData(
    val options: List<Option>? = emptyList(),
    val general: General? = null
)

@Serializable
data class Option(
    val url: String? = "",
    val title: String? = "",
    val openexternal: Boolean? = false
)

@Serializable
data class General(
    val url: String? = "",
    val title: String? = "",
    val openexternal: Boolean? = false
)

@Serializable
data class Agency(
    val status: String? = "",
    val hash: String? = "",
    val data: AgencyData? = null
)

@Serializable
data class AgencyData(
    val blz: String? = "",
    val agb: String? = "",
    val dkhhu: Boolean? = false,
    val photoTransfer: Boolean? = false,
    val ibeacon: Boolean? = false,
    val ibeaconResolver: String? = "",
    val ibeaconSettings: String? = "",
    val imprint: String? = "",
    val H2HTransfer: Boolean? = false,
    val nativeWhitelist: Boolean? = false,
    val pfmSettings: String? = "",
    val pfmBudgets: String? = "",
    val pfmForecast: String? = "",
    val pfmAnalysis: String? = "",
    val pfmContractCheck: String? = "",
    val pfmKeywords: String? = "",
    val sdepotSupport: Boolean? = false,
    val servlet: String? = "",
    val whitelist: String? = "",
    val transactionSettings: String? = "",
    val uca: Boolean? = false,
    val stockMarket: String? = "",
    val stockSearch: String? = "",
    val stockNews: String? = "",
    val stockCurrency: String? = "",
    val dynatrace: Boolean? = false,
    val foreignTransfer: String? = "",
    val individualProducts: String? = "",
    val wero: Boolean? = false,
    val benefitsRegister: String? = ""
)

@Serializable
data class Phone(
    val status: String? = "",
    val hash: String? = "",
    val data: List<PhoneData>? = emptyList()
)

@Serializable
data class PhoneData(
    val blz: Long? = 0L,
    val number: String? = "",
    val name: String? = "",
    val info: String? = "",
    val availability: List<String>? = emptyList(),
    val sort: Int? = 0,
    val phoneId: Int? = 0,
    val type: String? = ""
)

@Serializable
data class Nav(
    val status: String? = "",
    val hash: String? = "",
    val data: List<NavData>? = emptyList()
)

@Serializable
data class NavData(
    val uuid: String? = "",
    val type: String? = "",
    val sort: Int? = 0,
    val label: String? = "",
    val markAsNew: Boolean? = false,
    val webView: Boolean? = false,
    val categories: List<Category> = emptyList(), // Default to empty list
    val lastUpdateSeconds: Long? = 0L,
    val navId: Int? = 0,
    val url: String? = ""
)

@Serializable
data class Category(
    val sort: Int? = 0,
    val title: String? = "",
    val items: List<Item>? = emptyList(),
    val categoryId: Int? = 0,
    val navId: Int? = 0
)

@Serializable
data class Item(
    val uuid: String? = "",
    val categoryId: Int? = 0,
    val sort: Int? = 0,
    val url: String? = "",
    val webView: Boolean? = false,
    val markAsNew: Boolean? = false,
    val hasTeaser: Boolean? = false,
    val lastUpdateSeconds: Long? = 0L,
    val pageId: Int? = 0,
    val teaser_headline: String? = "",
    val teaser_subheadline: String? = ""
)
