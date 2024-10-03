package com.example.deeplinkwebviewapp.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.deeplinkwebviewapp.MyApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage?.notification?.title
        val body = remoteMessage?.notification?.body
        val customKey1 = remoteMessage?.data?.get("customKey1")
        val customKey2 = remoteMessage?.data?.get("customKey2")
        Log.d(TAG, "Title: ${title}, Body: ${body}, customKey1: ${customKey1}, customKey2: ${customKey2}")

        if ( customKey1.equals("IAM") ) {
            showNotification(title, body, customKey1, customKey2, null )
        }

        if ( customKey1.equals("IAMBANNER")){
            // if iam banner
            fetchImageAndShowNotification(title,body,customKey1,customKey2)
        }

        if ( customKey1.equals("BALANCE") ) {
            if (MyApplication.isAppInForeground) {
                // Sende die Daten per Broadcast an die MainActivity
                val intent = Intent("push-notification-received")  // Benenne das Intent entsprechend
                intent.putExtra("customKey1", customKey1)
                intent.putExtra("customKey2", customKey2)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }

        if ( customKey1.equals("BADGE") ) {
        }

        if ( customKey1.equals("REVIEW") ) {
            showNotification(title, body, customKey1, customKey2, null )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchImageAndShowNotification(title: String?, message: String?, customKey1: String?, customKey2: String?) {
        // Hier kannst du eine Coroutine oder eine andere Methode verwenden,
        // um das Bild asynchron abzurufen.
        GlobalScope.launch(Dispatchers.IO) {
            val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_banner)
            showNotification(title, message, customKey1, customKey2, imageBitmap)
        }
    }

    private fun showNotification(title: String?, message: String?, customKey1: String?, customKey2: String?, imageBitmap: Bitmap?) {
        val NOTIFICATION_ID = System.currentTimeMillis().toInt()
        val CHANNEL_ID = getString(R.string.default_notification_channel_id)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("customKey1", customKey1)
            putExtra("customKey2", customKey2)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Setze hier dein Icon
            .setContentTitle(title)
            .setContentText(message)
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
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO Handle token registration with server here
    }
}
