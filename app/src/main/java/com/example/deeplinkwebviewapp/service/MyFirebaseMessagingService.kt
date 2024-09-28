package `mipmap-xxhdpi`.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Log source of message
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Extract custom keys
            val customKey1 = remoteMessage.data["customKey1"]
            val customKey2 = remoteMessage.data["customKey2"]

            // If custom keys are present, perform the action
            if (customKey1 != null && customKey2 != null) {
                handleCustomAction(customKey1, customKey2)
            }
        }

        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body, remoteMessage.data["customKey1"], remoteMessage.data["customKey2"])
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO Handle token registration with server here
    }

    private fun handleCustomAction(customKey1: String, customKey2: String) {
        // Handle the custom action based on customKey1 and customKey2
        Log.d(
            TAG,
            "Handling custom action with customKey1: $customKey1 and customKey2: $customKey2"
        )

        // Example: Start MainActivity and pass the custom keys as extras
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("customKey1", customKey1)
            putExtra("customKey2", customKey2)
            putExtra("showAlert", true)  // Füge diese Zeile hinzu
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun showNotification(title: String?, message: String?, customKey1: String?, customKey2: String?) {
        // Erstelle ein Intent, das die App öffnet, wenn die Benachrichtigung angeklickt wird
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("customKey1", customKey1)
            putExtra("customKey2", customKey2)
            putExtra("showAlert", true)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Baue und zeige die Benachrichtigung
        val channelId = getString(R.string.default_notification_channel_id)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "New Notification")
            .setContentText(message ?: "You have received a new message.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message ?: "You have received a new message."))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Enable sound, vibration, and lights

        // Zeige die Benachrichtigung
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(0, builder.build())
        }
    }
}
