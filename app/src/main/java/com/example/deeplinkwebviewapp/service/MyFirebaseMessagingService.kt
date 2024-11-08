package com.example.deeplinkwebviewapp.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.HtmlCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.deeplinkwebviewapp.MyApplication
import com.example.deeplinkwebviewapp.data.AemBanner
import com.example.deeplinkwebviewapp.data.BankEntry
import com.example.deeplinkwebviewapp.data.PushNotificationPayload
import com.example.deeplinkwebviewapp.data.SfcIfResponse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Base64

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    private val myJsonDecoder: Json = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Log the message notification payload (falls vorhanden)
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Title: ${it.title}")
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        remoteMessage.data.let {
            Log.d(TAG, "Message data payload: $it")

            val pushPayloadStringB64 =
                remoteMessage.data["customKey1"] // Der Base64-codierte String

            if (pushPayloadStringB64 != null) {
                // Base64-Dekodierung
                val decodedBytes = Base64.getDecoder().decode(pushPayloadStringB64)

                val pushPayloadString =
                    String(decodedBytes, Charsets.UTF_8) // ByteArray in String umwandeln

                try {
                    // JSON-Deserialisierung
                    val pushNotificationPayload =
                        myJsonDecoder.decodeFromString<PushNotificationPayload>(pushPayloadString)
                    val title = pushNotificationPayload.title
                    val body = pushNotificationPayload.body
                    // Prüfen, ob IAM enthalten ist
                    if (pushNotificationPayload.iam != null) {
                        // Code ausführen, wenn IAM vorhanden ist
                        Log.d(TAG, "IAM detected: ${pushNotificationPayload.iam}")
                        if (pushNotificationPayload.iam.notificationImage != null) {
                            val sharedPreferences = getSharedPreferences(
                                "MyPreferences",
                                Context.MODE_PRIVATE
                            )

                            val blz = sharedPreferences.getString("BLZ", "").toString()
                            val stage = sharedPreferences.getString("SFStage", "").toString()
                            val productId = 444

                            val sfcService = SfcServiceFactory.create(
                                blz,
                                stage,
                                productId
                            )

                            sfcService.fetchVkaData(pushNotificationPayload.iam.contentId) { response: String? ->
                                response?.let {
                                    try {
                                        val sfcIfResponse: SfcIfResponse? =
                                            Json.decodeFromString(response)
                                        Log.d(TAG, "SfcIfData loaded")
                                        val ifData = sfcIfResponse?.services?.firstOrNull()?.IF
                                        val iamTitle = ifData?.disrupter?.headline?.let { it1 ->
                                            HtmlCompat.fromHtml(
                                                it1, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                                        }
                                        val iamBody = ifData?.disrupter?.text?.let { it1 ->
                                            HtmlCompat.fromHtml(
                                                it1, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                                        }

                                        when (pushNotificationPayload.iam.notificationImage) {
                                            "1" -> showNotificationWithImage(
                                                (if ( title == "" ) iamTitle else title),
                                                if ( body == "" ) iamBody else body,
                                                pushNotificationPayload,
                                                ifData?.overview?.get(0)?.banner
                                            )

                                            "2" -> {
                                                ifData?.confirmationBannerURL?.let { it1 ->
                                                    sfcService.fetchAemBanner(
                                                        it1
                                                    ) { response: AemBanner? ->
                                                        response?.let {
                                                            showNotificationWithImage(
                                                                if ( title == "" ) iamTitle else title,
                                                                if ( body == "" ) iamBody else body,
                                                                pushNotificationPayload,
                                                                response.banner
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            "3" -> showNotificationWithImage(
                                                if ( title == "" ) iamTitle else title,
                                                if ( body == "" ) iamBody else body,
                                                pushNotificationPayload,
                                                ifData?.disrupter?.image
                                            )

                                            "4" -> {
                                                ifData?.logoutPageURL?.let { it2 ->
                                                    sfcService.fetchAemPage(it2) { result ->
                                                        showNotificationWithImage(
                                                            if ( title == "" ) ifData.logoutPage?.headline else title,
                                                            if ( body == "" ) ifData.logoutPage?.text else body,
                                                            pushNotificationPayload,
                                                            result?.image
                                                        )
                                                    }
                                                }
                                            }
                                            else -> showNotification(
                                                if ( title == "" ) iamTitle else title,
                                                if ( body == "" ) iamBody else body,
                                                pushNotificationPayload,
                                                null
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            TAG,
                                            "Error processing SfmMobiResponse: ${e.localizedMessage}"
                                        )
                                    }
                                } ?: run {
                                    Log.e(TAG, "Fehler bei der Anfrage")
                                }
                            }
                        } else {
                            showNotification(
                                title,
                                body,
                                pushNotificationPayload,
                                null
                            )
                        }
                    }

                    if (pushNotificationPayload.webview != null) {
                        Log.d(TAG, "WEBVIEW detected: ${pushNotificationPayload.webview}")
                        showNotification(
                            title,
                            body,
                            pushNotificationPayload,
                            null
                        )
                    }

                    if (pushNotificationPayload.mailbox != null) {
                        // silent notification
                        Log.d(TAG, "MAILBOX detected: ${pushNotificationPayload.mailbox}")
                        if (MyApplication.isAppInForeground) {
                            broadcastNotificationIntent(title, body, pushNotificationPayload)
                        }
                        handleMailboxBadge(pushNotificationPayload)
                    }

                    if (pushNotificationPayload.balance != null) {
                        // wenn app im vordergrund, keine notification zeigen. das erledigt die app anders
                        if (MyApplication.isAppInForeground) {
                            broadcastNotificationIntent(title, body, pushNotificationPayload)
                        } else {
                            showNotification(
                                title,
                                body,
                                pushNotificationPayload,
                                null
                            )
                        }
                    }

                    if (pushNotificationPayload.update != null) {
                        Log.d(TAG, "UPDATE detected: ${pushNotificationPayload.update}")
                        // wenn app im vordergrund, ist keine benachrichtigung notwendig
                        if (!MyApplication.isAppInForeground) {
                            showNotification(
                                title,
                                body,
                                pushNotificationPayload,
                                null
                            )
                        }
                    }

                    if (pushNotificationPayload.ping != null) {
                        Log.d(TAG, "PING detected: ${pushNotificationPayload.ping}")
                        // TODO: hier ein Pong durchführen
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun showNotificationWithImage(title: String?, body: String?, pushNotificationPayload: PushNotificationPayload?, imageData: String?) {
        val decodedBytes = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
        val imageBitmap:Bitmap? = decodedBytes?.let { it1 -> BitmapFactory.decodeByteArray(decodedBytes, 0, it1.size) }
        GlobalScope.launch(Dispatchers.IO) {
            showNotification(
                title,
                body,
                pushNotificationPayload,
                imageBitmap)
        }
    }

    private fun handleMailboxBadge(pushNotificationPayload: PushNotificationPayload?) {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val obv = BankEntry(
            pushNotificationPayload?.blz ?: "",
            pushNotificationPayload?.obv ?: "")
        val blz = sharedPreferences.getString("BLZ", "") ?: ""
        val username = sharedPreferences.getString("Username", "") ?: ""
        val myMainObv = BankEntry(blz, username)
        if ( obv == myMainObv ) {
            val notificationId = 1
            val badgeCount = pushNotificationPayload?.mailbox?.count
            val channelId = getString(R.string.system_notification_channel_id)

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Kleines Icon festlegen
                .setContentTitle("Neue Nachricht")
                .setContentText("Du hast neue Nachrichten.")
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setNumber(badgeCount?:0) // Setzt die Anzahl für das Badge
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            // Zeige die Benachrichtigung an
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        }
    }

    private fun broadcastNotificationIntent(
        title: String?,
        body: String?,
        pushNotificationPayload: PushNotificationPayload?) {
        val intent = Intent("PUSH-NOTIFICATION-RECEIVED")  // Benenne das Intent entsprechend
        intent.putExtra("title", title)
        intent.putExtra("body", body)
        intent.putExtra("pushNotificationPayload", pushNotificationPayload)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun showNotification(
        title: String?,
        body: String?,
        pushNotificationPayload: PushNotificationPayload?,
        imageBitmap: Bitmap?) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = when {
            pushNotificationPayload?.iam != null -> getString(R.string.iam_notification_channel_id)
            pushNotificationPayload?.balance != null -> getString(R.string.account_alert_notification_channel_id)
            pushNotificationPayload?.mailbox != null -> getString(R.string.system_notification_channel_id)
            else -> getString(R.string.default_notification_channel_id)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("title", title)
            putExtra("body", body)
            putExtra("pushNotificationPayload", pushNotificationPayload)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Setze hier dein Icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Wenn das Bild vorhanden ist, setze es in die Benachrichtigung
        if (imageBitmap != null) {
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(imageBitmap)
                .bigLargeIcon(null as Bitmap?)) // Optional, um das große Icon zu entfernen
        }

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO Handle token registration with server here
    }
}
