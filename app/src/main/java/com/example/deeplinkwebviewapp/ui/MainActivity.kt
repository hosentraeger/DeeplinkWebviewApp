package com.example.deeplinkwebviewapp.ui

import com.example.deeplinkwebviewapp.viewmodel.MainViewModel
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.google.firebase.FirebaseApp
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.deeplinkwebviewapp.service.SilentLoginAndAdvisorDataServiceFactory
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.deeplinkwebviewapp.data.BankEntry
import com.example.deeplinkwebviewapp.data.IamPayload
import com.example.deeplinkwebviewapp.data.PushNotificationPayload
import com.google.android.material.navigation.NavigationView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.example.deeplinkwebviewapp.service.Logger
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.NavHostFragment
import com.example.deeplinkwebviewapp.data.SfcIfResponse
import com.example.deeplinkwebviewapp.service.MkaSession
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.deeplinkwebviewapp.viewmodel.AccountSettingsViewModel
import com.example.deeplinkwebviewapp.viewmodel.AccountSettingsViewModelFactory
import com.example.deeplinkwebviewapp.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity(), ChooseInstitionBottomSheet.OnChoiceSelectedListener {
    private val REQUEST_NOTIFICATION_PERMISSION_CODE = 101

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var viewModel: MainViewModel
    private val accountSettingsViewModel: AccountSettingsViewModel by viewModels {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        AccountSettingsViewModelFactory(application, sharedPreferences)
    }
    private lateinit var mkaSession: MkaSession

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var productsFragment: ProductsFragment

    @androidx.annotation.OptIn(ExperimentalBadgeUtils::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productsFragment = ProductsFragment()
        createNotificationChannel()
        createSystemNotificationChannel()
        createAccountAlertNotificationChannel()
        createIamNotificationChannel()

        // Firebase initialisieren
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        // Setze den OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Überprüfen, ob das Navigation Drawer geöffnet ist
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Wenn der Drawer nicht geöffnet ist, die Aktivität schließen
                    isEnabled = false // Deaktiviert den Callback
                    finish() // Aktivität schließen
                }
            }
        })
// Bottom Navigation konfigurieren
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

