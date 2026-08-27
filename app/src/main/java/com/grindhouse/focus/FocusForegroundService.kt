package com.grindhouse.focus

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Keeps a low-priority "Focus session - Xm left" notification visible for the
 * whole session, with an End button, and stops itself automatically when time
 * runs out. This is what makes the block feel intentional rather than a
 * silent background thing the user forgot was running.
 */
class FocusForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "focus_session_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_END = "com.grindhouse.focus.action.END_SESSION"
    }

    private var timer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_END) {
            endSession()
            return START_NOT_STICKY
        }

        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification(FocusSessionManager.remainingMillis(this)))
        startTicking()
        return START_STICKY
    }

    private fun startTicking() {
        timer?.cancel()
        val remaining = FocusSessionManager.remainingMillis(this)
        if (remaining <= 0) { endSession(); return }

        timer = object : CountDownTimer(remaining, 30_000L) {
            override fun onTick(msUntilFinished: Long) {
                val notification = buildNotification(FocusSessionManager.remainingMillis(applicationContext))
                getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
            }
            override fun onFinish() { endSession() }
        }.start()
    }

    private fun endSession() {
        FocusSessionManager.endSession(applicationContext)
        timer?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(remainingMillis: Long): Notification {
        val minutes = (remainingMillis / 60_000L).toInt().coerceAtLeast(0)

        val endIntent = Intent(this, FocusForegroundService::class.java).apply { action = ACTION_END }
        val endPendingIntent = PendingIntent.getService(
            this, 0, endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("🔥 Focus session active")
            .setContentText("$minutes min left · distracting apps are blocked")
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, "End session", endPendingIntent)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Focus Session", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
