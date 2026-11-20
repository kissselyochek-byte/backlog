package com.kisel.backlog

import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

// Thin shell around the web app: the queue and the interface live on GitHub Pages,
// so changes pushed to the repository show up here without a new APK.
class MainActivity : Activity() {
    private lateinit var web: WebView
    private val appUrl = "https://kissselyochek-byte.github.io/backlog/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.cacheMode = if (online()) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_CACHE_ELSE_NETWORK
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                if (url.host == Uri.parse(appUrl).host) return false
                startActivity(Intent(Intent.ACTION_VIEW, url))
                return true
            }
        }
        if (savedInstanceState != null) web.restoreState(savedInstanceState) else web.loadUrl(appUrl)
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
        web.settings.cacheMode = if (online()) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_CACHE_ELSE_NETWORK
        web.evaluateJavascript("window.onAppResume && window.onAppResume()", null)
    }

    override fun onPause() { web.onPause(); super.onPause() }

    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); web.saveState(outState) }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }

    private fun online(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
