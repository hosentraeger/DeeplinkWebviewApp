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
import com.google.gson.Gson


class MainActivity : AppCompatActivity(), ChooseInstitionBottomSheet.OnChoiceSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var viewModel: MainViewModel
    private val gson = Gson()
    private val settingsViewModel: SettingsViewModel by viewModels {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        SettingsViewModelFactory(application, sharedPreferences)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    @androidx.annotation.OptIn(ExperimentalBadgeUtils::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        createSystemNotificationChannel()

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

        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val factory = MainViewModelFactory(application, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
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
                showSettings()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission granted")
        }
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

    private fun handleIntent(intent: Intent) {
        val data = intent.data
        val extras = intent.extras

        if (data != null) {
            Log.d(TAG, "Opened via Deep Link: $data")
            handleDeeplink(intent)
        } else if (extras != null) {
            // Ausgabe aller erhaltenen Extras
            for (key in extras.keySet()) {
                Log.d(TAG, "Intent extra: $key = ${extras.getString(key)}")
            }
            // Hier kannst du spezifische Schlüssel prüfen
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
                    showSettings()
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
        val pushNotificationPayload: PushNotificationPayload? = intent?.extras?.getParcelable("pushNotificationPayload")
        val title = intent?.getStringExtra("title")
        val body = intent?.getStringExtra("body")

        if (pushNotificationPayload?.iam != null) {
            viewModel.loadVkaData(pushNotificationPayload)
        }
        if (pushNotificationPayload?.mailbox != null) {
            val userName = pushNotificationPayload.obv
            val blz = pushNotificationPayload.blz
            val myMainObv = settingsViewModel.getMainObv()
            if ((blz == null || myMainObv.blz == blz)&&(userName == null || myMainObv.username == userName))
                handleMailboxBadge(pushNotificationPayload.mailbox.count)
            }
/*
            "BALANCE" -> {
                val iban = customKey2?.substringBefore(":")
                val balance = customKey2?.substringAfter(":")
                if (MyApplication.isAppInForeground) {
                    Toast.makeText(
                        this,
                        "Der neue Kontostand für ${iban} ist ${balance}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showNotification(title, body, "Neuer Kontostand", "Der neue Kontostand für ${iban} ist ${balance}", null)
                }
            }

            "TRANSACTION" -> {
            }

            "WEBVIEWWITHSILENTLOGIN" -> {
                // val url = customKey2
            }

            "REVIEW" -> showNotification(title, body, customKey1, customKey2, null)
            "KILLSWITCH" -> {}
            "UPDATE" -> {}
            "SECURITY" -> {}
            "FEATURE" -> {}
            "RETROSPECT" -> {}
            "INSTANTPAYMENT" -> {}
            "GEO" -> {}
            else -> {
                Toast.makeText(
                    this@MainActivity,
                    "Message received: $customKey1 - $customKey2",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
 */
    }

    private fun showSettings() {
        startActivity(
            Intent(
                this,
                SettingsActivity::class.java
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

    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        // val passwordInput = dialogView.findViewById<EditText>(R.id.password_input)

        AlertDialog.Builder(this)
            .setTitle("App-Login")
            .setView(dialogView)
            .setPositiveButton("Login") { dialog, _ ->
                // val inputPassword = passwordInput.text.toString()
                // val correctPassword = getString(R.string.app_password)

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
        viewModel.pushNotificationPayload.observe(this) { pushNotificationPayload ->
            if (pushNotificationPayload != null) {
                if (pushNotificationPayload.iam?.showDisrupter == true) {
                    val intent = Intent(this, DisrupterActivity::class.java)
                    startActivity(intent)
                } else {

                    val targetUri = when ( pushNotificationPayload.iam?.uri ) {
                        null -> ""
                        "@Link1" -> "Link1"
                        "@Link2" -> "Link2"
                        "@Link3" -> "Link3"
                        else -> pushNotificationPayload.iam?.uri
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "weiter zu ${targetUri}", Toast.LENGTH_LONG).show()
                }
            }
        }
/*
        intent?.let {
            handleIntent(it)
        }
*/
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
            Log.d(
                TAG,
                "Error: Missing parameters for SilentLoginAndAdvisorDataService initialization."
            )
        }
    }

    fun handleGenericWebviewDeeplink(deeplinkUri: Uri) {
        var isSilentLogin = false
        var blz: String? = null
        Log.d(TAG, "handleGenericWebviewDeeplink, Uri: ${deeplinkUri}")

        val uriBuilder = Uri.parse("https://" + viewModel.getHostname()).buildUpon()
        val queryParameterNames = deeplinkUri.queryParameterNames
        for (paramName in queryParameterNames) {
            val paramValue = deeplinkUri.getQueryParameter(paramName)
            println("Parameter: $paramName, Wert: $paramValue")
            when (paramName) {
                "path" -> {
                    val decodedPath = Uri.decode(paramValue) // Dekodieren des Wertes
                    uriBuilder.appendEncodedPath(decodedPath.trim('/'))
                }

                "fallback" -> {}
                "blz" -> blz = paramValue
                "IF_SILENT_LOGIN" -> isSilentLogin = (paramValue == "true")
                else -> uriBuilder.appendQueryParameter(paramName, paramValue)
            }
        }
        val targetUri = uriBuilder.build().toString()
        val myObvs = settingsViewModel.getObvs()
        val filteredEntries = when {
            blz == null ||
            blz == "" -> listOf(settingsViewModel.getMainObv())
            blz == "choice" -> myObvs
            else -> myObvs.filter { it.blz in blz.split(",").map { it.trim() } }
        }
        when (filteredEntries.size) {
            0 -> Toast.makeText(this@MainActivity, "Dieses Angebot ist in Ihrer Sparkasse nicht verfügbar", Toast.LENGTH_LONG).show()
            1 -> {
                Log.d(TAG, "starting webview with uri ${targetUri}")
                openWebView(targetUri, isSilentLogin)
                }
            else -> {
                val bottomSheet = ChooseInstitionBottomSheet(filteredEntries.toTypedArray(), targetUri, isSilentLogin)
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            }
        }
    }

    fun handleIamWebviewDeeplink(deeplinkUri: Uri) {
        val contentId = deeplinkUri.getQueryParameter("contentId")
        // val eventId = deeplinkUri.getQueryParameter("eventId")
        if (contentId != null) {
            val iamPayload: IamPayload = IamPayload(contentId = contentId)
            var pushNotificationPayload: PushNotificationPayload = PushNotificationPayload(iam = iamPayload)
            viewModel.loadVkaData(pushNotificationPayload)
        }
    }

    fun handleMailboxBadge(badgeCount: Int ) {
        Log.d(TAG, "handleMailboxBadge, badgeCount: ${badgeCount}")
    }

    // Implementierung der Schnittstelle
    override fun onChoiceSelected(choice: BankEntry?, targetUri: String, isSilentLogin: Boolean) {
        if (choice?.blz != null ) {
            Log.d(TAG, "starting webview with uri ${targetUri}")
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
/*
    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchImageAndShowNotification(
        title: String?,
        message: String?,
        customKey1: String?,
        customKey2: String?
    ) {
        // Hier kannst du eine Coroutine oder eine andere Methode verwenden,
        // um das Bild asynchron abzurufen.
        GlobalScope.launch(Dispatchers.IO) {
            val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_banner)
            showNotification(title, message, customKey1, customKey2, imageBitmap)
        }
    }

    private fun showNotification(
        title: String?,
        message: String?,
        pushNotificationPayload: PushNotificationPayload,
        imageBitmap: Bitmap?
    ) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = getString(R.string.default_notification_channel_id)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("customKey1", customKey1)
            putExtra("customKey2", customKey2)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Setze hier dein Icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Wenn das Bild vorhanden ist, setze es in die Benachrichtigung
        if (imageBitmap != null) {
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitmap)
                    .bigLargeIcon(null as Bitmap?)
            ) // Optional, um das große Icon zu entfernen
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
 */
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