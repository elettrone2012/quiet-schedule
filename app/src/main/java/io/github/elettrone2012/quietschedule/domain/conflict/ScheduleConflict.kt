package io.github.elettrone2012.quietschedule.domain.conflict

import io.github.elettrone2012.quietschedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalTime

data class ScheduleConflict(
    val dayOfWeek: DayOfWeek,
    val overlapStart: LocalTime,
    val overlapEnd: LocalTime
)

fun findConflict(
    first: Schedule,
    second: Schedule
): ScheduleConflict? {
    val commonDays = first.daysOfWeek.intersect(second.daysOfWeek)

    for (day in commonDays) {
        val overlapStart = maxOf(first.startTime, second.startTime)
        val overlapEnd = minOf(first.endTime, second.endTime)

        if (overlapStart < overlapEnd) {
            return ScheduleConflict(
                dayOfWeek = day,
                overlapStart = overlapStart,
                overlapEnd = overlapEnd
            )
        }
    }

    return null
}