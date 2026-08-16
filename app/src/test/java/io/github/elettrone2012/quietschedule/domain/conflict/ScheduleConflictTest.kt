package io.github.elettrone2012.quietschedule.domain.conflict

import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleConflictTest {

    @Test
    fun adjacentSchedulesDoNotConflict() {
        val first = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 6 * 60,
            endMinute = 8 * 60
        )

        val second = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 8 * 60,
            endMinute = 10 * 60
        )

        val conflict = findConflict(
            first,
            second
        )

        assertNull(conflict)
    }

    @Test
    fun overlappingSchedulesConflict() {
        val first = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 6 * 60,
            endMinute = 8 * 60 + 1
        )

        val second = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 8 * 60,
            endMinute = 10 * 60
        )

        val conflict = findConflict(
            first,
            second
        )

        assertEquals(
            DayOfWeek.MONDAY,
            conflict?.dayOfWeek
        )

        assertEquals(
            LocalTime.of(8, 0),
            conflict?.overlapStart
        )

        assertEquals(
            LocalTime.of(8, 1),
            conflict?.overlapEnd
        )
    }

    @Test
    fun scheduleEndingAtMidnightConflictsWithLateSchedule() {
        val first = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 22 * 60,
            endMinute = Schedule.MINUTES_PER_DAY
        )

        val second = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 23 * 60,
            endMinute = 23 * 60 + 30
        )

        val conflict = findConflict(
            first,
            second
        )

        assertEquals(
            DayOfWeek.MONDAY,
            conflict?.dayOfWeek
        )

        assertEquals(
            LocalTime.of(23, 0),
            conflict?.overlapStart
        )

        assertEquals(
            LocalTime.of(23, 30),
            conflict?.overlapEnd
        )
    }

    @Test
    fun scheduleEndingAtMidnightIsAdjacentToNextDaysMidnightStart() {
        val mondayEvening = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startMinute = 22 * 60,
            endMinute = Schedule.MINUTES_PER_DAY
        )

        val tuesdayMorning = Schedule(
            daysOfWeek = setOf(DayOfWeek.TUESDAY),
            startMinute = 0,
            endMinute = 8 * 60
        )

        val conflict = findConflict(
            mondayEvening,
            tuesdayMorning
        )

        assertNull(conflict)
    }
}