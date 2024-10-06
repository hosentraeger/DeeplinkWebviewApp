package com.example.deeplinkwebviewapp.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.service.WebViewService
import kotlinx.coroutines.launch

class WebViewActivity : AppCompatActivity() {

    private lateinit var webViewService: WebViewService // WebViewService deklarieren

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        WebView.setWebContentsDebuggingEnabled(true) // Debugging aktivieren

        // WebView und Service initialisieren
        val webView: WebView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE // Cache nicht verwenden
        webView.settings.userAgentString = getString(R.string.user_agent_string_webview)

        webViewClientSetup(webView) // WebViewClient einrichten

        val url = intent.getStringExtra("EXTRA_URL")
        val isSilentLogin = intent.getBooleanExtra("IF_SILENT_LOGIN", false) // IF_SILENT_LOGIN aus Intent auslesen

        if (url != null) {
            loadUrlWithSession(webView, url, isSilentLogin) // URL mit Session-ID laden
        }
    }

    private fun webViewClientSetup(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(applicationContext, "Error: $description", Toast.LENGTH_SHORT).show()
            }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()

                // Überprüfen, ob die URL zu deiner eigenen App gehört
                if (url.startsWith("https://www.fsiebecke.de/_deeplink")) {
                    // Intent für deine eigene App
                    val intent = Intent(this@WebViewActivity, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse(url) // Setze die URL als Data des Intents
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP // Klarstellen, dass MainActivity an den Vordergrund kommt
                    }
                    startActivity(intent)
                    return true
                }

                // Für externe Links oder andere Apps
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    view.loadUrl(url)
                }

                return true
            }
/*
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                try {
                    val intent = Intent(this@WebViewActivity, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse(url) // Setze die URL als Data des Intents
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP // Klarstellen, dass MainActivity an den Vordergrund kommt
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    view.loadUrl(url)
                }
                return true
            }

 */
        }
    }

    private fun loadUrlWithSession(webView: WebView, url: String, isSilentLogin: Boolean) {
        webViewService = WebViewService() // Instanz des WebViewService erstellen
        lifecycleScope.launch {
            if (isSilentLogin) {
                webViewService.loadUrlWithSession(webView, url) // URL mit Session-ID laden
            } else {
                webView.loadUrl(url) // URL ohne Session-ID laden
            }
        }
    }
}
