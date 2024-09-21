package com.example.deeplinkwebviewapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.content.Intent.ACTION_VIEW
import android.content.Intent.CATEGORY_BROWSABLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView: WebView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE // Cache nicht verwenden
        WebView.setWebContentsDebuggingEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(applicationContext, "Error: $description", Toast.LENGTH_SHORT).show()
            }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest):Boolean {
                val url = request.url.toString()
                try {
                    val intent = Intent(ACTION_VIEW, Uri.parse(url)).apply {
                        // The URL should either launch directly in a non-browser app (if it's
                        // the default), or in the disambiguation dialog.
                        addCategory(CATEGORY_BROWSABLE)
                        flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Only browser apps are available, or a browser is the default.
                    // So you can open the URL directly in your app, for example in a
                    view.loadUrl(url)
                }

                return true
            }
        }
//        webView.loadUrl("https://www.example.com") // Ersetze dies mit der gewünschten URL
        webView.loadUrl("https://www.fsiebecke.de/deeplinks.html")
    }
}
