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

        binding.startFocusBtn.setOnClickListener { onStartOrEndClicked() }
        updateStatusText()
    }

    override fun onResume() {
        super.onResume()
        updateStatusText()
    }

    private fun tintButton(btn: android.widget.Button, selected: Boolean) {
        btn.isSelected = selected
        btn.backgroundTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (selected) "#6c5ce7" else "#1a1a24")
        )
    }

    private fun setupDurationButtons() {
        val presetButtons = mapOf(
            binding.duration25 to 25,
            binding.duration50 to 50,
            binding.duration90 to 90
        )
        fun selectPreset(minutes: Int) {
            selectedMinutes = minutes
            presetButtons.keys.forEach { tintButton(it, false) }
            presetButtons.forEach { (btn, m) -> if (m == minutes) tintButton(btn, true) }
            tintButton(binding.durationCustom, false)
            binding.durationCustom.text = "Custom"
        }
        presetButtons.forEach { (btn, minutes) -> btn.setOnClickListener { selectPreset(minutes) } }
        binding.durationCustom.setOnClickListener { showCustomDurationDialog() }
        selectPreset(25)
    }

    private fun showCustomDurationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_duration, null)
        val hoursPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.hoursPicker)
        val minutesPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.minutesPicker)

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 4
        hoursPicker.value = selectedMinutes / 60

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.value = selectedMinutes % 60
        minutesPicker.setFormatter { String.format("%02d", it) }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Custom duration")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val minutes = hoursPicker.value * 60 + minutesPicker.value
                if (minutes <= 0) {
                    Toast.makeText(this, "Pick a duration longer than 0 minutes", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                selectedMinutes = minutes
                listOf(binding.duration25, binding.duration50, binding.duration90).forEach { tintButton(it, false) }
                tintButton(binding.durationCustom, true)
                val h = minutes / 60
                val m = minutes % 60
                binding.durationCustom.text = if (h > 0) "${h}h ${m}m" else "${m}m"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun onStartOrEndClicked() {
        if (FocusSessionManager.isActive(this)) {
            endActiveSession()
            return
        }
        onStartClicked()
    }

    private fun endActiveSession() {
        val endIntent = Intent(this, FocusForegroundService::class.java).apply {
            action = FocusForegroundService.ACTION_END
        }
        startService(endIntent)
        Toast.makeText(this, "Focus session ended", Toast.LENGTH_SHORT).show()
        updateStatusText()
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
            binding.startFocusBtn.text = "End Focus Session"
            binding.startFocusBtn.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#e74c3c"))
        } else {
            binding.statusText.text = "No active session"
            binding.startFocusBtn.text = "Start Focus Session"
            binding.startFocusBtn.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6c5ce7"))
        }
    }

    private fun loadLaunchableApps(): List<InstalledApp> {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return installedApps
            .filter { it.packageName != packageName }
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map {
                InstalledApp(
                    packageName = it.packageName,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
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
            "One-time setup: tap ⋮ (top-right) → \"Allow restricted settings\", then come back and hit Start again",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
