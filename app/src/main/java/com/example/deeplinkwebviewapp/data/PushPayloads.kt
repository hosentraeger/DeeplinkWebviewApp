package com.example.deeplinkwebviewapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PushNotificationPayload(
    val blz: String? = null,
    val obv: String? = null,
    val title: String? = null,
    val body: String? = null,
    val webview: WebviewPayload? = null,
    val iam: IamPayload? = null,
    val mailbox: MailboxPayload? = null,
    val balance: BalancePayload? = null,
    val update: UpdatePayload? = null,
    val ping: PingPayload? = null
) : Parcelable

@Parcelize
@Serializable
data class WebviewPayload(
    val path: String
) : Parcelable

@Parcelize
@Serializable
data class IamPayload(
    val contentId: String,
    val notificationImage: String? = null,
    val overlayImage : String? = null,
    val expectFeedbackIfDisplayed: Boolean? = false,
    val expectFeedbackIfHit: Boolean? = false
) : Parcelable

@Parcelize
@Serializable
data class MailboxPayload(
    val count: Int
) : Parcelable

@Parcelize
@Serializable
data class BalancePayload(
    val iban: String,
    val balance: String
) : Parcelable

@Parcelize
@Serializable
data class UpdatePayload(
    val fromVersion: String
) : Parcelable

@Parcelize
@Serializable
class PingPayload  : Parcelable
