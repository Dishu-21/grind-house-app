package com.grindhouse.focus

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Fires every time the foreground window changes (i.e. every app switch).
 * If a focus session is active and the newly-foregrounded app is on the
 * blocklist, immediately launch BlockedActivity on top of it, which redirects
 * back into Grind House. This does NOT read screen content - just the
 * package name of whatever came to the foreground.
 */
class BlockAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app and the system launcher/UI - never block those.
        if (packageName == applicationContext.packageName) return
        if (packageName.contains("launcher") || packageName == "com.android.systemui") return

        if (!FocusSessionManager.isActive(applicationContext)) return
        if (packageName !in FocusSessionManager.blockedPackages(applicationContext)) return

        val intent = Intent(this, BlockedActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockedActivity.EXTRA_BLOCKED_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() { /* required override, nothing to clean up */ }
}
