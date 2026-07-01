package com.ticktock.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ticktock.MainActivity
import com.ticktock.data.TimeAlertProfile
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleProfile(profile: TimeAlertProfile) {
        scheduleAt(profile, SlotCalculator.nextSlot(profile))
    }

    fun scheduleNextSlot(profile: TimeAlertProfile) {
        val justAnnounced = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        scheduleAt(profile, SlotCalculator.nextSlotAfter(profile, justAnnounced))
    }

    private fun scheduleAt(profile: TimeAlertProfile, nextSlot: ZonedDateTime?) {
        cancelProfile(profile.id)
        if (nextSlot == null) return
        val triggerAtMillis = nextSlot.toInstant().toEpochMilli()
        val pendingIntent = createPendingIntent(profile.id)
        val showIntent = PendingIntent.getActivity(
            context,
            profile.id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
            pendingIntent,
        )
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
