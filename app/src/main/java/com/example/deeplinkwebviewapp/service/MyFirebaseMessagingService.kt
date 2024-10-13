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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.deeplinkwebviewapp.MyApplication
import com.example.deeplinkwebviewapp.data.BankEntry
import com.example.deeplinkwebviewapp.data.PushNotificationPayload
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
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        Log.d(TAG, "onMessageReceived: ${remoteMessage.toString()}")
        val pushPayloadStringB64 = remoteMessage.data["customKey1"] // Der Base64-codierte String
        if (pushPayloadStringB64 != null) {
            // Base64-Dekodierung
            val decodedBytes = Base64.getDecoder().decode(pushPayloadStringB64)
            val pushPayloadString = String(decodedBytes, Charsets.UTF_8) // ByteArray in String umwandeln
            try {
                // JSON-Deserialisierung
                val pushNotificationPayload =
                    myJsonDecoder.decodeFromString<PushNotificationPayload>(pushPayloadString)

                // Prüfen, ob IAM enthalten ist
                if (pushNotificationPayload.iam != null) {
                    // Code ausführen, wenn IAM vorhanden ist
                    Log.d(TAG, "IAM detected: ${pushNotificationPayload.iam}")
                    if (pushNotificationPayload.iam.useBanner == true) {
                        GlobalScope.launch(Dispatchers.IO) {
                            val imageBitmap =
                                BitmapFactory.decodeResource(resources, R.drawable.sample_banner)
                                showNotification(
                                    title,
                                    body,
                                    pushNotificationPayload,
                                    imageBitmap)
                        }
                    } else {
                        showNotification(
                            title,
                            body,
                            pushNotificationPayload,
                            null)
                    }
                }

                // Prüfen, ob WEBVIEW enthalten ist
                if (pushNotificationPayload.webview != null) {
                    // Code ausführen, wenn WEBVIEW vorhanden ist
                    Log.d(TAG, "WEBVIEW detected: ${pushNotificationPayload.webview}")
                    // Hier spezifische Logik für WEBVIEW hinzufügen
                }
                // Prüfen, ob MAILBOX enthalten ist
                if (pushNotificationPayload.mailbox != null) {
                    // Code ausführen, wenn MAILBOX vorhanden ist
                    Log.d(TAG, "MAILBOX detected: ${pushNotificationPayload.mailbox}")
                    if (MyApplication.isAppInForeground) {
                        broadcastNotificationIntent(title, body, pushNotificationPayload)
                    }
                    handleMailboxBadge(pushNotificationPayload)
                }

                // Prüfen, ob BALANCE enthalten ist
                if (pushNotificationPayload.balance != null ) {
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
                // Prüfen, ob UPDATE enthalten ist
                if (pushNotificationPayload.update != null) {
                    // Code ausführen, wenn UPDATE vorhanden ist
                    Log.d(TAG, "UPDATE detected: ${pushNotificationPayload.update}")
                    // Hier spezifische Logik für UPDATE hinzufügen
                }

                // Prüfen, ob PING enthalten ist
                if (pushNotificationPayload.ping != null) {
                    // Code ausführen, wenn PING vorhanden ist
                    Log.d(TAG, "PING detected: ${pushNotificationPayload.ping}")
                    // Hier spezifische Logik für PING hinzufügen
                }
            } catch (e: Exception ) {
                Log.e(TAG, e.toString())
            }
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
        val channelId = getString(R.string.default_notification_channel_id)

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
