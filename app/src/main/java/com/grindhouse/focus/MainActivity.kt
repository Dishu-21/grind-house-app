package com.grindhouse.focus

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.grindhouse.focus.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // TODO: point this at wherever Grind House is actually hosted.
    private val siteUrl = "https://dishu-21.github.io/grind-house/"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.webViewClient = WebViewClient()
        binding.webview.loadUrl(siteUrl)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_focus_mode) {
            startActivity(Intent(this, FocusModeActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) binding.webview.goBack() else super.onBackPressed()
    }
}
