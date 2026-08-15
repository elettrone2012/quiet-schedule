package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Profile
import java.time.DayOfWeek
import java.time.LocalTime

fun Profile.isActiveAt(
    dayOfWeek: DayOfWeek,
    time: LocalTime
): Boolean {
    if (!enabled) {
        return false
    }

    return schedules.any { schedule ->
        schedule.isActiveAt(
            dayOfWeek = dayOfWeek,
            time = time
        )
    }
}
