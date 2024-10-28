package com.example.deeplinkwebviewapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.deeplinkwebviewapp.R

class ProductsFragment : Fragment() {
    private lateinit var webView: WebView
    private var url: String? = null
    private var isWebViewInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString("url_key")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.webViewClient = WebViewClient()

        // URL setzen, falls sie bereits gesetzt wurde
        if (!isWebViewInitialized) {
            url?.let {
                webView.loadUrl(it)
                isWebViewInitialized = true
            }
        }
    }

    fun setUrl(newUrl: String) {
        url = newUrl
        if (this::webView.isInitialized) {
            webView.loadUrl(url!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Optional: WebView nicht zerstören, um den Zustand zu behalten
        // webView.destroy()
    }
}
