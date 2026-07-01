package com.ticktock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_alert_profiles")
data class TimeAlertProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val frequencyMinutes: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
)
