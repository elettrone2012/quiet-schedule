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
    val commonDays =
        first.daysOfWeek.intersect(
            second.daysOfWeek
        )

    for (day in commonDays) {
        val overlapStartMinute =
            maxOf(
                first.startMinute,
                second.startMinute
            )

        val overlapEndMinute =
            minOf(
                first.endMinute,
                second.endMinute
            )

        if (
            overlapStartMinute <
            overlapEndMinute
        ) {
            return ScheduleConflict(
                dayOfWeek = day,
                overlapStart =
                    Schedule.minuteToLocalTime(
                        overlapStartMinute
                    ),
                overlapEnd =
                    Schedule.minuteToLocalTime(
                        overlapEndMinute
                    )
            )
        }
    }

    return null
}