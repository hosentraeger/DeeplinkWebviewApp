package `mipmap-xhdpi`.deeplinkwebviewapp.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.android.identity.util.UUID
import `mipmap-xhdpi`.deeplinkwebviewapp.data.DeviceData
import `mipmap-xhdpi`.deeplinkwebviewapp.data.DeviceDataSingleton
import `mipmap-xhdpi`.deeplinkwebviewapp.Logger
import `mipmap-xhdpi`.deeplinkwebviewapp.ui.http.MyHttpClient
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.service.SfcService
import com.example.deeplinkwebviewapp.service.SfcServiceFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle // Fügt ActionBarDrawerToggle hinzu
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var previousLogin: String
    private lateinit var sfcService: SfcService
    companion object {
        private const val TAG = "MainActivity"
    }

    fun getCurrentTimestamp(): String {
        // Aktuelles Datum und Zeit in UTC
        val currentDateTime = ZonedDateTime.now(java.time.ZoneOffset.UTC)

        // Formatierer für das gewünschte ISO 8601 Format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        // Datum und Zeit formatieren und als String zurückgeben
        return currentDateTime.format(formatter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContentView(R.layout.activity_main)

        // Berechtigungen für Benachrichtigungen anfragen (Android 13+)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Berechtigung anfordern
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
        val intent = intent
        if (intent != null && intent.extras != null) {
            Log.d(
                TAG,
                "App started from push notification with extras: " + intent.extras.toString()
            )
        }
        // Überprüfe die Absicht und speichere die Werte
        handleIntent(intent)

        val deviceData = `mipmap-xhdpi`.deeplinkwebviewapp.data.DeviceDataSingleton.deviceData

        // SharedPreferences initialisieren
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var editor = sharedPreferences.edit()
        if (false == sharedPreferences.getBoolean("valid", false)) {
            editor.putString("BLZ", getString(R.string.default_blz))
            editor.putString("Username", getString(R.string.default_username))
            editor.putString("PIN", getString(R.string.default_pin))
            editor.putString("MKALine", getString(R.string.default_mka))
            editor.putString("SFStage", getString(R.string.default_stage))
            editor.putString("App", getString(R.string.default_app))
            editor.putString("DeeplinkURL", getString(R.string.default_deeplink_url))
            editor.putString("PushId", deviceData.push_id)
            editor.putBoolean("valid", true) // "key_name" ist der Schlüssel, 'true' der Boolean-Wert
        }

        previousLogin = sharedPreferences.getString("LastLogin", deviceData.last_login).toString()
        deviceData.device_id = sharedPreferences.getString("DeviceId", deviceData.device_id).toString()
        if (deviceData.device_id == "" ) {
            editor.putString("DeviceId", UUID.randomUUID().toString())
        }
        deviceData.push_id = sharedPreferences.getString("PushId", deviceData.push_id).toString()
        deviceData.login_id = sharedPreferences.getString("Username", deviceData.login_id).toString()
        deviceData.last_login = getCurrentTimestamp()
        editor.commit()


        // Toolbar hinzufügen
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Showcase"

        // DrawerLayout und NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // ActionBarDrawerToggle initialisieren und Drawer-Layout hinzufügen
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()  // Burger-Button anzeigen

        // Menü-Item-Listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_function_appstart -> {
                    // Hier den POST-Request senden
                    sendDeviceData ( )
                    true
                }
                R.id.nav_function_rundruf -> {
                    true
                }
                R.id.nav_function_kontakt -> {
                    true
                }
                R.id.nav_function_vka -> {
                    // VKA-Daten abrufen
                    lifecycleScope.launchWhenStarted {
                        val userName = ""
                        val vkaResponse = sfcService.getVkaData(userName)

                        withContext(Dispatchers.Main) {
                            if (vkaResponse != null) {
                                // Verarbeite die VKA-Daten und zeige sie im TextView an
                            } else {
                                // Zeige eine Fehlermeldung im TextView an
                            }
                        }
                    }
                    true
                }
                R.id.nav_function_angebote -> {
                    true
                }
                R.id.nav_function_rundruf -> {
                    true
                }
                R.id.nav_function_webview -> {
                    val url = sharedPreferences.getString("DeeplinkURL", getString(R.string.default_deeplink_url))
                    if (url != null) {
                        openWebView(url)
                    }
                    true
                }
                R.id.nav_function_greensmilies -> {
                    openWebView(getString(R.string.greensmilies_url))
                    true
                }
                R.id.nav_function_message -> {
                    // Öffne Funktion 1
                    showAlertDialog()
                    true
                }
                R.id.nav_function_einstellungen -> {
                    val settings_intent = Intent(this, `mipmap-xhdpi`.deeplinkwebviewapp.ui.SettingsActivity::class.java)
                    startActivity(settings_intent)
                    true
                }
                R.id.nav_function_log -> {
                    val log_intent = Intent(this, `mipmap-xhdpi`.deeplinkwebviewapp.ui.LogActivity::class.java)
                    startActivity(log_intent)
                    true
                }
                else -> false
            }
        }

        // Deeplink verarbeiten
        if (intent?.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            if (data != null && data.path == "/_deeplink/showAlert") {
                showAlertDialog()
            }
        }

        // Firebase Messaging Token abrufen und in die Logs schreiben
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                // Token-Abruf fehlgeschlagen
                return@addOnCompleteListener
            }

            // FCM-Token abrufen
            val token = task.result

            // Hier kannst du das Token speichern oder zu deinem Server senden
            Log.d(TAG, "FCM Token: $token")

            // FCM-Token in SharedPreferences speichern
            editor = sharedPreferences.edit()
            editor.putString("FCMToken", token)
            editor.apply() // Async speichern

            // FCM Token ins Log schreiben und in der SettingsActivity anzeigen
            `mipmap-xhdpi`.deeplinkwebviewapp.Logger.log("FCM Token: $token")
            if ( deviceData.push_id != token ) {
                deviceData.push_id = token
                sendDeviceData ( )
            }
        }
        sfcService = SfcServiceFactory.create(
            sharedPreferences.getString("BLZ", "").toString(),
            sharedPreferences.getString("Stage", "").toString(),
            "6.8.0" )
    }

    override fun onNewIntent(intent: Intent) {
        Log.d("MainActivity", "onNewIntent called with intent: $intent")
        super.onNewIntent(intent)
        // Handle the new intent when the activity is already running
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun handleIntent(intent: Intent) {
        val extras = intent.extras
        if (extras != null) {
            // Logge die gesamten Extras für Debugging
            for (key in extras.keySet()) {
                val value = extras.getString(key)
                Log.d("PushNotification", "Key: $key, Value: $value")
            }

            // Jetzt kannst du die spezifischen Keys abrufen, wenn nötig
            val customKey1 = extras.getString("customKey1")
            val customKey2 = extras.getString("customKey2")
            Log.d("PushNotification", "customKey1: $customKey1, customKey2: $customKey2")
            showPusNotificationAlertDialog(customKey1,customKey2)

        } else {
            Log.d("PushNotification", "No extras found in the intent.")
        }
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hinweis")
        builder.setMessage("Dies ist ein wichtiger Hinweis!")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }


    private fun showPusNotificationAlertDialog(customKey1: String?, customKey2: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Benachrichtigung erhalten")
        builder.setMessage("Custom Key 1: $customKey1\nCustom Key 2: $customKey2")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openWebView(url: String) {
        val intent = Intent(this, `mipmap-xhdpi`.deeplinkwebviewapp.ui.WebViewActivity::class.java).apply {
            putExtra("EXTRA_URL", url) // Den Parameter hinzufügen
        }
        startActivity(intent)
    }

    // Optional: Behandle die Rücktaste, um den Drawer zu schließen, falls geöffnet
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    fun sendDeviceData() {
        val deviceData: `mipmap-xhdpi`.deeplinkwebviewapp.data.DeviceData = `mipmap-xhdpi`.deeplinkwebviewapp.data.DeviceDataSingleton.deviceData

        `mipmap-xhdpi`.deeplinkwebviewapp.ui.http.MyHttpClient.getInstance().postDeviceData(deviceData) { response ->
            if (response != null) {
                `mipmap-xhdpi`.deeplinkwebviewapp.Logger.log("Gerätedaten erfolgreich gesendet: $response")
            } else {
                `mipmap-xhdpi`.deeplinkwebviewapp.Logger.log("Fehler beim Senden der Gerätedaten.")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            // Überprüfe, ob die Benachrichtigungserlaubnis erteilt wurde
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Benachrichtigungsberechtigung erteilt
            } else {
                // Benachrichtigungsberechtigung abgelehnt
            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val channelDescriptionText = getString(R.string.default_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}