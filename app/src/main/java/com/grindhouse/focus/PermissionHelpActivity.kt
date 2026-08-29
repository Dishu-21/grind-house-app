package com.grindhouse.focus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.grindhouse.focus.databinding.ActivityPermissionHelpBinding

class PermissionHelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Permission Setup"

        // Deliberately opens the full "All apps" list rather than jumping
        // straight to this app's own App Info page. On some OEM skins
        // (confirmed on Moto) the direct-to-app-info intent opens a
        // stripped-down page missing the ⋮ menu needed to unlock restricted
        // settings — reaching the same page via the All Apps list doesn't
        // have that problem.
        binding.openSettingsBtn.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
            } catch (e: Exception) {
                // Very old/unusual phones that don't support that action at
                // all - fall back to this app's own settings page directly.
                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(fallback)
            }
        }

        // For phones where Restricted Settings doesn't apply at all (older
        // Android, or the toggle's already unlocked) - skips the whole
        // multi-step dance and jumps straight to Accessibility settings.
        binding.directAccessibilityBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.notNowBtn.setOnClickListener { finish() }
    }
}
