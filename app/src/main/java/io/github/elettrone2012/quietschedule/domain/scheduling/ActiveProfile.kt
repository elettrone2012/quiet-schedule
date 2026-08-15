package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Profile
import java.time.DayOfWeek
import java.time.LocalTime

fun findActiveProfile(
    profiles: List<Profile>,
    dayOfWeek: DayOfWeek,
    time: LocalTime
): Profile? {
    return profiles.firstOrNull { profile ->
        profile.isActiveAt(
            dayOfWeek = dayOfWeek,
            time = time
        )
    }
}