package com.example.deeplinkwebviewapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle // Fügt ActionBarDrawerToggle hinzu
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // SharedPreferences initialisieren
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

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
                    val url = sharedPreferences.getString("DeeplinkURL", getString(R.string.default_deeplinks_url))
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
}
