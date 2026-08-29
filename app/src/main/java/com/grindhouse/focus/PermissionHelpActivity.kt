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

        binding.openSettingsBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }

        binding.notNowBtn.setOnClickListener { finish() }
    }
}
