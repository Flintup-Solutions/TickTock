package com.ticktock.ui

import java.util.Locale

data class Time12h(
    val hour: Int,
    val minute: Int,
    val isPm: Boolean,
) {
    init {
        require(hour in 1..12) { "Hour must be 1–12" }
        require(minute in 0..59) { "Minute must be 0–59" }
    }

    fun toMinutesOfDay(): Int = to24Hour(hour, isPm) * 60 + minute
}

fun to24Hour(hour12: Int, isPm: Boolean): Int {
    return when {
        hour12 == 12 && !isPm -> 0
        hour12 == 12 && isPm -> 12
        isPm -> hour12 + 12
        else -> hour12
    }
}

fun from24Hour(hour24: Int, minute: Int): Time12h {
    val isPm = hour24 >= 12
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return Time12h(hour = hour12, minute = minute, isPm = isPm)
}

fun formatTime12h(hour24: Int, minute: Int): String {
    val time = from24Hour(hour24, minute)
    val period = if (time.isPm) "PM" else "AM"
    return String.format(Locale.getDefault(), "%d:%02d %s", time.hour, time.minute, period)
}

fun isEndAfterStart(
    startHour: Int,
    startMinute: Int,
    startIsPm: Boolean,
    endHour: Int,
    endMinute: Int,
    endIsPm: Boolean,
): Boolean {
    val start = Time12h(startHour, startMinute, startIsPm).toMinutesOfDay()
    val end = Time12h(endHour, endMinute, endIsPm).toMinutesOfDay()
    return end > start
}
