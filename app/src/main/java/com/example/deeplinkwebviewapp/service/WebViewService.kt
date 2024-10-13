package com.example.deeplinkwebviewapp.service

import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebViewService {
    companion object {
        private const val TAG = "WebViewService"
    }

    suspend fun loadUrlWithSession(webView: WebView, url: String) = withContext(Dispatchers.Main) {
        val service = SilentLoginAndAdvisorDataServiceFactory.getService()
        // Versuche, die Session-ID abzurufen
        service.getSessionId(
            onSessionReceived = { sessionId ->
                if (sessionId != null) {
                    Log.d(TAG, "got sessionId $sessionId")
                    // Wenn die Session-ID erfolgreich abgerufen wurde, lade die URL mit den Headern
                    val headers = mapOf("'JSESSIONID" to sessionId)
                    webView.loadUrl(url, headers)
                } else {
                    // Fehlerbehandlung, wenn die Session-ID leer ist
                    Log.d(TAG, "sessionId is null")
                }
            },
            onError = { error ->
                // Fehlerbehandlung für den Fall, dass beim Abrufen der Session-ID ein Fehler auftritt
                Log.d ( TAG, "Error: Could not retrieve session ID: ${error.message}")
            }
        )
    }

    fun loadUrl(webView: WebView, url: String) {
        webView.loadUrl(url)
    }

}
