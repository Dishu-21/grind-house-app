package com.grindhouse.focus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grindhouse.focus.databinding.ActivityBlockedBinding

class BlockedActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
    }

    private lateinit var binding: ActivityBlockedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blockedPkg = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE)
        val remaining = FocusSessionManager.remainingMillis(applicationContext)
        val minutesLeft = (remaining / 60_000L).toInt().coerceAtLeast(0)

        binding.blockedSubtitle.text = if (blockedPkg != null) {
            "That app is off-limits during your focus session.\n$minutesLeft min left."
        } else {
            "$minutesLeft min left in your focus session."
        }

        binding.backToGrindHouseBtn.setOnClickListener {
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // Disallow the physical/gesture back button from revealing the blocked app underneath.
    override fun onBackPressed() {
        startActivity(android.content.Intent(this, MainActivity::class.java))
        finish()
    }
}
