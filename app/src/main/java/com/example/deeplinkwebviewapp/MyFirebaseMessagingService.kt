package com.example.deeplinkwebviewapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Log für eingehende Nachrichten
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Überprüfen, ob die Nachricht eine Benachrichtigung enthält
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(title: String?, message: String?) {
        val channelId = "my_channel_id"
        val notificationId = 1

        // Intent zur MainActivity erstellen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Notification erstellen
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Verwende das Standard-Icon
            .setContentTitle(title ?: "Neue Nachricht") // Titel der Notification
            .setContentText(message ?: "Sie haben eine neue Nachricht erhalten.") // Nachrichtentext
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Priorität der Notification
            .setContentIntent(pendingIntent) // Intent, der beim Klick auf die Notification geöffnet wird
            .setAutoCancel(true) // Die Notification wird automatisch entfernt, wenn der Benutzer darauf klickt

        // Berechtigungsprüfung für Benachrichtigungen ab Android 13 (API-Level 33)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Wenn die Berechtigung nicht erteilt wurde, breche den Vorgang ab
            return
        }

        // NotificationChannel für Android O und höher (API-Level 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Benachrichtigungskanal"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Notification anzeigen
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // Hier kannst du den neuen Token an deinen Server senden
    }
}
