package io.github.elettrone2012.quietschedule.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleTest {

    @Test
    fun validSameDayScheduleIsAccepted() {
        Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(17, 0)
        )
    }

    @Test
    fun overnightScheduleIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Schedule(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                startTime = LocalTime.of(22, 0),
                endTime = LocalTime.of(7, 0)
            )
        }
    }

    @Test
    fun zeroLengthScheduleIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Schedule(
                daysOfWeek = setOf(DayOfWeek.MONDAY),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(8, 0)
            )
        }
    }

    @Test
    fun scheduleWithoutDaysIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Schedule(
                daysOfWeek = emptySet(),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(17, 0)
            )
        }
    }
}