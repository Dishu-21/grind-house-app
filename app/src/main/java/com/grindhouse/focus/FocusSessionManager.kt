package com.grindhouse.focus

import android.content.Context

/**
 * Single source of truth for the current focus session.
 * Read/written from MainActivity, FocusModeActivity, the accessibility service,
 * and the foreground notification service - all through this one object so
 * they never fall out of sync with each other.
 */
object FocusSessionManager {
    private const val PREFS = "focus_session_prefs"
    private const val KEY_END_TIME = "end_time_millis"
    private const val KEY_BLOCKED_APPS = "blocked_package_names"

    fun startSession(context: Context, durationMinutes: Int, blockedPackages: Set<String>) {
        val endTime = System.currentTimeMillis() + durationMinutes * 60_000L
        prefs(context).edit()
            .putLong(KEY_END_TIME, endTime)
            .putStringSet(KEY_BLOCKED_APPS, blockedPackages)
            .apply()
    }

    fun endSession(context: Context) {
        prefs(context).edit()
            .putLong(KEY_END_TIME, 0L)
            .apply()
        // Deliberately leaves KEY_BLOCKED_APPS in place so "start again" remembers the last picklist.
    }

    fun isActive(context: Context): Boolean {
        return remainingMillis(context) > 0
    }

    fun remainingMillis(context: Context): Long {
        val end = prefs(context).getLong(KEY_END_TIME, 0L)
        return (end - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun blockedPackages(context: Context): Set<String> {
        return prefs(context).getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
