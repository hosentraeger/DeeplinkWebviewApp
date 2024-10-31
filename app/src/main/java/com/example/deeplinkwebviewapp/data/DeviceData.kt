package com.example.deeplinkwebviewapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DeviceData(
    var userData : UserData,
    var deviceReport : DeviceReport? = null,
    var featureSettings : FeatureSettings? = null,
    var appUsageStatistics : AppUsageStatistics? = null,
    var appMetrics : AppMetrics? = null,
    var deviceMetaData: DeviceMetaData? = null
) : Parcelable

@Parcelize
@Serializable
data class UserData(
    var push_id: String = "<not assigned>",
    var login_id: String = "<nobody>",
    var last_login: String = "1971-01-01T00:00:00Z",
) : Parcelable

@Parcelize
@Serializable
data class DeviceReport(
    var model: String? = null,
    var osVersion: String? = null,
    var appVariant: String? = null,
    var appVersion: String? = null,
    var wero: Boolean? = null,
    var kwitt: Boolean? = null,
    var applePay: Boolean? = null,
    var aloha: Boolean? = null,
    var systemLanguage: String? = null,
    var budgetBook: Boolean? = null,
    var pushNotificationSettings : PushNotificationSettings? = null,
    var modified: Boolean = false
) : Parcelable

@Parcelize
@Serializable
data class PushNotificationSettings(
    var system : Boolean = false,
    var individualOffers : Boolean = false,
    var accountAlarm : Boolean = false,
    var notificationBadge : Boolean = false,
    var lastPingNotificiationReceived : String? = null
) : Parcelable

@Parcelize
@Serializable
data class FeatureSettings(
    var appLanguage: String? = null,
    var theme: String? = null,
    var maxSessionDuration: Int? = null,
    var autoUpateBalance: Boolean? = null,
    var accountSorting: String? = null,
    var modified: Boolean = false
) : Parcelable

@Parcelize
@Serializable
data class AppUsageStatistics(
    var institutionsAndAccounts: InstitutionsAndAccounts? = null,
    var featuresRequiringAttention: List<String>? = null,
    var anonymized: Boolean = false,
    var modified: Boolean = false
) : Parcelable

@Parcelize
@Serializable
data class InstitutionsAndAccounts(
    var mkaAccounts: Int = 0,
    var savingBanks: Int = 0,
    var savingBanksAccounts: Int = 0,
    var mbfAccounts: Int = 0,
    var otherAccounts: Int = 0
) : Parcelable

@Parcelize
@Serializable
data class AppMetrics(
    var fullAppStartsSinceLastCommit: Int = 0,
    var fullAppStartsMsSinceLastCommit: Int = 0,
    var subsequentAppStartsSinceLastCommit: Int = 0,
    var subsequentAppStartsMsSinceLastCommit: Int = 0,
    var anonymized: Boolean = false
) : Parcelable

@Parcelize
@Serializable
data class DeviceMetaData(
    var deviceId: String,
    var lastConnect: String,
    var lastCommitIp: String,
    var lastCommit: String,
    var deviceDataCommitted: Boolean
) : Parcelable

@Parcelize
@Serializable
data class DeviceDataComputedValues(
    var appStartsTotal: Int,
    var appStartsThisMonth: Int,
    var appStartsLastMonth: Int,
    var averageUpdateMsFirstThisMonth: Int,
    var averageUpdateMsFirstLastMonth: Int,
    var averageUpdateMsSubsequentLastMonth: Int,
    var initialConnect: String,
    var initialConnectIp: String
) : Parcelable

@Parcelize
@Serializable
data class DeviceFeatures(
    var modifiedBackend: Boolean,
    var dynatrace: Boolean,
    var acceptTermsConditions: Boolean,
    var reviewPersonalData: Boolean,
    var incomeExpenseWidgetVariant: String,
    var click2Credit: Boolean,
    var click2CreditVariant: String,
    var aroundTheProperty: Boolean,
    var privateBanking: Boolean,
    var moneyBoxFieldTesting: Boolean,
    var mergerAssistant: Boolean
) : Parcelable
