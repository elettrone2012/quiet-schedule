package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalTime

fun Schedule.isActiveAt(
    dayOfWeek: DayOfWeek,
    time: LocalTime
): Boolean {
    if (dayOfWeek !in daysOfWeek) {
        return false
    }

    val currentMinute =
        Schedule.minuteOfDay(time)

    return currentMinute >= startMinute &&
            currentMinute < endMinute
}