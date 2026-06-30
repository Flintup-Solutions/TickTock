package com.ticktock.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ticktock.data.ProfileDatabase
import com.ticktock.tts.TimeAnnouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimeAnnouncementReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1L)
        if (profileId == -1L) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                val dao = ProfileDatabase.getInstance(context).profileDao()
                val profile = dao.getById(profileId) ?: return@launch

                if (SlotCalculator.shouldAnnounceNow(profile)) {
                    TimeAnnouncer.announceCurrentTime(context)
                }

                AlarmScheduler(context).scheduleProfile(profile)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_PROFILE_ID = "profile_id"
    }
}
