package com.example.deeplinkwebviewapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.deeplinkwebviewapp.R

class ProductsFragment : Fragment() {
    private lateinit var webView: WebView
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: ProductsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        webView = view.findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.webViewClient = WebViewClient()

        if (viewModel.webViewStateBundle != null) {
            // Zustand der WebView wiederherstellen
            webView.restoreState(viewModel.webViewStateBundle!!)
        } else {
            // Lade URL nur beim ersten Start oder wenn kein gespeicherter Zustand vorhanden ist
            val url = sharedPreferences.getString("individualOffersUrl", null)
            url?.let { webView.loadUrl(it) }
        }
    }

    override fun onPause() {
        super.onPause()
        // Speichert den Zustand der WebView im ViewModel
        if (viewModel.webViewStateBundle == null) {
            viewModel.webViewStateBundle = Bundle()
        }
        webView.saveState(viewModel.webViewStateBundle!!)
    }
}