// Setze den NavController für die BottomNavigationView
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    navController.navigate(R.id.nav_dashboard) // Fragment für Dashboard anzeigen
                    true
                }
                R.id.nav_products -> {
                    navController.navigate(R.id.nav_products)
                    true
                }
                R.id.nav_service -> {
                    navController.navigate(R.id.nav_service) // Fragment für Service anzeigen
                    true
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.nav_profile) // Fragment für Profil anzeigen
                    true
                }
                else -> false
            }
        }

        showPermissionExplanationDialog()

        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val factory = MainViewModelFactory(application, sharedPreferences)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        // Lade Preferences
        viewModel.initializePreferences()

        // Device-Daten initialisieren
        viewModel.initializeDeviceData()

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // val badge = navView.getOrCreateBadge(R.id.nav_function_angebote)
        // badge.number = 5 // Setze die Badge-Zahl
        // alternative implementierung ->
        val badge = BadgeDrawable.create(this)
        badge.number = 5 // Beispiel, Badge-Nummer einstellen
        badge.isVisible = true
        val menuItem = navView.menu.findItem(R.id.nav_function_angebote)
        menuItem.actionView?.let { actionView ->
            BadgeUtils.attachBadgeDrawable(badge, actionView)
        }
        // <- alternative implementierung

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
        navView.setNavigationItemSelectedListener { myMenuItem ->
            handleNavigationItemSelected(myMenuItem)
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

        // Zeige den Login-Dialog, bevor die App startet
        showLoginDialog()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionExplanationDialog() {
        // Berechtigungen für Benachrichtigungen anfragen (Android 13+)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Benachrichtigungen")
            builder.setMessage("Wir möchten dir Push-Benachrichtigungen senden, damit du immer informiert bist. Du kannst die Einstellungen später anpassen. Bitte klicke im nächsten Screen auf 'Zulassen'!")
            builder.setPositiveButton("Weiter") { _, _ ->
                // Starte die Berechtigungsanfrage, wenn der Nutzer zustimmt
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION_CODE
                )
            }
            builder.create().show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission granted")
        }
    }


    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        val blzInput = dialogView.findViewById<EditText>(R.id.bankleitzahl_input)
        val anmeldenameInput = dialogView.findViewById<EditText>(R.id.username_input)
        val pinInput = dialogView.findViewById<EditText>(R.id.password_input)
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val currentBlz = sharedPreferences.getString("BLZ", "").toString()
        val currentAnmeldename = sharedPreferences.getString("Username", "").toString()
        val currentPin = sharedPreferences.getString("PIN", "").toString()
        val kundenSystemId = sharedPreferences.getString("kundenSystemId", "").toString()
        blzInput.setText(currentBlz)
        anmeldenameInput.setText(currentAnmeldename)
        pinInput.setText(currentPin)

        AlertDialog.Builder(this)
            .setTitle("App-Login")
            .setView(dialogView)
            .setPositiveButton("Login") { dialog, _ ->
                val inputBlz = blzInput.text.toString().trim()
                val inputAnmeldename = anmeldenameInput.text.toString().trim()
                val inputPin = pinInput.text.toString().trim()

                val snackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    "Einloggen...",
                    Snackbar.LENGTH_INDEFINITE
                ).apply {
                    setAction("Abbrechen") {
                        // Handle cancellation, if necessary
                    }
                    show()
                }

                lifecycleScope.launch {
                    try {
                        mkaSession = MkaSession(
                            this@MainActivity,
                            "A1006C0B57AF9D44240CE6415",
                            "06080",
                            inputBlz
                        )
                        val response = withContext(Dispatchers.IO) {
                            mkaSession.login(inputAnmeldename, inputPin, kundenSystemId, "923", "Alle Geräte")
                        }

                        snackbar.dismiss()

                        if (response != null ) {
                            for(mkaResponseEntry in response) {
                                if (mkaResponseEntry.code == 3991) {
                                    val assignedKundenSystemId: String? =
                                        mkaResponseEntry.metadata.hbciResponseList?.firstOrNull()

                                    if (assignedKundenSystemId != null) {
                                        registerDevice(assignedKundenSystemId)
                                    }
                                }
                            }
                            proceedToMainApp()
                        } else {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Login fehlgeschlagen",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        snackbar.dismiss()
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Fehler: ${e.localizedMessage}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Abbrechen") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun registerDevice(kundenSystemId: String) {
        // Dialog zur Bestätigung der Registrierung
        AlertDialog.Builder(this)
            .setTitle("Geräteregistrierung erforderlich")
            .setMessage("Um fortzufahren, müssen Sie Ihr Gerät registrieren. Möchten Sie jetzt fortfahren?")
            .setPositiveButton("OK") { _, _ ->
                // Starte die Registrierung
                lifecycleScope.launch(Dispatchers.IO) {
                    val response = mkaSession.registerDevice(kundenSystemId, "923", "Alle Geräte")
                    withContext(Dispatchers.Main) {
                        val auftragsReferenz = response?.firstOrNull()?.metadata?.signatureChallenge?.orderReference
                        // Zeige den Bestätigungsdialog für die S-pushTAN-App
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("S-pushTAN-App")
                            .setMessage("Bitte bestätigen Sie die Registrierung in Ihrer S-pushTAN-App und klicken Sie dann hier.")
                            .setPositiveButton("OK") { _, _ ->
                                // Versuche, die Registrierung abzuschließen
                                try {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val finishRegistrationResponse =
                                            mkaSession.finishRegistration(auftragsReferenz!!, "923")
                                        withContext(Dispatchers.Main) {
                                            if (finishRegistrationResponse?.firstOrNull()?.code == 20) {
                                                val sharedPreferences =
                                                    this@MainActivity.getSharedPreferences(
                                                        "MyPreferences",
                                                        Context.MODE_PRIVATE
                                                    )
                                                val editor = sharedPreferences.edit()
                                                editor.putString("kundenSystemId", kundenSystemId)
                                                editor.apply()  // apply() speichert asynchron
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Gerät erfolgreich registriert!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Handle errors
                                }
                            }
                            .show()
                    }
                }
            }
            .setNegativeButton("Abbrechen") { dialog, _ ->
                dialog.dismiss()
                // Hier kannst du eine Aktion ausführen, wenn der Benutzer abbricht
            }
            .show()
    }


    private fun proceedToMainApp() {
        // Logik zum Starten der App nach erfolgreichem Login
        Log.d("MainActivity", "Login erfolgreich!")
        viewModel.sendDeviceData()
        viewModel.loadMobiData()

        // Registriere den BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            messageReceiver, IntentFilter("push-notification-received")
        )
        viewModel.mobiDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) {
                // Jetzt kannst du auf die Mobi-Daten zugreifen und sie verwenden
                // val mailboxUrl = viewModel.getMailboxUrl()
                initializeSilentLoginAndAdvisorDataService()
                intent?.let {
                    handleIntent(it)
                }
            } else {
                // Fehlerbehandlung oder Fallback
            }
        }

        // Beobachte die LiveData für die Imagedaten
        viewModel.VkaDataLoaded.observe(this) { loaded ->
            if (loaded == true) {
                val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                val jsonIamPayloadString = sharedPreferences.getString("iamPayload", "").toString()
                val iamPayload = Json.decodeFromString<IamPayload>(jsonIamPayloadString)
                val jsonVkaString = sharedPreferences.getString("vkaData", "").toString()
                val sfcIfResponse = Json.decodeFromString<SfcIfResponse>(jsonVkaString)
                if (iamPayload.overlayImage == "3" || iamPayload.overlayImage == "4") {
                    val intent = Intent(this, DisrupterActivity::class.java)
                    startActivity(intent)
                } else {
                    val url = sfcIfResponse.services.firstOrNull()?.IF?.disrupter?.firstLink?.url
                    if ( url != null )
                        openWebView(url, false)
                }
            } else {
                    // val url = pushNotificationPayload.iam?.
                    Toast.makeText(this@MainActivity, "wir gehen direkt zu link1, weil kein Störer gezeigt werden soll", Toast.LENGTH_LONG).show()
            }
/*
                when (pushNotificationPayload.iam?.overlayImage) {
                    "disrupterImage" -> {
                        val intent = Intent(this, DisrupterActivity::class.java)
                        intent.putExtra("image", pushNotificationPayload.iam?.overlayImage)
                        startActivity(intent)
                    }
                    "logoutPageImage" -> {
                        val intent = Intent(this, DisrupterActivity::class.java)
                        intent.putExtra("image", pushNotificationPayload.iam?.overlayImage)
                        startActivity(intent)
                    }
                    else -> {
                        // val url = pushNotificationPayload.iam?.
                        Toast.makeText(this@MainActivity, "wir gehen direkt zu link1, weil kein Störer gezeigt werden soll", Toast.LENGTH_LONG).show()
                    }
                }
            }
 */
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
                showLog()
                true
            }

            R.id.nav_function_einstellungen -> {
                showFeatureSettings()
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_content)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun createNotificationChannel() {
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

    private fun createSystemNotificationChannel() {
        val channelId = getString(R.string.system_notification_channel_id)
        val channelName = getString(R.string.system_notification_channel_name)
        val channelDescriptionText =
            getString(R.string.system_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_MIN
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescriptionText
            setShowBadge(true) // Badge-Anzeige aktivieren
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createAccountAlertNotificationChannel() {
        val channelId = getString(R.string.account_alert_notification_channel_id)
        val channelName = getString(R.string.account_alert_notification_channel_name)
        val channelDescriptionText =
            getString(R.string.account_alert_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    private fun createIamNotificationChannel() {
        val channelId = getString(R.string.iam_notification_channel_id)
        val channelName = getString(R.string.iam_notification_channel_name)
        val channelDescriptionText =
            getString(R.string.iam_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data
        val extras = intent.extras

        if (data != null) {
            Log.d(TAG, "Opened via Deep Link: $data")
            handleDeeplink(intent)
        } else if (extras != null) {
            if (extras.containsKey("pushNotificationPayload")) {
                Log.d(TAG, "Opened via Push Notification with pushNotificationPayload")
                handlePushNotification(intent)
            }
        } else {
            Log.d(TAG, "Opened normally")
        }
    }

    private fun handleDeeplink(intent: Intent?) {
        // val deeplinkRootUri = getString(R.string.deeplink_root_uri)
        val data: Uri? = intent?.data
        if (data != null) {
            when {
                data.path!!.startsWith("/_deeplink/webview") -> {
                    handleGenericWebviewDeeplink(intent.data!!)
                }

                data.path!!.startsWith("/_deeplink/iam") -> {
                    handleIamWebviewDeeplink(intent.data!!)
                }

                data.path!!.startsWith("/_deeplink/showAlert") -> {
                    showDeeplinkAlertDialog()
                }

                data.path!!.startsWith("/_deeplink/settings") -> {
                    showFeatureSettings()
                }

                data.path!!.startsWith("/_deeplink/log") -> {
                    showLog()
                }

                data.path!!.startsWith("/_deeplink") -> {
                    // just start the app
                    Logger.log("plain deeplink ${data.path}")
                }
            }
        }
    }

    private fun handlePushNotification(intent: Intent?) {
        Log.d(TAG, "handlePushNotification")
        // Daten aus dem Intent holen
        val pushNotificationPayload: PushNotificationPayload? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelable("pushNotificationPayload", PushNotificationPayload::class.java)
        } else {
            return
        }
//        val title = intent?.getStringExtra("title")
//        val body = intent?.getStringExtra("body")
        if (pushNotificationPayload?.iam != null) {
            viewModel.loadVkaData(pushNotificationPayload)
        }

        if (pushNotificationPayload?.mailbox != null) {
            val userName = pushNotificationPayload.obv
            val blz = pushNotificationPayload.blz
            val myMainObv = accountSettingsViewModel.getMainObv()
            if ((blz == null || myMainObv.blz == blz)&&(userName == null || myMainObv.username == userName))
                handleMailboxBadge(pushNotificationPayload)
        }

        if (pushNotificationPayload?.balance != null) {
            handleBalanceNotification(pushNotificationPayload)
        }

        if (pushNotificationPayload?.webview != null) {
            handleWebviewNotification(pushNotificationPayload)
        }

        if (pushNotificationPayload?.update != null) {
            handleUpdateNotification(pushNotificationPayload)
        }

        if (pushNotificationPayload?.ping != null) {
            Log.d(TAG, "push ping")
        }
/*
        "TRANSACTION"
        "WEBVIEWWITHSILENTLOGIN"
        "REVIEW"
        "KILLSWITCH"
        "UPDATE"
        "SECURITY"
        "FEATURE"
        "RETROSPECT"
        "INSTANTPAYMENT"
        "GEO"
 */
    }

    fun showFeatureSettings() {
        startActivity(
            Intent(
                this,
                SettingsActivity::class.java
            )
        ) // Aktion für Einstellungen
    }


    fun showAccountSettings() {
        startActivity(
            Intent(
                this,
                AccountSettingsActivity::class.java
            )
        ) // Aktion für Einstellungen
    }

    fun showSystemSettings() {
        startActivity(
            Intent(
                this,
                SystemParametersActivity::class.java
            )
        ) // Aktion für Einstellungen
    }

    private fun showLog() {
        startActivity(
            Intent(
                this,
                LogActivity::class.java
            )
        ) // Aktion für Log
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

    private fun initializeSilentLoginAndAdvisorDataService() {
        val servletUrl = viewModel.getServletUrl()
        val blz = accountSettingsViewModel.getBLZ()
        val loginName = accountSettingsViewModel.getUsername()
        val onlineBankingPin = accountSettingsViewModel.getPIN()

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
            Log.d(
                TAG,
                "Error: Missing parameters for SilentLoginAndAdvisorDataService initialization."
            )
        }
    }

    private fun handleGenericWebviewDeeplink(deeplinkUri: Uri) {
        var isSilentLogin = false
        var blz: String? = null

        Log.d(TAG, "handleGenericWebviewDeeplink, Uri: $deeplinkUri")

        val decodedPath =Uri.decode(deeplinkUri.getQueryParameter("path")).trim('/')

        val uriBuilder = Uri.parse("https://" + viewModel.getHostname()).buildUpon()

        if (decodedPath != null) {
            // Versuche decodedPath als Uri zu parsen
            val parsedUri = Uri.parse(decodedPath)

            // Prüfe, ob die geparste Uri ein Schema und einen Host enthält
            if (parsedUri.scheme != null && parsedUri.host != null) {
                // Verwende Schema, Host und Path von decodedPath
                uriBuilder.scheme(parsedUri.scheme)
                    .authority(parsedUri.authority)
                    .path(parsedUri.path)
            } else {
                // decodedPath hat kein gültiges Schema oder keinen Host, füge es als Pfad hinzu
                uriBuilder.appendEncodedPath(decodedPath)
            }
            val queryParameterNames = deeplinkUri.queryParameterNames
            for (paramName in queryParameterNames) {
                val paramValue = deeplinkUri.getQueryParameter(paramName)
                println("Parameter: $paramName, Wert: $paramValue")
                when (paramName) {
                    "path" -> {}
                    "fallback" -> {}
                    "blz" -> blz = paramValue
                    "IF_SILENT_LOGIN" -> isSilentLogin = (paramValue == "true")
                    else -> uriBuilder.appendQueryParameter(paramName, paramValue)
                }
            }
        }

        val targetUriString = uriBuilder.build().toString()
        val myObvs = accountSettingsViewModel.getObvs()
        val filteredEntries = when (blz) {
            null, "" -> listOf(accountSettingsViewModel.getMainObv())
            "choice" -> myObvs
            else -> myObvs.filter { it -> it.blz in blz.split(",").map { it.trim() } }
        }
        when (filteredEntries.size) {
            0 -> Toast.makeText(this@MainActivity, "Dieses Angebot ist in Ihrer Sparkasse nicht verfügbar", Toast.LENGTH_LONG).show()
            1 -> {
                Log.d(TAG, "starting webview with uri $targetUriString")
                openWebView(targetUriString, isSilentLogin)
                }
            else -> {
                val bottomSheet = ChooseInstitionBottomSheet(filteredEntries.toTypedArray(), targetUriString, isSilentLogin)
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            }
        }
    }

    private fun handleIamWebviewDeeplink(deeplinkUri: Uri) {
        val contentId = deeplinkUri.getQueryParameter("contentId")
        // val eventId = deeplinkUri.getQueryParameter("eventId")
        if (contentId != null) {
            // bau eine Struktur, als wäre eine push notification gekommen
            val iamPayload = IamPayload(contentId = contentId)
            val pushNotificationPayload = PushNotificationPayload(iam = iamPayload)
            viewModel.loadVkaData(pushNotificationPayload)
        }
    }

    private fun handleMailboxBadge(pushNotificationPayload: PushNotificationPayload ) {
        val badgeCount: Int = pushNotificationPayload.mailbox?.count ?: 0
        Log.d(TAG, "handleMailboxBadge, badgeCount: $badgeCount")
    }

    private fun handleBalanceNotification(pushNotificationPayload: PushNotificationPayload ) {
        val balance = pushNotificationPayload.balance?.balance
        Toast.makeText(this@MainActivity, "Kontostand aktualisiert! ($balance)", Toast.LENGTH_LONG).show()
    }

    private fun handleWebviewNotification(pushNotificationPayload: PushNotificationPayload){
        val url = pushNotificationPayload.webview?.path
        if (url != null && url != "") {
            openWebView(url, false /* dummy parameter */)
        }
    }

    private fun handleUpdateNotification(pushNotificationPayload: PushNotificationPayload){
        val version = pushNotificationPayload.update?.fromVersion
        if (version != null && version != "") {
            Toast.makeText(this@MainActivity, "mach ein Update!", Toast.LENGTH_LONG).show()
        }
    }

    // Implementierung der Schnittstelle
    override fun onChoiceSelected(choice: BankEntry?, targetUri: String, isSilentLogin: Boolean) {
        if (choice?.blz != null ) {
            Log.d(TAG, "starting webview with uri $targetUri")
            openWebView(targetUri, isSilentLogin)
        } else {
            Toast.makeText(this@MainActivity, "abgebrochen", Toast.LENGTH_LONG).show()
        }
    }

    // hier werden die push notifications verarbeitet, falls die App im Vordergrund ist
    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handlePushNotification(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter("PUSH-NOTIFICATION-RECEIVED")

        // Registrierung mit LocalBroadcastManager
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()

        // Deregistrierung mit LocalBroadcastManager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Den BroadcastReceiver abmelden, um Speicherlecks zu vermeiden
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }
}
