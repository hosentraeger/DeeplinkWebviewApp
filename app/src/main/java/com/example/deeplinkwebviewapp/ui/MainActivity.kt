package com.example.deeplinkwebviewapp.ui

import com.example.deeplinkwebviewapp.viewmodel.MainViewModel
import com.example.deeplinkwebviewapp.viewmodel.MainViewModelFactory
import com.example.deeplinkwebviewapp.viewmodel.SettingsViewModel
import com.example.deeplinkwebviewapp.viewmodel.SettingsViewModelFactory
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.deeplinkwebviewapp.R
import com.google.android.material.navigation.NavigationView

import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var viewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    companion object {
        private const val TAG = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        // Firebase initialisieren
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        // Berechtigungen für Benachrichtigungen anfragen (Android 13+)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val factory = MainViewModelFactory(application, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        // Lade Preferences
        viewModel.initializePreferences()

        // Device-Daten initialisieren
        viewModel.initializeDeviceData()

        // Beobachte die LiveData für die Imagedaten
        viewModel.disrupterImageData.observe(this) { imageData ->
            // Hier kannst du die Bilddaten verarbeiten und z.B. ein ImageView aktualisieren
            if (imageData != null) {
                val intent = Intent(this, DisrupterActivity::class.java)
                intent.putExtra("imageData", imageData) // Base64-Bilddaten übergeben
                startActivity(intent)
            }
        }

        val settingsFactory = SettingsViewModelFactory(application, sharedPreferences)
        settingsViewModel = ViewModelProvider(this, settingsFactory).get(SettingsViewModel::class.java)

        // Toolbar und NavigationView einrichten
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Showcase"

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // NavigationItemSelectedListener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_function_webview -> {
                    openWebView(
                        sharedPreferences.getString(
                            "DeeplinkURL",
                            getString(R.string.default_deeplink_url)
                        )!!
                    )
                    true
                }

                R.id.nav_function_vka -> {
                    val userId = settingsViewModel.getPersonennummer()
                    if (userId != null ) {
                        viewModel.loadVkaData(userId)
                    }
                    true
                }

                R.id.nav_function_greensmilies -> {
                    openWebView(getString(R.string.greensmilies_url))
                    true
                }

                R.id.nav_function_einstellungen -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                R.id.nav_function_log -> {
                    startActivity(Intent(this, LogActivity::class.java))
                    true
                }

                else -> {
                    val handled = viewModel.onNavItemSelected(menuItem.itemId)
                    if (handled) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                        true
                    } else {
                        false
                    }
                }
            }
        }

        intent?.let {
            handleIntent(it)
        }

        // Abrufen des FCM Tokens
        viewModel.fetchFcmToken()

        // Beobachten des FCM Tokens
        viewModel.fcmToken.observe(this) { token ->
            if (token != null) {
                // Token erfolgreich abgerufen
                Log.d(TAG, "Token: $token")
                // Hier kannst du den Token verwenden, z.B. an deinen Server senden
            } else {
                // Fehler beim Abrufen des Tokens
                Log.d(TAG, "Failed to retrieve token")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Berechtigung erteilt
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val channelDescriptionText =
                getString(R.string.default_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data
        val extras = intent.extras

        if (data != null) {
            Log.d(TAG, "Opened via Deep Link: $data")
            handleDeeplink(intent)
        } else if (extras != null) {
            // Ausgabe aller erhaltenen Extras
            for (key in extras.keySet()) {
                Log.d(TAG, "Intent extra: $key = ${extras.get(key)}")
            }

            // Hier kannst du spezifische Schlüssel prüfen
            if (extras.containsKey("your_custom_key")) {
                Log.d(TAG, "Opened via Push Notification with your_custom_key")
                handlePushNotification(intent)
            } else {
                Log.d(TAG, "Opened via Push Notification, but no 'from' key found")
                handlePushNotification(intent)
            }
        } else {
            Log.d(TAG, "Opened normally")
        }
    }

    // Deeplink-Verarbeitung
    private fun handleDeeplink(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && data.path == "/_deeplink/showAlert") {
            showAlertDialog() // Zeigt den Deeplink-Dialog
        }
    }

    // Push-Benachrichtigungs-Verarbeitung
    private fun handlePushNotification(intent: Intent?) {
        // Hier kannst du die Logik für die Verarbeitung der Push-Benachrichtigung hinzufügen.
        // Angenommen, die Benachrichtigung hat 'customKey1' und 'customKey2' als Payload
        val customKey1 = intent?.getStringExtra("customKey1")
        val customKey2 = intent?.getStringExtra("customKey2")

        if ( customKey1 == "IAM" && customKey2 != null ) {
            viewModel.loadVkaData(customKey2)
        } else if (customKey1 != null || customKey2 != null) {
            showPusNotificationAlertDialog(customKey1, customKey2) // Zeigt den Benachrichtigungsdialog
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hinweis")
            .setMessage("Dies ist ein wichtiger Hinweis!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showPusNotificationAlertDialog(customKey1: String?, customKey2: String?) {
        AlertDialog.Builder(this)
            .setTitle("Benachrichtigung erhalten")
            .setMessage("Custom Key 1: $customKey1\nCustom Key 2: $customKey2")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openWebView(url: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra("EXTRA_URL", url)
        }
        startActivity(intent)
    }
}