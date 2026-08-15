package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleActivityTest {

    private val schedule = Schedule(
        daysOfWeek = setOf(DayOfWeek.MONDAY),
        startTime = LocalTime.of(8, 0),
        endTime = LocalTime.of(10, 0)
    )

    @Test
    fun startTimeIsIncluded() {
        assertTrue(
            schedule.isActiveAt(
                dayOfWeek = DayOfWeek.MONDAY,
                time = LocalTime.of(8, 0)
            )
        )
    }

    @Test
    fun endTimeIsExcluded() {
        assertFalse(
            schedule.isActiveAt(
                dayOfWeek = DayOfWeek.MONDAY,
                time = LocalTime.of(10, 0)
            )
        )
    }

    @Test
    fun timeInsideRangeIsActive() {
        assertTrue(
            schedule.isActiveAt(
                dayOfWeek = DayOfWeek.MONDAY,
                time = LocalTime.of(9, 0)
            )
        )
    }

    @Test
    fun differentDayIsNotActive() {
        assertFalse(
            schedule.isActiveAt(
                dayOfWeek = DayOfWeek.TUESDAY,
                time = LocalTime.of(9, 0)
            )
        )
    }
}