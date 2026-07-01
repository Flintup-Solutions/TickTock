package com.ticktock.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class TimeAnnouncementReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1L)
        if (profileId == -1L) return

        val serviceIntent = Intent(context, AnnouncementService::class.java).apply {
            putExtra(EXTRA_PROFILE_ID, profileId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    companion object {
        const val EXTRA_PROFILE_ID = "profile_id"
    }
}
