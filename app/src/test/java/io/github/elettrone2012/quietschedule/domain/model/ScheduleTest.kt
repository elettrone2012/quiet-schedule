package io.github.elettrone2012.quietschedule.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleTest {

    @Test
    fun validSameDayScheduleIsAccepted() {
        val schedule = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 8 * 60,
            endMinute = 17 * 60
        )

        assertEquals(
            8 * 60,
            schedule.startMinute
        )

        assertEquals(
            17 * 60,
            schedule.endMinute
        )
    }

    @Test
    fun scheduleEndingAtMidnightIsAccepted() {
        val schedule = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 22 * 60,
            endMinute = Schedule.MINUTES_PER_DAY
        )

        assertEquals(
            22 * 60,
            schedule.startMinute
        )

        assertEquals(
            Schedule.MINUTES_PER_DAY,
            schedule.endMinute
        )

        assertTrue(
            schedule.endsAtEndOfDay
        )

        assertEquals(
            LocalTime.MIDNIGHT,
            schedule.endTime
        )
    }

    @Test
    fun midnightStartMorningScheduleIsAccepted() {
        val schedule = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 0,
            endMinute = 8 * 60
        )

        assertEquals(
            LocalTime.MIDNIGHT,
            schedule.startTime
        )

        assertFalse(
            schedule.endsAtEndOfDay
        )
    }

    @Test
    fun overnightScheduleIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Schedule(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                startMinute = 22 * 60,
                endMinute = 7 * 60
            )
        }
    }

    @Test
    fun zeroLengthScheduleIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Schedule(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                startMinute = 8 * 60,
                endMinute = 8 * 60
            )
        }
    }

    @Test
    fun fullDayScheduleIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Schedule(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                startMinute = 0,
                endMinute = Schedule.MINUTES_PER_DAY
            )
        }
    }

    @Test
    fun scheduleWithoutDaysIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Schedule(
                daysOfWeek = emptySet(),
                startMinute = 8 * 60,
                endMinute = 17 * 60
            )
        }
    }

    @Test
    fun invalidStartMinuteIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Schedule(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                startMinute = Schedule.MINUTES_PER_DAY,
                endMinute = Schedule.MINUTES_PER_DAY
            )
        }
    }

    @Test
    fun invalidEndMinuteIsRejected() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            Schedule(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                startMinute = 8 * 60,
                endMinute = Schedule.MINUTES_PER_DAY + 1
            )
        }
    }

    @Test
    fun minuteConversionWorks() {
        assertEquals(
            0,
            Schedule.minuteOfDay(
                LocalTime.MIDNIGHT
            )
        )

        assertEquals(
            8 * 60 + 30,
            Schedule.minuteOfDay(
                LocalTime.of(8, 30)
            )
        )

        assertEquals(
            LocalTime.of(22, 15),
            Schedule.minuteToLocalTime(
                22 * 60 + 15
            )
        )

        assertEquals(
            LocalTime.MIDNIGHT,
            Schedule.minuteToLocalTime(
                Schedule.MINUTES_PER_DAY
            )
        )
    }
}