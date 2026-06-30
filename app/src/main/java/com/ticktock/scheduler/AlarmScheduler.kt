package com.ticktock.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ticktock.data.TimeAlertProfile

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleProfile(profile: TimeAlertProfile) {
        cancelProfile(profile.id)
        val nextSlot = SlotCalculator.nextSlot(profile) ?: return
        val triggerAtMillis = nextSlot.toInstant().toEpochMilli()
        val pendingIntent = createPendingIntent(profile.id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    fun cancelProfile(profileId: Long) {
        alarmManager.cancel(createPendingIntent(profileId))
    }

    private fun createPendingIntent(profileId: Long): PendingIntent {
        val intent = Intent(context, TimeAnnouncementReceiver::class.java).apply {
            putExtra(TimeAnnouncementReceiver.EXTRA_PROFILE_ID, profileId)
        }
        return PendingIntent.getBroadcast(
            context,
            profileId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
