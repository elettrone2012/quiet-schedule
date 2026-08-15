package io.github.elettrone2012.quietschedule.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Schedule(
    val daysOfWeek: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    init {
        require(daysOfWeek.isNotEmpty()) {
            "At least one day of week must be selected"
        }

        require(endTime > startTime) {
            "End time must be after start time"
        }
    }
}