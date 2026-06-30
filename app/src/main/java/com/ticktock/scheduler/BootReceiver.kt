package com.ticktock.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ticktock.data.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                ProfileRepository.create(context).rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
