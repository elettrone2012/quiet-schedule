package io.github.elettrone2012.quietschedule.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Schedule(
    val daysOfWeek: Set<DayOfWeek>,
    val startMinute: Int,
    val endMinute: Int
) {
    init {
        require(daysOfWeek.isNotEmpty()) {
            "At least one day of week must be selected"
        }

        require(startMinute in 0 until MINUTES_PER_DAY) {
            "Start minute must be between 0 and 1439"
        }

        require(endMinute in 1..MINUTES_PER_DAY) {
            "End minute must be between 1 and 1440"
        }

        require(endMinute > startMinute) {
            "End time must be after start time"
        }

        require(
            !(startMinute == 0 &&
                    endMinute == MINUTES_PER_DAY)
        ) {
            "Full-day schedules are not supported"
        }
    }

    val startTime: LocalTime
        get() = minuteToLocalTime(startMinute)

    val endTime: LocalTime
        get() = minuteToLocalTime(endMinute)

    val endsAtEndOfDay: Boolean
        get() = endMinute == MINUTES_PER_DAY

    companion object {
        const val MINUTES_PER_DAY = 24 * 60

        fun minuteOfDay(
            time: LocalTime
        ): Int {
            return time.hour * 60 + time.minute
        }

        fun minuteToLocalTime(
            minute: Int
        ): LocalTime {
            require(minute in 0..MINUTES_PER_DAY)

            return if (minute == MINUTES_PER_DAY) {
                LocalTime.MIDNIGHT
            } else {
                LocalTime.of(
                    minute / 60,
                    minute % 60
                )
            }
        }
    }
}