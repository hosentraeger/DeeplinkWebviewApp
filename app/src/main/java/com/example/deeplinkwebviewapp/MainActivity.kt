package com.example.deeplinkwebviewapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.preference.PreferenceManager
import com.android.identity.util.UUID
import com.google.firebase.messaging.FirebaseMessaging

import com.google.gson.Gson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.example.deeplinkwebviewapp.DeviceDataSingleton
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle // Fügt ActionBarDrawerToggle hinzu
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var previousLogin: String
    private val gson = Gson()

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
        setContentView(R.layout.activity_main)
        val deviceData = DeviceDataSingleton.deviceData

        // SharedPreferences initialisieren
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
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
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_function_log -> {
                    val intent = Intent(this, LogActivity::class.java)
                    startActivity(intent)
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
            Log.d("FCMToken", "FCM Token: $token")

            // FCM-Token in SharedPreferences speichern
            val editor = sharedPreferences.edit()
            editor.putString("FCMToken", token)
            editor.apply() // Async speichern

            // FCM Token ins Log schreiben und in der SettingsActivity anzeigen
            Logger.log("FCM Token: $token")
            if ( deviceData.push_id != token ) {
                deviceData.push_id = token
                sendDeviceData ( )
            }
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

    private fun openWebView(url: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
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
        val deviceData: DeviceData = DeviceDataSingleton.deviceData

        MyHttpClient.getInstance().postDeviceData(deviceData) { response ->
            if (response != null) {
                Logger.log("Gerätedaten erfolgreich gesendet: $response")
            } else {
                Logger.log("Fehler beim Senden der Gerätedaten.")
            }
        }
    }
}