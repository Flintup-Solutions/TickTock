package com.ticktock.scheduler

import com.ticktock.data.TimeAlertProfile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object SlotCalculator {
    fun slotsForDay(date: LocalDate, profile: TimeAlertProfile): List<LocalDateTime> {
        val start = LocalTime.of(profile.startHour, profile.startMinute)
        val end = LocalTime.of(profile.endHour, profile.endMinute)
        if (!end.isAfter(start)) {
            return emptyList()
        }

        val slots = mutableListOf<LocalDateTime>()
        var current = start
        while (!current.isAfter(end)) {
            slots.add(date.atTime(current))
            current = current.plusMinutes(profile.frequencyMinutes.toLong())
        }
        return slots
    }

    fun nextSlot(profile: TimeAlertProfile, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime? {
        val zone = now.zone
        val truncatedNow = now.truncatedTo(ChronoUnit.MINUTES)

        val upcomingToday = slotsForDay(truncatedNow.toLocalDate(), profile)
            .map { it.atZone(zone) }
            .filter { !it.isBefore(truncatedNow) }

        if (upcomingToday.isNotEmpty()) {
            return upcomingToday.first()
        }

        val tomorrow = truncatedNow.toLocalDate().plusDays(1)
        return slotsForDay(tomorrow, profile)
            .firstOrNull()
            ?.atZone(zone)
    }

    fun nextSlotAfter(
        profile: TimeAlertProfile,
        after: ZonedDateTime,
    ): ZonedDateTime? {
        val zone = after.zone
        val truncatedAfter = after.truncatedTo(ChronoUnit.MINUTES)

        val laterToday = slotsForDay(truncatedAfter.toLocalDate(), profile)
            .map { it.atZone(zone) }
            .filter { it.isAfter(truncatedAfter) }

        if (laterToday.isNotEmpty()) {
            return laterToday.first()
        }

        val tomorrow = truncatedAfter.toLocalDate().plusDays(1)
        return slotsForDay(tomorrow, profile)
            .firstOrNull()
            ?.atZone(zone)
    }
}
