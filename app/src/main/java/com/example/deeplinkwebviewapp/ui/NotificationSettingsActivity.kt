package com.example.deeplinkwebviewapp.ui

// NotificationSettingsActivity.kt
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.ui.adapter.NotificationChannelAdapter

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var explanationText: TextView
    private lateinit var channelList: RecyclerView
    private lateinit var openSettingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        explanationText = findViewById(R.id.explanation_text)
        channelList = findViewById(R.id.channel_list)
        openSettingsButton = findViewById(R.id.open_settings_button)

        setupChannelList()
        setupOpenSettingsButton()
    }

    private fun setupChannelList() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notificationChannels
        } else {
            emptyList()
        }

        channelList.layoutManager = LinearLayoutManager(this)
        channelList.adapter = NotificationChannelAdapter(channels)
    }

    private fun setupOpenSettingsButton() {
        openSettingsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName) // Setzt das aktuelle Paket
            }
            startActivity(intent) // Startet die Benachrichtigungseinstellungen
        }
    }
}
