package com.grindhouse.focus

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.grindhouse.focus.databinding.ActivityFocusModeBinding

class FocusModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusModeBinding
    private lateinit var adapter: AppListAdapter
    private var selectedMinutes = 25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Focus Mode"

        val apps = loadLaunchableApps()
        adapter = AppListAdapter(apps.toMutableList())
        binding.appList.layoutManager = LinearLayoutManager(this)
        binding.appList.adapter = adapter

        setupDurationButtons()

        binding.startFocusBtn.setOnClickListener { onStartClicked() }
        updateStatusText()
    }

    override fun onResume() {
        super.onResume()
        updateStatusText()
    }

    private fun setupDurationButtons() {
        val buttons = mapOf(
            binding.duration25 to 25,
            binding.duration50 to 50,
            binding.duration90 to 90
        )
        fun select(minutes: Int) {
            selectedMinutes = minutes
            buttons.forEach { (btn, m) -> btn.isSelected = (m == minutes) }
        }
        buttons.forEach { (btn, minutes) -> btn.setOnClickListener { select(minutes) } }
        select(25)
    }

    private fun onStartClicked() {
        val selected = adapter.selectedPackages()
        if (selected.isEmpty()) {
            Toast.makeText(this, "Pick at least one app to block", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isAccessibilityServiceEnabled()) {
            promptEnableAccessibilityService()
            return
        }

        FocusSessionManager.startSession(this, selectedMinutes, selected)
        ContextCompat.startForegroundService(this, Intent(this, FocusForegroundService::class.java))
        Toast.makeText(this, "Focus session started - $selectedMinutes min", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateStatusText() {
        if (FocusSessionManager.isActive(this)) {
            val minutesLeft = (FocusSessionManager.remainingMillis(this) / 60_000L).toInt()
            binding.statusText.text = "Session active - $minutesLeft min left"
            binding.startFocusBtn.text = "Session already running"
        } else {
            binding.statusText.text = "No active session"
            binding.startFocusBtn.text = "Start Focus Session"
        }
    }

    /** Apps with a LAUNCHER activity are visible without any special permission (Android 11+ package visibility exemption). */
    private fun loadLaunchableApps(): List<InstalledApp> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolved
            .filter { it.activityInfo.packageName != packageName } // never let it block itself
            .distinctBy { it.activityInfo.packageName }
            .map {
                InstalledApp(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = "$packageName/${BlockAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    private fun promptEnableAccessibilityService() {
        Toast.makeText(
            this,
            "Turn on Grind House Focus under Accessibility settings, then come back and hit Start again",
            Toast.LENGTH_LONG
        ).show()
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}
