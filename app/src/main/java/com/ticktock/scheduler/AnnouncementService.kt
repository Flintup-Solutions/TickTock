package com.ticktock.scheduler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.ticktock.MainActivity
import com.ticktock.R
import com.ticktock.data.ProfileDatabase
import com.ticktock.tts.TimeAnnouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AnnouncementService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val profileId = intent?.getLongExtra(TimeAnnouncementReceiver.EXTRA_PROFILE_ID, -1L) ?: -1L
        if (profileId == -1L) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        createNotificationChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TickTock::Announcement")
            .apply { setReferenceCounted(false) }

        wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)

        serviceScope.launch {
            try {
                val profile = ProfileDatabase.getInstance(this@AnnouncementService)
                    .profileDao()
                    .getById(profileId)

                if (profile != null) {
                    TimeAnnouncer.announceCurrentTime(this@AnnouncementService)
                    AlarmScheduler(this@AnnouncementService).scheduleProfile(profile)
                }
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val launchIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_announcement_title))
            .setContentText(getString(R.string.notification_announcement_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(launchIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "ticktock_announcements"
        private const val NOTIFICATION_ID = 1001
        private const val WAKE_LOCK_TIMEOUT_MS = 60_000L
    }
}
