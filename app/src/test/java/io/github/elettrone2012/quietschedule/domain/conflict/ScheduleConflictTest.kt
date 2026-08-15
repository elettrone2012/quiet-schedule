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
            startTime = LocalTime.of(6, 0),
            endTime = LocalTime.of(8, 0)
        )

        val second = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0)
        )

        val conflict = findConflict(first, second)

        assertNull(conflict)
    }

    @Test
    fun overlappingSchedulesConflict() {
        val first = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startTime = LocalTime.of(6, 0),
            endTime = LocalTime.of(8, 1)
        )

        val second = Schedule(
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0)
        )

        val conflict = findConflict(first, second)

        assertEquals(DayOfWeek.MONDAY, conflict?.dayOfWeek)
        assertEquals(LocalTime.of(8, 0), conflict?.overlapStart)
        assertEquals(LocalTime.of(8, 1), conflict?.overlapEnd)
    }
}