package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ProfileActivityTest {

    private val schedule = Schedule(
        daysOfWeek = setOf(DayOfWeek.MONDAY),
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0)
    )

    @Test
    fun enabledProfileInsideScheduleIsActive() {
        val profile = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(schedule)
        )

        assertTrue(
            profile.isActiveAt(
                dayOfWeek = DayOfWeek.MONDAY,
                time = LocalTime.of(12, 0)
            )
        )
    }

    @Test
    fun disabledProfileInsideScheduleIsNotActive() {
        val profile = Profile(
            name = "Work",
            enabled = false,
            schedules = listOf(schedule)
        )

        assertFalse(
            profile.isActiveAt(
                dayOfWeek = DayOfWeek.MONDAY,
                time = LocalTime.of(12, 0)
            )
        )
    }

    @Test
    fun enabledProfileOutsideScheduleIsNotActive() {
        val profile = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(schedule)
        )

        assertFalse(
            profile.isActiveAt(
                dayOfWeek = DayOfWeek.MONDAY,
                time = LocalTime.of(18, 0)
            )
        )
    }
}