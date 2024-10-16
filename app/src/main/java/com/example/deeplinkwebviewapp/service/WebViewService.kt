package com.example.deeplinkwebviewapp.service

import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.webkit.CookieManager

class WebViewService {
    companion object {
        private const val TAG = "WebViewService"
    }

    suspend fun loadUrlWithSession(webView: WebView, url: String) = withContext(Dispatchers.Main) {
        val service = SilentLoginAndAdvisorDataServiceFactory.getService()
        // Versuche, die Session-ID abzurufen
        service.getSessionCookies(
            onSessionReceived = { cookies ->
                if (cookies != null) {
                    // Cookie-String erstellen
                    val cookieString = cookies.joinToString("; ") { it }
                    // Cookie in WebView setzen
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    CookieManager.getInstance().removeAllCookies(null)
                    cookieManager.setCookie(url, cookieString)
                    cookieManager.flush()
                    // URL mit Cookies laden
                    webView.loadUrl(url)
                } else {
                    // Fehlerbehandlung
                    Log.e(TAG, "sessionId is null")
                }
            },
            onError = { error ->
                // Fehlerbehandlung für den Fall, dass beim Abrufen der Session-ID ein Fehler auftritt
                Log.e ( TAG, "Error: Could not retrieve session ID: ${error.message}")
            }
        )
    }

    fun loadUrl(webView: WebView, url: String) {
        webView.loadUrl(url)
    }

}
