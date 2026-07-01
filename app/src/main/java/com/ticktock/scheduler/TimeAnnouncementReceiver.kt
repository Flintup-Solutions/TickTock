package com.ticktock.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.ticktock.data.ProfileDatabase
import com.ticktock.tts.TimeAnnouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimeAnnouncementReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1L)
        if (profileId == -1L) return

        val pendingResult = goAsync()
        val wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TickTock::Announcement")
            .apply { setReferenceCounted(false) }

        wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val profile = ProfileDatabase.getInstance(context)
                    .profileDao()
                    .getById(profileId)

                if (profile != null) {
                    TimeAnnouncer.announceCurrentTime(context)
                    AlarmScheduler(context).scheduleNextSlot(profile)
                }
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_PROFILE_ID = "profile_id"
        private const val WAKE_LOCK_TIMEOUT_MS = 30_000L
    }
}
