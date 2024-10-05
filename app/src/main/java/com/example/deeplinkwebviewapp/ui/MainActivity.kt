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
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
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
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.deeplinkwebviewapp.service.SilentLoginAndAdvisorDataServiceFactory
import androidx.activity.viewModels

class MainActivity : AppCompatActivity(), ChooseInstitionBottomSheet.OnChoiceSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var viewModel: MainViewModel
    private val settingsViewModel: SettingsViewModel by viewModels {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        SettingsViewModelFactory(application, sharedPreferences)
    }
    companion object {
        private const val TAG = "MainActivity"
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Daten aus dem Intent holen
            val customKey1 = intent.getStringExtra("customKey1")
            val customKey2 = intent.getStringExtra("customKey2")
            Toast.makeText(this@MainActivity, "Message received: $customKey1 - $customKey2", Toast.LENGTH_LONG).show()
            when ( customKey1 ) {
                "mailbox" -> {
                    val obv = customKey2?.substringBefore(":")
                    val messageCount = customKey2?.substringAfter(":")
                    true
                }
                "balance" -> {
                    val iban = customKey2?.substringBefore(":")
                    val balance = customKey2?.substringAfter(":")
                    true
                }
                "transaction" -> {
                    true
                }
                "webviewwithsilentlogin" -> {
                    val url = customKey2
                    true
                }
                "review" -> true
                "killswitch" -> true
                "update" -> true
                "security" -> true
                "feature" -> true
                "retrospect" -> true
                "instantpayment" -> true
                "geo" -> true

                else -> true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        // Firebase initialisieren
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        // Zeige den Login-Dialog, bevor die App startet
        showLoginDialog()

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

        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val factory = MainViewModelFactory(application, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        // Lade Preferences
        viewModel.initializePreferences()

        // Device-Daten initialisieren
        viewModel.initializeDeviceData()

        // Beobachte die LiveData für die Imagedaten
        viewModel.disrupterData.observe(this) { disrupterData ->
            if (disrupterData != null) {
                val intent = Intent(this, DisrupterActivity::class.java)
                intent.putExtra("disrupterDataJson", disrupterData)
                startActivity(intent)
            }
        }

        // val settingsFactory = SettingsViewModelFactory(application, sharedPreferences)
        // settingsViewModel = ViewModelProvider(this, settingsFactory).get(SettingsViewModel::class.java)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Toolbar und NavigationView einrichten
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Showcase"

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
            handleNavigationItemSelected(menuItem)
        }

        // Abrufen des FCM Tokens
        viewModel.fetchFcmToken()

        // Beobachten des FCM Tokens
        viewModel.fcmToken.observe(this) { token ->
            if (token != null) {
                // Token erfolgreich abgerufen
                Log.d(TAG, "Token: $token")
            } else {
                // Fehler beim Abrufen des Tokens
                Log.d(TAG, "Failed to retrieve token")
            }
        }
        // Registriere den BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            messageReceiver, IntentFilter("push-notification-received")
        )
        viewModel.mobiDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) {
                // Jetzt kannst du auf die Mobi-Daten zugreifen und sie verwenden
                val mailboxUrl = viewModel.getMailboxUrl()
                initializeSilentLoginAndAdvisorDataService ( )
                // Weiterverarbeitung
            } else {
                // Fehlerbehandlung oder Fallback
            }
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem): Boolean {
        val handled = when (menuItem.itemId) {
            R.id.nav_function_appstart -> {
                viewModel.sendDeviceData()
                viewModel.loadMobiData()
                true
            }
            R.id.nav_function_rundruf -> {
                true
            }
            R.id.nav_function_kontakt -> {
                // TEST
                // handleGenericWebviewDeeplink("/_deeplink/webview?path=%2Fde%2Fhome%2Fproducts%2Fmopedversicherung.webview.html&blz=choice&fallback=IF&IF_SILENT_LOGIN=true&wstart=true&n=true")
                true
            }
            R.id.nav_function_angebote -> {
                openWebView(viewModel.getMailboxUrl(), true)
                true
            }
            R.id.nav_function_deeplinks -> {
                openWebView(viewModel.getDeeplinksWebviewUrl(), false)
                true
            }
            R.id.nav_function_webview_greensmilies -> {
                openWebView(getString(R.string.greensmilies_url), false)
                true
            }
            R.id.nav_function_stoerer -> {
                // Rufe die DisrupterActivity mit den Standardwerten auf
                val intent = Intent(this, DisrupterActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.nav_function_deeplink_alert -> {
                showDeeplinkAlertDialog()
                true
            }
            R.id.nav_function_log -> {
                startActivity(Intent(this, LogActivity::class.java)) // Aktion für Log
                true
            }
            R.id.nav_function_einstellungen -> {
                startActivity(Intent(this, SettingsActivity::class.java)) // Aktion für Einstellungen
                true
            }
            R.id.nav_function_logout -> {
                finishAffinity() // Beendet alle Aktivitäten der App
                true
            }
            else -> false
        }

        // Schließt das Drawer-Layout nach der Auswahl
        drawerLayout.closeDrawer(GravityCompat.START)
        return handled // Rückgabe des Behandlungsstatus
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                toggle.onOptionsItemSelected(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // menuInflater.inflate(R.menu.drawer_menu, menu) // Inflate the menu
        return true
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
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
            if (extras.containsKey("customKey1")) {
                Log.d(TAG, "Opened via Push Notification with your_custom_key")
                handlePushNotification(intent)
            }
        } else {
            Log.d(TAG, "Opened normally")
        }
    }

    private fun handleDeeplink(intent: Intent?) {
        val deeplinkRootUri = getString(R.string.deeplink_root_uri)
        val data: Uri? = intent?.data
        if (data != null ) {
            when {
                data.path!!.startsWith("/_deeplink/webview") -> {
                    handleGenericWebviewDeeplink(intent?.data!!)
                    true
                }
                data.path!!.startsWith("/_deeplink/iam")  -> {
                    true
                }
                data.path!!.startsWith("/_deeplink/showAlert") -> {
                    showDeeplinkAlertDialog()
                    true
                }
                data.path!!.startsWith("/_deeplink") -> {
                    showDeeplinkAlertDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun handlePushNotification(intent: Intent?) {

        val customKey1 = intent?.getStringExtra("customKey1")
        val customKey2 = intent?.getStringExtra("customKey2")

        val handled = when (customKey1) {
            "IAM" -> {
                if (customKey2 != null) {
                    viewModel.loadVkaData(customKey2)
                }
                true
            }
            "IAMBANNER" -> {
                Toast.makeText(this, "Start aus IAM Banner, checke URL", Toast.LENGTH_SHORT).show()
                true
            }
            "BALANCE" -> {
                Toast.makeText(this, "Kontostand wird aktualisiert", Toast.LENGTH_SHORT).show()
                true
            }
            "BADGE" -> {
                Toast.makeText(this, "Badge-Counter wird aktualisiert", Toast.LENGTH_SHORT).show()
                true
            }
            "REVIEW" -> {
                Toast.makeText(this, "Bitte bewerte die App", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    private fun showDeeplinkAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hinweis")
            .setMessage("Dies ist ein wichtiger Hinweis!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openWebView(url: String, isSilentLogin: Boolean) {
        if (url.isNotBlank()) { // Überprüfe, ob die URL nicht leer ist
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("EXTRA_URL", url)
                putExtra("IF_SILENT_LOGIN", isSilentLogin)
            }
            startActivity(intent)
        } else {
            // Optional: Eine Nachricht anzeigen oder eine andere Aktion ausführen
            Toast.makeText(this, "Die URL ist leer.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.password_input)

        AlertDialog.Builder(this)
            .setTitle("App-Login")
            .setView(dialogView)
            .setPositiveButton("Login") { dialog, _ ->
                val inputPassword = passwordInput.text.toString()
                val correctPassword = getString(R.string.app_password)

                dialog.dismiss()
                // Starte die Hauptaktivität oder setze den Status auf eingeloggt
                proceedToMainApp()
            }
            .setNegativeButton("Abbrechen") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun proceedToMainApp() {
        // Logik zum Starten der App nach erfolgreichem Login
        Log.d("MainActivity", "Login erfolgreich!")
        viewModel.sendDeviceData()
        viewModel.loadMobiData()
        intent?.let {
            handleIntent(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Den BroadcastReceiver abmelden, um Speicherlecks zu vermeiden
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    private fun initializeSilentLoginAndAdvisorDataService() {
        val servletUrl = viewModel.getServletUrl()
        val blz = settingsViewModel.getBLZ()
        val loginName = settingsViewModel.getUsername()
        val onlineBankingPin = settingsViewModel.getPIN()

        if (servletUrl.isNotEmpty() && blz.isNotEmpty() && loginName.isNotEmpty() && onlineBankingPin.isNotEmpty()) {
            // Initialisiere die Factory
            SilentLoginAndAdvisorDataServiceFactory.initialize(
                servletUrl = servletUrl,
                blz = blz,
                loginName = loginName,
                onlineBankingPin = onlineBankingPin,
                lifecycleScope = lifecycleScope // Verwende den Activity-LifecycleScope
            )
        } else {
            Log.d(TAG, "Error: Missing parameters for SilentLoginAndAdvisorDataService initialization.")
        }
    }

    fun handleGenericWebviewDeeplink(deeplinkUri: Uri) {
        var isSilentLogin = false
        var blz: String? = null

        val uriBuilder = Uri.parse("https://" + viewModel.getHostname()).buildUpon()
        val queryParameterNames = deeplinkUri.queryParameterNames
        for (paramName in queryParameterNames) {
            val paramValue = deeplinkUri.getQueryParameter(paramName)
            println("Parameter: $paramName, Wert: $paramValue")
            when ( paramName ) {
                "path" -> {
                    val decodedPath = Uri.decode(paramValue) // Dekodieren des Wertes
                    uriBuilder.appendEncodedPath(decodedPath.trim('/'))
                }
                "fallback"-> {}
                "blz"-> blz = paramValue
                "IF_SILENT_LOGIN" -> isSilentLogin = (paramValue=="true")
                else -> uriBuilder.appendQueryParameter(paramName, paramValue)
            }
        }
        val targetUri = uriBuilder.build().toString()
        if (blz=="choice") {
            val myblz = settingsViewModel.getBLZ()
            val items = arrayOf("25050180", "10020030", "94059549", "${myblz}")
            val bottomSheet = ChooseInstitionBottomSheet(items, targetUri, isSilentLogin)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        } else {
            onChoiceSelected(blz, targetUri, isSilentLogin)
        }
    }
    // Implementierung der Schnittstelle
    override fun onChoiceSelected(blz: String?, targetUri: String, isSilentLogin: Boolean) {
        // Hier kannst du die Auswahl verarbeiten
        if (blz == null || blz==settingsViewModel.getBLZ()) {
            Log.d(TAG, "starting webview with uri ${targetUri}")
            openWebView(targetUri, isSilentLogin)
        } else {
            Toast.makeText(this@MainActivity, "ungültige BLZ ${blz}", Toast.LENGTH_LONG).show()
        }
    }
}
