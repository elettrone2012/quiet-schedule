package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Profile
import java.time.LocalDateTime

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
            profile.schedules.asSequence().flatMap { schedule ->
                schedule.daysOfWeek.asSequence().flatMap { day ->

                    val startDateTime = now
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                        .with(schedule.startTime)
                        .let { candidate ->
                            if (candidate > now) {
                                candidate
                            } else {
                                candidate.plusWeeks(1)
                            }
                        }

                    val endDateTime = now
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                        .with(schedule.endTime)
                        .let { candidate ->
                            if (candidate > now) {
                                candidate
                            } else {
                                candidate.plusWeeks(1)
                            }
                        }

                    sequenceOf(
                        ScheduleEvent(
                            profile = profile,
                            dateTime = startDateTime,
                            type = ScheduleEventType.START
                        ),
                        ScheduleEvent(
                            profile = profile,
                            dateTime = endDateTime,
                            type = ScheduleEventType.END
                        )
                    )
                }
            }
        }
        .minByOrNull { it.dateTime }
}