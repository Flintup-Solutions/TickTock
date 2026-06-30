package com.ticktock.data

import android.content.Context
import com.ticktock.scheduler.AlarmScheduler

class ProfileRepository(
    private val dao: ProfileDao,
    private val alarmScheduler: AlarmScheduler,
) {
    suspend fun getAllProfiles(): List<TimeAlertProfile> = dao.getAll()

    suspend fun addProfile(profile: TimeAlertProfile): Long {
        val id = dao.insert(profile)
        val saved = profile.copy(id = id)
        alarmScheduler.scheduleProfile(saved)
        return id
    }

    suspend fun deleteProfile(profile: TimeAlertProfile) {
        alarmScheduler.cancelProfile(profile.id)
        dao.delete(profile)
    }

    suspend fun rescheduleAll() {
        dao.getAll().forEach { alarmScheduler.scheduleProfile(it) }
    }

    companion object {
        fun create(context: Context): ProfileRepository {
            val db = ProfileDatabase.getInstance(context)
            val scheduler = AlarmScheduler(context)
            return ProfileRepository(db.profileDao(), scheduler)
        }
    }
}
