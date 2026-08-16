package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

data class ScheduleEvent(
    val profile: Profile,
    val dateTime: LocalDateTime,
    val type: ScheduleEventType
)

enum class ScheduleEventType {
    START,
    END
}

fun findNextScheduleEvent(
    profiles: List<Profile>,
    now: LocalDateTime
): ScheduleEvent? {
    return profiles
        .asSequence()
        .filter { it.enabled }
        .flatMap { profile ->
            profile.schedules
                .asSequence()
                .flatMap { schedule ->
                    schedule.daysOfWeek
                        .asSequence()
                        .flatMap { day ->

                            val startDateTime =
                                nextBoundary(
                                    day = day,
                                    minuteOfDay =
                                        schedule.startMinute,
                                    now = now
                                )

                            val endDateTime =
                                nextBoundary(
                                    day = day,
                                    minuteOfDay =
                                        schedule.endMinute,
                                    now = now
                                )

                            sequenceOf(
                                ScheduleEvent(
                                    profile = profile,
                                    dateTime =
                                        startDateTime,
                                    type =
                                        ScheduleEventType.START
                                ),
                                ScheduleEvent(
                                    profile = profile,
                                    dateTime =
                                        endDateTime,
                                    type =
                                        ScheduleEventType.END
                                )
                            )
                        }
                }
        }
        .minByOrNull {
            it.dateTime
        }
}

private fun nextBoundary(
    day: DayOfWeek,
    minuteOfDay: Int,
    now: LocalDateTime
): LocalDateTime {

    val scheduleDate =
        now.toLocalDate()
            .with(
                TemporalAdjusters
                    .nextOrSame(day)
            )

    val candidate =
        if (
            minuteOfDay ==
            Schedule.MINUTES_PER_DAY
        ) {
            scheduleDate
                .plusDays(1)
                .atStartOfDay()
        } else {
            scheduleDate.atTime(
                LocalTime.of(
                    minuteOfDay / 60,
                    minuteOfDay % 60
                )
            )
        }

    return if (candidate > now) {
        candidate
    } else {
        candidate.plusWeeks(1)
    }
}